/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

package com.mta.tehreer.bidi;

class BidiBuffer {

    public static long create(String string) {
        return nativeCreate(string);
    }

    public static long retain(long nativeBuffer) {
        nativeRetain(nativeBuffer);
        return nativeBuffer;
    }

    public static void release(long nativeBuffer) {
        nativeRelease(nativeBuffer);
    }

    private static native long nativeCreate(String string);
    private static native void nativeRetain(long nativeBuffer);
    private static native void nativeRelease(long nativeBuffer);
}
