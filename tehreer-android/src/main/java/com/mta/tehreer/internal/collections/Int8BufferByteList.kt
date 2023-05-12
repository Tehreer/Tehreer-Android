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

package com.mta.tehreer.internal.collections

import com.mta.tehreer.collections.ByteList
import com.mta.tehreer.internal.Raw
import com.mta.tehreer.internal.Raw.copyInt8Buffer
import com.mta.tehreer.internal.Raw.getInt8Value
import com.mta.tehreer.internal.util.Preconditions.checkArrayBounds
import com.mta.tehreer.internal.util.Preconditions.checkElementIndex
import com.mta.tehreer.internal.util.Preconditions.checkIndexRange
import com.mta.tehreer.internal.util.Preconditions.checkNotNull

internal class Int8BufferByteList(
    private val owner: Any?,
    private val pointer: Long,
    private val size: Int
) : ByteList() {
    override fun size(): Int {
        return size
    }

    override fun get(index: Int): Byte {
        checkElementIndex(index, size)
        return getInt8Value(pointer + index * Raw.INT8_SIZE)
    }

    override fun copyTo(array: ByteArray, atIndex: Int) {
        checkNotNull(array)
        checkArrayBounds(array, atIndex, size)

        copyInt8Buffer(pointer, array, atIndex, size)
    }

    override fun subList(fromIndex: Int, toIndex: Int): ByteList {
        checkIndexRange(fromIndex, toIndex, size)
        return Int8BufferByteList(owner, pointer + fromIndex * Raw.INT8_SIZE, toIndex - fromIndex)
    }
}
