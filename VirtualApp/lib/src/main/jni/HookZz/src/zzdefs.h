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

#include <stdint.h> // for: uint64_t
/*
    -ARM64
    http://infocenter.arm.com/help/topic/com.arm.doc.den0024a/DEN0024A_v8_architecture_PG.pdf (7.2.1
   Floating-point) (4.6.1 Floating-point register organization in AArch64) use struct and union to
   describe diagram in the above link, nice!

    -X86
    https://en.wikipedia.org/wiki/X86_calling_conventions
*/

#define ZZ_INT5_MASK 0x0000001f
#define ZZ_INT8_MASK 0x000000ff
#define ZZ_INT10_MASK 0x000003ff
#define ZZ_INT11_MASK 0x000007ff
#define ZZ_INT12_MASK 0x00000fff
#define ZZ_INT14_MASK 0x00003fff
#define ZZ_INT16_MASK 0x0000ffff
#define ZZ_INT18_MASK 0x0003ffff
#define ZZ_INT19_MASK 0x0007ffff
#define ZZ_INT24_MASK 0x00ffffff
#define ZZ_INT26_MASK 0x03ffffff
#define ZZ_INT28_MASK 0x0fffffff

#define THUMB_FUNCTION_ADDRESS(target_addr) (void *)((unsigned long)target_addr & ~(unsigned long)1)
#define INSTRUCTION_IS_THUMB(insn_addr) ((insn_addr & 0x1) == 0x1)
#define ALIGN_4(target_addr) ((unsigned long)target_addr & ~(unsigned long)3)