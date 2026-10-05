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

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import com.mta.tehreer.internal.layout.ReplacementRun
import com.mta.tehreer.internal.layout.isBlock
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.util.TypefaceStore
import com.mta.tehreer.layout.style.ViewSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class UnusedBlockSpan : ViewSpan() {
    override val placement: Placement get() = Placement.BLOCK
    override fun createView(context: Context): View =
        throw AssertionError("A truncated-line test should never need a view")
}

/**
 * Tests that a truncated [ComposedLine] never ends up with a block [ViewSpan] sharing space with
 * another glyph run - the invariant [BreakResolver]'s backward search exists to protect, since
 * [FrameResolver] gives a line with a block run all of its width, whatever else the line's
 * [GlyphRun] list holds. Goes through [Typesetter.createTruncatedLine], the same entry point
 * [TLabel] uses for [TruncationPlace.START]/[TruncationPlace.MIDDLE]/[TruncationPlace.END].
 */
class BreakResolverTruncatedLineTest {
    private fun typesetterOf(build: SpannableStringBuilder.() -> Unit): Typesetter {
        val spanned = SpannableStringBuilder().apply(build)
        val typeface = TypefaceStore.getNafeesWeb()
        val defaultSpans = listOf<Any>(TypefaceSpan(typeface), TypeSizeSpan(32.0f))

        return Typesetter(spanned, defaultSpans)
    }

    private fun SpannableStringBuilder.appendBlock(): Int {
        val start = length
        append("￼")
        setSpan(UnusedBlockSpan(), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return start
    }

    /**
     * No run on the line, other than the block's own, a truncation token's, and the (invisible)
     * whitespace/newline the block absorbed around it, holds real text - matching what the
     * existing forward-path tests already establish is fine (a block's line legitimately keeps
     * the separator it absorbed, see e.g. `TTextViewViewSpanTest.aBlockViewFillsTheLineThatIsAsTallAsIt`).
     */
    private fun assertBlockSharesTheLineWithNoOtherText(line: ComposedLine, text: CharSequence) {
        assertTrue("expected the line to hold a block", line.isBlock)

        for (glyphRun in line.runs) {
            val textRun = glyphRun.textRun
            val isBlockRun = textRun is ReplacementRun && textRun.isBlock
            val isWhitespaceOnly = glyphRun.charStart >= glyphRun.charEnd ||
                (glyphRun.charStart until glyphRun.charEnd).all { Character.isWhitespace(text[it]) }

            assertTrue(
                "a real (non-token, non-whitespace) run of other text shares the block's line",
                isBlockRun || glyphRun.isTruncated || isWhitespaceOnly
            )
        }
    }

    // START: the block is the nearest thing to the end kept - it, and the newline before it, must
    // survive truncation intact, alone.

    @Test
    fun startTruncationKeepsABlockNearestTheEndAloneOnItsLine() {
        val typesetter = typesetterOf {
            append("Hello there, this is a long line of leading text. ".repeat(3))
            append("\n")
            appendBlock()
        }
        val end = typesetter.spanned.length
        val fullWidth = typesetter.createSimpleLine(0, end).width

        val line = typesetter.createTruncatedLine(0, end, fullWidth / 4, BreakMode.LINE, TruncationPlace.START)
        assertBlockSharesTheLineWithNoOtherText(line, typesetter.spanned)
    }

    @Test
    fun startTruncationWithCharacterModeAlsoKeepsTheBlockAlone() {
        val typesetter = typesetterOf {
            append("Hello there, this is a long line of leading text. ".repeat(3))
            append("\n")
            appendBlock()
        }
        val end = typesetter.spanned.length
        val fullWidth = typesetter.createSimpleLine(0, end).width

        val line = typesetter.createTruncatedLine(0, end, fullWidth / 4, BreakMode.CHARACTER, TruncationPlace.START)
        assertBlockSharesTheLineWithNoOtherText(line, typesetter.spanned)
    }

    // MIDDLE: a block close to the middle must end up wholly on one side of the truncation token,
    // never split, never shared with the other side's text.

    /**
     * A block that is the very first thing in the text is kept by [BreakMode]-agnostic, width-
     * agnostic construction (see `BreakResolverViewSpanTest`): the forward half of a MIDDLE
     * truncation always resolves it from its own position, never from the extent budget. Whether
     * the *other* (backward) half also keeps some of the trailing text on the very same composed
     * line is a matter of how much width is left for it (that combination is `LineResolver`'s to
     * arbitrate, not something this change alters) - so what this asserts is the part the folded
     * scan actually owns: the block itself is never fragmented, and its glyph run is exactly the
     * block's own one-character range, whichever [mode] is used.
     */
    private fun assertMiddleTruncationKeepsALeadingBlockWhole(mode: BreakMode) {
        val typesetter = typesetterOf {
            appendBlock()
            append("\n")
            append("A short middle line.\n")
            append("Some more trailing text that will not fit. ".repeat(4))
        }
        val end = typesetter.spanned.length
        val fullWidth = typesetter.createSimpleLine(0, end).width

        val line = typesetter.createTruncatedLine(0, end, fullWidth / 3, mode, TruncationPlace.MIDDLE)

        assertTrue("expected truncation to actually happen", line.runs.any { it.isTruncated })
        assertTrue("expected the line to hold a block", line.isBlock)

        val blockRuns = line.runs.filter {
            val textRun = it.textRun
            textRun is ReplacementRun && textRun.isBlock
        }
        assertEquals("the block must appear exactly once, never split", 1, blockRuns.size)
        assertEquals(0, blockRuns[0].charStart)
        assertEquals(1, blockRuns[0].charEnd)
    }

    @Test
    fun middleTruncationKeepsALeadingBlockWholeOnItsLine() {
        assertMiddleTruncationKeepsALeadingBlockWhole(BreakMode.LINE)
    }

    @Test
    fun middleTruncationWithCharacterModeKeepsALeadingBlockWholeOnItsLine() {
        assertMiddleTruncationKeepsALeadingBlockWhole(BreakMode.CHARACTER)
    }

    // END (regression - the forward path already had coverage, but not through this exact entry
    // point together with a truncation token): a block reached while truncating from the end is
    // still kept alone.

    @Test
    fun endTruncationKeepsABlockAloneOnItsLine() {
        var blockStart = 0
        val typesetter = typesetterOf {
            append("Some leading text before the block. ")
            blockStart = appendBlock()
            append("\n")
            append("Some trailing text that will not fit.".repeat(3))
        }
        val end = typesetter.spanned.length
        val fullWidth = typesetter.createSimpleLine(0, end).width

        val line = typesetter.createTruncatedLine(0, end, fullWidth / 4, BreakMode.LINE, TruncationPlace.END)

        // The forward search stops right before the block (it is not the first thing), so it
        // never appears on this (first, and only, since END truncation only makes one line) line.
        assertTrue(line.charEnd <= blockStart)
        assertEquals(false, line.isBlock)
    }
}
