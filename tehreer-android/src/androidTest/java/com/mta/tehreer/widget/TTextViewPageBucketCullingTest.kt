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

package com.mta.tehreer.widget

import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * `TextContainer.layoutLines()`'s culling through the pages of `LineBoxes`, checked against a
 * brute-force scan of the line boxes. Uses `TextContainer.lineBoxesForTesting`,
 * `overrideLineBoxesForTesting` and `visibleRectForTesting` to engineer boxes whose tops and
 * bottoms are not monotonic, as the ink overflow of tall glyphs can make them.
 */
class TTextViewPageBucketCullingTest {
    private lateinit var host: TextViewHost

    @Before
    fun setUp() {
        host = TextViewHost()
    }

    private val container: TextContainer
        get() = host.container as TextContainer

    /** Waits until all [lineCount] boxes have arrived from the (chunked) `LineBoxesTask`. */
    private fun awaitFullyFramed(lineCount: Int) {
        host.awaitUntil("the whole document's line boxes have arrived") {
            container.lineBoxesForTesting().size >= lineCount
        }
    }

    /** The index of every line that currently has a [LineView]. */
    private fun shownBoxIndexes(): Set<Int> {
        val lines = host.lines
        return host.lineViews().mapNotNull { view ->
            val line = view.line ?: return@mapNotNull null
            val index = lines.indexOfFirst { it === line }
            if (index >= 0) index else null
        }.toSet()
    }

    /** The binary search over single boxes that `layoutLines` used to do, which assumes monotonic bottoms. */
    private fun oldFirstVisibleLineBoxIndex(boxes: List<Rect>, top: Int): Int {
        var low = 0
        var high = boxes.size

        while (low < high) {
            val mid = (low + high) ushr 1

            if (boxes[mid].bottom > top) {
                high = mid
            } else {
                low = mid + 1
            }
        }

        return low
    }

    /** The visible set that the old binary search and forward scan produced. */
    private fun oldVisibleIndexes(boxes: List<Rect>, visibleRect: Rect): Set<Int> {
        val result = mutableSetOf<Int>()
        var i = oldFirstVisibleLineBoxIndex(boxes, visibleRect.top)

        while (i < boxes.size && boxes[i].top < visibleRect.bottom) {
            if (Rect.intersects(boxes[i], visibleRect)) {
                result.add(i)
            }

            i += 1
        }

        return result
    }

    private fun bruteForceIndexes(boxes: List<Rect>, visibleRect: Rect): Set<Int> =
        boxes.indices.filter { Rect.intersects(boxes[it], visibleRect) }.toSet()

    /** Checks that every box that intersects the visible rect has a line view at [offset]. */
    private fun assertCullingMatchesBruteForce(offset: Int) {
        host.scrollTo(offset)

        val boxes = container.lineBoxesForTesting()
        val visibleRect = container.visibleRectForTesting()
        val lines = host.lines

        val expected = bruteForceIndexes(boxes, visibleRect)
        val lineViews = host.lineViews()

        assertTrue("nothing visible at scroll $offset", expected.isNotEmpty())

        for (index in expected) {
            val expectedLine = lines[index]
            assertTrue(
                "no line view for box $index (top=${boxes[index].top}, bottom=${boxes[index].bottom}) " +
                    "at scroll $offset",
                lineViews.any { it.line === expectedLine }
            )
        }
    }

    @Test
    fun cullingMatchesTheBruteForceScanAtTopMiddleAndBottomOfALongDocument() {
        host.show(spannedOf { append(arabicText(500)) })

        val lineCount = host.lines.size
        assertTrue("$lineCount lines is not long enough for this test", lineCount > 250)

        val maxScroll = host.maxScrollY
        assertTrue(maxScroll > host.height)

        awaitFullyFramed(lineCount)

        for (offset in listOf(0, 1, maxScroll / 4, maxScroll / 2, (maxScroll * 3) / 4, maxScroll - 1, maxScroll)) {
            assertCullingMatchesBruteForce(offset)
        }
    }

    /** Fewer lines than fit in one page - the single-page case. */
    @Test
    fun aShortDocumentFewerLinesThanOnePageStillWorksCorrectly() {
        host.show(spannedOf { append(arabicText(2)) })

        val lineCount = host.lines.size
        assertTrue("$lineCount lines is not short enough for this test", lineCount in 1..31)

        assertCullingMatchesBruteForce(0)

        val maxScroll = host.maxScrollY
        if (maxScroll > 0) {
            assertCullingMatchesBruteForce(maxScroll)
        }
    }

    /**
     * One box is inflated far past its nominal range, as the ink overflow of a tall glyph can do.
     * The test first finds an offset where the old binary search disagrees with a brute-force
     * scan, so the data really reproduces the flaw, and then checks that the pages find every
     * visible line at that very offset.
     */
    @Test
    fun adjacentOverlappingLineBoxesAreBothFoundDespiteLocalNonMonotonicity() {
        host.show(spannedOf { append(arabicText(500)) })

        val lineCount = host.lines.size
        assertTrue("$lineCount lines is not long enough for this test", lineCount > 300)

        awaitFullyFramed(lineCount)

        val layoutWidth = host.width
        val lineHeight = 20
        val spikeIndex = 150
        val spikeLines = 60 // the spike's box reaches 60 "line heights" past its own nominal one.

        val boxes = MutableList(lineCount) { k ->
            Rect(0, k * lineHeight, layoutWidth, (k + 1) * lineHeight)
        }
        boxes[spikeIndex] = Rect(
            0,
            spikeIndex * lineHeight,
            layoutWidth,
            spikeIndex * lineHeight + spikeLines * lineHeight
        )

        onMain { container.overrideLineBoxesForTesting(boxes.toMutableList()) }

        var foundOffset = -1
        var foundVisibleRect: Rect? = null
        var foundOldIndexes: Set<Int>? = null
        var foundExpectedIndexes: Set<Int>? = null

        val windowStart = (spikeIndex - 5) * lineHeight
        val windowEnd = (spikeIndex + spikeLines + 20) * lineHeight

        var offset = windowStart
        while (offset <= windowEnd) {
            host.scrollTo(offset.coerceAtLeast(0))

            val visibleRect = onMain { Rect(container.visibleRectForTesting()) }
            val expected = bruteForceIndexes(boxes, visibleRect)
            val old = oldVisibleIndexes(boxes, visibleRect)

            if (old != expected) {
                foundOffset = offset
                foundVisibleRect = visibleRect
                foundOldIndexes = old
                foundExpectedIndexes = expected
                break
            }

            offset += 5
        }

        assertNotEquals(
            "the synthetic overlap did not reproduce the old binary search's bug at any offset " +
                "in [$windowStart, $windowEnd] - test data needs adjusting",
            -1,
            foundOffset
        )

        assertNotEquals(foundExpectedIndexes, foundOldIndexes)

        host.scrollTo(foundOffset.coerceAtLeast(0))
        val shown = shownBoxIndexes()

        for (index in foundExpectedIndexes!!) {
            assertTrue(
                "line $index was expected to be visible at scroll $foundOffset " +
                    "(rect=$foundVisibleRect) but no line view showed it - old algorithm's " +
                    "answer was $foundOldIndexes, brute force was $foundExpectedIndexes",
                shown.contains(index)
            )
        }
    }
}
