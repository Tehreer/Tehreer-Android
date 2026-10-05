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

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * `TextContainer.layoutLines()`'s culling through the pages of `LineBoxes`, on a document long
 * enough for the difference from a linear scan to matter: the visible set at several scroll
 * offsets must be the one a full scan would produce, and a scroll deep into the document must not
 * be meaningfully slower than one at the top.
 *
 * `LineBoxesTask` delivers the boxes in chunks of 64 lines, and [TextViewHost.show] only waits for
 * the first one. Every test here waits for the last chunk too, so that no task keeps posting to
 * the main thread after the test method returns.
 */
class TTextViewLineCullingScaleTest {
    private lateinit var host: TextViewHost

    @Before
    fun setUp() {
        host = TextViewHost()
    }

    private fun assertVisibleLinesMatch(offset: Int) {
        host.scrollTo(offset)

        val expected = host.expectedVisibleLines()
        val lineViews = host.lineViews()

        assertTrue("nothing visible at scroll $offset", expected.isNotEmpty())

        for (line in expected) {
            assertTrue(
                "no line view for the line at ${line.originY} at scroll $offset",
                lineViews.any { it.line === line }
            )
        }

        // No line view left over for a line that is not actually visible (the forward scan
        // stopped correctly instead of over-collecting).
        for (lineView in lineViews) {
            val line = lineView.line ?: continue
            assertTrue(
                "a line view for ${line.originY} is on screen but not in the expected set " +
                    "at scroll $offset",
                expected.any { it === line } || !lineView.isShown
            )
        }
    }

    @Test
    fun cullingMatchesTheOldLinearScanAtTopMiddleAndBottomOfALongDocument() {
        host.show(spannedOf { append(arabicText(500)) })

        val lineCount = host.lines.size
        assertTrue("$lineCount lines is not long enough for this test", lineCount > 250)

        val maxScroll = host.maxScrollY
        assertTrue(maxScroll > host.height)
        host.awaitFullyFramed()

        for (offset in listOf(0, 1, maxScroll / 4, maxScroll / 2, (maxScroll * 3) / 4, maxScroll - 1, maxScroll)) {
            assertVisibleLinesMatch(offset)
        }
    }

    @Test
    fun scrollingDeepIntoALongDocumentIsNotMeaningfullySlowerThanNearTheTop() {
        host.show(spannedOf { append(arabicText(700)) })

        val lineCount = host.lines.size
        assertTrue("$lineCount lines is not long enough for this test", lineCount > 350)

        val maxScroll = host.maxScrollY
        host.awaitFullyFramed()

        fun timeScrolls(startOffset: Int, count: Int): Long {
            val start = System.nanoTime()
            repeat(count) { i -> host.scrollTo((startOffset + i).coerceIn(0, maxScroll)) }
            return System.nanoTime() - start
        }

        // Warm up caches/JIT before measuring either side.
        timeScrolls(0, 60)

        val nearTop = timeScrolls(0, 300)
        val nearBottom = timeScrolls((maxScroll - 300).coerceAtLeast(0), 300)

        // An O(n) scan from index 0 on every scroll event would make scrolling near the bottom
        // of a long document meaningfully more expensive than scrolling near the top; an
        // O(log n + k) one should not - allow generous slack, this is about orders of magnitude,
        // not a tight timing budget on a shared/emulated device.
        assertTrue(
            "scrolling near the bottom ($nearBottom ns for 300 events) was far slower than near " +
                "the top ($nearTop ns) of a $lineCount-line document - looks like an O(n) scan",
            nearBottom < nearTop * 5 + 200_000_000
        )
    }
}
