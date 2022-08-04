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

#ifndef platforms_arch_arm64_instructions_h
#define platforms_arch_arm64_instructions_h

#include "hookzz.h"

typedef struct _ZzInstruction {
    zaddr pc;
    zaddr address;
    uint8_t size;
    zuint32 insn;
} ZzInstruction;

typedef struct _ZzRelocateInstruction {
    const ZzInstruction *insn_ctx;
    zaddr relocated_offset;
    zsize relocated_length;
} ZzRelocateInstruction;

zuint32 get_insn_sub(zuint32 insn, int start, int length);
zbool insn_equal(zuint32 insn, char *opstr);

#endif