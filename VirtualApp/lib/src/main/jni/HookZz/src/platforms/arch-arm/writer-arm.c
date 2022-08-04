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

#include "writer-arm.h"

#include <stdlib.h>

// ATTENTION !!!:
// 写 writer 部分, 需要参考, `Instrcution Set Encoding` 部分
// `writer` REF: `ZzInstruction Set Encoding`

ZzArmWriter *zz_arm_writer_new(zpointer data_ptr) {
    ZzArmWriter *writer = (ZzArmWriter *)malloc(sizeof(ZzArmWriter));
    memset(writer, 0, sizeof(ZzArmWriter));

    zaddr align_address = (zaddr)data_ptr & ~(zaddr)3;
    writer->codedata = (zpointer)align_address;
    writer->base = (zpointer)align_address;
    writer->pc = align_address;
    writer->size = 0;

    writer->literal_insn_size = 0;
    memset(writer->literal_insns, 0, sizeof(ZzLiteralInstruction) * MAX_LITERAL_INSN_SIZE);

    return writer;
}

void zz_arm_writer_init(ZzArmWriter *self, zpointer data_ptr) { zz_arm_writer_reset(self, data_ptr); }

void zz_arm_writer_reset(ZzArmWriter *self, zpointer data_ptr) {

    zaddr align_address = (zaddr)data_ptr & ~(zaddr)3;
    self->codedata = (zpointer)align_address;
    self->base = (zpointer)align_address;
    self->pc = align_address;

    self->literal_insn_size = 0;
    memset(self->literal_insns, 0, sizeof(ZzLiteralInstruction) * MAX_LITERAL_INSN_SIZE);

    self->size = 0;
}

zsize zz_arm_writer_near_jump_range_size() { return ((1 << 23) << 2); }

// ------- relocator -------

ZzLiteralInstruction *zz_arm_writer_put_ldr_b_reg_relocate_address(ZzArmWriter *self, ZzARMReg reg, zaddr address,
                                                                   ZzLiteralInstruction **literal_insn_ptr) {
    zz_arm_writer_put_ldr_b_reg_address(self, reg, address);
    ZzLiteralInstruction *literal_insn = &(self->literal_insns[self->literal_insn_size - 1]);
    *literal_insn_ptr = literal_insn;
    return literal_insn;
}

ZzLiteralInstruction *zz_arm_writer_put_ldr_reg_relocate_address(ZzArmWriter *self, ZzARMReg reg, zaddr address,
                                                                 ZzLiteralInstruction **literal_insn_ptr) {
    zz_arm_writer_put_ldr_reg_address(self, reg, address);
    ZzLiteralInstruction *literal_insn = &(self->literal_insns[self->literal_insn_size - 1]);
    *literal_insn_ptr = literal_insn;
    return literal_insn;
}

// ------- user custom -------

void zz_arm_writer_put_ldr_b_reg_address(ZzArmWriter *self, ZzARMReg reg, zaddr address) {
    self->literal_insns[self->literal_insn_size].literal_insn_ptr = self->codedata;
    zz_arm_writer_put_ldr_reg_reg_imm(self, reg, ZZ_ARM_REG_PC, 0);
    zz_arm_writer_put_b_imm(self, 0x0);
    self->literal_insns[self->literal_insn_size++].literal_address_ptr = self->codedata;
    zz_arm_writer_put_bytes(self, (zpointer)&address, sizeof(zpointer));
}

void zz_arm_writer_put_bx_to_thumb(ZzArmWriter *self) {
    zz_arm_writer_put_sub_reg_reg_imm(self, ZZ_ARM_REG_SP, ZZ_ARM_REG_SP, 0x8);
    zz_arm_writer_put_str_reg_reg_imm(self, ZZ_ARM_REG_R1, ZZ_ARM_REG_SP, 0x0);
    zz_arm_writer_put_add_reg_reg_imm(self, ZZ_ARM_REG_R1, ZZ_ARM_REG_PC, 9);
    zz_arm_writer_put_str_reg_reg_imm(self, ZZ_ARM_REG_R1, ZZ_ARM_REG_SP, 0x4);
    zz_arm_writer_put_ldr_reg_reg_imm_index(self, ZZ_ARM_REG_R1, ZZ_ARM_REG_SP, 4, 0);
    zz_arm_writer_put_ldr_reg_reg_imm_index(self, ZZ_ARM_REG_PC, ZZ_ARM_REG_SP, 4, 0);
}
// ------- architecture default -------
void zz_arm_writer_put_bytes(ZzArmWriter *self, zbyte *data, zuint data_size) {
    memcpy(self->codedata, data, data_size);
    self->codedata = (zpointer)self->codedata + data_size;
    self->pc += data_size;
    self->size += data_size;
}

void zz_arm_writer_put_instruction(ZzArmWriter *self, zuint32 insn) {
    *(zuint32 *)(self->codedata) = insn;
    self->codedata = (zpointer)self->codedata + sizeof(zuint32);
    self->pc += 4;
    self->size += 4;
}

void zz_arm_writer_put_b_imm(ZzArmWriter *self, zuint32 imm) {
    zz_arm_writer_put_instruction(self, 0xea000000 | ((imm / 4) & 0xffffff));
}

void zz_arm_writer_put_ldr_reg_reg_imm(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zint32 imm) {
    ZzArmRegInfo rd, rs;

    zz_arm_register_describe(dst_reg, &rd);
    zz_arm_register_describe(src_reg, &rs);

    if (rs.meta == ZZ_ARM_REG_PC) {
        zz_arm_writer_put_ldr_reg_imm_literal(self, dst_reg, imm);
    } else {
        zbool P = 1;
        zbool U = 0;
        zbool W = 0;
        if (imm >= 0)
            U = 1;

        zz_arm_writer_put_ldr_reg_reg_imm_A1(self, dst_reg, src_reg, ABS(imm), P, U, W);
    }
}

void zz_arm_writer_put_ldr_reg_reg_imm_index(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zint32 imm,
                                             zbool index) {
    ZzArmRegInfo rd, rs;

    zz_arm_register_describe(dst_reg, &rd);
    zz_arm_register_describe(src_reg, &rs);

    zbool P = index;
    zbool U = 0;
    zbool W = 1;
    if (P == 0)
        W = 0;
    if (imm >= 0)
        U = 1;

    zz_arm_writer_put_ldr_reg_reg_imm_A1(self, dst_reg, src_reg, ABS(imm), P, U, W);
}
void zz_arm_writer_put_ldr_reg_reg_imm_A1(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zuint32 imm, zbool P,
                                          zbool U, zbool W) {
    ZzArmRegInfo rd, rs;

    zz_arm_register_describe(dst_reg, &rd);
    zz_arm_register_describe(src_reg, &rs);

    zz_arm_writer_put_instruction(self, 0xe4100000 | rd.index << 12 | rs.index << 16 | P << 24 | U << 23 | W << 21 |
                                            (imm & ZZ_INT12_MASK));
}
void zz_arm_writer_put_ldr_reg_imm_literal(ZzArmWriter *self, ZzARMReg dst_reg, zint32 imm) {
    ZzArmRegInfo rd;

    zz_arm_register_describe(dst_reg, &rd);
    zbool U = 0;
    if (imm >= 0)
        U = 1;
    zz_arm_writer_put_instruction(self, 0xe51f0000 | U << 23 | rd.index << 12 | (ABS(imm) & ZZ_INT12_MASK));
}

void zz_arm_writer_put_str_reg_reg_imm(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zint32 imm) {
    ZzArmRegInfo rd, rs;

    zz_arm_register_describe(dst_reg, &rd);
    zz_arm_register_describe(src_reg, &rs);

    zbool P = 1;
    zbool U = 0;
    zbool W = 0;
    if (imm >= 0)
        U = 1;
    zz_arm_writer_put_instruction(self, 0xe4000000 | rd.index << 12 | rs.index << 16 | P << 24 | U << 23 | W << 21 |
                                            (imm & ZZ_INT12_MASK));
}

void zz_arm_writer_put_ldr_reg_address(ZzArmWriter *self, ZzARMReg reg, zaddr address) {
    self->literal_insns[self->literal_insn_size].literal_insn_ptr = self->codedata;
    zz_arm_writer_put_ldr_reg_reg_imm(self, reg, ZZ_ARM_REG_PC, -4);
    self->literal_insns[self->literal_insn_size++].literal_address_ptr = self->codedata;
    zz_arm_writer_put_bytes(self, (zpointer)&address, sizeof(zpointer));
}

void zz_arm_writer_put_add_reg_reg_imm(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zuint32 imm) {
    ZzArmRegInfo rd, rs;

    zz_arm_register_describe(dst_reg, &rd);
    zz_arm_register_describe(src_reg, &rs);

    zz_arm_writer_put_instruction(self, 0xe2800000 | rd.index << 12 | rs.index << 16 | (imm & ZZ_INT12_MASK));
}

void zz_arm_writer_put_sub_reg_reg_imm(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zuint32 imm) {
    ZzArmRegInfo rd, rs;

    zz_arm_register_describe(dst_reg, &rd);
    zz_arm_register_describe(src_reg, &rs);

    zz_arm_writer_put_instruction(self, 0xe2400000 | rd.index << 12 | rs.index << 16 | (imm & ZZ_INT12_MASK));
}

void zz_arm_writer_put_bx_reg(ZzArmWriter *self, ZzARMReg reg) {
    ZzArmRegInfo rs;
    zz_arm_register_describe(reg, &rs);
    zz_arm_writer_put_instruction(self, 0xe12fff10 | rs.index);
}

void zz_arm_writer_put_nop(ZzArmWriter *self) { zz_arm_writer_put_instruction(self, 0xe320f000); }

void zz_arm_writer_put_push_reg(ZzArmWriter *self, ZzARMReg reg) {
    ZzArmRegInfo ri;
    zz_arm_register_describe(reg, &ri);
    zz_arm_writer_put_instruction(self, 0b11100101001011010000000000000100 | ri.index << 12);
    return;
}

void zz_arm_writer_put_pop_reg(ZzArmWriter *self, ZzARMReg reg) {
    ZzArmRegInfo ri;
    zz_arm_register_describe(reg, &ri);

    zz_arm_writer_put_instruction(self, 0b11100100100111010000000000000100 | ri.index << 12);
    return;
}