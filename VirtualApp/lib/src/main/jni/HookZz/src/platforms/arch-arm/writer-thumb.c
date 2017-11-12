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

#include "writer-thumb.h"

#include <stdlib.h>

// ATTENTION !!!:
// 写 writer 部分, 需要参考, `Instrcution Set Encoding` 部分
// `witer` REF: `ZzInstruction Set Encoding`

ZzThumbWriter *zz_thumb_writer_new(zpointer data_ptr) {
    ZzThumbWriter *writer = (ZzThumbWriter *)malloc(sizeof(ZzThumbWriter));
    memset(writer, 0, sizeof(ZzThumbWriter));

    zaddr align_address = (zaddr)data_ptr & ~(zaddr)3;
    writer->codedata = (zpointer)align_address;
    writer->base = (zpointer)align_address;
    writer->pc = align_address;
    writer->size = 0;

    writer->literal_insn_size = 0;
    memset(writer->literal_insns, 0, sizeof(ZzLiteralInstruction) * MAX_LITERAL_INSN_SIZE);

    return writer;
}

void zz_thumb_writer_init(ZzThumbWriter *self, zpointer data_ptr) { zz_thumb_writer_reset(self, data_ptr); }

void zz_thumb_writer_reset(ZzThumbWriter *self, zpointer data_ptr) {
    zaddr align_address = (zaddr)data_ptr & ~(zaddr)3;

    self->codedata = (zpointer)align_address;
    self->base = (zpointer)align_address;
    self->pc = align_address;
    self->size = 0;

    self->literal_insn_size = 0;
    memset(self->literal_insns, 0, sizeof(ZzLiteralInstruction) * MAX_LITERAL_INSN_SIZE);
}

zsize zz_thumb_writer_near_jump_range_size() { return ((1 << 23) << 1); }

// ------- relocator -------

ZzLiteralInstruction *zz_thumb_writer_put_ldr_b_reg_relocate_address(ZzThumbWriter *self, ZzARMReg reg, zaddr address,
                                                                     ZzLiteralInstruction **literal_insn_ptr) {
    zz_thumb_writer_put_ldr_b_reg_address(self, reg, address);
    ZzLiteralInstruction *literal_insn = &(self->literal_insns[self->literal_insn_size - 1]);
    *literal_insn_ptr = literal_insn;
    return literal_insn;
}

ZzLiteralInstruction *zz_thumb_writer_put_ldr_reg_relocate_address(ZzThumbWriter *self, ZzARMReg reg, zaddr address,
                                                                   ZzLiteralInstruction **literal_insn_ptr) {
    zz_thumb_writer_put_ldr_reg_address(self, reg, address);
    ZzLiteralInstruction *literal_insn = &(self->literal_insns[self->literal_insn_size - 1]);
    *literal_insn_ptr = literal_insn;
    return literal_insn;
}

// ------- custom -------

void zz_thumb_writer_put_ldr_b_reg_address(ZzThumbWriter *self, ZzARMReg reg, zaddr address) {
    ZzArmRegInfo ri;
    zz_arm_register_describe(reg, &ri);
    self->literal_insns[self->literal_insn_size].literal_insn_ptr = self->codedata;

    if ((((zaddr)self->pc) % 4)) {
        if (ri.meta <= ZZ_ARM_REG_R7) {
            zz_thumb_writer_put_ldr_reg_imm(self, reg, 0x4);
            zz_thumb_writer_put_nop(self);
        } else {
            zz_thumb_writer_put_ldr_reg_imm(self, reg, 0x4);
        }
    } else {
        if (ri.meta <= ZZ_ARM_REG_R7) {
            zz_thumb_writer_put_ldr_reg_imm(self, reg, 0x0);
        } else {
            zz_thumb_writer_put_ldr_reg_imm(self, reg, 0x4);
            zz_thumb_writer_put_nop(self);
        }
    }

    zz_thumb_writer_put_b_imm(self, 0x2);
    self->literal_insns[self->literal_insn_size++].literal_address_ptr = self->codedata;
    zz_thumb_writer_put_bytes(self, (zpointer)&address, sizeof(zpointer));
    return;
}

void zz_thumb_writer_put_ldr_reg_address(ZzThumbWriter *self, ZzARMReg reg, zaddr address) {
    ZzArmRegInfo ri;
    zz_arm_register_describe(reg, &ri);

    self->literal_insns[self->literal_insn_size].literal_insn_ptr = self->codedata;

    if ((((zaddr)self->pc) % 4)) {
        if (ri.meta <= ZZ_ARM_REG_R7) {
            zz_thumb_writer_put_ldr_reg_imm(self, reg, 0x0);
        } else {
            zz_thumb_writer_put_ldr_reg_imm(self, reg, 0x4);
            zz_thumb_writer_put_nop(self);
        }
    } else {
        zz_thumb_writer_put_ldr_reg_imm(self, reg, 0x0);
        if (ri.meta <= ZZ_ARM_REG_R7)
            zz_thumb_writer_put_nop(self);
    }

    self->literal_insns[self->literal_insn_size++].literal_address_ptr = self->codedata;
    zz_thumb_writer_put_bytes(self, (zpointer)&address, sizeof(zpointer));
    return;
}

// ------- architecture default -------
void zz_thumb_writer_put_nop(ZzThumbWriter *self) {
    zz_thumb_writer_put_instruction(self, 0x46c0);
    return;
}

void zz_thumb_writer_put_bytes(ZzThumbWriter *self, zbyte *data, zuint data_size) {
    memcpy(self->codedata, data, data_size);
    self->codedata = (zpointer)self->codedata + data_size;
    self->pc += data_size;
    self->size += data_size;
    return;
}

void zz_thumb_writer_put_instruction(ZzThumbWriter *self, uint16_t insn) {
    *(uint16_t *)(self->codedata) = insn;
    self->codedata = (zpointer)self->codedata + sizeof(uint16_t);
    self->pc += 2;
    self->size += 2;
    return;
}

void zz_thumb_writer_put_b_imm(ZzThumbWriter *self, zuint32 imm) {

    zz_thumb_writer_put_instruction(self, 0xe000 | ((imm / 2) & ZZ_INT11_MASK));
    return;
}

void zz_thumb_writer_put_bx_reg(ZzThumbWriter *self, ZzARMReg reg) {
    ZzArmRegInfo ri;

    zz_arm_register_describe(reg, &ri);

    if ((((zaddr)self->pc) % 4)) {
        zz_thumb_writer_put_nop(self);
    }

    zz_thumb_writer_put_instruction(self, 0x4700 | (ri.index << 3));
    zz_thumb_writer_put_nop(self);
    return;
}

void zz_thumb_writer_put_blx_reg(ZzThumbWriter *self, ZzARMReg reg) {
    ZzArmRegInfo ri;

    zz_arm_register_describe(reg, &ri);

    zz_thumb_writer_put_instruction(self, 0x4780 | (ri.index << 3));
    return;
}

// A8.8.18
void zz_thumb_writer_put_branch_imm(ZzThumbWriter *self, zuint32 imm, zbool link, zbool thumb) {
    union {
        zint32 i;
        zuint32 u;
    } distance;
    zuint16 s, j1, j2, imm10, imm11;

    distance.i = (zint32)(imm) / 2;

    s = (distance.u >> 31) & 1;
    j1 = (~((distance.u >> 22) ^ s)) & 1;
    j2 = (~((distance.u >> 21) ^ s)) & 1;

    imm10 = (distance.u >> 11) & ZZ_INT10_MASK;
    imm11 = distance.u & ZZ_INT11_MASK;

    zz_thumb_writer_put_instruction(self, 0xf000 | (s << 10) | imm10);
    zz_thumb_writer_put_instruction(self, 0x8000 | (link << 14) | (j1 << 13) | (thumb << 12) | (j2 << 11) | imm11);
    return;
}

void zz_thumb_writer_put_bl_imm(ZzThumbWriter *self, zuint32 imm) {
    zz_thumb_writer_put_branch_imm(self, imm, TRUE, TRUE);
    return;
}

void zz_thumb_writer_put_blx_imm(ZzThumbWriter *self, zuint32 imm) {
    zz_thumb_writer_put_branch_imm(self, imm, TRUE, FALSE);
    return;
}

void zz_thumb_writer_put_b_imm32(ZzThumbWriter *self, zuint32 imm) {
    zz_thumb_writer_put_branch_imm(self, imm, FALSE, TRUE);
    return;
}

// PAGE: A8-410
// A8.8.64 LDR (literal)
void zz_thumb_writer_put_ldr_reg_imm(ZzThumbWriter *self, ZzARMReg reg, zint32 imm) {
    ZzArmRegInfo ri;

    zz_arm_register_describe(reg, &ri);

    if (ri.meta <= ZZ_ARM_REG_R7 && imm >= 0 && imm < ((1 << 8) << 2)) {

        zz_thumb_writer_put_instruction(self, 0x4800 | (ri.index << 8) | ((imm / 4) & ZZ_INT8_MASK));
    } else if (imm < (1 << 12)) {
        zbool add = 0;
        if (imm >= 0)
            add = 1;
        zz_thumb_writer_put_instruction(self, 0xf85f | (add << 7));
        zz_thumb_writer_put_instruction(self, (ri.index << 12) | ABS(imm));
    }
    return;
}

zbool zz_thumb_writer_put_transfer_reg_reg_offset_T1(ZzThumbWriter *self, ZzThumbMemoryOperation operation,
                                                     ZzARMReg left_reg, ZzARMReg right_reg, zint32 right_offset) {
    ZzArmRegInfo lr, rr;

    zz_arm_register_describe(left_reg, &lr);
    zz_arm_register_describe(right_reg, &rr);

    zuint16 insn;

    if (right_offset < 0)
        return FALSE;

    if (lr.meta <= ZZ_ARM_REG_R7 && rr.meta <= ZZ_ARM_REG_R7 && right_offset < ((1 << 5) << 2)) {
        insn = 0x6000 | (right_offset / 4) << 6 | (rr.index << 3) | lr.index;
        if (operation == ZZ_THUMB_MEMORY_LOAD)
            insn |= 0x0800;
        zz_thumb_writer_put_instruction(self, insn);
        return TRUE;
    }
    return FALSE;
}

zbool zz_thumb_writer_put_transfer_reg_reg_offset_T2(ZzThumbWriter *self, ZzThumbMemoryOperation operation,
                                                     ZzARMReg left_reg, ZzARMReg right_reg, zint32 right_offset) {
    ZzArmRegInfo lr, rr;

    zz_arm_register_describe(left_reg, &lr);
    zz_arm_register_describe(right_reg, &rr);

    zuint16 insn;

    if (right_offset < 0)
        return FALSE;

    if (rr.meta == ZZ_ARM_REG_SP && lr.meta <= ZZ_ARM_REG_R7 && right_offset < ((1 << 8) << 2)) {
        insn = 0x9000 | (lr.index << 8) | (right_offset / 4);
        if (operation == ZZ_THUMB_MEMORY_LOAD)
            insn |= 0x0800;
        zz_thumb_writer_put_instruction(self, insn);
        return TRUE;
    }
    return FALSE;
}

zbool zz_thumb_writer_put_transfer_reg_reg_offset_T3(ZzThumbWriter *self, ZzThumbMemoryOperation operation,
                                                     ZzARMReg left_reg, ZzARMReg right_reg, zint32 right_offset) {
    ZzArmRegInfo lr, rr;

    zz_arm_register_describe(left_reg, &lr);
    zz_arm_register_describe(right_reg, &rr);

    zuint16 insn;

    if (right_offset < 0)
        return FALSE;

    if (right_offset < (1 << 12)) {
        if (rr.meta == ZZ_ARM_REG_PC) {
            zz_thumb_writer_put_ldr_reg_imm(self, left_reg, right_offset);
        }
        zz_thumb_writer_put_instruction(self,
                                        0xf8c0 | ((operation == ZZ_THUMB_MEMORY_LOAD) ? 0x0010 : 0x0000) | rr.index);
        zz_thumb_writer_put_instruction(self, (lr.index << 12) | right_offset);

        return TRUE;
    }
    return FALSE;
}

zbool zz_thumb_writer_put_transfer_reg_reg_offset_T4(ZzThumbWriter *self, ZzThumbMemoryOperation operation,
                                                     ZzARMReg left_reg, ZzARMReg right_reg, zint32 right_offset,
                                                     zbool index, zbool wback) {
    ZzArmRegInfo lr, rr;

    zz_arm_register_describe(left_reg, &lr);
    zz_arm_register_describe(right_reg, &rr);

    zuint16 insn;

    if (ABS(right_offset) < (1 << 8)) {
        if (rr.meta == ZZ_ARM_REG_PC) {
            zz_thumb_writer_put_ldr_reg_imm(self, left_reg, right_offset);
        } else {
            zbool add = 0;
            if (right_offset > 0)
                add = 1;
            zz_thumb_writer_put_instruction(self, 0xf840 | ((operation == ZZ_THUMB_MEMORY_LOAD) ? 0x0010 : 0x0000) |
                                                      rr.index);
            zz_thumb_writer_put_instruction(self, 0x0800 | (lr.index << 12) | (index << 10) | (add << 9) |
                                                      (wback << 8) | (ABS(right_offset)));
            return TRUE;
        }
    }
    return FALSE;
}

// PAGE: A8-406
// PAGE: A8.8.203 STR (immediate, Thumb)
static void zz_thumb_writer_put_transfer_reg_reg_offset(ZzThumbWriter *self, ZzThumbMemoryOperation operation,
                                                        ZzARMReg left_reg, ZzARMReg right_reg, zint32 right_offset) {
    if (zz_thumb_writer_put_transfer_reg_reg_offset_T1(self, operation, left_reg, right_reg, right_offset))
        return;

    if (zz_thumb_writer_put_transfer_reg_reg_offset_T2(self, operation, left_reg, right_reg, right_offset))
        return;

    if (zz_thumb_writer_put_transfer_reg_reg_offset_T3(self, operation, left_reg, right_reg, right_offset))
        return;
    if (zz_thumb_writer_put_transfer_reg_reg_offset_T4(self, operation, left_reg, right_reg, right_offset, 1, 0))
        return;
    return;
}

void zz_thumb_writer_put_ldr_reg_reg_offset(ZzThumbWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg,
                                            zint32 src_offset) {
    zz_thumb_writer_put_transfer_reg_reg_offset(self, ZZ_THUMB_MEMORY_LOAD, dst_reg, src_reg, src_offset);
    return;
}

void zz_thumb_writer_put_str_reg_reg_offset(ZzThumbWriter *self, ZzARMReg src_reg, ZzARMReg dst_reg,
                                            zint32 dst_offset) {
    zz_thumb_writer_put_transfer_reg_reg_offset(self, ZZ_THUMB_MEMORY_STORE, src_reg, dst_reg, dst_offset);
    return;
}

void zz_thumb_writer_put_ldr_index_reg_reg_offset(ZzThumbWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg,
                                                  zint32 src_offset, zbool index) {
    zz_thumb_writer_put_transfer_reg_reg_offset_T4(self, ZZ_THUMB_MEMORY_LOAD, dst_reg, src_reg, src_offset, index, 1);
    return;
}

void zz_thumb_writer_put_str_index_reg_reg_offset(ZzThumbWriter *self, ZzARMReg src_reg, ZzARMReg dst_reg,
                                                  zint32 dst_offset, zbool index) {
    zz_thumb_writer_put_transfer_reg_reg_offset_T4(self, ZZ_THUMB_MEMORY_STORE, src_reg, dst_reg, dst_offset, index, 1);
    return;
}

void zz_thumb_writer_put_str_reg_reg(ZzThumbWriter *self, ZzARMReg src_reg, ZzARMReg dst_reg) {
    zz_thumb_writer_put_str_reg_reg_offset(self, src_reg, dst_reg, 0);
    return;
}

void zz_thumb_writer_put_ldr_reg_reg(ZzThumbWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg) {
    zz_thumb_writer_put_ldr_reg_reg_offset(self, dst_reg, src_reg, 0);
    return;
}

void zz_thumb_writer_put_add_reg_imm(ZzThumbWriter *self, ZzARMReg dst_reg, zint32 imm) {
    ZzArmRegInfo dst;
    zuint16 sign_mask, insn;

    zz_arm_register_describe(dst_reg, &dst);

    sign_mask = 0x0000;
    if (dst.meta == ZZ_ARM_REG_SP) {

        if (imm < 0)
            sign_mask = 0x0080;

        insn = 0xb000 | sign_mask | ABS(imm / 4);
    } else {
        if (imm < 0)
            sign_mask = 0x0800;

        insn = 0x3000 | sign_mask | (dst.index << 8) | ABS(imm);
    }

    zz_thumb_writer_put_instruction(self, insn);
    return;
}

void zz_thumb_writer_put_sub_reg_imm(ZzThumbWriter *self, ZzARMReg dst_reg, zint32 imm) {
    zz_thumb_writer_put_add_reg_imm(self, dst_reg, -imm);
    return;
}

void zz_thumb_writer_put_add_reg_reg_imm(ZzThumbWriter *self, ZzARMReg dst_reg, ZzARMReg left_reg, zint32 right_value) {
    ZzArmRegInfo dst, left;
    zuint16 insn;

    zz_arm_register_describe(dst_reg, &dst);
    zz_arm_register_describe(left_reg, &left);

    if (left.meta == dst.meta) {
        return zz_thumb_writer_put_add_reg_imm(self, dst_reg, right_value);
    }

    if (dst.meta <= ZZ_ARM_REG_R7 && left.meta <= ZZ_ARM_REG_R7 && ABS(right_value) < (1 << 3)) {
        zuint32 sign_mask = 0;

        if (right_value < 0)
            sign_mask = 1 << 9;

        insn = 0x1c00 | sign_mask | (ABS(right_value) << 6) | (left.index << 3) | dst.index;
        zz_thumb_writer_put_instruction(self, insn);
    } else if ((left.meta == ZZ_ARM_REG_SP || left.meta == ZZ_ARM_REG_PC) && dst.meta <= ZZ_ARM_REG_R7 &&
               right_value > 0 && (right_value % 4 == 0) && right_value < (1 << 8)) {
        zuint16 base_mask;

        if (left.meta == ZZ_ARM_REG_SP)
            base_mask = 0x0800;
        else
            base_mask = 0x0000;

        insn = 0xa000 | base_mask | (dst.index << 8) | (right_value / 4);
        zz_thumb_writer_put_instruction(self, insn);
    } else {
        zuint16 insn1, insn2;
        zuint i, imm3, imm8;
        i = (ABS(right_value) >> (3 + 8)) & 0x1;
        imm3 = (ABS(right_value) >> 8) & 0b111;
        imm8 = ABS(right_value) & 0b11111111;

        // A8-708, sub
        // A8-306 add
        if (right_value < 0)
            zz_thumb_writer_put_instruction(self, 0b1111001010100000 | i << 10 | left.index);
        else
            zz_thumb_writer_put_instruction(self, 0b1111001000000000 | i << 10 | left.index);
        zz_thumb_writer_put_instruction(self, 0b0 | imm3 << 12 | dst.index << 8 | imm8);
    }

    return;
}

void zz_thumb_writer_put_sub_reg_reg_imm(ZzThumbWriter *self, ZzARMReg dst_reg, ZzARMReg left_reg, zint32 right_value) {
    zz_thumb_writer_put_add_reg_reg_imm(self, dst_reg, left_reg, -right_value);
    return;
}

void zz_thumb_writer_put_push_reg(ZzThumbWriter *self, ZzARMReg reg) {
    ZzArmRegInfo ri;
    zz_arm_register_describe(reg, &ri);

    zuint16 M, register_list;
    M = 0;

    zz_thumb_writer_put_instruction(self, 0b1011010000000000 | M << 8 | 1 << ri.index);
    return;
}

void zz_thumb_writer_put_pop_reg(ZzThumbWriter *self, ZzARMReg reg) {
    ZzArmRegInfo ri;
    zz_arm_register_describe(reg, &ri);

    zuint16 P, register_list;
    P = 0;

    zz_thumb_writer_put_instruction(self, 0b1011110000000000 | P << 8 | 1 << ri.index);
    return;
}

void zz_thumb_writer_put_add_reg_reg_reg(ZzThumbWriter *self, ZzARMReg dst_reg, ZzARMReg left_reg, ZzARMReg right_reg) {
    ZzArmRegInfo dst, left, right;
    zz_arm_register_describe(dst_reg, &dst);
    zz_arm_register_describe(left_reg, &left);
    zz_arm_register_describe(right_reg, &right);

    zuint16 Rm_ndx, Rn_ndx, Rd_ndx;
    Rd_ndx = dst.index;
    Rm_ndx = right.index;
    Rn_ndx = left.index;

    zz_thumb_writer_put_instruction(self, 0b0001100000000000 | Rm_ndx << 6 | Rn_ndx << 3 | Rd_ndx);
    return;
}