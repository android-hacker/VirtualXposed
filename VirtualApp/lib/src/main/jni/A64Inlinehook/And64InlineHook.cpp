/*
 *  @date   : 2018/04/18
 *  @author : Rprop (r_prop@outlook.com)
 *  https://github.com/Rprop/And64InlineHook
 */
/*
 MIT License

 Copyright (c) 2018 Rprop (r_prop@outlook.com)

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
#define  __STDC_FORMAT_MACROS

#if defined(__aarch64__)

#include <string.h>
#include <stdlib.h>
#include <sys/mman.h>
#include "And64InlineHook.hpp"
#define   A64_MAX_INSTRUCTIONS 5
#define   A64_MAX_REFERENCES   (A64_MAX_INSTRUCTIONS * 2)
#define   A64_NOP              0xd503201fu
#define   A64_JNIEXPORT        __attribute__((visibility("default")))
#define __flush_cache(c, n)        __builtin___clear_cache(reinterpret_cast<char *>(c), reinterpret_cast<char *>(c) + n)


typedef uint32_t *__restrict *__restrict instruction;
typedef struct
{
    struct fix_info
    {
        uint32_t *bp;
        uint32_t  ls; // left-shift counts
        uint32_t  ad; // & operand
    };
    struct insns_info
    {
        union
        {
            uint64_t insu;
            int64_t  ins;
            void    *insp;
        };
        fix_info fmap[A64_MAX_REFERENCES];
    };
    int64_t    basep;
    int64_t    endp;
    insns_info dat[A64_MAX_INSTRUCTIONS];

public:
    inline bool is_in_fixing_range(const int64_t absolute_addr) {
        return absolute_addr >= this->basep && absolute_addr < this->endp;
    }
    inline intptr_t get_ref_ins_index(const int64_t absolute_addr) {
        return static_cast<intptr_t>((absolute_addr - this->basep) / sizeof(uint32_t));
    }
    inline intptr_t get_and_set_current_index(uint32_t *__restrict inp, uint32_t *__restrict outp) {
        intptr_t current_idx = this->get_ref_ins_index(reinterpret_cast<int64_t>(inp));
        this->dat[current_idx].insp = outp;
        return current_idx;
    }
    inline void reset_current_ins(const intptr_t idx, uint32_t *__restrict outp) {
        this->dat[idx].insp = outp;
    }
    void insert_fix_map(const intptr_t idx, uint32_t *bp, uint32_t ls = 0u, uint32_t ad = 0xffffffffu) {
        for (auto &f : this->dat[idx].fmap) {
            if (f.bp == NULL) {
                f.bp = bp;
                f.ls = ls;
                f.ad = ad;
                return;
            } //if
        }
        // What? GGing..
    }
    void process_fix_map(const intptr_t idx) {
        for (auto &f : this->dat[idx].fmap) {
            if (f.bp == NULL) break;
            *(f.bp) = *(f.bp) | (((int32_t(this->dat[idx].ins - reinterpret_cast<int64_t>(f.bp)) >> 2) << f.ls) & f.ad);
            f.bp = NULL;
        }
    }
} context;

//-------------------------------------------------------------------------

static bool __fix_branch_imm(instruction inpp, instruction outpp, context *ctxp)
{
    static constexpr uint32_t mbits = 6u;
    static constexpr uint32_t mask  = 0xfc000000u; // 0b11111100000000000000000000000000
    static constexpr uint32_t rmask = 0x03ffffffu; // 0b00000011111111111111111111111111
    static constexpr uint32_t op_b  = 0x14000000u; // "b"  ADDR_PCREL26
    static constexpr uint32_t op_bl = 0x94000000u; // "bl" ADDR_PCREL26

    const uint32_t ins = *(*inpp);
    const uint32_t opc = ins & mask;
    switch (opc) {
        case op_b:
        case op_bl:
        {
            intptr_t current_idx  = ctxp->get_and_set_current_index(*inpp, *outpp);
            int64_t absolute_addr = reinterpret_cast<int64_t>(*inpp) + (static_cast<int32_t>(ins << mbits) >> (mbits - 2u)); // sign-extended
            int64_t new_pc_offset = static_cast<int64_t>(absolute_addr - reinterpret_cast<int64_t>(*outpp)) >> 2; // shifted
            bool special_fix_type = ctxp->is_in_fixing_range(absolute_addr);
            // whether the branch should be converted to absolute jump
            if (!special_fix_type && llabs(new_pc_offset) >= (rmask >> 1)) {
                bool b_aligned = (reinterpret_cast<uint64_t>(*outpp + 2) & 7u) == 0u;
                if (opc == op_b) {
                    if (b_aligned != true) {
                        (*outpp)[0] = A64_NOP;
                        ctxp->reset_current_ins(current_idx, ++(*outpp));
                    } //if
                    (*outpp)[0] = 0x58000051u; // LDR X17, #0x8
                    (*outpp)[1] = 0xd61f0220u; // BR X17
                    memcpy(*outpp + 2, &absolute_addr, sizeof(absolute_addr));
                    *outpp += 4;
                } else {
                    if (b_aligned == true) {
                        (*outpp)[0] = A64_NOP;
                        ctxp->reset_current_ins(current_idx, ++(*outpp));
                    } //if
                    (*outpp)[0] = 0x58000071u; // LDR X17, #12
                    (*outpp)[1] = 0x1000009eu; // ADR X30, #16
                    (*outpp)[2] = 0xd61f0220u; // BR X17
                    memcpy(*outpp + 3, &absolute_addr, sizeof(absolute_addr));
                    *outpp += 5;
                } //if
            } else {
                if (special_fix_type) {
                    intptr_t ref_idx = ctxp->get_ref_ins_index(absolute_addr);
                    if (ref_idx <= current_idx) {
                        new_pc_offset = static_cast<int64_t>(ctxp->dat[ref_idx].ins - reinterpret_cast<int64_t>(*outpp)) >> 2;
                    } else {
                        ctxp->insert_fix_map(ref_idx, *outpp, 0u, rmask);
                        new_pc_offset = 0;
                    } //if
                } //if

                (*outpp)[0] = opc | (new_pc_offset & ~mask);
                ++(*outpp);
            } //if

            ++(*inpp);
            return ctxp->process_fix_map(current_idx), true;
        }
    }
    return false;
}

//-------------------------------------------------------------------------

static bool __fix_cond_comp_test_branch(instruction inpp, instruction outpp, context *ctxp)
{
    static constexpr uint32_t lsb     = 5u;
    static constexpr uint32_t lmask01 = 0xff00001fu; // 0b11111111000000000000000000011111
    static constexpr uint32_t mask0   = 0xff000010u; // 0b11111111000000000000000000010000
    static constexpr uint32_t op_bc   = 0x54000000u; // "b.c"  ADDR_PCREL19
    static constexpr uint32_t mask1   = 0x7f000000u; // 0b01111111000000000000000000000000
    static constexpr uint32_t op_cbz  = 0x34000000u; // "cbz"  Rt, ADDR_PCREL19
    static constexpr uint32_t op_cbnz = 0x35000000u; // "cbnz" Rt, ADDR_PCREL19
    static constexpr uint32_t lmask2  = 0xfff8001fu; // 0b11111111111110000000000000011111
    static constexpr uint32_t mask2   = 0x7f000000u; // 0b01111111000000000000000000000000
    static constexpr uint32_t op_tbz  = 0x36000000u; // 0b00110110000000000000000000000000 "tbz"  Rt, BIT_NUM, ADDR_PCREL14
    static constexpr uint32_t op_tbnz = 0x37000000u; // 0b00110111000000000000000000000000 "tbnz" Rt, BIT_NUM, ADDR_PCREL14

    const uint32_t ins = *(*inpp);
    uint32_t     lmask = lmask01;
    if ((ins & mask0) != op_bc) {
        uint32_t opc   = ins & mask1;
        if (opc != op_cbz && opc != op_cbnz) {
            opc = ins & mask2;
            if (opc != op_tbz && opc != op_tbnz) {
                return false;
            } //if
            lmask = lmask2;
        } //if
    } //if

    intptr_t current_idx  = ctxp->get_and_set_current_index(*inpp, *outpp);
    int64_t absolute_addr = reinterpret_cast<int64_t>(*inpp) + ((ins & ~lmask) >> (lsb - 2u));
    int64_t new_pc_offset = static_cast<int64_t>(absolute_addr - reinterpret_cast<int64_t>(*outpp)) >> 2; // shifted
    bool special_fix_type = ctxp->is_in_fixing_range(absolute_addr);
    if (!special_fix_type && llabs(new_pc_offset) >= (~lmask >> (lsb + 1))) {
        if ((reinterpret_cast<uint64_t>(*outpp + 4) & 7u) != 0u) {
            (*outpp)[0] = A64_NOP;
            ctxp->reset_current_ins(current_idx, ++(*outpp));
        } //if
        (*outpp)[0] = (((8u >> 2u) << lsb) & ~lmask) | (ins & lmask); // B.C #0x8
        (*outpp)[1] = 0x14000005u; // B #0x14
        (*outpp)[2] = 0x58000051u; // LDR X17, #0x8
        (*outpp)[3] = 0xd61f0220u; // BR X17
        memcpy(*outpp + 4, &absolute_addr, sizeof(absolute_addr));
        *outpp += 6;
    } else {
        if (special_fix_type) {
            intptr_t ref_idx = ctxp->get_ref_ins_index(absolute_addr);
            if (ref_idx <= current_idx) {
                new_pc_offset = static_cast<int64_t>(ctxp->dat[ref_idx].ins - reinterpret_cast<int64_t>(*outpp)) >> 2;
            } else {
                ctxp->insert_fix_map(ref_idx, *outpp, lsb, ~lmask);
                new_pc_offset = 0;
            } //if
        } //if

        (*outpp)[0] = (static_cast<uint32_t>(new_pc_offset << lsb) & ~lmask) | (ins & lmask);
        ++(*outpp);
    } //if

    ++(*inpp);
    return ctxp->process_fix_map(current_idx), true;
}

//-------------------------------------------------------------------------

static bool __fix_loadlit(instruction inpp, instruction outpp, context *ctxp)
{
    const uint32_t ins = *(*inpp);

    // memory prefetch("prfm"), just skip it
    // http://infocenter.arm.com/help/topic/com.arm.doc.100069_0608_00_en/pge1427897420050.html
    if ((ins & 0xff000000u) == 0xd8000000u) {
        ctxp->process_fix_map(ctxp->get_and_set_current_index(*inpp, *outpp));
        ++(*inpp);
        return true;
    } //if

    static constexpr uint32_t msb        = 8u;
    static constexpr uint32_t lsb        = 5u;
    static constexpr uint32_t mask_30    = 0x40000000u; // 0b01000000000000000000000000000000
    static constexpr uint32_t mask_31    = 0x80000000u; // 0b10000000000000000000000000000000
    static constexpr uint32_t lmask      = 0xff00001fu; // 0b11111111000000000000000000011111
    static constexpr uint32_t mask_ldr   = 0xbf000000u; // 0b10111111000000000000000000000000
    static constexpr uint32_t op_ldr     = 0x18000000u; // 0b00011000000000000000000000000000 "LDR Wt/Xt, label" | ADDR_PCREL19
    static constexpr uint32_t mask_ldrv  = 0x3f000000u; // 0b00111111000000000000000000000000
    static constexpr uint32_t op_ldrv    = 0x1c000000u; // 0b00011100000000000000000000000000 "LDR St/Dt/Qt, label" | ADDR_PCREL19
    static constexpr uint32_t mask_ldrsw = 0xff000000u; // 0b11111111000000000000000000000000
    static constexpr uint32_t op_ldrsw   = 0x98000000u; // "LDRSW Xt, label" | ADDR_PCREL19 | load register signed word
    // LDR S0, #0 | 0b00011100000000000000000000000000 | 32-bit
    // LDR D0, #0 | 0b01011100000000000000000000000000 | 64-bit
    // LDR Q0, #0 | 0b10011100000000000000000000000000 | 128-bit
    // INVALID    | 0b11011100000000000000000000000000 | may be 256-bit

    uint32_t  mask     = mask_ldr;
    uintptr_t faligned = (ins & mask_30) ? 7u : 3u;
    if ((ins & mask_ldr) != op_ldr) {
        mask = mask_ldrv;
        if (faligned != 7u)
            faligned = (ins & mask_31) ? 15u : 3u;
        if ((ins & mask_ldrv) != op_ldrv) {
            if ((ins & mask_ldrsw) != op_ldrsw) {
                return false;
            } //if
            mask     = mask_ldrsw;
            faligned = 7u;
        } //if
    } //if

    intptr_t current_idx  = ctxp->get_and_set_current_index(*inpp, *outpp);
    int64_t absolute_addr = reinterpret_cast<int64_t>(*inpp) + ((static_cast<int32_t>(ins << msb) >> (msb + lsb - 2u)) & ~3u);
    int64_t new_pc_offset = static_cast<int64_t>(absolute_addr - reinterpret_cast<int64_t>(*outpp)) >> 2; // shifted
    bool special_fix_type = ctxp->is_in_fixing_range(absolute_addr);
    // special_fix_type may encounter issue when there are mixed data and code
    if (special_fix_type || (llabs(new_pc_offset) + (faligned + 1u - 4u) / 4u) >= (~lmask >> (lsb + 1))) { // inaccurate, but it works
        while ((reinterpret_cast<uint64_t>(*outpp + 2) & faligned) != 0u) {
            *(*outpp)++ = A64_NOP;
        }
        ctxp->reset_current_ins(current_idx, *outpp);

        // Note that if memory at absolute_addr is writeable (non-const), we will fail to fetch it.
        // And what's worse, we may unexpectedly overwrite something if special_fix_type is true...
        uint32_t ns = static_cast<uint32_t>((faligned + 1) / sizeof(uint32_t));
        (*outpp)[0] = (((8u >> 2u) << lsb) & ~mask) | (ins & lmask); // LDR #0x8
        (*outpp)[1] = 0x14000001u + ns; // B #0xc
        memcpy(*outpp + 2, reinterpret_cast<void *>(absolute_addr), faligned + 1);
        *outpp += 2 + ns;
    } else {
        faligned >>= 2; // new_pc_offset is shifted and 4-byte aligned
        while ((new_pc_offset & faligned) != 0) {
            *(*outpp)++   = A64_NOP;
            new_pc_offset = static_cast<int64_t>(absolute_addr - reinterpret_cast<int64_t>(*outpp)) >> 2;
        }
        ctxp->reset_current_ins(current_idx, *outpp);

        (*outpp)[0] = (static_cast<uint32_t>(new_pc_offset << lsb) & ~mask) | (ins & lmask);
        ++(*outpp);
    } //if

    ++(*inpp);
    return ctxp->process_fix_map(current_idx), true;
}

//-------------------------------------------------------------------------

static bool __fix_pcreladdr(instruction inpp, instruction outpp, context *ctxp)
{
    // Load a PC-relative address into a register
    // http://infocenter.arm.com/help/topic/com.arm.doc.100069_0608_00_en/pge1427897645644.html
    static constexpr uint32_t msb     = 8u;
    static constexpr uint32_t lsb     = 5u;
    static constexpr uint32_t mask    = 0x9f000000u; // 0b10011111000000000000000000000000
    static constexpr uint32_t rmask   = 0x0000001fu; // 0b00000000000000000000000000011111
    static constexpr uint32_t lmask   = 0xff00001fu; // 0b11111111000000000000000000011111
    static constexpr uint32_t fmask   = 0x00ffffffu; // 0b00000000111111111111111111111111
    static constexpr uint32_t max_val = 0x001fffffu; // 0b00000000000111111111111111111111
    static constexpr uint32_t op_adr  = 0x10000000u; // "adr"  Rd, ADDR_PCREL21
    static constexpr uint32_t op_adrp = 0x90000000u; // "adrp" Rd, ADDR_ADRP

    const uint32_t ins = *(*inpp);
    intptr_t current_idx;
    switch (ins & mask) {
        case op_adr:
        {
            current_idx           = ctxp->get_and_set_current_index(*inpp, *outpp);
            int64_t lsb_bytes     = static_cast<uint32_t>(ins << 1u) >> 30u;
            int64_t absolute_addr = reinterpret_cast<int64_t>(*inpp) + (((static_cast<int32_t>(ins << msb) >> (msb + lsb - 2u)) & ~3u) | lsb_bytes);
            int64_t new_pc_offset = static_cast<int64_t>(absolute_addr - reinterpret_cast<int64_t>(*outpp));
            bool special_fix_type = ctxp->is_in_fixing_range(absolute_addr);
            if (!special_fix_type && llabs(new_pc_offset) >= (max_val >> 1)) {
                if ((reinterpret_cast<uint64_t>(*outpp + 2) & 7u) != 0u) {
                    (*outpp)[0] = A64_NOP;
                    ctxp->reset_current_ins(current_idx, ++(*outpp));
                } //if

                (*outpp)[0] = 0x58000000u | (((8u >> 2u) << lsb) & ~mask) | (ins & rmask); // LDR #0x8
                (*outpp)[1] = 0x14000003u; // B #0xc
                memcpy(*outpp + 2, &absolute_addr, sizeof(absolute_addr));
                *outpp += 4;
            } else {
                if (special_fix_type) {
                    intptr_t ref_idx = ctxp->get_ref_ins_index(absolute_addr & ~3ull);
                    if (ref_idx <= current_idx) {
                        new_pc_offset = static_cast<int64_t>(ctxp->dat[ref_idx].ins - reinterpret_cast<int64_t>(*outpp));
                    } else {
                        ctxp->insert_fix_map(ref_idx, *outpp, lsb, fmask);
                        new_pc_offset = 0;
                    } //if
                } //if

                // the lsb_bytes will never be changed, so we can use lmask to keep it
                (*outpp)[0] = (static_cast<uint32_t>(new_pc_offset << (lsb - 2u)) & fmask) | (ins & lmask);
                ++(*outpp);
            } //if
        }
            break;
        case op_adrp:
        {
            current_idx           = ctxp->get_and_set_current_index(*inpp, *outpp);
            int32_t lsb_bytes     = static_cast<uint32_t>(ins << 1u) >> 30u;
            int64_t absolute_addr = (reinterpret_cast<int64_t>(*inpp) & ~0xfffll) + ((((static_cast<int32_t>(ins << msb) >> (msb + lsb - 2u)) & ~3u) | lsb_bytes) << 12);
            if (ctxp->is_in_fixing_range(absolute_addr)) {
                intptr_t ref_idx = ctxp->get_ref_ins_index(absolute_addr/* & ~3ull*/);
                if (ref_idx > current_idx) {
                    // the bottom 12 bits of absolute_addr are masked out,
                    // so ref_idx must be less than or equal to current_idx!
                } //if

                // *absolute_addr may be changed due to relocation fixing
                *(*outpp)++ = ins; // 0x90000000u;
            } else {
                if ((reinterpret_cast<uint64_t>(*outpp + 2) & 7u) != 0u) {
                    (*outpp)[0] = A64_NOP;
                    ctxp->reset_current_ins(current_idx, ++(*outpp));
                } //if

                (*outpp)[0] = 0x58000000u | (((8u >> 2u) << lsb) & ~mask) | (ins & rmask); // LDR #0x8
                (*outpp)[1] = 0x14000003u; // B #0xc
                memcpy(*outpp + 2, &absolute_addr, sizeof(absolute_addr)); // potential overflow?
                *outpp += 4;
            } //if
        }
            break;
        default:
            return false;
    }

    ctxp->process_fix_map(current_idx);
    ++(*inpp);
    return true;
}

//-------------------------------------------------------------------------

static void __fix_instructions(uint32_t *__restrict inp, int32_t count, uint32_t *__restrict outp)
{
    context ctx;
    ctx.basep = reinterpret_cast<int64_t>(inp);
    ctx.endp  = reinterpret_cast<int64_t>(inp + count);
    memset(ctx.dat, 0, sizeof(ctx.dat));
    static_assert(sizeof(ctx.dat) / sizeof(ctx.dat[0]) == A64_MAX_INSTRUCTIONS,
                  "please use A64_MAX_INSTRUCTIONS!");
#ifndef NDEBUG
    if (count > A64_MAX_INSTRUCTIONS) {
        A64_LOGE("too many fixing instructions!");
    } //if
#endif // NDEBUG

    uint32_t *const outp_base = outp;

    while (--count >= 0) {
        if (__fix_branch_imm(&inp, &outp, &ctx)) continue;
        if (__fix_cond_comp_test_branch(&inp, &outp, &ctx)) continue;
        if (__fix_loadlit(&inp, &outp, &ctx)) continue;
        if (__fix_pcreladdr(&inp, &outp, &ctx)) continue;

        // without PC-relative offset
        ctx.process_fix_map(ctx.get_and_set_current_index(inp, outp));
        *(outp++) = *(inp++);
    }

    static constexpr uint_fast64_t mask = 0x03ffffffu; // 0b00000011111111111111111111111111
    auto callback  = reinterpret_cast<int64_t>(inp);
    auto pc_offset = static_cast<int64_t>(callback - reinterpret_cast<int64_t>(outp)) >> 2;
    if (llabs(pc_offset) >= (mask >> 1)) {
        if ((reinterpret_cast<uint64_t>(outp + 2) & 7u) != 0u) {
            outp[0] = A64_NOP;
            ++outp;
        } //if
        outp[0] = 0x58000051u; // LDR X17, #0x8
        outp[1] = 0xd61f0220u; // BR X17
        *reinterpret_cast<int64_t *>(outp + 2) = callback;
        outp += 4;
    } else {
        outp[0] = 0x14000000u | (pc_offset & mask); // "B" ADDR_PCREL26
        ++outp;
    } //if

    const uintptr_t total = (outp - outp_base) * sizeof(uint32_t);
    __flush_cache(outp_base, total); // necessary
}

//-------------------------------------------------------------------------

extern "C" {
#define __attribute                __attribute__
#define aligned(x)                 __aligned__(x)
#define __intval(p)                reinterpret_cast<intptr_t>(p)
#define __uintval(p)               reinterpret_cast<uintptr_t>(p)
#define __ptr(p)                   reinterpret_cast<void *>(p)
#define __page_size                4096
#define __page_align(n)            __align_up(static_cast<uintptr_t>(n), __page_size)
#define __ptr_align(x)             __ptr(__align_down(reinterpret_cast<uintptr_t>(x), __page_size))
#define __align_up(x, n)           (((x) + ((n) - 1)) & ~((n) - 1))
#define __align_down(x, n)         ((x) & -(n))
#define __countof(x)               static_cast<intptr_t>(sizeof(x) / sizeof((x)[0])) // must be signed
#define __atomic_increase(p)       __sync_add_and_fetch(p, 1)
#define __sync_cmpswap(p, v, n)    __sync_bool_compare_and_swap(p, v, n)
#define __predict_true(exp)        __builtin_expect((exp) != 0, 1)
#define __make_rwx(p, n)           ::mprotect(__ptr_align(p), \
                                              __page_align(__uintval(p) + n) != __page_align(__uintval(p)) ? __page_align(n) + __page_size : __page_align(n), \
                                              PROT_READ | PROT_WRITE | PROT_EXEC)

//-------------------------------------------------------------------------

static __attribute((aligned(__page_size))) uint32_t __insns_pool[A64_MAX_BACKUPS][A64_MAX_INSTRUCTIONS * 10];

//-------------------------------------------------------------------------

class A64HookInit
{
public:
    A64HookInit()
    {
        __make_rwx(__insns_pool, sizeof(__insns_pool));
    }
};
static A64HookInit __init;

//-------------------------------------------------------------------------

static uint32_t *FastAllocateTrampoline()
{
    static_assert((A64_MAX_INSTRUCTIONS * 10 * sizeof(uint32_t)) % 8 == 0, "8-byte align");
    static volatile int32_t __index = -1;

    int32_t i = __atomic_increase(&__index);
    if (__predict_true(i >= 0 && i < __countof(__insns_pool))) {
        return __insns_pool[i];
    } //if

    return NULL;
}

//-------------------------------------------------------------------------

A64_JNIEXPORT void *A64HookFunctionV(void *const symbol, void *const replace,
                                     void *const rwx, const uintptr_t rwx_size)
{
    static constexpr uint_fast64_t mask = 0x03ffffffu; // 0b00000011111111111111111111111111

    uint32_t *trampoline = static_cast<uint32_t *>(rwx), *original = static_cast<uint32_t *>(symbol);

    static_assert(A64_MAX_INSTRUCTIONS >= 5, "please fix A64_MAX_INSTRUCTIONS!");
    auto pc_offset = static_cast<int64_t>(__intval(replace) - __intval(symbol)) >> 2;
    if (llabs(pc_offset) >= (mask >>1)) {
        int32_t count = (reinterpret_cast<uint64_t>(original + 2) & 7u) != 0u ? 5 : 4;
        if (trampoline) {
            if (rwx_size < count * 10u) {
                return NULL;
            } //if
            __fix_instructions(original, count, trampoline);
        } //if

        if (__make_rwx(original, 5 * sizeof(uint32_t)) == 0) {
            if (count == 5) {
                original[0] = A64_NOP;
                ++original;
            } //if
            original[0] = 0x58000051u; // LDR X17, #0x8
            original[1] = 0xd61f0220u; // BR X17
            *reinterpret_cast<int64_t *>(original + 2) = __intval(replace);
            __flush_cache(symbol, 5 * sizeof(uint32_t));

        } else {
            trampoline = NULL;
        } //if
    } else {
        if (trampoline) {
            if (rwx_size < 1u * 10u) {
                return NULL;
            } //if
            __fix_instructions(original, 1, trampoline);
        } //if

        if (__make_rwx(original, 1 * sizeof(uint32_t)) == 0) {
            __sync_cmpswap(original, *original, 0x14000000u | (pc_offset & mask)); // "B" ADDR_PCREL26
            __flush_cache(symbol, 1 * sizeof(uint32_t));

        } else {
            trampoline = NULL;
        } //if
    } //if

    return trampoline;
}

//-------------------------------------------------------------------------

A64_JNIEXPORT void A64HookFunction(void *const symbol, void *const replace, void **result)
{
    void *trampoline = NULL;
    if (result != NULL) {
        trampoline = FastAllocateTrampoline();
        *result = trampoline;
        if (trampoline == NULL) return;
    } //if

    trampoline = A64HookFunctionV(symbol, replace, trampoline, A64_MAX_INSTRUCTIONS * 10u);
    if (trampoline == NULL && result != NULL) {
        *result = NULL;
    } //if
}
}

#endif // defined(__aarch64__)