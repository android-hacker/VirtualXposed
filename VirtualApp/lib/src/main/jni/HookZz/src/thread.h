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

#ifndef thread_h
#define thread_h

// hookzz
#include "hookzz.h"

zpointer ZzThreadNewThreadLocalKeyPtr();

zpointer ZzThreadGetCurrentThreadData(zpointer key_ptr);

zbool ZzThreadSetCurrentThreadData(zpointer key_ptr, zpointer data);

long ZzThreadGetCurrentThreadID();

#endif