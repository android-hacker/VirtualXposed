//
// VirtualApp Native Project
//
#include "VMPatch.h"

typedef void (*Bridge_DalvikBridgeFunc)(const void **, void *, const void *, void *);

typedef jobject (*Native_openDexNativeFunc)(JNIEnv *, jclass, jstring, jstring, jint);

typedef jobject (*Native_openDexNativeFunc_N)(JNIEnv *, jclass, jstring, jstring, jint, jobject,
                                              jobject);

typedef jint (*Native_cameraNativeSetupFunc_T1)(JNIEnv *, jobject, jobject, jint, jint, jstring);

typedef jint (*Native_cameraNativeSetupFunc_T2)(JNIEnv *, jobject, jobject, jint, jstring);

typedef jint (*Native_getCallingUid)(JNIEnv *, jclass);


static struct {

    bool isArt;
    int nativeOffset;
    char *hostPackageName;
    jint apiLevel;
    jclass binder_class;
    jmethodID method_onGetCallingUid;
    jmethodID method_onOpenDexFileNative;

    void *art_work_around_app_jni_bugs;

    char *(*GetCstrFromString)(void *);

    void *(*GetStringFromCstr)(const char *);


    void *sym_IPCThreadState_self;
    void *sym_IPCThreadState_getCallingUid;

    Native_getCallingUid orig_getCallingUid;

    int cameraMethodType;
    Bridge_DalvikBridgeFunc orig_cameraNativeSetup_dvm;
    union {
        Native_cameraNativeSetupFunc_T1 t1;
        Native_cameraNativeSetupFunc_T2 t2;
    } orig_native_cameraNativeSetupFunc;

    Bridge_DalvikBridgeFunc orig_openDexFile_dvm;
    union {
        Native_openDexNativeFunc beforeN;
        Native_openDexNativeFunc_N afterN;
    } orig_native_openDexNativeFunc;

} gOffset;


extern JavaVM *g_vm;
extern jclass g_jclass;


void mark() {
    // Do nothing
};

jint getCallingUid(JNIEnv *env, jclass jclazz) {
    jint uid;
    if (gOffset.isArt) {
        uid = gOffset.orig_getCallingUid(env, jclazz);
    } else {
        int (*org_getCallingUid)(int) = (int (*)(int)) gOffset.sym_IPCThreadState_getCallingUid;
        int (*func_self)(void) = (int (*)(void)) gOffset.sym_IPCThreadState_self;
        uid = org_getCallingUid(func_self());
    }
    uid = env->CallStaticIntMethod(g_jclass, gOffset.method_onGetCallingUid, uid);
    return uid;
}


static JNINativeMethod gMarkMethods[] = {
        NATIVE_METHOD((void *) mark, "nativeMark", "()V"),
};

JNINativeMethod gUidMethods[] = {
        NATIVE_METHOD((void *) getCallingUid, "getCallingUid", "()I"),
};


static jobject new_native_openDexNativeFunc(JNIEnv *env, jclass jclazz, jstring javaSourceName,
                                            jstring javaOutputName, jint options) {
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray array = env->NewObjectArray(2, stringClass, NULL);

    if (javaSourceName) {
        env->SetObjectArrayElement(array, 0, javaSourceName);
    }
    if (javaOutputName) {
        env->SetObjectArrayElement(array, 1, javaOutputName);
    }
    env->CallStaticVoidMethod(g_jclass, gOffset.method_onOpenDexFileNative, array);

    jstring newSource = (jstring) env->GetObjectArrayElement(array, 0);
    jstring newOutput = (jstring) env->GetObjectArrayElement(array, 1);

    return gOffset.orig_native_openDexNativeFunc.beforeN(env, jclazz, newSource, newOutput,
                                                         options);
}

static jobject new_native_openDexNativeFunc_N(JNIEnv *env, jclass jclazz, jstring javaSourceName,
                                              jstring javaOutputName, jint options, jobject loader,
                                              jobject elements) {
    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray array = env->NewObjectArray(2, stringClass, NULL);

    if (javaSourceName) {
        env->SetObjectArrayElement(array, 0, javaSourceName);
    }
    if (javaOutputName) {
        env->SetObjectArrayElement(array, 1, javaOutputName);
    }
    env->CallStaticVoidMethod(g_jclass, gOffset.method_onOpenDexFileNative, array);

    jstring newSource = (jstring) env->GetObjectArrayElement(array, 0);
    jstring newOutput = (jstring) env->GetObjectArrayElement(array, 1);

    return gOffset.orig_native_openDexNativeFunc.afterN(env, jclazz, newSource, newOutput, options,
                                                        loader, elements);
}


static void
new_bridge_openDexNativeFunc(const void **args, void *pResult, const void *method, void *self) {
    JNIEnv *env = NULL;
    g_vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    g_vm->AttachCurrentThread(&env, NULL);

    typedef char *(*GetCstrFromString)(void *);
    typedef void *(*GetStringFromCstr)(const char *);

    const char *source = args[0] == NULL ? NULL : gOffset.GetCstrFromString((void *) args[0]);
    const char *output = args[1] == NULL ? NULL : gOffset.GetCstrFromString((void *) args[1]);

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

    env->CallStaticVoidMethod(g_jclass, gOffset.method_onOpenDexFileNative, array);

    jstring newSource = (jstring) env->GetObjectArrayElement(array, 0);
    jstring newOutput = (jstring) env->GetObjectArrayElement(array, 1);

    const char *_newSource = newSource == NULL ? NULL : env->GetStringUTFChars(newSource, NULL);
    const char *_newOutput = newOutput == NULL ? NULL : env->GetStringUTFChars(newOutput, NULL);

    args[0] = _newSource == NULL ? NULL : gOffset.GetStringFromCstr(_newSource);
    args[1] = _newOutput == NULL ? NULL : gOffset.GetStringFromCstr(_newOutput);

    if (source && orgSource) {
        env->ReleaseStringUTFChars(orgSource, source);
    }
    if (output && orgOutput) {
        env->ReleaseStringUTFChars(orgOutput, output);
    }

    gOffset.orig_openDexFile_dvm(args, pResult, method, self);
}


static jint new_native_cameraNativeSetupFunc_T1(JNIEnv *env, jobject thiz, jobject camera_this,
                                                jint cameraId, jint halVersion,
                                                jstring packageName) {

    jstring host = env->NewStringUTF(gOffset.hostPackageName);

    return gOffset.orig_native_cameraNativeSetupFunc.t1(env, thiz, camera_this, cameraId,
                                                            halVersion, host);
}

static jint new_native_cameraNativeSetupFunc_T2(JNIEnv *env, jobject thiz, jobject camera_this,
                                                jint cameraId, jstring packageName) {

    jstring host = env->NewStringUTF(gOffset.hostPackageName);

    return gOffset.orig_native_cameraNativeSetupFunc.t2(env, thiz, camera_this,
                                                                       cameraId,
                                                                       host);
}


static void
new_bridge_cameraNativeSetupFunc(const void **args, void *pResult, const void *method, void *self) {
    JNIEnv *env = NULL;
    g_vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    g_vm->AttachCurrentThread(&env, NULL);
    // args[0] = this
    if (gOffset.cameraMethodType == 1) {
        args[4] = gOffset.GetStringFromCstr(gOffset.hostPackageName);
    } else if (gOffset.cameraMethodType == 2) {
        args[5] = gOffset.GetStringFromCstr(gOffset.hostPackageName);
    }
    gOffset.orig_cameraNativeSetup_dvm(args, pResult, method, self);
}


void measureNativeOffset(JNIEnv *env, bool isArt) {

    jmethodID mtd_nativeHook = env->GetStaticMethodID(g_jclass, gMarkMethods[0].name,
                                                      gMarkMethods[0].signature);

    size_t startAddress = (size_t) mtd_nativeHook;
    size_t targetAddress = (size_t) mark;
    if (isArt && gOffset.art_work_around_app_jni_bugs) {
        targetAddress = (size_t) gOffset.art_work_around_app_jni_bugs;
    }

    int offset = 0;
    bool found = false;
    while (true) {
        if (*((size_t *) (startAddress + offset)) == targetAddress) {
            found = true;
            break;
        }
        offset += 4;
        if (offset >= 100) {
            LOGE("Error: Unable to find the jni function.");
            break;
        }
    }
    if (found) {
        gOffset.nativeOffset = offset;
        if (!isArt) {
            gOffset.nativeOffset += (sizeof(int) + sizeof(void *));
        }
    }
}


inline void replaceGetCallingUid(JNIEnv *env, jboolean isArt) {


    if (isArt) {
        size_t mtd_getCallingUid = (size_t) env->GetStaticMethodID(gOffset.binder_class,
                                                                   "getCallingUid", "()I");
        int nativeFuncOffset = gOffset.nativeOffset;
        void **jniFuncPtr = (void **) (mtd_getCallingUid + nativeFuncOffset);
        gOffset.orig_getCallingUid = (Native_getCallingUid) (*jniFuncPtr);
        *jniFuncPtr = (void *) getCallingUid;
    } else {
        env->RegisterNatives(gOffset.binder_class, gUidMethods, NELEM(gUidMethods));
    }

}

inline void
replaceOpenDexFileMethod(JNIEnv *env, jobject javaMethod, jboolean isArt, int apiLevel) {

    size_t mtd_openDexNative = (size_t) env->FromReflectedMethod(javaMethod);
    int nativeFuncOffset = gOffset.nativeOffset;
    void **jniFuncPtr = (void **) (mtd_openDexNative + nativeFuncOffset);

    if (!isArt) {
        gOffset.orig_openDexFile_dvm = (Bridge_DalvikBridgeFunc) (*jniFuncPtr);
        *jniFuncPtr = (void *) new_bridge_openDexNativeFunc;
    } else {
        if (apiLevel < ANDROID_N) {
            gOffset.orig_native_openDexNativeFunc.beforeN = (Native_openDexNativeFunc) (*jniFuncPtr);
            *jniFuncPtr = (void *) new_native_openDexNativeFunc;
        } else {
            gOffset.orig_native_openDexNativeFunc.afterN = (Native_openDexNativeFunc_N) (*jniFuncPtr);
            *jniFuncPtr = (void *) new_native_openDexNativeFunc_N;
        }
    }

}


inline void
replaceCameraNativeSetupMethod(JNIEnv *env, jobject javaMethod, jboolean isArt, int apiLevel) {

    if (!javaMethod) {
        return;
    }
    size_t mtd_cameraNativeSetup = (size_t) env->FromReflectedMethod(javaMethod);
    int nativeFuncOffset = gOffset.nativeOffset;
    void **jniFuncPtr = (void **) (mtd_cameraNativeSetup + nativeFuncOffset);

    if (!isArt) {
        gOffset.orig_cameraNativeSetup_dvm = (Bridge_DalvikBridgeFunc) (*jniFuncPtr);
        *jniFuncPtr = (void *) new_bridge_cameraNativeSetupFunc;
    } else {
        if (apiLevel >= ANDROID_L) {
            gOffset.orig_native_cameraNativeSetupFunc.t1 = (Native_cameraNativeSetupFunc_T1) (*jniFuncPtr);
            *jniFuncPtr = (void *) new_native_cameraNativeSetupFunc_T1;
        }
        if (ANDROID_JBMR2 <= apiLevel && apiLevel < ANDROID_L) {
            gOffset.orig_native_cameraNativeSetupFunc.t2 = (Native_cameraNativeSetupFunc_T2) (*jniFuncPtr);
            *jniFuncPtr = (void *) new_native_cameraNativeSetupFunc_T2;
        }
    }

}


/**
 * Only called once.
 * @param javaMethod Method from Java
 * @param isArt Dalvik or Art
 * @param apiLevel Api level from Java
 */
void patchAndroidVM(jobjectArray javaMethods, jstring packageName, jboolean isArt, jint apiLevel,
                    jint cameraMethodType) {

    JNIEnv *env = NULL;
    g_vm->GetEnv((void **) &env, JNI_VERSION_1_6);
    g_vm->AttachCurrentThread(&env, NULL);

    if (env->RegisterNatives(g_jclass, gMarkMethods, NELEM(gMarkMethods)) < 0) {
        return;
    }
    gOffset.isArt = isArt;
    gOffset.cameraMethodType = cameraMethodType;
    gOffset.hostPackageName = (char *) env->GetStringUTFChars(packageName, NULL);
    gOffset.apiLevel = apiLevel;
    void *soInfo = getVMHandle();
    gOffset.binder_class = env->FindClass("android/os/Binder");
    gOffset.method_onGetCallingUid = env->GetStaticMethodID(g_jclass, "onGetCallingUid", "(I)I");
    gOffset.method_onOpenDexFileNative = env->GetStaticMethodID(g_jclass, "onOpenDexFileNative",
                                                                "([Ljava/lang/String;)V");

    if (isArt) {
        gOffset.art_work_around_app_jni_bugs = dlsym(soInfo, "art_work_around_app_jni_bugs");
    } else {
        gOffset.sym_IPCThreadState_self = dlsym(RTLD_DEFAULT, "_ZN7android14IPCThreadState4selfEv");
        gOffset.sym_IPCThreadState_getCallingUid = dlsym(RTLD_DEFAULT,
                                                         "_ZNK7android14IPCThreadState13getCallingUidEv");
        if (gOffset.sym_IPCThreadState_getCallingUid == NULL) {
            gOffset.sym_IPCThreadState_getCallingUid = dlsym(RTLD_DEFAULT,
                                                             "_ZN7android14IPCThreadState13getCallingUidEv");
        }

        gOffset.GetCstrFromString = (char *(*)(void *)) dlsym(soInfo,
                                                              "_Z23dvmCreateCstrFromStringPK12StringObject");
        if (!gOffset.GetCstrFromString) {
            gOffset.GetCstrFromString = (char *(*)(void *)) dlsym(soInfo,
                                                                  "dvmCreateCstrFromString");
        }
        gOffset.GetStringFromCstr = (void *(*)(const char *)) dlsym(soInfo,
                                                                    "_Z23dvmCreateStringFromCstrPKc");
        if (!gOffset.GetStringFromCstr) {
            gOffset.GetStringFromCstr = (void *(*)(const char *)) dlsym(soInfo,
                                                                        "dvmCreateStringFromCstr");
        }
    }
    measureNativeOffset(env, isArt);
    replaceGetCallingUid(env, isArt);
    replaceOpenDexFileMethod(env, env->GetObjectArrayElement(javaMethods, OPEN_DEX), isArt,
                             apiLevel);
    replaceCameraNativeSetupMethod(env, env->GetObjectArrayElement(javaMethods, CAMERA_SETUP),
                                   isArt, apiLevel);
}

void *getVMHandle() {
    char soName[15] = {0};
    __system_property_get("persist.sys.dalvik.vm.lib.2", soName);
    if (soName[0] == '\x0') {
        __system_property_get("persist.sys.dalvik.vm.lib", soName);
    }
    void *soInfo = dlopen(soName, 0);
    if (!soInfo) {
        soInfo = RTLD_DEFAULT;
    }
    return soInfo;
}