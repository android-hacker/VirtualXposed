/**
 * @author Lody
 *
 */


#include <stdio.h>
#include <limits.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include "BinarySyscallFinder.h"

typedef unsigned long addr_t;

#if defined(__aarch64__)
#define AARCH64_SVC_0 0xD4000001
#define AARCH64_IS_MOV(insn) ((int32_t)((insn) & 0xFFE0001F) == 0xD2800008)

void
search_memory_syscall(const char *path, addr_t begin, addr_t end,
                      bool (*callback)(const char *, int, void *)) {
    addr_t start = begin;
    addr_t limit = end - sizeof(int32_t) * 2;
    do {
        int32_t *insn = reinterpret_cast<int32_t *>(start);
        if (insn[1] == AARCH64_SVC_0 && AARCH64_IS_MOV(insn[0])) {
            unsigned syscall_num = (unsigned) ((insn[0] >> 5) & 0xFFFF);
            if (!(*callback)(path, syscall_num, insn)) {
                break;
            }
        }
        start += sizeof(int32_t);
    } while (start < limit);
}
#elif defined(__arm__)

#define ARM_IS_MOV_R7_IMM(insn) (((insn) & 0xFF00F000) == 0xE3007000)

void
search_memory_syscall(const char *path, addr_t begin, addr_t end,
                      bool (*callback)(const char *, int, void *)) {
    addr_t start = begin;
    addr_t limit = end - sizeof(int32_t) * 4;
    do {
        int32_t *insn = reinterpret_cast<int32_t *>(start);
        if (insn[0] == 0xE1A0C007 && ARM_IS_MOV_R7_IMM(insn[1]) && insn[2] == 0xEF000000) {
            int32_t value = insn[1];
            int syscall = ((value & 0xF0000) >> 4) | (value & 0x00FFF);
            (*callback)(path, syscall, NULL);
        }
        start += 1;
    } while (start < limit);
}

#elif defined(__i386__)
void
search_memory_syscall(const char *path, addr_t begin, addr_t end,
                      bool (*callback)(const char *, int, void *)) {

}
#elif defined(__x86_64__)
void
search_memory_syscall(const char *path, addr_t begin, addr_t end,
                      bool (*callback)(const char *, int, void *)) {

}
#endif


bool has_code(const char *perm) {
    bool r = false, x = false;
    for (int i = 0; i < 5; ++i) {
        if (perm[i] == 'r') {
            r = true;
        }
        if (perm[i] == 'x') {
            x = true;
        }
    }
    return r && x;
}

void findSyscalls(const char *path, bool (*callback)(const char *, int, void *)) {
    FILE *f;
    if ((f = fopen("/proc/self/maps", "r")) == NULL) {
        return;
    }
    char buf[PATH_MAX + 100], perm[5], dev[6], mapname[PATH_MAX];
    addr_t begin, end, inode, foo;

    while (!feof(f)) {
        if (fgets(buf, sizeof(buf), f) == 0)
            break;
        mapname[0] = '\0';
        sscanf(buf, "%lx-%lx %4s %lx %5s %ld %s", &begin, &end, perm,
               &foo, dev, &inode, mapname);
        if (strstr(buf, path) && has_code(perm)) {
            search_memory_syscall(path, begin, end, callback);
        }
    }
    fclose(f);
}