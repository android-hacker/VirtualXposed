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
#import <Foundation/Foundation.h>
#import <dlfcn.h>
#import <mach-o/dyld.h>
#import <objc/runtime.h>

@interface HookZz : NSObject

@end

@implementation HookZz

+ (void)load {
    [self zzMethodSwizzlingHook];
}

void objcMethod_pre_call(RegState *rs, ThreadStack *threadstack, CallStack *callstack) {
    zpointer t = (void *)0x1234;
    // STACK_SET(callstack ,"key_x", t, void *);
    // STACK_SET(callstack ,"key_y", t, zpointer);
    // NSLog(@"hookzz OC-Method: -[UIViewController %s]",
    // (zpointer)(rs->general.regs.x1));
}

void objcMethod_post_call(RegState *rs, ThreadStack *threadstack, CallStack *callstack) {
    // zpointer x = STACK_GET(callstack, "key_x", void *);
    // zpointer y = STACK_GET(callstack, "key_y", zpointer);
    // NSLog(@"function over, and get 'key_x' is: %p", x);
    // NSLog(@"function over, and get 'key_y' is: %p", y);
}

+ (void)zzMethodSwizzlingHook {
    Class hookClass = objc_getClass("UIViewController");
    SEL oriSEL = @selector(viewWillAppear:);
    Method oriMethod = class_getInstanceMethod(hookClass, oriSEL);
    IMP oriImp = method_getImplementation(oriMethod);

    ZzEnableDebugMode();
    ZzHookPrePost((void *)oriImp, objcMethod_pre_call, objcMethod_post_call);
}

@end

/*
(lldb) disass -n "-[UIViewController viewWillAppear:]" -c 3
UIKit`-[UIViewController viewWillAppear:]:
    0x18881c10c <+0>: adrp   x8, 126868
    0x18881c110 <+4>: ldrsw  x8, [x8, #0x280]
    0x18881c114 <+8>: ldr    x9, [x0, x8]

(lldb) c
Process 41637 resuming
(lldb) c
Process 41637 resuming
(lldb) c
Process 41637 resuming
2017-08-30 02:01:58.954875+0800 T007[41637:10198806] hookzz OC-Method:
-[UIViewController viewWillAppear:] 2017-08-30 02:01:58.956558+0800
T007[41637:10198806] function over, and get 'key_x' is: 0x1234 2017-08-30
02:01:58.956654+0800 T007[41637:10198806] function over, and get 'key_y' is:
0x1234 (lldb) disass -n "-[UIViewController viewWillAppear:]" -c 3
UIKit`-[UIViewController viewWillAppear:]:
    0x18881c10c <+0>: b      0x1810b0b4c
    0x18881c110 <+4>: ldrsw  x8, [x8, #0x280]
    0x18881c114 <+8>: ldr    x9, [x0, x8]
*/