LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := iohook

LOCAL_CFLAGS := -Wno-error=format-security -fpermissive
LOCAL_CFLAGS += -fno-rtti -fno-exceptions

LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Foundation
LOCAL_C_INCLUDES += $(LOCAL_PATH)/InlineHook

LOCAL_SRC_FILES := Core.cpp \
				   Foundation/IOUniformer.cpp \
				   Foundation/VMPatch.cpp \
				   InlineHook/MSHook.cpp \
				   InlineHook/x86_64.cpp \
				   InlineHook/ARM.cpp \
				   InlineHook/Debug.cpp \
				   InlineHook/Hooker.cpp \
				   InlineHook/PosixMemory.cpp \
				   InlineHook/Thumb.cpp \
				   InlineHook/util.cpp \
				   InlineHook/x86.cpp

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)


