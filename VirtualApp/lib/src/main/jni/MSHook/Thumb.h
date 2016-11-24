/*
 * Thumb.h
 *
 *  Created on: 2016��2��22��
 *      Author: peng
 */

#ifndef THUMB_H_
#define THUMB_H_

#include "Debug.h"
#include "Log.h"
#include "PosixMemory.h"
#include <stdlib.h>
#include <errno.h>
#include <sys/mman.h>

#define T$Label(l, r) \
	(((r) - (l)) * 2 - 4 + ((l) % 2 == 0 ? 0 : 2))
#define T$pop_$r0$ 0xbc01 // pop {r0}
#define T$b(im) /* b im */ \
	(0xde00 | (im & 0xff))
#define T$blx(rm) /* blx rm */ \
	(0x4780 | (rm << 3))
#define T$bx(rm) /* bx rm */ \
	(0x4700 | (rm << 3))
#define T$nop /* nop */ \
	(0x46c0)
#define T$add_rd_rm(rd, rm) /* add rd, rm */ \
	(0x4400 | (((rd) & 0x8) >> 3 << 7) | (((rm) & 0x8) >> 3 << 6) | (((rm) & 0x7) << 3) | ((rd) & 0x7))
#define T$push_r(r) /* push r... */ \
	(0xb400 | (((r) & (1 << A$lr)) >> A$lr << 8) | ((r) & 0xff))
#define T$pop_r(r) /* pop r... */ \
	(0xbc00 | (((r) & (1 << A$pc)) >> A$pc << 8) | ((r) & 0xff))
#define T$mov_rd_rm(rd, rm) /* mov rd, rm */ \
	(0x4600 | (((rd) & 0x8) >> 3 << 7) | (((rm) & 0x8) >> 3 << 6) | (((rm) & 0x7) << 3) | ((rd) & 0x7))
#define T$ldr_rd_$rn_im_4$(rd, rn, im) /* ldr rd, [rn, #im * 4] */ \
	(0x6800 | (((im) & 0x1f) << 6) | ((rn) << 3) | (rd))
#define T$ldr_rd_$pc_im_4$(rd, im) /* ldr rd, [PC, #im * 4] */ \
	(0x4800 | ((rd) << 8) | ((im) & 0xff))
#define T$cmp_rn_$im(rn, im) /* cmp rn, #im */ \
	(0x2000 | ((rn) << 8) | ((im) & 0xff))
#define T$it$_cd(cd, ms) /* it<ms>, cd */ \
	(0xbf00 | ((cd) << 4) | (ms))
#define T$cbz$_rn_$im(op,rn,im) /* cb<op>z rn, #im */ \
	(0xb100 | ((op) << 11) | (((im) & 0x40) >> 6 << 9) | (((im) & 0x3e) >> 1 << 3) | (rn))
#define T$b$_$im(cond,im) /* b<cond> #im */ \
	(cond == A$al ? 0xe000 | (((im) >> 1) & 0x7ff) : 0xd000 | ((cond) << 8) | (((im) >> 1) & 0xff))
#define T1$ldr_rt_$rn_im$(rt, rn, im) /* ldr rt, [rn, #im] */ \
	(0xf850 | ((im < 0 ? 0 : 1) << 7) | (rn))
#define T2$ldr_rt_$rn_im$(rt, rn, im) /* ldr rt, [rn, #im] */ \
	(((rt) << 12) | abs(im))
#define T1$mrs_rd_apsr(rd) /* mrs rd, apsr */ \
	(0xf3ef)
#define T2$mrs_rd_apsr(rd) /* mrs rd, apsr */ \
	(0x8000 | ((rd) << 8))
#define T1$msr_apsr_nzcvqg_rn(rn) /* msr apsr, rn */ \
	(0xf380 | (rn))
#define T2$msr_apsr_nzcvqg_rn(rn) /* msr apsr, rn */ \
	(0x8c00)
#define T$msr_apsr_nzcvqg_rn(rn) /* msr apsr, rn */ \
	(T2$msr_apsr_nzcvqg_rn(rn) << 16 | T1$msr_apsr_nzcvqg_rn(rn))
#define A$ldr_rd_$rn_im$(rd, rn, im) /* ldr rd, [rn, #im] */ \
    (0xe5100000 | ((im) < 0 ? 0 : 1 << 23) | ((rn) << 16) | ((rd) << 12) | abs(im))

static inline bool T$32bit$i(uint16_t ic) {
	return ((ic & 0xe000) == 0xe000 && (ic & 0x1800) != 0x0000);
}

static inline bool T$pcrel$cbz(uint16_t ic) {
	return (ic & 0xf500) == 0xb100;
}

static inline bool T$pcrel$b(uint16_t ic) {
	return (ic & 0xf000) == 0xd000 && (ic & 0x0e00) != 0x0e00;
}

static inline bool T2$pcrel$b(uint16_t *ic) {
	return (ic[0] & 0xf800) == 0xf000 && ((ic[1] & 0xd000) == 0x9000 || ((ic[1] & 0xd000) == 0x8000 && (ic[0] & 0x0380) != 0x0380));
}

static inline bool T$pcrel$bl(uint16_t *ic) {
	return (ic[0] & 0xf800) == 0xf000 && ((ic[1] & 0xd000) == 0xd000 || (ic[1] & 0xd001) == 0xc000);
}

static inline bool T$pcrel$ldr(uint16_t ic) {
	return (ic & 0xf800) == 0x4800;
}

static inline bool T$pcrel$add(uint16_t ic) {
	return (ic & 0xff78) == 0x4478;
}

static inline bool T$pcrel$ldrw(uint16_t ic) {
	return (ic & 0xff7f) == 0xf85f;
}

static size_t MSGetInstructionWidthThumb(void *start) {
	uint16_t *thumb(reinterpret_cast<uint16_t *>(start));
	return T$32bit$i(thumb[0]) ? 4 : 2;
}

static size_t MSGetInstructionWidthARM(void *start) {
	return 4;
}

namespace Thumb{
	static size_t MSGetInstructionWidth(void *start);
	extern "C" void SubstrateHookFunctionThumb(SubstrateProcessRef process, void *symbol, void *replace, void **result);
}
#endif /* THUMB_H_ */
