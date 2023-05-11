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

package com.mta.tehreer.internal.sfnt.tables.cpal

import android.graphics.Color
import androidx.annotation.ColorInt
import com.mta.tehreer.internal.sfnt.SfntTable

internal class ColorRecordsArray(
    private val data: SfntTable
) {
    @ColorInt
    operator fun get(index: Int): Int {
        val offset = index * 4
        val blue = data.readUInt8(offset)
        val green = data.readUInt8(offset + 1)
        val red = data.readUInt8(offset + 2)
        val alpha = data.readUInt8(offset + 3)

        return Color.argb(alpha.toInt(), red.toInt(), green.toInt(), blue.toInt())
    }
}
