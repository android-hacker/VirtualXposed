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

#include <stdlib.h>
#include <string.h>

#include "interceptor.h"
#include "trampoline.h"
#include "zzinfo.h"

#define ZZHOOKENTRIES_DEFAULT 100
ZzInterceptor *g_interceptor = NULL;

ZZSTATUS ZzInitializeInterceptor(void) {
    ZzInterceptor *interceptor = g_interceptor;
    ZzHookFunctionEntrySet *hook_function_entry_set;

    if (NULL == interceptor) {
        interceptor = (ZzInterceptor *)malloc(sizeof(ZzInterceptor));
        memset(interceptor, 0, sizeof(ZzInterceptor));

        hook_function_entry_set = &(interceptor->hook_function_entry_set);
        hook_function_entry_set->capacity = ZZHOOKENTRIES_DEFAULT;
        hook_function_entry_set->entries =
            (ZzHookFunctionEntry **)malloc(sizeof(ZzHookFunctionEntry *) * hook_function_entry_set->capacity);
        memset(hook_function_entry_set->entries, 0, sizeof(ZzHookFunctionEntry *) * hook_function_entry_set->capacity);

        if (!hook_function_entry_set->entries) {
            return ZZ_FAILED;
        }
        hook_function_entry_set->size = 0;
        g_interceptor = interceptor;
        interceptor->is_support_rx_page = ZzMemoryIsSupportAllocateRXPage();
        if (interceptor->is_support_rx_page) {
            interceptor->allocator = ZzNewAllocator();
            interceptor->backend = ZzBuildInteceptorBackend(interceptor->allocator);
        } else {
            interceptor->allocator = NULL;
            interceptor->backend = NULL;
        }
        return ZZ_DONE_INIT;
    }
    return ZZ_ALREADY_INIT;
}

ZzHookFunctionEntry *ZzFindHookFunctionEntry(zpointer target_ptr) {
    ZzInterceptor *interceptor = g_interceptor;
    if (!interceptor)
        return NULL;

    ZzHookFunctionEntrySet *hook_function_entry_set = &(interceptor->hook_function_entry_set);

    int i;
    for (i = 0; i < hook_function_entry_set->size; ++i) {
        if ((hook_function_entry_set->entries)[i] && target_ptr == (hook_function_entry_set->entries)[i]->target_ptr) {
            return (hook_function_entry_set->entries)[i];
        }
    }
    return NULL;
}

ZZSTATUS ZzAddHookFunctionEntry(ZzHookFunctionEntry *entry) {
    ZzInterceptor *interceptor = g_interceptor;
    if (!interceptor)
        return ZZ_FAILED;

    ZzHookFunctionEntrySet *hook_function_entry_set = &(interceptor->hook_function_entry_set);

    if (hook_function_entry_set->size >= hook_function_entry_set->capacity) {
        ZzHookFunctionEntry **entries = (ZzHookFunctionEntry **)realloc(
            hook_function_entry_set->entries, sizeof(ZzHookFunctionEntry *) * hook_function_entry_set->capacity * 2);
        if (!entries)
            return ZZ_FAILED;

        hook_function_entry_set->capacity = hook_function_entry_set->capacity * 2;
        hook_function_entry_set->entries = entries;
    }
    hook_function_entry_set->entries[hook_function_entry_set->size++] = entry;
    return ZZ_SUCCESS;
}

void ZzInitializeHookFunctionEntry(ZzHookFunctionEntry *entry, int hook_type, zpointer target_ptr,
                                   zpointer target_end_ptr, zpointer replace_call, PRECALL pre_call, HALFCALL half_call,
                                   POSTCALL post_call, zbool try_near_jump) {
    ZzInterceptor *interceptor = g_interceptor;
    ZzHookFunctionEntrySet *hook_function_entry_set = &(interceptor->hook_function_entry_set);

    memset(entry, 0, sizeof(ZzHookFunctionEntry));

    entry->hook_type = hook_type;
    entry->id = hook_function_entry_set->size;
    entry->isEnabled = 0;
    entry->try_near_jump = try_near_jump;
    entry->interceptor = interceptor;
    entry->target_ptr = target_ptr;
    entry->target_end_ptr = target_end_ptr;
    entry->replace_call = replace_call;
    entry->pre_call = (zpointer)pre_call;
    entry->half_call = (zpointer)half_call;
    entry->post_call = (zpointer)post_call;
    entry->on_enter_trampoline = NULL;
    entry->on_invoke_trampoline = NULL;
    entry->on_half_trampoline = NULL;
    entry->on_leave_trampoline = NULL;
    entry->origin_prologue.address = target_ptr;
    entry->thread_local_key = ZzThreadNewThreadLocalKeyPtr();

    /* key function */
    ZzBuildTrampoline(interceptor->backend, entry);
    ZzAddHookFunctionEntry(entry);
}

ZZSTATUS ZzBuildHook(zpointer target_ptr, zpointer replace_call_ptr, zpointer *origin_ptr, PRECALL pre_call_ptr,
                     POSTCALL post_call_ptr, zbool try_near_jump) {
#if defined(__i386__) || defined(__x86_64__)
    ZzInfoLog("%s", "x86 & x86_64 arch not support");
    return ZZ_FAILED;
#endif

    ZZSTATUS status = ZZ_DONE_HOOK;
    ZzInterceptor *interceptor = g_interceptor;
    ZzHookFunctionEntrySet *hook_function_entry_set = NULL;
    ZzHookFunctionEntry *entry;

    if (!interceptor) {
        ZzInitializeInterceptor();
        if (!g_interceptor)
            return ZZ_FAILED;
        if (!g_interceptor->is_support_rx_page) {
            return ZZ_FAILED;
        }
    }

    interceptor = g_interceptor;
    hook_function_entry_set = &(interceptor->hook_function_entry_set);

    do {
        // check is already hooked
        if (ZzFindHookFunctionEntry(target_ptr)) {
            status = ZZ_ALREADY_HOOK;
            break;
        }

        entry = (ZzHookFunctionEntry *)malloc(sizeof(ZzHookFunctionEntry));
        memset(entry, 0, sizeof(ZzHookFunctionEntry));

        ZzInitializeHookFunctionEntry(entry, HOOK_FUNCTION_TYPE, target_ptr, 0, replace_call_ptr, pre_call_ptr, NULL,
                                      post_call_ptr, try_near_jump);

        if (origin_ptr)
            *origin_ptr = entry->on_invoke_trampoline;

    } while (0);
    return status;
}

ZZSTATUS ZzBuildHookAddress(zpointer target_start_ptr, zpointer target_end_ptr, PRECALL pre_call_ptr,
                            HALFCALL half_call_ptr, zbool try_near_jump) {
#if defined(__i386__) || defined(__x86_64__)
    ZzInfoLog("%s", "x86 & x86_64 arch not support");
    return ZZ_FAILED;
#endif
    ZZSTATUS status = ZZ_DONE_HOOK;
    ZzInterceptor *interceptor = g_interceptor;
    ZzHookFunctionEntrySet *hook_function_entry_set = NULL;
    ZzHookFunctionEntry *entry = NULL;

    if (!interceptor) {
        ZzInitializeInterceptor();
        if (!g_interceptor)
            return ZZ_FAILED;
        if (!g_interceptor->is_support_rx_page) {
            return ZZ_FAILED;
        }
    }

    interceptor = g_interceptor;
    hook_function_entry_set = &(interceptor->hook_function_entry_set);

    do {
        // check is already hooked
        if (ZzFindHookFunctionEntry(target_start_ptr)) {
            status = ZZ_ALREADY_HOOK;
            break;
        }

        entry = (ZzHookFunctionEntry *)malloc(sizeof(ZzHookFunctionEntry));
        memset(entry, 0, sizeof(ZzHookFunctionEntry));

        ZzInitializeHookFunctionEntry(entry, HOOK_ADDRESS_TYPE, target_start_ptr, target_end_ptr, NULL, pre_call_ptr,
                                      half_call_ptr, NULL, try_near_jump);

    } while (0);
    return status;
}

ZZSTATUS ZzEnableHook(zpointer target_ptr) {
    ZZSTATUS status = ZZ_DONE_ENABLE;
    ZzInterceptor *interceptor = g_interceptor;
    ZzHookFunctionEntry *entry = ZzFindHookFunctionEntry(target_ptr);

    if (!entry) {
        status = ZZ_NO_BUILD_HOOK;
        Xinfo(" %p not build HookFunctionEntry!", target_ptr);
        return status;
    }

    if (entry->isEnabled) {
        status = ZZ_ALREADY_ENABLED;
        Xinfo("HookFunctionEntry %p already enable!", target_ptr);
        return status;
    }

    return ZzActivateTrampoline(interceptor->backend, entry);
}

ZZSTATUS ZzHook(zpointer target_ptr, zpointer replace_ptr, zpointer *origin_ptr, PRECALL pre_call_ptr,
                POSTCALL post_call_ptr, zbool try_near_jump) {
    ZzBuildHook(target_ptr, replace_ptr, origin_ptr, pre_call_ptr, post_call_ptr, try_near_jump);
    ZzEnableHook(target_ptr);
    return ZZ_SUCCESS;
}

ZZSTATUS ZzHookPrePost(zpointer target_ptr, PRECALL pre_call_ptr, POSTCALL post_call_ptr) {
    ZzBuildHook(target_ptr, NULL, NULL, pre_call_ptr, post_call_ptr, FALSE);
    ZzEnableHook(target_ptr);
    return ZZ_SUCCESS;
}

ZZSTATUS ZzHookReplace(zpointer target_ptr, zpointer replace_ptr, zpointer *origin_ptr) {
    ZzBuildHook(target_ptr, replace_ptr, origin_ptr, NULL, NULL, FALSE);
    ZzEnableHook(target_ptr);
    return ZZ_SUCCESS;
}

ZZSTATUS ZzHookAddress(zpointer target_start_ptr, zpointer target_end_ptr, PRECALL pre_call_ptr,
                       HALFCALL half_call_ptr) {
    ZzBuildHookAddress(target_start_ptr, target_end_ptr, pre_call_ptr, half_call_ptr, FALSE);
    ZzEnableHook(target_start_ptr);
    return ZZ_SUCCESS;
}

#ifdef TARGET_IS_IOS

ZZSTATUS ZzSolidifyHook(zpointer target_fileoff, zpointer replace_call_ptr, zpointer *origin_ptr, PRECALL pre_call_ptr,
                        POSTCALL post_call_ptr) {
    ZZSTATUS status = ZZ_DONE_HOOK;
    ZzInterceptor *interceptor = g_interceptor;
    ZzHookFunctionEntrySet *hook_function_entry_set = NULL;
    ZzHookFunctionEntry *entry = NULL;

    if (!interceptor) {
        ZzInitializeInterceptor();
        if (!g_interceptor)
            return ZZ_FAILED;
    }

    interceptor = g_interceptor;

    entry = (ZzHookFunctionEntry *)malloc(sizeof(ZzHookFunctionEntry));
    entry->target_ptr = target_fileoff;
    entry->replace_call = replace_call_ptr;
    entry->pre_call = (zpointer)pre_call_ptr;
    entry->post_call = (zpointer)post_call_ptr;

    ZzActivateSolidifyTrampoline(entry, (zaddr)target_fileoff);

    if (origin_ptr)
        *origin_ptr = entry->on_invoke_trampoline;
    return status;
}
#endif
