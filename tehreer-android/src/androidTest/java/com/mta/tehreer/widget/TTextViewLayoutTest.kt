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

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Coordinates, padding, culling and repainting of [TTextView]. */
class TTextViewLayoutTest {
    private lateinit var host: TextViewHost

    @Before
    fun setUp() {
        host = TextViewHost()
    }

    private fun showLongText(): SpannableStringBuilder {
        val text = SpannableStringBuilder(arabicText(70))
        host.show(text)

        // The line boxes are delivered in chunks of 64 lines; stay below.
        val lineCount = host.lines.size
        assertTrue("$lineCount lines is not a good length for these tests", lineCount in 20..63)

        return text
    }

    private fun assertVisibleLinesExist() {
        val lineViews = host.lineViews()
        val expected = host.expectedVisibleLines()
        assertTrue(expected.isNotEmpty())

        for (line in expected) {
            val lineView = lineViews.firstOrNull { it.line === line }
            assertTrue(
                "No line view for the line at ${line.originY} with the scroll at ${host.scrollY}",
                lineView != null
            )

            // The view must really be laid out over its line.
            val top = line.originY - line.ascent
            val bottom = line.originY + line.descent + line.leading
            assertTrue(lineView!!.top <= Math.ceil(top.toDouble()).toInt())
            assertTrue(lineView.bottom >= Math.floor(bottom.toDouble()).toInt())
        }
    }

    private fun assertCullingAtEveryScrollOffset() {
        val maxScroll = host.maxScrollY
        assertTrue("$maxScroll is not enough to scroll through", maxScroll > host.height)

        var mostLines = 0
        for (offset in listOf(0, 1, 137, 300, maxScroll / 2, maxScroll - 90, maxScroll)) {
            host.scrollTo(offset)
            assertVisibleLinesExist()
            mostLines = maxOf(mostLines, host.expectedVisibleLines().size)
        }

        // Views are recycled, not piled up.
        val viewCount = host.lineViews().size
        assertTrue("$viewCount line views for at most $mostLines visible lines", viewCount <= mostLines + 3)
    }

    @Test
    fun linesAreCulledAgainstTheViewportWithoutPadding() {
        showLongText()
        assertCullingAtEveryScrollOffset()
    }

    @Test
    fun linesAreCulledAgainstTheViewportWithPadding() {
        onMain { host.view.setPadding(30, 100, 30, 60) }
        showLongText()

        // The text is not clipped to the padding, so the lines that scroll through the top
        // padding have to be there.
        assertFalse(host.view.clipToPadding)
        assertCullingAtEveryScrollOffset()
    }

    @Test
    fun linesAreCulledAgainstTheViewportInsideAWrapper() {
        onMain { host.view.setPadding(0, 40, 0, 0) }

        val wrapper = onMain { LinearLayout(host.view.context).apply { orientation = LinearLayout.VERTICAL } }
        host.wrapContainer(
            wrapper,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        )
        onMain {
            wrapper.addView(View(host.view.context), 0, LinearLayout.LayoutParams(1, 90))
            (host.container.layoutParams as LinearLayout.LayoutParams).apply {
                topMargin = 70
                leftMargin = 25
                rightMargin = 25
            }
        }

        showLongText()

        val (originX, originY) = host.containerOrigin()
        assertEquals(25, originX)
        assertEquals(40 + 90 + 70, originY)
        assertCullingAtEveryScrollOffset()
    }

    @Test
    fun clipToPaddingIsFalseByDefaultAndKeepsAnExplicitValue() {
        assertFalse(host.view.clipToPadding)

        val explicit = onMain {
            TTextView(host.view.context).apply { clipToPadding = true }
        }
        assertTrue(explicit.clipToPadding)
    }

    @Test
    fun paddingAndTextKeepTheirPlace() {
        onMain { host.view.setPadding(30, 100, 30, 60) }
        showLongText()

        val (originX, originY) = host.containerOrigin()
        assertEquals(30, originX)
        assertEquals(100, originY)
        assertEquals(host.width - 60, host.container.width)

        host.scrollTo(250)
        assertEquals(100 - 250, host.containerOrigin().second)
    }

    @Test
    fun scrollingDoesNotRequestALayoutOfTheContainer() {
        showLongText()

        // A line that comes in for the first time is a new child, which is a layout. Go through
        // all of the text to have all the views that it takes.
        for (offset in 0..host.maxScrollY step 25) {
            host.scrollTo(offset)
        }
        host.scrollTo(0)
        host.relayout()
        assertFalse(onMain { host.container.isLayoutRequested })

        // From there on, it is views that are recycled.
        for (offset in listOf(40, 200, 333, 900, 1200, 640, 3, host.maxScrollY)) {
            host.scrollTo(offset)
            assertFalse("A scroll to $offset requested a layout", onMain { host.container.isLayoutRequested })
        }
    }

    @Test
    fun textColorRepaintsTheLineViewsThatAreAlreadyThere() {
        showLongText()
        assertTrue(host.lineViews().all { it.renderer.fillColor == Color.BLACK })

        onMain { host.view.setTextColor(Color.RED) }

        val lineViews = host.lineViews()
        assertTrue(lineViews.isNotEmpty())
        assertTrue(lineViews.all { it.renderer.fillColor == Color.RED })
        assertEquals(Color.RED, host.view.textColor)

        // ...and the ink that is drawn is red.
        val bitmap = host.render()
        var red = 0
        var black = 0
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                if (Color.red(pixel) > 200 && Color.green(pixel) < 60) red++
                if (Color.red(pixel) < 60 && Color.green(pixel) < 60) black++
            }
        }
        assertTrue(red > 500)
        assertEquals(0, black)
    }

    @Test
    fun separatorColorReachesTheLineViewsThatAreAlreadyThere() {
        showLongText()
        assertTrue(host.lineViews().all { it.separatorColor == Color.TRANSPARENT })

        onMain { host.view.setSeparatorColor(Color.BLUE) }

        assertTrue(host.lineViews().all { it.separatorColor == Color.BLUE })

        host.scrollTo(400)
        assertTrue(host.lineViews().all { it.separatorColor == Color.BLUE })
    }

    @Test
    fun charIndexUnderPositionIsNegativeOutsideTheText() {
        onMain { host.view.setPadding(30, 100, 30, 60) }
        showLongText()

        val lines = host.lines
        val first = lines.first()
        val (firstX, firstY) = host.centerOf(first)

        // On the first line.
        val offset = onMain { host.view.getCharIndexUnderPosition(firstX, firstY) }
        assertTrue(offset >= first.charStart && offset < first.charEnd)

        // In the top padding, above the first line.
        assertEquals(-1, onMain { host.view.getCharIndexUnderPosition(firstX, 40f) })
        assertEquals(-1, onMain { host.view.getCharIndexUnderPosition(firstX, 99f) })
        // In the left and right padding.
        assertEquals(-1, onMain { host.view.getCharIndexUnderPosition(5f, firstY) })
        assertEquals(-1, onMain { host.view.getCharIndexUnderPosition(host.width - 5f, firstY) })
        // Outside the view.
        assertEquals(-1, onMain { host.view.getCharIndexUnderPosition(-10f, firstY) })
        assertEquals(-1, onMain { host.view.getCharIndexUnderPosition(firstX, -10f) })

        // In the bottom padding, below the last line, at the end.
        host.scrollTo(host.maxScrollY)
        assertEquals(-1, onMain { host.view.getCharIndexUnderPosition(firstX, host.height - 20f) })
        val last = lines.last()
        val (lastX, lastY) = host.centerOf(last)
        val lastOffset = onMain { host.view.getCharIndexUnderPosition(lastX, lastY) }
        assertTrue(lastOffset >= last.charStart && lastOffset < last.charEnd)
    }

    @Test
    fun charIndexForPositionFindsTheNearestLineOutsideTheText() {
        onMain { host.view.setPadding(30, 100, 30, 60) }
        showLongText()

        val lines = host.lines
        val first = lines.first()
        val (firstX, firstY) = host.centerOf(first)

        // In the top padding, above the first line.
        val above = onMain { host.view.getCharIndexForPosition(firstX, 40f) }
        assertTrue(above >= first.charStart && above < first.charEnd)

        // In the bottom padding, below the last line.
        val last = lines.last()
        val (lastX, _) = host.centerOf(last)
        host.scrollTo(host.maxScrollY)
        val below = onMain { host.view.getCharIndexForPosition(lastX, host.height - 20f) }
        assertTrue(below >= last.charStart && below < last.charEnd)

        // Beside the text of a line it is still -1.
        assertEquals(-1, onMain { host.view.getCharIndexForPosition(-10f, firstY) })
    }

    @Test
    fun charIndexForPositionFollowsTheScroll() {
        onMain { host.view.setPadding(30, 100, 30, 60) }
        showLongText()

        val lines = host.lines

        for (offset in listOf(0, 137, 300, host.maxScrollY / 2, host.maxScrollY)) {
            host.scrollTo(offset)

            var checked = 0
            for (line in host.expectedVisibleLines()) {
                val (x, y) = host.centerOf(line)
                if (y < 100 || y > host.height - 60) continue

                val index = onMain { host.view.getCharIndexForPosition(x, y) }
                assertTrue(
                    "Scroll $offset, line ${lines.indexOf(line)}: $index is not in " +
                            "[${line.charStart}, ${line.charEnd})",
                    index >= line.charStart && index < line.charEnd
                )
                checked++
            }
            assertTrue(checked > 3)
        }
    }

    @Test
    fun charIndexForPositionIsNegativeInTheGapsOfAShortLastLine() {
        host.show(SpannableStringBuilder(arabicText(40) + "\nابجد"))
        host.scrollTo(host.maxScrollY)

        val last = host.lines.last()
        val (originX, _) = host.containerOrigin()
        val (_, lastY) = host.centerOf(last)

        // The text is right to left, so the last line, which is short, sits at the right.
        assertTrue(last.paragraphLevel.toInt() and 1 == 1)
        assertTrue(originX + last.originX > 40)

        assertEquals(-1, onMain { host.view.getCharIndexForPosition(originX + 2f, lastY) })
        assertNotEquals(-1, onMain { host.view.getCharIndexForPosition(originX + last.originX + 2f, lastY) })
    }

    @Test
    fun charIndexForPositionOnRightToLeftTextRunsFromTheRight() {
        showLongText()

        val line = host.lines[2]
        val (originX, originY) = host.containerOrigin()
        val y = originY + line.originY - line.ascent / 2 + line.descent / 2

        assertTrue(line.paragraphLevel.toInt() and 1 == 1)

        val atRight = onMain { host.view.getCharIndexForPosition(originX + line.originX + line.width - 2, y) }
        val atLeft = onMain { host.view.getCharIndexForPosition(originX + line.originX + 2, y) }

        // The text starts at the right edge of the line and ends at its left edge.
        assertTrue("$atRight for the start of ${line.charStart}", atRight in line.charStart..line.charStart + 2)
        assertTrue("$atLeft for the end of ${line.charEnd}", atLeft in line.charEnd - 3 until line.charEnd)
    }

    @Test
    fun charIndexForPositionOnLeftToRightTextRunsFromTheLeft() {
        val text = SpannableStringBuilder("abc def ghi jkl mno pqr stu vwx yz ".repeat(20))
        host.show(text)

        val line = host.lines[1]
        val (originX, originY) = host.containerOrigin()
        val y = originY + line.originY - line.ascent / 2 + line.descent / 2

        assertTrue(line.paragraphLevel.toInt() and 1 == 0)

        val atLeft = onMain { host.view.getCharIndexForPosition(originX + line.originX + 1, y) }
        val atRight = onMain { host.view.getCharIndexForPosition(originX + line.originX + line.width - 1, y) }

        assertTrue("$atLeft for the start of ${line.charStart}", atLeft in line.charStart..line.charStart + 1)
        assertTrue("$atRight for the end of ${line.charEnd}", atRight in line.charEnd - 3 until line.charEnd)
    }

    @Test
    fun theViewportGrowingShowsTheLinesThatComeIn() {
        showLongText()
        host.scrollTo(300)

        val before = host.lineViews().count { it.line != null }
        onMain {
            host.view.measure(
                View.MeasureSpec.makeMeasureSpec(host.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(host.height + 400, View.MeasureSpec.EXACTLY)
            )
            host.view.layout(0, 0, host.width, host.height + 400)
        }

        assertTrue(host.lineViews().count { it.line != null } > before)
    }
}
