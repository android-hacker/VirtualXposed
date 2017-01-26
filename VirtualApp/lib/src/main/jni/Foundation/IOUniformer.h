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
#include<dirent.h>
#include <sys/syscall.h>

#include <InlineHook/MSHook.h>
#include "Helper.h"


#define HOOK_IO(func) hook_template("libc.so", #func, (void*) new_##func, (void**) &orig_##func)
#define HOOK_DEF(ret, func, ...) \
  ret (*orig_##func)(__VA_ARGS__); \
  ret new_##func(__VA_ARGS__)


namespace IOUniformer {
    void startUniformer(int api_level);

    void redirect(const char*orig_path, const char*new_path);

    const char *query(const char *orig_path);

    const char *restore(const char *redirected_path);
}

#endif //NDK_HOOK_H
