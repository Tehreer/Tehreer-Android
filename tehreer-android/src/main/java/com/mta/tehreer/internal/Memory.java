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

package com.mta.tehreer.internal;

public class Memory {

    public static native boolean isEqual(long address, long other, int size);
    public static native int getHash(long address, int size);

    public static native byte getByte(long address, int offset);
    public static native byte putByte(long address, int offset, byte value);
    public static native byte[] toByteArray(long address, int size);
}
