/*
`xcrun --sdk iphoneos --find clang` \
-fPIC -shared -dynamiclib \
-arch arm64 \
-isysroot `xcrun --sdk iphoneos --show-sdk-path` \
solidifytrampoline.c \
-o  solidifytrampoline.dylib
*/

__attribute__((__naked__)) void on_enter_trampoline_template() {
    __asm__ volatile(
        /* store entry address and reserve space for next hop */
        "sub sp, sp, 0x10\n"

        /* push x16 */
        "str x16,[sp, #-0x10]\n"
        "adr x16, #0\n"
        "ldr x17, #0x8\n"
        "b #0xc\n"
        /* entry address'address offset!!! */
        ".long 0x0\n"
        ".long 0x0\n"
        "add x17, x17, x16\n"
        /* pop x16 */
        "ldr x16, [sp], #0x10\n"
        "ldr x17, [x17]\n"

        "str x17, [sp]\n"

        /* push x16 */
        "str x16,[sp, #-0x10]\n"
        "adr x16, #0\n"
        "ldr x17, #0x8\n"
        "b #0xc\n"
        /* enter_thunk address'address offset!!! */
        ".long 0x0\n"
        ".long 0x0\n"
        "add x17, x17, x16\n"
        /* pop x16 */
        "ldr x16, [sp], #0x10\n"
        "ldr x17, [x17]\n"

        "br x17");
}

__attribute__((__naked__)) void on_inovke_trampoline_template() {
    __asm__ volatile(
        /* fixed instruction */
        "nop\n"
        "nop\n"
        "nop\n"
        "nop\n"
        "nop\n"
        "nop\n"
        "nop\n"
        "nop\n"

        /* push x16 */
        "str x16,[sp, #-0x10]\n"
        "adr x16, #0\n"
        "ldr x17, #0x8\n"
        "b #0xc\n"
        /* rest of orgin function address'address offset !!! */
        ".long 0x0\n"
        ".long 0x0\n"
        "add x17, x17, x16\n"
        /* pop x16 */
        "ldr x16, [sp], #0x10\n"
        "ldr x17, [x17]\n"

        "br x17");
}

__attribute__((__naked__)) void on_leave_trampoline_template() {
    __asm__ volatile(
        /* store entry address and reserve space for next hop */
        "sub sp, sp, 0x10\n"

        /* push x16 */
        "str x16,[sp, #-0x10]\n"
        "adr x16, #0\n"
        "ldr x17, #0x8\n"
        "b #0xc\n"
        /* entry address'address offset!!! */
        ".long 0x0\n"
        ".long 0x0\n"
        "add x17, x17, x16\n"
        /* pop x16 */
        "ldr x16, [sp], #0x10\n"
        "ldr x17, [x17]\n"

        "str x17, [sp]\n"

        /* push x16 */
        "str x16,[sp, #-0x10]\n"
        "adr x16, #0\n"
        "ldr x17, #0x8\n"
        "b #0xc\n"
        /* leave_thunk address'address offset!!! */
        ".long 0x0\n"
        ".long 0x0\n"
        "add x17, x17, x16\n"
        /* pop x16 */
        "ldr x16, [sp], #0x10\n"
        "ldr x17, [x17]\n"

        "br x17");
}