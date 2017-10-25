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

#ifndef platforms_arch_arm_regs_h
#define platforms_arch_arm_regs_h

// platforms
#include "instructions.h"

// hookzz

// zzdeps
#include "hookzz.h"
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

// REF:
// ARM Architecture Reference Manual
// A2.4 Registers

typedef enum _ZzReg {
    ZZ_ARM_REG_R0 = 0,
    ZZ_ARM_REG_R1,
    ZZ_ARM_REG_R2,
    ZZ_ARM_REG_R3,
    ZZ_ARM_REG_R4,
    ZZ_ARM_REG_R5,
    ZZ_ARM_REG_R6,
    ZZ_ARM_REG_R7,
    ZZ_ARM_REG_R8,
    ZZ_ARM_REG_R9,
    ZZ_ARM_REG_R10,
    ZZ_ARM_REG_R11,
    ZZ_ARM_REG_R12,
    ZZ_ARM_REG_R13,
    ZZ_ARM_REG_R14,
    ZZ_ARM_REG_R15,
    ZZ_ARM_REG_SP = ZZ_ARM_REG_R13,
    ZZ_ARM_REG_LR = ZZ_ARM_REG_R14,
    ZZ_ARM_REG_PC = ZZ_ARM_REG_R15
} ZzARMReg;

typedef struct _ZzArmRegInfo {
    zuint index;
    zuint meta;
    zuint width;
} ZzArmRegInfo;

void zz_arm_register_describe(ZzARMReg reg, ZzArmRegInfo *ri);

#endif