NO_COLOR=\x1b[0m
OK_COLOR=\x1b[32;01m
ERROR_COLOR=\x1b[31;01m
WARN_COLOR=\x1b[33;01m

HOOKZZ_INCLUDE_DIR := $(abspath ../../include)
HOOKZZ_LIB_DIR := $(abspath ../../build/android-armv7)

CFLAGS ?= -O0 -g -std=c99 -pie -fPIE

HOST ?= $(shell uname -s)
HOST_ARCH ?= $(shell uname -m)

ARCH := arm

ifeq ($(ARCH), arm)
	ZZ_ARCH := armv7
	ZZ_API_LEVEL := android-19
	ZZ_CROSS_PREFIX := arm-linux-androideabi-
else ifeq ($(ARCH), arm64)
	ZZ_ARCH := arm64
	ZZ_API_LEVEL := android-21
	ZZ_CROSS_PREFIX := aarch64-linux-android-
endif

HOST_DIR := $(shell echo $(HOST) | tr A-Z a-z)-$(HOST_ARCH)
ZZ_NDK_HOME := $(shell dirname `which ndk-build`)
ZZ_SDK_ROOT := $(ZZ_NDK_HOME)/platforms/$(ZZ_API_LEVEL)/arch-$(ARCH)
ZZ_GCC_BIN := $(ZZ_NDK_HOME)/toolchains/$(ZZ_CROSS_PREFIX)4.9/prebuilt/$(HOST_DIR)/bin/$(ZZ_CROSS_PREFIX)gcc
ZZ_GXX_BIN := $(ZZ_NDK_HOME)/toolchains/$(ZZ_CROSS_PREFIX)4.9/prebuilt/$(HOST_DIR)/bin/$(ZZ_CROSS_PREFIX)g++
ZZ_AR_BIN := $(ZZ_NDK_HOME)/toolchains/$(ZZ_CROSS_PREFIX)4.9/prebuilt/$(HOST_DIR)/bin/$(ZZ_CROSS_PREFIX)ar
ZZ_RANLIB_BIN := $(ZZ_NDK_HOME)/toolchains/$(ZZ_CROSS_PREFIX)4.9/prebuilt/$(HOST_DIR)/bin/$(ZZ_CROSS_PREFIX)ranlib

ZZ_GCC_SOURCE := $(ZZ_GCC_BIN) --sysroot=$(ZZ_SDK_ROOT)
ZZ_GXX_SOURCE := $(ZZ_GXX_BIN) --sysroot=$(ZZ_SDK_ROOT)
ZZ_GCC_TEST := $(ZZ_GCC_BIN) --sysroot=$(ZZ_SDK_ROOT)
ZZ_GXX_TEST := $(ZZ_GXX_BIN) --sysroot=$(ZZ_SDK_ROOT)

test: 
	@$(ZZ_GCC_TEST) $(CFLAGS) -I$(HOOKZZ_INCLUDE_DIR) -c test_hook_open_arm.c -o test_hook_open_arm.o
	@$(ZZ_GCC_TEST) $(CFLAGS) -I$(HOOKZZ_INCLUDE_DIR) test_hook_open_arm.o  -L$(HOOKZZ_LIB_DIR) -lhookzz.static -o $(HOOKZZ_LIB_DIR)/test_hook_open_arm
	@echo "$(OK_COLOR)build [test_hook_open_arm.dylib] success for armv7-ios! $(NO_COLOR)"

	@$(ZZ_GCC_TEST) $(CFLAGS) -I$(HOOKZZ_INCLUDE_DIR) -c test_hook_address_thumb.c -o test_hook_address_thumb.o
	@$(ZZ_GCC_TEST) $(CFLAGS) -I$(HOOKZZ_INCLUDE_DIR) test_hook_address_thumb.o  -L$(HOOKZZ_LIB_DIR) -lhookzz.static -o $(HOOKZZ_LIB_DIR)/test_hook_address_thumb
	@echo "$(OK_COLOR)build [test_hook_address_thumb.dylib] success for armv7-ios! $(NO_COLOR)"

	@$(ZZ_GCC_TEST) $(CFLAGS) -I$(HOOKZZ_INCLUDE_DIR) -c test_hook_printf.c -o test_hook_printf.o
	@$(ZZ_GCC_TEST) $(CFLAGS) -I$(HOOKZZ_INCLUDE_DIR) test_hook_printf.o  -L$(HOOKZZ_LIB_DIR) -lhookzz.static -o $(HOOKZZ_LIB_DIR)/test_hook_printf
	@echo "$(OK_COLOR)build [test_hook_printf.dylib] success for armv7-ios! $(NO_COLOR)"

	@echo "$(OK_COLOR)build [test] success for armv7-android-hookzz! $(NO_COLOR)"

clean:
	@rm -rf $(shell find ./ -name "*\.o" | xargs echo)
	@rm -rf $(shell find $(HOOKZZ_LIB_DIR) -name "test_*" | xargs echo)
	@echo "$(OK_COLOR)clean all *.o success!$(NO_COLOR)"