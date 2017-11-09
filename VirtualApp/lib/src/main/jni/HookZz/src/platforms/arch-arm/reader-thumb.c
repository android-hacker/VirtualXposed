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

zbool insn_is_thumb2(zuint32 insn) {
    // PAGE: A6-221
    // PAGE: A6-230

    if (insn_equal(insn & 0x0000FFFF, "11101xxxxxxxxxxx") || insn_equal(insn & 0x0000FFFF, "11111xxxxxxxxxxx") ||
        insn_equal(insn & 0x0000FFFF, "11110xxxxxxxxxxx")) {
        return TRUE;
    } else {
        return FALSE;
    }
}

zpointer zz_thumb_reader_read_one_instruction(ZzInstruction *insn_ctx, zpointer address) {
    // ZzInstruction *insn_ctx = (ZzInstruction *)malloc(sizeof(ZzInstruction));
    insn_ctx->pc = (zaddr)address + 4;
    insn_ctx->address = (zaddr)address;
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
THUMBInsnType GetTHUMBInsnType(zuint16 insn1, zuint16 insn2) {

    if (!insn_is_thumb2(insn1) && insn_equal(insn1, "1011x0x1xxxxxxxx")) {
        return THUMB_INS_CBNZ_CBZ;
    }

    if (!insn_is_thumb2(insn1) && insn_equal(insn1, "01000100xxxxxxxx")) {
        return THUMB_INS_ADD_register_T2;
    }

    if (!insn_is_thumb2(insn1) && insn_equal(insn1, "01001xxxxxxxxxxx")) {
        return THUMB_INS_LDR_literal_T1;
    }

    if (insn_is_thumb2(insn1) && insn_equal(insn1, "11111000x1011111") && insn_equal(insn2, "xxxxxxxxxxxxxxxx")) {
        return THUMB_INS_LDR_literal_T2;
    }

    if (!insn_is_thumb2(insn1) && insn_equal(insn1, "10100xxxxxxxxxxx")) {
        return THUMB_INS_ADR_T1;
    }

    if (insn_is_thumb2(insn1) && insn_equal(insn1, "11110x1010101111") && insn_equal(insn2, "0xxxxxxxxxxxxxxx")) {
        return THUMB_INS_ADR_T2;
    }

    if (insn_is_thumb2(insn1) && insn_equal(insn1, "11110x1000001111") && insn_equal(insn2, "0xxxxxxxxxxxxxxx")) {
        return THUMB_INS_ADR_T3;
    }

    if (!insn_is_thumb2(insn1) && insn_equal(insn1, "1101xxxxxxxxxxxx")) {
        return THUMB_INS_B_T1;
    }

    if (!insn_is_thumb2(insn1) && insn_equal(insn1, "11100xxxxxxxxxxx")) {
        return THUMB_INS_B_T2;
    }

    if (insn_is_thumb2(insn1) && insn_equal(insn1, "11110xxxxxxxxxxx") && insn_equal(insn2, "10x0xxxxxxxxxxxx")) {
        return THUMB_INS_B_T3;
    }

    if (insn_is_thumb2(insn1) && insn_equal(insn1, "11110xxxxxxxxxxx") && insn_equal(insn2, "10x1xxxxxxxxxxxx")) {
        return THUMB_INS_B_T4;
    }

    if (insn_is_thumb2(insn1) && insn_equal(insn1, "11110xxxxxxxxxxx") && insn_equal(insn2, "11x1xxxxxxxxxxxx")) {
        return THUMB_INS_BLBLX_immediate_T1;
    }

    if (insn_is_thumb2(insn1) && insn_equal(insn1, "11110xxxxxxxxxxx") && insn_equal(insn2, "11x0xxxxxxxxxxxx")) {
        return THUMB_INS_BLBLX_immediate_T2;
    }

    return THUMB_UNDEF;
}