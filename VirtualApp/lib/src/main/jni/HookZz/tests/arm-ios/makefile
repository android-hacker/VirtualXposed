NO_COLOR=\x1b[0m
OK_COLOR=\x1b[32;01m
ERROR_COLOR=\x1b[31;01m
WARN_COLOR=\x1b[33;01m


HOOKZZ_INCLUDE_DIR := $(abspath ../../include)
HOOKZZ_LIB_DIR := $(abspath ../../build/ios-armv7)

ZZ_GCC_TEST := $(shell xcrun --sdk iphoneos --find clang) -isysroot $(shell xcrun --sdk iphoneos --show-sdk-path) -arch armv7 -O0 -g

# -undefined dynamic_lookup
test: 
	@$(ZZ_GCC_TEST) -I$(HOOKZZ_INCLUDE_DIR) -c test_hook_oc_thumb.m -o test_hook_oc_thumb.o
	@$(ZZ_GCC_TEST) -dynamiclib  -Wl,-U,_func -framework Foundation -L$(HOOKZZ_LIB_DIR) -lhookzz.static test_hook_oc_thumb.o -arch armv7 -o $(HOOKZZ_LIB_DIR)/test_hook_oc_thumb.dylib
	@echo "$(OK_COLOR)build [test_hook_oc_thumb.dylib] success for armv7-ios! $(NO_COLOR)"

	@$(ZZ_GCC_TEST) -I$(HOOKZZ_INCLUDE_DIR) -c test_hook_open_arm.c -o test_hook_open_arm.o
	@$(ZZ_GCC_TEST) -dynamiclib -Wl,-U,_func -framework Foundation -L$(HOOKZZ_LIB_DIR) -lhookzz.static test_hook_open_arm.o -o $(HOOKZZ_LIB_DIR)/test_hook_open_arm.dylib
	@echo "$(OK_COLOR)build [test_hook_open_arm.dylib] success for armv7-ios! $(NO_COLOR)"

	@$(ZZ_GCC_TEST) -I$(HOOKZZ_INCLUDE_DIR) -c test_hook_address_thumb.c -o test_hook_address_thumb.o
	@$(ZZ_GCC_TEST) -dynamiclib -Wl,-U,_func -framework Foundation -L$(HOOKZZ_LIB_DIR) -lhookzz.static test_hook_address_thumb.o -o $(HOOKZZ_LIB_DIR)/test_hook_address_thumb.dylib
	@echo "$(OK_COLOR)build [test_hook_address_thumb.dylib] success for armv7-ios! $(NO_COLOR)"

	@$(ZZ_GCC_TEST) -I$(HOOKZZ_INCLUDE_DIR) -c test_hook_printf.c -o test_hook_printf.o
	@$(ZZ_GCC_TEST) -dynamiclib -Wl,-U,_func -framework Foundation -L$(HOOKZZ_LIB_DIR) -lhookzz.static test_hook_printf.o -o $(HOOKZZ_LIB_DIR)/test_hook_printf.dylib
	@echo "$(OK_COLOR)build [test_hook_printf.dylib] success for armv7-ios! $(NO_COLOR)"


	@$(ZZ_GCC_TEST) -I$(HOOKZZ_INCLUDE_DIR) -c test_hook_freeaddr.c -o test_hook_freeaddr.o
	@$(ZZ_GCC_TEST) -dynamiclib -Wl,-U,_func -framework Foundation -L$(HOOKZZ_LIB_DIR) -lhookzz.static test_hook_freeaddr.o -o $(HOOKZZ_LIB_DIR)/test_hook_freeaddr.dylib
	@echo "$(OK_COLOR)build [test_hook_freeaddr.dylib] success for armv7-ios! $(NO_COLOR)"


	@echo "$(OK_COLOR)build [test] success for armv7-ios-hookzz! $(NO_COLOR)"

clean:
	@rm -rf $(shell find ./ -name "*\.o" | xargs echo)
	@rm -rf $(shell find $(HOOKZZ_LIB_DIR) -name "test_*" | xargs echo)
	@echo "$(OK_COLOR)clean all *.o success!$(NO_COLOR)"