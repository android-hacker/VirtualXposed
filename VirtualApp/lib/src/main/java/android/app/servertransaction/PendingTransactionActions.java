/*
 * Copyright 2017 The Android Open Source Project
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
package android.app.servertransaction;
/**
 * Container that has data pending to be used at later stages of
 * {@link ClientTransaction}.
 * An instance of this class is passed to each individual transaction item, so it can use some
 * information from previous steps or add some for the following steps.
 *
 * @hide
 */
public class PendingTransactionActions {
}