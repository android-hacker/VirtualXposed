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

#ifndef writer_h
#define writer_h

#include "hookzz.h"

#define MAX_LITERAL_INSN_SIZE 128

typedef struct _ZzLiteralInstruction {
    zpointer literal_insn_ptr;
    zaddr *literal_address_ptr;
} ZzLiteralInstruction;

typedef struct _ZzWriter {
    zpointer codedata;
    zpointer base;
    zaddr pc;
    zsize size;

    ZzLiteralInstruction literal_insns[MAX_LITERAL_INSN_SIZE];
    zsize literal_insn_size;

} ZzWriter;

#endif
