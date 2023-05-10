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

    private fun appendText(
        builder: SpannableStringBuilder,
        text: CharSequence,
        spans: Array<Any>? = null
    ): BidiInfoActivity {
        val start = builder.length
        val end = start + text.length

        builder.append(text)

        for (s in spans.orEmpty()) {
            builder.setSpan(s, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return this
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

        appendText(builder, "Paragraph $index\n", spansFirstHeading())
        appendText(builder, "Paragraph Text:", spansInlineHeading())
        appendText(builder, " “$paragraphText”\n")
        appendText(builder, "Paragraph Range:", spansInlineHeading())
        appendText(builder, " Start=$paragraphStart Length=$paragraphLength\n")
        appendText(builder, "Base Level:", spansInlineHeading())
        appendText(builder, " ${paragraph.baseLevel}\n\n")

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

        appendText(builder, "\n")
    }

    private fun writeRunText(builder: SpannableStringBuilder, run: BidiRun, index: Int) {
        val runStart = run.charStart
        val runEnd = run.charEnd
        val runLength = runEnd - runStart
        val runText = (if (run.isRightToLeft) RLI else LRI).toString() + bidiText.substring(runStart, runEnd) + PDI

        appendText(builder, "Run $index\n", spansSecondHeading())
        appendText(builder, "Run Text:", spansInlineHeading())
        appendText(builder, " “$runText”\n")
        appendText(builder, "Run Range:", spansInlineHeading())
        appendText(builder, " Start=$runStart Length=$runLength\n")
        appendText(builder, "Embedding Level:", spansInlineHeading())
        appendText(builder, " ${run.embeddingLevel}\n\n")
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
            appendText(builder, "Visual Order\n", spansSecondHeading())

            for (i in 0 until runCount) {
                val runIndex = visualMap.valueAt(i)
                appendText(builder, "<Run $runIndex>", spansInlineHeading())
                appendText(builder, " ")
            }

            appendText(builder, "\n\n")
        }
    }

    private fun writeMirrorsText(builder: SpannableStringBuilder, line: BidiLine) {
        var wroteHeading = false

        for (bidiPair in line.mirroringPairs) {
            if (!wroteHeading) {
                wroteHeading = true
                appendText(builder, "Mirrors\n", spansSecondHeading())
            }

            appendText(builder, "*", spansInlineHeading())
            appendText(builder, " Index=" + bidiPair.charIndex)
            appendText(
                builder,
                " Character=‘" + String(Character.toChars(bidiPair.actualCodePoint)) + "’"
            )
            appendText(
                builder, " Mirror=‘${String(Character.toChars(bidiPair.pairingCodePoint))}’\n"
            )
        }

        if (wroteHeading) {
            appendText(builder, "\n")
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
