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

#ifndef zzdeps_posix_memory_utils_posix_h
#define zzdeps_posix_memory_utils_posix_h

#include <err.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>

#include "../common/memory-utils-common.h"
#include "../zz.h"

zsize zz_posix_vm_get_page_size();

zbool zz_posix_vm_check_address_valid_via_msync(const zpointer p);

zbool zz_posix_vm_check_address_valid_via_signal(zpointer p);

zbool zz_posix_vm_protect(const zaddr address, zsize size, int page_prot);

zbool zz_posix_vm_protect_as_executable(const zaddr address, zsize size);

zbool zz_posxi_vm_protect_as_writable(const zaddr address, zsize size);

zpointer zz_posix_vm_allocate_pages(zsize n_pages);

zpointer zz_posix_vm_allocate(zsize size);

zpointer zz_posix_vm_allocate_near_pages(zaddr address, zsize range_size, zsize n_pages);

zpointer zz_posix_vm_search_text_code_cave(zaddr address, zsize range_size, zsize size);

zbool zz_posix_vm_patch_code(const zaddr address, const zpointer codedata, zuint codedata_size);

#endif