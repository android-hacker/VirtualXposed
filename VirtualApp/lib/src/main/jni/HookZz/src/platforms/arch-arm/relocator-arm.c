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

// static zbool zz_arm_branch_is_unconditional(const cs_insn *insn_ctx) {
//     switch (insn_ctx->detail->arm.cc) {
//     case ARM_CC_INVALID:
//     case ARM_CC_AL:
//         return TRUE;
//     default:
//         return FALSE;
//     }
// }

// PAGE: A8-410
static zbool zz_arm_relocator_rewrite_LDR_A1(ZzArmRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm12 = get_insn_sub(insn, 0, 12);
    zuint32 imm32 = imm12 << 2;
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
static zbool zz_arm_relocator_rewrite_ADR_A1(ZzArmRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm12 = get_insn_sub(insn, 0, 12);
    zuint32 imm32 = imm12 << 2;
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;
    int Rt_ndx = get_insn_sub(insn, 12, 4);
    zz_arm_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);
    return TRUE;
}

// PAGE: A8-322
static zbool zz_arm_relocator_rewrite_ADR_A2(ZzArmRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm12 = get_insn_sub(insn, 0, 12);
    zuint32 imm32 = imm12 << 2;
    zaddr target_address;
    target_address = insn_ctx->pc - imm32;
    int Rt_ndx = get_insn_sub(insn, 12, 4);
    zz_arm_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);
    return TRUE;
}

// PAGE: A8-334
static zbool zz_arm_relocator_rewrite_B_A1(ZzArmRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm24 = get_insn_sub(insn, 0, 24);
    zuint32 imm32 = imm24 << 2;
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;
    zz_arm_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);
    return TRUE;
}

// PAGE: A8-348
static zbool zz_arm_relocator_rewrite_BLBLX_A1(ZzArmRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm24 = get_insn_sub(insn, 0, 24);
    zuint32 imm32 = imm24 << 2;
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;
    zz_arm_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_LR, insn_ctx->pc + 2 * 4);
    zz_arm_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);
    return TRUE;
}

// PAGE: A8-348
static zbool zz_arm_relocator_rewrite_BLBLX_A2(ZzArmRelocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 H = get_insn_sub(insn, 24, 1);
    zuint32 imm24 = get_insn_sub(insn, 0, 24);
    zuint32 imm32 = imm24 << 2 | H << 1;
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;
    zz_arm_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_LR, insn_ctx->pc + 2 * 4);
    zz_arm_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);
    return TRUE;
}

zbool zz_arm_relocator_write_one(ZzArmRelocator *self) {
    ZzInstruction *insn_ctx;
    zbool rewritten = FALSE;

    if (self->inpos != self->outpos) {
        insn_ctx = &self->input_insns[self->outpos];
        self->outpos++;
    } else
        return FALSE;

    insn_ctx->pc = self->input_pc + 8;

    switch (GetARMInsnType(insn_ctx->insn)) {
    case ARM_INS_LDR_A1:
        rewritten = zz_arm_relocator_rewrite_LDR_A1(self, insn_ctx);
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
    case ARM_INS_BLBLX_A1:
        rewritten = zz_arm_relocator_rewrite_BLBLX_A1(self, insn_ctx);
        break;
    case ARM_INS_BLBLX_A2:
        rewritten = zz_arm_relocator_rewrite_BLBLX_A2(self, insn_ctx);
        break;
    case ARM_UNDEF:
        rewritten = FALSE;
        break;
    }
    if (!rewritten)
        zz_arm_writer_put_bytes(self->output, (zbyte *)&insn_ctx->insn, insn_ctx->size);
    return TRUE;
}

// static zbool zz_arm_relocator_rewrite_ldr(ZzArmRelocator *self, ZzInstruction *insn_ctx) {

//     const cs_arm_op *dst = &insn_ctx->detail->operands[0];
//     const cs_arm_op *src = &insn_ctx->detail->operands[1];
//     zint disp;

//     if (src->type != ARM_OP_MEM || src->mem.base != ARM_REG_PC)
//         return TRUE;

//     disp = src->mem.disp;

//     zz_arm_writer_put_ldr_b_reg_address(self->output, dst->reg, insn_ctx->pc);
//     if (disp > 0xff) {
//         zz_arm_writer_put_add_reg_reg_imm(self->output, dst->reg, dst->reg, 0xc00 | ((disp >> 8) & 0xff));
//     }
//     zz_arm_writer_put_add_reg_reg_imm(self->output, dst->reg, dst->reg, disp & 0xff);
//     zz_arm_writer_put_ldr_reg_reg_imm(self->output, dst->reg, dst->reg, 0);

//     return TRUE;
// }

// static zbool zz_arm_relocator_rewrite_add(ZzArmRelocator *self, ZzInstruction *insn_ctx) {
//     const cs_arm_op *dst = &insn_ctx->detail->operands[0];
//     const cs_arm_op *left = &insn_ctx->detail->operands[1];
//     const cs_arm_op *right = &insn_ctx->detail->operands[2];

//     if (left->reg != ARM_REG_PC || right->type != ARM_OP_REG)
//         return FALSE;

//     if (right->reg == dst->reg) {
//         zz_arm_writer_put_add_reg_reg_imm(self->output, dst->reg, dst->reg, insn_ctx->pc & 0xff);
//         zz_arm_writer_put_add_reg_reg_imm(self->output, dst->reg, dst->reg, 0xc00 | ((insn_ctx->pc >> 8) & 0xff));
//         zz_arm_writer_put_add_reg_reg_imm(self->output, dst->reg, dst->reg, 0x800 | ((insn_ctx->pc >> 16) & 0xff));
//         zz_arm_writer_put_add_reg_reg_imm(self->output, dst->reg, dst->reg, 0x400 | ((insn_ctx->pc >> 24) & 0xff));
//     } else {
//         zz_arm_writer_put_ldr_reg_address(self->output, dst->reg, insn_ctx->pc);
//         zz_arm_writer_put_add_reg_reg_imm(self->output, dst->reg, right->reg, 0);
//     }

//     return TRUE;
// }

// static zbool zz_arm_relocator_rewrite_b(ZzArmRelocator *self, cs_mode target_mode, ZzInstruction *insn_ctx) {
//     cs_insn *insn_cs = insn_ctx->insn_cs;
//     const cs_arm_op *target = &insn_ctx->detail->operands[0];

//     if (target->type != ARM_OP_IMM)
//         return FALSE;

//     zz_arm_writer_put_ldr_reg_address(self->output, ARM_REG_PC,
//                                       (target_mode == CS_MODE_THUMB) ? target->imm | 1 : target->imm);
//     return TRUE;
// }

// static zbool zz_arm_relocator_rewrite_bl(ZzArmRelocator *self, cs_mode target_mode, ZzInstruction *insn_ctx) {
//     const cs_arm_op *target = &insn_ctx->detail->operands[0];

//     if (target->type != ARM_OP_IMM)
//         return FALSE;

//     zz_arm_writer_put_ldr_reg_address(self->output, ARM_REG_LR, (zaddr)self->output->pc + (2 * 4));
//     zz_arm_writer_put_ldr_reg_address(self->output, ARM_REG_PC,
//                                       (target_mode == CS_MODE_THUMB) ? target->imm | 1 : target->imm);
//     return TRUE;
// }