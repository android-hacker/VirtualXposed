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

#include "relocator-arm64.h"
#include <stdlib.h>
#include <string.h>

#define MAX_RELOCATOR_INSTRUCIONS_SIZE 64

void zz_arm64_relocator_init(ZzArm64Relocator *relocator, zpointer input_code, ZzArm64Writer *output) {
    relocator->inpos = 0;
    relocator->outpos = 0;

    relocator->input_start = input_code;
    relocator->input_cur = input_code;
    relocator->input_pc = (zaddr)input_code;
    relocator->input_insns = (ZzInstruction *)malloc(MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzInstruction));

    memset(relocator->input_insns, 0, MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzInstruction));

    relocator->output = output;
}

void zz_arm64_relocator_reset(ZzArm64Relocator *self, zpointer input_code, ZzArm64Writer *output) {
    self->input_cur = input_code;
    self->input_start = input_code;
    self->input_pc = (zaddr)input_code;

    self->inpos = 0;
    self->outpos = 0;

    self->output = output;
}

zsize zz_arm64_relocator_read_one(ZzArm64Relocator *self, ZzInstruction *instruction) {
    ZzInstruction *insn_ctx = &self->input_insns[self->inpos];

    zz_arm64_reader_read_one_instruction(insn_ctx, self->input_cur);

    // switch (0) {}

    self->inpos++;

    if (instruction != NULL)
        instruction = insn_ctx;

    self->input_cur += insn_ctx->size;
    self->input_pc += insn_ctx->size;

    return self->input_cur - self->input_start;
}

void zz_arm64_relocator_write_all(ZzArm64Relocator *self) {
    zuint count = 0;
    while (zz_arm64_relocator_write_one(self))
        count++;
}

void zz_arm64_relocator_try_relocate(zpointer address, zuint min_bytes, zuint *max_bytes) {
    *max_bytes = 16;
    return;
}

// PAGE: C6-673
static zbool zz_arm64_relocator_rewrite_LDR_literal(ZzArm64Relocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    // TODO: check opc == 10, with signed
    zuint32 imm19 = get_insn_sub(insn, 5, 19);
    zuint64 offset = imm19 << 2;

    zaddr target_address;
    target_address = insn_ctx->pc + offset;
    int Rt_ndx = get_insn_sub(insn, 0, 4);

    zz_arm64_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);
    zz_arm64_writer_put_ldr_reg_reg_offset(self->output, Rt_ndx, Rt_ndx, 0);
    return TRUE;
}

// PAGE: C6-535
static zbool zz_arm64_relocator_rewrite_ADR(ZzArm64Relocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 immhi = get_insn_sub(insn, 5, 19);
    zuint32 immlo = get_insn_sub(insn, 29, 2);
    zuint64 imm = immhi << 2 | immlo;

    zaddr target_address;
    target_address = insn_ctx->pc + imm;
    int Rt_ndx = get_insn_sub(insn, 0, 4);

    zz_arm64_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);
    return TRUE;
}

// PAGE: C6-536
static zbool zz_arm64_relocator_rewrite_ADRP(ZzArm64Relocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 immhi = get_insn_sub(insn, 5, 19);
    zuint32 immlo = get_insn_sub(insn, 29, 2);
    // 12 is PAGE-SIZE
    zuint64 imm = immhi << 2 << 12 | immlo << 12;

    zaddr target_address;
    target_address = (insn_ctx->pc & 0xFFFFFFFFFFFFF000) + imm;
    int Rt_ndx = get_insn_sub(insn, 0, 4);

    zz_arm64_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);
    return TRUE;
}

// PAGE: C6-550
static zbool zz_arm64_relocator_rewrite_B(ZzArm64Relocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm26 = get_insn_sub(insn, 0, 26);

    zuint64 offset = imm26 << 2;

    zaddr target_address;
    target_address = insn_ctx->pc + offset;
    int Rt_ndx = get_insn_sub(insn, 0, 4);

    zz_arm64_writer_put_ldr_br_reg_address(self->output, ZZ_ARM64_REG_X17, target_address);
    return TRUE;
}

// PAGE: C6-560
static zbool zz_arm64_relocator_rewrite_BL(ZzArm64Relocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm26 = get_insn_sub(insn, 0, 26);

    zuint64 offset = imm26 << 2;

    zaddr target_address;
    target_address = insn_ctx->pc + offset;
    int Rt_ndx = get_insn_sub(insn, 0, 4);

    zz_arm64_writer_put_ldr_blr_b_reg_address(self->output, ZZ_ARM64_REG_X17, target_address);
    return TRUE;
}

// 0x000 : b.cond 0x8;

// 0x004 : b 0x14

// 0x008 : ldr x17, [pc, #4]
// 0x00c : br x17
// 0x010 : .long 0x0
// 0x014 : .long 0x0

// 0x018 : remain code

// PAGE: C6-549
static zbool zz_arm64_relocator_rewrite_B_cond(ZzArm64Relocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm19 = get_insn_sub(insn, 5, 19);

    zuint64 offset = imm19 << 2;

    zaddr target_address;
    target_address = insn_ctx->pc + offset;

    zuint32 cond = get_insn_sub(insn, 0, 4);

    zz_arm64_writer_put_b_cond_imm(self->output, cond, 0x8);
    zz_arm64_writer_put_b_imm(self->output, 0x14);
    zz_arm64_writer_put_ldr_br_reg_address(self->output, ZZ_ARM64_REG_X17, target_address);

    return TRUE;
}

zbool zz_arm64_relocator_write_one(ZzArm64Relocator *self) {
    const ZzInstruction *insn_ctx;
    zbool rewritten = FALSE;

    if (self->inpos != self->outpos) {
        insn_ctx = &self->input_insns[self->outpos];
        self->outpos++;
    } else
        return FALSE;

    switch (GetARM64InsnType(insn_ctx->insn)) {
    case ARM64_INS_LDR_literal:
        rewritten = zz_arm64_relocator_rewrite_LDR_literal(self, insn_ctx);
        break;
    case ARM64_INS_ADR:
        rewritten = zz_arm64_relocator_rewrite_ADR(self, insn_ctx);
        break;
    case ARM64_INS_ADRP:
        rewritten = zz_arm64_relocator_rewrite_ADRP(self, insn_ctx);
        break;
    case ARM64_INS_B:
        rewritten = zz_arm64_relocator_rewrite_B(self, insn_ctx);
        break;
    case ARM64_INS_BL:
        rewritten = zz_arm64_relocator_rewrite_BL(self, insn_ctx);
        break;
    case ARM64_INS_B_cond:
        rewritten = zz_arm64_relocator_rewrite_B_cond(self, insn_ctx);
        break;
    default:
        rewritten = FALSE;
        break;
    }
    if (!rewritten)
        zz_arm64_writer_put_bytes(self->output, (zbyte *)&insn_ctx->insn, insn_ctx->size);
    return TRUE;
}