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

import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.ReplacementSpan
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.util.Preconditions.checkArgument
import java.util.IdentityHashMap
import java.util.TreeMap

/**
 * Gives a text to Core, to make a typesetter of it. The spans of the text are resolved into the
 * attributes that Core knows, which are set on the ranges that the spans cover. Core merges the
 * ranges that end up with the same attributes, so that they are shaped together.
 */
internal class TypesetterInput(
    text: String,
    private val spanned: Spanned,
    defaultSpans: List<Any>
) {
    /** The handle of the typesetter of Core, which the caller has to dispose. */
    val nativeTypesetter: Long

    /** The typefaces of the text, by the handles that their runs have. */
    val typefaces = HashMap<Long, Typeface>()

    /** The holders of the replacements, by the starts of their spans. */
    val holders = TreeMap<Int, ReplacementHolder>()

    init {
        val textHandle = nCreateText(text)
        if (textHandle == 0L) {
            throw RuntimeException("Could not create the typesetter")
        }

        try {
            setRunAttributes(textHandle, RunStyle.initial(defaultSpans))
            setColorAttributes(textHandle)
            setParagraphAttributes(textHandle)
        } catch (throwable: Throwable) {
            nDisposeText(textHandle)
            throw throwable
        }

        nativeTypesetter = nCreateTypesetter(textHandle)
        if (nativeTypesetter == 0L) {
            throw RuntimeException("Could not create the typesetter")
        }
    }

    private fun setRunAttributes(textHandle: Long, initial: RunStyle) {
        // A replacement is made once for its span, so that Core takes the ranges of the span as a
        // single run if nothing else changes in them.
        val replacements = IdentityHashMap<ReplacementSpan, Long>()
        var start = 0

        try {
            while (start < spanned.length) {
                val end = spanned.nextSpanTransition(start, spanned.length, MetricAffectingSpan::class.java)
                val style = initial.with(spanned.getSpans(start, end, MetricAffectingSpan::class.java))

                val typeface = style.typeface
                checkArgument(typeface != null, "No typeface is specified for range [$start, $end)")

                typefaces[typeface!!.coreHandle] = typeface
                nSetTypeface(textHandle, start, end, typeface.coreHandle)
                nSetTypeSize(textHandle, start, end, style.typeSize)
                nSetScaleX(textHandle, start, end, style.scaleX)
                nSetBaselineOffset(textHandle, start, end, style.baselineShift)

                style.replacement?.let { span ->
                    val replacement = replacements.getOrPut(span) {
                        val holder = makeHolder(span, typeface, style.typeSize)
                        nCreateReplacement(holder, holder.isBlock)
                    }

                    nSetReplacement(textHandle, start, end, replacement)
                }

                start = end
            }
        } finally {
            // The text holds the replacements now.
            replacements.values.forEach { nReleaseReplacement(it) }
        }
    }

    private fun makeHolder(span: ReplacementSpan, typeface: Typeface, typeSize: Float): ReplacementHolder {
        val sizeByEm = typeSize / typeface.unitsPerEm
        val spanStart = spanned.getSpanStart(span)
        val holder = ReplacementHolder(
            span, spanned, spanStart, spanned.getSpanEnd(span),
            typeface.ascent * sizeByEm, typeface.descent * sizeByEm, typeface.leading * sizeByEm
        )

        holders[spanStart] = holder

        return holder
    }

    // The color of a span replaces those that came before it where they overlap.
    private fun setColorAttributes(textHandle: Long) {
        for (span in spanned.getSpans(0, spanned.length, ForegroundColorSpan::class.java)) {
            val spanStart = spanned.getSpanStart(span)
            val spanEnd = spanned.getSpanEnd(span)

            if (spanStart < spanEnd) {
                nSetForegroundColor(textHandle, spanStart, spanEnd, span.foregroundColor)
            }
        }
    }

    private fun setParagraphAttributes(textHandle: Long) {
        for (paragraph in ParagraphInput.of(spanned)) {
            val start = paragraph.start
            val end = paragraph.end

            if (paragraph.alignment >= 0) {
                nSetTextAlignment(textHandle, start, end, paragraph.alignment)
            }

            nSetFirstLineHeadIndent(textHandle, start, end, paragraph.firstMargin)
            nSetHeadIndent(textHandle, start, end, paragraph.restMargin)
            nSetFirstIndentLineCount(textHandle, start, end, paragraph.firstLineCount)
        }
    }

    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nCreateText(text: String): Long
        @JvmStatic external fun nDisposeText(textHandle: Long)
        @JvmStatic external fun nSetTypeface(textHandle: Long, start: Int, end: Int, typeface: Long)
        @JvmStatic external fun nSetTypeSize(textHandle: Long, start: Int, end: Int, value: Float)
        @JvmStatic external fun nSetScaleX(textHandle: Long, start: Int, end: Int, value: Float)
        @JvmStatic external fun nSetBaselineOffset(textHandle: Long, start: Int, end: Int, value: Float)
        @JvmStatic external fun nSetForegroundColor(textHandle: Long, start: Int, end: Int, value: Int)
        @JvmStatic external fun nCreateReplacement(holder: Any, isBlock: Boolean): Long
        @JvmStatic external fun nSetReplacement(textHandle: Long, start: Int, end: Int, replacement: Long)
        @JvmStatic external fun nReleaseReplacement(replacement: Long)
        @JvmStatic external fun nSetTextAlignment(textHandle: Long, start: Int, end: Int, value: Int)
        @JvmStatic external fun nSetFirstLineHeadIndent(textHandle: Long, start: Int, end: Int, value: Float)
        @JvmStatic external fun nSetHeadIndent(textHandle: Long, start: Int, end: Int, value: Float)
        @JvmStatic external fun nSetFirstIndentLineCount(textHandle: Long, start: Int, end: Int, value: Int)
        @JvmStatic external fun nCreateTypesetter(textHandle: Long): Long
    }
}
