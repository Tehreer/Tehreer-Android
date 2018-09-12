/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.util;

public final class Preconditions {
    public static void checkNotNull(Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
    }

    public static void checkNotNull(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }

    public static void checkElementIndex(int index, int size) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    public static void checkArrayBounds(byte[] array, int offset, int size) {
        if (offset < 0 || (array.length - offset) < size) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static void checkArrayBounds(int[] array, int offset, int size) {
        if (offset < 0 || (array.length - offset) < size) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static void checkArrayBounds(float[] array, int offset, int size) {
        if (offset < 0 || (array.length - offset) < size) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static void checkIndexRange(int start, int end, int size) {
        if (start < 0 || end > size || start > end) {
            throw new IndexOutOfBoundsException();
        }
    }

    private Preconditions() { }
}
