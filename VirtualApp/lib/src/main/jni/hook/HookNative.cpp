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
        g_sym_IPCThreadState_self = dlsym(RTLD_DEFAULT, "_ZN7android14IPCThreadState4selfEv");
        g_sym_IPCThreadState_getCallingUid = dlsym(RTLD_DEFAULT, "_ZNK7android14IPCThreadState13getCallingUidEv");
        if (g_sym_IPCThreadState_getCallingUid == NULL) {
            g_sym_IPCThreadState_getCallingUid = dlsym(RTLD_DEFAULT, "_ZN7android14IPCThreadState13getCallingUidEv");
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
        LOGD("Done hook Binder");
    }
}



namespace HOOK_JAVA {
    void mark() {}

    typedef void (*Bridge_DalvikBridgeFunc)(const void **, void *, const void *, void *);
    typedef jobject (*Native_openDexNativeFunc)(JNIEnv *, jclass, jstring, jstring, jint);

    static Bridge_DalvikBridgeFunc org_DalvikBridgeFunc;
    static Native_openDexNativeFunc org_native_openDexNativeFunc;

    static jmethodID g_methodid_onOpenDexFileNative;

    static jobject new_native_openDexNativeFunc(JNIEnv* env, jclass jclazz, jstring javaSourceName, jstring javaOutputName, jint options) {
        jclass stringClass = env->FindClass("java/lang/String");
        jobjectArray array = env->NewObjectArray(2, stringClass, NULL);

        if (javaSourceName) {
            env->SetObjectArrayElement(array, 0, javaSourceName);
        }
        if (javaOutputName) {
            env->SetObjectArrayElement(array, 1, javaOutputName);
        }
        env->CallStaticVoidMethod(g_jclass, g_methodid_onOpenDexFileNative, array);

        jstring newSource = (jstring) env->GetObjectArrayElement(array, 0);
        jstring newOutput = (jstring) env->GetObjectArrayElement(array, 1);

        return org_native_openDexNativeFunc(env, jclazz, newSource, newOutput, options);
    }

    static void new_bridge_openDexNativeFunc(const void **args, void *pResult, const void *method, void *self) {
        JNIEnv *env = NULL;
        g_vm->GetEnv((void **) &env, JNI_VERSION_1_6);
        g_vm->AttachCurrentThread(&env, NULL);

        typedef char* (*GetCstrFromString)(void *);
        typedef void* (*GetStringFromCstr)(const char*);

        GetCstrFromString getCstrFromString;
        getCstrFromString =(GetCstrFromString) dlsym(RTLD_DEFAULT, "_Z23dvmCreateCstrFromStringPK12StringObject");
        if (!getCstrFromString) {
            getCstrFromString =(GetCstrFromString) dlsym(RTLD_DEFAULT, "dvmCreateCstrFromString");
        }

        const char* source = args[0] == NULL ? NULL : getCstrFromString((void*) args[0]);
        const char* output = args[1] == NULL ? NULL : getCstrFromString((void*) args[1]);

        jstring orgSource = source == NULL ? NULL : env->NewStringUTF(source);
        jstring orgOutput = output == NULL ? NULL : env->NewStringUTF(output);

        jclass stringClass = env->FindClass("java/lang/String");
        jobjectArray array = env->NewObjectArray(2, stringClass, NULL);
        if (orgSource) {
            env->SetObjectArrayElement(array, 0, orgSource);
        }
        if (orgOutput) {
            env->SetObjectArrayElement(array, 1, orgOutput);
        }

        env->CallStaticVoidMethod(g_jclass, g_methodid_onOpenDexFileNative, array);

        jstring newSource = (jstring) env->GetObjectArrayElement(array, 0);
        jstring newOutput = (jstring) env->GetObjectArrayElement(array, 1);

        const char *_newSource = newSource == NULL ? NULL : env->GetStringUTFChars(newSource, NULL);
        const char *_newOutput = newOutput == NULL ? NULL : env->GetStringUTFChars(newOutput, NULL);

        GetStringFromCstr getStringFromCstr;
        getStringFromCstr =(GetStringFromCstr) dlsym(RTLD_DEFAULT, "_Z23dvmCreateStringFromCstrPKc");
        if (!getStringFromCstr) {
            getStringFromCstr =(GetStringFromCstr) dlsym(RTLD_DEFAULT, "dvmCreateStringFromCstr");
        }

        args[0] = _newSource == NULL ? NULL : getStringFromCstr(_newSource);
        args[1] = _newOutput == NULL ? NULL : getStringFromCstr(_newOutput);

        if (source && orgSource) {
            env->ReleaseStringUTFChars(orgSource, source);
        }
        if (output && orgOutput) {
            env->ReleaseStringUTFChars(orgOutput, output);
        }

        org_DalvikBridgeFunc(args, pResult, method, self);
    }



    ///////////////////binder////////////////////////
    typedef void (*Bridge_DalvikBridgeFunc_Binder)(const void **, void *, const void *, void *);
    typedef jint (*Native_getCallingUidNativeFunc_Binder)(JNIEnv *, jclass);

    static Bridge_DalvikBridgeFunc_Binder org_DalvikBridgeFunc_Binder;
    static Native_getCallingUidNativeFunc_Binder org_native_getCallingUid;

    static jmethodID g_methodid_onGetCallingUid;

    static jint new_native_getCallingUidNativeFunc(JNIEnv* env, jclass jclazz) {

        jint org_uid = org_native_getCallingUid(env, jclazz);

        jint new_uid = env->CallStaticIntMethod(g_jclass, g_methodid_onGetCallingUid, org_uid);

        return new_uid;
    }

    static void new_bridge_getCallingUidNativeFunc(const void **args, void *pResult, const void *method, void *self) {
        JNIEnv *env = NULL;
        g_vm->GetEnv((void **) &env, JNI_VERSION_1_6);
        g_vm->AttachCurrentThread(&env, NULL);

        org_DalvikBridgeFunc_Binder(args, pResult, method, self);

        jint new_uid = env->CallStaticIntMethod(g_jclass, g_methodid_onGetCallingUid, *(int*)pResult);

        *(int*)pResult = new_uid;

    }
    //////////////////end binder////////////////////




    static JNINativeMethod gMethods[] = {
            NATIVE_METHOD((void *) mark, "nativeMark", "()V"),
    };

    static int  nativeFuncOffset;

    void searchJniOffset() {
        JNIEnv *env = NULL;
        g_vm->GetEnv((void **) &env, JNI_VERSION_1_4);
        g_vm->AttachCurrentThread(&env, NULL);

        if (env->RegisterNatives(g_jclass, gMethods, NELEM(gMethods)) < 0) {
            LOGE("hook mark failed! because register methods FAILED!!!");
            return;
        }
        void * art_work_around_app_jni_bugs = dlsym(RTLD_DEFAULT, "art_work_around_app_jni_bugs");

        jmethodID mtd_nativeHook = env->GetStaticMethodID(g_jclass, gMethods[0].name, gMethods[0].signature);

        size_t memory = (size_t) mtd_nativeHook;
        size_t destAddr = art_work_around_app_jni_bugs ? (size_t) art_work_around_app_jni_bugs : (size_t) mark;

        int offset = 0;
        bool found = false;
        while (true) {
            if (*((size_t*) (memory + offset)) == destAddr) {
                found = true;
                break;
            }
            offset += 4;
            if (offset >= 100) {
                LOGE("Unable to find the jniFunc.");
                break;
            }
        }
        if (found) {
            LOGE("Get Offset : %d", offset);
            nativeFuncOffset = offset;
        }
    }

    void hook(jobject javaMethod, jboolean isArt) {
        LOGD("hook java...");
        JNIEnv *env = NULL;
        g_vm->GetEnv((void **) &env, JNI_VERSION_1_4);
        g_vm->AttachCurrentThread(&env, NULL);

        g_methodid_onOpenDexFileNative = env->GetStaticMethodID(g_jclass, "onOpenDexFileNative", "([Ljava/lang/String;)V");

        g_methodid_onGetCallingUid = env->GetStaticMethodID(g_jclass, "onGetCallingUid", "(I)I");

        if (!isArt) {
            nativeFuncOffset += (sizeof(int) + sizeof(void*));//this is jni Bridge offset.
        }

        size_t mtd_openDexNative = (size_t) env->FromReflectedMethod(javaMethod);

        void** jniFuncPtr = (void**)(mtd_openDexNative + nativeFuncOffset);

        LOGE("offset=%d", nativeFuncOffset);
        if (!isArt) {
            LOGD("replace dalvik method.");
            org_DalvikBridgeFunc = (Bridge_DalvikBridgeFunc)(*jniFuncPtr);
            *jniFuncPtr = (void*) new_bridge_openDexNativeFunc;
        } else {
            LOGD("replace art method.");
            org_native_openDexNativeFunc = (Native_openDexNativeFunc)(*jniFuncPtr);
            *jniFuncPtr = (void*) new_native_openDexNativeFunc;
        }


//        jclass cls_Binder = env->FindClass("android/os/Binder");
//        size_t mtd_getCallingUid = (size_t) env->GetStaticMethodID(cls_Binder, "getCallingUid", "()I");
//
//        void** jniFuncPtr_Binder = (void**)(mtd_getCallingUid + nativeFuncOffset);
//
//        LOGE("offset=%d", nativeFuncOffset);
//        if (!isArt) {
//            LOGD("replace dalvik method.");
//            org_DalvikBridgeFunc_Binder = (Bridge_DalvikBridgeFunc_Binder)(*jniFuncPtr_Binder);
//            *jniFuncPtr_Binder = (void*) new_bridge_getCallingUidNativeFunc;
//        } else {
//            LOGD("replace art method.");
//            org_native_getCallingUid = (Native_getCallingUidNativeFunc_Binder)(*jniFuncPtr_Binder);
//            *jniFuncPtr_Binder = (void*) new_native_getCallingUidNativeFunc;
//        }

        LOGD("DONE java hook!");
    }
};



void HOOK_NATIVE::hook(jobject javaMethod, jboolean isArt) {
    LOGI("Begin Native hooks...");
    HOOK_JAVA::searchJniOffset();
    HOOK_BINDER::hook();
    HOOK_JAVA::hook(javaMethod, isArt);
    LOGI("End Native hooks SUCCESS!");
}


