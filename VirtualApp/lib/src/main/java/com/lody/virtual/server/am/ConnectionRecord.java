/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.lody.virtual.server.am;

import android.app.IServiceConnection;

/**
 * Description of a single binding to a service.
 */
final class ConnectionRecord {
    final AppBindRecord binding;    // The application/service binding.
    final IServiceConnection conn;  // The client connection.
    final int flags;                // Binding options.
    boolean serviceDead;            // Well is it?

    ConnectionRecord(AppBindRecord _binding,
               IServiceConnection _conn, int _flags) {
        binding = _binding;
        conn = _conn;
        flags = _flags;
    }
}
