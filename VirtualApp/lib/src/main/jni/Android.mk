LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := va-native

LOCAL_CFLAGS := -Wno-error=format-security -fpermissive
LOCAL_CFLAGS += -fno-rtti -fno-exceptions

LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Foundation
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Substrate


LOCAL_SRC_FILES := Core.cpp \
				   Foundation/IOUniformer.cpp \
				   Foundation/VMPatch.cpp \
                   Substrate/hde64.c \
                   Substrate/SubstrateDebug.cpp \
                   Substrate/SubstrateHook.cpp \
                   Substrate/SubstratePosixMemory.cpp \
                   Substrate/SymbolFinder.cpp \


LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
