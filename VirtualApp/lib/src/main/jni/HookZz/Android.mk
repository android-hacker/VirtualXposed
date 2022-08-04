#
# ndk-build NDK_PROJECT_PATH=. APP_BUILD_SCRIPT=./android.mk APP_ABI=armeabi(armeabi-v7a/arm64-v8a)
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

ZZ_INCLUDE := $(LOCAL_PATH)/include \
			$(LOCAL_PATH)/src

ZZ_SRC := $(wildcard $(LOCAL_PATH)/src/*.c) \
			$(wildcard $(LOCAL_PATH)/src/zzdeps/common/*.c) \
			$(wildcard $(LOCAL_PATH)/src/zzdeps/linux/*.c) \
			$(wildcard $(LOCAL_PATH)/src/zzdeps/posix/*.c) \
			$(wildcard $(LOCAL_PATH)/src/platforms/backend-linux/*.c) \
			$(wildcard $(LOCAL_PATH)/src/platforms/backend-posix/*.c)

ifeq ($(TARGET_ARCH), arm)
	ZZ_SRC += $(wildcard $(LOCAL_PATH)/src/platforms/arch-arm/*.c) \
			$(wildcard $(LOCAL_PATH)/src/platforms/backend-arm/*.c)
else ifeq ($(TARGET_ARCH), arm64)
	ZZ_SRC += $(wildcard $(LOCAL_PATH)/src/platforms/arch-arm64/*.c) \
			$(wildcard $(LOCAL_PATH)/src/platforms/backend-arm64/*.c)
else ifeq ($(TARGET_ARCH), x86)
	ZZ_SRC += $(wildcard $(LOCAL_PATH)/src/platforms/arch-x86/*.c) \
			$(wildcard $(LOCAL_PATH)/src/platforms/backend-x86/*.c)
endif

LOCAL_MODULE := hookzz
LOCAL_C_INCLUDES := $(ZZ_INCLUDE)
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_SRC_FILES := 	$(ZZ_SRC)

include $(BUILD_STATIC_LIBRARY)