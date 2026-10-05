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

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.mta.tehreer.internal.layout.ReplacementRun
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.util.TypefaceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Reuses [NeverShownBlockSpan] (declared in [BreakResolverViewSpanTest]'s file, same package) - a
 * minimal block span, just for exercising [RunCollection.findBlockForward]/
 * [RunCollection.findBlockBackward] directly.
 *
 * Direct tests of [RunCollection.findBlockForward]/[RunCollection.findBlockBackward] - the
 * allocation-free, cached-and-binary-searched replacements for the old `Measurement`-returning
 * `measureChars`/`measureCharsFromEnd`. Reaches the intrinsic [RunCollection] through
 * [Typesetter.getRuns], which is package-private to `com.mta.tehreer.layout` - the same reason
 * this test lives in that package rather than `com.mta.tehreer.internal.layout`.
 */
class RunCollectionBlockTest {
    private fun SpannableStringBuilder.appendBlock(): Int {
        val start = length
        append("￼")
        setSpan(NeverShownBlockSpan(), start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return start
    }

    private fun typesetterOf(build: SpannableStringBuilder.() -> Unit): Typesetter {
        val spanned = SpannableStringBuilder().apply(build)
        val typeface = TypefaceStore.getNafeesWeb()
        val defaultSpans = listOf<Any>(TypefaceSpan(typeface), TypeSizeSpan(32.0f))

        return Typesetter(spanned, defaultSpans)
    }

    @Test
    fun aRangeWithNoBlockFindsNothingEitherWay() {
        val typesetter = typesetterOf { append("AAAA BBBB CCCC") }
        val runs = typesetter.getRuns()
        val end = typesetter.spanned.length

        assertNull(runs.findBlockForward(0, end))
        assertNull(runs.findBlockBackward(0, end))
    }

    @Test
    fun aSingleBlockIsFoundBothWays() {
        var blockStart = 0
        val typesetter = typesetterOf {
            append("AAAA")
            blockStart = appendBlock()
            append("BBBB")
        }
        val runs = typesetter.getRuns()
        val end = typesetter.spanned.length

        val forward = runs.findBlockForward(0, end)
        val backward = runs.findBlockBackward(0, end)

        assertEquals(blockStart, forward?.startIndex)
        assertEquals(blockStart, backward?.startIndex)

        // Outside the block's own range, on either side, there is nothing to find.
        assertNull(runs.findBlockForward(0, blockStart))
        assertNull(runs.findBlockBackward(blockStart + 1, end))
    }

    @Test
    fun forwardReturnsTheFirstOfTwoBlocksAndBackwardTheLast() {
        var firstStart = 0
        var secondStart = 0
        val typesetter = typesetterOf {
            append("AAAA")
            firstStart = appendBlock()
            append("BBBB")
            secondStart = appendBlock()
            append("CCCC")
        }
        val runs = typesetter.getRuns()
        val end = typesetter.spanned.length

        val forward = runs.findBlockForward(0, end)
        val backward = runs.findBlockBackward(0, end)

        assertEquals(firstStart, forward?.startIndex)
        assertEquals(secondStart, backward?.startIndex)

        // A range that only reaches the first block finds it both ways; a range that starts
        // right after it only reaches the second.
        assertEquals(firstStart, runs.findBlockForward(0, secondStart)?.startIndex)
        assertEquals(firstStart, runs.findBlockBackward(0, secondStart)?.startIndex)
        assertEquals(secondStart, runs.findBlockForward(firstStart + 1, end)?.startIndex)
        assertEquals(secondStart, runs.findBlockBackward(firstStart + 1, end)?.startIndex)
    }

    @Test
    fun theBlockItselfIsReturned() {
        var blockStart = 0
        val typesetter = typesetterOf {
            append("AAAA")
            blockStart = appendBlock()
            append("BBBB")
        }
        val runs = typesetter.getRuns()
        val end = typesetter.spanned.length

        val block = runs.findBlockForward(0, end)
        assert(block is ReplacementRun)
        assertEquals(blockStart, block!!.startIndex)
        assertEquals(blockStart + 1, block.endIndex)
    }
}
