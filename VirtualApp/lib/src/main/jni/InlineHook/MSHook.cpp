#include <stdio.h>
#include <unistd.h>
#include "util.h"

#include "Hooker.h"
#include "MSHook.h"


int inlineHook(const char *soname, const char *symbol, void *replace_func,
               void **old_func) {
    int ret = -1;
    void *addr = NULL;
    if (findSymbol(symbol, soname, (unsigned long *) &addr) < 0) {
        return -1;
    }
    Cydia::MSHookFunction(addr, replace_func, old_func);
    ret = 0;
    return ret;
}

int findSymbol(const char *name, const char *libn,
              unsigned long *addr) {
    return find_name(getpid(), name, libn, addr);
}

int inlineHookDirect(unsigned int addr, void *replace_func, void **old_func) {
    if (addr == 0) {
        return -1;
    }
    Cydia::MSHookFunction((void *) addr, replace_func, old_func);
    return 0;
}
