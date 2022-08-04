
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

#include <errno.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>

#include "memory-utils-posix.h"
// http://renatocunha.com/blog/2015/12/msync-pointer-validity/
zbool zz_posix_vm_check_address_valid_via_msync(const zpointer p) {
    int ret = 0;
    zsize page_size;
    zpointer base;
    /* get the page size */
    page_size = zz_posix_vm_get_page_size();
    /* find the address of the page that contains p */
    base = (void *)((((size_t)p) / page_size) * page_size);
    /* call msync, if it returns non-zero, return FALSE */
    ret = msync(base, page_size, MS_ASYNC) != -1;
    return ret ? ret : errno != ENOMEM;
}

// ATTENTION !!!
// lldb is still catch EXC_BAD_ACCESS, without lldb is ok.
// https://www.cocoawithlove.com/2010/10/testing-if-arbitrary-pointer-is-valid.html
// https://stackoverflow.com/questions/26829119/how-to-make-lldb-ignore-exc-bad-access-exception
// ---check start---
#include <setjmp.h>
#include <signal.h>

static sigjmp_buf sigjmp_env;

void PointerReadFailedHandler(int signum) { siglongjmp(sigjmp_env, 1); }

zbool zz_posix_vm_check_address_valid_via_signal(zpointer p) {
    // Set up SIGSEGV and SIGBUS handlers
    struct sigaction new_segv_action, old_segv_action;
    struct sigaction new_bus_action, old_bus_action;
    new_segv_action.sa_handler = PointerReadFailedHandler;
    new_bus_action.sa_handler = PointerReadFailedHandler;
    sigemptyset(&new_segv_action.sa_mask);
    sigemptyset(&new_bus_action.sa_mask);
    new_segv_action.sa_flags = 0;
    new_bus_action.sa_flags = 0;
    sigaction(SIGSEGV, &new_segv_action, &old_segv_action);
    sigaction(SIGBUS, &new_bus_action, &old_bus_action);

    // The signal handler will return us to here if a signal is raised
    if (sigsetjmp(sigjmp_env, 1)) {
        sigaction(SIGSEGV, &old_segv_action, NULL);
        sigaction(SIGBUS, &old_bus_action, NULL);
        return FALSE;
    }
    // ATTENTION !!! this function is conflict with LLDB, reason is below.
    // lldb is still catch EXC_BAD_ACCESS, without lldb is ok.
    // or you can use `zz_check_address_valid_via_mem` replace
    // https://stackoverflow.com/questions/26829119/how-to-make-lldb-ignore-exc-bad-access-exception
    char x = *(char *)p;
    return TRUE;
}

zsize zz_posix_vm_get_page_size() { return getpagesize(); }

// int mprotect(void *addr, size_t len, int prot);
zbool zz_posix_vm_protect(const zaddr address, zsize size, int page_prot) {
    int r;

    zsize page_size;
    zaddr aligned_addr;
    zsize aligned_size;

    page_size = zz_posix_vm_get_page_size();
    aligned_addr = (zaddr)address & ~(page_size - 1);
    aligned_size = (1 + ((address + size - 1 - aligned_addr) / page_size)) * page_size;

    r = mprotect((zpointer)aligned_addr, aligned_size, page_prot);
    if (r == -1) {
        Xerror("r = %d, at (%p) error!", r, (zpointer)address);
        return FALSE;
    }
    return TRUE;
}

zbool zz_posix_vm_protect_as_executable(const zaddr address, zsize size) {
    return zz_posix_vm_protect(address, size, (PROT_READ | PROT_EXEC | PROT_WRITE));
}

zbool zz_posxi_vm_protect_as_writable(const zaddr address, zsize size) {
    if (!zz_posix_vm_protect(address, size, (PROT_READ | PROT_EXEC | PROT_WRITE)))
        return FALSE;
    return TRUE;
}

//  void *mmap(void *addr, size_t len, int prot, int flags, int fd, off_t offset);
zpointer zz_posix_vm_allocate_pages(zsize n_pages) {
    zpointer page_mmap;
    int kr;
    zsize page_size;
    page_size = zz_posix_vm_get_page_size();

    if (n_pages <= 0) {
        n_pages = 1;
    }

    page_mmap = mmap(0, page_size * n_pages, PROT_WRITE | PROT_READ, MAP_ANONYMOUS | MAP_PRIVATE, -1, 0);

    if (page_mmap == MAP_FAILED) {
        perror("mmap");
        return NULL;
    }

    if (!zz_posix_vm_protect((zaddr)page_mmap, page_size * n_pages, (PROT_WRITE | PROT_READ)))
        return NULL;
    return (zpointer)page_mmap;
}

zpointer zz_posix_vm_allocate(zsize size) {
    zsize page_size;
    zpointer result;
    zsize n_pages;

    page_size = zz_posix_vm_get_page_size();
    n_pages = ((size + page_size - 1) & ~(page_size - 1)) / page_size;

    result = zz_posix_vm_allocate_pages(n_pages);
    return (zpointer)result;
}

zpointer zz_posix_vm_allocate_near_pages(zaddr address, zsize range_size, zsize n_pages) {
    zaddr aligned_addr;
    zpointer page_mmap;
    zaddr t;
    zsize page_size;
    page_size = zz_posix_vm_get_page_size();

    if (n_pages <= 0) {
        n_pages = 1;
    }
    aligned_addr = (zaddr)address & ~(page_size - 1);

    zaddr target_start_addr = aligned_addr - range_size;
    zaddr target_end_addr = aligned_addr + range_size;

    for (t = target_start_addr; t < target_end_addr; t += page_size) {
        page_mmap = mmap((zpointer)t, page_size * n_pages, PROT_WRITE | PROT_READ,
                         MAP_ANONYMOUS | MAP_PRIVATE | MAP_FIXED, -1, 0);
        if (page_mmap != MAP_FAILED) {
            return (zpointer)page_mmap;
        }
    }
    return NULL;
}

zpointer zz_posix_vm_search_text_code_cave(zaddr address, zsize range_size, zsize size) {
    char zeroArray[128];
    char readZeroArray[128];
    zaddr aligned_addr, tmp_addr, target_search_start, target_search_end;
    zsize page_size;

    memset(zeroArray, 0, 128);

    page_size = zz_posix_vm_get_page_size();
    aligned_addr = (zaddr)address & ~(page_size - 1);
    target_search_start = aligned_addr - range_size;
    target_search_end = aligned_addr + range_size;

    Xdebug("searching for %p cave, use 0x1000 interval.", (zpointer)address);
    for (tmp_addr = target_search_start; tmp_addr < target_search_end; tmp_addr += 0x1000) {
        if (zz_posix_vm_check_address_valid_via_signal((zpointer)tmp_addr))
            if (memcpy(readZeroArray, (zpointer)tmp_addr, 128)) {
                if (!memcmp(readZeroArray, zeroArray, 128)) {
                    return (void *)tmp_addr;
                }
            }
    }
    return NULL;
}

/*
  ref:
  substitute/lib/darwin/execmem.c:execmem_foreign_write_with_pc_patch
  frida-gum-master/gum/gummemory.c:gum_memory_patch_code

  frida-gum-master/gum/backend-darwin/gummemory-darwin.c:gum_alloc_n_pages

  mach mmap use __vm_allocate and __vm_map
  https://github.com/bminor/glibc/blob/master/sysdeps/mach/hurd/mmap.c
  https://github.com/bminor/glibc/blob/master/sysdeps/mach/munmap.c

  http://shakthimaan.com/downloads/hurd/A.Programmers.Guide.to.the.Mach.System.Calls.pdf
*/

zbool zz_posix_vm_patch_code(const zaddr address, const zpointer codedata, zuint codedata_size) {
    zsize page_size;
    zaddr start_page_addr, end_page_addr;
    zsize page_offset, range_size;

    page_size = zz_posix_vm_get_page_size();
    /*
      https://www.gnu.org/software/hurd/gnumach-doc/Memory-Attributes.html
     */
    start_page_addr = (address) & ~(page_size - 1);
    end_page_addr = ((address + codedata_size - 1)) & ~(page_size - 1);
    page_offset = address - start_page_addr;
    range_size = (end_page_addr + page_size) - start_page_addr;

    //  another method, pelease read `REF`;

    // zpointer code_mmap = mmap(NULL, range_size, PROT_READ | PROT_WRITE,
    //                           MAP_ANON | MAP_SHARED, -1, 0);
    // if (code_mmap == MAP_FAILED) {
    //   return;
    // }

    zpointer code_mmap = zz_posix_vm_allocate(range_size);

    memcpy(code_mmap, (void *)start_page_addr, range_size);

    memcpy(code_mmap + page_offset, codedata, codedata_size);

    /* SAME: mprotect(code_mmap, range_size, prot); */
    // if (!zz_posix_vm_protect((zaddr)code_mmap, range_size, PROT_READ | PROT_EXEC))
    //     return FALSE;

    zaddr target = (zaddr)start_page_addr;
    zz_posxi_vm_protect_as_writable(start_page_addr, range_size);
    memcpy((zpointer)start_page_addr, (zpointer)code_mmap, range_size);
    zz_posix_vm_protect_as_executable(start_page_addr, range_size);
    munmap(code_mmap, range_size);
    return TRUE;
}
