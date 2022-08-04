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

#ifndef platforms_arch_arm_instructions_h
#define platforms_arch_arm_instructions_h

#include "hookzz.h"

typedef enum _INSN_TYPE { ARM_INSN, THUMB_INSN, THUMB2_INSN } InsnType;

typedef struct _Instruction {
    InsnType type;
    zaddr pc;
    zaddr address;
    zuint8 size;
    union {
        zuint32 trick_insn;
        struct {
            zuint16 trick_insn1;
            zuint16 trick_insn2;
        };
    };

    zuint32 insn;
    zuint16 insn1;
    zuint16 insn2;
} ZzInstruction;

typedef struct _ZzRelocateInstruction {
    const ZzInstruction *insn_ctx;
    zaddr relocated_offset;
    zsize relocated_length;
} ZzRelocateInstruction;

zuint32 get_insn_sub(zuint32 insn, int start, int length);
zbool insn_equal(zuint32 insn, char *opstr);

#endif