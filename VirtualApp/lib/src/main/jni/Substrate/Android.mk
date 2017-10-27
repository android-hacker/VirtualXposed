LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := substrate

LOCAL_C_INCLUDES += $(LOCAL_PATH)

LOCAL_SRC_FILES := hde64.c \
                   SubstrateDebug.cpp \
                   SubstrateHook.cpp \
                   SubstratePosixMemory.cpp \
                   SymbolFinder.cpp \

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)

include $(BUILD_STATIC_LIBRARY)