/*
 * Copyright (C) 2017-2018 Muhammad Tayyab Akram
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

public final class Raw {

    static {
        JniBridge.loadLibrary();
    }

    public static final int INT8_SIZE = 1;
    public static final int INT16_SIZE = 2;
    public static final int INT32_SIZE = 4;
    public static final int POINTER_SIZE = sizeOfIntPtr();

    private static native int sizeOfIntPtr();

    public static native byte getInt8Value(long pointer);
    public static native int getInt32Value(long pointer);
    public static native int getIntPtrValue(long pointer);
    public static native int getUInt16Value(long pointer);

    public static native void copyInt8Buffer(long pointer, byte[] destination, int start, int length);
    public static native void copyInt32Buffer(long pointer, float[] destination, int start, int length, float scale);
    public static native void copyUInt8Buffer(long pointer, int[] destination, int start, int length);
    public static native void copyUInt16Buffer(long pointer, int[] destination, int start, int length);
    public static native void copyUIntPtrBuffer(long pointer, int[] destination, int start, int length);

    private Raw() {
    }
}
