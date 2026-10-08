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

import com.mta.tehreer.collections.PointList
import com.mta.tehreer.internal.Raw
import com.mta.tehreer.internal.util.Preconditions.checkArrayBounds
import com.mta.tehreer.internal.util.Preconditions.checkElementIndex
import com.mta.tehreer.internal.util.Preconditions.checkIndexRange

private const val FIELD_COUNT = 2

/**
 * A list of points that lie in native memory as pairs of floats. The owner is kept so that it does
 * not get disposed while the list is in use.
 */
internal class FloatBufferPointList(
    private val owner: Any?,
    private val pointer: Long,
    private val size: Int
) : PointList() {
    override fun size(): Int {
        return size
    }

    override fun getX(index: Int): Float {
        checkElementIndex(index, size)
        return Raw.getFloatValue(owner, pointer + index * FIELD_COUNT * Raw.FLOAT_SIZE)
    }

    override fun getY(index: Int): Float {
        checkElementIndex(index, size)
        return Raw.getFloatValue(owner, pointer + (index * FIELD_COUNT + 1) * Raw.FLOAT_SIZE)
    }

    override fun copyTo(array: FloatArray, atIndex: Int) {
        checkArrayBounds(array, atIndex, size * FIELD_COUNT)

        Raw.copyFloatBuffer(owner, pointer, array, atIndex, size * FIELD_COUNT)
    }

    override fun subList(fromIndex: Int, toIndex: Int): PointList {
        checkIndexRange(fromIndex, toIndex, size)
        return FloatBufferPointList(owner, pointer + fromIndex * FIELD_COUNT * Raw.FLOAT_SIZE, toIndex - fromIndex)
    }
}
