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

#include "hookzz.h"
#include <stdio.h>
#include <unistd.h>

static void hack_this_function() {
#ifdef __arm64__
    __asm__("mov X0, #0\n"
            "mov w16, #20\n"
            "svc #0x80\n"
            "nop\n"
            "nop\n"
            "nop\n"
            "nop");
#endif
}

static void sorry_to_exit() {
#ifdef __arm64__
    __asm__("mov X0, #0\n"
            "mov w16, #1\n"
            "svc #0x80");
#endif
}

void getpid_pre_call(RegState *rs, ThreadStack *threadstack, CallStack *callstack) {
    unsigned long request = *(unsigned long *)(&rs->general.regs.x16);
    printf("request(x16) is: %ld\n", request);
    printf("x0 is: %ld\n", (long)rs->general.regs.x0);
}

void getpid_half_call(RegState *rs, ThreadStack *threadstack, CallStack *callstack) {
    pid_t x0 = (pid_t)(rs->general.regs.x0);
    printf("getpid() return at x0 is: %d\n", x0);
}

__attribute__((constructor)) void test_hook_address() {
    void *hack_this_function_ptr = (void *)hack_this_function;
    // hook address with only `pre_call`
    // ZzBuildHookAddress(hack_this_function_ptr + 8, hack_this_function_ptr + 12, (void
    // *)getpid_pre_call, NULL);

    // hook address with only `half_call`
    // ZzBuildHookAddress(hack_this_function_ptr + 8, hack_this_function_ptr + 12, NULL, (void
    // *)getpid_half_call);

    // hook address with both `half_call` and `pre_call`
    ZzBuildHookAddress(hack_this_function_ptr + 8, hack_this_function_ptr + 12, getpid_pre_call, getpid_half_call,
                       TRUE);
    ZzEnableHook((void *)hack_this_function_ptr + 8);

    void *sorry_to_exit_ptr = (void *)sorry_to_exit;
    unsigned long nop_bytes = 0xD503201F;
    ZzRuntimeCodePatch((unsigned long)sorry_to_exit_ptr + 8, (zpointer)&nop_bytes, 4);

    hack_this_function();
    sorry_to_exit();

    printf("hack success -.0\n");
}

/*
(lldb) disass -n hack_this_function
test_hook_address.dylib`hack_this_function:
    0x1000b0280 <+0>:  mov    x0, #0x0
    0x1000b0284 <+4>:  mov    w16, #0x14
    0x1000b0288 <+8>:  svc    #0x80
    0x1000b028c <+12>: ret

(lldb) disass -n sorry_to_exit
test_hook_address.dylib`sorry_to_exit:
    0x1000b0290 <+0>:  mov    x0, #0x0
    0x1000b0294 <+4>:  mov    w16, #0x1
    0x1000b0298 <+8>:  svc    #0x80
    0x1000b029c <+12>: ret

(lldb) c
Process 41414 resuming
request(x16) is: 20
x0 is: 0
getpid() return at x0 is: 41414
hack success -.0
(lldb) disass -n hack_this_function
test_hook_address.dylib`hack_this_function:
    0x1000b0280 <+0>:  mov    x0, #0x0
    0x1000b0284 <+4>:  mov    w16, #0x14
    0x1000b0288 <+8>:  b      0x1001202cc
    0x1000b028c <+12>: ret

(lldb) disass -n sorry_to_exit
test_hook_address.dylib`sorry_to_exit:
    0x1000b0290 <+0>:  mov    x0, #0x0
    0x1000b0294 <+4>:  mov    w16, #0x1
    0x1000b0298 <+8>:  nop
    0x1000b029c <+12>: ret
*/