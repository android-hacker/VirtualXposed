# HookZz

画了一半的流程, 其实另一半还有 invoke trampoline, leave trampoline, 大致相同.

```
                 enter            enter
function         trampoline       thunk
+------------+   +------------+   +------------+
| hook patch +--->            +--->            |
+------------+   |            |   | storeReg   |
|            |   |            |   |            |
|            |   +------------+   | prep args  |
|            |                    |            |
|            |                    | call func  <---+begin_invocation_func()
|            |                    |            |
+------------+                    | restoreReg |
                                  |            |
                                  +------------+

```

# HookFramework 架构设计 2.0

一般来说可以分为以下几个模块

1. 内存分配 模块
2. 指令写 模块
3. 指令读 模块
4. 指令修复 模块
5. 跳板 模块
6. 调度器 模块
7. 栈 模块

## 1. 内存分配 模块

这里主要关注三个点:

1. 内存的分配
2. 内存属性修改
3. 内存布局获取

#### 1.1 内存的分配

**设计方面:** 1. 提供一个 allocator 去管理/分配内存 2. 需要封装成架构无关的 API.

通常使用 posix 标准的 `mmap`, darwin 下的 mach kernel 分配内存使用 `mmap` 实际使用的是 `mach_vm_allocate`. [move to detail]( https://github.com/bminor/glibc/blob/master/sysdeps/mach/hurd/mmap.c)

在入口点的 patch, 通常会使用绝对地址跳到 `trampoline`, 如果使用绝对地址跳, 将会占用 4 条指令, 如下的形式.

```
ldr x17, #0x8
b #0xc
.long 0x0
.long 0x0
br x17
```

但是如果可以使用 `B #0x?`, 实现相对地址跳(near jump), 将是最好的, 在 armv8 中的可以想在 `+-128MB` 范围内进行 `near jump`, 具体可以参考 `ARM Architecture Reference Manual ARMv8, for ARMv8-A architecture profile Page: C6-550`. 所以问题转换为找到一块 `rx-` 的内存写入 enter trampline. 

大概有以下几种方法可以获取到 `rx-` 内存块.

1. 尝试使用 mmap 的 fixed flag 分配相近内存

2. 当时获取进程内的所有动态库列表, 之后搜索每一个动态库的 `__TEXT`, 查找是否存在 code cave.(尝试搜索内存空洞(`code cave`), 搜索 `__text` 这个 `section` 其实更准确来说是搜索 `__TEXT` 这个 `segment`. 由于内存页对齐的原因以及其他原因很容易出现 `code cave`. 所以只需要搜索这个区间内的 `00` 即可, `00` 本身就是无效指令, 所以可以判断该位置无指令使用.)

3. 获取当前进程的内存布局, 对所有 `rx-` 属性内存页搜索 code cave. (内存布局的获取会在1.3详细提到)

```
__asm__ {
	// 第一次绝对地址跳, 跳转到修复模块, 执行正常流程
	"ldr x17, #0x8\n"
	"b #0xc\n"
	".long\n"
	".long\n"
	"br x17"

	// double jump, 跳转到 on_enter_trampoline
	"ldr x17, #0x8\n"
	"b #0xc\n"
	".long\n"
	".long\n"
	"br x17"
}
```

#### 1.2 内存属性修改

通常使用 posix 标准的 `mprotect`, darwin 下的 mach kernel 修改内存属性使用的是 `mach_vm_protect`, 注意: ios 不允许引用 `#include <mach_vm.h>`, 可以用单独拷贝一份该头文件到项目下.

这一部分与具体的操作系统有关. 比如 .

在 lldb 中可以通过 `memory region address` 查看地址的内存属性.

当然这里也存在一个巨大的坑, ios 下无法分配 `rwx` 属性的内存页. 这导致 inlinehook 无法在非越狱系统上使用, 并且只有 `MobileSafari` 才有 `VM_FLAGS_MAP_JIT` 权限. 具体解释请参下方 **[坑 - rwx 与 codesigning]**.

#### 1.3 内存布局获取

linux 下可以通过 `/proc/pid/maps` 获取当前进程的内存, 而且也仅有此方法.

darwin 下有 `vmmap` 这个命令可以在 `macOS` 使用, darwin 下 pid 与 `task_t`, 所以具体的内存页管理都在 `task_t` 这个内核结构体里. 但是可以通过 `vm_region` 函数遍历获取当前进程的所有属性的内存页. 通常用来爆破 dyld 的地址


#### 2. 指令写 模块

先说坑,  非越狱状态下不允许设置 `rw-` 为 `r-x`, 或者  设置 `r-x` 为 `rx-`. 具体解释请参考下方坑 **[坑-rwx 与 codesigning]**.

其实这里的指令写有种简单的方法, 就是在本地生成指令的16进制串, 之后直接写即可. 但这种应该是属于 hardcode.

这里使用 `frida-gum` 和 `CydiaSubstrace` 都用的方法, 把需要用到的指令都写成一个小函数.

例如:

```
// frida-gum/gum/arch-arm64/gumarm64writer.c
void
gum_arm64_zz_arm64_writer_put_ldr_reg_address (GumArm64Writer * self,
                                      ZzARM64Reg reg,
                                      GumAddress address)
{
  gum_arm64_writer_put_ldr_reg_u64 (self, reg, (zuint64) address);
}

void
gum_arm64_writer_put_ldr_reg_u64 (GumArm64Writer * self,
                                  ZzARM64Reg reg,
                                  zuint64 val)
{
  GumArm64RegInfo ri;

  gum_arm64_zz_arm64_writer_describe_reg (self, reg, &ri);

  g_assert_cmpuint (ri.width, ==, 64);

  gum_arm64_writer_add_literal_reference_here (self, val);
  gum_arm64_zz_arm64_writer_put_instruction (self,
      (ri.is_integer ? 0x58000000 : 0x5c000000) | ri.index);
}

```

其实有另外一个小思路,  有一点小不足, 就是确定指令片段的长度, 但其实也有解决方法, **可以放几条特殊指令作为结尾标记**.

先使用内联汇编写一个函数.

```

__attribute__((__naked__)) static void ctx_save() {
  __asm__ volatile(

      /* reserve space for next_hop */
      "sub sp, sp, #(2*8)\n"

      /* save {q0-q7} */
      "sub sp, sp, #(8*16)\n"
      "stp q6, q7, [sp, #(6*16)]\n"
      "stp q4, q5, [sp, #(4*16)]\n"
      "stp q2, q3, [sp, #(2*16)]\n"
      "stp q0, q1, [sp, #(0*16)]\n"

      /* save {x1-x30} */
      "sub sp, sp, #(30*8)\n"
      "stp fp, lr, [sp, #(28*8)]\n"
      "stp x27, x28, [sp, #(26*8)]\n"
      "stp x25, x26, [sp, #(24*8)]\n"
      "stp x23, x24, [sp, #(22*8)]\n"
      "stp x21, x22, [sp, #(20*8)]\n"
      "stp x19, x20, [sp, #(18*8)]\n"
      "stp x17, x18, [sp, #(16*8)]\n"
      "stp x15, x16, [sp, #(14*8)]\n"
      "stp x13, x14, [sp, #(12*8)]\n"
      "stp x11, x12, [sp, #(10*8)]\n"
      "stp x9, x10, [sp, #(8*8)]\n"
      "stp x7, x8, [sp, #(6*8)]\n"
      "stp x5, x6, [sp, #(4*8)]\n"
      "stp x3, x4, [sp, #(2*8)]\n"
      "stp x1, x2, [sp, #(0*8)]\n"

      /* save sp, x0 */
      "sub sp, sp, #(2*8)\n"
      "add x1, sp, #(2*8 + 8*16 + 30*8 + 2*8)\n"
      "stp x1, x0, [sp, #(0*8)]\n"

      /* alignment padding + dummy PC */
      "sub sp, sp, #(2*8)\n");
}

```

之后直接复制这块函数内存数据即可, 这一般适合那种指令片段堆.

```
void ZzThunkerBuildEnterThunk(ZzWriter *writer)
{

    // pop x17
    zz_arm64_writer_put_ldr_reg_reg_offset(writer, ZZ_ARM64_REG_X17, ZZ_ARM64_REG_SP, 0);
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 16);

    zz_arm64_writer_put_bytes(writer, (void *)ctx_save, 26 * 4);

    // call `function_context_begin_invocation`
    zz_arm64_writer_put_bytes(writer, (void *)pass_enter_func_args, 4 * 4);
    zz_arm64_writer_put_ldr_reg_address(
        writer, ZZ_ARM64_REG_X17,
        (zaddr)(zpointer)function_context_begin_invocation);
    zz_arm64_writer_put_blr_reg(writer, ZZ_ARM64_REG_X17);

    zz_arm64_writer_put_bytes(writer, (void *)ctx_restore, 23 * 4);
}
```

#### 3. 指令读 模块

这一部分实际上就是 `disassembler`, 这一部分可以直接使用 `capstone`, 这里需要把 `capstone` 编译成多种架构.

#### 4. 指令修复 模块

这里的指令修复主要是发生在 hook 函数头几条指令, 由于备份指令到另一个地址, 这就需要对所有 `PC(IP)` 相关指令进行修复. 对于确定的哪些指令需要修复可以参考 [Move to <解析ARM和x86_x64指令格式>](http://jmpews.github.io/2017/05/17/pwn/%E8%A7%A3%E6%9E%90ARM%E5%92%8Cx86_x64%E6%8C%87%E4%BB%A4%E6%A0%BC%E5%BC%8F/).

大致的思路就是: 判断 `capstone` 读取到的指令 ID, 针对特定指令写一个小函数进行修复.

例如在 `frida-gum` 中:

```
frida-gum/gum/arch-arm64/gumarm64relocator.c
static gboolean
gum_arm64_relocator_rewrite_b (GumArm64Relocator * self,
                               GumCodeGenCtx * ctx)
{
  const cs_arm64_op * target = &ctx->detail->operands[0];

  (void) self;

  gum_arm64_zz_arm64_writer_put_ldr_reg_address (ctx->output, ZZ_ARM64_REG_X16,
      target->imm);
  gum_arm64_zz_arm64_writer_put_br_reg (ctx->output, ZZ_ARM64_REG_X16);

  return TRUE;
}
```

#### 5. 跳板 模块

跳板模块的设计是希望各个模块的实现更浅的耦合, 跳板函数主要作用就是进行跳转, 并准备 `跳转目标` 需要的参数. 举个例子, 被 hook 的函数经过入口跳板(`enter_trampoline`), 跳转到调度函数(`enter_chunk`), 需要被 hook 的函数相关信息等, 这个就需要在构造跳板时完成.

#### 6. 调度 模块

可以理解为所有被 hook 的函数都必须经过的函数, 类似于 `objc_msgSend`, 在这里通过栈返回值来控制函数(`replace_call`, `pre_call`, `half_call`, `post_call`)调用顺序.

本质有些类似于 `objc_msgSend` 所有的被 hook 的函数都在经过 `enter_trampoline` 跳板后, 跳转到 `enter_thunk`, 在此进行下一步的跳转判断决定, 并不是直接跳转到 `replace_call`.

#### 7. 栈模块

如果希望在 `pre_call` 和 `post_call`  使用同一个局部变量, 就想在同一个函数内一样. 在 `frida-js` 中也就是 `this` 这个关键字. 这就需要自建函数栈, 模拟栈的行为. 同时还要避免线程冲突, 所以需要使用 `thread local variable`, 为每一个线程中的每一个 `hook-entry` 添加线程栈, 同时为每一次调用添加函数栈. 所以这里存在两种栈. 1. 线程栈(保存了该 hook-entry 的所有当前函数调用栈) 2. 函数调用栈(本次函数调用时的栈)

# 坑

## `ldr` 指令

在进行指令修复时, 需要需要将 PC 相关的地址转换为绝对地址, 其中涉及到保存地址到寄存器. 一般来说是使用指令 `ldr`. 也就是说如何完成该函数 `zz_arm64_writer_put_ldr_reg_address(relocate_writer, ZZ_ARM64_REG_X17, target_addr);`

`frida-gum` 的实现原理是, 有一个相对地址表, 在整体一段写完后进行修复.

```
void
gum_arm64_writer_put_ldr_reg_u64 (GumArm64Writer * self,
                                  ZzARM64Reg reg,
                                  zuint64 val)
{
  GumArm64RegInfo ri;

  gum_arm64_zz_arm64_writer_describe_reg (self, reg, &ri);

  g_assert_cmpuint (ri.width, ==, 64);

  gum_arm64_writer_add_literal_reference_here (self, val);
  gum_arm64_zz_arm64_writer_put_instruction (self,
      (ri.is_integer ? 0x58000000 : 0x5c000000) | ri.index);
}
```

在 HookZz 中的实现, 直接将地址写在指令后, 之后使用 `b` 到正常的下一条指令, 从而实现将地址保存到寄存器.

```
void zz_arm64_writer_put_ldr_reg_address(ZzWriter *self, ZzARM64Reg reg, zaddr address)
{
    zz_arm64_writer_put_ldr_reg_imm(self, reg, (zuint)0x8);
    zz_arm64_writer_put_b_imm(self, (zaddr)0xc);
    zz_arm64_writer_put_bytes(self, (zpointer)&address, sizeof(address));
}
```

也就是下面的样子.

```
__asm__ {
	"ldr x17, #0x8\n"
	"b #0xc\n"
	".long\n"
	".long\n"
	"br x17"
}
```

## 寄存器污染

在进行 inlinehook 需要进行各种跳转, 通常会以以下模板进行跳转.

```
0:  ldr x16, 8;
4:  br x16;
8:  0x12345678
12: 0x00000000
```

问题在于这会造成 x16 寄存器被污染(arm64 中 `svc #0x80` 使用 x16 传递系统调用号) 所以这里有两种思路解决这个问题.

思路一:

在使用寄存器之前进行 `push`, 跳转后 `pop`, 这里存在一个问题就是在原地址的几条指令进行 `patch code` 时一定会污染一个寄存器(也不能说一定, 如果这时进行压栈, 在之后的 `invoke_trampline` 会导致函数栈发生改变, 此时有个解决方法可以 `pop` 出来, 由 hook-entry 或者其他变量暂时保存, 但这时需要处理锁的问题. )

思路二:

挑选合适的寄存器, 不考虑污染问题. 这时可以参考, 下面的资料, 选择 x16 or x17, 或者自己做一个实验 `otool -tv ~/Downloads/DiSpecialDriver64 > ~/Downloads/DiSpecialDriver64.txt` 通过 dump 一个 arm64 程序的指令, 来判断哪个寄存器用的最少, 但是不要使用 `x18` 寄存器, 你对该寄存器的修改是无效的.

Tips: 之前还想过为对每一个寄存器都做适配, 用户可以选择当前的 `hook-entry` 选择哪一个寄存器作为临时寄存器.

参考资料:

```
PAGE: 9-3
Programmer’s Guide for ARMv8-A
9.1 Register use in the AArch64 Procedure Call Standard 
9.1.1 Parameters in general-purpose registers
```

这里也有一个问题,  这也是 `frida-gum` 中遇到一个问题, 就是对于 `svc #0x80` 类系统调用, 系统调用号(syscall number)的传递是利用 `x16` 寄存器进行传递的, 所以本框架使用 `x17` 寄存器, 并且在传递参数时使用 `push` & `pop`, 在跳转后恢复 `x17`, 避免了一个寄存器的使用.

## `rwx` 与 `codesigning`

对于非越狱, 不能分配可执行内存, 不能进行 `code patch`.

两篇原理讲解 codesign 的原理

```
https://papers.put.as/papers/ios/2011/syscan11_breaking_ios_code_signing.pdf
http://www.newosxbook.com/articles/CodeSigning.pdf
```

以及源码分析如下:

crash 异常如下, 其中 `0x0000000100714000` 是 mmap 分配的页.

```
Exception Type:  EXC_BAD_ACCESS (SIGKILL - CODESIGNING)
Exception Subtype: unknown at 0x0000000100714000
Termination Reason: Namespace CODESIGNING, Code 0x2
Triggered by Thread:  0
```

寻找对应的错误码

```
xnu-3789.41.3/bsd/sys/reason.h
/*
 * codesigning exit reasons
 */
#define CODESIGNING_EXIT_REASON_TASKGATED_INVALID_SIG 1
#define CODESIGNING_EXIT_REASON_INVALID_PAGE          2
#define CODESIGNING_EXIT_REASON_TASK_ACCESS_PORT      3
```

找到对应处理函数, 请仔细阅读注释里内容, 不做解释了.

```
# xnu-3789.41.3/osfmk/vm/vm_fault.c:2632

	/* If the map is switched, and is switch-protected, we must protect
	 * some pages from being write-faulted: immutable pages because by 
	 * definition they may not be written, and executable pages because that
	 * would provide a way to inject unsigned code.
	 * If the page is immutable, we can simply return. However, we can't
	 * immediately determine whether a page is executable anywhere. But,
	 * we can disconnect it everywhere and remove the executable protection
	 * from the current map. We do that below right before we do the 
	 * PMAP_ENTER.
	 */
	cs_enforcement_enabled = cs_enforcement(NULL);

	if(cs_enforcement_enabled && map_is_switched && 
	   map_is_switch_protected && page_immutable(m, prot) && 
	   (prot & VM_PROT_WRITE))
	{
		return KERN_CODESIGN_ERROR;
	}

	if (cs_enforcement_enabled && page_nx(m) && (prot & VM_PROT_EXECUTE)) {
		if (cs_debug)
			printf("page marked to be NX, not letting it be mapped EXEC\n");
		return KERN_CODESIGN_ERROR;
	}

	if (cs_enforcement_enabled &&
	    !m->cs_validated &&
	    (prot & VM_PROT_EXECUTE) &&
	    !(caller_prot & VM_PROT_EXECUTE)) {
		/*
		 * FOURK PAGER:
		 * This page has not been validated and will not be
		 * allowed to be mapped for "execute".
		 * But the caller did not request "execute" access for this
		 * fault, so we should not raise a code-signing violation
		 * (and possibly kill the process) below.
		 * Instead, let's just remove the "execute" access request.
		 * 
		 * This can happen on devices with a 4K page size if a 16K
		 * page contains a mix of signed&executable and
		 * unsigned&non-executable 4K pages, making the whole 16K
		 * mapping "executable".
		 */
		prot &= ~VM_PROT_EXECUTE;
	}

	/* A page could be tainted, or pose a risk of being tainted later.
	 * Check whether the receiving process wants it, and make it feel
	 * the consequences (that hapens in cs_invalid_page()).
	 * For CS Enforcement, two other conditions will 
	 * cause that page to be tainted as well: 
	 * - pmapping an unsigned page executable - this means unsigned code;
	 * - writeable mapping of a validated page - the content of that page
	 *   can be changed without the kernel noticing, therefore unsigned
	 *   code can be created
	 */
	if (!cs_bypass &&
	    (m->cs_tainted ||
	     (cs_enforcement_enabled &&
	      (/* The page is unsigned and wants to be executable */
	       (!m->cs_validated && (prot & VM_PROT_EXECUTE))  ||
	       /* The page should be immutable, but is in danger of being modified
		* This is the case where we want policy from the code directory -
		* is the page immutable or not? For now we have to assume that 
		* code pages will be immutable, data pages not.
		* We'll assume a page is a code page if it has a code directory 
		* and we fault for execution.
		* That is good enough since if we faulted the code page for
		* writing in another map before, it is wpmapped; if we fault
		* it for writing in this map later it will also be faulted for executing 
		* at the same time; and if we fault for writing in another map
		* later, we will disconnect it from this pmap so we'll notice
		* the change.
		*/
	      (page_immutable(m, prot) && ((prot & VM_PROT_WRITE) || m->wpmapped))
	      ))
		    )) 
	{
```

#### 其他文章:

http://ddeville.me/2014/04/dynamic-linking

> Later on, whenever a page fault occurs the vm_fault function in `vm_fault.c` is called. During the page fault the signature is validated if necessary. The signature will need to be validated if the page is mapped in user space, if the page belongs to a code-signed object, if the page will be writable or simply if it has not previously been validated. Validation happens in the `vm_page_validate_cs` function inside vm_fault.c (the validation process and how it is enforced continually and not only at load time is interesting, see Charlie Miller’s book for more details).

> If for some reason the page cannot be validated, the kernel checks whether the `CS_KILL` flag has been set and kills the process if necessary. There is a major distinction between iOS and OS X regarding this flag. All iOS processes have this flag set whereas on OS X, although code signing is checked it is not set and thus not enforced.

> In our case we can safely assume that the (missing) code signature couldn’t be verified leading to the kernel killing the process.

---