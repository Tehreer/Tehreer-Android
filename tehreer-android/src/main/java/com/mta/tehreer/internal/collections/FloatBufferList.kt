/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.internal.Raw
import com.mta.tehreer.internal.util.Preconditions.checkArrayBounds
import com.mta.tehreer.internal.util.Preconditions.checkElementIndex
import com.mta.tehreer.internal.util.Preconditions.checkIndexRange
import com.mta.tehreer.internal.util.Preconditions.checkNotNull

/**
 * A list of floats that lie in native memory. The owner is kept so that it does not get disposed
 * while the list is in use.
 */
internal class FloatBufferList(
    private val owner: Any?,
    private val pointer: Long,
    private val size: Int
) : FloatList() {
    override fun size(): Int {
        return size
    }

    override fun get(index: Int): Float {
        checkElementIndex(index, size)
        return Raw.getFloatValue(pointer + index * Raw.FLOAT_SIZE)
    }

    override fun copyTo(array: FloatArray, atIndex: Int) {
        checkNotNull(array)
        checkArrayBounds(array, atIndex, size)

        Raw.copyFloatBuffer(pointer, array, atIndex, size)
    }

    override fun subList(fromIndex: Int, toIndex: Int): FloatList {
        checkIndexRange(fromIndex, toIndex, size)
        return FloatBufferList(owner, pointer + fromIndex * Raw.FLOAT_SIZE, toIndex - fromIndex)
    }
}
