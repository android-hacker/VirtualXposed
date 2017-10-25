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

#ifndef platforms_backend_arm64_thunker_arm64
#define platforms_backend_arm64_thunker_arm64

// platforms
#include "platforms/arch-arm64/relocator-arm64.h"
#include "platforms/arch-arm64/writer-arm64.h"

#include "interceptor-arm64.h"

// hookzz
#include "stack.h"
#include "thunker.h"

// zzdeps
#include "hookzz.h"
#include "zzdeps/common/debugbreak.h"
#include "zzdeps/zz.h"

#endif