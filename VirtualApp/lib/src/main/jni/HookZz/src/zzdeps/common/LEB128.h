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

/*
 *  refs: https://github.com/aquynh/capstone/blob/master/LEB128.h
 *  refs: http://llvm.org/docs/doxygen/html/LEB128_8h_source.html
 *  refs: ld64-274.2/src/other/dyldinfo.cpp:1760
 */

#if !defined(_MSC_VER) || !defined(_KERNEL_MODE)

#include <stdint.h>

#endif

/// Utility function to decode a ULEB128 value.
static inline uint64_t decodeULEB128(const uint8_t *p, unsigned *n) {
    const uint8_t *orig_p = p;
    uint64_t Value = 0;
    unsigned Shift = 0;
    do {
        Value += (*p & 0x7f) << Shift;
        Shift += 7;
    } while (*p++ >= 128);
    if (n)
        *n = (unsigned)(p - orig_p);
    return Value;
}