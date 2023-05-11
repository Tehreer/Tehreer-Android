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

import androidx.annotation.IntDef
import com.mta.tehreer.internal.sfnt.SfntTable

internal class PaletteTypesArray(
    private val data: SfntTable
) {
    @IntDef(flag = true, value = [USABLE_WITH_LIGHT_BACKGROUND, USABLE_WITH_DARK_BACKGROUND])
    @Retention(AnnotationRetention.SOURCE)
    annotation class Flags

    @Flags
    operator fun get(index: Int) = data.readInt32(index * 4)

    companion object {
        const val USABLE_WITH_LIGHT_BACKGROUND = 0x0001
        const val USABLE_WITH_DARK_BACKGROUND = 0x0002
    }
}
