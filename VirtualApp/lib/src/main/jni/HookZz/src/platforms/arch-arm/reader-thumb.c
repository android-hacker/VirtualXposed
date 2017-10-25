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

#include "reader-thumb.h"

// static csh handle;

// void zz_thumb_reader_capstone_init(void) {
//     cs_err err = 0;

//     err = cs_open(CS_ARCH_ARM, CS_MODE_THUMB, &handle);
//     if (err) {
//         Xerror("Failed on cs_open() with error returned: %u\n", err);
//         exit(-1);
//     }

//     cs_option(handle, CS_OPT_DETAIL, CS_OPT_ON);
// }

// cs_insn *zz_thumb_reader_disassemble_at(zpointer address) {
//     if (!handle)
//         zz_thumb_reader_capstone_init();
//     cs_insn *insn;
//     size_t count;
//     count = cs_disasm(handle, address, 16, (unsigned long)address, 0, &insn);
//     if (!insn) {
// #if defined(DEBUG_MODE)
//         debug_break();
// #endif
//         Xerror("zz_thumb_reader_disassemble_at error at %p", (zpointer)address);
//     }
//     return insn;
// }

zbool insn_is_thumb2(zuint32 insn) {
    // PAGE: A6-221
    // PAGE: A6-230
    if (get_insn_sub(insn & 0x0000FFFF, 11, 2) == 0) {
        return FALSE;
    }
    return TRUE;

    if (insn_equal(insn & 0x0000FFFF, "11101xxxxxxxxxxx") || insn_equal(insn & 0x0000FFFF, "11110xxxxxxxxxxx") ||
        insn_equal(insn & 0x0000FFFF, "11110xxxxxxxxxxx")) {
        return TRUE;
    } else {
        return FALSE;
    }
}

zpointer zz_thumb_reader_read_one_instruction(ZzInstruction *insn_ctx, zpointer address) {
    // ZzInstruction *insn_ctx = (ZzInstruction *)malloc(sizeof(ZzInstruction));
    insn_ctx->pc = (zaddr)address;
    insn_ctx->insn = *(zuint32 *)address;

    // PAGE: A6-221
    if (insn_is_thumb2(insn_ctx->insn)) {
        insn_ctx->type = THUMB2_INSN;
        insn_ctx->size = 4;
        insn_ctx->insn1 = insn_ctx->insn & 0x0000FFFF;
        insn_ctx->insn2 = (insn_ctx->insn & 0xFFFF0000) >> 16;
    } else {
        insn_ctx->type = THUMB_INSN;
        insn_ctx->size = 2;
        insn_ctx->insn1 = insn_ctx->insn & 0x0000FFFF;
        insn_ctx->insn2 = 0;
    }
    return (zpointer)insn_ctx->pc;
}

// ARM Manual
// A5 ARM Instruction Set Encoding
// A5.3 Load/store word and unsigned byte
THUMBInsnType GetTHUMBInsnType(zuint32 insn) {
    // zuint32 insn = insn_ctx->insn;
    zuint32 op, op1;

    if (!insn_is_thumb2(insn) && insn_equal(insn, "01001xxxxxxxxxxx")) {
        return THUMB_INS_LDR_T1;
    }

    if (insn_is_thumb2(insn) && insn_equal(insn, "11111000x1011111xxxxxxxxxxxxxxxx")) {
        return THUMB_INS_LDR_T2;
    }

    if (!insn_is_thumb2(insn) && insn_equal(insn, "10100xxxxxxxxxxx")) {
        return THUMB_INS_ADR_T1;
    }

    if (insn_is_thumb2(insn) && insn_equal(insn, "11110x10101011110xxxxxxxxxxxxxxx")) {
        return THUMB_INS_ADR_T2;
    }

    if (insn_is_thumb2(insn) && insn_equal(insn, "11110x10000011110xxxxxxxxxxxxxxx")) {
        return THUMB_INS_ADR_T3;
    }

    if (!insn_is_thumb2(insn) && insn_equal(insn, "1101xxxxxxxxxxxx")) {
        return THUMB_INS_B_T1;
    }

    if (!insn_is_thumb2(insn) && insn_equal(insn, "11100xxxxxxxxxxx")) {
        return THUMB_INS_B_T2;
    }

    if (insn_is_thumb2(insn) && insn_equal(insn, "11110xxxxxxxxxxx10x0xxxxxxxxxxxx")) {
        return THUMB_INS_B_T3;
    }

    if (insn_is_thumb2(insn) && insn_equal(insn, "11110xxxxxxxxxxx10x0xxxxxxxxxxxx")) {
        return THUMB_INS_B_T4;
    }

    if (insn_is_thumb2(insn) && insn_equal(insn, "11110xxxxxxxxxxx11x1xxxxxxxxxxxx")) {
        return THUMB_INS_BLBLX_T1;
    }

    if (insn_is_thumb2(insn) && insn_equal(insn, "11110xxxxxxxxxxx11x0xxxxxxxxxxxx")) {
        return THUMB_INS_BLBLX_T2;
    }

    return THUMB_UNDEF;
}