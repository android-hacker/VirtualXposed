APP_ABI := armeabi-v7a x86
APP_PLATFORM := android-14
APP_STL := gnustl_static
APP_OPTIM := release
VA_ROOT          := $(call my-dir)
NDK_MODULE_PATH  := $(NDK_MODULE_PATH):$(VA_ROOT)