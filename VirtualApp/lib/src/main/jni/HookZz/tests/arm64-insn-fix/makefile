NO_COLOR=\x1b[0m
OK_COLOR=\x1b[32;01m
ERROR_COLOR=\x1b[31;01m
WARN_COLOR=\x1b[33;01m


HOOKZZ_INCLUDE_DIR := -I$(abspath ../../include) -I$(abspath ../../src)
HOOKZZ_LIB_DIR := -L$(abspath ../../build/ios-arm64)

ZZ_GCC_TEST := $(shell xcrun --sdk iphoneos --find clang) -isysroot $(shell xcrun --sdk iphoneos --show-sdk-path) -arch arm64 -O0 -g

# -undefined dynamic_lookup
test: 
	@$(ZZ_GCC_TEST) $(HOOKZZ_INCLUDE_DIR) -c test_insn_fix.c -o test_insn_fix.o
	@$(ZZ_GCC_TEST) -dynamiclib $(HOOKZZ_LIB_DIR) -lhookzz.static test_insn_fix.o -o test_insn_fix.dylib
	@echo "$(OK_COLOR)build [test_insn_fix.dylib] success for arm64! $(NO_COLOR)"
clean:
	rm -rf test_insn_fix.o
	rm -rf test_insn_fix.dylib
