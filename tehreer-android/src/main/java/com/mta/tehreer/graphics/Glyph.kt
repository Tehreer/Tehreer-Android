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

package com.mta.tehreer.graphics

import android.graphics.Path

internal class Glyph {
    var type = 0
    var image: GlyphImage? = null
    var outline: GlyphOutline? = null
    var path: Path? = null

    val isLoaded: Boolean
        get() = type != 0

    companion object {
        const val TYPE_MASK = 0x0001
        const val TYPE_COLOR = 0x0002
        const val TYPE_MIXED = 0x0003
    }
}
