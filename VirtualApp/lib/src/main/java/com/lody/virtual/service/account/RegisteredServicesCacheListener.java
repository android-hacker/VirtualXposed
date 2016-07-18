/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lody.virtual.service.account;

/**
 * Listener for changes to the set of registered services managed by a RegisteredServicesCache.
 * @hide
 */
public interface RegisteredServicesCacheListener<V> {
    /**
     * Invoked when a service is registered or changed.
     * @param type the type of registered service
     * @param removed true if the service was removed
     */
    void onServiceChanged(V type, boolean removed);
}
