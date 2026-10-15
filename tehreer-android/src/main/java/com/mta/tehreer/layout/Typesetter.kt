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

import android.text.SpannableString
import android.text.Spanned
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan

/**
 * Represents a typesetter which performs text layout. It can be used to create lines, perform line
 * breaking, and do other contextual analysis based on the characters in the string.
 */
class Typesetter private constructor(
    private val source: TText,

    /**
     * The spanned source text for which this typesetter object was created.
     */
    val spanned: Spanned
) {
    private class Finalizable(private val nativeTypesetter: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeTypesetter)
        }
    }

    internal val nativeTypesetter: Long
    private val textLength: Int
    private val typefaces: Map<Long, Typeface>
    private val holders: Map<Long, ReplacementHolder>
    private val finalizable: Finalizable

    init {
        require(source.length > 0) { "Text is empty" }

        nativeTypesetter = nCreateTypesetter(source.nativeText)
        check(nativeTypesetter != 0L) { "Could not create the typesetter" }

        // The text can be edited later on, while the lines made here keep the typefaces and
        // replacements that the typesetter was made with.
        textLength = source.length
        typefaces = HashMap(source.typefaces)
        holders = HashMap(source.holders)
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
        makeSpanned(text, typeface, typeSize)
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
        makeText(spanned, defaultSpans ?: emptyList()), spanned
    )

    /**
     * Constructs the typesetter object using a text of Core. The text is copied, so it can be
     * edited afterwards without changing the typesetter. The spanned text of the typesetter is
     * made of the text with [TText.toSpanned].
     *
     * @param text The text to typeset, which must not be in the middle of a group of edits.
     *
     * @throws IllegalArgumentException if `text` is empty.
     */
    constructor(text: TText) : this(text, text.toSpanned())

    private fun checkSubRange(fromIndex: Int, toIndex: Int) {
        require(fromIndex >= 0) { "From Index: $fromIndex" }
        require(toIndex <= textLength) { "To Index: $toIndex, Text Length: $textLength" }
        require(toIndex > fromIndex) { "Bad Range: [$fromIndex, $toIndex)" }
    }

    /**
     * Suggests a forward break index based on the provided range and width. The measurement
     * proceeds from first character to last character. If there is still room after measuring all
     * characters, then last index is returned. Otherwise, break index is returned.
     *
     * @param fromIndex The index to the first character (inclusive) for break calculations.
     * @param toIndex The index to the last character (exclusive) for break calculations.
     * @param breakExtent The requested break extent.
     * @param breakMode The requested break mode.
     * @return The index (exclusive) that would cause the break.
     *
     * @throws IllegalArgumentException if `fromIndex` is negative, or `toIndex` is greater than the
     *         length of source text, or `fromIndex` is greater than or equal to `toIndex`
     */
    fun suggestForwardBreak(
        fromIndex: Int, toIndex: Int, breakExtent: Float, breakMode: BreakMode
    ): Int {
        checkSubRange(fromIndex, toIndex)

        return nSuggestForwardBreak(nativeTypesetter, fromIndex, toIndex, breakExtent, breakMode.ordinal)
    }

    /**
     * Suggests a backward break index based on the provided range and width. The measurement
     * proceeds from last character to first character. If there is still room after measuring all
     * characters, then first index is returned. Otherwise, break index is returned.
     *
     * @param fromIndex The index to the first character (inclusive) for break calculations.
     * @param toIndex The index to the last character (exclusive) for break calculations.
     * @param breakExtent The requested break extent.
     * @param breakMode The requested break mode.
     * @return The index (inclusive) that would cause the break.
     *
     * @throws IllegalArgumentException if `fromIndex` is negative, or `toIndex` is greater than the
     *         length of source text, or `fromIndex` is greater than or equal to `toIndex`
     */
    fun suggestBackwardBreak(
        fromIndex: Int, toIndex: Int, breakExtent: Float, breakMode: BreakMode
    ): Int {
        checkSubRange(fromIndex, toIndex)

        return nSuggestBackwardBreak(nativeTypesetter, fromIndex, toIndex, breakExtent, breakMode.ordinal)
    }

    /**
     * Creates a simple line of specified string range.
     *
     * @param fromIndex The index to first character of the line in source text.
     * @param toIndex The index after the last character of the line in source text.
     * @param layoutWidth The width of the frame that the line is made for, or zero if there is
     *                    none. The replacements that decide their room by the width of the frame,
     *                    such as a view that fills it, get it from here.
     * @return The new line object.
     *
     * @throws IllegalArgumentException if `fromIndex` is negative, or `toIndex` is greater than the
     *         length of source text, or `fromIndex` is greater than or equal to `toIndex`
     */
    @JvmOverloads
    fun createSimpleLine(fromIndex: Int, toIndex: Int, layoutWidth: Float = 0.0f): ComposedLine {
        checkSubRange(fromIndex, toIndex)

        return makeLine(nCreateSimpleLine(nativeTypesetter, fromIndex, toIndex, layoutWidth))
    }

    /**
     * Creates a line of specified string range, truncating it with ellipsis character (U+2026) or
     * three dots if it overflows the max width.
     *
     * @param fromIndex The index to first character of the line in source text.
     * @param toIndex The index after the last character of the line in source text.
     * @param maxWidth The width at which truncation will begin.
     * @param breakMode The truncation mode to be used on the line.
     * @param truncationPlace The place of truncation for the line.
     * @return The new line which is truncated if it overflows the `maxWidth`.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *  - `fromIndex` is negative
     *  - `toIndex` is greater than the length of source text
     *  - `fromIndex` is greater than or equal to `toIndex`
     */
    fun createTruncatedLine(
        fromIndex: Int, toIndex: Int, maxWidth: Float,
        breakMode: BreakMode, truncationPlace: TruncationPlace
    ): ComposedLine {
        checkSubRange(fromIndex, toIndex)

        val token = makeLine(
            nCreateTruncationToken(nativeTypesetter, fromIndex, toIndex, truncationPlace.ordinal, null)
        )

        return createTruncated(fromIndex, toIndex, maxWidth, breakMode, truncationPlace, token)
    }

    /**
     * Creates a line of specified string range, truncating it if it overflows the max width.
     *
     * @param fromIndex The index to first character of the line in source text.
     * @param toIndex The index after the last character of the line in source text.
     * @param maxWidth The width at which truncation will begin.
     * @param breakMode The truncation mode to be used on the line.
     * @param truncationPlace The place of truncation for the line.
     * @param truncationToken The token to indicate the line truncation.
     * @return The new line which is truncated if it overflows the `maxWidth`.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *  - `fromIndex` is negative
     *  - `toIndex` is greater than the length of source text
     *  - `fromIndex` is greater than or equal to `toIndex`
     *  - `truncationToken` is empty
     */
    fun createTruncatedLine(
        fromIndex: Int, toIndex: Int, maxWidth: Float,
        breakMode: BreakMode, truncationPlace: TruncationPlace, truncationToken: String
    ): ComposedLine {
        checkSubRange(fromIndex, toIndex)
        require(truncationToken.isNotEmpty()) { "Truncation token is empty" }

        val token = makeLine(
            nCreateTruncationToken(
                nativeTypesetter, fromIndex, toIndex, truncationPlace.ordinal, truncationToken
            )
        )

        return createTruncated(fromIndex, toIndex, maxWidth, breakMode, truncationPlace, token)
    }

    /**
     * Creates a line of specified string range, truncating it if it overflows the max width.
     *
     * @param fromIndex The index to first character of the line in source text.
     * @param toIndex The index after the last character of the line in source text.
     * @param maxWidth The width at which truncation will begin.
     * @param breakMode The truncation mode to be used on the line.
     * @param truncationPlace The place of truncation for the line.
     * @param truncationToken The token to indicate the line truncation.
     * @return The new line which is truncated if it overflows the `maxWidth`.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *  - `fromIndex` is negative
     *  - `toIndex` is greater than the length of source text
     *  - `fromIndex` is greater than or equal to `toIndex`
     */
    fun createTruncatedLine(
        fromIndex: Int, toIndex: Int, maxWidth: Float,
        breakMode: BreakMode, truncationPlace: TruncationPlace, truncationToken: ComposedLine
    ): ComposedLine {
        checkSubRange(fromIndex, toIndex)

        return createTruncated(fromIndex, toIndex, maxWidth, breakMode, truncationPlace, truncationToken)
    }

    /**
     * Creates a justified line of specified string range.
     *
     * @param fromIndex The index to first character of the line in source text.
     * @param toIndex The index after the last character of the line in source text.
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
        fromIndex: Int, toIndex: Int, justificationFactor: Float, justificationWidth: Float
    ): ComposedLine {
        checkSubRange(fromIndex, toIndex)

        return makeLine(
            nCreateJustifiedLine(
                nativeTypesetter, fromIndex, toIndex, justificationFactor, justificationWidth
            )
        )
    }

    private fun createTruncated(
        fromIndex: Int, toIndex: Int, maxWidth: Float, breakMode: BreakMode,
        truncationPlace: TruncationPlace, token: ComposedLine
    ): ComposedLine {
        return makeLine(
            nCreateTruncatedLine(
                nativeTypesetter, fromIndex, toIndex, maxWidth, breakMode.ordinal,
                truncationPlace.ordinal, token.nativeLine, token
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
    internal fun makeFrame(nativeFrame: Long): ComposedFrame {
        if (nativeFrame == 0L) {
            throw RuntimeException("Could not create the frame")
        }

        return ComposedFrame(nativeFrame, spanned, typefaces, holders)
    }

    private external fun nSuggestForwardBreak(
        nativeTypesetter: Long, fromIndex: Int, toIndex: Int, extent: Float, breakMode: Int
    ): Int
    private external fun nSuggestBackwardBreak(
        nativeTypesetter: Long, fromIndex: Int, toIndex: Int, extent: Float, breakMode: Int
    ): Int

    private external fun nCreateSimpleLine(
        nativeTypesetter: Long, fromIndex: Int, toIndex: Int, layoutWidth: Float
    ): Long
    private external fun nCreateTruncationToken(
        nativeTypesetter: Long, fromIndex: Int, toIndex: Int, place: Int, token: String?
    ): Long
    private external fun nCreateTruncatedLine(
        nativeTypesetter: Long, fromIndex: Int, toIndex: Int, extent: Float,
        breakMode: Int, place: Int, nativeToken: Long, token: Any
    ): Long
    private external fun nCreateJustifiedLine(
        nativeTypesetter: Long, fromIndex: Int, toIndex: Int, factor: Float, extent: Float
    ): Long

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

        fun makeText(spanned: Spanned, defaultSpans: List<Any>): TText {
            require(spanned.isNotEmpty()) { "Text is empty" }

            return TText(spanned, defaultSpans)
        }

        @JvmStatic external fun nCreateTypesetter(nativeText: Long): Long
        @JvmStatic external fun nDispose(nativeTypesetter: Long)

    }
}
