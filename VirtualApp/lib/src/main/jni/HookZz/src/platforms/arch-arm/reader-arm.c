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

// static csh handle;

// void zz_arm_reader_capstone_init(void) {
//     cs_err err = 0;

//     err = cs_open(CS_ARCH_ARM, CS_MODE_ARM, &handle);
//     if (err) {
//         Xerror("Failed on cs_open() with error returned: %u\n", err);
//         exit(-1);
//     }

//     cs_option(handle, CS_OPT_DETAIL, CS_OPT_ON);
// }

// cs_insn *zz_arm_reader_disassemble_at(zpointer address) {
//     if (!handle)
//         zz_arm_reader_capstone_init();
//     cs_insn *insn;
//     size_t count;
//     count = cs_disasm(handle, address, 16, (unsigned long)address, 0, &insn);
//     if (!insn) {
// #if defined(DEBUG_MODE)
//         debug_break();
// #endif
//         Xerror("zz_arm_reader_disassemble_at error at %p", (zpointer)address);
//     }
//     return insn;
// }

zpointer zz_arm_reader_read_one_instruction(ZzInstruction *insn_ctx, zpointer address) {
    // ZzInstruction *insn = (ZzInstruction *)malloc(sizeof(ZzInstruction));

    insn_ctx->type = ARM_INSN;
    insn_ctx->address = (zaddr)address;
    insn_ctx->pc = (zaddr)address;
    insn_ctx->insn = *(zuint32 *)address;
    insn_ctx->size = 4;
    return (zpointer)insn_ctx->pc;
}

// ARM Manual
// A5 ARM Instruction Set Encoding
// A5.3 Load/store word and unsigned byte
ARMInsnType GetARMInsnType(zuint32 insn) {
    zuint32 op, op1;
    op1 = get_insn_sub(insn, 20, 5);

    if (insn_equal(insn, "xxxx0101x0011111xxxxxxxxxxxxxxxx")) {
        return ARM_INS_LDR_A1;
    }

    if (insn_equal(insn, "xxxx001010001111xxxxxxxxxxxxxxxx")) {
        return ARM_INS_ADR_A1;
    }

    if (insn_equal(insn, "xxxx001001001111xxxxxxxxxxxxxxxx")) {
        return ARM_INS_ADR_A2;
    }

    if (insn_equal(insn, "xxxx1010xxxxxxxxxxxxxxxxxxxxxxxx")) {
        return ARM_INS_B_A1;
    }

    if (insn_equal(insn, "xxxx1011xxxxxxxxxxxxxxxxxxxxxxxx")) {
        return ARM_INS_BLBLX_A1;
    }
    if (insn_equal(insn, "1111101xxxxxxxxxxxxxxxxxxxxxxxxx")) {
        return ARM_INS_BLBLX_A2;
    }
    return ARM_UNDEF;
}