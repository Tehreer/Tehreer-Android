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
import android.graphics.Canvas
import android.graphics.Rect
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.util.TypefaceStore
import com.mta.tehreer.layout.style.ViewSpan
import com.mta.tehreer.widget.arabicText
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A minimal block span, just for exercising the break search - no view is ever asked for, since
 * these tests never lay out or attach anything. Internal (not `private`) so [RunCollectionBlockTest],
 * in the same package, can reuse it too.
 */
internal class NeverShownBlockSpan : ViewSpan() {
    override val placement: Placement get() = Placement.BLOCK
    override fun createView(context: Context): View =
        throw AssertionError("A break-search test should never need a view")
}

/**
 * Tests of [BreakResolver]'s handling of a block [ViewSpan] (one with a line of its own),
 * through [Typesetter.suggestForwardBreak] and [Typesetter.suggestBackwardBreak] directly - the
 * same entry points [FrameResolver] and [LineResolver]'s truncated-line builders use.
 *
 * A huge [BIG_EXTENT] is used throughout: a block ends its line regardless of how much room is
 * left, so the extent budget is deliberately made a non-issue, isolating the block behaviour from
 * ordinary width-based breaking (which the rest of the widget test suite already covers).
 */
class BreakResolverViewSpanTest {
    private val BIG_EXTENT = 1.0e6f

    private fun typesetterOf(build: SpannableStringBuilder.() -> Unit): Typesetter {
        val spanned = SpannableStringBuilder().apply(build)
        val typeface = TypefaceStore.getNafeesWeb()
        val defaultSpans = listOf<Any>(TypefaceSpan(typeface), TypeSizeSpan(32.0f))

        return Typesetter(spanned, defaultSpans)
    }

    private fun SpannableStringBuilder.appendBlock(): Int {
        val start = length
        append("￼")
        setSpan(NeverShownBlockSpan(), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return start
    }

    // Forward: a block that is not the first thing.

    @Test
    fun aBlockThatIsNotFirstEndsTheLineBeforeIt() {
        var blockStart = 0
        val typesetter = typesetterOf {
            append("AAAA")
            blockStart = appendBlock()
            append("BBBB")
        }

        for (mode in BreakMode.values()) {
            val breakIndex = typesetter.suggestForwardBreak(0, typesetter.spanned.length, BIG_EXTENT, mode)
            assertEquals("mode=$mode", blockStart, breakIndex)
        }
    }

    @Test
    fun aBlockThatIsNotFirstInRightToLeftTextEndsTheLineBeforeIt() {
        var blockStart = 0
        val typesetter = typesetterOf {
            append(arabicText(2))
            blockStart = appendBlock()
            append(arabicText(2))
        }

        val breakIndex = typesetter.suggestForwardBreak(0, typesetter.spanned.length, BIG_EXTENT, BreakMode.LINE)
        assertEquals(blockStart, breakIndex)
    }

    // Forward: a block that is the first thing keeps its own line.

    @Test
    fun aBlockThatIsFirstKeepsItsLineThroughTheNewlineThatFollows() {
        val typesetter = typesetterOf {
            appendBlock()
            append("\nBBBB")
        }

        val breakIndex = typesetter.suggestForwardBreak(0, typesetter.spanned.length, BIG_EXTENT, BreakMode.LINE)
        assertEquals(2, breakIndex) // The block (1 char) plus the newline.
    }

    @Test
    fun aBlockThatIsFirstAbsorbsSpacesBeforeTheNewlineToo() {
        val typesetter = typesetterOf {
            appendBlock()
            append("  \nCCCC")
        }

        val breakIndex = typesetter.suggestForwardBreak(0, typesetter.spanned.length, BIG_EXTENT, BreakMode.LINE)
        assertEquals(4, breakIndex) // The block, two spaces and the newline.
    }

    @Test
    fun aBlockThatIsFirstAndHasNoTrailingWhitespaceKeepsOnlyItself() {
        val typesetter = typesetterOf {
            appendBlock()
            append("CCCC")
        }

        val breakIndex = typesetter.suggestForwardBreak(0, typesetter.spanned.length, BIG_EXTENT, BreakMode.LINE)
        assertEquals(1, breakIndex)
    }

    // Forward: a block immediately followed by another block - each gets its own line.

    @Test
    fun twoConsecutiveBlocksEachEndUpAloneOnTheirOwnLine() {
        var secondBlockStart = 0
        val typesetter = typesetterOf {
            appendBlock()
            secondBlockStart = appendBlock()
            append("CCCC")
        }
        val end = typesetter.spanned.length

        // The first block is the first thing: its own (empty) line, nothing to absorb after it
        // since the very next character is the second block, not whitespace.
        val firstBreak = typesetter.suggestForwardBreak(0, end, BIG_EXTENT, BreakMode.LINE)
        assertEquals(1, firstBreak)

        // Starting right after it, the second block is now the first thing for that search.
        val secondBreak = typesetter.suggestForwardBreak(firstBreak, end, BIG_EXTENT, BreakMode.LINE)
        assertEquals(secondBlockStart + 1, secondBreak)
    }

    // Backward: a block that is the nearest thing to the boundary absorbs the separator before it.

    @Test
    fun aBlockNearestTheBoundaryAbsorbsTheNewlineBeforeIt() {
        var blockStart = 0
        val typesetter = typesetterOf {
            append("AAAA\n")
            blockStart = appendBlock()
        }
        val end = typesetter.spanned.length

        for (mode in BreakMode.values()) {
            val breakIndex = typesetter.suggestBackwardBreak(0, end, BIG_EXTENT, mode)
            assertEquals("mode=$mode", blockStart - 1, breakIndex) // The newline, and the block.
        }
    }

    @Test
    fun aBlockNearestTheBoundaryInRightToLeftTextAbsorbsTheSeparatorBeforeIt() {
        var blockStart = 0
        val typesetter = typesetterOf {
            append(arabicText(2))
            append("\n")
            blockStart = appendBlock()
        }
        val end = typesetter.spanned.length

        val breakIndex = typesetter.suggestBackwardBreak(0, end, BIG_EXTENT, BreakMode.LINE)
        assertEquals(blockStart - 1, breakIndex)
    }

    @Test
    fun aBlockNearestTheBoundaryAbsorbsIndentationAfterItsOwnNewlineToo() {
        var blockStart = 0
        val typesetter = typesetterOf {
            append("AAAA\n  ")
            blockStart = appendBlock()
        }
        val end = typesetter.spanned.length

        val breakIndex = typesetter.suggestBackwardBreak(0, end, BIG_EXTENT, BreakMode.LINE)
        assertEquals(4, breakIndex) // The newline that starts the block's line, and the indent.
    }

    @Test
    fun aBlockNearestTheBoundaryWithNoLeadingWhitespaceKeepsOnlyItself() {
        var blockStart = 0
        val typesetter = typesetterOf {
            append("AAAA")
            blockStart = appendBlock()
        }
        val end = typesetter.spanned.length

        val breakIndex = typesetter.suggestBackwardBreak(0, end, BIG_EXTENT, BreakMode.LINE)
        assertEquals(blockStart, breakIndex)
    }

    @Test
    fun theLastOfTwoConsecutiveBlocksIsNearestAndKeepsOnlyItself() {
        var firstBlockStart = 0
        var secondBlockStart = 0
        val typesetter = typesetterOf {
            append("AAAA")
            firstBlockStart = appendBlock()
            secondBlockStart = appendBlock()
        }
        val end = typesetter.spanned.length

        // Only the second (nearest) block is kept; the first block and the text before it are
        // excluded entirely, exactly mirroring the forward two-consecutive-blocks case.
        val breakIndex = typesetter.suggestBackwardBreak(0, end, BIG_EXTENT, BreakMode.LINE)
        assertEquals(secondBlockStart, breakIndex)
        assertEquals(firstBlockStart + 1, secondBlockStart)
    }

    // Backward: a block that is NOT the nearest thing to the boundary - the boundary snaps to the
    // block's own end, pulling in nothing from the block or before it. A combining mark right
    // after the block is what reliably produces this: by the Unicode line/grapheme break rules, a
    // combining mark is never separated from what precedes it, so the mark and the block are
    // measured together in one step, with the step's near (boundary) edge past the mark, not at
    // the block's own end.

    @Test
    fun aBlockNotNearestTheBoundarySnapsToItsOwnEnd() {
        var blockStart = 0
        var blockEnd = 0
        val typesetter = typesetterOf {
            append("CCCC")
            blockStart = appendBlock()
            blockEnd = length
            append("́") // Combining acute accent - stays glued to the block before it.
            append("DDDD")
        }
        val end = typesetter.spanned.length

        for (mode in BreakMode.values()) {
            val breakIndex = typesetter.suggestBackwardBreak(0, end, BIG_EXTENT, mode)
            // The mark and "DDDD" are kept (they came after the block); the block and "CCCC"
            // before it are not.
            assertEquals("mode=$mode", blockEnd, breakIndex)
        }
    }

    // Backward + forward together (as createMiddleTruncatedLine uses them): the two halves must
    // not disagree about a block near the middle.

    @Test
    fun aBlockNearTheMiddleIsKeptWholeByOnlyOneOfTheTwoHalves() {
        var blockStart = 0
        val typesetter = typesetterOf {
            append("AAAA")
            blockStart = appendBlock()
            append("BBBB")
        }
        val end = typesetter.spanned.length

        val firstHalfEnd = typesetter.suggestForwardBreak(0, end, BIG_EXTENT / 2, BreakMode.LINE)
        val secondHalfStart = typesetter.suggestBackwardBreak(0, end, BIG_EXTENT / 2, BreakMode.LINE)

        // The forward half stops before the block (not the first thing); the backward half, once
        // it reaches back far enough, would find the block nearest its own boundary and keep it -
        // either way, the two halves must not both claim the block, nor leave a gap that splits it.
        assertEquals(blockStart, firstHalfEnd)
        org.junit.Assert.assertTrue(secondHalfStart <= blockStart)
    }
}
