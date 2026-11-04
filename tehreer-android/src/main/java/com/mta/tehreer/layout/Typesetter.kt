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

import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.ScaleXSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.layout.style.DecorationColorSpan
import com.mta.tehreer.layout.style.OpenTypeFeaturesSpan
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.sfnt.OpenTypeFeature

/**
 * The spans that apply at an index of a [Typesetter], and how many characters they apply to.
 *
 * @property spans The spans that apply at the index.
 * @property length The number of characters, starting at the index, over which the spans stay the
 *           same.
 */
class SpanRun(val spans: List<Any>, val length: Int)

/**
 * The `Typesetter` class holds a text, which is UTF-16 characters along with the attributes of
 * their ranges (typeface, type size, colors, decorations, language, font features and so on), shapes
 * it, and creates lines from it. It can be used to create lines, perform line breaking, search for
 * boundaries, and do other contextual analysis based on the characters in the string. A
 * [FrameResolver] lays out the frames of a typesetter.
 *
 * Core shapes each paragraph of the text when the paragraph is made or changed, so lines can be
 * made over and over again for different ranges, extents and settings without shaping the text
 * again.
 *
 * A typesetter does not change once it is created, so it can be shared between threads. Use
 * [MutableTypesetter] to edit a text.
 *
 * ### Spans and attributes
 *
 * Core knows attributes, while Android knows spans, so the wrapper maps one to the other. A
 * typesetter is made from a `Spanned` by setting the attributes that its spans stand for, and
 * [toSpanned] and [getSpanRun] make spans from attributes again:
 *
 * | Attribute | Span |
 * |-----------|------|
 * | typeface | [TypefaceSpan] |
 * | type size | [TypeSizeSpan] |
 * | horizontal scale | `ScaleXSpan` |
 * | foreground color | `ForegroundColorSpan` |
 * | background color | `BackgroundColorSpan` |
 * | underline | `UnderlineSpan` |
 * | strikethrough | `StrikethroughSpan` |
 * | decoration color | [DecorationColorSpan] |
 * | font features | [OpenTypeFeaturesSpan] |
 * | replacement | the `ReplacementSpan` that was set |
 * | text alignment (leading, center, trailing) | `AlignmentSpan.Standard` |
 * | first line and other indents | `LeadingMarginSpan.Standard` |
 *
 * The spans that depend on the text around them, such as `StyleSpan`, `RelativeSizeSpan` and
 * `SuperscriptSpan`, are resolved when they are set, so they come back as the typeface, size or
 * baseline offset that they resulted in. The language, the baseline offset, the vertical scale, the
 * obliqueness and the left and right alignments have no span to come back as.
 */
open class Typesetter internal constructor(
    internal val nativeTypesetter: Long,
    /** The typefaces of the text, by the handles that Core knows them with. */
    internal val typefaces: HashMap<Long, Typeface>,
    /** The holders of the replacement spans, by the handles of their replacements in Core. */
    internal val holders: HashMap<Long, ReplacementHolder>,
    private var source: Spanned? = null
) {
    private class Finalizable(private val nativeTypesetter: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeTypesetter)
        }
    }

    private val finalizable: Finalizable

    init {
        check(nativeTypesetter != 0L) { "Could not create the typesetter" }
        finalizable = Finalizable(nativeTypesetter)
    }

    /**
     * Constructs the typesetter object using a string that has no attributes. Lines cannot be made
     * from it until a typeface is set, so it is for the queries that need none, such as
     * [findBoundary] and [getBoundaryRange]; use a [MutableTypesetter] to set one.
     *
     * @param string The string of the text.
     */
    constructor(string: String) : this(nCreate(string), HashMap(), HashMap(), null)

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
     * Constructs the typesetter object using a spanned text. The attributes that Core knows are set
     * on the ranges that the spans cover, see the table in [Typesetter].
     *
     * @param spanned The spanned text to typeset.
     *
     * @throws IllegalArgumentException if `spanned` is empty.
     */
    constructor(spanned: Spanned) : this(spanned, null)

    constructor(spanned: Spanned, defaultSpans: List<Any>?) : this(
        makeMutable(spanned, defaultSpans ?: emptyList()), spanned
    )

    // The temporary typesetter shares the shaping of its paragraphs with the copy, and hands over
    // the typefaces and the replacements that it knows.
    private constructor(temporary: MutableTypesetter, spanned: Spanned) : this(
        nCreateCopy(temporary.nativeTypesetter), temporary.typefaces, temporary.holders, spanned
    )

    /**
     * Whether this typesetter can be edited.
     */
    open val isMutable: Boolean
        get() = false

    /**
     * The spanned text of this typesetter. For a typesetter that was made of a spanned text, it is
     * that text, with the spans that Core does not know. Otherwise it is made of the attributes,
     * see [toSpanned], and it is a new text each time for a mutable typesetter.
     */
    open val spanned: Spanned
        get() = source ?: toSpanned().also { source = it }

    /**
     * The typefaces and the replacements that the lines made now keep. A mutable typesetter can be
     * edited afterwards, so it gives a snapshot.
     */
    internal open val lineTypefaces: Map<Long, Typeface>
        get() = typefaces

    internal open val lineHolders: Map<Long, ReplacementHolder>
        get() = holders

    /**
     * The number of UTF-16 characters in this typesetter.
     */
    val length: Int
        get() = nGetLength(nativeTypesetter)

    /**
     * The string of this typesetter.
     */
    val string: String
        get() = getString(0, length)

    /**
     * Returns the string of a range of this typesetter.
     *
     * @param startIndex The index to the first character of the range.
     * @param endIndex The index after the last character of the range.
     * @return The string of the range.
     *
     * @throws IllegalArgumentException if the range is not within this typesetter.
     */
    fun getString(startIndex: Int, endIndex: Int): String {
        checkRange(startIndex, endIndex)

        return nGetString(nativeTypesetter, startIndex, endIndex) ?: ""
    }

    /**
     * Returns the spans that apply at an index, and how many characters they apply to.
     *
     * @param index The index of a character.
     * @return The spans that the character has, along with the length of the run, starting at the
     *         index, over which they stay the same. It is `null` if the index is not within the
     *         text.
     */
    fun getSpanRun(index: Int): SpanRun? {
        val run = readAttributes(index) ?: return null

        return SpanRun(makeSpans(run).map { it.second }, run.length)
    }

    /**
     * Returns an immutable copy of this typesetter, which shares the shaping of the paragraphs, so
     * it costs little. This typesetter can be changed afterwards without changing the copy.
     *
     * @throws IllegalStateException if this typesetter is in the middle of a group of edits.
     */
    fun copy(): Typesetter {
        val nativeCopy = nCreateCopy(nativeTypesetter)
        check(nativeCopy != 0L) { "Could not copy the typesetter" }

        return Typesetter(nativeCopy, HashMap(typefaces), HashMap(holders), source)
    }

    /**
     * Returns a mutable copy of this typesetter. It can be edited while this typesetter remains
     * unchanged.
     *
     * @throws IllegalStateException if this typesetter is in the middle of a group of edits.
     */
    fun mutableCopy(): MutableTypesetter {
        val nativeCopy = nCreateMutableCopy(nativeTypesetter)
        check(nativeCopy != 0L) { "Could not copy the typesetter" }

        return MutableTypesetter(nativeCopy, HashMap(typefaces), HashMap(holders))
    }

    /**
     * Makes a spanned text of this typesetter, with the spans of the attributes of its ranges, see the
     * table in [Typesetter]. The spans are made for each range, and they are shared between the ranges
     * that follow each other with the same value.
     */
    fun toSpanned(): Spanned {
        val spanned = SpannableString(string)
        val open = HashMap<List<Long>, Pair<Int, Any>>()
        var index = 0

        while (index < length) {
            val run = readAttributes(index) ?: break
            val spans = makeSpans(run)
            val keys = spans.map { it.first }.toSet()

            // The spans that do not go on end where the run begins.
            val ended = open.keys.filter { it !in keys }
            for (key in ended) {
                val (start, span) = open.remove(key)!!
                spanned.setSpan(span, start, index, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            for ((key, span) in spans) {
                if (key !in open) {
                    open[key] = Pair(index, span)
                }
            }

            index += run.length
        }

        for ((start, span) in open.values) {
            spanned.setSpan(span, start, index, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return spanned
    }

    override fun toString(): String {
        return string
    }

    internal fun checkRange(startIndex: Int, endIndex: Int) {
        require(startIndex >= 0 && endIndex >= startIndex && endIndex <= length) {
            "Range: [$startIndex, $endIndex), Text Length: $length"
        }
    }

    /** The attributes that apply over a run of a text, by their types. */
    internal class AttributeRun(val length: Int, private val bits: Map<Int, Long>) {
        fun has(type: Int) = bits.containsKey(type)
        fun bitsOf(type: Int) = bits.getValue(type)
        fun floatOf(type: Int) = Float.fromBits(bits.getValue(type).toInt())
        fun intOf(type: Int) = bits.getValue(type).toInt()

        fun floatOrNull(type: Int) = if (has(type)) floatOf(type) else null
    }

    internal fun readAttributes(index: Int): AttributeRun? {
        if (index < 0 || index >= length) {
            return null
        }

        val runLength = IntArray(1)
        val items = nGetAttributes(nativeTypesetter, index, runLength) ?: return null
        val bits = HashMap<Int, Long>()

        for (i in 0 until items.size step 2) {
            bits[items[i].toInt()] = items[i + 1]
        }

        return AttributeRun(runLength[0], bits)
    }

    /** Makes the spans of the attributes of a run, each with the key that tells its value. */
    private fun makeSpans(run: AttributeRun): List<Pair<List<Long>, Any>> {
        val spans = ArrayList<Pair<List<Long>, Any>>()

        fun add(type: Int, vararg extra: Long, make: () -> Any?) {
            if (run.has(type)) {
                make()?.let { spans.add(Pair(listOf(type.toLong(), run.bitsOf(type), *extra.toTypedArray()), it)) }
            }
        }

        add(AttributeType.TYPEFACE) { typefaces[run.bitsOf(AttributeType.TYPEFACE)]?.let { TypefaceSpan(it) } }
        add(AttributeType.TYPE_SIZE) { TypeSizeSpan(run.floatOf(AttributeType.TYPE_SIZE)) }
        add(AttributeType.SCALE_X) { ScaleXSpan(run.floatOf(AttributeType.SCALE_X)) }
        add(AttributeType.FOREGROUND_COLOR) { ForegroundColorSpan(run.intOf(AttributeType.FOREGROUND_COLOR)) }
        add(AttributeType.BACKGROUND_COLOR) { BackgroundColorSpan(run.intOf(AttributeType.BACKGROUND_COLOR)) }
        add(AttributeType.UNDERLINE) { if (run.intOf(AttributeType.UNDERLINE) != 0) UnderlineSpan() else null }
        add(AttributeType.STRIKETHROUGH) { if (run.intOf(AttributeType.STRIKETHROUGH) != 0) StrikethroughSpan() else null }
        add(AttributeType.DECORATION_COLOR) { DecorationColorSpan(run.intOf(AttributeType.DECORATION_COLOR)) }
        add(AttributeType.FONT_FEATURES) {
            val values = nGetFontFeatures(run.bitsOf(AttributeType.FONT_FEATURES))
            OpenTypeFeaturesSpan(
                (0 until values.size step 2).map { OpenTypeFeature.of(values[it], values[it + 1]) }.toSet()
            )
        }
        add(AttributeType.REPLACEMENT) { holders[run.bitsOf(AttributeType.REPLACEMENT)]?.span }

        if (run.has(AttributeType.TEXT_ALIGNMENT)) {
            val alignment = when (run.intOf(AttributeType.TEXT_ALIGNMENT)) {
                ALIGNMENT_CENTER -> Layout.Alignment.ALIGN_CENTER
                ALIGNMENT_LEADING -> Layout.Alignment.ALIGN_NORMAL
                ALIGNMENT_TRAILING -> Layout.Alignment.ALIGN_OPPOSITE
                else -> null
            }
            add(AttributeType.TEXT_ALIGNMENT) { alignment?.let { AlignmentSpan.Standard(it) } }
        }

        val first = run.floatOrNull(AttributeType.FIRST_LINE_HEAD_INDENT) ?: 0.0f
        val rest = run.floatOrNull(AttributeType.HEAD_INDENT) ?: 0.0f
        if (first != 0.0f || rest != 0.0f) {
            spans.add(Pair(
                listOf(MARGIN_KEY, first.toRawBits().toLong(), rest.toRawBits().toLong()),
                LeadingMarginSpan.Standard(first.toInt(), rest.toInt())
            ))
        }

        return spans
    }

    /**
     * Finds the boundary of a type that is next to an index, according to Unicode Standard Annex
     * #29 for graphemes and words, and the separators of the text for paragraphs. The start and
     * the end of the text are boundaries of every type. It needs no typeface, and it is not
     * available while a [MutableTypesetter] is in the middle of a group of edits.
     *
     * @param index The index to search from, which can be the length of the text.
     * @param type The type of the boundary.
     * @param direction The direction of the search: a forward search finds the first boundary
     *        after the index, and a backward search finds the last boundary before it.
     * @return The index of the boundary, or `null` if the index is not within `0..length`, there
     *         is nothing after it for a forward search or before it for a backward search, or the
     *         typesetter is in the middle of a group of edits.
     */
    fun findBoundary(index: Int, type: BoundaryType, direction: BoundaryDirection): Int? {
        if (index < 0 || index > length) {
            return null
        }

        val boundary = nFindBoundary(nativeTypesetter, index, type.ordinal, direction.ordinal)

        return if (boundary >= 0) boundary else null
    }

    /**
     * Gets the range of the segment of a type that contains an index, for example the word under
     * a caret.
     *
     * @param index The index that the segment contains, which can be the length of the text and
     *        has an empty segment.
     * @param type The type of the boundary.
     * @return The range of the segment, or `null` if the index is not within `0..length`, or the
     *         typesetter is in the middle of a group of edits.
     */
    fun getBoundaryRange(index: Int, type: BoundaryType): BoundaryRange? {
        if (index < 0 || index > length) {
            return null
        }

        val packed = nGetBoundaryRange(nativeTypesetter, index, type.ordinal)
        if (packed < 0) {
            return null
        }

        return BoundaryRange((packed ushr 32).toInt(), (packed and 0xFFFFFFFFL).toInt())
    }

    private fun checkSubRange(fromIndex: Int, toIndex: Int) {
        require(fromIndex >= 0) { "From Index: $fromIndex" }
        require(toIndex <= length) { "To Index: $toIndex, Text Length: $length" }
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
     *         length of the text, or `fromIndex` is greater than or equal to `toIndex`
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
     *         length of the text, or `fromIndex` is greater than or equal to `toIndex`
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
     *         length of the text, or `fromIndex` is greater than or equal to `toIndex`
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
     *  - `toIndex` is greater than the length of the text
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
     *  - `toIndex` is greater than the length of the text
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
     *  - `toIndex` is greater than the length of the text
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
            throw RuntimeException(
                "Could not create the line: the range may have no typeface, or the typesetter is in the middle of a group of edits"
            )
        }

        return ComposedLine(nativeLine, lineTypefaces, lineHolders)
    }

    /**
     * Wraps a frame of Core, which is made from this typesetter.
     */
    internal fun makeFrame(nativeFrame: Long): ComposedFrame {
        if (nativeFrame == 0L) {
            throw RuntimeException(
                "Could not create the frame: the range may have no typeface, or the typesetter is in the middle of a group of edits"
            )
        }

        return ComposedFrame(nativeFrame, spanned, lineTypefaces, lineHolders)
    }

    private external fun nGetLength(nativeTypesetter: Long): Int
    private external fun nGetString(nativeTypesetter: Long, startIndex: Int, endIndex: Int): String?
    private external fun nGetAttributes(nativeTypesetter: Long, index: Int, runLength: IntArray): LongArray?
    private external fun nFindBoundary(nativeTypesetter: Long, index: Int, type: Int, direction: Int): Int
    private external fun nGetBoundaryRange(nativeTypesetter: Long, index: Int, type: Int): Long

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

    internal companion object {
        const val ALIGNMENT_CENTER = 1
        const val ALIGNMENT_LEADING = 3
        const val ALIGNMENT_TRAILING = 4

        private const val MARGIN_KEY = 1000L

        init {
            JniBridge.loadLibrary()
        }

        private fun makeSpanned(text: String, typeface: Typeface, typeSize: Float): Spanned {
            require(text.isNotEmpty()) { "Text is empty" }

            val spanned = SpannableString(text)
            spanned.setSpan(TypefaceSpan(typeface), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            spanned.setSpan(TypeSizeSpan(typeSize), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

            return spanned
        }

        private fun makeMutable(spanned: Spanned, defaultSpans: List<Any>): MutableTypesetter {
            require(spanned.isNotEmpty()) { "Text is empty" }

            return MutableTypesetter(spanned, defaultSpans)
        }

        @JvmStatic external fun nCreate(string: String): Long
        @JvmStatic external fun nCreateCopy(nativeTypesetter: Long): Long
        @JvmStatic external fun nCreateMutableCopy(nativeTypesetter: Long): Long
        @JvmStatic external fun nGetFontFeatures(featuresHandle: Long): IntArray
        @JvmStatic external fun nDispose(nativeTypesetter: Long)
    }
}

/** The types of the attributes of Core, which are the values of its `TRAttribute*` constants. */
internal object AttributeType {
    const val TYPEFACE = 1
    const val TYPE_SIZE = 2
    const val SCALE_X = 3
    const val SCALE_Y = 4
    const val BASELINE_OFFSET = 5
    const val OBLIQUENESS = 6
    const val REPLACEMENT = 7
    const val TEXT_ALIGNMENT = 8
    const val FIRST_LINE_HEAD_INDENT = 9
    const val HEAD_INDENT = 10
    const val FIRST_INDENT_LINE_COUNT = 12
    const val FOREGROUND_COLOR = 19
    const val LANGUAGE = 21
    const val FONT_FEATURES = 22
    const val BACKGROUND_COLOR = 23
    const val UNDERLINE = 24
    const val STRIKETHROUGH = 25
    const val DECORATION_COLOR = 26
}
