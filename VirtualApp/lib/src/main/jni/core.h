//
// Created by Xfast on 2016/7/22.
//

#ifndef NDK_CORE_H
#define NDK_CORE_H

#include <jni.h>
#include <stdlib.h>


#include "helper/helper.h"
#include "hook/Hook.h"

__BEGIN_DECLS
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved);
__END_DECLS


#endif //NDK_CORE_H
