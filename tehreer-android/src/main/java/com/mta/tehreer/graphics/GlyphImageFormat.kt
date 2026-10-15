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
package com.mta.tehreer.graphics

/**
 * Specifies the pixel format of a [GlyphImage].
 */
enum class GlyphImageFormat(internal val value: Int) {
    /**
     * One byte for each pixel, holding its coverage. The bitmap of the image is `ALPHA_8`.
     */
    ALPHA(0),

    /**
     * Four bytes for each pixel, holding its color with the channels premultiplied by alpha. The
     * bitmap of the image is `ARGB_8888`.
     */
    ARGB(1)
}
