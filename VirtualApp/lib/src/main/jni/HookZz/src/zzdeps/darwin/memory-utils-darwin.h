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

#ifndef zzdeps_darwin_memory_utils_darwin_h
#define zzdeps_darwin_memory_utils_darwin_h

#include <err.h>
#include <stdio.h>
#include <stdlib.h>

#include <mach/mach_error.h>
#include <mach/task.h>

#if defined(__arm64__) || defined(__arm__)

#include "mach_vm.h"

#else
#include <mach/mach_vm.h>
#endif

#include "../posix/memory-utils-posix.h"
#include "../zz.h"

#define KR_ERROR(kr)                                                                                                   \
    {                                                                                                                  \
        Xerror("kr = %d, reason: %s!", kr, mach_error_string(kr));                                                     \
        debug_break();                                                                                                 \
    }
#define KR_ERROR_AT(kr, address)                                                                                       \
    {                                                                                                                  \
        Xerror("kr = %d, at %p, reason: %s!", kr, (zpointer)address, mach_error_string(kr));                           \
        debug_break();                                                                                                 \
    }

zbool zz_vm_read_data_via_task(task_t task, const zaddr address, zpointer buffer, zsize length);

char *zz_vm_read_string_via_task(task_t task, const zaddr address);

zaddr zz_vm_search_data_via_task(task_t task, const zaddr start_addr, const zaddr end_addr, zbyte *data,
                                 zsize data_len);

zbool zz_vm_check_address_valid_via_task(task_t task, const zaddr address);

zbool zz_vm_can_allocate_rx_page();

zbool zz_vm_protect_via_task(task_t task, const zaddr address, zsize size, vm_prot_t page_prot);

zbool zz_vm_protect_as_executable_via_task(task_t task, const zaddr address, zsize size);

zbool zz_vm_protect_as_writable_via_task(task_t task, const zaddr address, zsize size);

zbool zz_vm_get_page_info_via_task(task_t task, const zaddr address, vm_prot_t *prot_p, vm_inherit_t *inherit_p);

zpointer zz_vm_allocate_pages_via_task(task_t task, zsize n_pages);

zpointer zz_vm_allocate_near_pages_via_task(task_t task, zaddr address, zsize range_size, zsize n_pages);

zpointer zz_vm_allocate_via_task(task_t task, zsize size);

zpointer zz_vm_search_text_code_cave_via_task(task_t task, zaddr address, zsize range_size, zsize *size_ptr);

zpointer zz_vm_search_text_code_cave_via_dylibs(zaddr address, zsize range_size, zsize size);

zpointer zz_vm_search_code_cave(zaddr address, zsize range_size, zsize size);

zbool zz_vm_patch_code_via_task(task_t task, const zaddr address, const zpointer codedata, zuint codedata_size);

#endif
