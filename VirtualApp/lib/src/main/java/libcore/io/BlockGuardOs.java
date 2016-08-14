/*
 * Copyright (C) 2011 The Android Open Source Project
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

package libcore.io;

import android.system.ErrnoException;

import java.io.FileDescriptor;

/**
 * Informs BlockGuard of any activity it should be aware of.
 */
public class BlockGuardOs extends ForwardingOs{
    public BlockGuardOs(Os os) {
        super(os);
    }

    private FileDescriptor tagSocket(FileDescriptor fd) throws ErrnoException {
        return null;
    }

    private void untagSocket(FileDescriptor fd) throws ErrnoException {

    }




}
