//
// Created by Xfast on 2016/8/8.
//
#include "HookNative.h"



extern JavaVM *g_vm;
extern jclass g_jclass;



namespace HOOK_BINDER {
    // src: IPCThreadState.cpp
    static void *g_sym_IPCThreadState_self;
    static void *g_sym_IPCThreadState_getCallingUid;
    static jmethodID g_methodid_onGetCallingUid;

    int getCallingUid(JNIEnv *env, jclass jclazz) {
        int (*org_getCallingUid)(int) = (int (*)(int)) g_sym_IPCThreadState_getCallingUid;
        int (*func_self)(void) = (int (*)(void)) g_sym_IPCThreadState_self;
        int uid = org_getCallingUid(func_self());
        if (uid == getuid()) {
            uid = env->CallStaticIntMethod(g_jclass, g_methodid_onGetCallingUid, uid);
            return uid;
        }
        return uid;
    }

    static JNINativeMethod gMethods[] = {
            NATIVE_METHOD((void *) getCallingUid, "getCallingUid", "()I"),
    };

    void hook() {
        LOGD("hook Binder...");
        g_sym_IPCThreadState_self = dlsym((void*)0xFFFFFFFF, "_ZN7android14IPCThreadState4selfEv");
        g_sym_IPCThreadState_getCallingUid = dlsym((void*)0xFFFFFFFF, "_ZNK7android14IPCThreadState13getCallingUidEv");
        if (g_sym_IPCThreadState_getCallingUid == NULL) {
            g_sym_IPCThreadState_getCallingUid = dlsym((void*)0xFFFFFFFF, "_ZN7android14IPCThreadState13getCallingUidEv");
        }
        if (g_sym_IPCThreadState_self == NULL || g_sym_IPCThreadState_getCallingUid == NULL) {
            LOGE("hook Binder failed!");
            return;
        }
        LOGD("sym_IPCThreadState=%p, sym_getCallingUid=%p", g_sym_IPCThreadState_self, g_sym_IPCThreadState_getCallingUid);
        JNIEnv *env = NULL;
        g_vm->GetEnv((void **) &env, JNI_VERSION_1_4);
        g_vm->AttachCurrentThread(&env, NULL);
        g_methodid_onGetCallingUid = env->GetStaticMethodID(g_jclass, JAVA_CALLBACK__BINDER__ON_GET_CALLING_UID, JAVA_CALLBACK__BINDER__ON_GET_CALLING_UID_SIGNATURE);
        jclass jclass_Binder = env->FindClass("android/os/Binder");
        if (env->RegisterNatives(jclass_Binder, gMethods, NELEM(gMethods)) < 0) {
            LOGE("hook Binder failed! because register methods FAILED!!!");
            return;
        }
    }
}


void HOOK_NATIVE::hook() {
    LOGI("Begin Native hooks...");

    HOOK_BINDER::hook();

    LOGI("End Native hooks SUCCESS!!!");
}
