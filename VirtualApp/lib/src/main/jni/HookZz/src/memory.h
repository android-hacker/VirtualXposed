//    Copyright 2017 jmpews
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.

#ifndef memory_h
#define memory_h

// platforms

// hookzz
#include "hookzz.h"

// zzdeps
#include "zzdefs.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

// #include "platforms/darwin/memory-darwin.h"
// #include "zzdeps/darwin/memory-utils-darwin.h"

zsize ZzMemoryGetPageSzie();

zpointer ZzMemoryAllocatePages(zsize n_pages);
zpointer ZzMemoryAllocateNearPages(zaddr address, zsize redirect_range_size, zsize n_pages);
zpointer ZzMemoryAllocate(zsize size);
zbool ZzMemoryPatchCode(const zaddr address, const zpointer codedata, zuint codedata_size);
zbool ZzMemoryProtectAsExecutable(const zaddr address, zsize size);
zbool ZzMemoryProtectAsWritable(const zaddr address, zsize size);
zbool ZzMemoryIsSupportAllocateRXPage();
zpointer ZzMemorySearchCodeCave(zaddr address, zsize redirect_range_size, zsize size);

#endif