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

import android.graphics.Color
import android.graphics.Path
import androidx.annotation.ColorInt
import com.mta.tehreer.Disposable
import com.mta.tehreer.internal.JniBridge.loadLibrary

internal class GlyphRasterizer(key: GlyphKey) : Disposable {
    private val nativeRasterizer: Long

    init {
        nativeRasterizer = nCreate(
            key.typeface!!.nativeTypeface,
            key.pixelWidth, key.pixelHeight,
            0x10000, -key.skewX, 0, 0x10000
        )
    }

    fun getGlyphType(glyphId: Int): Int {
        return nGetGlyphType(nativeRasterizer, glyphId)
    }

    fun getGlyphImage(glyphId: Int, @ColorInt foregroundColor: Int = Color.TRANSPARENT): GlyphImage? {
        return nGetGlyphImage(nativeRasterizer, glyphId, foregroundColor)
    }

    fun getStrokeImage(
        glyphOutline: GlyphOutline,
        lineRadius: Int,
        lineCap: Int,
        lineJoin: Int,
        miterLimit: Int
    ): GlyphImage? {
        return nGetStrokeImage(
            nativeRasterizer, glyphOutline.nativeOutline,
            lineRadius, lineCap, lineJoin, miterLimit
        )
    }

    fun getGlyphOutline(glyphId: Int): GlyphOutline? {
        val nativeOutline = nGetGlyphOutline(nativeRasterizer, glyphId)
        return if (nativeOutline != 0L) GlyphOutline(nativeOutline) else null
    }

    fun getGlyphPath(glyphId: Int): Path {
        return nGetGlyphPath(nativeRasterizer, glyphId)
    }

    override fun dispose() {
        nDispose(nativeRasterizer)
    }

    companion object {
        init {
            loadLibrary()
        }

        @JvmStatic private external fun nCreate(
            nativeTypeface: Long, pixelWidth: Int, pixelHeight: Int,
            transformXX: Int, transformXY: Int, transformYX: Int, transformYY: Int
        ): Long
        @JvmStatic private external fun nDispose(nativeRasterizer: Long)

        @JvmStatic private external fun nGetGlyphType(nativeRasterizer: Long, glyphId: Int): Int

        @JvmStatic private external fun nGetGlyphImage(
            nativeRasterizer: Long,
            glyphId: Int,
            foregroundColor: Int
        ): GlyphImage?

        @JvmStatic private external fun nGetStrokeImage(
            nativeRasterizer: Long, nativeOutline: Long,
            lineRadius: Int, lineCap: Int, lineJoin: Int, miterLimit: Int
        ): GlyphImage?

        @JvmStatic private external fun nGetGlyphOutline(nativeRasterizer: Long, glyphId: Int): Long

        @JvmStatic private external fun nGetGlyphPath(nativeRasterizer: Long, glyphId: Int): Path
    }
}
