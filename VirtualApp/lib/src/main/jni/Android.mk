LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := iohook

LOCAL_CFLAGS := -Wno-error=format-security -fpermissive
LOCAL_CFLAGS += -fno-rtti -fno-exceptions

LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/hook
LOCAL_C_INCLUDES += $(LOCAL_PATH)/helper
LOCAL_C_INCLUDES += $(LOCAL_PATH)/MSHook

LOCAL_SRC_FILES := core.cpp \
				   hook/Hook.cpp \
				   hook/HookNative.cpp \
				   MSHook/hook.cpp \
				   MSHook/x86_64.cpp \
				   MSHook/ARM.cpp \
				   MSHook/Debug.cpp \
				   MSHook/Hooker.cpp \
				   MSHook/PosixMemory.cpp \
				   MSHook/Thumb.cpp \
				   MSHook/util.cpp \
				   MSHook/x86.cpp

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)


