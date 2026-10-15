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
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LocaleSpan
import android.text.style.MetricAffectingSpan
import android.text.style.ReplacementSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.layout.style.DecorationColorSpan
import com.mta.tehreer.layout.style.OpenTypeFeaturesSpan
import com.mta.tehreer.sfnt.ShapingEngine
import java.util.IdentityHashMap

/**
 * Sets the attributes of Core on a text, which has the string of a spanned text already. The spans
 * of the spanned text are resolved all together, as they apply one after the other on the ranges
 * where they overlap. Core merges the ranges that end up with the same attributes, so that they
 * are shaped together.
 */
internal class SpannedConverter(
    private val text: TMutableText,
    private val spanned: Spanned,
    private val defaultSpans: List<Any>
) {
    fun convert() {
        setRunAttributes(RunStyle.initial(defaultSpans))
        setColorAttributes()
        setDecorationAttributes()
        setShapingAttributes()
        setParagraphAttributes()
    }

    private fun setRunAttributes(initial: RunStyle) {
        // A replacement is made once for its span, so that Core takes the ranges of the span as a
        // single run if nothing else changes in them.
        val replacements = IdentityHashMap<ReplacementSpan, Long>()
        var start = 0

        try {
            while (start < spanned.length) {
                val end = spanned.nextSpanTransition(start, spanned.length, MetricAffectingSpan::class.java)
                val style = initial.with(spanned.getSpans(start, end, MetricAffectingSpan::class.java))

                val typeface = style.typeface
                require(typeface != null) { "No typeface is specified for range [$start, $end)" }

                text.putTypeface(start, end, typeface)
                text.putTypeSize(start, end, style.typeSize)
                text.putScaleX(start, end, style.scaleX)
                text.putBaselineOffset(start, end, style.baselineShift)

                style.replacement?.let { span ->
                    val replacement = replacements.getOrPut(span) {
                        text.createReplacement(makeHolder(span, typeface, style.typeSize))
                    }

                    text.putReplacement(start, end, replacement)
                }

                start = end
            }
        } finally {
            // The text holds the replacements now.
            replacements.values.forEach { text.releaseReplacement(it) }
        }
    }

    private fun makeHolder(span: ReplacementSpan, typeface: Typeface, typeSize: Float): ReplacementHolder {
        val sizeByEm = typeSize / typeface.unitsPerEm

        return ReplacementHolder(
            span, spanned, spanned.getSpanStart(span), spanned.getSpanEnd(span),
            typeface.ascent * sizeByEm, typeface.descent * sizeByEm, typeface.leading * sizeByEm
        )
    }

    // The color of a span replaces those that came before it where they overlap.
    private fun setColorAttributes() {
        for (span in spanned.getSpans(0, spanned.length, ForegroundColorSpan::class.java)) {
            forEachRange(span) { start, end -> text.putForegroundColor(start, end, span.foregroundColor) }
        }
    }

    private fun setDecorationAttributes() {
        for (span in spanned.getSpans(0, spanned.length, BackgroundColorSpan::class.java)) {
            forEachRange(span) { start, end -> text.putBackgroundColor(start, end, span.backgroundColor) }
        }
        for (span in spanned.getSpans(0, spanned.length, UnderlineSpan::class.java)) {
            forEachRange(span) { start, end -> text.putUnderline(start, end, true) }
        }
        for (span in spanned.getSpans(0, spanned.length, StrikethroughSpan::class.java)) {
            forEachRange(span) { start, end -> text.putStrikethrough(start, end, true) }
        }
        for (span in spanned.getSpans(0, spanned.length, DecorationColorSpan::class.java)) {
            forEachRange(span) { start, end -> text.putDecorationColor(start, end, span.color) }
        }
    }

    private fun setShapingAttributes() {
        for (span in spanned.getSpans(0, spanned.length, LocaleSpan::class.java)) {
            val locale = span.locale ?: continue
            val languageTag = ShapingEngine.getLanguageTag(locale.toLanguageTag())

            forEachRange(span) { start, end -> text.putLanguage(start, end, languageTag) }
        }
        for (span in spanned.getSpans(0, spanned.length, OpenTypeFeaturesSpan::class.java)) {
            forEachRange(span) { start, end -> text.putFontFeatures(start, end, span) }
        }
    }

    private inline fun forEachRange(span: Any, action: (start: Int, end: Int) -> Unit) {
        val start = spanned.getSpanStart(span)
        val end = spanned.getSpanEnd(span)

        if (start < end) {
            action(start, end)
        }
    }

    private fun setParagraphAttributes() {
        for (paragraph in ParagraphInput.of(spanned)) {
            val start = paragraph.start
            val end = paragraph.end

            if (paragraph.alignment >= 0) {
                text.putTextAlignment(start, end, paragraph.alignment)
            }

            text.putFirstLineHeadIndent(start, end, paragraph.firstMargin)
            text.putHeadIndent(start, end, paragraph.restMargin)
            text.putFirstIndentLineCount(start, end, paragraph.firstLineCount)
        }
    }
}
