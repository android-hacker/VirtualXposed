//
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
    const char *so_path = Environment::current()->GetStringUTFChars(selfSoPath, NULL);
    IOUniformer::startUniformer(so_path, apiLevel, preview_api_level);
}

static void jni_nativeIOWhitelist(alias_ref<jclass> jclazz, jstring _path) {
    const char *path = Environment::current()->GetStringUTFChars(_path, NULL);
    IOUniformer::whitelist(path);
}

static void jni_nativeIOForbid(alias_ref<jclass> jclazz, jstring _path) {
    const char *path = Environment::current()->GetStringUTFChars(_path, NULL);
    IOUniformer::forbid(path);
}


static void jni_nativeIORedirect(alias_ref<jclass> jclazz, jstring orgPath, jstring newPath) {
    const char *org_path = Environment::current()->GetStringUTFChars(orgPath, NULL);
    const char *new_path = Environment::current()->GetStringUTFChars(newPath, NULL);
    IOUniformer::redirect(org_path, new_path);
}

static jstring jni_nativeGetRedirectedPath(alias_ref<jclass> jclazz, jstring orgPath) {
    const char *org_path = Environment::current()->GetStringUTFChars(orgPath, NULL);
    const char *redirected_path = IOUniformer::query(org_path);
    if (redirected_path != NULL) {
        return Environment::current()->NewStringUTF(redirected_path);
    }
    return NULL;
}

static jstring jni_nativeRestoreRedirectedPath(alias_ref<jclass> jclazz, jstring redirectedPath) {
    const char *redirected_path = Environment::current()->GetStringUTFChars(redirectedPath, NULL);
    const char *org_path = IOUniformer::restore(redirected_path);
    return Environment::current()->NewStringUTF(org_path);
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
                makeNativeMethod("nativeRestoreRedirectedPath", jni_nativeRestoreRedirectedPath),
                makeNativeMethod("nativeLaunchEngine", jni_nativeLaunchEngine),
        });
    });
}

extern "C" __attribute__((constructor)) void _init(void) {
    IOUniformer::init_before_all();
}


