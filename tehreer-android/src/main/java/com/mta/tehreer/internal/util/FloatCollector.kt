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

package com.mta.tehreer.internal.util

import androidx.annotation.Keep

/**
 * Collects the floats that native code hands over one by one, so that it does not have to fill an
 * array of a size that it cannot know beforehand.
 */
@Keep
internal class FloatCollector {
    private var values = FloatArray(16)

    var size = 0
        private set

    @Keep
    fun add(value: Float) {
        if (size == values.size) {
            values = values.copyOf(size * 2)
        }

        values[size++] = value
    }

    operator fun get(index: Int): Float {
        require(index in 0 until size) { "Index: $index, Size: $size" }
        return values[index]
    }

    fun toArray(): FloatArray {
        return values.copyOf(size)
    }
}
