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

#ifndef trampoline_h
#define trampoline_h

// platforms

// hookzz
#include "hookzz.h"
#include "interceptor.h"

// zzdeps
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

/*
    prepare_trampline:
        1. 判断跳板类型
        2. 初始化

    enter_trampoline:
        1. 跳转到 `enter_thunk`

    invoke_trampoline:
        1. 之前保存的指令(涉及到指令修复)
        2. 跳转到剩余的指令

    leave_trampoline
        1. 跳转到 `leave_thunk`
 */

typedef struct _ZzTrampoline {
    ZzCodeSlice *code_slice;
} ZzTrampoline;

ZZSTATUS ZzPrepareTrampoline(struct _ZzInterceptorBackend *self, ZzHookFunctionEntry *entry);
ZZSTATUS ZzBuildTrampoline(struct _ZzInterceptorBackend *self, ZzHookFunctionEntry *entry);
ZZSTATUS ZzActivateTrampoline(struct _ZzInterceptorBackend *self, ZzHookFunctionEntry *entry);
struct _ZzInterceptorBackend *ZzBuildInteceptorBackend(ZzAllocator *allocator);

ZZSTATUS ZzBuildEnterTrampoline(struct _ZzInterceptorBackend *self, ZzHookFunctionEntry *entry);
ZZSTATUS ZzBuildHalfTrampoline(struct _ZzInterceptorBackend *self, ZzHookFunctionEntry *entry);
ZZSTATUS ZzBuildInvokeTrampoline(struct _ZzInterceptorBackend *self, ZzHookFunctionEntry *entry);
ZZSTATUS ZzBuildLeaveTrampoline(struct _ZzInterceptorBackend *self, ZzHookFunctionEntry *entry);

#ifdef TARGET_IS_IOS
ZZSTATUS ZzActivateSolidifyTrampoline(ZzHookFunctionEntry *entry, zaddr target_fileoff);
#endif

#endif