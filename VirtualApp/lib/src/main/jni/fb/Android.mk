LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= \
       assert.cpp \
       jni/ByteBuffer.cpp \
       jni/Countable.cpp \
       jni/Environment.cpp \
       jni/Exceptions.cpp \
       jni/fbjni.cpp \
       jni/Hybrid.cpp \
       jni/jni_helpers.cpp \
       jni/LocalString.cpp \
       jni/OnLoad.cpp \
       jni/References.cpp \
       jni/WeakReference.cpp \
       log.cpp \
       lyra/lyra.cpp \
       onload.cpp \

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

LOCAL_CFLAGS := -DLOG_TAG=\"libfb\" -DDISABLE_CPUCAP -DDISABLE_XPLAT -fexceptions -frtti
LOCAL_CFLAGS += -Wall -Werror
# include/utils/threads.h has unused parameters
LOCAL_CFLAGS += -Wno-unused-parameter
ifeq ($(TOOLCHAIN_PERMISSIVE),true)
  LOCAL_CFLAGS += -Wno-error=unused-but-set-variable
endif
LOCAL_CFLAGS += -DHAVE_POSIX_CLOCKS

CXX11_FLAGS := -std=gnu++11
LOCAL_CFLAGS += $(CXX11_FLAGS)

LOCAL_EXPORT_CPPFLAGS := $(CXX11_FLAGS)

LOCAL_LDLIBS := -llog -ldl -landroid
LOCAL_EXPORT_LDLIBS := -llog

LOCAL_MODULE := libfb

include $(BUILD_STATIC_LIBRARY)

