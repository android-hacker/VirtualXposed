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

#ifndef interceptor_h
#define interceptor_h

// platforms

// hookzz
#include "allocator.h"
#include "hookzz.h"
#include "stack.h"
#include "thread.h"
#include "thunker.h"
#include "writer.h"

// zzdeps
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

typedef struct _FunctionBackup {
    zpointer address;
    zsize size;
    zbyte data[32];
} FunctionBackup;

struct _ZzInterceptor;

/*
 * hook entry
 */

#define HOOK_FUNCTION_TYPE 1
#define HOOK_ADDRESS_TYPE 2

struct _ZzHookFunctionEntryBackend;
typedef struct _ZzHookFunctionEntry {
    int hook_type;
    unsigned long id;
    zbool isEnabled;
    zbool try_near_jump;

    zpointer thread_local_key;
    struct _ZzHookFunctionEntryBackend *backend;

    zpointer target_ptr;
    zpointer target_end_ptr;
    zpointer target_half_ret_addr;

    zpointer pre_call;
    zpointer half_call;
    zpointer post_call;
    zpointer replace_call;

    FunctionBackup origin_prologue;

    zpointer on_enter_transfer_trampoline;
    zpointer on_enter_trampoline;
    zpointer on_half_trampoline;
    zpointer on_invoke_trampoline;
    zpointer on_leave_trampoline;

    struct _ZzInterceptor *interceptor;
} ZzHookFunctionEntry;

typedef struct {
    ZzHookFunctionEntry **entries;
    zsize size;
    zsize capacity;
} ZzHookFunctionEntrySet;

struct _ZzInterceptorBackend;

typedef struct _ZzInterceptor {
    zbool is_support_rx_page;
    ZzHookFunctionEntrySet hook_function_entry_set;
    struct _ZzInterceptorBackend *backend;
    ZzAllocator *allocator;
} ZzInterceptor;

#endif