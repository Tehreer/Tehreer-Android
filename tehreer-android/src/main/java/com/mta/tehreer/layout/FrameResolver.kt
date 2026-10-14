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

import androidx.annotation.FloatRange
import com.mta.tehreer.internal.JniBridge

/**
 * This class resolves text frames by using a typesetter object.
 */
class FrameResolver {
    private class Finalizable(private val nativeResolver: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeResolver)
        }
    }

    private val nativeResolver = nCreate()
    private val finalizable = Finalizable(nativeResolver)

    /**
     * The typesetter to use for resolving frames.
     */
    var typesetter: Typesetter? = null
        set(value) {
            field = value
            nSetTypesetter(nativeResolver, value?.nativeTypesetter ?: 0)
        }

    /**
     * The width of the frame. The default value is unbounded, which is `Float.MAX_VALUE`.
     */
    var frameWidth: Float
        get() = nGetFrameWidth(nativeResolver)
        set(value) {
            nSetFrameSize(nativeResolver, value, nGetFrameHeight(nativeResolver))
        }

    /**
     * The height of the frame. The default value is unbounded, which is `Float.MAX_VALUE`.
     */
    var frameHeight: Float
        get() = nGetFrameHeight(nativeResolver)
        set(value) {
            nSetFrameSize(nativeResolver, nGetFrameWidth(nativeResolver), value)
        }

    /**
     * Whether or not to tightly fit the lines horizontally in a frame. If enabled, the resulting
     * frame will have a minimum width that tightly encloses all the lines of specified text. The
     * default value is `false`.
     */
    var fitsHorizontally: Boolean
        get() = nGetFitsHorizontally(nativeResolver)
        set(value) {
            nSetFitsHorizontally(nativeResolver, value)
        }

    /**
     * Whether or not to tightly fit the lines vertically in a frame. If enabled, the resulting
     * frame will have a minimum height that tightly encloses all the lines of specified text. The
     * default value is `false`.
     */
    var fitsVertically: Boolean
        get() = nGetFitsVertically(nativeResolver)
        set(value) {
            nSetFitsVertically(nativeResolver, value)
        }

    /**
     * The text alignment to apply on each line of a frame. The default value is
     * [TextAlignment.LEADING].
     */
    var textAlignment: TextAlignment
        get() = when (nGetTextAlignment(nativeResolver)) {
            1 -> TextAlignment.CENTER
            2 -> TextAlignment.RIGHT
            3 -> TextAlignment.LEADING
            4 -> TextAlignment.TRAILING
            else -> TextAlignment.LEFT
        }
        set(value) {
            nSetTextAlignment(nativeResolver, when (value) {
                TextAlignment.LEFT -> 0
                TextAlignment.CENTER -> 1
                TextAlignment.RIGHT -> 2
                TextAlignment.LEADING -> 3
                TextAlignment.TRAILING -> 4
            })
        }

    /**
     * The vertical alignment to apply on the contents of a frame. The default value is
     * [VerticalAlignment.TOP].
     */
    var verticalAlignment: VerticalAlignment
        get() = when (nGetVerticalAlignment(nativeResolver)) {
            1 -> VerticalAlignment.MIDDLE
            2 -> VerticalAlignment.BOTTOM
            else -> VerticalAlignment.TOP
        }
        set(value) {
            nSetVerticalAlignment(nativeResolver, when (value) {
                VerticalAlignment.TOP -> 0
                VerticalAlignment.MIDDLE -> 1
                VerticalAlignment.BOTTOM -> 2
            })
        }

    /**
     * The truncation mode to apply on the last line of a frame in case of overflow. The default
     * value is [BreakMode.LINE].
     */
    var truncationMode: BreakMode
        get() = when (nGetTruncationMode(nativeResolver)) {
            1 -> BreakMode.CHARACTER
            else -> BreakMode.LINE
        }
        set(value) {
            nSetTruncationMode(nativeResolver, when (value) {
                BreakMode.LINE -> 0
                BreakMode.CHARACTER -> 1
            })
        }

    /**
     * The truncation place for the last line of a frame. It takes effect only when the truncation
     * is enabled. The default value is [TruncationPlace.END].
     *
     * @see isTruncationEnabled
     */
    var truncationPlace: TruncationPlace
        get() = when (nGetTruncationPlace(nativeResolver)) {
            0 -> TruncationPlace.START
            1 -> TruncationPlace.MIDDLE
            else -> TruncationPlace.END
        }
        set(value) {
            nSetTruncationPlace(nativeResolver, when (value) {
                TruncationPlace.START -> 0
                TruncationPlace.MIDDLE -> 1
                TruncationPlace.END -> 2
            })
        }

    /**
     * Whether or not to truncate the last line of a frame in case of overflow. The default value
     * is `false`.
     *
     * @see truncationPlace
     */
    var isTruncationEnabled: Boolean
        get() = nIsTruncationEnabled(nativeResolver)
        set(value) {
            nSetTruncationEnabled(nativeResolver, value)
        }

    /**
     * Whether or not to justify the lines in a frame. The default value is `false`.
     */
    var isJustificationEnabled: Boolean
        get() = nIsJustificationEnabled(nativeResolver)
        set(value) {
            nSetJustificationEnabled(nativeResolver, value)
        }

    /**
     * The justification level which can range from 0.0 to 1.0. A lower value increases the
     * tightness between words while a higher value decreases it. The default value is `1.0f`.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    @set:FloatRange(from = 0.0, to = 1.0)
    var justificationLevel: Float
        get() = nGetJustificationLevel(nativeResolver)
        set(value) {
            nSetJustificationLevel(nativeResolver, value)
        }

    /**
     * The maximum number of lines that a frame should consist of. The default value is zero, which
     * means that the number of lines is not limited.
     */
    var maxLines: Int
        get() = nGetMaxLines(nativeResolver)
        set(value) {
            nSetMaxLines(nativeResolver, value)
        }

    /**
     * The extra spacing in pixels to add after each line of a frame. It is resolved after line
     * height multiplier. The default value is zero.
     *
     * The extra spacing is added in the leading of each line soon after it is composed.
     *
     * @see lineHeightMultiplier
     */
    var extraLineSpacing: Float
        get() = nGetExtraLineSpacing(nativeResolver)
        set(value) {
            nSetExtraLineSpacing(nativeResolver, value)
        }

    /**
     * The height multiplier to apply on each line of a frame. It is resolved before extra line
     * spacing, and it is ignored if it is not positive. The default value is `1.0f`.
     *
     * The additional spacing is adjusted in such a way that text remains in the middle of the line.
     *
     * @see extraLineSpacing
     */
    var lineHeightMultiplier: Float
        get() = nGetLineHeightMultiplier(nativeResolver)
        set(value) {
            nSetLineHeightMultiplier(nativeResolver, value)
        }

    /**
     * Creates a frame representing specified string range in source text.
     *
     * The resolver keeps on filling the frame until it either runs out of text or it finds that
     * text no longer fits in frame size. The resulting frame consists of at least one line even
     * if frame size is smaller.
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

        val nativeFrame = nCreateFrame(nativeResolver, charStart, charEnd)

        return typesetter.makeFrame(nativeFrame)
    }

    private external fun nSetTypesetter(nativeResolver: Long, nativeTypesetter: Long)
    private external fun nGetFrameWidth(nativeResolver: Long): Float
    private external fun nGetFrameHeight(nativeResolver: Long): Float
    private external fun nSetFrameSize(nativeResolver: Long, width: Float, height: Float)
    private external fun nGetFitsHorizontally(nativeResolver: Long): Boolean
    private external fun nSetFitsHorizontally(nativeResolver: Long, fits: Boolean)
    private external fun nGetFitsVertically(nativeResolver: Long): Boolean
    private external fun nSetFitsVertically(nativeResolver: Long, fits: Boolean)
    private external fun nGetTextAlignment(nativeResolver: Long): Int
    private external fun nSetTextAlignment(nativeResolver: Long, textAlignment: Int)
    private external fun nGetVerticalAlignment(nativeResolver: Long): Int
    private external fun nSetVerticalAlignment(nativeResolver: Long, verticalAlignment: Int)
    private external fun nGetTruncationMode(nativeResolver: Long): Int
    private external fun nSetTruncationMode(nativeResolver: Long, truncationMode: Int)
    private external fun nGetTruncationPlace(nativeResolver: Long): Int
    private external fun nSetTruncationPlace(nativeResolver: Long, truncationPlace: Int)
    private external fun nIsTruncationEnabled(nativeResolver: Long): Boolean
    private external fun nSetTruncationEnabled(nativeResolver: Long, isEnabled: Boolean)
    private external fun nIsJustificationEnabled(nativeResolver: Long): Boolean
    private external fun nSetJustificationEnabled(nativeResolver: Long, isEnabled: Boolean)
    private external fun nGetJustificationLevel(nativeResolver: Long): Float
    private external fun nSetJustificationLevel(nativeResolver: Long, justificationLevel: Float)
    private external fun nGetMaxLines(nativeResolver: Long): Int
    private external fun nSetMaxLines(nativeResolver: Long, maxLines: Int)
    private external fun nGetExtraLineSpacing(nativeResolver: Long): Float
    private external fun nSetExtraLineSpacing(nativeResolver: Long, extraLineSpacing: Float)
    private external fun nGetLineHeightMultiplier(nativeResolver: Long): Float
    private external fun nSetLineHeightMultiplier(nativeResolver: Long, multiplier: Float)
    private external fun nCreateFrame(nativeResolver: Long, charStart: Int, charEnd: Int): Long

    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nCreate(): Long
        @JvmStatic external fun nDispose(nativeResolver: Long)
    }
}
