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

#include "reader-arm64.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

// static csh handle;

// void zz_arm64_reader_capstone_init(void) {
//     cs_err err = 0;

//     err = cs_open(CS_ARCH_ARM6464, CS_MODE_ARM64, &handle);
//     if (err) {
//         Xerror("Failed on cs_open() with error returned: %u\n", err);
//         exit(-1);
//     }

//     cs_option(handle, CS_OPT_DETAIL, CS_OPT_ON);
// }

// cs_insn *zz_arm64_reader_disassemble_at(zpointer address) {
//     if (!handle)
//         zz_arm64_reader_capstone_init();
//     cs_insn *insn;
//     size_t count;
//     count = cs_disasm(handle, address, 16, (unsigned long)address, 0, &insn);
//     if (!insn) {
// #if defined(DEBUG_MODE)
//         debug_break();
// #endif
//         Xerror("zz_arm64_reader_disassemble_at error at %p", (zpointer)address);
//     }
//     return insn;
// }

zpointer zz_arm64_reader_read_one_instruction(ZzInstruction *insn_ctx, zpointer address) {
    // ZzInstruction *insn = (ZzInstruction *)malloc(sizeof(ZzInstruction));

    insn_ctx->address = (zaddr)address;
    insn_ctx->size = 4;
    insn_ctx->pc = (zaddr)address;
    insn_ctx->insn = *(zuint32 *)address;
    return (zpointer)insn_ctx->pc;
}

ARM64InsnType GetARM64InsnType(zuint32 insn) {
    zuint32 op, op1;
    op1 = get_insn_sub(insn, 20, 5);

    // PAGE: C6-673
    if (insn_equal(insn, "01011000xxxxxxxxxxxxxxxxxxxxxxxx")) {
        return ARM64_INS_LDR_literal;
    }

    // PAGE: C6-535
    if (insn_equal(insn, "0xx10000xxxxxxxxxxxxxxxxxxxxxxxx")) {
        return ARM64_INS_ADR;
    }

    // PAGE: C6-536
    if (insn_equal(insn, "1xx10000xxxxxxxxxxxxxxxxxxxxxxxx")) {
        return ARM64_INS_ADRP;
    }

    // PAGE: C6-550
    if (insn_equal(insn, "000101xxxxxxxxxxxxxxxxxxxxxxxxxx")) {
        return ARM64_INS_B;
    }

    // PAGE: C6-560
    if (insn_equal(insn, "100101xxxxxxxxxxxxxxxxxxxxxxxxxx")) {
        return ARM64_INS_BL;
    }

    // PAGE: C6-549
    if (insn_equal(insn, "01010100xxxxxxxxxxxxxxxxxxx0xxxx")) {
        return ARM64_INS_B_cond;
    }

    return ARM64_UNDEF;
}