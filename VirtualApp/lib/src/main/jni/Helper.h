//
// VirtualApp Native Project
//

#ifndef NDK_LOG_H
#define NDK_LOG_H

#include <android/log.h>

#define TAG "VA-Native"

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,  TAG, __VA_ARGS__)
#define LOGDT(T, ...) __android_log_print(ANDROID_LOG_DEBUG,  T, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

#define FREE(ptr, org_ptr) { if ((void*) ptr != NULL && (void*) ptr != (void*) org_ptr) { free((void*) ptr); } }

#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))

#define NATIVE_METHOD(func_ptr, func_name, signature) { func_name, signature, reinterpret_cast<void*>(func_ptr) }

#define JAVA_CLASS "com/lody/virtual/IOHook"

#define ANDROID_JBMR2    18
#define ANDROID_L        21
#define ANDROID_N        24


#endif //NDK_LOG_H
