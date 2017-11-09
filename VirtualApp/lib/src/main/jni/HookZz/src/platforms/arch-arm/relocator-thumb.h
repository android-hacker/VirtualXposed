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

#ifndef platforms_arch_arm_relocator_thumb_h
#define platforms_arch_arm_relocator_thumb_h

// platforms
#include "instructions.h"
#include "reader-thumb.h"
#include "regs-arm.h"
#include "writer-thumb.h"

// hookzz
#include "writer.h"

// zzdeps
#include "hookzz.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

typedef struct _ZzThumbRelocator {
    zbool try_relocated_again;
    zsize try_relocated_length;
    zpointer input_start;
    zpointer input_cur;
    zaddr input_pc;
    ZzInstruction *input_insns;
    ZzRelocateInstruction *output_insns;
    ZzLiteralInstruction **relocate_literal_insns;
    zsize relocate_literal_insns_size;
    ZzThumbWriter *output;
    zuint inpos;
    zuint outpos;
} ZzThumbRelocator;

void zz_thumb_relocator_init(ZzThumbRelocator *relocator, zpointer input_code, ZzThumbWriter *writer);
void zz_thumb_relocator_reset(ZzThumbRelocator *self, zpointer input_code, ZzThumbWriter *output);
zsize zz_thumb_relocator_read_one(ZzThumbRelocator *self, ZzInstruction *instruction);
zbool zz_thumb_relocator_write_one(ZzThumbRelocator *self);
void zz_thumb_relocator_relocate_writer(ZzThumbRelocator *relocator, zaddr code_address);
void zz_thumb_relocator_write_all(ZzThumbRelocator *self);
void zz_thumb_relocator_try_relocate(zpointer address, zuint min_bytes, zuint *max_bytes);

#endif