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

package com.mta.tehreer.internal

import com.mta.tehreer.internal.JniBridge.loadLibrary

internal object Raw {
    init {
        loadLibrary()
    }

    const val INT8_SIZE = 1
    const val INT16_SIZE = 2
    const val INT32_SIZE = 4
    val POINTER_SIZE = sizeOfIntPtr()

    private external fun sizeOfIntPtr(): Int

    @JvmStatic external fun getInt8Value(pointer: Long): Byte
    @JvmStatic external fun getInt16Value(pointer: Long): Short
    @JvmStatic external fun getInt32Value(pointer: Long): Int
    @JvmStatic external fun getIntPtrValue(pointer: Long): Long

    @JvmStatic external fun copyInt8Buffer(
        pointer: Long,
        destination: ByteArray, start: Int, length: Int
    )
    @JvmStatic external fun copyUInt8Buffer(
        pointer: Long,
        destination: IntArray, start: Int, length: Int
    )
}
