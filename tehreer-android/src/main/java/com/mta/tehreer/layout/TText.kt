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
 * The spans that apply at an index of a [TText], and how many characters they apply to.
 *
 * @property spans The spans that apply at the index.
 * @property length The number of characters, starting at the index, over which the spans stay the
 *           same.
 */
class SpanRun(val spans: List<Any>, val length: Int)

/**
 * The `TText` class represents the text of Core: UTF-16 characters along with the attributes of
 * their ranges, such as typeface, type size, colors, decorations, language and font features. A
 * [Typesetter] lays out a text.
 *
 * A text does not change once it is created, so it can be shared between threads. Use
 * [TMutableText] to edit a text.
 *
 * ### Spans and attributes
 *
 * Core knows attributes, while Android knows spans, so the wrapper maps one to the other. A text is
 * made from a `Spanned` by setting the attributes that its spans stand for, and [toSpanned] and
 * [getSpanRun] make spans from attributes again:
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
open class TText internal constructor(
    internal val nativeText: Long,
    /** The typefaces of the text, by the handles that Core knows them with. */
    internal val typefaces: HashMap<Long, Typeface>,
    /** The holders of the replacement spans, by the handles of their replacements in Core. */
    internal val holders: HashMap<Long, ReplacementHolder>
) {
    private class Finalizable(private val nativeText: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeText)
        }
    }

    private val finalizable: Finalizable

    init {
        check(nativeText != 0L) { "Could not create the text" }
        finalizable = Finalizable(nativeText)
    }

    /**
     * Creates a text of the string, which has no attributes.
     *
     * @param string The string of the text.
     */
    constructor(string: String) : this(nCreate(string), HashMap(), HashMap())

    /**
     * Creates a text of the spanned text. The attributes that Core knows are set on the ranges
     * that the spans cover, see the table in [TText].
     *
     * @param spanned The spanned text.
     */
    constructor(spanned: Spanned) : this(TMutableText(spanned))

    internal constructor(spanned: Spanned, defaultSpans: List<Any>) : this(
        TMutableText(spanned, defaultSpans)
    )

    // The temporary text hands over the text of Core, which stays as it is from now on.
    private constructor(source: TMutableText) : this(
        nRetain(source.nativeText), source.typefaces, source.holders
    )

    /**
     * Whether this text can be edited.
     */
    open val isMutable: Boolean
        get() = false

    /**
     * The number of UTF-16 characters in this text.
     */
    val length: Int
        get() = nGetLength(nativeText)

    /**
     * The string of this text.
     */
    val string: String
        get() = getString(0, length)

    /**
     * Returns the string of a range of this text.
     *
     * @param startIndex The index to the first character of the range.
     * @param endIndex The index after the last character of the range.
     * @return The string of the range.
     *
     * @throws IllegalArgumentException if the range is not within this text.
     */
    fun getString(startIndex: Int, endIndex: Int): String {
        checkRange(startIndex, endIndex)

        return nGetString(nativeText, startIndex, endIndex) ?: ""
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
     * Returns an immutable copy of this text.
     */
    fun copy(): TText {
        return TText(nCreateCopy(nativeText), HashMap(typefaces), HashMap(holders))
    }

    /**
     * Returns a mutable copy of this text.
     */
    fun mutableCopy(): TMutableText {
        return TMutableText(nCreateMutableCopy(nativeText), HashMap(typefaces), HashMap(holders))
    }

    /**
     * Makes a spanned text of this text, with the spans of the attributes of its ranges, see the
     * table in [TText]. The spans are made for each range, and they are shared between the ranges
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
        val items = nGetAttributes(nativeText, index, runLength) ?: return null
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

    private external fun nGetLength(nativeText: Long): Int
    private external fun nGetString(nativeText: Long, startIndex: Int, endIndex: Int): String?
    private external fun nGetAttributes(nativeText: Long, index: Int, runLength: IntArray): LongArray?

    internal companion object {
        const val ALIGNMENT_CENTER = 1
        const val ALIGNMENT_LEADING = 3
        const val ALIGNMENT_TRAILING = 4

        private const val MARGIN_KEY = 1000L

        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nCreate(string: String): Long
        @JvmStatic external fun nCreateMutable(): Long
        @JvmStatic external fun nCreateCopy(nativeText: Long): Long
        @JvmStatic external fun nCreateMutableCopy(nativeText: Long): Long
        @JvmStatic external fun nRetain(nativeText: Long): Long
        @JvmStatic external fun nGetFontFeatures(featuresHandle: Long): IntArray
        @JvmStatic external fun nDispose(nativeText: Long)
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
