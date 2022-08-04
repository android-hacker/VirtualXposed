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

#include "relocator-x86.h"
#include <stdlib.h>
#include <string.h>

#define MAX_RELOCATOR_INSTRUCIONS_SIZE 64

void zz_x86_relocator_init(ZzX86Relocator *relocator, zpointer input_code, ZzX86Writer *output) {
}

void zz_x86_relocator_reset(ZzX86Relocator *self, zpointer input_code, ZzX86Writer *output) {
}

zsize zz_x86_relocator_read_one(ZzX86Relocator *self, ZzInstruction *instruction) {
    return 0;
}

zaddr zz_x86_relocator_get_insn_relocated_offset(ZzX86Relocator *self, zaddr address) {
    return 0;
}

void zz_x86_relocator_relocate_writer(ZzX86Relocator *relocator, zaddr code_address) {
}

void zz_x86_relocator_write_all(ZzX86Relocator *self) {
}

void zz_x86_relocator_try_relocate(zpointer address, zuint min_bytes, zuint *max_bytes) {
}

zbool zz_x86_relocator_write_one(ZzX86Relocator *self) {
    return TRUE;
}