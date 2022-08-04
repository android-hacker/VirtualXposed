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
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>

void open_pre_call(RegState *rs, ThreadStack *threadstack, CallStack *callstack) {
    zpointer t = (void *)0x1234;
    // STACK_SET(callstack ,"key_x", t, void *);
    // STACK_SET(callstack ,"key_y", t, zpointer);
    // NSLog(@"hookzz OC-Method: -[UIViewController %s]",
    // (zpointer)(rs->general.regs.x1));
}

void open_post_call(RegState *rs, ThreadStack *threadstack, CallStack *callstack) {
    // zpointer x = STACK_GET(callstack, "key_x", void *);
    // zpointer y = STACK_GET(callstack, "key_y", zpointer);
    // NSLog(@"function over, and get 'key_x' is: %p", x);
    // NSLog(@"function over, and get 'key_y' is: %p", y);
}

__attribute__((constructor)) void test_hook_printf() {
    void *open_ptr = (void *)open;

    ZzEnableDebugMode();
    ZzHookPrePost((void *)open_ptr, open_pre_call, open_post_call);

    open("/home/zz", O_RDONLY);
}

int main(int args, char **argv) {}
