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

#include "writer.h"
#include <string.h>

ZzInstruction *writer_put_jmp(zpointer address)
{

    JMP_ABS jmp = {
        0xFF, 0x25, 0x00000000, // FF25 00000000: JMP [RIP+6]
        0x0000000000000000ULL   // Absolute destination address
    };
    ZzInstruction *ins = (ZzInstruction *)malloc(sizeof(ZzInstruction));
    jmp.address = address;
    ins->size = sizeof(jmp);
    memcpy(ins->bytes, &jmp, sizeof(jmp));
    return ins;
}