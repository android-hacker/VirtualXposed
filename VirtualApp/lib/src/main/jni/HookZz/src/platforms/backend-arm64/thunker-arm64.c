/**
 *    Copyright 2017 jmpews
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

#include "thunker-arm64.h"
#include "zzinfo.h"
#include <string.h>

/*
    Programmer’s Guide for ARMv8-A:
        Page: (6-15)
        Page: (6-16)

    STP X9, X8, [X4]
        Stores the doubleword in X9 to address X4 and stores the doubleword
   in X8 to address X4 + 8. LDP X8, X2, [X0, #0x10]! Loads doubleword at
   address X0 + 0x10 into X8 and the doubleword at address X0 + 0x10 + 8
   into X2 and add 0x10 to X0. See Figure 6-7.
 */

// 前提: 不能直接访问 pc, 也就说只有通过寄存器才能实现绝对地址跳

// __attribute__((__naked__)) static void ctx_save() {
//     __asm__ volatile(

//         /* save {q0-q7} */
//         "sub sp, sp, #(8*16)\n"
//         "stp q6, q7, [sp, #(6*16)]\n"
//         "stp q4, q5, [sp, #(4*16)]\n"
//         "stp q2, q3, [sp, #(2*16)]\n"
//         "stp q0, q1, [sp, #(0*16)]\n"

//         /* save {x1-x30} */
//         "sub sp, sp, #(30*8)\n"
//         // "stp fp, lr, [sp, #(28*8)]\n"
//         "stp x29, x30, [sp, #(28*8)]\n"
//         "stp x27, x28, [sp, #(26*8)]\n"
//         "stp x25, x26, [sp, #(24*8)]\n"
//         "stp x23, x24, [sp, #(22*8)]\n"
//         "stp x21, x22, [sp, #(20*8)]\n"
//         "stp x19, x20, [sp, #(18*8)]\n"
//         "stp x17, x18, [sp, #(16*8)]\n"
//         "stp x15, x16, [sp, #(14*8)]\n"
//         "stp x13, x14, [sp, #(12*8)]\n"
//         "stp x11, x12, [sp, #(10*8)]\n"
//         "stp x9, x10, [sp, #(8*8)]\n"
//         "stp x7, x8, [sp, #(6*8)]\n"
//         "stp x5, x6, [sp, #(4*8)]\n"
//         "stp x3, x4, [sp, #(2*8)]\n"
//         "stp x1, x2, [sp, #(0*8)]\n"

//         // C6.1.3
//         // Use of the stack pointer
//         // save x0 (and reserve sp, but this is trick.)
//         "sub sp, sp, #(2*8)\n"
//         "str x0, [sp, #8]\n");
// }

// __attribute__((__naked__)) static void ctx_restore() {
//     __asm__ volatile(
//         // C6.1.3
//         // Use of the stack pointer
//         // restore x0
//         "ldr x0, [sp, #8]\n"
//         "add sp, sp, #(2*8)\n"

//         /* restore {x1-x30} */
//         "ldp x1, x2, [sp], #16\n"
//         "ldp x3, x4, [sp], #16\n"
//         "ldp x5, x6, [sp], #16\n"
//         "ldp x7, x8, [sp], #16\n"
//         "ldp x9, x10, [sp], #16\n"
//         "ldp x11, x12, [sp], #16\n"
//         "ldp x13, x14, [sp], #16\n"
//         "ldp x15, x16, [sp], #16\n"
//         "ldp x17, x18, [sp], #16\n"
//         "ldp x19, x20, [sp], #16\n"
//         "ldp x21, x22, [sp], #16\n"
//         "ldp x23, x24, [sp], #16\n"
//         "ldp x25, x26, [sp], #16\n"
//         "ldp x27, x28, [sp], #16\n"
//         // "ldp fp, lr, [sp], #16\n"
//         "ldp x29, x30, [sp], #16\n"

//         /* restore {q0-q7} */
//         "ldp q0, q1, [sp], #32\n"
//         "ldp q2, q3, [sp], #32\n"
//         "ldp q4, q5, [sp], #32\n"
//         "ldp q6, q7, [sp], #32\n");
// }

// just like pre_call, wow!
void function_context_begin_invocation(ZzHookFunctionEntry *entry, zpointer next_hop, RegState *rs,
                                       zpointer caller_ret_addr) {
    Xinfo("target %p call begin-invocation", entry->target_ptr);

    ZzThreadStack *stack = ZzGetCurrentThreadStack(entry->thread_local_key);
    if (!stack) {
        stack = ZzNewThreadStack(entry->thread_local_key);
    }
    ZzCallStack *callstack = ZzNewCallStack();
    ZzPushCallStack(stack, callstack);

    /* call pre_call */
    if (entry->pre_call) {
        PRECALL pre_call;
        pre_call = entry->pre_call;
        (*pre_call)(rs, (ThreadStack *)stack, (CallStack *)callstack);
    }

    /* set next hop */
    if (entry->replace_call) {
        *(zpointer *)next_hop = entry->replace_call;
    } else {
        *(zpointer *)next_hop = entry->on_invoke_trampoline;
    }

    if (entry->hook_type == HOOK_FUNCTION_TYPE) {
        callstack->caller_ret_addr = *(zpointer *)caller_ret_addr;
        *(zpointer *)caller_ret_addr = entry->on_leave_trampoline;
    }
}

// just like post_call, wow!
void function_context_half_invocation(ZzHookFunctionEntry *entry, zpointer next_hop, RegState *rs,
                                      zpointer caller_ret_addr) {
    Xdebug("target %p call half-invocation", entry->target_ptr);

    ZzThreadStack *stack = ZzGetCurrentThreadStack(entry->thread_local_key);
    if (!stack) {
#if defined(DEBUG_MODE)
        debug_break();
#endif
    }
    ZzCallStack *callstack = ZzPopCallStack(stack);

    /* call half_call */
    if (entry->half_call) {
        HALFCALL half_call;
        half_call = entry->half_call;
        (*half_call)(rs, (ThreadStack *)stack, (CallStack *)callstack);
    }

    /*  set next hop */
    *(zpointer *)next_hop = (zpointer)entry->target_half_ret_addr;

    ZzFreeCallStack(callstack);
}

// just like post_call, wow!
void function_context_end_invocation(ZzHookFunctionEntry *entry, zpointer next_hop, RegState *rs) {
    Xdebug("%p call end-invocation", entry->target_ptr);

    ZzThreadStack *stack = ZzGetCurrentThreadStack(entry->thread_local_key);
    if (!stack) {
#if defined(DEBUG_MODE)
        debug_break();
#endif
    }
    ZzCallStack *callstack = ZzPopCallStack(stack);

    /* call post_call */
    if (entry->post_call) {
        POSTCALL post_call;
        post_call = entry->post_call;
        (*post_call)(rs, (ThreadStack *)stack, (CallStack *)callstack);
    }

    /* set next hop */
    *(zpointer *)next_hop = callstack->caller_ret_addr;
    ZzFreeCallStack(callstack);
}

// __attribute__((__naked__)) void enter_thunk_template() {
//     /* register save */
//     __asm__ volatile(

//         /* save {q0-q7} */
//         "sub sp, sp, #(8*16)\n"
//         "stp q6, q7, [sp, #(6*16)]\n"
//         "stp q4, q5, [sp, #(4*16)]\n"
//         "stp q2, q3, [sp, #(2*16)]\n"
//         "stp q0, q1, [sp, #(0*16)]\n"

//         /* save {x1-x30} */
//         "sub sp, sp, #(30*8)\n"
//         // "stp fp, lr, [sp, #(28*8)]\n"
//         "stp x29, x30, [sp, #(28*8)]\n"
//         "stp x27, x28, [sp, #(26*8)]\n"
//         "stp x25, x26, [sp, #(24*8)]\n"
//         "stp x23, x24, [sp, #(22*8)]\n"
//         "stp x21, x22, [sp, #(20*8)]\n"
//         "stp x19, x20, [sp, #(18*8)]\n"
//         "stp x17, x18, [sp, #(16*8)]\n"
//         "stp x15, x16, [sp, #(14*8)]\n"
//         "stp x13, x14, [sp, #(12*8)]\n"
//         "stp x11, x12, [sp, #(10*8)]\n"
//         "stp x9, x10, [sp, #(8*8)]\n"
//         "stp x7, x8, [sp, #(6*8)]\n"
//         "stp x5, x6, [sp, #(4*8)]\n"
//         "stp x3, x4, [sp, #(2*8)]\n"
//         "stp x1, x2, [sp, #(0*8)]\n"

//         // C6.1.3
//         // Use of the stack pointer
//         // save x0 (and reserve sp, but this is trick.)
//         "sub sp, sp, #(2*8)\n"
//         "str x0, [sp, #8]\n");

//     __asm__ volatile(
//         /* use the `ctx_save` left space, to store origin sp */
//         "add x1, sp, #0x190\n"
//         "str x1, [sp, #0]\n"
//         /* alignment padding */
//         "sub sp, sp, #0x10\n"
//         /* prepare args */
//         /* x0: entry address, x1: next hop, x2: RegState address, x3: caller_ret_addr */
//         "ldr x0, [sp, #0x190]\n"
//         "add x1, sp, #0x198\n"
//         "add x2, sp, #0x10\n"
//         "add x3, sp, #0x108\n"
//         "ldr x17, #0xc\n"
//         "blr x17\n"
//         "b #0xc\n"
//         /* function_context_begin_invocation address */
//         /* TODO: need some trick. */
//         // ((void (*)(void))(function_context_begin_invocation))();
//         ".long 0x0\n"
//         ".long 0x0\n"
//         /* restore alignment padding */
//         "add sp, sp, #0x10\n");

//     /* register restore */
//     __asm__ volatile(
//         // C6.1.3
//         // Use of the stack pointer
//         // restore x0
//         "ldr x0, [sp, #8]\n"
//         "add sp, sp, #(2*8)\n"

//         /* restore {x1-x30} */
//         "ldp x1, x2, [sp], #16\n"
//         "ldp x3, x4, [sp], #16\n"
//         "ldp x5, x6, [sp], #16\n"
//         "ldp x7, x8, [sp], #16\n"
//         "ldp x9, x10, [sp], #16\n"
//         "ldp x11, x12, [sp], #16\n"
//         "ldp x13, x14, [sp], #16\n"
//         "ldp x15, x16, [sp], #16\n"
//         "ldp x17, x18, [sp], #16\n"
//         "ldp x19, x20, [sp], #16\n"
//         "ldp x21, x22, [sp], #16\n"
//         "ldp x23, x24, [sp], #16\n"
//         "ldp x25, x26, [sp], #16\n"
//         "ldp x27, x28, [sp], #16\n"
//         // "ldp fp, lr, [sp], #16\n"
//         "ldp x29, x30, [sp], #16\n"

//         /* restore {q0-q7} */
//         "ldp q0, q1, [sp], #32\n"
//         "ldp q2, q3, [sp], #32\n"
//         "ldp q4, q5, [sp], #32\n"
//         "ldp q6, q7, [sp], #32\n");
//     /* register save */
//     __asm__ volatile(
//         /* jump to next hop */
//         /* next hop address(store at reserve space  */
//         "ldr x17, [sp, #8]\n"
//         "add sp, sp, #0x10\n"
//         "br x17");
// }

void zz_arm64_thunker_build_enter_thunk(ZzWriter *writer) {
    /* save general registers and sp */
    zz_arm64_writer_put_bytes(writer, (void *)ctx_save, 23 * 4);
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_X1, ZZ_ARM64_REG_SP, 8 + CTX_SAVE_STACK_OFFSET + 2 * 8);

    /* trick: use the `ctx_save` left [sp]*/
    zz_arm64_writer_put_str_reg_reg_offset(writer, ZZ_ARM64_REG_X1, ZZ_ARM64_REG_SP, 0 * 8);

    /* alignment padding + dummy PC */
    zz_arm64_writer_put_sub_reg_reg_imm(writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 8);

    /* pass enter func args */
    /* entry */
    zz_arm64_writer_put_ldr_reg_reg_offset(writer, ZZ_ARM64_REG_X0, ZZ_ARM64_REG_SP, 2 * 8 + 8 + CTX_SAVE_STACK_OFFSET);
    /* next hop*/
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_X1, ZZ_ARM64_REG_SP,
                                        2 * 8 + 8 + CTX_SAVE_STACK_OFFSET + 0x8);
    /* RegState */
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_X2, ZZ_ARM64_REG_SP, 2 * 8);
    /* caller ret address */
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_X3, ZZ_ARM64_REG_SP, 2 * 8 + 2 * 8 + 28 * 8 + 8);

    /* call function_context_begin_invocation */
    zz_arm64_writer_put_ldr_blr_b_reg_address(writer, ZZ_ARM64_REG_X17, (zaddr)function_context_begin_invocation);

    /* alignment padding + dummy PC */
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 8);

    /* restore general registers stack */
    zz_arm64_writer_put_bytes(writer, (void *)ctx_restore, 21 * 4);

    /* load next hop to x17 */
    zz_arm64_writer_put_ldr_reg_reg_offset(writer, ZZ_ARM64_REG_X17, ZZ_ARM64_REG_SP, 0x8);

    /* restore next hop and arg stack */
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 8);

    /* jump to next hop */
    zz_arm64_writer_put_br_reg(writer, ZZ_ARM64_REG_X17);
}

void zz_arm64_thunker_build_half_thunk(ZzWriter *writer) {
    /* save general registers and sp */
    zz_arm64_writer_put_bytes(writer, (void *)ctx_save, 23 * 4);
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_X1, ZZ_ARM64_REG_SP, 8 + CTX_SAVE_STACK_OFFSET + 2 * 8);

    /* trick: use the `ctx_save` left [sp]*/
    zz_arm64_writer_put_str_reg_reg_offset(writer, ZZ_ARM64_REG_X1, ZZ_ARM64_REG_SP, 0 * 8);

    /* alignment padding + dummy PC */
    zz_arm64_writer_put_sub_reg_reg_imm(writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 8);

    /* pass enter func args */
    /* entry */
    zz_arm64_writer_put_ldr_reg_reg_offset(writer, ZZ_ARM64_REG_X0, ZZ_ARM64_REG_SP, 2 * 8 + 8 + CTX_SAVE_STACK_OFFSET);
    /* next hop*/
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_X1, ZZ_ARM64_REG_SP,
                                        2 * 8 + 8 + CTX_SAVE_STACK_OFFSET + 0x8);

    /* RegState */
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_X2, ZZ_ARM64_REG_SP, 2 * 8);
    /* caller ret address */
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_X3, ZZ_ARM64_REG_SP, 2 * 8 + 2 * 8 + 28 * 8 + 8);

    /* call function_context_half_invocation */
    zz_arm64_writer_put_ldr_blr_b_reg_address(writer, ZZ_ARM64_REG_X17, (zaddr)function_context_half_invocation);

    /* alignment padding + dummy PC */
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 8);

    /* restore general registers stack */
    zz_arm64_writer_put_bytes(writer, (void *)ctx_restore, 21 * 4);

    /* load next hop to x17 */
    zz_arm64_writer_put_ldr_reg_reg_offset(writer, ZZ_ARM64_REG_X17, ZZ_ARM64_REG_SP, 0x8);

    /* restore next hop and arg stack */
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 8);

    /* jump to next hop */
    zz_arm64_writer_put_br_reg(writer, ZZ_ARM64_REG_X17);
}

// __attribute__((__naked__)) void leave_thunk_template() {
//     /* register save */
//     __asm__ volatile(

//         /* save {q0-q7} */
//         "sub sp, sp, #(8*16)\n"
//         "stp q6, q7, [sp, #(6*16)]\n"
//         "stp q4, q5, [sp, #(4*16)]\n"
//         "stp q2, q3, [sp, #(2*16)]\n"
//         "stp q0, q1, [sp, #(0*16)]\n"

//         /* save {x1-x30} */
//         "sub sp, sp, #(30*8)\n"
//         // "stp fp, lr, [sp, #(28*8)]\n"
//         "stp x29, x30, [sp, #(28*8)]\n"
//         "stp x27, x28, [sp, #(26*8)]\n"
//         "stp x25, x26, [sp, #(24*8)]\n"
//         "stp x23, x24, [sp, #(22*8)]\n"
//         "stp x21, x22, [sp, #(20*8)]\n"
//         "stp x19, x20, [sp, #(18*8)]\n"
//         "stp x17, x18, [sp, #(16*8)]\n"
//         "stp x15, x16, [sp, #(14*8)]\n"
//         "stp x13, x14, [sp, #(12*8)]\n"
//         "stp x11, x12, [sp, #(10*8)]\n"
//         "stp x9, x10, [sp, #(8*8)]\n"
//         "stp x7, x8, [sp, #(6*8)]\n"
//         "stp x5, x6, [sp, #(4*8)]\n"
//         "stp x3, x4, [sp, #(2*8)]\n"
//         "stp x1, x2, [sp, #(0*8)]\n"

//         // C6.1.3
//         // Use of the stack pointer
//         // save x0 (and reserve sp, but this is trick.)
//         "sub sp, sp, #(2*8)\n"
//         "str x0, [sp, #8]\n");

//     __asm__ volatile(
//         /* use the `ctx_save` left space, to store origin sp */
//         "add x1, sp, #0x190\n"
//         "str x1, [sp, #0]\n"
//         /* alignment padding */
//         "sub sp, sp, #0x10\n"
//         /* prepare args */
//         /* x0: entry address, x1: next hop, x2: RegState address, x3: caller_ret_addr */
//         "ldr x0, [sp, #0x190]\n"
//         "add x1, sp, #0x198\n"
//         "add x2, sp, #0x10\n"
//         "ldr x17, #0xc\n"
//         "blr x17\n"
//         "b #0xc\n"
//         /* function_context_end_invocation address */
//         ".long 0x0\n"
//         ".long 0x0\n"
//         /* restore alignment padding */
//         "add sp, sp, #0x10\n");

//     /* register restore */
//     __asm__ volatile(
//         // C6.1.3
//         // Use of the stack pointer
//         // restore x0
//         "ldr x0, [sp, #8]\n"
//         "add sp, sp, #(2*8)\n"

//         /* restore {x1-x30} */
//         "ldp x1, x2, [sp], #16\n"
//         "ldp x3, x4, [sp], #16\n"
//         "ldp x5, x6, [sp], #16\n"
//         "ldp x7, x8, [sp], #16\n"
//         "ldp x9, x10, [sp], #16\n"
//         "ldp x11, x12, [sp], #16\n"
//         "ldp x13, x14, [sp], #16\n"
//         "ldp x15, x16, [sp], #16\n"
//         "ldp x17, x18, [sp], #16\n"
//         "ldp x19, x20, [sp], #16\n"
//         "ldp x21, x22, [sp], #16\n"
//         "ldp x23, x24, [sp], #16\n"
//         "ldp x25, x26, [sp], #16\n"
//         "ldp x27, x28, [sp], #16\n"
//         // "ldp fp, lr, [sp], #16\n"
//         "ldp x29, x30, [sp], #16\n"

//         /* restore {q0-q7} */
//         "ldp q0, q1, [sp], #32\n"
//         "ldp q2, q3, [sp], #32\n"
//         "ldp q4, q5, [sp], #32\n"
//         "ldp q6, q7, [sp], #32\n");
//     /* register save */
//     __asm__ volatile(
//         /* jump to next hop */
//         /* next hop address(store at reserve space  */
//         "ldr x17, [sp, #8]\n"
//         "add sp, sp, #0x10\n"
//         "br x17");
// }

void zz_arm64_thunker_build_leave_thunk(ZzWriter *writer) {
    /* save general registers and sp */
    zz_arm64_writer_put_bytes(writer, (void *)ctx_save, 23 * 4);
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_X1, ZZ_ARM64_REG_SP, 8 + CTX_SAVE_STACK_OFFSET + 2 * 8);

    /* trick: use the `ctx_save` left [sp]*/
    zz_arm64_writer_put_str_reg_reg_offset(writer, ZZ_ARM64_REG_X1, ZZ_ARM64_REG_SP, 0 * 8);

    /* alignment padding + dummy PC */
    zz_arm64_writer_put_sub_reg_reg_imm(writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 8);

    /* pass enter func args */
    /* entry */
    zz_arm64_writer_put_ldr_reg_reg_offset(writer, ZZ_ARM64_REG_X0, ZZ_ARM64_REG_SP, 2 * 8 + 8 + CTX_SAVE_STACK_OFFSET);
    /* next hop*/
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_X1, ZZ_ARM64_REG_SP,
                                        2 * 8 + 8 + CTX_SAVE_STACK_OFFSET + 0x8);

    /* RegState */
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_X2, ZZ_ARM64_REG_SP, 2 * 8);

    /* call function_context_end_invocation */
    zz_arm64_writer_put_ldr_blr_b_reg_address(writer, ZZ_ARM64_REG_X17, (zaddr)function_context_end_invocation);

    /* alignment padding + dummy PC */
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 8);

    /* restore general registers stack */
    zz_arm64_writer_put_bytes(writer, (void *)ctx_restore, 21 * 4);

    /* load next hop to x17 */
    zz_arm64_writer_put_ldr_reg_reg_offset(writer, ZZ_ARM64_REG_X17, ZZ_ARM64_REG_SP, 0x8);

    /* restore next hop and arg stack */
    zz_arm64_writer_put_add_reg_reg_imm(writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 8);

    /* jump to next hop */
    zz_arm64_writer_put_br_reg(writer, ZZ_ARM64_REG_X17);
}

ZZSTATUS ZzThunkerBuildThunk(ZzInterceptorBackend *self) {
    zbyte temp_code_slice_data[512] = {0};
    ZzArm64Writer *arm64_writer = NULL;
    ZzCodeSlice *code_slice = NULL;
    ZZSTATUS status = ZZ_SUCCESS;

    arm64_writer = &self->arm64_writer;
    zz_arm64_writer_reset(arm64_writer, temp_code_slice_data);

    /* build enter_thunk */
    zz_arm64_thunker_build_enter_thunk(arm64_writer);

    /* code patch */
    code_slice = zz_code_patch_arm64_writer(arm64_writer, self->allocator, 0, 0);
    if (code_slice)
        self->enter_thunk = (void *)enter_thunk_template;
    else
        return ZZ_FAILED;

    /* debug log */
    if (ZzIsEnableDebugMode()) {
        char buffer[1024] = {};
        sprintf(buffer + strlen(buffer), "%s\n", "ZzThunkerBuildThunk:");
        sprintf(buffer + strlen(buffer), "LogInfo: enter_thunk at %p, use enter_thunk_template.\n",
                (void *)enter_thunk_template);
        ZzInfoLog("%s", buffer);
    }

    zz_arm64_writer_reset(arm64_writer, temp_code_slice_data);

    /* build  leave_thunk */
    zz_arm64_thunker_build_leave_thunk(arm64_writer);

    /* code patch */
    code_slice = zz_code_patch_arm64_writer(arm64_writer, self->allocator, 0, 0);
    if (code_slice)
        self->leave_thunk = code_slice->data;
    else
        return ZZ_FAILED;

    /* debug log */
    if (ZzIsEnableDebugMode()) {
        char buffer[1024] = {};
        sprintf(buffer + strlen(buffer), "%s\n", "ZzThunkerBuildThunk:");
        sprintf(buffer + strlen(buffer), "LogInfo: leave_thunk at %p, length: %ld.\n", code_slice->data,
                code_slice->size);
        ZzInfoLog("%s", buffer);
    }

    zz_arm64_writer_reset(arm64_writer, temp_code_slice_data);

    /* build half_thunk */
    zz_arm64_thunker_build_half_thunk(arm64_writer);

    /* code patch */
    code_slice = zz_code_patch_arm64_writer(arm64_writer, self->allocator, 0, 0);
    if (code_slice)
        self->half_thunk = code_slice->data;
    else
        return ZZ_FAILED;

    /* debug log */
    if (ZzIsEnableDebugMode()) {
        char buffer[1024] = {};
        sprintf(buffer + strlen(buffer), "%s\n", "ZzThunkerBuildThunk:");
        sprintf(buffer + strlen(buffer), "LogInfo: half_thunk at %p, length: %ld.\n", code_slice->data,
                code_slice->size);
        ZzInfoLog("%s", buffer);
    }

    return status;
}
