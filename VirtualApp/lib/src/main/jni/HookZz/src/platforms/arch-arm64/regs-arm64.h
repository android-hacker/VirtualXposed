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

#ifndef platforms_arch_arm64_regs_h
#define platforms_arch_arm64_regs_h

// platforms
#include "instructions.h"

// hookzz

// zzdeps
#include "hookzz.h"
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

typedef enum _ZzARM64Reg {
    ZZ_ARM64_REG_X0 = 0,
    ZZ_ARM64_REG_X1,
    ZZ_ARM64_REG_X2,
    ZZ_ARM64_REG_X3,
    ZZ_ARM64_REG_X4,
    ZZ_ARM64_REG_X5,
    ZZ_ARM64_REG_X6,
    ZZ_ARM64_REG_X7,
    ZZ_ARM64_REG_X8,
    ZZ_ARM64_REG_X9,
    ZZ_ARM64_REG_X10,
    ZZ_ARM64_REG_X11,
    ZZ_ARM64_REG_X12,
    ZZ_ARM64_REG_X13,
    ZZ_ARM64_REG_X14,
    ZZ_ARM64_REG_X15,
    ZZ_ARM64_REG_X16,
    ZZ_ARM64_REG_X17,
    ZZ_ARM64_REG_X18,
    ZZ_ARM64_REG_X19,
    ZZ_ARM64_REG_X20,
    ZZ_ARM64_REG_X21,
    ZZ_ARM64_REG_X22,
    ZZ_ARM64_REG_X23,
    ZZ_ARM64_REG_X24,
    ZZ_ARM64_REG_X25,
    ZZ_ARM64_REG_X26,
    ZZ_ARM64_REG_X27,
    ZZ_ARM64_REG_X28,
    ZZ_ARM64_REG_X29,
    ZZ_ARM64_REG_X30,
    ZZ_ARM64_REG_X31,
    ZZ_ARM64_REG_FP = ZZ_ARM64_REG_X29,
    ZZ_ARM64_REG_LR = ZZ_ARM64_REG_X30,
    ZZ_ARM64_REG_SP = ZZ_ARM64_REG_X31
} ZzARM64Reg;

typedef struct _ZzArm64RegInfo {
    zuint index;
    zuint meta;
    zuint width;
    zbool is_integer;
} ZzArm64RegInfo;

void zz_arm64_register_describe(ZzARM64Reg reg, ZzArm64RegInfo *ri);

#endif