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

// zsize zz_arm64_relocator_read_one(ZzArm64Relocator *self, ZzInstruction *instruction) {
//     insn_cs **insn_ctx_ptr, *insn_cs;
//     ZzInstruction insn_ctx = self->input_insns[self->inpos];
//     insn_ctx_ptr = &insn_ctx.insn_cs;

//     if (cs_disasm(self->capstone, self->input_cur, 4, self->input_pc, 1, insn_ctx_ptr) != 1) {
//         return 0;
//     }

//     insn_cs = *insn_ctx_ptr;

//     // zbool flag = TRUE;
//     // switch (insn_cs->id) {
//     // case ARM64_INS_B:
//     //     if (branch_is_unconditional(ins))
//     //         flag = relocator_rewrite_b(ins, relocate_writer);
//     //     else
//     //         flag = relocator_rewrite_b_cond(ins, relocate_writer);
//     //     break;
//     // case ARM64_INS_LDR:
//     //     flag = relocator_rewrite_ldr(ins, relocate_writer);
//     //     break;
//     // case ARM64_INS_ADR:
//     // case ARM64_INS_ADRP:
//     //     flag = relocator_rewrite_adr(ins, relocate_writer);
//     //     break;
//     // case ARM64_INS_BL:
//     //     flag = relocator_rewrite_bl(ins, relocate_writer);
//     //     break;
//     // default:
//     //     zz_arm64_writer_put_bytes(relocate_writer, address, insn_cs->size);
//     // }
//     // if (!flag)
//     //     zz_arm64_writer_put_bytes(relocate_writer, address, insn_cs->size);

//     if (instruction != NULL)
//         *instruction = insn_ctx;

//     self->input_cur += insn_cs->size;
//     self->input_pc += insn_cs->size;

//     return self->input_cur - self->input_start;
// }

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

// static zbool zz_arm64_branch_is_unconditional(const cs_insn *insn) {
//     switch (insn->detail->arm64.cc) {
//     case ARM64_CC_INVALID:
//     case ARM64_CC_AL:
//     case ARM64_CC_NV:
//         return TRUE;
//     default:
//         return FALSE;
//     }
// }

// static zbool zz_arm64_relocator_rewrite_ldr(ZzArm64Relocator *self, ZzInstruction *insn_ctx) {
//     const cs_arm64_op *dst = &insn_ctx->detail->operands[0];
//     const cs_arm64_op *src = &insn_ctx->detail->operands[1];
//     zbool dst_reg_is_fp_or_simd;
//     ZzARM64Reg tmp_reg;

//     (void)self;

//     if (src->type != ARM64_OP_IMM)
//         return FALSE;

//     dst_reg_is_fp_or_simd = (dst->reg >= ARM64_REG_S0 && dst->reg <= ARM64_REG_S31) ||
//                             (dst->reg >= ARM64_REG_D0 && dst->reg <= ARM64_REG_D31) ||
//                             (dst->reg >= ARM64_REG_Q0 && dst->reg <= ARM64_REG_Q31);
//     if (dst_reg_is_fp_or_simd) {
// #if defined(DEBUG_MODE)
//         debug_break();
// #endif
//     } else {
//         if (dst->reg >= ARM64_REG_W0 && dst->reg <= ARM64_REG_W28)
//             tmp_reg = ZZ_ARM64_REG_X0 + (dst->reg - ARM64_REG_W0);
//         else if (dst->reg >= ARM64_REG_W29 && dst->reg <= ARM64_REG_W30)
//             tmp_reg = ZZ_ARM64_REG_X29 + (dst->reg - ARM64_REG_W29);
//         else
//             tmp_reg = dst->reg;

//         zz_arm64_writer_put_ldr_b_reg_address(self->output, tmp_reg, src->imm);
//         zz_arm64_writer_put_ldr_reg_reg_offset(self->output, dst->reg, tmp_reg, 0);
//     }

//     return TRUE;
// }

// static zbool zz_arm64_relocator_rewrite_adr(ZzArm64Relocator *self, ZzInstruction *insn_ctx) {
//     const cs_arm64_op *dst = &insn_ctx->detail->operands[0];
//     const cs_arm64_op *label = &insn_ctx->detail->operands[1];

//     zz_arm64_writer_put_ldr_b_reg_address(self->output, dst->reg, label->imm);
//     return TRUE;
// }

// static zbool zz_arm64_relocator_rewrite_b(ZzArm64Relocator *self, ZzInstruction *insn_ctx) {
//     const cs_arm64_op *target = &insn_ctx->detail->operands[0];

//     zz_arm64_writer_put_ldr_b_reg_address(self->output, ZZ_ARM64_REG_X17, target->imm);

//     return TRUE;
// }

// static zbool zz_arm64_relocator_rewrite_b_cond(ZzArm64Relocator *self, ZzInstruction *insn_ctx) {
//     const cs_arm64_op *target = &insn_ctx->detail->operands[0];

//     zz_arm64_writer_put_b_cond_imm(self->output, insn_ctx->detail->cc, 0x8);
//     zz_arm64_writer_put_b_imm(self->output, 0x4 + 0x14);

//     zz_arm64_writer_put_ldr_br_reg_address(self->output, ZZ_ARM64_REG_X17, target->imm);

//     return TRUE;
// }

// static zbool zz_arm64_relocator_rewrite_bl(ZzArm64Relocator *self, ZzInstruction *insn_ctx) {
//     const cs_arm64_op *target = &insn_ctx->detail->operands[0];

//     zz_arm64_writer_put_ldr_br_reg_address(self->output, ZZ_ARM64_REG_LR, target->imm);

//     return TRUE;
// }

// zbool relocator_rewrite_ldr(ZzInstruction *ins, ZzArm64Writer *relocate_writer)
// {
//     cs_arm64 ins_csd = ins->insn_cs->detail->arm64;
//     const cs_arm64_op *dst = &ins_csd.operands[0];
//     const cs_arm64_op *src = &ins_csd.operands[1];
//     if (src->type != ARM64_OP_IMM)
//         return FALSE;
//     return TRUE;
// }

// zbool relocator_rewrite_b(ZzInstruction *ins, ZzArm64Writer *relocate_writer) {
//     cs_arm64 ins_csd = ins->insn_cs->detail->arm64;
//     zaddr target_addr = ins_csd.operands[0].imm;

//     // zz_arm64_writer_put_ldr_br_b_reg_address(relocate_writer,
//     ZZ_ARM64_REG_X17,
//     // target_addr);
//     zz_arm64_writer_put_ldr_reg_address(relocate_writer, ZZ_ARM64_REG_X17,
//                                         target_addr);
//     zz_arm64_writer_put_br_reg(relocate_writer, ZZ_ARM64_REG_X17);
//     return TRUE;
// }

// zbool relocator_rewrite_bl(ZzInstruction *ins, ZzArm64Writer *relocate_writer)
// {
//     cs_arm64 ins_csd = ins->insn_cs->detail->arm64;
//     zaddr target_addr = ins_csd.operands[0].imm;

//     zz_arm64_writer_put_ldr_reg_address(relocate_writer, ZZ_ARM64_REG_X17,
//                                         target_addr);
//     zz_arm64_writer_put_blr_reg(relocate_writer, ZZ_ARM64_REG_X17);
//     return TRUE;
// }

// /*
//     origin:
//         1. j.eq [3]

//         2. [...]
//         3. [...]

//     rwrite:
//         1. j.eq [1.2]
//         1.1 b [2]
//         1.2 abs_jmp [3]

//         2. [...]
//         3. [...]
//  */
// zbool relocator_rewrite_b_cond(ZzInstruction *ins,
//                                ZzArm64Writer *relocate_writer) {
//     cs_arm64 ins_csd = ins->insn_cs->detail->arm64;
//     zaddr target_addr = ins_csd.operands[0].imm;

//     zz_arm64_writer_put_b_cond_imm(relocate_writer, ins_csd.cc, 0x8);
//     zz_arm64_writer_put_b_imm(relocate_writer, 0x4 + 0x14);

//     // zz_arm64_writer_put_ldr_br_b_reg_address(relocate_writer,
//     ZZ_ARM64_REG_X17,
//     // target_addr);
//     zz_arm64_writer_put_ldr_reg_address(relocate_writer, ZZ_ARM64_REG_X17,
//                                         target_addr);
//     zz_arm64_writer_put_br_reg(relocate_writer, ZZ_ARM64_REG_X17);
//     return TRUE;
// }

// zbool relocator_rewrite_adr(ZzInstruction *ins, ZzArm64Writer *relocate_writer)
// {
//     cs_arm64 ins_csd = ins->insn_cs->detail->arm64;

//     const cs_arm64_op dst = ins_csd.operands[0];
//     const cs_arm64_op label = ins_csd.operands[1];
//     zz_arm64_writer_put_ldr_reg_address(relocate_writer, dst.reg, label.imm);
//     return TRUE;
// }

// PAGE: C6-673
static zbool zz_arm64_relocator_rewrite_LDR_literal(ZzArm64Relocator *self, ZzInstruction *insn_ctx) {
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
static zbool zz_arm64_relocator_rewrite_ADR(ZzArm64Relocator *self, ZzInstruction *insn_ctx) {
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
static zbool zz_arm64_relocator_rewrite_ADRP(ZzArm64Relocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 immhi = get_insn_sub(insn, 5, 19);
    zuint32 immlo = get_insn_sub(insn, 29, 2);
    // 12 is PAGE-SIZE
    zuint64 imm = immhi << 2 << 12 | immlo << 12;

    zaddr target_address;
    target_address = (insn_ctx->pc & (1 << 12)) + imm;
    int Rt_ndx = get_insn_sub(insn, 0, 4);

    zz_arm64_writer_put_ldr_b_reg_address(self->output, Rt_ndx, target_address);
    return TRUE;
}

// PAGE: C6-550
static zbool zz_arm64_relocator_rewrite_B(ZzArm64Relocator *self, ZzInstruction *insn_ctx) {
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
static zbool zz_arm64_relocator_rewrite_BL(ZzArm64Relocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm26 = get_insn_sub(insn, 0, 26);

    zuint64 offset = imm26 << 2;

    zaddr target_address;
    target_address = insn_ctx->pc + offset;
    int Rt_ndx = get_insn_sub(insn, 0, 4);

    zz_arm64_writer_put_ldr_blr_b_reg_address(self->output, ZZ_ARM64_REG_X17, target_address);
    return TRUE;
}

/*
    origin:
        1. j.eq [3]

        2. [...]
        3. [...]

    rwrite:
        1. j.eq [1.2]
        1.1 b [2]
        1.2 abs_jmp [3]

        2. [...]
        3. [...]
 */

// PAGE: C6-549
static zbool zz_arm64_relocator_rewrite_B_cond(ZzArm64Relocator *self, ZzInstruction *insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm19 = get_insn_sub(insn, 5, 19);

    zuint64 offset = imm19 << 2;

    zaddr target_address;
    target_address = insn_ctx->pc + offset;

    zuint32 cond = get_insn_sub(insn, 0, 4);

    zz_arm64_writer_put_b_cond_imm(self->output, cond, 0x8);
    zz_arm64_writer_put_b_imm(self->output, 0xc);
    zz_arm64_writer_put_ldr_br_reg_address(self->output, ZZ_ARM64_REG_X17, target_address);

    return TRUE;
}

zbool zz_arm64_relocator_write_one(ZzArm64Relocator *self) {
    ZzInstruction *insn_ctx;
    zbool rewritten = FALSE;

    if (self->inpos != self->outpos) {
        insn_ctx = &self->input_insns[self->outpos];
        self->outpos++;
    } else
        return FALSE;

    insn_ctx->pc = insn_ctx->pc;

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