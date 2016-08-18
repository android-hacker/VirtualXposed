//
// VirtualApp Native Project
//

#ifndef NDK_HOOK_NATIVE_H
#define NDK_HOOK_NATIVE_H


#include <jni.h>
#include <dlfcn.h>
#include <stddef.h>
#include <fcntl.h>
#include <sys/system_properties.h>

#include "../helper/helper.h"


void hookNative(jobject javaMethod, jboolean isArt);


#endif //NDK_HOOK_NATIVE_H
