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

package com.mta.tehreer.layout

import android.graphics.RectF
import androidx.annotation.FloatRange
import com.mta.tehreer.internal.JniBridge

/**
 * This class resolves text frames by using a typesetter object.
 */
class FrameResolver {
    /**
     * The typesetter to use for resolving frames.
     */
    var typesetter: Typesetter? = null

    private val _frameBounds = RectF(0f, 0f, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)

    /**
     * The rectangle specifying the frame bounds. The default value is an infinite rectangle at zero
     * origin.
     */
    var frameBounds: RectF
        get() = RectF(_frameBounds)
        set(value) {
            _frameBounds.set(value)
        }

    /**
     * Whether or not to tightly fit the lines horizontally in a frame. If enabled, the resulting
     * frame will have a minimum width that tightly encloses all the lines of specified text. The
     * default value is `false`.
     */
    var fitsHorizontally = false

    /**
     * Whether or not to tightly fit the lines vertically in a frame. If enabled, the resulting
     * frame will have a minimum height that tightly encloses all the lines of specified text. The
     * default value is `false`.
     */
    var fitsVertically = false

    /**
     * The text alignment to apply on each line of a frame. The default value is
     * [TextAlignment.LEADING].
     */
    var textAlignment = TextAlignment.LEADING

    /**
     * The vertical alignment to apply on the contents of a frame. The default value is
     * [VerticalAlignment.TOP].
     */
    var verticalAlignment = VerticalAlignment.TOP

    /**
     * The truncation mode to apply on the last line of a frame in case of overflow. The default
     * value is [BreakMode.LINE].
     */
    var truncationMode = BreakMode.LINE

    /**
     * The truncation place for the last line of a frame. The truncation is disabled if the value is
     * `null`.
     */
    var truncationPlace: TruncationPlace? = null

    /**
     * Whether or not to justify the lines in a frame. The default value is `false`.
     */
    var isJustificationEnabled = false

    /**
     * The justification level which can range from 0.0 to 1.0. A lower value increases the
     * tightness between words while a higher value decreases it. The default value is `1.0f`.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    @set:FloatRange(from = 0.0, to = 1.0)
    var justificationLevel = 1.0f

    /**
     * The maximum number of lines that a frame should consist of.
     */
    var maxLines = 0

    /**
     * The extra spacing in pixels to add after each line of a frame. It is resolved before line
     * height multiplier. The default value is zero.
     *
     * The extra spacing is added in the leading of each line soon after it is composed.
     *
     * @see lineHeightMultiplier
     */
    var extraLineSpacing = 0.0f

    /**
     * The height multiplier to apply on each line of a frame. It is resolved after extra line
     * spacing. The default value is zero, which means that it is not applied.
     *
     * The additional spacing is adjusted in such a way that text remains in the middle of the line.
     *
     * @see extraLineSpacing
     */
    var lineHeightMultiplier = 0.0f

    private fun nativeTextAlignment(): Int {
        return when (textAlignment) {
            TextAlignment.CENTER -> 1
            TextAlignment.RIGHT -> 2
            TextAlignment.LEADING -> 3
            TextAlignment.TRAILING -> 4
            else -> 0
        }
    }

    /**
     * Creates a frame representing specified string range in source text.
     *
     * The resolver keeps on filling the frame until it either runs out of text or it finds that
     * text no longer fits in frame bounds. The resulting frame consists of at least one line even
     * if frame bounds are smaller.
     *
     * @param charStart The index to first character of the frame in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @return A new composed frame.
     *
     * @throws IllegalArgumentException if `charStart` is negative, or `charEnd` is greater than the
     *         length of source text, or `charStart` is greater than or equal to `charEnd`.
     */
    fun createFrame(charStart: Int, charEnd: Int): ComposedFrame {
        val typesetter = checkNotNull(typesetter) { "Typesetter is not set" }
        val textLength = typesetter.spanned.length

        require(charStart >= 0) { "Char Start: $charStart" }
        require(charEnd <= textLength) { "Char End: $charEnd, Text Length: $textLength" }
        require(charEnd > charStart) { "Bad Range: [$charStart, $charEnd)" }

        val nativeFrame = nCreateFrame(
            typesetter.nativeTypesetter, charStart, charEnd,
            _frameBounds.width(), _frameBounds.height(),
            fitsHorizontally, fitsVertically,
            nativeTextAlignment(), verticalAlignment.ordinal,
            truncationMode.ordinal, truncationPlace?.ordinal ?: -1,
            isJustificationEnabled, justificationLevel, maxLines,
            extraLineSpacing, lineHeightMultiplier
        )

        return typesetter.makeFrame(nativeFrame, _frameBounds.left, _frameBounds.top)
    }

    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nCreateFrame(
            nativeTypesetter: Long, charStart: Int, charEnd: Int,
            width: Float, height: Float,
            fitsHorizontally: Boolean, fitsVertically: Boolean,
            textAlignment: Int, verticalAlignment: Int,
            truncationMode: Int, truncationPlace: Int,
            isJustificationEnabled: Boolean, justificationLevel: Float, maxLines: Int,
            extraLineSpacing: Float, lineHeightMultiplier: Float
        ): Long
    }
}
