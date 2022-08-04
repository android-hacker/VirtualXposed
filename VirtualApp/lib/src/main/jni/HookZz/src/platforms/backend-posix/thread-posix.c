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

#include "thread-posix.h"

zpointer ZzThreadNewThreadLocalKeyPtr() { return zz_posix_thread_new_thread_local_key_ptr(); }

zpointer ZzThreadGetCurrentThreadData(zpointer key_ptr) {
    return zz_posix_thread_get_current_thread_data(key_ptr);
}

zbool ZzThreadSetCurrentThreadData(zpointer key_ptr, zpointer data) {
    return zz_posix_thread_set_current_thread_data(key_ptr, data);
}

long ZzThreadGetCurrentThreadID() { return zz_posix_get_current_thread_id(); }