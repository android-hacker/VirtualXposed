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

#include "relocator-arm.h"

#include <stdlib.h>
#include <string.h>

#define MAX_RELOCATOR_INSTRUCIONS_SIZE 64

void zz_arm_relocator_init(ZzArmRelocator *relocator, zpointer input_code, ZzArmWriter *output) {
    relocator->inpos = 0;
    relocator->outpos = 0;

    relocator->input_start = input_code;
    relocator->input_cur = input_code;
    relocator->input_pc = (zaddr)input_code;

    relocator->input_insns = (ZzInstruction *)malloc(MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzInstruction));
    memset(relocator->input_insns, 0, MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzInstruction));

    relocator->output = output;
}

void zz_arm_relocator_reset(ZzArmRelocator *self, zpointer input_code, ZzArmWriter *output) {
    self->input_cur = input_code;
    self->input_start = input_code;
    self->input_pc = (zaddr)input_code;

    self->inpos = 0;
    self->outpos = 0;

    self->output = output;
}

zsize zz_arm_relocator_read_one(ZzArmRelocator *self, ZzInstruction *instruction) {
    ZzInstruction *insn_ctx = &self->input_insns[self->inpos];

    zz_arm_reader_read_one_instruction(insn_ctx, self->input_cur);

    // switch (1) {}

    self->inpos++;

    if (instruction != NULL)
        *instruction = *insn_ctx;

    self->input_cur += insn_ctx->size;
    self->input_pc += insn_ctx->size;

    return self->input_cur - self->input_start;
}
void zz_arm_relocator_try_relocate(zpointer address, zuint min_bytes, zuint *max_bytes) {
    *max_bytes = 16;
    return;
}

void zz_arm_relocator_write_all(ZzArmRelocator *self) {
    zuint count = 0;
    while (zz_arm_relocator_write_one(self))
        count++;
}
// PAGE: A8-312
static zbool zz_arm_relocator_rewrite_ADD_register_A1(ZzArmRelocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;

    zuint32 Rn_ndx, Rd_ndx, Rm_ndx;
    Rn_ndx = get_insn_sub(insn, 16, 4);
    Rd_ndx = get_insn_sub(insn, 12, 4);
    Rm_ndx = get_insn_sub(insn, 0, 4);

    if (Rn_ndx != ZZ_ARM_REG_PC) {
        return FALSE;
    }
    // push R7
    zz_arm_writer_put_push_reg(self->output, ZZ_ARM_REG_R7);
    zz_arm_writer_put_ldr_b_reg_address(self->output, ZZ_ARM_REG_R7, insn_ctx->pc);
    zz_arm_writer_put_instruction(self->output, (insn & 0xFFF0FFFF) | ZZ_ARM_REG_R7 << 16);
    // pop R7
    zz_arm_writer_put_pop_reg(self->output, ZZ_ARM_REG_R7);
    return TRUE;
}

// PAGE: A8-410
static zbool zz_arm_relocator_rewrite_LDR_literal_A1(ZzArmRelocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm12 = get_insn_sub(insn, 0, 12);
    zuint32 imm32 = imm12;
    zbool add = get_insn_sub(insn, 7 + 16, 1) == 1;
    zaddr target_address;
    if (add)
        target_address = insn_ctx->pc + imm32;
    else
        target_address = insn_ctx->pc - imm32;
    int Rt_ndx = get_insn_sub(insn, 12, 4);

    zz_arm_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);
    zz_arm_writer_put_ldr_reg_reg_imm(self->output, Rt_ndx, Rt_ndx, 0);
    return TRUE;
}

// PAGE: A8-322
static zbool zz_arm_relocator_rewrite_ADR_A1(ZzArmRelocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm12 = get_insn_sub(insn, 0, 12);
    zuint32 imm32 = imm12;
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;
    int Rt_ndx = get_insn_sub(insn, 12, 4);
    zz_arm_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);
    return TRUE;
}

// PAGE: A8-322
static zbool zz_arm_relocator_rewrite_ADR_A2(ZzArmRelocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm12 = get_insn_sub(insn, 0, 12);
    zuint32 imm32 = imm12;
    zaddr target_address;
    target_address = insn_ctx->pc - imm32;
    int Rt_ndx = get_insn_sub(insn, 12, 4);
    zz_arm_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);
    return TRUE;
}

// 0x000 : b.cond 0x0;
// 0x004 : b 0x4
// 0x008 : ldr pc, [pc, #0]
// 0x00c : .long 0x0
// 0x010 : remain code

// PAGE: A8-334
static zbool zz_arm_relocator_rewrite_B_A1(ZzArmRelocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm24 = get_insn_sub(insn, 0, 24);
    zuint32 imm32 = imm24 << 2;
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;

    zz_arm_writer_put_instruction(self->output, (insn & 0xFF000000) | 0);
    zz_arm_writer_put_b_imm(self->output, 0x4);
    zz_arm_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);
    return TRUE;
}

// 0x000 : bl.cond 0x0;

// 0x004 : b 0x10

// 0x008 : ldr lr, [pc, #0]
// 0x00c : b 0x0
// 0x010 : .long 0x0

// 0x014 : ldr pc, [pc, #0]
// 0x018 : .long 0x0

// 0x01c : remain code

// PAGE: A8-348
static zbool zz_arm_relocator_rewrite_BLBLX_immediate_A1(ZzArmRelocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm24 = get_insn_sub(insn, 0, 24);
    zuint32 imm32 = imm24 << 2;
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;

    zz_arm_writer_put_instruction(self->output, (insn & 0xFF000000) | 0);
    zz_arm_writer_put_b_imm(self->output, 0x10);
    zz_arm_writer_put_ldr_b_reg_address(self->output, ZZ_ARM_REG_LR, insn_ctx->pc - 4);
    zz_arm_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);
    return TRUE;
}

// PAGE: A8-348
static zbool zz_arm_relocator_rewrite_BLBLX_immediate_A2(ZzArmRelocator *self, const ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 H = get_insn_sub(insn, 24, 1);
    zuint32 imm24 = get_insn_sub(insn, 0, 24);
    zuint32 imm32 = (imm24 << 2) | (H << 1);
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;
    zz_arm_writer_put_ldr_b_reg_address(self->output, ZZ_ARM_REG_LR, insn_ctx->pc - 4);
    zz_arm_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);
    return TRUE;
}

zbool zz_arm_relocator_write_one(ZzArmRelocator *self) {
    const ZzInstruction *insn_ctx;
    zbool rewritten = FALSE;

    if (self->inpos != self->outpos) {
        insn_ctx = &self->input_insns[self->outpos];
        self->outpos++;
    } else
        return FALSE;

    switch (GetARMInsnType(insn_ctx->insn)) {
    case ARM_INS_ADD_register_A1:
        rewritten = zz_arm_relocator_rewrite_ADD_register_A1(self, insn_ctx);
        break;
    case ARM_INS_LDR_literal_A1:
        rewritten = zz_arm_relocator_rewrite_LDR_literal_A1(self, insn_ctx);
        break;
    case ARM_INS_ADR_A1:
        rewritten = zz_arm_relocator_rewrite_ADR_A1(self, insn_ctx);
        break;
    case ARM_INS_ADR_A2:
        rewritten = zz_arm_relocator_rewrite_ADR_A2(self, insn_ctx);
        break;
    case ARM_INS_B_A1:
        rewritten = zz_arm_relocator_rewrite_B_A1(self, insn_ctx);
        break;
    case ARM_INS_BLBLX_immediate_A1:
        rewritten = zz_arm_relocator_rewrite_BLBLX_immediate_A1(self, insn_ctx);
        break;
    case ARM_INS_BLBLX_immediate_A2:
        rewritten = zz_arm_relocator_rewrite_BLBLX_immediate_A2(self, insn_ctx);
        break;
    case ARM_UNDEF:
        rewritten = FALSE;
        break;
    }
    if (!rewritten)
        zz_arm_writer_put_bytes(self->output, (zbyte *)&insn_ctx->insn, insn_ctx->size);
    return TRUE;
}
