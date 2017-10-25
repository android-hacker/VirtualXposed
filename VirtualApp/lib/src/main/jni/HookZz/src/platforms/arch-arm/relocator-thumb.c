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

#include <stdlib.h>
#include <string.h>

#include "relocator-thumb.h"

#define MAX_RELOCATOR_INSTRUCIONS_SIZE 64

void zz_thumb_relocator_init(ZzThumbRelocator *relocator, zpointer input_code, ZzThumbWriter *writer) {
    relocator->inpos = 0;
    relocator->outpos = 0;

    relocator->input_start = input_code;
    relocator->input_cur = input_code;
    relocator->input_pc = (zaddr)input_code;
    relocator->input_insns = (ZzInstruction *)malloc(MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzInstruction));
    memset(relocator->input_insns, 0, MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzInstruction));
}

void zz_thumb_relocator_reset(ZzThumbRelocator *self, zpointer input_code, ZzThumbWriter *output) {
    self->input_cur = input_code;
    self->input_start = input_code;
    self->input_pc = (zaddr)input_code;

    self->inpos = 0;
    self->outpos = 0;

    self->output = output;
}

zsize zz_thumb_relocator_read_one(ZzThumbRelocator *self, ZzInstruction *instruction) {
    ZzInstruction *insn_ctx = &self->input_insns[self->inpos];

    zz_thumb_reader_read_one_instruction(insn_ctx, self->input_cur);

    // switch (1) {}

    self->inpos++;

    if (instruction != NULL)
        instruction = insn_ctx;

    self->input_cur += insn_ctx->size;
    self->input_pc += insn_ctx->size;

    return self->input_cur - self->input_start;
}

void zz_thumb_relocator_try_relocate(zpointer address, zuint min_bytes, zuint *max_bytes) {
    *max_bytes = 16;
    return;
}

void zz_thumb_relocator_write_all(ZzThumbRelocator *self) {
    zuint count = 0;
    while (zz_thumb_relocator_write_one(self))
        count++;
}

// PAGE: A8-410
zbool zz_thumb_relocator_rewrite_LDR_T1(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn1 = insn_ctx->insn1;
    zuint32 imm8 = get_insn_sub(insn1, 0, 8);
    zuint32 imm32 = imm8 << 2;
    zaddr target_address = insn_ctx->pc + imm32;
    int Rt_ndx = get_insn_sub(insn1, 8, 3);
    zz_thumb_writer_put_ldr_reg_address(self->output, Rt_ndx, target_address);
    zz_thumb_writer_put_ldr_reg_reg_offset(self->output, Rt_ndx, Rt_ndx, 0);
    return TRUE;
}

// PAGE: A8-410
zbool zz_thumb_relocator_rewrite_LDR_T2(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn1 = insn_ctx->insn1;
    zuint32 insn2 = insn_ctx->insn2;

    zuint32 imm12 = get_insn_sub(insn2, 0, 12);
    zuint32 imm32 = imm12;

    zbool add = get_insn_sub(insn_ctx->insn1, 7, 1) == 1;
    zaddr target_address;
    if (add)
        target_address = insn_ctx->pc + imm32;
    else
        target_address = insn_ctx->pc - imm32;
    int Rt_ndx = get_insn_sub(insn_ctx->insn2, 12, 4);

    zz_thumb_writer_put_ldr_reg_address(self->output, Rt_ndx, target_address);
    zz_thumb_writer_put_ldr_reg_reg_offset(self->output, Rt_ndx, Rt_ndx, 0);
    return TRUE;
}

// PAGE: A8-322
zbool zz_thumb_relocator_rewrite_ADR_T1(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn1 = insn_ctx->insn1;

    zuint32 imm8 = get_insn_sub(insn1, 0, 8);
    zuint32 imm32 = imm8 << 2;
    zaddr target_address = insn_ctx->pc + imm32;
    int Rt_ndx = get_insn_sub(insn1, 8, 3);

    zz_thumb_writer_put_ldr_reg_address(self->output, Rt_ndx, target_address);
    return TRUE;
}

// PAGE: A8-322
zbool zz_thumb_relocator_rewrite_ADR_T2(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn1 = insn_ctx->insn1;
    zuint32 insn2 = insn_ctx->insn2;

    zuint32 imm32 =
        get_insn_sub(insn2, 0, 8) | (get_insn_sub(insn2, 12, 3) << 8) | ((get_insn_sub(insn1, 10, 1) << (3 + 8)));

    zaddr target_address;
    target_address = insn_ctx->pc - imm32;
    int Rt_ndx = get_insn_sub(insn_ctx->insn2, 8, 4);
    zz_thumb_writer_put_ldr_reg_address(self->output, Rt_ndx, target_address);
    return TRUE;
}

// PAGE: A8-322
zbool zz_thumb_relocator_rewrite_ADR_T3(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn1 = insn_ctx->insn1;
    zuint32 insn2 = insn_ctx->insn2;

    zuint32 imm32 =
        get_insn_sub(insn2, 0, 8) | (get_insn_sub(insn2, 12, 3) << 8) | ((get_insn_sub(insn1, 10, 1) << (3 + 8)));

    zaddr target_address;
    target_address = insn_ctx->pc + imm32;
    int Rt_ndx = get_insn_sub(insn_ctx->insn2, 8, 4);
    zz_thumb_writer_put_ldr_reg_address(self->output, Rt_ndx, target_address);
    return TRUE;
}

// PAGE: A8-334
zbool zz_thumb_relocator_rewrite_B_T1(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn1 = insn_ctx->insn1;
    // zuint32 insn2 = insn_ctx->insn2;

    zuint32 imm8 = get_insn_sub(insn1, 0, 8);
    zuint32 imm32 = imm8 << 1;
    zaddr target_address = insn_ctx->pc + imm32;

    zz_thumb_writer_put_instruction(self->output, (insn1 & 0xFF00));
    zz_thumb_writer_put_b_imm(self->output, 0x6);
    zz_thumb_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);
    return TRUE;
}

// PAGE: A8-334
zbool zz_thumb_relocator_rewrite_B_T2(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn1 = insn_ctx->insn1;
    // zuint32 insn2 = insn_ctx->insn2;

    zuint32 imm11 = get_insn_sub(insn1, 0, 11);
    zuint32 imm32 = imm11 << 1;
    zaddr target_address = insn_ctx->pc + imm32;

    zz_thumb_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);
    return TRUE;
}

// PAGE: A8-334
zbool zz_thumb_relocator_rewrite_B_T3(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn1 = insn_ctx->insn1;
    zuint32 insn2 = insn_ctx->insn2;

    int S = get_insn_sub(insn_ctx->insn1, 10, 1);
    int J2 = get_insn_sub(insn_ctx->insn2, 11, 1);
    int J1 = get_insn_sub(insn_ctx->insn2, 13, 1);
    int imm6 = get_insn_sub(insn_ctx->insn1, 0, 6);
    int imm11 = get_insn_sub(insn_ctx->insn2, 0, 11);
    zuint32 imm32 =
        imm11 << 1 | imm6 << (1 + 11) | J1 << (1 + 11 + 6) | J2 << (1 + 11 + 6 + 1) | S << (1 + 11 + 6 + 1 + 1);
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;

    zz_thumb_writer_put_instruction(self->output, (insn_ctx->insn & 0b11010000000000001111101111000000) | 0b1);
    zz_thumb_writer_put_b_imm(self->output, 0x6);
    zz_thumb_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);
    return TRUE;
}

// PAGE: A8-334
zbool zz_thumb_relocator_rewrite_B_T4(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn1 = insn_ctx->insn1;
    zuint32 insn2 = insn_ctx->insn2;

    zuint32 S = get_insn_sub(insn_ctx->insn1, 10 + 16, 1);
    zuint32 J2 = get_insn_sub(insn_ctx->insn2, 11, 1);
    zuint32 J1 = get_insn_sub(insn_ctx->insn2, 13, 1);
    zuint32 imm10 = get_insn_sub(insn_ctx->insn1, 0, 10);
    zuint32 imm11 = get_insn_sub(insn_ctx->insn2, 0, 11);
    zuint32 I1 = (~(J1 ^ S));
    zuint32 I2 = (~(J2 ^ S));
    zuint32 imm32 =
        imm11 << 1 | imm10 << (1 + 11) | I1 << (1 + 11 + 6) | I2 << (1 + 11 + 6 + 1) | S << (1 + 11 + 6 + 1 + 1);
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;

    zz_thumb_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);
    return TRUE;
}

// PAGE: A8-348
zbool zz_thumb_relocator_rewrite_BLBLX_T1(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn1 = insn_ctx->insn1;
    zuint32 insn2 = insn_ctx->insn2;

    zuint32 S = get_insn_sub(insn_ctx->insn1, 10, 1);
    zuint32 J2 = get_insn_sub(insn_ctx->insn2, 11, 1);
    zuint32 J1 = get_insn_sub(insn_ctx->insn2, 13, 1);
    zuint32 imm10 = get_insn_sub(insn_ctx->insn1, 0, 10);
    zuint32 imm11 = get_insn_sub(insn_ctx->insn2, 0, 11);
    zuint32 I1 = (~(J1 ^ S));
    zuint32 I2 = (~(J2 ^ S));
    zuint32 imm32 =
        imm11 << 1 | imm10 << (1 + 11) | I1 << (1 + 11 + 6) | I2 << (1 + 11 + 6 + 1) | S << (1 + 11 + 6 + 1 + 1);
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;

    zz_thumb_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_LR, insn_ctx->pc + 2 * 4);
    zz_thumb_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address + 1);
    return TRUE;
}

// PAGE: A8-348
zbool zz_thumb_relocator_rewrite_BLBLX_T2(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn1 = insn_ctx->insn1;
    zuint32 insn2 = insn_ctx->insn2;

    zuint32 S = get_insn_sub(insn_ctx->insn1, 10, 1);
    zuint32 J2 = get_insn_sub(insn_ctx->insn2, 11, 1);
    zuint32 J1 = get_insn_sub(insn_ctx->insn2, 13, 1);
    zuint32 imm10_1 = get_insn_sub(insn_ctx->insn1, 0, 10);
    zuint32 imm10_16 = get_insn_sub(insn_ctx->insn2, 1, 10);
    zuint32 I1 = (~(J1 ^ S));
    zuint32 I2 = (~(J2 ^ S));
    zuint32 imm32 =
        imm10_1 << 2 | imm10_16 << (2 + 10) | I1 << (2 + 10 + 6) | I2 << (2 + 10 + 6 + 1) | S << (2 + 10 + 6 + 1 + 1);
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;

    zz_thumb_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_LR, insn_ctx->pc + 2 * 4);
    zz_thumb_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address + 1);
    return TRUE;
}

zbool zz_thumb_relocator_write_one(ZzThumbRelocator *self) {
    ZzInstruction *insn_ctx;
    zbool rewritten = FALSE;

    if (self->inpos != self->outpos) {
        insn_ctx = &self->input_insns[self->outpos];
        self->outpos++;
    } else
        return FALSE;

    insn_ctx->pc = self->input_pc + 4;

    switch (GetTHUMBInsnType(insn_ctx->insn)) {
    case THUMB_INS_LDR_T1:
        rewritten = zz_thumb_relocator_rewrite_LDR_T1(self, insn_ctx);
        break;
    case THUMB_INS_LDR_T2:
        rewritten = zz_thumb_relocator_rewrite_LDR_T2(self, insn_ctx);
        break;
    case THUMB_INS_ADR_T1:
        rewritten = zz_thumb_relocator_rewrite_ADR_T1(self, insn_ctx);
        break;
    case THUMB_INS_ADR_T2:
        rewritten = zz_thumb_relocator_rewrite_ADR_T2(self, insn_ctx);
        break;
    case THUMB_INS_ADR_T3:
        rewritten = zz_thumb_relocator_rewrite_ADR_T3(self, insn_ctx);
        break;
    case THUMB_INS_B_T1:
        rewritten = zz_thumb_relocator_rewrite_B_T1(self, insn_ctx);
        break;
    case THUMB_INS_B_T2:
        rewritten = zz_thumb_relocator_rewrite_B_T2(self, insn_ctx);
        break;
    case THUMB_INS_B_T3:
        rewritten = zz_thumb_relocator_rewrite_B_T3(self, insn_ctx);
        break;
    case THUMB_INS_B_T4:
        rewritten = zz_thumb_relocator_rewrite_B_T4(self, insn_ctx);
        break;
    case THUMB_INS_BLBLX_T1:
        rewritten = zz_thumb_relocator_rewrite_BLBLX_T1(self, insn_ctx);
        break;
    case THUMB_INS_BLBLX_T2:
        rewritten = zz_thumb_relocator_rewrite_BLBLX_T2(self, insn_ctx);
        break;
    case THUMB_UNDEF:
        rewritten = FALSE;
        break;
    }
    if (!rewritten)
        zz_thumb_writer_put_bytes(self->output, (zbyte *)&insn_ctx->insn, insn_ctx->size);
    return TRUE;
}

// zbool zz_thumb_relocator_rewrite_ldr(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
//     cs_arm_op *dst = &insn_ctx->detail->operands[0];
//     cs_arm_op *src = &insn_ctx->detail->operands[1];
//     zaddr absolute_pc;

//     if (src->type != ARM_OP_MEM || src->mem.base != ARM_REG_PC)
//         return FALSE;

//     absolute_pc = insn_ctx->pc & ~((zaddr)(4 - 1));
//     absolute_pc += src->mem.disp;

//     zz_thumb_writer_put_ldr_b_reg_address(self->output, dst->reg, absolute_pc);
//     zz_thumb_writer_put_ldr_reg_reg(self->output, dst->reg, dst->reg);
//     return TRUE;
// }

// zbool zz_thumb_relocator_rewrite_add(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
//     const cs_arm_op *dst = &insn_ctx->detail->operands[0];
//     const cs_arm_op *src = &insn_ctx->detail->operands[1];
//     arm_reg temp_reg;

//     if (insn_ctx->detail->op_count != 2)
//         return FALSE;
//     else if (src->type != ARM_OP_REG || src->reg != ARM_REG_PC)
//         return FALSE;

//     if (dst->reg != ARM_REG_R0)
//         temp_reg = ARM_REG_R0;
//     else
//         temp_reg = ARM_REG_R1;

// #if defined(DEBUG_MODE)
//     debug_break();
// #endif
//     return TRUE;
// }

// zbool zz_thumb_relocator_rewrite_b(ZzThumbRelocator *self, cs_mode target_mode, ZzInstruction *insn_ctx) {
//     const cs_arm_op *target = &insn_ctx->detail->operands[0];

//     if (target->type != ARM_OP_IMM)
//         return FALSE;
// #if defined(DEBUG_MODE)
//     debug_break();
// #endif
//     return TRUE;
// }

// zbool zz_thumb_relocator_rewrite_b_cond(ZzThumbRelocator *self, ZzInstruction *insn_ctx) {
//     const cs_arm_op *target = &insn_ctx->detail->operands[0];

//     if (target->type != ARM_OP_IMM)
//         return FALSE;

// #if defined(DEBUG_MODE)
//     debug_break();
// #endif
//     return TRUE;
// }

// zbool zz_thumb_relocator_rewrite_bl(ZzThumbRelocator *self, cs_mode target_mode, ZzInstruction *insn_ctx) {
//     const cs_arm_op *target = &insn_ctx->detail->operands[0];

//     if (target->type != ARM_OP_IMM)
//         return FALSE;
// #if defined(DEBUG_MODE)
//     debug_break();
// #endif
//     return TRUE;
// }
