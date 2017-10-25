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

#ifndef platforms_arch_arm64_reader_h
#define platforms_arch_arm64_reader_h

// platforms
#include "instructions.h"

// hookzz

// zzdeps
#include "hookzz.h"
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

typedef enum _ARM64InsnType {
    ARM64_INS_LDR_literal,
    ARM64_INS_ADR,
    ARM64_INS_ADRP,
    ARM64_INS_B,
    ARM64_INS_BL,
    ARM64_INS_B_cond,
    ARM64_UNDEF
} ARM64InsnType;

ARM64InsnType GetARM64InsnType(zuint32 insn);
zpointer zz_arm64_reader_read_one_instruction(ZzInstruction *insn_ctx, zpointer address);

#endif