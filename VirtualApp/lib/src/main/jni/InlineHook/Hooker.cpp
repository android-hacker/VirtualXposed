#include <unistd.h>
#include "Hooker.h"
#include "util.h"
#include "ARM.h"
#include "Thumb.h"
#include "x86.h"

void Cydia::MSHookFunction(void *symbol, void *replace, void **result) {

    SubstrateProcessRef process = NULL;
    if (MSDebug){
        MSLog(MSLogLevelNotice, "SubstrateHookFunction(process:%p, symbol:%p, replace:%p, result:%p)", process, symbol, replace, result);
    }
#if defined(__arm__) || defined(__thumb__)
    if ((reinterpret_cast<uintptr_t>(symbol) & 0x1) == 0){
        return ARM::SubstrateHookFunctionARM(process, symbol, replace, result);
    }else{
        return Thumb::SubstrateHookFunctionThumb(process, reinterpret_cast<void *>(reinterpret_cast<uintptr_t>(symbol) & ~0x1), replace, result);
    }
#endif


#if defined(__i386__) || defined(__x86_64__)
    return x86::SubstrateHookFunctionx86(process, symbol, replace, result);
#endif
}

void Cydia::MSHookFunction(const char *soname, const char *symbol, void *replace_func,
                           void **old_func) {
    void *addr = NULL;
    if (find_name(getpid(), symbol, soname, (unsigned long *)&addr) < 0) {
        MSLog(MSLogLevelError, "Not found %s in %s.", symbol, soname);
        return;
    }
    Cydia::MSHookFunction(addr, replace_func, old_func);
}