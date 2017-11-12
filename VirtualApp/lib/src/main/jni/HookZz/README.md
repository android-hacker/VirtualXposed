# What is HookZz ?

**a hook framwork for arm/arm64/ios/android**

ref to: [frida-gum](https://github.com/frida/frida-gum) and [minhook](https://github.com/TsudaKageyu/minhook) and [substrate](https://github.com/jevinskie/substrate).

**special thanks to [frida-gum](https://github.com/frida/frida-gum) perfect code and modular architecture, frida is aircraft carrier, HookZz is boat, but still with some tricks**

**thanks for @lauos with contributing android code**

# Features

- **solidify inlinehook without Jailbreak [new-90%]**

- **GOT hook with HookZz(i.e. change fishhook to inlinehook), better for APM [new-0%]**

- [HookZz-Modules help you to hook.](https://github.com/jmpews/HookZzModules)

- the power to access registers directly

- hook function with `replace_call`

- hook function with `pre_call` and `post_call`

- hook **address(a piece of code)** with `pre_call` and `half_call`

- (almost)only **one instruction** to hook(i.e. hook **short funciton, even only one instruction**) [arm/thumb/arm64]

- runtime code patch, without codesign limit

- it's cute

# Getting Started

[Move to HookZz Getting Started](https://jmpews.github.io/zzpp/getting-started/) **[need update]**

# How it works ?

[Move to HookFrameworkDesign.md](https://github.com/jmpews/HookZz/blob/master/docs/HookFrameworkDesign.md) **[need update]**

# Why I do this?

1. for arsenal - [zzdeps](https://github.com/jmpews/zzdeps)
2. for low-level control

# Who use this?

**[VirtualApp](https://github.com/asLody/VirtualApp) An open source implementation of MultiAccount.(Support 4.0 - 8.0)**

**[AppleTrace](https://github.com/everettjf/AppleTrace) Trace tool for iOS/macOS (similar to systrace for Android)**

# Docs

[Move to HookZz docs](https://jmpews.github.io/zzpp/hookzz-docs/) **[need update]**

# Example

[Move to HookZz example](https://jmpews.github.io/zzpp/hookzz-example/) **[need update]**

# Modules

[Move to HookZzModules](https://github.com/jmpews/HookZzModules) **[need update]**

# Compile

## build for arm64-ios

#### 1. build `libhookzz.dylib` and `libhookzz.static.a`

```
jmpews at jmpewsdeMBP in ~/Desktop/SpiderZz/project/HookZz
λ : >>> make clean; make BACKEND=ios ARCH=arm64
clean all *.o success!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/allocator.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/interceptor.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/memory.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/stack.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/trampoline.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/backend-posix/thread-posix.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/backend-darwin/memory-darwin.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm64/instructions.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm64/reader-arm64.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm64/regs-arm64.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm64/relocator-arm64.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm64/writer-arm64.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/backend-arm64/thunker-arm64.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/backend-arm64/trampoline-arm64.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/zzdeps/common/memory-utils-common.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/zzdeps/posix/memory-utils-posix.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/zzdeps/posix/thread-utils-posix.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/zzdeps/darwin/macho-utils-darwin.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/zzdeps/darwin/memory-utils-darwin.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/backend-arm64/interceptor-template-arm64.o]!
build success for arm64-ios-hookzz!
```

check `build/ios-arm64/*`.

#### 2. build tests dylib

```
jmpews at jmpewsdeMBP in ~/Desktop/SpiderZz/project/HookZz/tests/arm64-ios
λ : >>> make clean; make
clean all *.o success!
build [test_hook_oc.dylib] success for arm64-ios!
build [test_hook_address.dylib] success for arm64-ios!
build [test_hook_printf.dylib] success for arm64-ios!
build [test] success for arm64-ios-hookzz!
```

check `build/ios-arm64/*`.

## build for arm-ios

ignore...

## build for arm-android

#### 1. build `libhookzz.so` and `libhookzz.static.a`

```
jmpews at jmpewsdeMBP in ~/Desktop/SpiderZz/project/HookZz
λ : >>> make clean; make BACKEND=android ARCH=arm
clean all *.o success!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/allocator.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/interceptor.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/memory.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/stack.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/trampoline.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/backend-posix/thread-posix.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/backend-linux/memory-linux.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm/instructions.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm/reader-arm.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm/reader-thumb.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm/regs-arm.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm/relocator-arm.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm/relocator-thumb.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm/writer-arm.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/arch-arm/writer-thumb.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/backend-arm/thunker-arm.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/platforms/backend-arm/trampoline-arm.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/zzdeps/common/memory-utils-common.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/zzdeps/posix/memory-utils-posix.o]!
generate [/Users/jmpews/Desktop/SpiderZz/project/HookZz/src/zzdeps/posix/thread-utils-posix.o]!
build success for arm-android-hookzz!
```

and check `build/android-armv7`

#### build tests ELF

```
jmpews at jmpewsdeMBP in ~/Desktop/SpiderZz/project/HookZz/tests/arm-android
λ : >>> make clean; make
clean all *.o success!
build [test_hook_open_arm.dylib] success for armv7-ios!
build [test_hook_address_thumb.dylib] success for armv7-ios!
build [test_hook_printf.dylib] success for armv7-ios!
build [test] success for armv7-android-hookzz!
```

and check `build/android-armv7/*`

# Quick Example

#### `test_hook_printf.c` output for arm64-ios

test hook `printf` with `try_near_jump` option , and `ZzEnableDebugMode()` with `replace_call`, `pre_call`, `post_call`.

```
ZzThunkerBuildThunk:
LogInfo: enter_thunk at 0x100162c20, use enter_thunk_template.

ZzThunkerBuildThunk:
LogInfo: leave_thunk at 0x1001500f4, length: 240.

ZzBuildEnterTrampoline:
LogInfo: on_enter_trampoline at 0x1001502d8, length: 44. hook-entry: 0x145e0c720. and will jump to enter_thunk(0x100162c20).

ZzBuildEnterTransferTrampoline:
LogInfo: on_enter_transfer_trampoline at 0x180f1f414, length: 20. and will jump to on_enter_trampoline(0x1001502d8).

ZzBuildInvokeTrampoline:
LogInfo: on_invoke_trampoline at 0x100150304, length: 24. and will jump to rest code(0x181402a60).
ArmInstructionFix: origin instruction at 0x181402a5c, relocator end at 0x181402a60, relocator instruction nums 1
origin_prologue: 0xf4 0x4f 0xbe 0xa9 

ZzBuildLeaveTrampoline:
LogInfo: on_leave_trampoline at 0x10015031c, length: 44. and will jump to leave_thunk(0x1001500f4).

HookZzzzzzz, %d, %p, %d, %d, %d, %d, %d, %d, %d

printf-pre-call
call printf
HookZzzzzzz, 1, 0x2, 3, 4, 5, 6, 7, 8, 9
HookZzzzzzz, %d, %p, %d, %d, %d, %d, %d, %d, %d

printf-post-call
```
