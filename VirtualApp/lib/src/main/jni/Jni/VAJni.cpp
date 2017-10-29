#include <elf.h>//
// VirtualApp Native Project
//
#include <Foundation/IOUniformer.h>
#include <fb/include/fb/Build.h>
#include <fb/include/fb/ALog.h>
#include <fb/include/fb/fbjni.h>
#include "VAJni.h"

using namespace facebook::jni;

static void jni_nativeLaunchEngine(alias_ref<jclass> clazz, JArrayClass<jobject> javaMethods,
                            jstring packageName,
                            jboolean isArt, jint apiLevel, jint cameraMethodType) {
    hookAndroidVM(javaMethods, packageName, isArt, apiLevel, cameraMethodType);
}


static void jni_nativeEnableIORedirect(alias_ref<jclass>, jstring selfSoPath, jint apiLevel,
                                jint preview_api_level) {
    JNIEnv *env = Environment::current();
    const char *so_path = env->GetStringUTFChars(selfSoPath, NULL);
    IOUniformer::startUniformer(so_path, apiLevel, preview_api_level);
    env->ReleaseStringUTFChars(selfSoPath, so_path);
}

static void jni_nativeIOWhitelist(alias_ref<jclass> jclazz, jstring _path) {
    JNIEnv *env = Environment::current();
    const char *path = Environment::current()->GetStringUTFChars(_path, NULL);
    IOUniformer::whitelist(path);
    env->ReleaseStringUTFChars(_path, path);
}

static void jni_nativeIOForbid(alias_ref<jclass> jclazz, jstring _path) {
    JNIEnv *env = Environment::current();
    const char *path = env->GetStringUTFChars(_path, NULL);
    IOUniformer::forbid(path);
    env->ReleaseStringUTFChars(_path, path);
}


static void jni_nativeIORedirect(alias_ref<jclass> jclazz, jstring origPath, jstring newPath) {
    JNIEnv *env = Environment::current();
    const char *orig_path = env->GetStringUTFChars(origPath, NULL);
    const char *new_path = env->GetStringUTFChars(newPath, NULL);
    IOUniformer::redirect(orig_path, new_path);
    env->ReleaseStringUTFChars(origPath, orig_path);
    env->ReleaseStringUTFChars(newPath, new_path);

}

static jstring jni_nativeGetRedirectedPath(alias_ref<jclass> jclazz, jstring origPath) {
    JNIEnv *env = Environment::current();
    const char *orig_path = env->GetStringUTFChars(origPath, NULL);
    const char *redirected_path = IOUniformer::query(orig_path);
    env->ReleaseStringUTFChars(origPath, orig_path);
    if (redirected_path != NULL) {
        return env->NewStringUTF(redirected_path);
    }
    return NULL;
}

static jstring jni_nativeReverseRedirectedPath(alias_ref<jclass> jclazz, jstring redirectedPath) {
    JNIEnv *env = Environment::current();
    const char *redirected_path = env->GetStringUTFChars(redirectedPath, NULL);
    const char *orig_path = IOUniformer::reverse(redirected_path);
    env->ReleaseStringUTFChars(redirectedPath, redirected_path);
    return Environment::current()->NewStringUTF(orig_path);
}


alias_ref<jclass> nativeEngineClass;


JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    return initialize(vm, [] {
        nativeEngineClass = findClassStatic("com/lody/virtual/client/NativeEngine");
        nativeEngineClass->registerNatives({
                makeNativeMethod("nativeEnableIORedirect", jni_nativeEnableIORedirect),
                makeNativeMethod("nativeIOWhitelist", jni_nativeIOWhitelist),
                makeNativeMethod("nativeIOForbid", jni_nativeIOForbid),
                makeNativeMethod("nativeIORedirect", jni_nativeIORedirect),
                makeNativeMethod("nativeGetRedirectedPath", jni_nativeGetRedirectedPath),
                makeNativeMethod("nativeReverseRedirectedPath", jni_nativeReverseRedirectedPath),
                makeNativeMethod("nativeLaunchEngine", jni_nativeLaunchEngine),
        });
    });
}

extern "C" __attribute__((constructor)) void _init(void) {
    IOUniformer::init_env_before_all();
}


