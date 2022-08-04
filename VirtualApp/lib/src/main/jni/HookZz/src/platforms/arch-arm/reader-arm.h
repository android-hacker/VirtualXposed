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

#ifndef platforms_arch_arm_reader_arm_h
#define platforms_arch_arm_reader_arm_h

// platforms
#include "instructions.h"

// hookzz

// zzdeps
#include "hookzz.h"
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

typedef enum _ARMInsnType {
    ARM_INS_ADD_register_A1,
    ARM_INS_LDR_literal_A1,
    ARM_INS_ADR_A1,
    ARM_INS_ADR_A2,
    ARM_INS_B_A1,
    ARM_INS_BLBLX_immediate_A1,
    ARM_INS_BLBLX_immediate_A2,
    ARM_UNDEF
} ARMInsnType;

ARMInsnType GetARMInsnType(zuint32 insn);
zpointer zz_arm_reader_read_one_instruction(ZzInstruction *insn_ctx, zpointer address);

#endif