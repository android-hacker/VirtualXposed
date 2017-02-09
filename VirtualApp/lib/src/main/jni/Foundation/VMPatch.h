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

#include "Helper.h"

enum METHODS {
    OPEN_DEX = 0, CAMERA_SETUP
};

void patchAndroidVM(jobjectArray javaMethods, jstring packageName, jboolean isArt, jint apiLevel, jint cameraMethodType);

void *getVMHandle();


#endif //NDK_HOOK_NATIVE_H
