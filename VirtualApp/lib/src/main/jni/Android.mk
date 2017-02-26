LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := va-native

LOCAL_CFLAGS := -Wno-error=format-security -fpermissive
LOCAL_CFLAGS += -fno-rtti -fno-exceptions

LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Foundation
LOCAL_C_INCLUDES += $(LOCAL_PATH)/MSHook
LOCAL_C_INCLUDES += $(LOCAL_PATH)/GodinHook

ifeq ($(TARGET_ARCH_ABI),x86)
    ARCH_FILES := \
        MSHook/MSHook.cpp \
        MSHook/x86_64.cpp \
        MSHook/ARM.cpp \
        MSHook/Debug.cpp \
        MSHook/Hooker.cpp \
        MSHook/PosixMemory.cpp \
        MSHook/Thumb.cpp \
        MSHook/util.cpp \
        MSHook/x86.cpp \

else ifeq ($(TARGET_ARCH_ABI),x86_64)
    ARCH_FILES := \
        MSHook/MSHook.cpp \
        MSHook/x86_64.cpp \
        MSHook/ARM.cpp \
        MSHook/Debug.cpp \
        MSHook/Hooker.cpp \
        MSHook/PosixMemory.cpp \
        MSHook/Thumb.cpp \
        MSHook/util.cpp \
        MSHook/x86.cpp \

else
    ARCH_FILES := \
        GodinHook/mem_helper.cpp \
        GodinHook/instruction/instruction_helper.cpp \
        GodinHook/instruction/arm_instruction.cpp \
        GodinHook/instruction/thumb_instruction.cpp \
        GodinHook/native_hook.cpp \
        GodinHook/thread_helper.cpp \
        MSHook/MSHook.cpp \
        MSHook/x86_64.cpp \
        MSHook/ARM.cpp \
        MSHook/Debug.cpp \
        MSHook/Hooker.cpp \
        MSHook/PosixMemory.cpp \
        MSHook/Thumb.cpp \
        MSHook/util.cpp \
        MSHook/x86.cpp \

endif


LOCAL_SRC_FILES := Core.cpp \
				   Foundation/IOUniformer.cpp \
				   Foundation/VMPatch.cpp \
				   $(ARCH_FILES) \


LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
