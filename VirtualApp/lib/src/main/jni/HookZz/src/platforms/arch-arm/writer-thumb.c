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
    int t = 4 - (zaddr)data_ptr % 4;

    writer->codedata = data_ptr + t;
    writer->base = data_ptr + t;
    writer->pc = data_ptr + t;
    writer->size = 0;
    return writer;
}

void zz_thumb_writer_init(ZzThumbWriter *self, zpointer data_ptr) { zz_thumb_writer_reset(self, data_ptr); }

void zz_thumb_writer_reset(ZzThumbWriter *self, zpointer data_ptr) {
    int t = (zaddr)data_ptr % 4;

    self->codedata = data_ptr + t;
    self->base = data_ptr + t;
    self->pc = data_ptr + t + 4;
    self->size = 0;
}

zpointer zz_thumb_writer_put_ldr_b_reg_address(ZzThumbWriter *self, ZzARMReg reg, zaddr address) {
    ZzArmRegInfo ri;

    zz_arm_register_describe(reg, &ri);

    // if (((zaddr)self->pc) % 4) {
    //     zz_thumb_writer_put_ldr_reg_imm(self, reg, 0x4);
    //     if (ri.meta <= ZZ_ARM_REG_R7) {
    //         zz_thumb_writer_put_nop(self);
    //     }
    // } else {
    //     if (ri.meta <= ZZ_ARM_REG_R7) {
    //         zz_thumb_writer_put_ldr_reg_imm(self, reg, 0x0);
    //     } else {
    //         zz_thumb_writer_put_ldr_reg_imm(self, reg, 0x4);
    //         zz_thumb_writer_put_nop(self);
    //     }
    // }

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
    zz_thumb_writer_put_bytes(self, (zpointer)&address, sizeof(zpointer));
    return self->pc;
}

zpointer zz_thumb_writer_put_ldr_reg_address(ZzThumbWriter *self, ZzARMReg reg, zaddr address) {
    ZzArmRegInfo ri;

    zz_arm_register_describe(reg, &ri);
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

    // if ((((zaddr)self->pc) % 4)) {
    //     zz_thumb_writer_put_nop(self);
    // }
    zz_thumb_writer_put_bytes(self, (zpointer)&address, sizeof(zpointer));
    return self->pc;
}
// ------- user custom -------

// ------- architecture default -------
zpointer zz_thumb_writer_put_nop(ZzThumbWriter *self) {
    zz_thumb_writer_put_instruction(self, 0x46c0);
    return self->pc;
}

zpointer zz_thumb_writer_put_bytes(ZzThumbWriter *self, zbyte *data, zuint data_size) {
    memcpy(self->codedata, data, data_size);
    self->codedata = (zpointer)self->codedata + data_size;
    self->pc += data_size;
    self->size += data_size;
    return self->pc;
}

zpointer zz_thumb_writer_put_instruction(ZzThumbWriter *self, uint16_t insn) {
    *(uint16_t *)(self->codedata) = insn;
    self->codedata = (zpointer)self->codedata + sizeof(uint16_t);
    self->pc += 2;
    self->size += 2;
    return self->pc;
}

zpointer zz_thumb_writer_put_b_imm(ZzThumbWriter *self, zuint32 imm) {

    zz_thumb_writer_put_instruction(self, 0xe000 | ((imm / 2) & ZZ_INT11_MASK));
    return self->pc;
}

zpointer zz_thumb_writer_put_bx_reg(ZzThumbWriter *self, ZzARMReg reg) {
    ZzArmRegInfo ri;

    zz_arm_register_describe(reg, &ri);

    if ((((zaddr)self->pc) % 4)) {
        zz_thumb_writer_put_nop(self);
    }

    zz_thumb_writer_put_instruction(self, 0x4700 | (ri.index << 3));
    zz_thumb_writer_put_nop(self);
    return self->pc;
}

zpointer zz_thumb_writer_put_blx_reg(ZzThumbWriter *self, ZzARMReg reg) {
    ZzArmRegInfo ri;

    zz_arm_register_describe(reg, &ri);

    zz_thumb_writer_put_instruction(self, 0x4780 | (ri.index << 3));
    return self->pc;
}

// A8.8.18
zpointer zz_thumb_writer_put_branch_imm(ZzThumbWriter *self, zuint32 imm, zbool link, zbool thumb) {
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
    return self->pc;
}

zpointer zz_thumb_writer_put_bl_imm(ZzThumbWriter *self, zuint32 imm) {
    zz_thumb_writer_put_branch_imm(self, imm, TRUE, TRUE);
    return self->pc;
}

zpointer zz_thumb_writer_put_blx_imm(ZzThumbWriter *self, zuint32 imm) {
    zz_thumb_writer_put_branch_imm(self, imm, TRUE, FALSE);
    return self->pc;
}

zpointer zz_thumb_writer_put_b_imm32(ZzThumbWriter *self, zuint32 imm) {
    zz_thumb_writer_put_branch_imm(self, imm, FALSE, TRUE);
    return self->pc;
}

// PAGE: A8-410
// A8.8.64 LDR (literal)
zpointer zz_thumb_writer_put_ldr_reg_imm(ZzThumbWriter *self, ZzARMReg reg, zint32 imm) {
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
    return self->pc;
}

// static zpointer zz_thumb_writer_put_transfer_reg_reg_offset(ZzThumbWriter
// *self,
//                                                         ZzThumbMemoryOperation
//                                                         operation,
//                                                         ZzARMReg left_reg,
//                                                         ZzARMReg right_reg,
//                                                         zint32 right_offset)
//                                                         {
//     zz_arm_register_describe(left_reg, &lr);
//     zz_arm_register_describe(right_reg, &rr);

//     if (right_offset >= 0) {
//         if (lr.meta <= ZZ_ARM_REG_R7 && (rr.meta <= ZZ_ARM_REG_R7 || rr.meta ==
//         ZZ_ARM_REG_SP) &&
//             ((rr.meta == ZZ_ARM_REG_SP && right_offset <= 1020) ||
//              (rr.meta != ZZ_ARM_REG_SP && right_offset <= 124)) &&
//             (right_offset % 4) == 0) {
//             zuint16 insn;

//             if (rr.meta == ZZ_ARM_REG_SP)
//                 insn = 0x9000 | (lr.index << 8) | (right_offset / 4);
//             else
//                 insn = 0x6000 | (right_offset / 4) << 6 | (rr.index << 3) |
//                 lr.index;

//             if (operation == ZZ_THUMB_MEMORY_LOAD)
//                 insn |= 0x0800;

//             zz_thumb_writer_put_instruction(self, insn);
//         } else {
//             if (right_offset > 4095)
//                 return;
//             zz_thumb_writer_put_instruction(
//                 self, 0xf8c0 | ((operation == ZZ_THUMB_MEMORY_LOAD) ? 0x0010
//                 : 0x0000) |
//                 rr.index);
//             zz_thumb_writer_put_instruction(self, (lr.index << 12) |
//             right_offset);
//         }
//     } else {
//         if ((rr.index & 0xF) == 0xF) {
//             zz_thumb_writer_put_ldr_reg_imm(self, left_reg, right_offset);
//         } else {
//             zz_thumb_writer_put_instruction(
//                 self, 0xf840 | ((operation == ZZ_THUMB_MEMORY_LOAD) ? 0x0010
//                 : 0x0000) |
//                 rr.index);
//             zz_thumb_writer_put_instruction(self, 0x0c00 | (lr.index << 12) |
//                                                       (ABS(right_offset) &
//                                                       ZZ_INT8_MASK));
//         }
//     }
// }

zpointer zz_thumb_writer_put_transfer_reg_reg_offset_T1(ZzThumbWriter *self, ZzThumbMemoryOperation operation,
                                                        ZzARMReg left_reg, ZzARMReg right_reg, zint32 right_offset) {
    ZzArmRegInfo lr, rr;

    zz_arm_register_describe(left_reg, &lr);
    zz_arm_register_describe(right_reg, &rr);

    zuint16 insn;

    if (right_offset < 0)
        return 0;

    if (lr.meta <= ZZ_ARM_REG_R7 && rr.meta <= ZZ_ARM_REG_R7 && right_offset < ((1 << 5) << 2)) {
        insn = 0x6000 | (right_offset / 4) << 6 | (rr.index << 3) | lr.index;
        if (operation == ZZ_THUMB_MEMORY_LOAD)
            insn |= 0x0800;
        zz_thumb_writer_put_instruction(self, insn);
        return self->pc;
    }
    return 0;
}

zpointer zz_thumb_writer_put_transfer_reg_reg_offset_T2(ZzThumbWriter *self, ZzThumbMemoryOperation operation,
                                                        ZzARMReg left_reg, ZzARMReg right_reg, zint32 right_offset) {
    ZzArmRegInfo lr, rr;

    zz_arm_register_describe(left_reg, &lr);
    zz_arm_register_describe(right_reg, &rr);

    zuint16 insn;

    if (right_offset < 0)
        return 0;

    if (rr.meta == ZZ_ARM_REG_SP && lr.meta <= ZZ_ARM_REG_R7 && right_offset < ((1 << 8) << 2)) {
        insn = 0x9000 | (lr.index << 8) | (right_offset / 4);
        if (operation == ZZ_THUMB_MEMORY_LOAD)
            insn |= 0x0800;
        zz_thumb_writer_put_instruction(self, insn);
        return self->pc;
    }
    return 0;
}

zpointer zz_thumb_writer_put_transfer_reg_reg_offset_T3(ZzThumbWriter *self, ZzThumbMemoryOperation operation,
                                                        ZzARMReg left_reg, ZzARMReg right_reg, zint32 right_offset) {
    ZzArmRegInfo lr, rr;

    zz_arm_register_describe(left_reg, &lr);
    zz_arm_register_describe(right_reg, &rr);

    zuint16 insn;

    if (right_offset < 0)
        return 0;

    if (right_offset < (1 << 12)) {
        // if (operation == ZZ_THUMB_MEMORY_LOAD && rr.meta == ZZ_ARM_REG_PC &&
        // (self->pc % 4)) {
        //     zz_thumb_writer_put_nop(self);
        // }
        if (rr.meta == ZZ_ARM_REG_PC) {
            zz_thumb_writer_put_ldr_reg_imm(self, left_reg, right_offset);
        }
        zz_thumb_writer_put_instruction(self,
                                        0xf8c0 | ((operation == ZZ_THUMB_MEMORY_LOAD) ? 0x0010 : 0x0000) | rr.index);
        zz_thumb_writer_put_instruction(self, (lr.index << 12) | right_offset);

        return self->pc;
    }
    return 0;
}

zpointer zz_thumb_writer_put_transfer_reg_reg_offset_T4(ZzThumbWriter *self, ZzThumbMemoryOperation operation,
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
            return self->pc;
        }
    }
    return 0;
}

// PAGE: A8-406
// PAGE: A8.8.203 STR (immediate, Thumb)
static zpointer zz_thumb_writer_put_transfer_reg_reg_offset(ZzThumbWriter *self, ZzThumbMemoryOperation operation,
                                                            ZzARMReg left_reg, ZzARMReg right_reg,
                                                            zint32 right_offset) {
    if (zz_thumb_writer_put_transfer_reg_reg_offset_T1(self, operation, left_reg, right_reg, right_offset))
        return self->pc;

    if (zz_thumb_writer_put_transfer_reg_reg_offset_T2(self, operation, left_reg, right_reg, right_offset))
        return self->pc;

    if (zz_thumb_writer_put_transfer_reg_reg_offset_T3(self, operation, left_reg, right_reg, right_offset))
        return self->pc;
    if (zz_thumb_writer_put_transfer_reg_reg_offset_T4(self, operation, left_reg, right_reg, right_offset, 1, 0))
        return self->pc;
    return 0;
}

zpointer zz_thumb_writer_put_ldr_reg_reg_offset(ZzThumbWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg,
                                                zint32 src_offset) {
    zz_thumb_writer_put_transfer_reg_reg_offset(self, ZZ_THUMB_MEMORY_LOAD, dst_reg, src_reg, src_offset);
    return self->pc;
}

zpointer zz_thumb_writer_put_str_reg_reg_offset(ZzThumbWriter *self, ZzARMReg src_reg, ZzARMReg dst_reg,
                                                zint32 dst_offset) {
    zz_thumb_writer_put_transfer_reg_reg_offset(self, ZZ_THUMB_MEMORY_STORE, src_reg, dst_reg, dst_offset);
    return self->pc;
}

zpointer zz_thumb_writer_put_ldr_index_reg_reg_offset(ZzThumbWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg,
                                                      zint32 src_offset, zbool index) {
    zz_thumb_writer_put_transfer_reg_reg_offset_T4(self, ZZ_THUMB_MEMORY_LOAD, dst_reg, src_reg, src_offset, index, 1);
    return self->pc;
}

zpointer zz_thumb_writer_put_str_index_reg_reg_offset(ZzThumbWriter *self, ZzARMReg src_reg, ZzARMReg dst_reg,
                                                      zint32 dst_offset, zbool index) {
    zz_thumb_writer_put_transfer_reg_reg_offset_T4(self, ZZ_THUMB_MEMORY_STORE, src_reg, dst_reg, dst_offset, index, 1);
    return self->pc;
}

zpointer zz_thumb_writer_put_str_reg_reg(ZzThumbWriter *self, ZzARMReg src_reg, ZzARMReg dst_reg) {
    zz_thumb_writer_put_str_reg_reg_offset(self, src_reg, dst_reg, 0);
    return self->pc;
}

zpointer zz_thumb_writer_put_ldr_reg_reg(ZzThumbWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg) {
    zz_thumb_writer_put_ldr_reg_reg_offset(self, dst_reg, src_reg, 0);
    return self->pc;
}

zpointer zz_thumb_writer_put_add_reg_imm(ZzThumbWriter *self, ZzARMReg dst_reg, zint32 imm) {
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
    return self->pc;
}

zpointer zz_thumb_writer_put_sub_reg_imm(ZzThumbWriter *self, ZzARMReg dst_reg, zint32 imm) {
    zz_thumb_writer_put_add_reg_imm(self, dst_reg, -imm);
    return self->pc;
}

zpointer zz_thumb_writer_put_add_reg_reg_imm(ZzThumbWriter *self, ZzARMReg dst_reg, ZzARMReg left_reg,
                                             zint32 right_value) {
    ZzArmRegInfo dst, left;
    zuint16 insn;

    zz_arm_register_describe(dst_reg, &dst);
    zz_arm_register_describe(left_reg, &left);

    if (left.meta == dst.meta) {
        return zz_thumb_writer_put_add_reg_imm(self, dst_reg, right_value);
    }

    if (left.meta == ZZ_ARM_REG_SP || left.meta == ZZ_ARM_REG_PC) {
        zuint16 base_mask;

        if (right_value < 0 || right_value % 4 != 0)
            return 0;

        if (left.meta == ZZ_ARM_REG_SP)
            base_mask = 0x0800;
        else
            base_mask = 0x0000;

        insn = 0xa000 | base_mask | (dst.index << 8) | (right_value / 4);
    } else {
        zuint16 sign_mask = 0x0000;

        if (ABS(right_value) > 7)
            return 0;

        if (right_value < 0)
            sign_mask = 0x0200;

        insn = 0x1c00 | sign_mask | (ABS(right_value) << 6) | (left.index << 3) | dst.index;
    }

    zz_thumb_writer_put_instruction(self, insn);

    return self->pc;
}

zpointer zz_thumb_writer_put_sub_reg_reg_imm(ZzThumbWriter *self, ZzARMReg dst_reg, ZzARMReg left_reg,
                                             zint32 right_value) {
    zz_thumb_writer_put_add_reg_reg_imm(self, dst_reg, left_reg, -right_value);
    return self->pc;
}

zsize zz_thumb_writer_near_jump_range_size() { return ((1 << 23) << 1); }
