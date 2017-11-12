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

#include <string.h>

#include "writer-arm64.h"
#include <stdlib.h>

// REF:
// ARM Architecture Reference Manual ARMV8
// C2.1 Understanding the A64 instruction descriptions
// C2.1.3 The instruction encoding or encodings

// ATTENTION !!!:
// 写 writer 部分, 需要参考, `Instrcution Set Encoding` 部分
// `witer` REF: `ZzInstruction Set Encoding`

ZzArm64Writer *zz_arm64_writer_new(zpointer data_ptr) {
    ZzArm64Writer *writer = (ZzArm64Writer *)malloc(sizeof(ZzArm64Writer));
    memset(writer, 0, sizeof(ZzArm64Writer));

    zaddr align_address = (zaddr)data_ptr & ~(zaddr)3;
    writer->codedata = (zpointer)align_address;
    writer->base = (zpointer)align_address;
    writer->pc = align_address;
    writer->size = 0;

    writer->literal_insn_size = 0;
    memset(writer->literal_insns, 0, sizeof(ZzLiteralInstruction) * MAX_LITERAL_INSN_SIZE);

    return writer;
}

void zz_arm64_writer_init(ZzArm64Writer *self, zpointer target_addr) { zz_arm64_writer_reset(self, target_addr); }

void zz_arm64_writer_reset(ZzArm64Writer *self, zpointer data_ptr) {
    zaddr align_address = (zaddr)data_ptr & ~(zaddr)3;

    self->codedata = (zpointer)align_address;
    self->base = (zpointer)align_address;
    self->pc = align_address;
    self->size = 0;

    self->literal_insn_size = 0;
    memset(self->literal_insns, 0, sizeof(ZzLiteralInstruction) * MAX_LITERAL_INSN_SIZE);
}

// ======= relocator =======

ZzLiteralInstruction *zz_arm64_writer_put_ldr_br_reg_relocate_address(ZzWriter *self, ZzARM64Reg reg, zaddr address,
                                                                      ZzLiteralInstruction **literal_insn_ptr) {

    zz_arm64_writer_put_ldr_br_reg_address(self, reg, address);
    ZzLiteralInstruction *literal_insn = &(self->literal_insns[self->literal_insn_size - 1]);
    *literal_insn_ptr = literal_insn;
    return literal_insn;
}

// ======= user custom =======

void zz_arm64_writer_put_ldr_br_reg_address(ZzWriter *self, ZzARM64Reg reg, zaddr address) {
    self->literal_insns[self->literal_insn_size].literal_insn_ptr = self->codedata;
    zz_arm64_writer_put_ldr_reg_imm(self, reg, (zuint)0x8);
    zz_arm64_writer_put_br_reg(self, reg);
    self->literal_insns[self->literal_insn_size++].literal_address_ptr = self->codedata;
    zz_arm64_writer_put_bytes(self, (zpointer)&address, sizeof(zpointer));
}

void zz_arm64_writer_put_ldr_blr_b_reg_address(ZzWriter *self, ZzARM64Reg reg, zaddr address) {
    self->literal_insns[self->literal_insn_size].literal_insn_ptr = self->codedata;
    zz_arm64_writer_put_ldr_reg_imm(self, reg, (zuint)0xc);
    zz_arm64_writer_put_blr_reg(self, reg);
    zz_arm64_writer_put_b_imm(self, 0xc);
    self->literal_insns[self->literal_insn_size++].literal_address_ptr = self->codedata;
    zz_arm64_writer_put_bytes(self, (zpointer)&address, sizeof(zpointer));
}

void zz_arm64_writer_put_ldr_b_reg_address(ZzWriter *self, ZzARM64Reg reg, zaddr address) {
    self->literal_insns[self->literal_insn_size].literal_insn_ptr = self->codedata;
    zz_arm64_writer_put_ldr_reg_imm(self, reg, (zuint)0x8);
    zz_arm64_writer_put_b_imm(self, 0xc);
    self->literal_insns[self->literal_insn_size++].literal_address_ptr = self->codedata;
    zz_arm64_writer_put_bytes(self, (zpointer)&address, sizeof(address));
}

zsize zz_arm64_writer_near_jump_range_size() { return ((1 << 25) << 2); }

void zz_arm64_writer_put_ldr_br_b_reg_address(ZzWriter *self, ZzARM64Reg reg, zaddr address) {
    self->literal_insns[self->literal_insn_size].literal_insn_ptr = self->codedata;
    zz_arm64_writer_put_ldr_reg_imm(self, reg, (zuint)0xc);
    zz_arm64_writer_put_br_reg(self, reg);
    zz_arm64_writer_put_b_imm(self, 0xc);
    self->literal_insns[self->literal_insn_size++].literal_address_ptr = self->codedata;
    zz_arm64_writer_put_bytes(self, (zpointer)&address, sizeof(address));
}

// ======= default =======

void zz_arm64_writer_put_ldr_reg_imm(ZzWriter *self, ZzARM64Reg reg, zuint32 offset) {
    ZzArm64RegInfo ri;
    zz_arm64_register_describe(reg, &ri);

    zuint32 imm19, Rt_ndx;

    imm19 = offset >> 2;
    Rt_ndx = ri.index;

    zz_arm64_writer_put_instruction(self, 0x58000000 | imm19 << 5 | Rt_ndx);
    return;
}

// PAGE: C6-871
void zz_arm64_writer_put_str_reg_reg_offset(ZzWriter *self, ZzARM64Reg src_reg, ZzARM64Reg dst_reg, zuint64 offset) {
    ZzArm64RegInfo rs, rd;

    zz_arm64_register_describe(src_reg, &rs);
    zz_arm64_register_describe(dst_reg, &rd);

    zuint32 size, v = 0, opc = 0, Rn_ndx, Rt_ndx;
    Rn_ndx = rd.index;
    Rt_ndx = rs.index;

    if (rs.is_integer) {
        size = (rs.width == 64) ? 0b11 : 0b10;
    }

    zuint32 imm12 = offset >> size;

    zz_arm64_writer_put_instruction(self, 0x39000000 | size << 30 | opc << 22 | imm12 << 10 | Rn_ndx << 5 | Rt_ndx);
    return;
}

void zz_arm64_writer_put_ldr_reg_reg_offset(ZzWriter *self, ZzARM64Reg dst_reg, ZzARM64Reg src_reg, zuint64 offset) {
    ZzArm64RegInfo rs, rd;

    zz_arm64_register_describe(src_reg, &rs);
    zz_arm64_register_describe(dst_reg, &rd);

    zuint32 size, v = 0, opc = 0b01, Rn_ndx, Rt_ndx;
    Rn_ndx = rs.index;
    Rt_ndx = rd.index;

    if (rs.is_integer) {
        size = (rs.width == 64) ? 0b11 : 0b10;
    }

    zuint32 imm12 = offset >> size;

    zz_arm64_writer_put_instruction(self, 0x39000000 | size << 30 | opc << 22 | imm12 << 10 | Rn_ndx << 5 | Rt_ndx);
    return;
}

// C6-562
void zz_arm64_writer_put_br_reg(ZzWriter *self, ZzARM64Reg reg) {
    ZzArm64RegInfo ri;
    zz_arm64_register_describe(reg, &ri);

    zuint32 op = 0, Rn_ndx;
    Rn_ndx = ri.index;
    zz_arm64_writer_put_instruction(self, 0xd61f0000 | op << 21 | Rn_ndx << 5);
    return;
}

// C6-561
void zz_arm64_writer_put_blr_reg(ZzWriter *self, ZzARM64Reg reg) {
    ZzArm64RegInfo ri;
    zz_arm64_register_describe(reg, &ri);

    zuint32 op = 0b01, Rn_ndx;

    Rn_ndx = ri.index;

    zz_arm64_writer_put_instruction(self, 0xd63f0000 | op << 21 | Rn_ndx << 5);
    return;
}

// C6-550
void zz_arm64_writer_put_b_imm(ZzWriter *self, zuint64 offset) {
    zuint32 op = 0b0, imm26;
    imm26 = (offset >> 2) & 0x03ffffff;
    zz_arm64_writer_put_instruction(self, 0x14000000 | op << 31 | imm26);
    return;
}

// TODO: standard form, need fix others
// PAGE: C6-549
void zz_arm64_writer_put_b_cond_imm(ZzWriter *self, zuint32 condition, zuint64 imm) {
    zuint32 imm19, cond;
    cond = condition;
    imm19 = (imm >> 2) & 0x7ffff;
    zz_arm64_writer_put_instruction(self, 0x54000000 | imm19 << 5 | cond);
    return;
}

// C6-525
void zz_arm64_writer_put_add_reg_reg_imm(ZzWriter *self, ZzARM64Reg dst_reg, ZzARM64Reg left_reg, zuint64 imm) {
    ZzArm64RegInfo rd, rl;

    zz_arm64_register_describe(dst_reg, &rd);
    zz_arm64_register_describe(left_reg, &rl);

    zuint32 sf = 1, op = 0, S = 0, shift = 0b00, imm12, Rn_ndx, Rd_ndx;

    Rd_ndx = rd.index;
    Rn_ndx = rl.index;
    imm12 = imm & 0xFFF;

    zz_arm64_writer_put_instruction(self, 0x11000000 | sf << 31 | op << 30 | S << 29 | shift << 22 | imm12 << 10 |
                                              Rn_ndx << 5 | Rd_ndx);
    return;
}

// C6-930
void zz_arm64_writer_put_sub_reg_reg_imm(ZzWriter *self, ZzARM64Reg dst_reg, ZzARM64Reg left_reg, zuint64 imm) {
    ZzArm64RegInfo rd, rl;

    zz_arm64_register_describe(dst_reg, &rd);
    zz_arm64_register_describe(left_reg, &rl);

    zuint32 sf = 1, op = 1, S = 0, shift = 0b00, imm12, Rn_ndx, Rd_ndx;

    Rd_ndx = rd.index;
    Rn_ndx = rl.index;
    imm12 = imm & 0xFFF;

    zz_arm64_writer_put_instruction(self, 0x11000000 | sf << 31 | op << 30 | S << 29 | shift << 22 | imm12 << 10 |
                                              Rn_ndx << 5 | Rd_ndx);
    return;
}

void zz_arm64_writer_put_bytes(ZzWriter *self, zbyte *data, zsize size) {
    memcpy(self->codedata, data, size);
    self->codedata = (zpointer)self->codedata + size;
    self->pc += size;
    self->size += size;
    return;
}

void zz_arm64_writer_put_instruction(ZzWriter *self, zuint32 insn) {
    *(zuint32 *)(self->codedata) = insn;
    self->codedata = (zpointer)self->codedata + sizeof(zuint32);
    self->pc += 4;
    self->size += 4;
    return;
}
