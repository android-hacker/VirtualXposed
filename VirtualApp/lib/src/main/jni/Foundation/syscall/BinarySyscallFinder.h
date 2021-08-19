#ifndef BINARY_SYSCALL_FINDER_H
#define BINARY_SYSCALL_FINDER_H

#include <stdint.h>

#define BREAK_FIND_SYSCALL 0
#define CONTINUE_FIND_SYSCALL 1

void findSyscalls(const char *path, bool (*callback)(const char *, int, void *));

#endif //BINARY_SYSCALL_FINDER_H
