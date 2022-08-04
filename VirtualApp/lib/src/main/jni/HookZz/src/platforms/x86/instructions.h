//    Copyright 2017 jmpews
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.

#ifndef instructions_h
#define instructions_h

#include "../../../include/hookzz.h"
#include "../../../include/zz.h"
#include "capstone.h"

// Structs for writing x86/x64 instructions.

// 8-bit relative jump.
typedef struct _JMP_REL_SHORT {
    uint8_t opcode; // EB xx: JMP +2+xx
    uint8_t operand;
} __attribute__((packed)) JMP_REL_SHORT, *PJMP_REL_SHORT;

// 32-bit direct relative jump/call.
typedef struct _JMP_REL {
    uint8_t opcode;  // E9/E8 xxxxxxxx: JMP/CALL +5+xxxxxxxx
    zuint32 operand; // Relative destination address
} __attribute__((packed)) JMP_REL, *PJMP_REL, CALL_REL;

// 64-bit indirect absolute jump.
typedef struct _JMP_ABS {
    uint8_t opcode0; // FF25 00000000: JMP [+6]
    uint8_t opcode1;
    zuint32 dummy;
    uint64_t address; // Absolute destination address
} __attribute__((packed)) JMP_ABS, *PJMP_ABS;

// 64-bit indirect absolute call.
typedef struct _CALL_ABS {
    uint8_t opcode0; // FF15 00000002: CALL [+6]
    uint8_t opcode1;
    zuint32 dummy0;
    uint8_t dummy1; // EB 08:         JMP +10
    uint8_t dummy2;
    uint64_t address; // Absolute destination address
} __attribute__((packed)) CALL_ABS;

// 32-bit direct relative conditional jumps.
typedef struct _JCC_REL {
    uint8_t opcode0; // 0F8* xxxxxxxx: J** +6+xxxxxxxx
    uint8_t opcode1;
    zuint32 operand; // Relative destination address
} __attribute__((packed)) JCC_REL;

/* must understand this, by jmpews */
// 64bit indirect absolute conditional jumps that x64 lacks.
typedef struct _JCC_ABS {
    /*
        TODO:
        need prefix ? like `uint8_t prefix;`
     */
    uint8_t opcode; // 7* 0E:         J** +16
    uint8_t dummy0;
    uint8_t dummy1; // FF25 00000000: JMP [+6]
    uint8_t dummy2;
    zuint32 dummy3;
    uint64_t address; // Absolute destination address
} __attribute__((packed)) JCC_ABS;

typedef struct _Instruction {
    zpointer address;
    cs_insn *ins_cs;
    uint8_t size;
    zbyte bytes[16];
} ZzInstruction;

// not use!!!
typedef struct _RelocatedInstruction {
    ZzInstruction old_ins;
    ZzInstruction new_ins;
} RelocatedInstruction;

// not use!!!
typedef struct _RelocatedTrampoline {
    zpointer old_target;
    zpointer new_target;

    uint8_t old_size;
    uint8_t new_size;

    ZzInstruction old_inss[16];
    ZzInstruction new_inss[16];
} RelocatedTrampoline;

#endif