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

#include <stdlib.h>
#include <string.h>

#include "stack.h"

ZzThreadStack *ZzGetCurrentThreadStack(zpointer key_ptr) {
    ZzThreadStack *stack = (ZzThreadStack *)ZzThreadGetCurrentThreadData(key_ptr);
    if (!stack)
        return NULL;
    return stack;
}

ZzThreadStack *ZzNewThreadStack(zpointer key_ptr) {
    ZzThreadStack *stack;
    stack = (ZzThreadStack *)malloc(sizeof(ZzThreadStack));
    stack->capacity = 4;
    ZzCallStack **callstacks = (ZzCallStack **)malloc(sizeof(ZzCallStack *) * (stack->capacity));
    if (!callstacks)
        return NULL;
    stack->callstacks = callstacks;
    stack->size = 0;
    stack->key_ptr = key_ptr;
    stack->thread_id = ZzThreadGetCurrentThreadID();
    ZzThreadSetCurrentThreadData(key_ptr, (zpointer)stack);
    return stack;
}

ZzCallStack *ZzNewCallStack() {
    ZzCallStack *callstack;
    callstack = (ZzCallStack *)malloc(sizeof(ZzCallStack));

    callstack->capacity = 4;

    callstack->items = (ZzCallStackItem *)malloc(sizeof(ZzCallStackItem) * callstack->capacity);
    if (!callstack->items)
        return NULL;

    callstack->size = 0;
    return callstack;
}

void ZzFreeCallStack(ZzCallStack *callstack) {
    free(callstack->items);
    free(callstack);
    callstack = NULL;
}

ZzCallStack *ZzPopCallStack(ZzThreadStack *stack) {
    if (stack->size > 0)
        stack->size--;
    else
        return NULL;
    ZzCallStack *callstack = stack->callstacks[stack->size];
    return callstack;
}

zbool ZzPushCallStack(ZzThreadStack *stack, ZzCallStack *callstack) {
    if (!stack)
        return FALSE;

    if (stack->size >= stack->capacity) {
        ZzCallStack **callstacks =
            (ZzCallStack **)realloc(stack->callstacks, sizeof(ZzCallStack *) * (stack->capacity) * 2);
        if (!callstacks)
            return FALSE;
        stack->callstacks = callstacks;
        stack->capacity = stack->capacity * 2;
    }

    callstack->call_id = stack->size;
    callstack->threadstack = (ThreadStack *)stack;

    stack->callstacks[stack->size++] = callstack;
    return TRUE;
}

zpointer ZzGetCallStackData(CallStack *callstack_ptr, char *key) {
    ZzCallStack *callstack = (ZzCallStack *)callstack_ptr;
    if (!callstack)
        return NULL;
    int i;
    for (i = 0; i < callstack->size; ++i) {
        if (!strcmp(callstack->items[i].key, key)) {
            return callstack->items[i].value;
        }
    }
    return NULL;
}

ZzCallStackItem *ZzNewCallStackData(ZzCallStack *callstack) {
    if (!callstack)
        return NULL;
    if (callstack->size >= callstack->capacity) {
        ZzCallStackItem *callstackitems =
            (ZzCallStackItem *)realloc(callstack->items, sizeof(ZzCallStackItem) * callstack->capacity * 2);
        if (!callstackitems)
            return NULL;
        callstack->items = callstackitems;
        callstack->capacity = callstack->capacity * 2;
    }
    return &(callstack->items[callstack->size++]);
}

zbool ZzSetCallStackData(CallStack *callstack_ptr, char *key, zpointer value_ptr, zsize value_size) {
    ZzCallStack *callstack = (ZzCallStack *)callstack_ptr;
    if (!callstack)
        return FALSE;

    ZzCallStackItem *item = ZzNewCallStackData(callstack);

    char *key_tmp = (char *)malloc(strlen(key) + 1);
    strncpy(key_tmp, key, strlen(key) + 1);

    zpointer value_tmp = (zpointer)malloc(value_size);
    memcpy(value_tmp, value_ptr, value_size);
    item->key = key_tmp;
    item->value = value_tmp;
    return TRUE;
}
