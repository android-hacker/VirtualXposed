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

#ifndef platforms_arch_x86_relocator_h
#define platforms_arch_x86_relocator_h

// platforms
#include "instructions.h"
#include "reader-x86.h"
#include "regs-x86.h"
#include "writer-x86.h"

// hookzz
#include "writer.h"

// zzdeps
#include "hookzz.h"
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

typedef struct _ZzX86Relocator {
    zbool try_relocated_again;
    zsize try_relocated_length;
    zpointer input_start;
    zpointer input_cur;
    zaddr input_pc;
    zuint inpos;
    zuint outpos;
    ZzInstruction *input_insns;
    ZzRelocateInstruction *output_insns;
    ZzX86Writer *output;
    ZzLiteralInstruction **relocate_literal_insns;
    zsize relocate_literal_insns_size;
} ZzX86Relocator;

void zz_x86_relocator_init(ZzX86Relocator *relocator, zpointer input_code, ZzX86Writer *writer);
void zz_x86_relocator_reset(ZzX86Relocator *self, zpointer input_code, ZzX86Writer *output);

zsize zz_x86_relocator_read_one(ZzX86Relocator *self, ZzInstruction *instruction);
zbool zz_x86_relocator_write_one(ZzX86Relocator *self);
void zz_x86_relocator_write_all(ZzX86Relocator *self);
void zz_x86_relocator_try_relocate(zpointer address, zuint min_bytes, zuint *max_bytes);

#endif