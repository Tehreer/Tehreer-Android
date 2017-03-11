/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

package com.mta.tehreer.util;

/**
 * Interface for disposable objects.
 */
public interface Disposable {
    /**
     * Releases the native memory of this object. Failing to call this method will cause memory
     * leaks.
     * <p>
     * <strong>Note:</strong> The behavior is undefined if this method is called more than once on
     * the same object.
     */
    void dispose();
}
