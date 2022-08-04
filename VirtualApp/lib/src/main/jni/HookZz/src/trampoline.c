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

#include "trampoline.h"

ZZSTATUS ZzBuildTrampoline(struct _ZzInterceptorBackend *self, ZzHookFunctionEntry *entry) {

    ZzPrepareTrampoline(self, entry);
    ZzBuildEnterTrampoline(self, entry);

    if (entry->hook_type == HOOK_ADDRESS_TYPE) {
        ZzBuildHalfTrampoline(self, entry);
        ZzBuildInvokeTrampoline(self, entry);
    } else {
        ZzBuildInvokeTrampoline(self, entry);
        ZzBuildLeaveTrampoline(self, entry);
    }

    return ZZ_DONE;
}
