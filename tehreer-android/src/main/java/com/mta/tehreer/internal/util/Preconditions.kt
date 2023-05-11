/*
 * Copyright (C) 2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.util

internal object Preconditions {
    @JvmStatic
    fun checkNotNull(obj: Any?) {
        if (obj == null) {
            throw NullPointerException()
        }
    }

    @JvmStatic
    fun checkNotNull(obj: Any?, message: String?) {
        if (obj == null) {
            throw NullPointerException(message)
        }
    }

    @JvmStatic
    fun checkArgument(expression: Boolean, message: String?) {
        if (!expression) {
            throw IllegalArgumentException(message)
        }
    }

    @JvmStatic
    fun checkElementIndex(index: Int, size: Int) {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
    }

    @JvmStatic
    fun checkArrayBounds(array: ByteArray, offset: Int, size: Int) {
        if (offset < 0 || array.size - offset < size) {
            throw ArrayIndexOutOfBoundsException()
        }
    }

    @JvmStatic
    fun checkArrayBounds(array: IntArray, offset: Int, size: Int) {
        if (offset < 0 || array.size - offset < size) {
            throw ArrayIndexOutOfBoundsException()
        }
    }

    @JvmStatic
    fun checkArrayBounds(array: FloatArray, offset: Int, size: Int) {
        if (offset < 0 || array.size - offset < size) {
            throw ArrayIndexOutOfBoundsException()
        }
    }

    @JvmStatic
    fun checkIndexRange(start: Int, end: Int, size: Int) {
        if (start < 0 || end > size || start > end) {
            throw IndexOutOfBoundsException()
        }
    }
}
