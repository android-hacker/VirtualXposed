//
// VirtualApp Native Project
//
#include <Jni/VAJni.h>
#include <Substrate/CydiaSubstrate.h>
#include "VMPatch.h"
#include "fake_dlfcn.h"

namespace FunctionDef {
    typedef void (*Function_DalvikBridgeFunc)(const void **, void *, const void *, void *);

    typedef jobject (*Function_openDexNativeFunc)(JNIEnv *, jclass, jstring, jstring, jint);

    typedef jobject (*Native_openDexNativeFunc_N)(JNIEnv *, jclass, jstring, jstring, jint, jobject,
                                                  jobject);


    typedef jint (*Function_cameraNativeSetupFunc_T1)(JNIEnv *, jobject, jobject, jint, jstring);

    typedef jint (*Function_cameraNativeSetupFunc_T2)(JNIEnv *, jobject, jobject, jint, jint,
                                                      jstring);

    typedef jint (*Function_cameraNativeSetupFunc_T3)(JNIEnv *, jobject, jobject, jint, jint,
                                                      jstring,
                                                      jboolean);

    typedef jint (*Function_cameraNativeSetupFunc_T4)(JNIEnv *, jobject, jobject, jint, jstring,
                                                      jboolean);

    typedef jint (*Function_getCallingUid)(JNIEnv *, jclass);

    typedef jint (*Function_audioRecordNativeCheckPermission)(JNIEnv *, jobject, jstring);
}

using namespace FunctionDef;


static struct {

    bool is_art;
    int native_offset;
    char *host_packageName;
    jint api_level;
    jmethodID method_onGetCallingUid;
    jmethodID method_onOpenDexFileNative;

    void *art_work_around_app_jni_bugs;

    char *(*GetCstrFromString)(void *);

    void *(*GetStringFromCstr)(const char *);

    int (*native_getCallingUid)(int);

    int (*IPCThreadState_self)(void);

    Function_getCallingUid jni_orig_getCallingUid;
    Function_DalvikBridgeFunc orig_cameraNativeSetup_dvm;

    int cameraMethodType;
    union {
        Function_cameraNativeSetupFunc_T1 t1;
        Function_cameraNativeSetupFunc_T2 t2;
        Function_cameraNativeSetupFunc_T3 t3;
        Function_cameraNativeSetupFunc_T4 t4;
    } orig_native_cameraNativeSetupFunc;

    Function_DalvikBridgeFunc orig_openDexFile_dvm;
    union {
        Function_openDexNativeFunc beforeN;
        Native_openDexNativeFunc_N afterN;
    } orig_openDexNativeFunc_art;

    Function_audioRecordNativeCheckPermission orig_audioRecordNativeCheckPermission;

} patchEnv;


jint getCallingUid(alias_ref<jclass> clazz) {
    jint uid;
    if (patchEnv.is_art) {
        uid = patchEnv.jni_orig_getCallingUid(Environment::ensureCurrentThreadIsAttached(),
                                              clazz.get());
    } else {
        uid = patchEnv.native_getCallingUid(patchEnv.IPCThreadState_self());
    }
    uid = Environment::ensureCurrentThreadIsAttached()->CallStaticIntMethod(nativeEngineClass.get(),
                                                                            patchEnv.method_onGetCallingUid,
                                                                            uid);
    return uid;
}


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
    env->CallStaticVoidMethod(nativeEngineClass.get(), patchEnv.method_onOpenDexFileNative, array);

    jstring newSource = (jstring) env->GetObjectArrayElement(array, 0);
    jstring newOutput = (jstring) env->GetObjectArrayElement(array, 1);

    return patchEnv.orig_openDexNativeFunc_art.beforeN(env, jclazz, newSource, newOutput,
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
    env->CallStaticVoidMethod(nativeEngineClass.get(), patchEnv.method_onOpenDexFileNative, array);

    jstring newSource = (jstring) env->GetObjectArrayElement(array, 0);
    jstring newOutput = (jstring) env->GetObjectArrayElement(array, 1);

    return patchEnv.orig_openDexNativeFunc_art.afterN(env, jclazz, newSource, newOutput, options,
                                                      loader, elements);
}


static void
new_bridge_openDexNativeFunc(const void **args, void *pResult, const void *method, void *self) {

    JNIEnv *env = Environment::ensureCurrentThreadIsAttached();

    const char *source = args[0] == NULL ? NULL : patchEnv.GetCstrFromString((void *) args[0]);
    const char *output = args[1] == NULL ? NULL : patchEnv.GetCstrFromString((void *) args[1]);

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
    env->CallStaticVoidMethod(nativeEngineClass.get(), patchEnv.method_onOpenDexFileNative, array);

    jstring newSource = (jstring) env->GetObjectArrayElement(array, 0);
    jstring newOutput = (jstring) env->GetObjectArrayElement(array, 1);

    const char *_newSource = newSource == NULL ? NULL : env->GetStringUTFChars(newSource, NULL);
    const char *_newOutput = newOutput == NULL ? NULL : env->GetStringUTFChars(newOutput, NULL);

    args[0] = _newSource == NULL ? NULL : patchEnv.GetStringFromCstr(_newSource);
    args[1] = _newOutput == NULL ? NULL : patchEnv.GetStringFromCstr(_newOutput);

    if (source && orgSource) {
        env->ReleaseStringUTFChars(orgSource, source);
    }
    if (output && orgOutput) {
        env->ReleaseStringUTFChars(orgOutput, output);
    }

    patchEnv.orig_openDexFile_dvm(args, pResult, method, self);
}

static jint new_native_cameraNativeSetupFunc_T1(JNIEnv *env, jobject thiz, jobject camera_this,
                                                jint cameraId, jstring packageName) {

    jstring host = env->NewStringUTF(patchEnv.host_packageName);

    return patchEnv.orig_native_cameraNativeSetupFunc.t1(env, thiz, camera_this,
                                                         cameraId,
                                                         host);
}

static jint new_native_cameraNativeSetupFunc_T2(JNIEnv *env, jobject thiz, jobject camera_this,
                                                jint cameraId, jint halVersion,
                                                jstring packageName) {

    jstring host = env->NewStringUTF(patchEnv.host_packageName);

    return patchEnv.orig_native_cameraNativeSetupFunc.t2(env, thiz, camera_this, cameraId,
                                                         halVersion, host);
}

static jint new_native_cameraNativeSetupFunc_T3(JNIEnv *env, jobject thiz, jobject camera_this,
                                                jint cameraId, jint halVersion,
                                                jstring packageName, jboolean option) {

    jstring host = env->NewStringUTF(patchEnv.host_packageName);

    return patchEnv.orig_native_cameraNativeSetupFunc.t3(env, thiz, camera_this, cameraId,
                                                         halVersion, host, option);
}

static jint new_native_cameraNativeSetupFunc_T4(JNIEnv *env, jobject thiz, jobject camera_this,
                                                jint cameraId,
                                                jstring packageName, jboolean option) {

    jstring host = env->NewStringUTF(patchEnv.host_packageName);

    return patchEnv.orig_native_cameraNativeSetupFunc.t4(env, thiz, camera_this, cameraId, host,
                                                         option);
}


static jint
new_native_audioRecordNativeCheckPermission(JNIEnv *env, jobject thiz, jstring _packagename) {
    jstring host = env->NewStringUTF(patchEnv.host_packageName);
    return patchEnv.orig_audioRecordNativeCheckPermission(env, thiz, host);
}


static void
new_bridge_cameraNativeSetupFunc(const void **args, void *pResult, const void *method, void *self) {
    // args[0] = this
    switch (patchEnv.cameraMethodType) {
        case 1:
            args[4] = patchEnv.GetStringFromCstr(patchEnv.host_packageName);
            break;
        case 2:
            args[5] = patchEnv.GetStringFromCstr(patchEnv.host_packageName);
            break;
        case 3:
            args[5] = patchEnv.GetStringFromCstr(patchEnv.host_packageName);
            break;
        case 4:
            args[4] = patchEnv.GetStringFromCstr(patchEnv.host_packageName);
            break;
    }
    patchEnv.orig_cameraNativeSetup_dvm(args, pResult, method, self);
}

void mark() {
    // Do nothing
};

static size_t getArtMethodAddress(jobject javaMethod, jmethodID methodId) {
    if (patchEnv.api_level < 30) {
        return (size_t) methodId;
    } else {
        JNIEnv *env = Environment::current();
        jclass executableClass = env->FindClass("java/lang/reflect/Executable");
        jfieldID artMethod = env->GetFieldID(executableClass, "artMethod", "J");
        jlong addr = env->GetLongField(javaMethod, artMethod);
        return addr;
    }
}

void measureNativeOffset(bool isArt) {

    jmethodID markMethod = nativeEngineClass->getStaticMethod<void(void)>("nativeMark").getId();

    jobject method = Environment::current()->ToReflectedMethod(nativeEngineClass.get(), markMethod, JNI_TRUE);
    size_t startAddress = (size_t) getArtMethodAddress(method, markMethod);
    size_t targetAddress = (size_t) mark;
    if (isArt && patchEnv.art_work_around_app_jni_bugs) {
        targetAddress = (size_t) patchEnv.art_work_around_app_jni_bugs;
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
            ALOGE("Error: Unable to find the jni function.");
            break;
        }
    }
    if (found) {
        patchEnv.native_offset = offset;
        if (!isArt) {
            patchEnv.native_offset += (sizeof(int) + sizeof(void *));
        }
    }
}


inline void replaceGetCallingUid(jboolean isArt) {
    auto binderClass = findClassLocal("android/os/Binder");
    if (isArt) {
        size_t mtd_getCallingUid = (size_t) binderClass->getStaticMethod<jint(void)>(
                "getCallingUid").getId();
        int nativeFuncOffset = patchEnv.native_offset;
        void **jniFuncPtr = (void **) (mtd_getCallingUid + nativeFuncOffset);
        patchEnv.jni_orig_getCallingUid = (Function_getCallingUid) (*jniFuncPtr);
        *jniFuncPtr = (void *) getCallingUid;
    } else {
        binderClass->registerNatives({makeNativeMethod("getCallingUid", getCallingUid)});
    }
}

inline void
replaceOpenDexFileMethod(jobject javaMethod, jboolean isArt, int apiLevel) {
    jmethodID openDexNative = Environment::current()->FromReflectedMethod(javaMethod);
    size_t mtd_openDexNative = getArtMethodAddress(javaMethod, openDexNative);
    int nativeFuncOffset = patchEnv.native_offset;
    void **jniFuncPtr = (void **) (mtd_openDexNative + nativeFuncOffset);

    if (!isArt) {
        patchEnv.orig_openDexFile_dvm = (Function_DalvikBridgeFunc) (*jniFuncPtr);
        *jniFuncPtr = (void *) new_bridge_openDexNativeFunc;
    } else {
        if (apiLevel < 24) {
            patchEnv.orig_openDexNativeFunc_art.beforeN = (Function_openDexNativeFunc) (*jniFuncPtr);
            *jniFuncPtr = (void *) new_native_openDexNativeFunc;
        } else {
            patchEnv.orig_openDexNativeFunc_art.afterN = (Native_openDexNativeFunc_N) (*jniFuncPtr);
            *jniFuncPtr = (void *) new_native_openDexNativeFunc_N;
        }
    }

}


inline void
replaceCameraNativeSetupMethod(jobject javaMethod, jboolean isArt, int apiLevel) {

    if (!javaMethod) {
        return;
    }
    jmethodID cameraNativeSetup = Environment::current()->FromReflectedMethod(javaMethod);
    size_t mtd_cameraNativeSetup = getArtMethodAddress(javaMethod, cameraNativeSetup);
    int nativeFuncOffset = patchEnv.native_offset;
    void **jniFuncPtr = (void **) (mtd_cameraNativeSetup + nativeFuncOffset);

    if (!isArt) {
        patchEnv.orig_cameraNativeSetup_dvm = (Function_DalvikBridgeFunc) (*jniFuncPtr);
        *jniFuncPtr = (void *) new_bridge_cameraNativeSetupFunc;
    } else {
        switch (patchEnv.cameraMethodType) {
            case 1:
                patchEnv.orig_native_cameraNativeSetupFunc.t1 = (Function_cameraNativeSetupFunc_T1) (*jniFuncPtr);
                *jniFuncPtr = (void *) new_native_cameraNativeSetupFunc_T1;
                break;
            case 2:
                patchEnv.orig_native_cameraNativeSetupFunc.t2 = (Function_cameraNativeSetupFunc_T2) (*jniFuncPtr);
                *jniFuncPtr = (void *) new_native_cameraNativeSetupFunc_T2;
                break;
            case 3:
                patchEnv.orig_native_cameraNativeSetupFunc.t3 = (Function_cameraNativeSetupFunc_T3) (*jniFuncPtr);
                *jniFuncPtr = (void *) new_native_cameraNativeSetupFunc_T3;
                break;
            case 4:
                patchEnv.orig_native_cameraNativeSetupFunc.t4 = (Function_cameraNativeSetupFunc_T4) (*jniFuncPtr);
                *jniFuncPtr = (void *) new_native_cameraNativeSetupFunc_T4;
                break;
        }
    }

}


void
replaceAudioRecordNativeCheckPermission(jobject javaMethod, jboolean isArt, int api) {
    if (!javaMethod || !isArt) {
        return;
    }
    jmethodID methodStruct = Environment::current()->FromReflectedMethod(javaMethod);
    size_t mtd_methodStruct = getArtMethodAddress(javaMethod, methodStruct);
    void **funPtr = (void **) (mtd_methodStruct + patchEnv.native_offset);
    patchEnv.orig_audioRecordNativeCheckPermission = (Function_audioRecordNativeCheckPermission) (*funPtr);
    *funPtr = (void *) new_native_audioRecordNativeCheckPermission;
}


/**
 * Only called once.
 * @param javaMethod Method from Java
 * @param isArt Dalvik or Art
 * @param apiLevel Api level from Java
 */
void hookAndroidVM(JArrayClass<jobject> javaMethods,
                   jstring packageName, jboolean isArt, jint apiLevel,
                   jint cameraMethodType) {

    JNIEnv *env = Environment::current();

    JNINativeMethod methods[] = {
            NATIVE_METHOD((void *) mark, "nativeMark", "()V"),
    };
    if (env->RegisterNatives(nativeEngineClass.get(), methods, 1) < 0) {
        return;
    }
    patchEnv.is_art = isArt;
    patchEnv.cameraMethodType = cameraMethodType;
    patchEnv.host_packageName = (char *) env->GetStringUTFChars(packageName,
                                                                NULL);
    patchEnv.api_level = apiLevel;
    void *soInfo = getDvmOrArtSOHandle();
    patchEnv.method_onGetCallingUid = nativeEngineClass->getStaticMethod<jint(jint)>(
            "onGetCallingUid").getId();
    patchEnv.method_onOpenDexFileNative = env->GetStaticMethodID(nativeEngineClass.get(),
                                                                 "onOpenDexFileNative",
                                                                 "([Ljava/lang/String;)V");

    if (isArt) {
        patchEnv.art_work_around_app_jni_bugs = dlsym(soInfo, "art_work_around_app_jni_bugs");
    } else {
        // workaround for dlsym returns null when system has libhoudini
        void *h = dlopen("/system/lib/libandroid_runtime.so", RTLD_LAZY);
        {
            patchEnv.IPCThreadState_self = (int (*)(void)) dlsym(RTLD_DEFAULT,
                                                                 "_ZN7android14IPCThreadState4selfEv");
            patchEnv.native_getCallingUid = (int (*)(int)) dlsym(RTLD_DEFAULT,
                                                                 "_ZNK7android14IPCThreadState13getCallingUidEv");
            if (patchEnv.IPCThreadState_self == NULL) {
                patchEnv.IPCThreadState_self = (int (*)(void)) dlsym(RTLD_DEFAULT,
                                                                     "_ZN7android14IPCThreadState13getCallingUidEv");
            }
        }
        if (h != NULL) {
            dlclose(h);
        }

        patchEnv.GetCstrFromString = (char *(*)(void *)) dlsym(soInfo,
                                                               "_Z23dvmCreateCstrFromStringPK12StringObject");
        if (!patchEnv.GetCstrFromString) {
            patchEnv.GetCstrFromString = (char *(*)(void *)) dlsym(soInfo,
                                                                   "dvmCreateCstrFromString");
        }
        patchEnv.GetStringFromCstr = (void *(*)(const char *)) dlsym(soInfo,
                                                                     "_Z23dvmCreateStringFromCstrPKc");
        if (!patchEnv.GetStringFromCstr) {
            patchEnv.GetStringFromCstr = (void *(*)(const char *)) dlsym(soInfo,
                                                                         "dvmCreateStringFromCstr");
        }
    }
    measureNativeOffset(isArt);
    // Crash on Q if hook directly by modify entrypoint of function.
    // Just skip this step on Q and get never crash
    if(apiLevel <= 28)
        replaceGetCallingUid(isArt);

    replaceOpenDexFileMethod(javaMethods.getElement(OPEN_DEX).get(), isArt,
                             apiLevel);
    replaceCameraNativeSetupMethod(javaMethods.getElement(CAMERA_SETUP).get(),
                                   isArt, apiLevel);
    replaceAudioRecordNativeCheckPermission(javaMethods.getElement(
            AUDIO_NATIVE_CHECK_PERMISSION).get(),
                                            isArt, apiLevel);
}

bool processNothing(void* thiz, void* new_methods){ return true; }
bool (*orig_ProcessProfilingInfo)(void*, void*);

bool compileNothing(void* thiz, void* thread, void* method, bool osr) { return false; }
bool (*orig_CompileNothing)(void* thiz, void* thread, void* method, bool osr);

void (*org_notifyJitActivity)(void *);
void notifyNothing(void *thiz) {
    return;
}

void disableJit(int apiLevel) {
#ifdef __arm__
    void *libart = fake_dlopen("/system/lib/libart.so", RTLD_NOW);
    if (libart) {
        // disable profile.
        void *processProfilingInfo = NULL;
        const char *processProfileInfoFunc =
                apiLevel < 26 ? "_ZN3art12ProfileSaver20ProcessProfilingInfoEPt" :
                "_ZN3art12ProfileSaver20ProcessProfilingInfoEbPt";
        processProfilingInfo = fake_dlsym(libart, processProfileInfoFunc);
        ALOGE("processProfileingInfo: %p", processProfilingInfo);
        if (processProfilingInfo) {
            MSHookFunction(processProfilingInfo, (void*)processNothing, (void**)&orig_ProcessProfilingInfo);
        }

        // disable jit
        void *compileMethod = NULL;
        compileMethod = fake_dlsym(libart,
                                   "_ZN3art3jit3Jit13CompileMethodEPNS_9ArtMethodEPNS_6ThreadEb");
        ALOGE("compileMethod: %p", compileMethod);
        if (compileMethod) {
            MSHookFunction(compileMethod, (void*) compileNothing, (void**) &orig_CompileNothing);
        }

        void *notifyJitActivity = fake_dlsym(libart, "_ZN3art12ProfileSaver17NotifyJitActivityEv");
        if (notifyJitActivity) {
            MSHookFunction(notifyJitActivity, (void *) notifyNothing,
                          (void **) &org_notifyJitActivity);
        }
    }
#endif
}

void *getDvmOrArtSOHandle() {
    char so_name[25] = {0};
    __system_property_get("persist.sys.dalvik.vm.lib.2", so_name);
    if (strlen(so_name) == 0) {
        __system_property_get("persist.sys.dalvik.vm.lib", so_name);
    }
    void *soInfo = dlopen(so_name, 0);
    if (!soInfo) {
        soInfo = RTLD_DEFAULT;
    }
    return soInfo;
}
