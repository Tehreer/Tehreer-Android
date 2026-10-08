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
import android.text.SpannableString
import android.text.Spanned
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.util.StringUtils
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import java.util.NavigableMap

/**
 * Represents a typesetter which performs text layout. It can be used to create lines, perform line
 * breaking, and do other contextual analysis based on the characters in the string.
 */
class Typesetter private constructor(
    private val text: String,

    /**
     * The spanned source text for which this typesetter object was created.
     */
    val spanned: Spanned,
    defaultSpans: List<Any>?
) {
    private class Finalizable(private val nativeTypesetter: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeTypesetter)
        }
    }

    internal val nativeTypesetter: Long
    private val typefaces: Map<Long, Typeface>
    private val holders: NavigableMap<Int, ReplacementHolder>
    private val finalizable: Finalizable

    init {
        val input = TypesetterInput(text, spanned, defaultSpans ?: emptyList())

        nativeTypesetter = input.nativeTypesetter
        typefaces = input.typefaces
        holders = input.holders
        finalizable = Finalizable(nativeTypesetter)
    }

    /**
     * Constructs the typesetter object using given text, typeface and type size.
     *
     * @param text The text to typeset.
     * @param typeface The typeface to use.
     * @param typeSize The type size to apply.
     *
     * @throws IllegalArgumentException if `text` is empty.
     */
    constructor(text: String, typeface: Typeface, typeSize: Float) : this(
        text, makeSpanned(text, typeface, typeSize), null
    )

    /**
     * Constructs the typesetter object using a spanned text.
     *
     * @param spanned The spanned text to typeset.
     *
     * @throws IllegalArgumentException if `spanned` is empty.
     */
    constructor(spanned: Spanned) : this(spanned, null)

    constructor(spanned: Spanned, defaultSpans: List<Any>?) : this(
        copyText(spanned), spanned, defaultSpans
    )

    private fun checkSubRange(charStart: Int, charEnd: Int) {
        require(charStart >= 0) { "Char Start: $charStart" }
        require(charEnd <= text.length) { "Char End: $charEnd, Text Length: ${text.length}" }
        require(charEnd > charStart) { "Bad Range: [$charStart, $charEnd)" }
    }

    /**
     * Suggests a forward break index based on the provided range and width. The measurement
     * proceeds from first character to last character. If there is still room after measuring all
     * characters, then last index is returned. Otherwise, break index is returned.
     *
     * @param charStart The index to the first character (inclusive) for break calculations.
     * @param charEnd The index to the last character (exclusive) for break calculations.
     * @param breakExtent The requested break extent.
     * @param breakMode The requested break mode.
     * @return The index (exclusive) that would cause the break.
     *
     * @throws IllegalArgumentException if `charStart` is negative, or `charEnd` is greater than the
     *         length of source text, or `charStart` is greater than or equal to `charEnd`
     */
    fun suggestForwardBreak(
        charStart: Int, charEnd: Int, breakExtent: Float, breakMode: BreakMode
    ): Int {
        checkSubRange(charStart, charEnd)

        return nSuggestForwardBreak(nativeTypesetter, charStart, charEnd, breakExtent, breakMode.ordinal)
    }

    /**
     * Suggests a backward break index based on the provided range and width. The measurement
     * proceeds from last character to first character. If there is still room after measuring all
     * characters, then first index is returned. Otherwise, break index is returned.
     *
     * @param charStart The index to the first character (inclusive) for break calculations.
     * @param charEnd The index to the last character (exclusive) for break calculations.
     * @param breakExtent The requested break extent.
     * @param breakMode The requested break mode.
     * @return The index (inclusive) that would cause the break.
     *
     * @throws IllegalArgumentException if `charStart` is negative, or `charEnd` is greater than the
     *         length of source text, or `charStart` is greater than or equal to `charEnd`
     */
    fun suggestBackwardBreak(
        charStart: Int, charEnd: Int, breakExtent: Float, breakMode: BreakMode
    ): Int {
        checkSubRange(charStart, charEnd)

        return nSuggestBackwardBreak(nativeTypesetter, charStart, charEnd, breakExtent, breakMode.ordinal)
    }

    /**
     * Creates a simple line of specified string range.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @return The new line object.
     *
     * @throws IllegalArgumentException if `charStart` is negative, or `charEnd` is greater than the
     *         length of source text, or `charStart` is greater than or equal to `charEnd`
     */
    fun createSimpleLine(charStart: Int, charEnd: Int): ComposedLine {
        checkSubRange(charStart, charEnd)

        return makeLine(nCreateSimpleLine(nativeTypesetter, charStart, charEnd))
    }

    /**
     * Creates a line of specified string range, truncating it with ellipsis character (U+2026) or
     * three dots if it overflows the max width.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @param maxWidth The width at which truncation will begin.
     * @param breakMode The truncation mode to be used on the line.
     * @param truncationPlace The place of truncation for the line.
     * @return The new line which is truncated if it overflows the `maxWidth`.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *  - `charStart` is negative
     *  - `charEnd` is greater than the length of source text
     *  - `charStart` is greater than or equal to `charEnd`
     */
    fun createTruncatedLine(
        charStart: Int, charEnd: Int, maxWidth: Float,
        breakMode: BreakMode, truncationPlace: TruncationPlace
    ): ComposedLine {
        checkSubRange(charStart, charEnd)

        val token = makeLine(
            nCreateTruncationToken(nativeTypesetter, charStart, charEnd, truncationPlace.ordinal, null)
        )

        return createTruncated(charStart, charEnd, maxWidth, breakMode, truncationPlace, token)
    }

    /**
     * Creates a line of specified string range, truncating it if it overflows the max width.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @param maxWidth The width at which truncation will begin.
     * @param breakMode The truncation mode to be used on the line.
     * @param truncationPlace The place of truncation for the line.
     * @param truncationToken The token to indicate the line truncation.
     * @return The new line which is truncated if it overflows the `maxWidth`.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *  - `charStart` is negative
     *  - `charEnd` is greater than the length of source text
     *  - `charStart` is greater than or equal to `charEnd`
     *  - `truncationToken` is empty
     */
    fun createTruncatedLine(
        charStart: Int, charEnd: Int, maxWidth: Float,
        breakMode: BreakMode, truncationPlace: TruncationPlace, truncationToken: String
    ): ComposedLine {
        checkSubRange(charStart, charEnd)
        require(truncationToken.isNotEmpty()) { "Truncation token is empty" }

        val token = makeLine(
            nCreateTruncationToken(
                nativeTypesetter, charStart, charEnd, truncationPlace.ordinal, truncationToken
            )
        )

        return createTruncated(charStart, charEnd, maxWidth, breakMode, truncationPlace, token)
    }

    /**
     * Creates a line of specified string range, truncating it if it overflows the max width.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @param maxWidth The width at which truncation will begin.
     * @param breakMode The truncation mode to be used on the line.
     * @param truncationPlace The place of truncation for the line.
     * @param truncationToken The token to indicate the line truncation.
     * @return The new line which is truncated if it overflows the `maxWidth`.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *  - `charStart` is negative
     *  - `charEnd` is greater than the length of source text
     *  - `charStart` is greater than or equal to `charEnd`
     */
    fun createTruncatedLine(
        charStart: Int, charEnd: Int, maxWidth: Float,
        breakMode: BreakMode, truncationPlace: TruncationPlace, truncationToken: ComposedLine
    ): ComposedLine {
        checkSubRange(charStart, charEnd)

        return createTruncated(charStart, charEnd, maxWidth, breakMode, truncationPlace, truncationToken)
    }

    /**
     * Creates a justified line of specified string range.
     *
     * @param charStart The index to first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @param justificationFactor The factor that specifies the full or partial justification. When
     *                            set to 1.0 or greater, full justification is performed. If this
     *                            parameter is set to less than 1.0, varying degrees of partial
     *                            justification are performed. If it is set to 0 or less, no
     *                            justification is performed.
     * @param justificationWidth The width at which the line should be justified. If it is less than
     *                           the actual width of the line, then negative justification is
     *                           performed (that is, words are squeezed together).
     * @return The new justified line.
     */
    fun createJustifiedLine(
        charStart: Int, charEnd: Int, justificationFactor: Float, justificationWidth: Float
    ): ComposedLine {
        checkSubRange(charStart, charEnd)

        return makeLine(
            nCreateJustifiedLine(
                nativeTypesetter, charStart, charEnd, justificationFactor, justificationWidth
            )
        )
    }

    /**
     * Creates a frame full of lines in the rectangle provided by the `frameRect` parameter. The
     * typesetter will continue to fill the frame until it either runs out of text or it finds that
     * text no longer fits.
     *
     * @param charStart The index to first character of the frame in source text.
     * @param charEnd The index after the last character of the frame in source text.
     * @param frameRect The rectangle specifying the frame to fill.
     * @param textAlignment The horizontal text alignment of the lines in frame.
     * @return The new frame object.
     */
    fun createFrame(
        charStart: Int, charEnd: Int, frameRect: RectF, textAlignment: TextAlignment
    ): ComposedFrame {
        checkSubRange(charStart, charEnd)
        require(!frameRect.isEmpty) { "Frame rect is empty" }

        val resolver = FrameResolver()
        resolver.typesetter = this
        resolver.frameBounds = frameRect
        resolver.textAlignment = textAlignment

        return resolver.createFrame(charStart, charEnd)
    }

    private fun createTruncated(
        charStart: Int, charEnd: Int, maxWidth: Float, breakMode: BreakMode,
        truncationPlace: TruncationPlace, token: ComposedLine
    ): ComposedLine {
        return makeLine(
            nCreateTruncatedLine(
                nativeTypesetter, charStart, charEnd, maxWidth, breakMode.ordinal,
                truncationPlace.ordinal, token.nativeLine
            )
        )
    }

    private fun makeLine(nativeLine: Long): ComposedLine {
        if (nativeLine == 0L) {
            throw RuntimeException("Could not create the line")
        }

        return ComposedLine(nativeLine, typefaces, holders)
    }

    /**
     * Wraps a frame of Core, which is made from this typesetter.
     */
    internal fun makeFrame(nativeFrame: Long, originX: Float, originY: Float): ComposedFrame {
        if (nativeFrame == 0L) {
            throw RuntimeException("Could not create the frame")
        }

        return ComposedFrame(nativeFrame, originX, originY, spanned, typefaces, holders)
    }

    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        fun makeSpanned(text: String, typeface: Typeface, typeSize: Float): Spanned {
            require(text.isNotEmpty()) { "Text is empty" }

            val spanned = SpannableString(text)
            spanned.setSpan(TypefaceSpan(typeface), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            spanned.setSpan(TypeSizeSpan(typeSize), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            return spanned
        }

        fun copyText(spanned: Spanned): String {
            require(spanned.isNotEmpty()) { "Text is empty" }

            return StringUtils.copyString(spanned)
        }

        @JvmStatic external fun nDispose(nativeTypesetter: Long)

        @JvmStatic external fun nSuggestForwardBreak(
            nativeTypesetter: Long, charStart: Int, charEnd: Int, extent: Float, breakMode: Int
        ): Int
        @JvmStatic external fun nSuggestBackwardBreak(
            nativeTypesetter: Long, charStart: Int, charEnd: Int, extent: Float, breakMode: Int
        ): Int

        @JvmStatic external fun nCreateSimpleLine(
            nativeTypesetter: Long, charStart: Int, charEnd: Int
        ): Long
        @JvmStatic external fun nCreateTruncationToken(
            nativeTypesetter: Long, charStart: Int, charEnd: Int, place: Int, token: String?
        ): Long
        @JvmStatic external fun nCreateTruncatedLine(
            nativeTypesetter: Long, charStart: Int, charEnd: Int, extent: Float,
            breakMode: Int, place: Int, nativeToken: Long
        ): Long
        @JvmStatic external fun nCreateJustifiedLine(
            nativeTypesetter: Long, charStart: Int, charEnd: Int, factor: Float, extent: Float
        ): Long
    }
}
