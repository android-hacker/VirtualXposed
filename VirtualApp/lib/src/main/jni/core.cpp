//
// Created by Xfast on 2016/7/21.
//
#include "core.h"


void hook(JNIEnv *env, jclass jclazz, jint apiLevel) {
    static bool hasHooked = false;
    if (hasHooked) {
        return;
    }
    HOOK::hook(apiLevel);
    hasHooked = true;
}

void redirect(JNIEnv *env, jclass jclazz, jstring orgPath, jstring newPath) {
    const char *org_path = env->GetStringUTFChars(orgPath, NULL);
    const char *new_path = env->GetStringUTFChars(newPath, NULL);
    HOOK::redirect(org_path, new_path);
}

jstring query(JNIEnv *env, jclass jclazz, jstring orgPath) {
    const char *org_path = env->GetStringUTFChars(orgPath, NULL);
    const char *redirected_path = HOOK::query(org_path);
    return env->NewStringUTF(redirected_path);
}

jstring restore(JNIEnv *env, jclass jclazz, jstring redirectedPath) {
    const char *redirected_path = env->GetStringUTFChars(redirectedPath, NULL);
    const char *org_path = HOOK::restore(redirected_path);
    return env->NewStringUTF(org_path);
}


static JNINativeMethod gMethods[] = {
        NATIVE_METHOD((void *) redirect, "nativeRedirect", "(Ljava/lang/String;Ljava/lang/String;)V"),
        NATIVE_METHOD((void *) hook,     "nativeHook",     "(I)V"),
        //...hum
        NATIVE_METHOD((void *) query,    "nativeGetRedirectedPath",     "(Ljava/lang/String;)Ljava/lang/String;"),
        NATIVE_METHOD((void *) restore,  "nativeRestoreRedirectedPath", "(Ljava/lang/String;)Ljava/lang/String;"),
//        NATIVE_METHOD((void *) reject,  "rejectPath",             "(Ljava/lang/String;)V"),
};


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("GetEnv() FAILED!!!");
        return JNI_ERR;
    }

    jclass javaClass = env->FindClass(JAVA_CLASS);
    if (javaClass == NULL) {
        LOGE("unable to find class: %s", JAVA_CLASS);
        return JNI_ERR;
    }
    env->UnregisterNatives(javaClass);
    if (env->RegisterNatives(javaClass, gMethods, NELEM(gMethods)) < 0) {
        LOGE("register methods FAILED!!!");
        return JNI_ERR;
    }
    env->DeleteLocalRef(javaClass);

    LOGI("JavaVM::GetEnv() SUCCESS!");
    return JNI_VERSION_1_4;
}

