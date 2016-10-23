//
// VirtualApp Native Project
//

#ifndef NDK_HOOK_H
#define NDK_HOOK_H


#include <string>
#include <map>
#include <jni.h>
#include <dlfcn.h>
#include <stddef.h>
#include <fcntl.h>
#include <sys/syscall.h>

#include "../MSHook/hook.h"
#include "../helper/helper.h"


#define HOOK_IO(func) hook_template("libc.so", #func, (void*) new_##func, (void**) &org_##func)
#define DEX2OAT_BIN "/system/bin/dex2oat"
#define HOOK_DEF(ret, func, ...) \
  ret (*org_##func)(__VA_ARGS__); \
  ret new_##func(__VA_ARGS__)


namespace HOOK {
    void hook(int api_level);

    void redirect(const char*org_path, const char*new_path);

    const char *query(const char *org_path);

    const char *restore(const char *redirected_path);

    void enableTurboDex(bool enable);
}

#endif //NDK_HOOK_H
