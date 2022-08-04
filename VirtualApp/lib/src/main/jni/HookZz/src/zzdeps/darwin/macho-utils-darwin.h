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

#ifndef zzdeps_darwin_macho_utils_h
#define zzdeps_darwin_macho_utils_h

#include <err.h>
#include <mach/task.h>
#include <stdio.h>
#include <stdlib.h>

#include <mach-o/dyld.h>

#include "../zz.h"

zpointer zz_macho_get_dyld_load_address_via_task(task_t task);

task_t zz_darwin_get_task_via_pid(int pid);

struct section_64 *zz_macho_get_section_64_via_name(struct mach_header_64 *header, char *sect_name);

struct segment_command_64 *zz_macho_get_segment_64_via_name(struct mach_header_64 *header,
                                                            char *segment_name);

zpointer zz_macho_get_section_64_address_via_name(struct mach_header_64 *header, char *sect_name);

zpointer zz_macho_get_symbol_via_name(struct mach_header_64 *header, const char *name);

struct load_command *zz_macho_get_load_command_via_cmd(struct mach_header_64 *header, zuint32 cmd);

#endif
