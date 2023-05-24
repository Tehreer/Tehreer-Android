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

package com.mta.tehreer.internal.layout

import com.mta.tehreer.collections.IntList
import com.mta.tehreer.internal.util.Preconditions.checkArrayBounds
import com.mta.tehreer.internal.util.Preconditions.checkElementIndex
import com.mta.tehreer.internal.util.Preconditions.checkIndexRange

internal class ClusterMap(
    private val list: IntList,
    private val offset: Int,
    private val size: Int,
    private val difference: Int
) : IntList() {
    override fun size(): Int {
        return size
    }

    override fun get(index: Int): Int {
        checkElementIndex(index, size)
        return list[index + offset] - difference
    }

    override fun copyTo(array: IntArray, atIndex: Int) {
        checkNotNull(array)
        checkArrayBounds(array, atIndex, size)

        for (i in 0 until size) {
            array[i + atIndex] = list[i + offset] - difference
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): IntList {
        checkIndexRange(fromIndex, toIndex, size)
        return ClusterMap(list, offset + fromIndex, toIndex - fromIndex, difference)
    }
}
