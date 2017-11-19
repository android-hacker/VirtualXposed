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

#include "interceptor-arm64.h"
#include "zzinfo.h"
#include <stdlib.h>
#include <string.h>

#define ZZ_ARM64_TINY_REDIRECT_SIZE 4
#define ZZ_ARM64_FULL_REDIRECT_SIZE 16

ZzInterceptorBackend *ZzBuildInteceptorBackend(ZzAllocator *allocator) {
    if (!ZzMemoryIsSupportAllocateRXPage()) {
        return NULL;
    }
    ZZSTATUS status;

    ZzInterceptorBackend *backend = (ZzInterceptorBackend *)malloc(sizeof(ZzInterceptorBackend));
    memset(backend, 0, sizeof(ZzInterceptorBackend));

    zz_arm64_writer_init(&backend->arm64_writer, NULL);
    zz_arm64_relocator_init(&backend->arm64_relocator, NULL, &backend->arm64_writer);

    backend->allocator = allocator;
    backend->enter_thunk = NULL;
    backend->half_thunk = NULL;
    backend->leave_thunk = NULL;

    status = ZzThunkerBuildThunk(backend);
    if (status == ZZ_FAILED) {
        ZzInfoLog("%s", "ZzThunkerBuildThunk return ZZ_FAILED\n");
        return NULL;
    }

    return backend;
}

ZzCodeSlice *zz_code_patch_arm64_writer(ZzArm64Writer *arm64_writer, ZzAllocator *allocator, zaddr target_addr,
                                        zsize range_size) {
    ZzCodeSlice *code_slice = NULL;
    if (range_size > 0) {
        code_slice = ZzNewNearCodeSlice(allocator, target_addr, range_size, arm64_writer->size);
    } else {
        code_slice = ZzNewCodeSlice(allocator, arm64_writer->size + 4);
    }
    if (!code_slice)
        return NULL;

    if (!ZzMemoryPatchCode((zaddr)code_slice->data, arm64_writer->base, arm64_writer->size)) {
        free(code_slice);
        return NULL;
    }
    return code_slice;
}

ZzCodeSlice *zz_code_patch_arm64_relocate_writer(ZzArm64Relocator *relocator, ZzArm64Writer *arm64_writer,
                                                 ZzAllocator *allocator, zaddr target_addr, zsize range_size) {
    ZzCodeSlice *code_slice = NULL;
    if (range_size > 0) {
        code_slice = ZzNewNearCodeSlice(allocator, target_addr, range_size, arm64_writer->size);
    } else {
        code_slice = ZzNewCodeSlice(allocator, arm64_writer->size + 4);
    }
    if (!code_slice)
        return NULL;

    if (!ZzMemoryPatchCode((zaddr)code_slice->data, arm64_writer->base, arm64_writer->size)) {
        free(code_slice);
        return NULL;
    }
    return code_slice;
}

ZZSTATUS ZzPrepareTrampoline(ZzInterceptorBackend *self, ZzHookFunctionEntry *entry) {
    zaddr target_addr = (zaddr)entry->target_ptr;
    zuint redirect_limit = 0;

    ZzArm64HookFunctionEntryBackend *entry_backend;
    entry_backend = (ZzArm64HookFunctionEntryBackend *)malloc(sizeof(ZzArm64HookFunctionEntryBackend));
    memset(entry_backend, 0, sizeof(ZzArm64HookFunctionEntryBackend));

    entry->backend = (struct _ZzHookFunctionEntryBackend *)entry_backend;

    if (entry->try_near_jump) {
        entry_backend->redirect_code_size = ZZ_ARM64_TINY_REDIRECT_SIZE;
    } else {
        zz_arm64_relocator_try_relocate((zpointer)target_addr, ZZ_ARM64_FULL_REDIRECT_SIZE, &redirect_limit);
        if (redirect_limit != 0 && redirect_limit > ZZ_ARM64_TINY_REDIRECT_SIZE &&
            redirect_limit < ZZ_ARM64_FULL_REDIRECT_SIZE) {
            entry->try_near_jump = TRUE;
            entry_backend->redirect_code_size = ZZ_ARM64_TINY_REDIRECT_SIZE;
        } else if (redirect_limit != 0 && redirect_limit < ZZ_ARM64_TINY_REDIRECT_SIZE) {
            return ZZ_FAILED;
        } else {
            entry_backend->redirect_code_size = ZZ_ARM64_FULL_REDIRECT_SIZE;
        }
    }

    self->arm64_relocator.try_relocated_length = entry_backend->redirect_code_size;
    zz_arm64_relocator_init(&self->arm64_relocator, (zpointer)target_addr, &self->arm64_writer);
    return ZZ_SUCCESS;
}

ZZSTATUS ZzBuildEnterTransferTrampoline(ZzInterceptorBackend *self, ZzHookFunctionEntry *entry) {
    zbyte temp_code_slice_data[256] = {0};
    ZzArm64Writer *arm64_writer = NULL;
    ZzCodeSlice *code_slice = NULL;
    ZzArm64HookFunctionEntryBackend *entry_backend = (ZzArm64HookFunctionEntryBackend *)entry->backend;
    ZZSTATUS status = ZZ_SUCCESS;
    zaddr target_addr = (zaddr)entry->target_ptr;

    arm64_writer = &self->arm64_writer;
    zz_arm64_writer_reset(arm64_writer, temp_code_slice_data);
    zz_arm64_writer_put_ldr_br_reg_address(arm64_writer, ZZ_ARM64_REG_X17, (zaddr)entry->on_enter_trampoline);
    code_slice =
        zz_code_patch_arm64_writer(arm64_writer, self->allocator, target_addr, zz_arm64_writer_near_jump_range_size());
    if (code_slice)
        entry->on_enter_transfer_trampoline = code_slice->data;
    else
        return ZZ_FAILED;

    if (ZzIsEnableDebugMode()) {
        char buffer[1024] = {};
        sprintf(buffer + strlen(buffer), "%s\n", "ZzBuildEnterTransferTrampoline:");
        sprintf(buffer + strlen(buffer),
                "LogInfo: on_enter_transfer_trampoline at %p, length: %ld. and will jump to on_enter_trampoline(%p).\n",
                code_slice->data, code_slice->size, entry->on_enter_trampoline);
        ZzInfoLog("%s", buffer);
    }

    free(code_slice);
    return status;
}
ZZSTATUS ZzBuildEnterTrampoline(ZzInterceptorBackend *self, ZzHookFunctionEntry *entry) {
    zbyte temp_code_slice_data[256] = {0};
    ZzArm64Writer *arm64_writer = NULL;
    ZzCodeSlice *code_slice = NULL;
    ZzArm64HookFunctionEntryBackend *entry_backend = (ZzArm64HookFunctionEntryBackend *)entry->backend;
    ZZSTATUS status = ZZ_SUCCESS;
    zaddr target_addr = (zaddr)entry->target_ptr;

    arm64_writer = &self->arm64_writer;
    zz_arm64_writer_reset(arm64_writer, temp_code_slice_data);

    /* prepare 2 stack space: 1. next_hop 2. entry arg */
    zz_arm64_writer_put_sub_reg_reg_imm(arm64_writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 0x8);
    zz_arm64_writer_put_ldr_b_reg_address(arm64_writer, ZZ_ARM64_REG_X17, (zaddr)entry);
    zz_arm64_writer_put_str_reg_reg_offset(arm64_writer, ZZ_ARM64_REG_X17, ZZ_ARM64_REG_SP, 0x0);

    /* jump to enter thunk */
    zz_arm64_writer_put_ldr_br_reg_address(arm64_writer, ZZ_ARM64_REG_X17, (zaddr)self->enter_thunk);

    /* code patch */
    code_slice = zz_code_patch_arm64_writer(arm64_writer, self->allocator, 0, 0);
    if (code_slice)
        entry->on_enter_trampoline = code_slice->data;
    else
        return ZZ_FAILED;

    /* debug log */
    if (ZzIsEnableDebugMode()) {
        char buffer[1024] = {};
        sprintf(buffer + strlen(buffer), "%s\n", "ZzBuildEnterTrampoline:");
        sprintf(buffer + strlen(buffer),
                "LogInfo: on_enter_trampoline at %p, length: %ld. hook-entry: %p. and will jump to enter_thunk(%p).\n",
                code_slice->data, code_slice->size, (void *)entry, (void *)self->enter_thunk);
        ZzInfoLog("%s", buffer);
    }

    if (entry_backend->redirect_code_size == ZZ_ARM64_TINY_REDIRECT_SIZE) {
        ZzBuildEnterTransferTrampoline(self, entry);
    }

    free(code_slice);
    return status;
}

ZZSTATUS ZzBuildInvokeTrampoline(ZzInterceptorBackend *self, ZzHookFunctionEntry *entry) {
    zbyte temp_code_slice_data[256] = {0};
    ZzCodeSlice *code_slice = NULL;
    ZzArm64HookFunctionEntryBackend *entry_backend = (ZzArm64HookFunctionEntryBackend *)entry->backend;
    ZZSTATUS status = ZZ_SUCCESS;
    zaddr target_addr = (zaddr)entry->target_ptr;
    zpointer restore_target_addr;

    ZzArm64Relocator *arm64_relocator;
    ZzArm64Writer *arm64_writer;
    arm64_relocator = &self->arm64_relocator;
    arm64_writer = &self->arm64_writer;

    zz_arm64_writer_reset(arm64_writer, temp_code_slice_data);
    zz_arm64_relocator_reset(arm64_relocator, (zpointer)target_addr, arm64_writer);
    zsize tmp_relocator_insn_size = 0;
    entry->target_half_ret_addr = 0;

    if (entry->hook_type == HOOK_FUNCTION_TYPE) {
        do {
            zz_arm64_relocator_read_one(arm64_relocator, NULL);
            tmp_relocator_insn_size = arm64_relocator->input_cur - arm64_relocator->input_start;
        } while (tmp_relocator_insn_size < entry_backend->redirect_code_size);
        zz_arm64_relocator_write_all(arm64_relocator);
    } else if (entry->hook_type == HOOK_ADDRESS_TYPE) {
        do {
            zz_arm64_relocator_read_one(arm64_relocator, NULL);
            zz_arm64_relocator_write_one(arm64_relocator);
            tmp_relocator_insn_size = arm64_relocator->input_cur - arm64_relocator->input_start;
            if (arm64_relocator->input_cur >= entry->target_end_ptr && !entry->target_half_ret_addr) {
                zz_arm64_writer_put_ldr_br_reg_address(arm64_writer, ZZ_ARM64_REG_X17,
                                                       (zaddr)entry->on_half_trampoline);

                entry->target_half_ret_addr = (zpointer)arm64_writer->size;
            }
        } while (tmp_relocator_insn_size < entry_backend->redirect_code_size ||
                 arm64_relocator->input_cur < entry->target_end_ptr);
    }

    /* jump to rest target address */
    restore_target_addr = (zpointer)((zaddr)target_addr + tmp_relocator_insn_size);
    zz_arm64_writer_put_ldr_br_reg_address(arm64_writer, ZZ_ARM64_REG_X17, (zaddr)restore_target_addr);

    /* code patch */
    code_slice = zz_code_patch_arm64_relocate_writer(arm64_relocator, arm64_writer, self->allocator, 0, 0);
    if (code_slice)
        entry->on_invoke_trampoline = code_slice->data;
    else
        return ZZ_FAILED;

    /* update target_half_ret_addr */
    if (entry->hook_type == HOOK_ADDRESS_TYPE) {
        entry->target_half_ret_addr += (zaddr)code_slice->data;
    }

    /* debug log */
    if (ZzIsEnableDebugMode()) {
        char buffer[1024] = {0};
        sprintf(buffer + strlen(buffer), "%s\n", "ZzBuildInvokeTrampoline:");
        sprintf(buffer + strlen(buffer),
                "LogInfo: on_invoke_trampoline at %p, length: %ld. and will jump to rest code(%p).\n", code_slice->data,
                code_slice->size, restore_target_addr);
        sprintf(buffer + strlen(buffer),
                "ArmInstructionFix: origin instruction at %p, relocator end at %p, relocator instruction nums %ld\n",
                (&self->arm64_relocator)->input_start, (&self->arm64_relocator)->input_cur,
                (&self->arm64_relocator)->inpos);

        char origin_prologue[256] = {0};
        int t = 0;
        zpointer p;
        for (p = (&self->arm64_relocator)->input_start; p < (&self->arm64_relocator)->input_cur; p++, t = t + 5) {
            sprintf(origin_prologue + t, "0x%.2x ", *(unsigned char *)p);
        }
        sprintf(buffer + strlen(buffer), "origin_prologue: %s\n", origin_prologue);

        ZzInfoLog("%s", buffer);
    }

    free(code_slice);
    return status;
}

ZZSTATUS ZzBuildHalfTrampoline(ZzInterceptorBackend *self, ZzHookFunctionEntry *entry) {
    zbyte temp_code_slice_data[256] = {0};
    ZzArm64Writer *arm64_writer = NULL;
    ZzCodeSlice *code_slice = NULL;
    ZzArm64HookFunctionEntryBackend *entry_backend = (ZzArm64HookFunctionEntryBackend *)entry->backend;
    ZZSTATUS status = ZZ_SUCCESS;
    zaddr target_addr = (zaddr)entry->target_ptr;

    arm64_writer = &self->arm64_writer;
    zz_arm64_writer_reset(arm64_writer, temp_code_slice_data);

    /* prepare 2 stack space: 1. next_hop 2. entry arg */
    zz_arm64_writer_put_sub_reg_reg_imm(arm64_writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 0x8);
    zz_arm64_writer_put_ldr_b_reg_address(arm64_writer, ZZ_ARM64_REG_X17, (zaddr)entry);
    zz_arm64_writer_put_str_reg_reg_offset(arm64_writer, ZZ_ARM64_REG_X17, ZZ_ARM64_REG_SP, 0x0);

    /* jump to half thunk */
    zz_arm64_writer_put_ldr_br_reg_address(arm64_writer, ZZ_ARM64_REG_X17, (zaddr)self->half_thunk);

    /* code patch */
    code_slice = zz_code_patch_arm64_writer(arm64_writer, self->allocator, 0, 0);
    if (code_slice)
        entry->on_half_trampoline = code_slice->data;
    else
        return ZZ_FAILED;

    return status;
}

ZZSTATUS ZzBuildLeaveTrampoline(ZzInterceptorBackend *self, ZzHookFunctionEntry *entry) {
    zbyte temp_code_slice_data[256] = {0};
    ZzCodeSlice *code_slice = NULL;
    ZzArm64HookFunctionEntryBackend *entry_backend = (ZzArm64HookFunctionEntryBackend *)entry->backend;
    zaddr target_addr = (zaddr)entry->target_ptr;
    ZzArm64Writer *arm64_writer = NULL;

    arm64_writer = &self->arm64_writer;
    zz_arm64_writer_reset(arm64_writer, temp_code_slice_data);

    /* prepare 2 stack space: 1. next_hop 2. entry arg */
    zz_arm64_writer_put_sub_reg_reg_imm(arm64_writer, ZZ_ARM64_REG_SP, ZZ_ARM64_REG_SP, 2 * 0x8);
    zz_arm64_writer_put_ldr_b_reg_address(arm64_writer, ZZ_ARM64_REG_X17, (zaddr)entry);
    zz_arm64_writer_put_str_reg_reg_offset(arm64_writer, ZZ_ARM64_REG_X17, ZZ_ARM64_REG_SP, 0x0);

    /* jump to leave thunk */
    zz_arm64_writer_put_ldr_br_reg_address(arm64_writer, ZZ_ARM64_REG_X17, (zaddr)self->leave_thunk);

    /* code patch */
    code_slice = zz_code_patch_arm64_writer(arm64_writer, self->allocator, 0, 0);
    if (code_slice)
        entry->on_leave_trampoline = code_slice->data;
    else
        return ZZ_FAILED;

    /* debug log */
    if (ZzIsEnableDebugMode()) {
        char buffer[1024] = {};
        sprintf(buffer + strlen(buffer), "%s\n", "ZzBuildLeaveTrampoline:");
        sprintf(buffer + strlen(buffer),
                "LogInfo: on_leave_trampoline at %p, length: %ld. and will jump to leave_thunk(%p).\n",
                code_slice->data, code_slice->size, self->leave_thunk);
        ZzInfoLog("%s", buffer);
    }

    free(code_slice);
    return ZZ_DONE;
}

ZZSTATUS ZzActivateTrampoline(ZzInterceptorBackend *self, ZzHookFunctionEntry *entry) {
    zbyte temp_code_slice_data[256] = {0};
    ZzCodeSlice *code_slice = NULL;
    ZzArm64HookFunctionEntryBackend *entry_backend = (ZzArm64HookFunctionEntryBackend *)entry->backend;
    ZZSTATUS status = ZZ_SUCCESS;
    zaddr target_addr = (zaddr)entry->target_ptr;
    ZzArm64Writer *arm64_writer;

    arm64_writer = &self->arm64_writer;
    zz_arm64_writer_reset(arm64_writer, temp_code_slice_data);
    arm64_writer->pc = target_addr;

    if (entry_backend->redirect_code_size == ZZ_ARM64_TINY_REDIRECT_SIZE) {
        zz_arm64_writer_put_b_imm(arm64_writer, (zaddr)entry->on_enter_transfer_trampoline - (zaddr)arm64_writer->pc);
    } else {
        zz_arm64_writer_put_ldr_br_reg_address(arm64_writer, ZZ_ARM64_REG_X17, (zaddr)entry->on_enter_trampoline);
    }

    if (!ZzMemoryPatchCode((zaddr)target_addr, arm64_writer->base, arm64_writer->size))
        status = ZZ_FAILED;

    return status;
}

#ifdef TARGET_IS_IOS

#include <mach-o/dyld.h>
#include <zzdeps/darwin/macho-utils-darwin.h>

typedef struct _ZzInterceptorBackendNoJB {
    void *enter_thunk; // hardcode
    void *leave_thunk; // hardcode
    unsigned long num_of_entry;
    unsigned long code_seg_offset;
    unsigned long data_seg_offset;
} ZzInterceptorBackendNoJB;

typedef struct _ZzHookFunctionEntryNoJB {
    void *target_fileoff;
    unsigned long is_near_jump;
    void *entry_address;
    void *on_enter_trampoline;  // HookZzData, 99% hardcode
    void *on_invoke_trampoline; // HookZzData, fixed instructions
    void *on_leave_trampoline;  // HookZzData, 99% hardcode
} ZzHookFunctionEntryNoJB;

ZZSTATUS ZzActivateSolidifyTrampoline(ZzHookFunctionEntry *entry, zaddr target_fileoff) {
    struct mach_header_64 *header = (struct mach_header_64 *)_dyld_get_image_header(0);
    struct segment_command_64 *text_seg_cmd = zz_macho_get_segment_64_via_name(header, "__TEXT");
    struct segment_command_64 *data_seg_cmd = zz_macho_get_segment_64_via_name(header, "HookZzData");
    zaddr aslr_slide = (zaddr)header - text_seg_cmd->vmaddr;
    ZzInterceptorBackendNoJB *nojb_backend = (ZzInterceptorBackendNoJB *)(aslr_slide + data_seg_cmd->vmaddr);
    nojb_backend->enter_thunk = (void *)enter_thunk_template;
    nojb_backend->leave_thunk = (void *)leave_thunk_template;

    ZzHookFunctionEntryNoJB *nojb_entry =
        (ZzHookFunctionEntryNoJB *)(data_seg_cmd->vmaddr + sizeof(ZzHookFunctionEntryNoJB) + aslr_slide);
    unsigned long i;
    for (i = 0; i < nojb_backend->num_of_entry; i++) {
        nojb_entry = &nojb_entry[i];
        if ((zaddr)nojb_entry->target_fileoff == target_fileoff) {
            nojb_entry->entry_address = entry;
            entry->on_enter_trampoline = (zpointer)(nojb_entry->on_enter_trampoline + aslr_slide);
            entry->on_invoke_trampoline = nojb_entry->on_invoke_trampoline + aslr_slide;
            entry->on_leave_trampoline = nojb_entry->on_leave_trampoline + aslr_slide;
        }
    }
    return ZZ_SUCCESS;
}
#endif
