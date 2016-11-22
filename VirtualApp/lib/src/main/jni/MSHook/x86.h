/*
 * x86.h
 *
 *  Created on: 2016��2��22��
 *      Author: peng
 */

#ifndef X86_H_
#define X86_H_

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <sys/mman.h>
#include "CydiaSubstrate.h"
#include "PosixMemory.h"
#include "Log.h"
#include "Debug.h"

template <typename Type_>
_disused static _finline void MSWrite(uint8_t *&buffer, Type_ value) {
    *reinterpret_cast<Type_ *>(buffer) = value;
    buffer += sizeof(Type_);
}

_disused static _finline void MSWrite(uint8_t *&buffer, uint8_t *data, size_t size) {
    memcpy(buffer, data, size);
    buffer += size;
}

#ifdef __LP64__
static const bool ia32 = false;
#else
static const bool ia32 = true;
#endif

enum I$r {
    I$rax, I$rcx, I$rdx, I$rbx,
    I$rsp, I$rbp, I$rsi, I$rdi,
    I$r8, I$r9, I$r10, I$r11,
    I$r12, I$r13, I$r14, I$r15,
};

_disused static bool MSIs32BitOffset(uintptr_t target, uintptr_t source) {
    intptr_t offset(target - source);
    return int32_t(offset) == offset;
}

_disused static size_t MSSizeOfSkip() {
    return 5;
}

_disused static size_t MSSizeOfPushPointer(uintptr_t target) {
    return uint64_t(target) >> 32 == 0 ? 5 : 13;
}

_disused static size_t MSSizeOfPushPointer(void *target) {
    return MSSizeOfPushPointer(reinterpret_cast<uintptr_t>(target));
}

_disused static size_t MSSizeOfJump(bool blind, uintptr_t target, uintptr_t source = 0) {
    if (ia32 || (!blind && MSIs32BitOffset(target, source + 5)))
        return MSSizeOfSkip();
    else
        return MSSizeOfPushPointer(target) + 1;
}

_disused static size_t MSSizeOfJump(uintptr_t target, uintptr_t source) {
    return MSSizeOfJump(false, target, source);
}

_disused static size_t MSSizeOfJump(uintptr_t target) {
    return MSSizeOfJump(true, target);
}

_disused static size_t MSSizeOfJump(void *target, void *source) {
    return MSSizeOfJump(reinterpret_cast<uintptr_t>(target), reinterpret_cast<uintptr_t>(source));
}

_disused static size_t MSSizeOfJump(void *target) {
    return MSSizeOfJump(reinterpret_cast<uintptr_t>(target));
}

_disused static void MSWriteSkip(uint8_t *&current, ssize_t size) {
    MSWrite<uint8_t>(current, 0xe9);
    MSWrite<uint32_t>(current, size);
}

_disused static void MSPushPointer(uint8_t *&current, uintptr_t target) {
    MSWrite<uint8_t>(current, 0x68);
    MSWrite<uint32_t>(current, target);

    if (uint32_t high = uint64_t(target) >> 32) {
        MSWrite<uint8_t>(current, 0xc7);
        MSWrite<uint8_t>(current, 0x44);
        MSWrite<uint8_t>(current, 0x24);
        MSWrite<uint8_t>(current, 0x04);
        MSWrite<uint32_t>(current, high);
    }
}

_disused static void MSPushPointer(uint8_t *&current, void *target) {
    return MSPushPointer(current, reinterpret_cast<uintptr_t>(target));
}

_disused static void MSWriteCall(uint8_t *&current, I$r target) {
    if (target >> 3 != 0)
        MSWrite<uint8_t>(current, 0x40 | (target & 0x08) >> 3);
    MSWrite<uint8_t>(current, 0xff);
    MSWrite<uint8_t>(current, 0xd0 | (target & 0x07));
}

_disused static void MSWriteCall(uint8_t *&current, uintptr_t target) {
    uintptr_t source(reinterpret_cast<uintptr_t>(current));

    if (ia32 || MSIs32BitOffset(target, source + 5)) {
        MSWrite<uint8_t>(current, 0xe8);
        MSWrite<uint32_t>(current, target - (source + 5));
    } else {
        MSPushPointer(current, target);

        MSWrite<uint8_t>(current, 0x83);
        MSWrite<uint8_t>(current, 0xc4);
        MSWrite<uint8_t>(current, 0x08);

        MSWrite<uint8_t>(current, 0x67);
        MSWrite<uint8_t>(current, 0xff);
        MSWrite<uint8_t>(current, 0x54);
        MSWrite<uint8_t>(current, 0x24);
        MSWrite<uint8_t>(current, 0xf8);
    }
}

template <typename Type_>
_disused static void MSWriteCall(uint8_t *&current, Type_ *target) {
    return MSWriteCall(current, reinterpret_cast<uintptr_t>(target));
}

_disused static void MSWriteJump(uint8_t *&current, uintptr_t target) {
    uintptr_t source(reinterpret_cast<uintptr_t>(current));

    if (ia32 || MSIs32BitOffset(target, source + 5))
        MSWriteSkip(current, target - (source + 5));
    else {
        MSPushPointer(current, target);
        MSWrite<uint8_t>(current, 0xc3);
    }
}

_disused static void MSWriteJump(uint8_t *&current, void *target) {
    return MSWriteJump(current, reinterpret_cast<uintptr_t>(target));
}

_disused static void MSWriteJump(uint8_t *&current, I$r target) {
    if (target >> 3 != 0)
        MSWrite<uint8_t>(current, 0x40 | (target & 0x08) >> 3);
    MSWrite<uint8_t>(current, 0xff);
    MSWrite<uint8_t>(current, 0xe0 | (target & 0x07));
}

_disused static void MSWritePop(uint8_t *&current, uint8_t target) {
    if (target >> 3 != 0)
        MSWrite<uint8_t>(current, 0x40 | (target & 0x08) >> 3);
    MSWrite<uint8_t>(current, 0x58 | (target & 0x07));
}

_disused static size_t MSSizeOfPop(uint8_t target) {
    return target >> 3 != 0 ? 2 : 1;
}

_disused static void MSWritePush(uint8_t *&current, I$r target) {
    if (target >> 3 != 0)
        MSWrite<uint8_t>(current, 0x40 | (target & 0x08) >> 3);
    MSWrite<uint8_t>(current, 0x50 | (target & 0x07));
}

_disused static void MSWriteAdd(uint8_t *&current, I$r target, uint8_t source) {
    MSWrite<uint8_t>(current, 0x83);
    MSWrite<uint8_t>(current, 0xc4 | (target & 0x07));
    MSWrite<uint8_t>(current, source);
}

_disused static void MSWriteSet64(uint8_t *&current, I$r target, uintptr_t source) {
    MSWrite<uint8_t>(current, 0x48 | (target & 0x08) >> 3 << 2);
    MSWrite<uint8_t>(current, 0xb8 | (target & 0x7));
    MSWrite<uint64_t>(current, source);
}

template <typename Type_>
_disused static void MSWriteSet64(uint8_t *&current, I$r target, Type_ *source) {
    return MSWriteSet64(current, target, reinterpret_cast<uintptr_t>(source));
}

_disused static void MSWriteMove64(uint8_t *&current, uint8_t source, uint8_t target) {
    MSWrite<uint8_t>(current, 0x48 | (target & 0x08) >> 3 << 2 | (source & 0x08) >> 3);
    MSWrite<uint8_t>(current, 0x8b);
    MSWrite<uint8_t>(current, (target & 0x07) << 3 | (source & 0x07));
}

_disused static size_t MSSizeOfMove64() {
    return 3;
}

namespace x86{
	extern "C" void SubstrateHookFunctionx86(SubstrateProcessRef process, void *symbol, void *replace, void **result);
}

#endif /* X86_H_ */
