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
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2
import android.text.style.LocaleSpan
import android.text.style.RelativeSizeSpan
import android.text.style.ReplacementSpan
import android.text.style.ScaleXSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TextAppearanceSpan
import android.text.style.UnderlineSpan
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.util.StringUtils
import com.mta.tehreer.layout.style.DecorationColorSpan
import com.mta.tehreer.layout.style.OpenTypeFeaturesSpan
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.sfnt.ShapingEngine

/**
 * The `TMutableText` class is a [TText] that can be edited: its characters can be appended,
 * inserted, deleted and replaced, and the attributes of its ranges set and removed through spans.
 * It is not thread safe, so it must not be edited while another thread uses it.
 *
 * Edits can be grouped between [beginEditing] and [endEditing], which lets Core analyze the text
 * once for all of them. A text must not be in the middle of a group when a [Typesetter] is made of
 * it.
 *
 * The spans that [setSpan] and [removeSpan] understand, and the attributes that they stand for, are
 * listed in [TText]. Where the replacement of a [ReplacementSpan] is concerned, the span sees the
 * string and the range that the text had when it was set.
 */
class TMutableText : TText {
    /**
     * Creates an empty text.
     */
    constructor() : super(nCreateMutable(), HashMap(), HashMap())

    /**
     * Creates a text of the string, which has no attributes.
     *
     * @param string The string of the text.
     */
    constructor(string: String) : this() {
        append(string)
    }

    /**
     * Creates a text of the spanned text. The attributes that Core knows are set on the ranges
     * that the spans cover, see the table in [TText].
     *
     * @param spanned The spanned text.
     */
    constructor(spanned: Spanned) : this(spanned, emptyList())

    internal constructor(spanned: Spanned, defaultSpans: List<Any>) : this() {
        beginEditing()
        try {
            append(StringUtils.copyString(spanned))
            SpannedConverter(this, spanned, defaultSpans).convert()
        } finally {
            endEditing()
        }
    }

    internal constructor(
        nativeText: Long, typefaces: HashMap<Long, Typeface>, holders: HashMap<Long, ReplacementHolder>
    ) : super(nativeText, typefaces, holders)

    override val isMutable: Boolean
        get() = true

    /**
     * Begins a batch of edits, which Core handles together when [endEditing] is called.
     */
    fun beginEditing() {
        nBeginEditing(nativeText)
    }

    /**
     * Ends a batch of edits.
     */
    fun endEditing() {
        nEndEditing(nativeText)
    }

    /**
     * Appends the string to the end of this text.
     *
     * @param string The string to append.
     */
    fun append(string: String) {
        check(nAppend(nativeText, string)) { "Could not append the string" }
    }

    /**
     * Inserts the string at an index of this text.
     *
     * @param index The index at which to insert, which can be the length of this text.
     * @param string The string to insert.
     *
     * @throws IllegalArgumentException if `index` is negative or greater than the length.
     */
    fun insert(index: Int, string: String) {
        require(index in 0..length) { "Index: $index, Text Length: $length" }

        check(nInsert(nativeText, index, string)) { "Could not insert the string" }
    }

    /**
     * Deletes a range of this text.
     *
     * @param startIndex The index to the first character of the range.
     * @param endIndex The index after the last character of the range.
     *
     * @throws IllegalArgumentException if the range is not within this text.
     */
    fun delete(startIndex: Int, endIndex: Int) {
        checkRange(startIndex, endIndex)

        check(nDelete(nativeText, startIndex, endIndex)) { "Could not delete the range" }
    }

    /**
     * Replaces a range of this text with the string.
     *
     * @param startIndex The index to the first character of the range.
     * @param endIndex The index after the last character of the range.
     * @param string The string that takes the place of the range.
     *
     * @throws IllegalArgumentException if the range is not within this text.
     */
    fun replace(startIndex: Int, endIndex: Int, string: String) {
        checkRange(startIndex, endIndex)

        check(nReplace(nativeText, startIndex, endIndex, string)) { "Could not replace the range" }
    }

    /**
     * Replaces the whole content of this text with the string. The attributes are left to Core,
     * which handles it as replacing the range of the whole text.
     *
     * @param string The new string of the text.
     */
    fun setString(string: String) {
        check(nSetString(nativeText, string)) { "Could not set the string" }
    }

    /**
     * Sets the attributes that a span stands for on a range of this text. They take the place of
     * the ones that the range had, whichever spans they came from. See [TText] for the spans that
     * are known, and [removeSpan] for taking the attributes back.
     *
     * A span that depends on the text around it, such as `StyleSpan`, `RelativeSizeSpan`,
     * `TextAppearanceSpan`, the `TypefaceSpan` of Android, `SuperscriptSpan` and `SubscriptSpan`,
     * is resolved against the attributes that each part of the range has now. A paragraph span
     * (`AlignmentSpan`, `LeadingMarginSpan`) applies to every paragraph that the range touches.
     *
     * @param span The span whose attributes are set.
     * @param startIndex The index to the first character of the range.
     * @param endIndex The index after the last character of the range.
     *
     * @throws IllegalArgumentException if the range is not within this text or the span is not
     *         known.
     */
    fun setSpan(span: Any, startIndex: Int, endIndex: Int) {
        checkRange(startIndex, endIndex)
        require(attributeTypesOf(span).isNotEmpty()) { "Unknown span: $span" }

        if (startIndex == endIndex) {
            return
        }

        when (span) {
            is TypefaceSpan -> putTypeface(startIndex, endIndex, span.typeface)
            is TypeSizeSpan -> putTypeSize(startIndex, endIndex, span.size)
            is AbsoluteSizeSpan -> putTypeSize(startIndex, endIndex, span.size.toFloat())
            is ScaleXSpan -> putScaleX(startIndex, endIndex, span.scaleX)
            is ForegroundColorSpan -> putForegroundColor(startIndex, endIndex, span.foregroundColor)
            is BackgroundColorSpan -> putBackgroundColor(startIndex, endIndex, span.backgroundColor)
            is UnderlineSpan -> putUnderline(startIndex, endIndex, true)
            is StrikethroughSpan -> putStrikethrough(startIndex, endIndex, true)
            is DecorationColorSpan -> putDecorationColor(startIndex, endIndex, span.color)
            is LocaleSpan -> span.locale?.let {
                putLanguage(startIndex, endIndex, ShapingEngine.getLanguageTag(it.toLanguageTag()))
            }
            is OpenTypeFeaturesSpan -> putFontFeatures(startIndex, endIndex, span)
            is ReplacementSpan -> setReplacementSpan(span, startIndex, endIndex)
            is AlignmentSpan -> setAlignment(span, startIndex, endIndex)
            is LeadingMarginSpan -> setLeadingMargin(span, startIndex, endIndex)
            else -> setContextualSpan(span, startIndex, endIndex)
        }
    }

    /**
     * Removes the attributes that a span stands for from a range of this text, whichever spans
     * they came from. Only the kind of the span matters, so another span of the same class can be
     * given.
     *
     * @param span The span whose attributes are removed.
     * @param startIndex The index to the first character of the range.
     * @param endIndex The index after the last character of the range.
     *
     * @throws IllegalArgumentException if the range is not within this text or the span is not
     *         known.
     */
    fun removeSpan(span: Any, startIndex: Int, endIndex: Int) {
        checkRange(startIndex, endIndex)

        val types = attributeTypesOf(span)
        require(types.isNotEmpty()) { "Unknown span: $span" }

        if (startIndex < endIndex) {
            for (type in types) {
                nRemoveAttribute(nativeText, startIndex, endIndex, type)
            }
        }
    }

    private fun attributeTypesOf(span: Any): IntArray = when (span) {
        is TypefaceSpan, is android.text.style.TypefaceSpan, is StyleSpan ->
            intArrayOf(AttributeType.TYPEFACE)
        is TextAppearanceSpan -> intArrayOf(AttributeType.TYPEFACE, AttributeType.TYPE_SIZE)
        is TypeSizeSpan, is AbsoluteSizeSpan, is RelativeSizeSpan -> intArrayOf(AttributeType.TYPE_SIZE)
        is ScaleXSpan -> intArrayOf(AttributeType.SCALE_X)
        is SuperscriptSpan, is SubscriptSpan -> intArrayOf(AttributeType.BASELINE_OFFSET)
        is ForegroundColorSpan -> intArrayOf(AttributeType.FOREGROUND_COLOR)
        is BackgroundColorSpan -> intArrayOf(AttributeType.BACKGROUND_COLOR)
        is UnderlineSpan -> intArrayOf(AttributeType.UNDERLINE)
        is StrikethroughSpan -> intArrayOf(AttributeType.STRIKETHROUGH)
        is DecorationColorSpan -> intArrayOf(AttributeType.DECORATION_COLOR)
        is LocaleSpan -> intArrayOf(AttributeType.LANGUAGE)
        is OpenTypeFeaturesSpan -> intArrayOf(AttributeType.FONT_FEATURES)
        is ReplacementSpan -> intArrayOf(AttributeType.REPLACEMENT)
        is AlignmentSpan -> intArrayOf(AttributeType.TEXT_ALIGNMENT)
        is LeadingMarginSpan -> intArrayOf(
            AttributeType.FIRST_LINE_HEAD_INDENT, AttributeType.HEAD_INDENT,
            AttributeType.FIRST_INDENT_LINE_COUNT
        )
        else -> intArrayOf()
    }

    private fun setReplacementSpan(span: ReplacementSpan, startIndex: Int, endIndex: Int) {
        val run = readAttributes(startIndex)
        val typeface = run?.takeIf { it.has(AttributeType.TYPEFACE) }
            ?.let { typefaces[it.bitsOf(AttributeType.TYPEFACE)] }
        val typeSize = run?.floatOrNull(AttributeType.TYPE_SIZE) ?: 0.0f
        val sizeByEm = if (typeface != null) typeSize / typeface.unitsPerEm else 0.0f

        val holder = ReplacementHolder(
            span, string, startIndex, endIndex,
            (typeface?.ascent ?: 0) * sizeByEm, (typeface?.descent ?: 0) * sizeByEm,
            (typeface?.leading ?: 0) * sizeByEm
        )

        val replacement = createReplacement(holder)
        putReplacement(startIndex, endIndex, replacement)

        // The text holds the replacement now.
        releaseReplacement(replacement)
    }

    private fun setAlignment(span: AlignmentSpan, startIndex: Int, endIndex: Int) {
        val alignment = when (span.alignment) {
            Layout.Alignment.ALIGN_NORMAL -> ALIGNMENT_LEADING
            Layout.Alignment.ALIGN_CENTER -> ALIGNMENT_CENTER
            Layout.Alignment.ALIGN_OPPOSITE -> ALIGNMENT_TRAILING
            else -> -1
        }

        if (alignment >= 0) {
            nSetTextAlignment(nativeText, startIndex, endIndex, alignment)
        }
    }

    private fun setLeadingMargin(span: LeadingMarginSpan, startIndex: Int, endIndex: Int) {
        val lineCount = if (span is LeadingMarginSpan2) maxOf(1, span.leadingMarginLineCount) else 1

        nSetFirstLineHeadIndent(nativeText, startIndex, endIndex, span.getLeadingMargin(true).toFloat())
        nSetHeadIndent(nativeText, startIndex, endIndex, span.getLeadingMargin(false).toFloat())
        nSetFirstIndentLineCount(nativeText, startIndex, endIndex, lineCount)
    }

    private fun setContextualSpan(span: Any, startIndex: Int, endIndex: Int) {
        val isBaselineShift = span is SuperscriptSpan || span is SubscriptSpan
        var index = startIndex

        while (index < endIndex) {
            val run = readAttributes(index) ?: break
            val segmentEnd = minOf(endIndex, index + run.length)

            val typeface = run.takeIf { it.has(AttributeType.TYPEFACE) }
                ?.let { typefaces[it.bitsOf(AttributeType.TYPEFACE)] }
            val style = RunStyle.of(
                typeface, run.floatOrNull(AttributeType.TYPE_SIZE) ?: DEFAULT_TYPE_SIZE,
                run.floatOrNull(AttributeType.SCALE_X) ?: 1.0f
            ).with(arrayOf(span))

            style.typeface?.let { putTypeface(index, segmentEnd, it) }
            putTypeSize(index, segmentEnd, style.typeSize)
            putScaleX(index, segmentEnd, style.scaleX)
            if (isBaselineShift) {
                putBaselineOffset(index, segmentEnd, style.baselineShift)
            }

            index = segmentEnd
        }
    }

    // The functions below set one attribute on a range that is known to be valid.

    internal fun putTypeface(start: Int, end: Int, typeface: Typeface) {
        typefaces[typeface.nativeTypeface] = typeface
        nSetTypeface(nativeText, start, end, typeface.nativeTypeface)
    }

    internal fun putTypeSize(start: Int, end: Int, value: Float) =
        nSetTypeSize(nativeText, start, end, value)

    internal fun putScaleX(start: Int, end: Int, value: Float) =
        nSetScaleX(nativeText, start, end, value)

    internal fun putBaselineOffset(start: Int, end: Int, value: Float) =
        nSetBaselineOffset(nativeText, start, end, value)

    internal fun putForegroundColor(start: Int, end: Int, value: Int) =
        nSetForegroundColor(nativeText, start, end, value)

    internal fun putBackgroundColor(start: Int, end: Int, value: Int) =
        nSetBackgroundColor(nativeText, start, end, value)

    internal fun putUnderline(start: Int, end: Int, value: Boolean) =
        nSetUnderline(nativeText, start, end, value)

    internal fun putStrikethrough(start: Int, end: Int, value: Boolean) =
        nSetStrikethrough(nativeText, start, end, value)

    internal fun putDecorationColor(start: Int, end: Int, value: Int) =
        nSetDecorationColor(nativeText, start, end, value)

    internal fun putLanguage(start: Int, end: Int, languageTag: Int) =
        nSetLanguage(nativeText, start, end, languageTag)

    internal fun putFontFeatures(start: Int, end: Int, span: OpenTypeFeaturesSpan) {
        val tags = span.features.map { it.tag() }.toIntArray()
        val values = span.features.map { it.value() }.toIntArray()

        nSetFontFeatures(nativeText, start, end, tags, values)
    }

    /** Makes the replacement of Core for a holder, which the caller has to release in the end. */
    internal fun createReplacement(holder: ReplacementHolder): Long {
        val replacement = nCreateReplacement(holder, holder.isBlock)
        check(replacement != 0L) { "Could not create the replacement" }

        holders[replacement] = holder

        return replacement
    }

    /** Sets a replacement that is made already, as many ranges as needed can share it. */
    internal fun putReplacement(start: Int, end: Int, replacement: Long) =
        nSetReplacement(nativeText, start, end, replacement)

    internal fun releaseReplacement(replacement: Long) = nReleaseReplacement(replacement)

    internal fun putTextAlignment(start: Int, end: Int, value: Int) =
        nSetTextAlignment(nativeText, start, end, value)

    internal fun putFirstLineHeadIndent(start: Int, end: Int, value: Float) =
        nSetFirstLineHeadIndent(nativeText, start, end, value)

    internal fun putHeadIndent(start: Int, end: Int, value: Float) =
        nSetHeadIndent(nativeText, start, end, value)

    internal fun putFirstIndentLineCount(start: Int, end: Int, value: Int) =
        nSetFirstIndentLineCount(nativeText, start, end, value)

    private companion object {
        const val DEFAULT_TYPE_SIZE = 16.0f

        @JvmStatic external fun nBeginEditing(nativeText: Long)
        @JvmStatic external fun nEndEditing(nativeText: Long)
        @JvmStatic external fun nAppend(nativeText: Long, string: String): Boolean
        @JvmStatic external fun nInsert(nativeText: Long, index: Int, string: String): Boolean
        @JvmStatic external fun nDelete(nativeText: Long, start: Int, end: Int): Boolean
        @JvmStatic external fun nReplace(nativeText: Long, start: Int, end: Int, string: String): Boolean
        @JvmStatic external fun nSetString(nativeText: Long, string: String): Boolean
        @JvmStatic external fun nRemoveAttribute(nativeText: Long, start: Int, end: Int, type: Int): Boolean
        @JvmStatic external fun nSetTypeface(nativeText: Long, start: Int, end: Int, typeface: Long)
        @JvmStatic external fun nSetTypeSize(nativeText: Long, start: Int, end: Int, value: Float)
        @JvmStatic external fun nSetScaleX(nativeText: Long, start: Int, end: Int, value: Float)
        @JvmStatic external fun nSetBaselineOffset(nativeText: Long, start: Int, end: Int, value: Float)
        @JvmStatic external fun nSetForegroundColor(nativeText: Long, start: Int, end: Int, value: Int)
        @JvmStatic external fun nSetBackgroundColor(nativeText: Long, start: Int, end: Int, value: Int)
        @JvmStatic external fun nSetUnderline(nativeText: Long, start: Int, end: Int, value: Boolean)
        @JvmStatic external fun nSetStrikethrough(nativeText: Long, start: Int, end: Int, value: Boolean)
        @JvmStatic external fun nSetDecorationColor(nativeText: Long, start: Int, end: Int, value: Int)
        @JvmStatic external fun nSetLanguage(nativeText: Long, start: Int, end: Int, value: Int)
        @JvmStatic external fun nSetFontFeatures(
            nativeText: Long, start: Int, end: Int, tags: IntArray, values: IntArray
        )
        @JvmStatic external fun nCreateReplacement(holder: Any, isBlock: Boolean): Long
        @JvmStatic external fun nSetReplacement(nativeText: Long, start: Int, end: Int, replacement: Long)
        @JvmStatic external fun nReleaseReplacement(replacement: Long)
        @JvmStatic external fun nSetTextAlignment(nativeText: Long, start: Int, end: Int, value: Int)
        @JvmStatic external fun nSetFirstLineHeadIndent(nativeText: Long, start: Int, end: Int, value: Float)
        @JvmStatic external fun nSetHeadIndent(nativeText: Long, start: Int, end: Int, value: Float)
        @JvmStatic external fun nSetFirstIndentLineCount(nativeText: Long, start: Int, end: Int, value: Int)
    }
}
