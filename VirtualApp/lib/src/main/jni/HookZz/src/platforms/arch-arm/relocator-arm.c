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
    relocator->output = output;
    relocator->relocate_literal_insns_size = 0;
    relocator->try_relocated_length = 0;

    relocator->input_insns = (ZzInstruction *)malloc(MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzInstruction));
    memset(relocator->input_insns, 0, MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzInstruction));
    relocator->output_insns =
        (ZzRelocateInstruction *)malloc(MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzRelocateInstruction));
    memset(relocator->output_insns, 0, MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzRelocateInstruction));
    relocator->relocate_literal_insns =
        (ZzLiteralInstruction **)malloc(MAX_LITERAL_INSN_SIZE * sizeof(ZzLiteralInstruction *));
    memset(relocator->relocate_literal_insns, 0, MAX_LITERAL_INSN_SIZE * sizeof(ZzLiteralInstruction *));
}

void zz_arm_relocator_reset(ZzArmRelocator *self, zpointer input_code, ZzArmWriter *output) {
    self->input_cur = input_code;
    self->input_start = input_code;
    self->input_pc = (zaddr)input_code;
    self->inpos = 0;
    self->outpos = 0;
    self->output = output;
    self->relocate_literal_insns_size = 0;
    self->try_relocated_length = 0;

    memset(self->input_insns, 0, MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzInstruction));
    memset(self->output_insns, 0, MAX_RELOCATOR_INSTRUCIONS_SIZE * sizeof(ZzRelocateInstruction));
    memset(self->relocate_literal_insns, 0, MAX_LITERAL_INSN_SIZE * sizeof(ZzLiteralInstruction *));
}

zsize zz_arm_relocator_read_one(ZzArmRelocator *self, ZzInstruction *instruction) {
    ZzInstruction *insn_ctx = &self->input_insns[self->inpos];
    ZzRelocateInstruction *re_insn_ctx = &self->output_insns[self->inpos];

    re_insn_ctx->insn_ctx = insn_ctx;
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
    int tmp_size = 0;
    zpointer target_addr;
    ZzInstruction insn_ctx;
    zbool early_end = FALSE;
    target_addr = (zpointer)address;

    do {
        zz_arm_reader_read_one_instruction(&insn_ctx, target_addr);
        switch (GetARMInsnType(insn_ctx.insn)) {
        case ARM_INS_B_A1: {
            zuint32 cond = get_insn_sub(insn_ctx.insn, 28, 4);
            if (cond == 0xE)
                early_end = TRUE;
        }; break;
        default:;
        }
        tmp_size += insn_ctx.size;
        target_addr = target_addr + insn_ctx.size;
    } while (tmp_size < min_bytes);

    if (early_end) {
        *max_bytes = tmp_size;
    }
    return;
}

zaddr zz_arm_relocator_get_insn_relocated_offset(ZzArmRelocator *self, zaddr address) {
    const ZzInstruction *insn_ctx;
    const ZzRelocateInstruction *re_insn_ctx;
    int i;
    for (i = 0; i < self->inpos; i++) {
        re_insn_ctx = &self->output_insns[i];
        insn_ctx = re_insn_ctx->insn_ctx;
        if (insn_ctx->address == address && re_insn_ctx->relocated_offset) {
            return re_insn_ctx->relocated_offset;
        }
    }
    return 0;
}

void zz_arm_relocator_relocate_writer(ZzArmRelocator *relocator, zaddr code_address) {
    ZzArmWriter *arm_writer;
    arm_writer = relocator->output;
    if (relocator->relocate_literal_insns_size) {
        int i;
        zaddr literal_address, relocated_offset, relocated_address, *literal_address_ptr;
        for (i = 0; i < relocator->relocate_literal_insns_size; i++) {
            literal_address_ptr = (zaddr *)relocator->relocate_literal_insns[i]->literal_address_ptr;
            literal_address = *literal_address_ptr;
            relocated_offset = zz_arm_relocator_get_insn_relocated_offset(relocator, literal_address);
            if (relocated_offset) {
                relocated_address = code_address + relocated_offset;
                *literal_address_ptr = relocated_address;
            }
        }
    }
}

void zz_arm_relocator_write_all(ZzArmRelocator *self) {
    zuint count = 0;
    zuint outpos = self->outpos;
    ZzArmWriter arm_writer = *self->output;

    while (zz_arm_relocator_write_one(self))
        count++;
}

// PAGE: A8-312
static zbool zz_arm_relocator_rewrite_ADD_register_A1(ZzArmRelocator *self, const ZzInstruction *insn_ctx,
                                                      ZzRelocateInstruction *re_insn_ctx) {
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
static zbool zz_arm_relocator_rewrite_LDR_literal_A1(ZzArmRelocator *self, const ZzInstruction *insn_ctx,
                                                     ZzRelocateInstruction *re_insn_ctx) {
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
static zbool zz_arm_relocator_rewrite_ADR_A1(ZzArmRelocator *self, const ZzInstruction *insn_ctx,
                                             ZzRelocateInstruction *re_insn_ctx) {
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
static zbool zz_arm_relocator_rewrite_ADR_A2(ZzArmRelocator *self, const ZzInstruction *insn_ctx,
                                             ZzRelocateInstruction *re_insn_ctx) {
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
static zbool zz_arm_relocator_rewrite_B_A1(ZzArmRelocator *self, const ZzInstruction *insn_ctx,
                                           ZzRelocateInstruction *re_insn_ctx) {
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
static zbool zz_arm_relocator_rewrite_BLBLX_immediate_A1(ZzArmRelocator *self, const ZzInstruction *insn_ctx,
                                                         ZzRelocateInstruction *re_insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 imm24 = get_insn_sub(insn, 0, 24);
    zuint32 imm32 = imm24 << 2;
    zaddr target_address;
    target_address = ALIGN_4(insn_ctx->pc) + imm32;

    // CurrentInstrSet = thumb
    // targetInstrSet = arm

    // convert 'bl' to 'b', but save 'cond'
    zz_arm_writer_put_instruction(self->output, (insn & 0xF0000000) | 0b1010 << 24 | 0);

    ZzArmWriter ouput_bak = *self->output;

    zz_arm_writer_put_b_imm(self->output, 0);
    ZzLiteralInstruction **literal_insn_ptr = &(self->relocate_literal_insns[self->relocate_literal_insns_size++]);
    zz_arm_writer_put_ldr_b_reg_relocate_address(self->output, ZZ_ARM_REG_LR, insn_ctx->pc - 4, literal_insn_ptr);
    zz_arm_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);

    // overwrite `zz_arm_writer_put_b_imm`
    zz_arm_writer_put_b_imm(&ouput_bak, self->output->pc - ouput_bak.pc - 8);
    return TRUE;
}

// PAGE: A8-348
static zbool zz_arm_relocator_rewrite_BLBLX_immediate_A2(ZzArmRelocator *self, const ZzInstruction *insn_ctx,
                                                         ZzRelocateInstruction *re_insn_ctx) {
    zuint32 insn = insn_ctx->insn;
    zuint32 H = get_insn_sub(insn, 24, 1);
    zuint32 imm24 = get_insn_sub(insn, 0, 24);
    zuint32 imm32 = (imm24 << 2) | (H << 1);
    zaddr target_address;
    target_address = insn_ctx->pc + imm32;

    ZzLiteralInstruction **literal_insn_ptr = &(self->relocate_literal_insns[self->relocate_literal_insns_size++]);
    zz_arm_writer_put_ldr_b_reg_relocate_address(self->output, ZZ_ARM_REG_LR, insn_ctx->pc - 4, literal_insn_ptr);
    zz_arm_writer_put_ldr_reg_address(self->output, ZZ_ARM_REG_PC, target_address);

    return TRUE;
}

zbool zz_arm_relocator_write_one(ZzArmRelocator *self) {
    const ZzInstruction *insn_ctx;
    ZzRelocateInstruction *re_insn_ctx;
    zbool rewritten = FALSE;

    if (self->inpos != self->outpos) {
        insn_ctx = &self->input_insns[self->outpos];
        re_insn_ctx = &self->output_insns[self->outpos];

        self->outpos++;
    } else
        return FALSE;

    re_insn_ctx->relocated_offset = (zaddr)self->output->pc - (zaddr)self->output->base;

    switch (GetARMInsnType(insn_ctx->insn)) {
    case ARM_INS_ADD_register_A1:
        rewritten = zz_arm_relocator_rewrite_ADD_register_A1(self, insn_ctx, re_insn_ctx);
        break;
    case ARM_INS_LDR_literal_A1:
        rewritten = zz_arm_relocator_rewrite_LDR_literal_A1(self, insn_ctx, re_insn_ctx);
        break;
    case ARM_INS_ADR_A1:
        rewritten = zz_arm_relocator_rewrite_ADR_A1(self, insn_ctx, re_insn_ctx);
        break;
    case ARM_INS_ADR_A2:
        rewritten = zz_arm_relocator_rewrite_ADR_A2(self, insn_ctx, re_insn_ctx);
        break;
    case ARM_INS_B_A1:
        rewritten = zz_arm_relocator_rewrite_B_A1(self, insn_ctx, re_insn_ctx);
        break;
    case ARM_INS_BLBLX_immediate_A1:
        rewritten = zz_arm_relocator_rewrite_BLBLX_immediate_A1(self, insn_ctx, re_insn_ctx);
        break;
    case ARM_INS_BLBLX_immediate_A2:
        rewritten = zz_arm_relocator_rewrite_BLBLX_immediate_A2(self, insn_ctx, re_insn_ctx);
        break;
    case ARM_UNDEF:
        rewritten = FALSE;
        break;
    }
    if (!rewritten)
        zz_arm_writer_put_bytes(self->output, (zbyte *)&insn_ctx->insn, insn_ctx->size);

    re_insn_ctx->relocated_length =
        (zaddr)self->output->pc - (zaddr)self->output->base - (zaddr)re_insn_ctx->relocated_offset;
    return TRUE;
}
