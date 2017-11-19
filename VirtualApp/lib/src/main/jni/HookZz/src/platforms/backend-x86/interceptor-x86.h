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

#ifndef platforms_backend_x86_intercetor_x86
#define platforms_backend_x86_intercetor_x86

// platforms
#include "platforms/arch-x86/relocator-x86.h"
#include "platforms/arch-x86/writer-x86.h"

// hookzz
#include "allocator.h"
#include "interceptor.h"
#include "thunker.h"

// zzdeps
#include "hookzz.h"
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

#define CTX_SAVE_STACK_OFFSET (8 + 30 * 8 + 8 * 16)

typedef struct _ZzInterceptorBackend {

} ZzInterceptorBackend;

typedef struct _ZzX86HookFuntionEntryBackend {
} ZzX86HookFunctionEntryBackend;

void ctx_save();
void ctx_restore();
void enter_thunk_template();
void leave_thunk_template();
void on_enter_trampoline_template();
void on_invoke_trampoline_template();
void on_leave_trampoline_template();

ZzCodeSlice *zz_code_patch_x86_writer(ZzX86Writer *x86_writer, ZzAllocator *allocator, zaddr target_addr,
                                      zsize range_size);
#endif