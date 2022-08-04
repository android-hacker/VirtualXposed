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

#ifndef platforms_arch_arm_reader_thumb_h
#define platforms_arch_arm_reader_thumb_h

// platforms
#include "instructions.h"

// hookzz

// zzdeps
#include "hookzz.h"
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

typedef enum _THUMBInsnType {
    THUMB_INS_CBNZ_CBZ,
    THUMB_INS_ADD_register_T2,
    THUMB_INS_LDR_literal_T1,
    THUMB_INS_LDR_literal_T2,
    THUMB_INS_ADR_T1,
    THUMB_INS_ADR_T2,
    THUMB_INS_ADR_T3,
    THUMB_INS_B_T1,
    THUMB_INS_B_T2,
    THUMB_INS_B_T3,
    THUMB_INS_B_T4,
    THUMB_INS_BLBLX_immediate_T1,
    THUMB_INS_BLBLX_immediate_T2,
    THUMB_UNDEF
} THUMBInsnType;

THUMBInsnType GetTHUMBInsnType(zuint16 insn1, zuint16 insn2);
zpointer zz_thumb_reader_read_one_instruction(ZzInstruction *insn_ctx, zpointer address);

#endif