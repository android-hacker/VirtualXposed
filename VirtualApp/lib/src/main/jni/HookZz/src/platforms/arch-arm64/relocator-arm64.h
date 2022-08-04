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

#ifndef platforms_arch_arm64_relocator_h
#define platforms_arch_arm64_relocator_h

// platforms
#include "instructions.h"
#include "reader-arm64.h"
#include "regs-arm64.h"
#include "writer-arm64.h"

// hookzz
#include "writer.h"

// zzdeps
#include "hookzz.h"
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

typedef struct _ZzArm64Relocator {
    zbool try_relocated_again;
    zsize try_relocated_length;
    zpointer input_start;
    zpointer input_cur;
    zaddr input_pc;
    zuint inpos;
    zuint outpos;
    ZzInstruction *input_insns;
    ZzRelocateInstruction *output_insns;
    ZzArm64Writer *output;
    ZzLiteralInstruction **relocate_literal_insns;
    zsize relocate_literal_insns_size;
} ZzArm64Relocator;

void zz_arm64_relocator_init(ZzArm64Relocator *relocator, zpointer input_code, ZzArm64Writer *writer);
void zz_arm64_relocator_reset(ZzArm64Relocator *self, zpointer input_code, ZzArm64Writer *output);

zsize zz_arm64_relocator_read_one(ZzArm64Relocator *self, ZzInstruction *instruction);
zbool zz_arm64_relocator_write_one(ZzArm64Relocator *self);
void zz_arm64_relocator_write_all(ZzArm64Relocator *self);
void zz_arm64_relocator_try_relocate(zpointer address, zuint min_bytes, zuint *max_bytes);

/* rewrite */
static zbool zz_arm64_relocator_rewrite_ldr(ZzArm64Relocator *self, const ZzInstruction *insn_ctx,
                                            ZzRelocateInstruction *re_insn_ctx);
static zbool zz_arm64_relocator_rewrite_adr(ZzArm64Relocator *self, const ZzInstruction *insn_ctx,
                                            ZzRelocateInstruction *re_insn_ctx);
static zbool zz_arm64_relocator_rewrite_b(ZzArm64Relocator *self, const ZzInstruction *insn_ctx,
                                          ZzRelocateInstruction *re_insn_ctx);
static zbool zz_arm64_relocator_rewrite_b_cond(ZzArm64Relocator *self, const ZzInstruction *insn_ctx,
                                               ZzRelocateInstruction *re_insn_ctx);
static zbool zz_arm64_relocator_rewrite_bl(ZzArm64Relocator *self, const ZzInstruction *insn_ctx,
                                           ZzRelocateInstruction *re_insn_ctx);
#endif