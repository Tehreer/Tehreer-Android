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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * `TextContainer.layoutLines()`'s culling through the `LineLocator` of Core, checked against a
 * brute-force scan of the line boxes. Uses `TextContainer.lineBoxesForTesting` and
 * `visibleRectForTesting`.
 */
class TTextViewPageBucketCullingTest {
    private lateinit var host: TextViewHost

    @Before
    fun setUp() {
        host = TextViewHost()
    }

    private val container: TextContainer
        get() = host.container as TextContainer

    /** Waits until the line locator of all [lineCount] lines has arrived from the `LineLocatorTask`. */
    private fun awaitFullyFramed(lineCount: Int) {
        host.awaitUntil("the line locator of the whole document has arrived") {
            container.lineBoxesForTesting().size >= lineCount
        }
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
}
