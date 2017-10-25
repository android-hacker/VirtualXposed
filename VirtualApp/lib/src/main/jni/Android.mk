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
				   Foundation/SymbolFinder.cpp \
				   Foundation/Path.cpp \
				   Foundation/SandboxFs.cpp \

LOCAL_LDLIBS := -llog
LOCAL_STATIC_LIBRARIES := hookzz

include $(BUILD_SHARED_LIBRARY)
include $(LOCAL_PATH)/HookZz/Android.mk