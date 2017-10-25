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

#include "reader.h"

#include <string.h>

static csh handle;

void capstone_init(void)
{
    cs_err err = 0;

#if defined(__x86_64__)
    err = cs_open(CS_ARCH_X86, CS_MODE_64, &handle);
#elif defined(__arm64__)
    err = cs_open(CS_ARCH_ARM64, CS_MODE_ARM, &handle);
#endif
    if (err)
    {
        Xerror("Failed on cs_open() with error returned: %u\n", err);
        exit(-1);
    }

    cs_option(handle, CS_OPT_DETAIL, CS_OPT_ON);
}

static cs_insn *zz_arm64_reader_disassemble_at(zpointer address)
{
    if (!handle)
        capstone_init();
    cs_insn *insn;
    size_t count;
    count = cs_disasm(handle, address, 16, address, 0, &insn);
    return insn;
}

void relocator_read_one(ZzInstruction *old_ins, ZzInstruction *new_ins)
{

    // capstone ins
    cs_insn *ins_cs = zz_arm64_reader_disassemble_at(old_ins->address);

    // capstone ins detail
    // cs_detail *ins_csd = ins_cs->detail->x86;
    cs_x86 ins_csd = ins_cs->detail->x86;

    old_ins->ins_cs = ins_cs;
    old_ins->size = ins_cs->size;
    uint8_t needFix = 0;

    zpointer copy_ins_start;
    uint8_t copy_ins_size;

    // https://c9x.me/x86/html/file_module_x86_id_146.html

    /*
    ATTENTION: why 0x01 ^ cond? because of use `method_1`

    origin:
        1: je <3>
        2: push rax;
        3: push rbx;

    method_1:
        1: jne <3>
        2: jmp(abs) <4>
        3: push rax
        4: push rbx

    method_2:
        1: je <3>
        2: jmp(near) <4>
        3: jmp(abs) <5>
        4: push rax
        5: push rbx

    */
    if ((ins_csd.opcode[0] & 0xF0) == 0x70 || (ins_csd.opcode[0] & 0xFC) == 0xE0 ||
        (ins_csd.opcode[1] & 0xF0) == 0x80)
    {
        // the imm is calculate by capstone, so the imm is dest;
        zpointer dest = (zpointer)ins_csd.operands[0].imm;
        zpointer offset = (zpointer)ins_csd.operands[0].imm - old_ins->address - old_ins->size;

        zpointer new_offset = dest - new_ins->address + sizeof(JMP_ABS);

        if (dest > new_ins->address && dest < (new_ins->address + sizeof(JMP_ABS)))
        {
            zpointer internal_jmp_dest = 0;
            if (internal_jmp_dest < dest)
            {
                internal_jmp_dest = dest;
                Xerror("origin: %p, trampoline: %p is trampoline-internal-jmp !", old_ins->address, new_ins->address);
                return;
            }
        }
        else
        {
            needFix = 1;
            uint8_t cond = ((ins_csd.opcode[0] != 0x0F ? ins_csd.opcode[0] : ins_csd.opcode[2]) & 0x0F);

            jcc.opcode = 0x71 ^ cond;
            jcc.address = dest;
        }

        copy_ins_start = &jcc;
        copy_ins_size = sizeof(jcc);
    }

    if (needFix)
    {
        new_ins->size = copy_ins_size;
        memcpy(new_ins->bytes, copy_ins_start, copy_ins_size);
    }
    else
    {
        /*
            yes, we can just write to new_ins->address, according to the module of design patterns, we can't do `write` operation at here.
            memcpy(new_ins->address, old_ins->address, old_ins->size);
         */
        new_ins->size = old_ins->size;
        memcpy(new_ins->bytes, old_ins->address, old_ins->size);
    }
    memcpy(old_ins->bytes, old_ins->address, old_ins->size);
}

void relocator_invoke_trampoline(ZzTrampoline *trampoline, zpointer target, uint8_t *read_size, zpointer read_backup)
{
}
