#include "hookzz.h"
#include <stdio.h>
#include <unistd.h>

static void thumb_insn_need_fix() {
    __asm__ volatile(".code 16\n"

                     "add r0, pc\n"

                     "ldr r0, [pc, #8]\n"
                     "ldr.W r0, [pc, #8]\n"

                     "adr r0, #8\n"
                     "adr.W r0, #8\n"
                     "adr.W r0, #-8\n"

                     "beq #8\n"
                     "b #8\n"
                     "beq.W #8\n"
                     "b.W #8\n"

                     "bl #8\n"
                     "blx #8\n"
                     "nop");
}

#include "platforms/backend-arm64/interceptor-arm64.h"
#include <stdlib.h>

#if 1
__attribute__((constructor)) void test_insn_fix_thumb() {

    ZzInterceptorBackend *backend = (ZzInterceptorBackend *)malloc(sizeof(ZzInterceptorBackend));
    zbyte temp_code_slice_data[256] = {0};

    zz_arm_writer_init(&backend->arm_writer, NULL);
    zz_arm_relocator_init(&backend->arm_relocator, NULL, &backend->arm_writer);
    zz_thumb_writer_init(&backend->thumb_writer, NULL);
    zz_thumb_relocator_init(&backend->thumb_relocator, NULL, &backend->thumb_writer);

    ZzThumbRelocator *thumb_relocator;
    ZzThumbWriter *thumb_writer;
    thumb_relocator = &backend->thumb_relocator;
    thumb_writer = &backend->thumb_writer;

    zz_thumb_writer_reset(thumb_writer, temp_code_slice_data);

    zz_thumb_relocator_reset(thumb_relocator, (zpointer)((zaddr)thumb_insn_need_fix & ~(zaddr)1), thumb_writer);
    zsize tmp_relocator_insn_size = 0;

    do {
        zz_thumb_relocator_read_one(thumb_relocator, NULL);
        zz_thumb_relocator_write_one(thumb_relocator);
        tmp_relocator_insn_size = thumb_relocator->input_cur - thumb_relocator->input_start;
    } while (tmp_relocator_insn_size < 36);
}
#endif

#if 0
__attribute__((__naked__)) void arm_insn_need_fix() {
    __asm__ volatile(".arm\n"
                     "add r0, pc, r0\n"

                     "ldr r0, [pc, #8]\n"

                     "adr r0, #8\n"
                     "adr r0, #-8\n"

                     "beq #8\n"
                     "b #8\n"

                     "bl #8\n"
                     "blx #8\n"
                     "nop");
}

#include "platforms/backend-arm/interceptor-arm.h"
#include <stdlib.h>

__attribute__((constructor)) void test_insn_fix_arm() {

    ZzInterceptorBackend *backend = (ZzInterceptorBackend *)malloc(sizeof(ZzInterceptorBackend));
    zbyte temp_code_slice_data[256] = {0};

    zz_arm_writer_init(&backend->arm_writer, NULL);
    zz_arm_relocator_init(&backend->arm_relocator, NULL, &backend->arm_writer);
    zz_thumb_writer_init(&backend->thumb_writer, NULL);
    zz_thumb_relocator_init(&backend->thumb_relocator, NULL, &backend->thumb_writer);

    ZzArmRelocator *arm_relocator;
    ZzArmWriter *arm_writer;
    arm_relocator = &backend->arm_relocator;
    arm_writer = &backend->arm_writer;

    zz_arm_writer_reset(arm_writer, temp_code_slice_data);

    zz_arm_relocator_reset(arm_relocator, (zpointer)((zaddr)arm_insn_need_fix & ~(zaddr)1), arm_writer);
    zsize tmp_relocator_insn_size = 0;

    do {
        zz_arm_relocator_read_one(arm_relocator, NULL);
        zz_arm_relocator_write_one(arm_relocator);
        tmp_relocator_insn_size = arm_relocator->input_cur - arm_relocator->input_start;
    } while (tmp_relocator_insn_size < 36);
}
#endif