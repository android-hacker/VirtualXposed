/**
 *    Copyright 2017 jmpews
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

#ifndef hook_zz_h
#define hook_zz_h

#include <stdbool.h>
#include <stdint.h>

#define DEBUG_MODE 0

#ifndef zz_type
#define zz_type

typedef void *zpointer;
typedef unsigned long zsize;
typedef unsigned long zaddr;

typedef uint64_t zuint64;
typedef uint32_t zuint32;
typedef uint16_t zuint16;
typedef uint8_t zuint8;

typedef int32_t zint32;
typedef int16_t zint16;
typedef int8_t zint8;

typedef unsigned long zuint;
typedef long zint;
typedef unsigned char zbyte;
typedef bool zbool;

#endif

#if defined(FALSE)
#else
#define FALSE 0
#define TRUE 1
#endif

#ifndef zz_register_type
#define zz_register_type
#if defined(__arm64__) || defined(__aarch64__)
typedef union FPReg_ {
    __int128_t q;
    struct {
        double d1;
        double d2;
    } d;
    struct {
        float f1;
        float f2;
        float f3;
        float f4;
    } f;
} FPReg;

typedef struct _RegState {
    uint64_t sp;

    union {
        uint64_t x[29];
        struct {
            uint64_t x0, x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11, x12, x13, x14, x15, x16, x17, x18, x19, x20, x21,
                x22, x23, x24, x25, x26, x27, x28;
        } regs;
    } general;

    uint64_t fp;
    uint64_t lr;

    union {
        FPReg q[8];
        FPReg q0, q1, q2, q3, q4, q5, q6, q7;
    } floating;
} RegState;
#elif defined(__arm__)
typedef struct _RegState {
    zuint32 sp;

    union {
        zuint32 r[13];
        struct {
            zuint32 r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12;
        } regs;
    } general;

    zuint32 lr;
} RegState;
#elif defined(__i386__)
typedef struct _RegState {
} RegState;
#elif defined(__x86_64__)
typedef struct _RegState {
} RegState;
#endif
#endif

typedef enum _ZZSTATUS {
    ZZ_UNKOWN = -1,
    ZZ_DONE = 0,
    ZZ_SUCCESS,
    ZZ_FAILED,
    ZZ_DONE_HOOK,
    ZZ_DONE_INIT,
    ZZ_DONE_ENABLE,
    ZZ_ALREADY_HOOK,
    ZZ_ALREADY_INIT,
    ZZ_ALREADY_ENABLED,
    ZZ_NEED_INIT,
    ZZ_NO_BUILD_HOOK
} ZZSTATUS;

typedef struct _CallStack {
    zsize call_id;
    struct _ThreadStack *threadstack;
} CallStack;

typedef struct _ThreadStack {
    zsize thread_id;
    zsize size;
} ThreadStack;

typedef void (*PRECALL)(RegState *rs, ThreadStack *threadstack, CallStack *callstack);
typedef void (*POSTCALL)(RegState *rs, ThreadStack *threadstack, CallStack *callstack);
typedef void (*HALFCALL)(RegState *rs, ThreadStack *threadstack, CallStack *callstack);

// ------- export API -------

zpointer ZzGetCallStackData(CallStack *callstack_ptr, char *key);
zbool ZzSetCallStackData(CallStack *callstack_ptr, char *key, zpointer value_ptr, zsize value_size);

#define STACK_CHECK_KEY(callstack, key) (bool)ZzGetCallStackData(callstack, key)
#define STACK_GET(callstack, key, type) *(type *)ZzGetCallStackData(callstack, key)
#define STACK_SET(callstack, key, value, type) ZzSetCallStackData(callstack, key, &(value), sizeof(type))

ZZSTATUS ZzBuildHook(zpointer target_ptr, zpointer replace_call_ptr, zpointer *origin_ptr, PRECALL pre_call_ptr,
                     POSTCALL post_call_ptr, zbool try_near_jump);
ZZSTATUS ZzBuildHookAddress(zpointer target_start_ptr, zpointer target_end_ptr, PRECALL pre_call_ptr,
                            HALFCALL half_call_ptr, zbool try_near_jump);
ZZSTATUS ZzEnableHook(zpointer target_ptr);

ZZSTATUS ZzHook(zpointer target_ptr, zpointer replace_ptr, zpointer *origin_ptr, PRECALL pre_call_ptr,
                POSTCALL post_call_ptr, zbool try_near_jump);
ZZSTATUS ZzHookPrePost(zpointer target_ptr, PRECALL pre_call_ptr, POSTCALL post_call_ptr);
ZZSTATUS ZzHookReplace(zpointer target_ptr, zpointer replace_ptr, zpointer *origin_ptr);
ZZSTATUS ZzHookAddress(zpointer target_start_ptr, zpointer target_end_ptr, PRECALL pre_call_ptr,
                       HALFCALL half_call_ptr);

void ZzEnableDebugMode(void);

ZZSTATUS ZzRuntimeCodePatch(zaddr address, zpointer codedata, zsize codedata_size);

// ------- export API -------

#if defined(__arm64__) || defined(__aarch64__)
#if defined(__APPLE__) && defined(__MACH__)
#include <TargetConditionals.h>
#if TARGET_OS_IPHONE
#define TARGET_IS_IOS 1
#endif
#endif
#endif
#ifdef TARGET_IS_IOS
ZZSTATUS ZzSolidifyHook(zpointer target_fileoff, zpointer replace_call_ptr, zpointer *origin_ptr, PRECALL pre_call_ptr,
                        POSTCALL post_call_ptr);
#endif

#endif