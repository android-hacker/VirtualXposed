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

#include <string.h>
#include <stdlib.h>

#include "writer-x86.h"

ZzX86Writer *zz_x86_writer_new(zpointer data_ptr) {
    return NULL;
}

void zz_x86_writer_init(ZzX86Writer *self, zpointer target_addr) { zz_x86_writer_reset(self, target_addr); }

void zz_x86_writer_reset(ZzX86Writer *self, zpointer data_ptr) {
}

zsize zz_x86_writer_near_jump_range_size() { return 0; }


void zz_x86_writer_put_bytes(ZzWriter *self, zbyte *data, zsize size) {

}

void zz_x86_writer_put_instruction(ZzWriter *self, zuint32 insn) {

}


// ======= relocator =======

// ======= user custom =======

// ======= default =======
