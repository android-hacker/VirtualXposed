NO_COLOR=\x1b[0m
OK_COLOR=\x1b[32;01m
ERROR_COLOR=\x1b[31;01m
WARN_COLOR=\x1b[33;01m

HOOKZZ_NAME := hookzz
HOOKZZ_DIR := $(abspath .)
LOCAL_PATH := $(abspath .)
OUTPUT_DIR := $(abspath build)

CFLAGS ?= -O0 -g
CXXFLAGS = $(CFLAGS) -stdlib=libc++ -std=c++11 -gmodules
LDFLAGS ?=
LIBS_CFLAGS ?= -fPIC

HOST ?= $(shell uname -s)
HOST_ARCH ?= $(shell uname -m)

ifeq ($(HOST), Darwin)

endif

ZZ_SRCS_PATH := $(abspath $(LOCAL_PATH)/src)
ZZ_DEPS_PATH := $(abspath $(LOCAL_PATH)/src/zzdeps)
ZZ_CAPSTONE_DEPS_PATH := $(abspath $(LOCAL_PATH)/deps/capstone)

ZZ_COMMON_SRCS := $(wildcard $(ZZ_SRCS_PATH)/*.c)
ZZ_SRCS := $(ZZ_COMMON_SRCS) \
			$(wildcard $(ZZ_SRCS_PATH)/platforms/backend-posix/*.c)

# zzdeps
ZZ_DEPS_SRCS := $(wildcard $(ZZ_DEPS_PATH)/common/*.c) \
		$(wildcard $(ZZ_DEPS_PATH)/posix/*.c) 

BACKEND ?= ios
ARCH ?= arm64

ifeq ($(BACKEND), ios)
	ifeq ($(ARCH), arm)
		ZZ_ARCH := armv7
	else ifeq ($(ARCH), arm64)
		ZZ_ARCH := arm64
	endif

	ZZ_BACKEND := ios
	ZZ_GXX_BIN := $(shell xcrun --sdk iphoneos --find clang++)
	ZZ_GCC_BIN := $(shell xcrun --sdk iphoneos --find clang)
	ZZ_SDK_ROOT := $(shell xcrun --sdk iphoneos --show-sdk-path)
	ZZ_AR_BIN := $(shell which ar)
	ZZ_RANLIB_BIN := $(shell which ranlib)

	ZZ_DEPS_SRCS += $(wildcard $(ZZ_DEPS_PATH)/darwin/*.c)
	ZZ_SRCS += $(wildcard $(ZZ_SRCS_PATH)/platforms/backend-darwin/*.c)
	
	ZZ_CFLAGS := -g -fPIC -shared -dynamiclib
	ZZ_DLL := lib$(HOOKZZ_NAME).dylib

	CFLAGS += -arch $(ZZ_ARCH)
	
	ZZ_GCC_SOURCE := $(ZZ_GCC_BIN) -isysroot $(ZZ_SDK_ROOT)
	ZZ_GXX_SOURCE := $(ZZ_GXX_BIN) -isysroot $(ZZ_SDK_ROOT)
	ZZ_GCC_TEST := $(ZZ_GCC_BIN) -isysroot $(ZZ_SDK_ROOT)
	ZZ_GXX_TEST := $(ZZ_GXX_BIN) -isysroot $(ZZ_SDK_ROOT)
else ifeq ($(BACKEND), macos)
	ifeq ($(ARCH), x86)
		ZZ_ARCH := i386
	else ifeq ($(ARCH), x86_64)
		ZZ_ARCH := x86_64
	endif

	ZZ_BACKEND := macos
	ZZ_GXX_BIN := $(shell xcrun --sdk macosx --find clang++)
	ZZ_GCC_BIN := $(shell xcrun --sdk macosx --find clang)
	ZZ_SDK_ROOT := $(shell xcrun --sdk macosx --show-sdk-path)
	ZZ_AR_BIN := $(shell which ar)
	ZZ_RANLIB_BIN := $(shell which ranlib)

	ZZ_DEPS_SRCS += $(wildcard $(ZZ_DEPS_PATH)/darwin/*.c)
	ZZ_SRCS += $(wildcard $(ZZ_SRCS_PATH)/platforms/backend-darwin/*.c)
	
	ZZ_CFLAGS := -g -fPIC -shared -dynamiclib
	ZZ_DLL := lib$(HOOKZZ_NAME).dylib

	CFLAGS += -arch $(ZZ_ARCH)
	
	ZZ_GCC_SOURCE := $(ZZ_GCC_BIN) -isysroot $(ZZ_SDK_ROOT)
	ZZ_GXX_SOURCE := $(ZZ_GXX_BIN) -isysroot $(ZZ_SDK_ROOT)
	ZZ_GCC_TEST := $(ZZ_GCC_BIN) -isysroot $(ZZ_SDK_ROOT)
	ZZ_GXX_TEST := $(ZZ_GXX_BIN) -isysroot $(ZZ_SDK_ROOT)
else ifeq ($(BACKEND), android)
	ZZ_BACKEND := android

	ifeq ($(ARCH), arm)
		ZZ_ARCH := armv7
		ZZ_API_LEVEL := android-19
		ZZ_CROSS_PREFIX := arm-linux-androideabi-
		ZZ_BIN_CROSS_PREFIX := arm-linux-androideabi-
	else ifeq ($(ARCH), arm64)
		ZZ_ARCH := arm64
		ZZ_API_LEVEL := android-21
		ZZ_CROSS_PREFIX := aarch64-linux-android-
		ZZ_BIN_CROSS_PREFIX := aarch64-linux-android-
	else ifeq ($(ARCH), x86)
		ZZ_ARCH := x86
		ZZ_API_LEVEL := android-21
		ZZ_CROSS_PREFIX := x86-
		ZZ_BIN_CROSS_PREFIX := i686-linux-android-

	endif

	HOST_DIR := $(shell echo $(HOST) | tr A-Z a-z)-$(HOST_ARCH)
	ZZ_NDK_HOME := $(shell dirname `which ndk-build`)
	ZZ_SDK_ROOT := $(ZZ_NDK_HOME)/platforms/$(ZZ_API_LEVEL)/arch-$(ARCH)
	ZZ_GCC_BIN := $(ZZ_NDK_HOME)/toolchains/$(ZZ_CROSS_PREFIX)4.9/prebuilt/$(HOST_DIR)/bin/$(ZZ_BIN_CROSS_PREFIX)gcc
	ZZ_GXX_BIN := $(ZZ_NDK_HOME)/toolchains/$(ZZ_CROSS_PREFIX)4.9/prebuilt/$(HOST_DIR)/bin/$(ZZ_BIN_CROSS_PREFIX)g++
	ZZ_AR_BIN := $(ZZ_NDK_HOME)/toolchains/$(ZZ_CROSS_PREFIX)4.9/prebuilt/$(HOST_DIR)/bin/$(ZZ_BIN_CROSS_PREFIX)ar
	ZZ_RANLIB_BIN := $(ZZ_NDK_HOME)/toolchains/$(ZZ_CROSS_PREFIX)4.9/prebuilt/$(HOST_DIR)/bin/$(ZZ_BIN_CROSS_PREFIX)ranlib

	ZZ_DEPS_SRCS += $(wildcard $(ZZ_DEPS_PATH)/linux/*.c)
	ZZ_SRCS += $(wildcard $(ZZ_SRCS_PATH)/platforms/backend-linux/*.c)

	ZZ_CFLAGS := -g -fPIC -shared
	ZZ_DLL := lib$(HOOKZZ_NAME).so

	ZZ_GCC_SOURCE := $(ZZ_GCC_BIN) --sysroot=$(ZZ_SDK_ROOT)
	ZZ_GXX_SOURCE := $(ZZ_GXX_BIN) --sysroot=$(ZZ_SDK_ROOT)
	ZZ_GCC_TEST := $(ZZ_GCC_BIN) --sysroot=$(ZZ_SDK_ROOT)
	ZZ_GXX_TEST := $(ZZ_GXX_BIN) --sysroot=$(ZZ_SDK_ROOT)
endif

ZZ_SRCS += $(wildcard $(ZZ_SRCS_PATH)/platforms/arch-$(ARCH)/*.c) \
		$(wildcard $(ZZ_SRCS_PATH)/platforms/backend-$(ARCH)/*.c)

ifeq ($(ARCH), arm64)
ZZ_SS += $(wildcard $(ZZ_SRCS_PATH)/platforms/backend-$(ARCH)/*.s)
endif
	
ZZ_EXPORT_INCLUDE := -I$(LOCAL_PATH)/include

ZZ_SRCS_INCLUDE := $(ZZ_EXPORT_INCLUDE) \
		-I$(ZZ_SRCS_PATH)

ZZ_DEPS_OBJS := $(ZZ_DEPS_SRCS:.c=.o)

OUTPUT_DIR := $(OUTPUT_DIR)/$(ZZ_BACKEND)-$(ZZ_ARCH)

ZZ_GCC_SOURCE += $(ZZ_SRCS_INCLUDE)
ZZ_GCC_TEST += $(ZZ_INCLUDE)


LDFLAGS += $(ZZ_LIB)

ZZ_SS_OBJS := $(ZZ_SS:.s=.o)
ZZ_SRCS_OBJS := $(ZZ_SRCS:.c=.o)
ZZ_OBJS := $(ZZ_SRCS_OBJS) $(ZZ_DEPS_OBJS) $(ZZ_SS_OBJS)

# ATTENTION !!!
# 1. simple `ar` can't make a 'static library', need `ar -x` to extract `libcapstone.ios.arm64.a` and then `ar rcs` to pack as `.a`
# 2. must `rm -rf  $(OUTPUT_DIR)/libhookzz.static.a`, very important!!!
$(HOOKZZ_NAME) : $(ZZ_OBJS)
	@mkdir -p $(OUTPUT_DIR)
	@rm -rf $(OUTPUT_DIR)/*

	@$(ZZ_GCC_SOURCE) $(ZZ_CFLAGS) $(CFLAGS) $(LDFLAGS) $(ZZ_OBJS) -o $(OUTPUT_DIR)/$(ZZ_DLL)
	@$(ZZ_AR_BIN) -rcs $(OUTPUT_DIR)/lib$(HOOKZZ_NAME).static.a $(ZZ_OBJS)

	@echo "$(OK_COLOR)build success for $(ARCH)-$(BACKEND)-hookzz! $(NO_COLOR)"

$(ZZ_SRCS_OBJS): %.o : %.c
	@$(ZZ_GCC_SOURCE) $(CFLAGS) -c $< -o $@
	@echo "$(OK_COLOR)generate [$@]! $(NO_COLOR)"

$(ZZ_DEPS_OBJS): %.o : %.c
	@$(ZZ_GCC_SOURCE) $(CFLAGS) -c $< -o $@
	@echo "$(OK_COLOR)generate [$@]! $(NO_COLOR)"

$(ZZ_SS_OBJS): %.o : %.s
	@$(ZZ_GCC_SOURCE) $(CFLAGS) -c $< -o $@
	@echo "$(OK_COLOR)generate [$@]! $(NO_COLOR)"

clean:
	@rm -rf $(shell find ./src -name "*\.o" | xargs echo)
	@rm -rf $(OUTPUT_DIR)
	@echo "$(OK_COLOR)clean all *.o success!$(NO_COLOR)"