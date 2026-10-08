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
import android.text.style.AlignmentSpan
import android.text.style.LeadingMarginSpan
import android.text.style.LeadingMarginSpan.LeadingMarginSpan2
import android.text.style.ParagraphStyle
import com.mta.tehreer.internal.layout.Paragraphs

/**
 * The style of a paragraph, as the spans that cover it say. A span applies to every paragraph that
 * it overlaps, and the margins of the spans of a paragraph are added up. The top most alignment of
 * a paragraph wins.
 */
internal class ParagraphInput(
    val start: Int,
    val end: Int
) {
    /** The alignment in the values of Core, or -1 if no span gives one. */
    var alignment = -1
        private set

    /** The margin of the lines that come first in the paragraph. */
    var firstMargin = 0.0f
        private set

    /** The margin of the other lines. */
    var restMargin = 0.0f
        private set

    /** The number of lines that use the first margin. */
    var firstLineCount = 1
        private set

    private fun add(span: Any) {
        if (span is AlignmentSpan) {
            alignment = when (span.alignment) {
                Layout.Alignment.ALIGN_NORMAL -> ALIGNMENT_LEADING
                Layout.Alignment.ALIGN_CENTER -> ALIGNMENT_CENTER
                Layout.Alignment.ALIGN_OPPOSITE -> ALIGNMENT_TRAILING
                else -> alignment
            }
        }
        if (span is LeadingMarginSpan) {
            firstMargin += span.getLeadingMargin(true)
            restMargin += span.getLeadingMargin(false)

            if (span is LeadingMarginSpan2) {
                firstLineCount = maxOf(firstLineCount, span.leadingMarginLineCount)
            }
        }
    }

    companion object {
        private const val ALIGNMENT_CENTER = 1
        private const val ALIGNMENT_LEADING = 3
        private const val ALIGNMENT_TRAILING = 4

        /** Finds the paragraphs of a text that have an alignment or a margin, in their order. */
        fun of(spanned: Spanned): List<ParagraphInput> {
            val spans = spanned.getSpans(0, spanned.length, ParagraphStyle::class.java)
                .filter { it is AlignmentSpan || it is LeadingMarginSpan }
            val paragraphs = sortedMapOf<Int, ParagraphInput>()

            if (spans.isNotEmpty()) {
                val bounds = Paragraphs.boundsOf(spanned)

                for (span in spans) {
                    val spanStart = spanned.getSpanStart(span)
                    val spanEnd = spanned.getSpanEnd(span)
                    val first = Paragraphs.indexOf(bounds, spanStart)
                    val last = Paragraphs.indexOf(bounds, maxOf(spanStart, spanEnd - 1))

                    for (index in first..last) {
                        paragraphs.getOrPut(index) {
                            ParagraphInput(bounds[index * 2], bounds[index * 2 + 1])
                        }.add(span)
                    }
                }
            }

            return paragraphs.values.toList()
        }
    }
}
