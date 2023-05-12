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

import androidx.annotation.IntDef
import kotlin.math.roundToInt

internal class GlyphAttributes {
    @IntDef(LINECAP_BUTT, LINECAP_ROUND, LINECAP_SQUARE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LineCap

    @IntDef(LINEJOIN_ROUND, LINEJOIN_BEVEL, LINEJOIN_MITER_VARIABLE, LINEJOIN_MITER_FIXED)
    @Retention(AnnotationRetention.SOURCE)
    annotation class LineJoin

    private val dataKey = GlyphKey.Data()
    private val colorKey = GlyphKey.Color()
    private val strokeKey = GlyphKey.Stroke()

    fun setTypeface(typeface: Typeface?) {
        dataKey.typeface = typeface
    }

    fun setPixelWidth(pixelWidth: Float) {
        dataKey.pixelWidth = (pixelWidth * 64.0f).roundToInt()
    }

    fun setPixelHeight(pixelHeight: Float) {
        dataKey.pixelHeight = (pixelHeight * 64.0f).roundToInt()
    }

    fun setSkewX(skewX: Float) {
        dataKey.skewX = (skewX * 0x10000).roundToInt()
    }

    fun setForegroundColor(foregroundColor: Int) {
        colorKey.foregroundColor = foregroundColor
    }

    fun setLineRadius(lineRadius: Float) {
        strokeKey.lineRadius = (lineRadius * 64.0f).roundToInt()
    }

    fun setLineCap(lineCap: Int) {
        strokeKey.lineCap = lineCap
    }

    fun setLineJoin(lineJoin: Int) {
        strokeKey.lineJoin = lineJoin
    }

    fun setMiterLimit(miterLimit: Float) {
        strokeKey.miterLimit = (miterLimit * 0x10000).roundToInt()
    }

    // Minimum size supported by FreeType is 64x64.
    val isRenderable: Boolean
        get() = dataKey.pixelWidth >= 64 && dataKey.pixelHeight >= 64

    fun dataKey(): GlyphKey.Data {
        return dataKey
    }

    fun colorKey(): GlyphKey.Color {
        colorKey.set(dataKey)
        return colorKey
    }

    fun strokeKey(): GlyphKey.Stroke {
        strokeKey.set(dataKey)
        return strokeKey
    }

    companion object {
        const val LINECAP_BUTT = 0
        const val LINECAP_ROUND = 1
        const val LINECAP_SQUARE = 2

        const val LINEJOIN_ROUND = 0
        const val LINEJOIN_BEVEL = 1
        const val LINEJOIN_MITER_VARIABLE = 2
        const val LINEJOIN_MITER_FIXED = 3
    }
}
