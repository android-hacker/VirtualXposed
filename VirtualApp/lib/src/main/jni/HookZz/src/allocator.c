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

#include "allocator.h"
#include "zzdeps/zz.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define DEFAULT_ALLOCATOR_CAPACITY 4

ZzAllocator *ZzNewAllocator() {

    if (!ZzMemoryIsSupportAllocateRXPage())
        return NULL;

    ZzAllocator *allocator;
    allocator = (ZzAllocator *)malloc(sizeof(ZzAllocator));
    allocator->memory_pages = (ZzMemoryPage **)malloc(sizeof(ZzMemoryPage *) * DEFAULT_ALLOCATOR_CAPACITY);
    if (!allocator->memory_pages)
        return NULL;
    allocator->size = 0;
    allocator->capacity = DEFAULT_ALLOCATOR_CAPACITY;
    return allocator;
}

ZzMemoryPage *ZzNewMemoryPage() {
    zsize page_size = ZzMemoryGetPageSzie();
    zpointer page_ptr = NULL;
    zpointer cave_ptr = NULL;
    ZzMemoryPage *page = NULL;

    page_ptr = ZzMemoryAllocatePages(1);
    if (!page_ptr) {
        return NULL;
    }
    if (!ZzMemoryProtectAsExecutable((zaddr)page_ptr, page_size)) {
        Xerror("ZzMemoryProtectAsExecutable error at %p", page_ptr);
#if defined(DEBUG_MODE)
        debug_break();
#endif
        exit(1);
    }

    page = (ZzMemoryPage *)malloc(sizeof(ZzMemoryPage));
    page->base = page_ptr;
    page->curr_pos = page_ptr;
    page->size = page_size;
    page->used_size = 0;
    return page;
}

ZzMemoryPage *ZzNewNearMemoryPage(zaddr address, zsize redirect_range_size) {
    zsize page_size = ZzMemoryGetPageSzie();
    zpointer page_ptr = NULL;
    zpointer cave_ptr = NULL;
    ZzMemoryPage *page = NULL;

    page_ptr = ZzMemoryAllocateNearPages(address, redirect_range_size, 1);
    if (!page_ptr) {
        return NULL;
    }

    if (!ZzMemoryProtectAsExecutable((zaddr)page_ptr, page_size)) {
        Xerror("ZzMemoryProtectAsExecutable error at %p", page_ptr);
#if defined(DEBUG_MODE)
        debug_break();
#endif
        exit(1);
    }

    page = (ZzMemoryPage *)malloc(sizeof(ZzMemoryPage));

    page->base = page_ptr;

    if ((zaddr)page_ptr > address && ((zaddr)page_ptr + page_size) > (address + redirect_range_size)) {
        page->size = (address + redirect_range_size) - (zaddr)page_ptr;
        page->used_size = 0;
        page->curr_pos = page_ptr;
    } else if ((zaddr)page_ptr < address && (zaddr)page_ptr < (address - redirect_range_size)) {
        page->size = page_size;
        page->used_size = (address - redirect_range_size) - (zaddr)page_ptr;
        page->curr_pos = (zpointer)(address - redirect_range_size);
    } else {
        page->size = page_size;
        page->used_size = 0;
        page->curr_pos = page_ptr;
    }
    page->isCodeCave = FALSE;
    return page;
}

ZzMemoryPage *ZzNewNearCodeCave(zaddr address, zsize redirect_range_size, zsize code_slice_size) {
    zsize page_size = ZzMemoryGetPageSzie();
    zpointer cave_ptr = NULL;
    ZzMemoryPage *page = NULL;
    zsize cave_size = code_slice_size;

    cave_ptr = ZzMemorySearchCodeCave(address, redirect_range_size, cave_size);

    if (!cave_ptr)
        return NULL;

    page = (ZzMemoryPage *)malloc(sizeof(ZzMemoryPage));
    page->base = cave_ptr;
    page->curr_pos = cave_ptr;
    page->size = cave_size;
    page->used_size = 0;
    page->isCodeCave = TRUE;
    return page;
}

ZZSTATUS ZzAddMemoryPage(ZzAllocator *allocator, ZzMemoryPage *page) {
    if (!allocator)
        return ZZ_FAILED;
    if (allocator->size >= allocator->capacity) {
        ZzMemoryPage **pages = realloc(allocator->memory_pages, sizeof(ZzMemoryPage) * (allocator->capacity) * 2);
        if (!pages) {
            return ZZ_FAILED;
        }
        allocator->capacity = allocator->capacity * 2;
        allocator->memory_pages = pages;
    }
    allocator->memory_pages[allocator->size++] = page;
    return ZZ_SUCCESS;
}

//  1. try allocate from the history pages
//  2. try allocate a new page
//  3. add it to the page manager
ZzCodeSlice *ZzNewCodeSlice(ZzAllocator *allocator, zsize code_slice_size) {
    ZzCodeSlice *code_slice = NULL;
    ZzMemoryPage *page = NULL;
    int i;

    for (i = 0; i < allocator->size; i++) {
        page = allocator->memory_pages[i];
        // 1. page is initialized
        // 2. can't be codecave
        // 3. the rest memory of this page is enough for code_slice_size
        // 4. the page address is near

        if ((zaddr)page->curr_pos % 4) {
            int t = 4 - (zaddr)page->curr_pos % 4;
            page->used_size += t;
            page->curr_pos += t;
        }

        if (page->base && !page->isCodeCave && (page->size - page->used_size) > code_slice_size) {
            code_slice = (ZzCodeSlice *)malloc(sizeof(ZzCodeSlice));
            code_slice->data = page->curr_pos;
            code_slice->size = code_slice_size;

            page->curr_pos += code_slice_size;
            page->used_size += code_slice_size;
            return code_slice;
        }
    }

    page = ZzNewMemoryPage();
    ZzAddMemoryPage(allocator, page);

    if ((zaddr)page->curr_pos % 4) {
        int t = 4 - (zaddr)page->curr_pos % 4;
        page->used_size += t;
        page->curr_pos += t;
    }

    code_slice = (ZzCodeSlice *)malloc(sizeof(ZzCodeSlice));
    code_slice->data = page->curr_pos;
    code_slice->size = code_slice_size;

    page->curr_pos += code_slice_size;
    page->used_size += code_slice_size;

    return code_slice;
}

//  1. try allocate from the history pages
//  2. try allocate a new near page
//  3. add it to the page manager
ZzCodeSlice *ZzNewNearCodeSlice(ZzAllocator *allocator, zaddr address, zsize redirect_range_size,
                                zsize code_slice_size) {
    ZzCodeSlice *code_slice = NULL;
    ZzMemoryPage *page = NULL;
    int i;
    for (i = 0; i < allocator->size; i++) {
        page = allocator->memory_pages[i];
        // 1. page is initialized
        // 2. can't be codecave
        // 3. the rest memory of this page is enough for code_slice_size
        // 4. the page address is near
        if (page->base && !page->isCodeCave) {
            int flag = 0;
            zaddr split_addr = 0;

            if ((zaddr)page->curr_pos < address) {
                if (address - redirect_range_size < (zaddr)page->curr_pos) {
                    // enough for code_slice_size
                    if ((page->size - page->used_size) < code_slice_size)
                        continue;
                    flag = 1;
                } else if (address - redirect_range_size > (zaddr)page->curr_pos &&
                           (address - redirect_range_size) < ((zaddr)page->base + page->size)) {
                    // enough for code_slice_size
                    if (((zaddr)page->base + page->size) - (address - redirect_range_size) < code_slice_size)
                        continue;
                    split_addr = address - redirect_range_size;
                    flag = 2;
                }
            } else {
                if (address + redirect_range_size > ((zaddr)page->base + page->size)) {
                    // enough for code_slice_size
                    if ((page->size - page->used_size) < code_slice_size)
                        continue;
                    flag = 1;
                } else if ((address + redirect_range_size) > (zaddr)page->curr_pos &&
                           (address + redirect_range_size) < ((zaddr)page->base + page->size)) {
                    if ((address + redirect_range_size) - (zaddr)page->curr_pos > code_slice_size)
                        continue;
                    flag = 1;
                }
            }

            if (1 == flag) {
                code_slice = (ZzCodeSlice *)malloc(sizeof(ZzCodeSlice));
                code_slice->isCodeCave = page->isCodeCave;
                code_slice->data = page->curr_pos;
                code_slice->size = code_slice_size;

                page->curr_pos += code_slice_size;
                page->used_size += code_slice_size;
                return code_slice;
            } else if (2 == flag) {

                // new page
                ZzMemoryPage *new_page = (ZzMemoryPage *)malloc(sizeof(ZzMemoryPage));
                new_page->base = (zpointer)split_addr;
                new_page->size = ((zaddr)page->base + page->size) - split_addr;
                new_page->used_size = 0;
                new_page->curr_pos = (zpointer)split_addr;
                ZzAddMemoryPage(allocator, new_page);

                // origin page
                page->size = split_addr - (zaddr)page->base;

                code_slice = (ZzCodeSlice *)malloc(sizeof(ZzCodeSlice));
                code_slice->isCodeCave = FALSE;
                code_slice->data = new_page->curr_pos;
                code_slice->size = code_slice_size;

                new_page->curr_pos += code_slice_size;
                new_page->used_size += code_slice_size;
                return code_slice;
            }
        }
    }

#if 0
    page = ZzNewNearMemoryPage(address, redirect_range_size);
    // try allocate again, avoid the boundary page
    if (page && (page->size - page->used_size) < code_slice_size) {
        page = ZzNewNearMemoryPage(address, redirect_range_size);
    }
#endif
    page = NULL;

    if (!page) {
        page = ZzNewNearCodeCave(address, redirect_range_size, code_slice_size);
        if (!page)
            return NULL;
    }
    if (!page)
        return NULL;

    code_slice = (ZzCodeSlice *)malloc(sizeof(ZzCodeSlice));
    code_slice->isCodeCave = page->isCodeCave;
    code_slice->data = page->curr_pos;
    code_slice->size = code_slice_size;

    page->curr_pos += code_slice_size;
    page->used_size += code_slice_size;

    return code_slice;
}
