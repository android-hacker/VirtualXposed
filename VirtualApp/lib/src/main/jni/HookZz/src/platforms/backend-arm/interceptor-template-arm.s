// .section	__TEXT,__text,regular,pure_instructions
// .ios_version_min 11, 0
.align 4
.globl _ctx_save
.globl _ctx_restore
.globl _enter_thunk_template
.globl _leave_thunk_template
.globl _on_enter_trampoline_template
.globl _on_invoke_trampoline_template
.globl _on_leave_trampoline_template



_on_enter_trampoline_template:
	sub sp, #0xc
	str r1, [sp, #0x0]
	ldr r1, [pc, #0x0]
	b #0x2
	.long 0x0
	.long 0x0
	str r1, [sp, #0x4]
	ldr r1, [sp, #0x0]
	add sp, #0x4
	ldr.w pc, [pc, #0x2]
