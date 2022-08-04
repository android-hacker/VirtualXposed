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

#ifndef platforms_arch_arm_relocator_arm_h
#define platforms_arch_arm_relocator_arm_h

// platforms
#include "instructions.h"
#include "reader-arm.h"
#include "regs-arm.h"
#include "writer-arm.h"

// hookzz
#include "writer.h"

// zzdeps
#include "hookzz.h"
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

typedef struct _ZzArmRelocator {
    zbool try_relocated_again;
    zsize try_relocated_length;
    zpointer input_start;
    zpointer input_cur;
    zaddr input_pc;
    zuint inpos;
    zuint outpos;
    ZzInstruction *input_insns;
    ZzRelocateInstruction *output_insns;
    ZzLiteralInstruction **relocate_literal_insns;
    zsize relocate_literal_insns_size;
    ZzArmWriter *output;
} ZzArmRelocator;

void zz_arm_relocator_init(ZzArmRelocator *relocator, zpointer input_code, ZzArmWriter *output);
void zz_arm_relocator_reset(ZzArmRelocator *self, zpointer input_code, ZzArmWriter *output);
void zz_arm_relocator_write_all(ZzArmRelocator *self);
zsize zz_arm_relocator_read_one(ZzArmRelocator *self, ZzInstruction *instruction);
void zz_arm_relocator_try_relocate(zpointer address, zuint min_bytes, zuint *max_bytes);
zbool zz_arm_relocator_write_one(ZzArmRelocator *self);
void zz_arm_relocator_relocate_writer(ZzArmRelocator *relocator, zaddr code_address);
#endif