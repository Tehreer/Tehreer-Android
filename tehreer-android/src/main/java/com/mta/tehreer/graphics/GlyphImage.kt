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

import android.graphics.Bitmap
import com.mta.tehreer.internal.JniBridge

/**
 * The rendered image of a glyph, which a [Renderer] makes with [Renderer.getGlyphImage] or
 * [Renderer.getStrokeImage]. The pixels of an image never change, so it can be shared between
 * threads.
 *
 * The image of an outline is rendered at the size that was asked for. The image of a glyph of a
 * bitmap font is the one of its nearest strike, which is not scaled to the type size.
 */
class GlyphImage internal constructor(private val nativeImage: Long) {
    private class Finalizable(private val nativeImage: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeImage)
        }
    }

    private val finalizable = Finalizable(nativeImage)

    /**
     * The pixel format of this image.
     */
    val format: GlyphImageFormat
        get() = GlyphImageFormat.entries.first { it.value == nGetFormat(nativeImage) }

    /**
     * The distance from the origin of the glyph to the left edge of this image. The origin is the
     * pen position, and the distance grows to the right.
     */
    val left: Int
        get() = nGetLeft(nativeImage)

    /**
     * The distance from the baseline of the glyph to the top edge of this image. The distance
     * grows upward, so it is positive for the part of the glyph that is above the baseline.
     */
    val top: Int
        get() = nGetTop(nativeImage)

    /**
     * The width of this image in pixels.
     */
    val width: Int
        get() = nGetWidth(nativeImage)

    /**
     * The height of this image in pixels.
     */
    val height: Int
        get() = nGetHeight(nativeImage)

    /**
     * The bitmap that has the pixels of this image, which is [Bitmap.Config.ALPHA_8] for
     * [GlyphImageFormat.ALPHA] and [Bitmap.Config.ARGB_8888] for [GlyphImageFormat.ARGB].
     *
     * The bitmap is made once, when it is asked for first, and it is the very one that drawing
     * uses for this image. It is shared, so it **must not be modified**.
     */
    val bitmap: Bitmap
        get() = checkNotNull(nGetBitmap(nativeImage)) { "The image has no pixels" }

    private external fun nGetFormat(nativeImage: Long): Int
    private external fun nGetLeft(nativeImage: Long): Int
    private external fun nGetTop(nativeImage: Long): Int
    private external fun nGetWidth(nativeImage: Long): Int
    private external fun nGetHeight(nativeImage: Long): Int
    private external fun nGetBitmap(nativeImage: Long): Bitmap?

    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nDispose(nativeImage: Long)
    }
}
