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

__attribute__((__naked__)) static void hack_this_function() {
#ifdef __arm__
    __asm__ volatile(".code 32\n"
                     "mov r0, #0\n"
                     "mov r12, #20\n"
                     "svc #0x80\n"
                     "nop\n"
                     "nop\n"
                     "nop\n"
                     "nop\n"
                     "nop\n"
                     "nop");
#endif
}

__attribute__((__naked__)) static void sorry_to_exit() {
#ifdef __arm__
    __asm__ volatile(".code 32\n"
                     "mov r0, #0\n"
                     "mov r12, #1\n"
                     "svc #0x80");
#endif
}

void getpid_pre_call(RegState *rs, ThreadStack *threadstack, CallStack *callstack) {
    unsigned long request = *(unsigned long *)(&rs->general.regs.r12);
    printf("request(r12) is: %ld\n", request);
    printf("r0 is: %ld\n", (long)rs->general.regs.r0);
}

void getpid_half_call(RegState *rs, ThreadStack *threadstack, CallStack *callstack) {
    pid_t r0 = (pid_t)(rs->general.regs.r0);
    printf("getpid() return at r0 is: %d\n", r0);
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
    ZzEnableDebugMode();
    ZzHookAddress(hack_this_function_ptr + 8, hack_this_function_ptr + 8 + 4, getpid_pre_call, getpid_half_call);

    void *sorry_to_exit_ptr = (void *)sorry_to_exit;
    unsigned long nop_bytes = 0xE1A00000;
    ZzRuntimeCodePatch((unsigned long)sorry_to_exit_ptr + 8, (zpointer)&nop_bytes, 4);

    hack_this_function();
    sorry_to_exit();

    printf("hack success -.0\n");
}

int main(int args, char **argv) {}
