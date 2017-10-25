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

#include "regs-arm64.h"

void zz_arm64_register_describe(ZzARM64Reg reg, ZzArm64RegInfo *ri) {
    if (reg >= ZZ_ARM64_REG_X0 && reg <= ZZ_ARM64_REG_X28) {
        ri->is_integer = TRUE;
        ri->width = 64;
        ri->meta = ZZ_ARM64_REG_X0 + (reg - ZZ_ARM64_REG_X0);
    } else if (reg == ZZ_ARM64_REG_X29 || reg == ZZ_ARM64_REG_FP) {
        ri->is_integer = TRUE;
        ri->width = 64;
        ri->meta = ZZ_ARM64_REG_X29;
    } else if (reg == ZZ_ARM64_REG_X30 || reg == ZZ_ARM64_REG_LR) {
        ri->is_integer = TRUE;
        ri->width = 64;
        ri->meta = ZZ_ARM64_REG_X30;
    } else if (reg == ZZ_ARM64_REG_SP) {
        ri->is_integer = TRUE;
        ri->width = 64;
        ri->meta = ZZ_ARM64_REG_X31;
    } else {
        Serror("zz_arm64_register_describe error.");
#if defined(DEBUG_MODE)
        debug_break();
#endif
        ri->index = 0;
    }
    ri->index = ri->meta - ZZ_ARM64_REG_X0;
}
