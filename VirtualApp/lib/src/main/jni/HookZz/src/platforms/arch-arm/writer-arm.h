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

#ifndef platforms_arch_arm_writer_arm_h
#define platforms_arch_arm_writer_arm_h

#include <string.h>

// platforms
#include "instructions.h"
#include "reader-arm.h"
#include "regs-arm.h"
#include "writer-arm.h"

// hookzz
#include "writer.h"

// zzdeps
#include "hookzz.h"
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

typedef ZzWriter ZzArmWriter;
ZzArmWriter *zz_arm_writer_new(zpointer data_ptr);
void zz_arm_writer_init(ZzArmWriter *self, zpointer data_ptr);
void zz_arm_writer_reset(ZzArmWriter *self, zpointer data_ptr);
zsize zz_arm_writer_near_jump_range_size();

// ------- user custom -------

void zz_arm_writer_put_ldr_b_reg_address(ZzArmWriter *self, ZzARMReg reg, zaddr address);
void zz_arm_writer_put_bx_to_thumb(ZzArmWriter *self);

// ------- architecture default -------

void zz_arm_writer_put_bytes(ZzArmWriter *self, zbyte *data, zuint data_size);
void zz_arm_writer_put_instruction(ZzArmWriter *self, zuint32 insn);
void zz_arm_writer_put_b_imm(ZzArmWriter *self, zuint32 imm);
void zz_arm_writer_put_bx_reg(ZzArmWriter *self, ZzARMReg reg);
void zz_arm_writer_put_nop(ZzArmWriter *self);
void zz_arm_writer_put_ldr_reg_reg_imm(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zint32 imm);
void zz_arm_writer_put_str_reg_reg_imm(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zint32 imm);
void zz_arm_writer_put_ldr_reg_imm_literal(ZzArmWriter *self, ZzARMReg dst_reg, zint32 imm);
void zz_arm_writer_put_ldr_reg_reg_imm_index(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zint32 imm,
                                             zbool index);
void zz_arm_writer_put_ldr_reg_reg_imm_A1(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zuint32 imm, zbool P,
                                          zbool U, zbool W);
void zz_arm_writer_put_ldr_reg_address(ZzArmWriter *self, ZzARMReg reg, zaddr address);
void zz_arm_writer_put_add_reg_reg_imm(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zuint32 imm);
void zz_arm_writer_put_sub_reg_reg_imm(ZzArmWriter *self, ZzARMReg dst_reg, ZzARMReg src_reg, zuint32 imm);
void zz_arm_writer_put_push_reg(ZzArmWriter *self, ZzARMReg reg);
void zz_arm_writer_put_pop_reg(ZzArmWriter *self, ZzARMReg reg);
ZzLiteralInstruction *zz_arm_writer_put_ldr_b_reg_relocate_address(ZzArmWriter *self, ZzARMReg reg, zaddr address,
                                                                   ZzLiteralInstruction **literal_insn_ptr);
ZzLiteralInstruction *zz_arm_writer_put_ldr_reg_relocate_address(ZzArmWriter *self, ZzARMReg reg, zaddr address,
                                                                 ZzLiteralInstruction **literal_insn_ptr);
#endif