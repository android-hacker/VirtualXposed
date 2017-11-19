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

#include "reader-arm.h"

zpointer zz_arm_reader_read_one_instruction(ZzInstruction *insn_ctx, zpointer address) {
    insn_ctx->type = ARM_INSN;
    insn_ctx->address = (zaddr)address;
    insn_ctx->pc = (zaddr)address + 8;
    insn_ctx->insn = *(zuint32 *)address;
    insn_ctx->size = 4;
    return (zpointer)insn_ctx->pc;
}

// ARM Manual
// A5 ARM Instruction Set Encoding
// A5.3 Load/store word and unsigned byte
ARMInsnType GetARMInsnType(zuint32 insn) {

    if (insn_equal(insn, "xxxx0000100xxxxxxxxxxxxxxxx0xxxx") && (get_insn_sub(insn, 28, 4) != 0xF)) {
        return ARM_INS_ADD_register_A1;
    }

    if (insn_equal(insn, "xxxx0101x0011111xxxxxxxxxxxxxxxx") && (get_insn_sub(insn, 28, 4) != 0xF)) {
        return ARM_INS_LDR_literal_A1;
    }

    if (insn_equal(insn, "xxxx001010001111xxxxxxxxxxxxxxxx") && (get_insn_sub(insn, 28, 4) != 0xF)) {
        return ARM_INS_ADR_A1;
    }
    if (insn_equal(insn, "xxxx001001001111xxxxxxxxxxxxxxxx") && (get_insn_sub(insn, 28, 4) != 0xF)) {
        return ARM_INS_ADR_A2;
    }
    if (insn_equal(insn, "xxxx1010xxxxxxxxxxxxxxxxxxxxxxxx") && (get_insn_sub(insn, 28, 4) != 0xF)) {
        return ARM_INS_B_A1;
    }
    if (insn_equal(insn, "xxxx1011xxxxxxxxxxxxxxxxxxxxxxxx") && (get_insn_sub(insn, 28, 4) != 0xF)) {
        return ARM_INS_BLBLX_immediate_A1;
    }
    if (insn_equal(insn, "1111101xxxxxxxxxxxxxxxxxxxxxxxxx")) {
        return ARM_INS_BLBLX_immediate_A2;
    }

    return ARM_UNDEF;
}