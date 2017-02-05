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

public class Raw {

    public static native byte getInt8(long pointer, int index);
    public static native int getInt32(long pointer, int index);
    public static native int getUInt16(long pointer, int index);
    public static native int getSizeValue(long pointer, int index);

    public static native byte[] arrayForInt8Values(long pointer, int count);
    public static native int[] arrayForUInt16Values(long pointer, int count);
    public static native int[] arrayForSizeValues(long pointer, int count);
    public static native float[] arrayForInt32Floats(long pointer, int count, float scale);
    public static native float[] arrayForInt32Points(long pointer, int count, float scale);

    private Raw() {
    }
}
