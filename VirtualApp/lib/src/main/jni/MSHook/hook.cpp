#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include "util.h"
#include "log.h"

#include "Hooker.h"
#include "hook.h"


int elfHook(const char *soname, const char *symbol, void *replace_func,
            void **old_func) {
    int ret = -1;
    void *addr = NULL;
    if (find_name(getpid(), symbol, soname, (unsigned long *) &addr) < 0) {
        MS_LOGW("Not find: %s\n", symbol);
        return -1;
    }
    Cydia::MSHookFunction(addr, replace_func, old_func);
    ret = 0;
    return ret;
}

int elfHookDirect(unsigned int addr, void *replace_func, void **old_func) {
    if (addr == 0) {
        MS_LOGW("hook direct addr:%p  error!", (void *) addr);
        return -1;
    }
    Cydia::MSHookFunction((void *) addr, replace_func, old_func);
    return 0;
}
