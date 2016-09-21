//
// VirtualApp Native Project
//
#include "core.h"


JavaVM *g_vm;
jclass g_jclass;



void hook_native(JNIEnv *env, jclass jclazz, jobject javaMethod, jboolean isArt, jint apiLevel) {
    static bool hasHooked = false;
    if (hasHooked) {
        return;
    }
    hookNative(javaMethod, isArt, apiLevel);
    hasHooked = true;
}


void hook_io(JNIEnv *env, jclass jclazz, jint apiLevel) {
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
        NATIVE_METHOD((void *) hook_io,  "nativeHook",                  "(I)V"),
        NATIVE_METHOD((void *) redirect, "nativeRedirect",              "(Ljava/lang/String;Ljava/lang/String;)V"),
        NATIVE_METHOD((void *) query,    "nativeGetRedirectedPath",     "(Ljava/lang/String;)Ljava/lang/String;"),
        NATIVE_METHOD((void *) restore,  "nativeRestoreRedirectedPath", "(Ljava/lang/String;)Ljava/lang/String;"),

        NATIVE_METHOD((void *) hook_native, "nativeHookNative", "(Ljava/lang/Object;ZI)V"),
};



JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    jclass javaClass = env->FindClass(JAVA_CLASS);
    if (javaClass == NULL) {
        LOGE("Ops: Unable to find hook class.");
        return JNI_ERR;
    }
    if (env->RegisterNatives(javaClass, gMethods, NELEM(gMethods)) < 0) {
        LOGE("Ops: Unable to register the native methods.");
        return JNI_ERR;
    }
    g_vm = vm;
    g_jclass = (jclass) env->NewGlobalRef(javaClass);
    env->DeleteLocalRef(javaClass);
    return JNI_VERSION_1_6;
}



JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved) {
    JNIEnv *env;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return;
    }
    env->DeleteGlobalRef((jobject)g_vm);
    env->DeleteGlobalRef((jobject)g_jclass);
}

