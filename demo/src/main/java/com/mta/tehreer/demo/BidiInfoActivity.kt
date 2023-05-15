/*
 * Copyright (C) 2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.demo

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.ScrollingMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.SparseIntArray
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mta.tehreer.unicode.*

private fun SpannableStringBuilder.append(
    text: CharSequence,
    spans: Array<Any>? = null
) : SpannableStringBuilder {
    val start = length
    val end = start + text.length

    append(text)

    for (s in spans.orEmpty()) {
        setSpan(s, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    return this
}

class BidiInfoActivity : AppCompatActivity() {
    private lateinit var bidiText: String
    private var density = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bidi_info)

        bidiText = intent.getCharSequenceExtra(BIDI_TEXT).toString()
        density = resources.displayMetrics.scaledDensity

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val builder = SpannableStringBuilder()
        writeBidiText(builder)

        val bidiTextView = findViewById<TextView>(R.id.text_view_bidi)
        bidiTextView.movementMethod = ScrollingMovementMethod.getInstance()
        bidiTextView.text = builder
    }

    private fun spansFirstHeading(): Array<Any> {
        return arrayOf(
            AbsoluteSizeSpan((20.0f * density + 0.5f).toInt()),
            StyleSpan(Typeface.BOLD)
        )
    }

    private fun spansSecondHeading(): Array<Any> {
        return arrayOf(
            AbsoluteSizeSpan((16.0f * density + 0.5f).toInt()),
            StyleSpan(Typeface.BOLD)
        )
    }

    private fun spansInlineHeading(): Array<Any> {
        return arrayOf(
            StyleSpan(Typeface.ITALIC),
            UnderlineSpan()
        )
    }

    private fun writeBidiText(builder: SpannableStringBuilder) {
        builder.setSpan(
            AbsoluteSizeSpan((16.0f * density + 0.5f).toInt()),
            0,
            0,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        builder.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            0,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )

        var algorithm: BidiAlgorithm? = null

        try {
            algorithm = BidiAlgorithm(bidiText)
            writeAlgorithmText(builder, algorithm)
        } finally {
            algorithm?.dispose()
        }
    }

    private fun writeAlgorithmText(builder: SpannableStringBuilder, algorithm: BidiAlgorithm) {
        var paragraphIndex = 1
        var paragraphStart = 0
        val suggestedEnd = bidiText.length

        while (paragraphStart != suggestedEnd) {
            var paragraph: BidiParagraph? = null

            try {
                paragraph = algorithm.createParagraph(
                    paragraphStart,
                    suggestedEnd,
                    BaseDirection.DEFAULT_LEFT_TO_RIGHT
                )
                writeParagraphText(builder, paragraph, paragraphIndex)

                paragraphIndex++
                paragraphStart = paragraph.charEnd
            } finally {
                paragraph?.dispose()
            }
        }
    }

    private fun writeParagraphText(
        builder: SpannableStringBuilder,
        paragraph: BidiParagraph,
        index: Int
    ) {
        val paragraphStart = paragraph.charStart
        val paragraphEnd = paragraph.charEnd
        val paragraphLength = paragraphEnd - paragraphStart
        val paragraphText = (if (paragraph.baseLevel.toInt() and 1 == 1) RLI else LRI).toString() + bidiText.substring(paragraphStart, paragraphEnd) + PDI

        builder.append("Paragraph $index\n", spansFirstHeading())
        builder.append("Paragraph Text:", spansInlineHeading())
        builder.append(" “$paragraphText”\n")
        builder.append("Paragraph Range:", spansInlineHeading())
        builder.append(" Start=$paragraphStart Length=$paragraphLength\n")
        builder.append("Base Level:", spansInlineHeading())
        builder.append(" ${paragraph.baseLevel}\n\n")

        var counter = 1
        for (bidiRun in paragraph.logicalRuns) {
            writeRunText(builder, bidiRun, counter)
            counter++
        }

        var line: BidiLine? = null

        try {
            line = paragraph.createLine(paragraphStart, paragraphEnd)
            writeLineText(builder, line)
            writeMirrorsText(builder, line)
        } finally {
            line?.dispose()
        }

        builder.append("\n")
    }

    private fun writeRunText(builder: SpannableStringBuilder, run: BidiRun, index: Int) {
        val runStart = run.charStart
        val runEnd = run.charEnd
        val runLength = runEnd - runStart
        val runText = (if (run.isRightToLeft) RLI else LRI).toString() + bidiText.substring(runStart, runEnd) + PDI

        builder.append("Run $index\n", spansSecondHeading())
        builder.append("Run Text:", spansInlineHeading())
        builder.append(" “$runText”\n")
        builder.append("Run Range:", spansInlineHeading())
        builder.append(" Start=$runStart Length=$runLength\n")
        builder.append("Embedding Level:", spansInlineHeading())
        builder.append(" ${run.embeddingLevel}\n\n")
    }

    private fun writeLineText(builder: SpannableStringBuilder, line: BidiLine) {
        val visualMap = SparseIntArray()

        var counter = 1
        for (bidiRun in line.visualRuns) {
            visualMap.put(bidiRun.charStart, counter)
            counter++
        }

        val runCount = visualMap.size()
        if (runCount > 0) {
            builder.append("Visual Order\n", spansSecondHeading())

            for (i in 0 until runCount) {
                val runIndex = visualMap.valueAt(i)
                builder.append("<Run $runIndex>", spansInlineHeading())
                builder.append(" ")
            }

            builder.append("\n\n")
        }
    }

    private fun writeMirrorsText(builder: SpannableStringBuilder, line: BidiLine) {
        var wroteHeading = false

        for (bidiPair in line.mirroringPairs) {
            if (!wroteHeading) {
                wroteHeading = true
                builder.append("Mirrors\n", spansSecondHeading())
            }

            builder.append("*", spansInlineHeading())
            builder.append(" Index=" + bidiPair.charIndex)
            builder.append(
                " Character=‘" + String(Character.toChars(bidiPair.actualCodePoint)) + "’"
            )
            builder.append(
                builder, " Mirror=‘${String(Character.toChars(bidiPair.pairingCodePoint))}’\n"
            )
        }

        if (wroteHeading) {
            builder.append("\n")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val BIDI_TEXT = "bidi_text"

        private const val LRI = '\u2066'
        private const val RLI = '\u2067'
        private const val PDI = '\u2069'
    }
}
