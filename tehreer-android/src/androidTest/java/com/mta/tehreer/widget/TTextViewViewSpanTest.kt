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

import android.content.Context
import android.graphics.Rect
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.MotionEvent
import android.view.View
import com.mta.tehreer.layout.ComposedLine
import com.mta.tehreer.layout.style.ViewSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.roundToInt

/**
 * A view that takes every touch and counts its clicks: a press followed by a release. A real click
 * is performed by a message that a view outside a window never gets to run.
 */
internal class ClickView(context: Context) : View(context) {
    var clicks = 0
        private set

    /** Every action that reached the view. */
    val actions = mutableListOf<Int>()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        actions.add(event.actionMasked)

        if (event.actionMasked == MotionEvent.ACTION_UP) {
            clicks += 1
        }

        return true
    }
}

/** A span with fixed sizes whose views are [ClickView]s, and that remembers every view it made. */
internal class TestViewSpan(
    private val kind: Placement,
    private val roomWidth: Int = 0,
    private val roomHeight: Int,
    private val roomMargins: Rect = Rect(),
    private val roomOffset: Int = 0,
    private val retain: Boolean = false
) : ViewSpan() {
    val created = mutableListOf<ClickView>()

    override val placement: Placement get() = kind
    override val width: Int get() = roomWidth
    override val height: Int get() = roomHeight
    override val margins: Rect get() = Rect(roomMargins)
    override val baselineOffset: Int get() = roomOffset
    override val retainWhenOffscreen: Boolean get() = retain

    override fun createView(context: Context): View = ClickView(context).also { created.add(it) }
}

/** Views inside the text: [ViewSpan]. */
class TTextViewViewSpanTest {
    private lateinit var host: TextViewHost

    @Before
    fun setUp() {
        host = TextViewHost()
    }

    private fun block(height: Int = 120, top: Int = 0, bottom: Int = 0, retain: Boolean = false) =
        TestViewSpan(ViewSpan.Placement.BLOCK, 0, height, Rect(0, top, 0, bottom), 0, retain)

    private fun inline(width: Int = 80, height: Int = 50, offset: Int = 0) =
        TestViewSpan(ViewSpan.Placement.INLINE, width, height, Rect(), offset)

    private fun SpannableStringBuilder.appendView(span: ViewSpan, inline: Boolean = false): Int {
        val start = length
        append(if (inline) "￼" else "￼\n")
        setSpan(span, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return start
    }

    private fun lineOf(charIndex: Int): ComposedLine = onMain {
        val frame = host.view.composedFrame!!
        frame.lines[frame.getLineIndexForChar(charIndex)]
    }

    private fun boundsOf(view: View) = Rect(view.left, view.top, view.right, view.bottom)

    /** The rect of the view in the coordinates of the container. */
    private fun rectOf(span: ViewSpan): Rect = onMain { boundsOf(span.view!!) }

    // Blocks.

    @Test
    fun aBlockViewFillsTheLineThatIsAsTallAsIt() {
        val span = block(height = 120, top = 30, bottom = 20)
        var start = 0

        host.show(spannedOf {
            append(arabicText(6))
            append("\n")
            start = appendView(span)
            append(arabicText(6))
        })

        val view = span.view
        assertNotNull(view)
        assertTrue(host.container === view!!.parent)

        // The view is as wide as the text and as tall as it was told to be...
        val rect = rectOf(span)
        assertEquals(0, rect.left)
        assertEquals(host.width, rect.right)
        assertEquals(120, rect.height())

        // ...the line is as tall as the view and its margins, and no more, nothing above and
        // nothing below the view but the margins...
        val line = lineOf(start)
        assertEquals(170f, line.height, 0.001f)
        assertEquals(0f, line.leading, 0.001f)
        assertEquals((line.originY - line.ascent + 30).roundToInt(), rect.top)

        // ...and the text that follows starts where the line ends.
        val next = lineOf(start + 2)
        assertEquals(line.originY - line.ascent + line.height, next.originY - next.ascent, 0.01f)
    }

    @Test
    fun aBlockViewShorterThanTheTextHasNoBlankSpaceAround() {
        val span = block(height = 10)
        var start = 0

        host.show(spannedOf {
            append(arabicText(2))
            append("\n")
            start = appendView(span)
            append(arabicText(2))
        })

        assertEquals(10f, lineOf(start).height, 0.001f)
        assertEquals(10, rectOf(span).height())
    }

    @Test
    fun aBlockViewIsAtTheSamePlaceInLeftToRightAndRightToLeftText() {
        val ltr = block(height = 90)
        val rtl = block(height = 90)
        var ltrStart = 0
        var rtlStart = 0

        host.show(spannedOf {
            append("Hello there. ".repeat(4))
            append("\n")
            ltrStart = appendView(ltr)
            append("Hello there. ".repeat(4))
            append("\n")
            append(arabicText(4))
            append("\n")
            append(arabicText(4))
            rtlStart = appendView(rtl)
            append(arabicText(4))
        })

        assertEquals(0, lineOf(ltrStart).paragraphLevel.toInt() and 1)
        assertEquals(1, lineOf(rtlStart).paragraphLevel.toInt() and 1)

        for ((span, start) in listOf(ltr to ltrStart, rtl to rtlStart)) {
            val line = lineOf(start)
            val rect = rectOf(span)

            assertEquals(0, rect.left)
            assertEquals(host.width, rect.right)
            assertEquals((line.originY - line.ascent).roundToInt(), rect.top)
            assertEquals(90, rect.height())
        }
    }

    @Test
    fun aViewSpanWrittenInJavaWorks() {
        val span = JavaViewSpan()
        var start = 0

        host.show(spannedOf {
            append(arabicText(2))
            append("\n")
            start = appendView(span)
            append(arabicText(2))
        })

        assertEquals(76f, lineOf(start).height, 0.001f)
        assertNotNull(span.view)
        assertEquals(64, rectOf(span).height())
    }

    // Inline views.

    private fun assertInlineViewIsOnItsCharacter(span: ViewSpan, start: Int, offset: Int, height: Int) {
        val line = lineOf(start)
        val rect = rectOf(span)

        // Where the character is, worked out from the caret edges of the line.
        val edges = onMain { line.computeVisualEdges(start, start + 1) }
        assertEquals(2, edges.size)
        assertEquals((line.originX + edges[0]).roundToInt(), rect.left)
        assertEquals((line.originX + edges[1]).roundToInt(), rect.right)

        // The bottom is under the baseline by the offset.
        assertEquals((line.originY + offset).roundToInt(), rect.bottom)
        assertEquals(height, rect.height())
    }

    @Test
    fun anInlineViewIsOnItsCharacterInRightToLeftText() {
        val span = inline(width = 80, height = 50)
        var start = 0

        host.show(spannedOf {
            append(arabicText(1))
            start = appendView(span, inline = true)
            append(arabicText(1))
            append("\n")
            append(arabicText(30))
        })

        assertEquals(1, lineOf(start).paragraphLevel.toInt() and 1)
        assertInlineViewIsOnItsCharacter(span, start, 0, 50)
        assertEquals(80, rectOf(span).width())
    }

    @Test
    fun anInlineViewIsOnItsCharacterInLeftToRightText() {
        val span = inline(width = 80, height = 50, offset = 12)
        var start = 0

        host.show(spannedOf {
            append("Hello there. ")
            start = appendView(span, inline = true)
            append(" Hello there again.")
            append("\n")
            append(arabicText(30))
        })

        assertEquals(0, lineOf(start).paragraphLevel.toInt() and 1)
        assertInlineViewIsOnItsCharacter(span, start, 12, 50)
        assertEquals(80, rectOf(span).width())
    }

    @Test
    fun aLineGrowsToHoldAnInlineViewTallerThanTheText() {
        val tall = inline(width = 60, height = 200, offset = 40)
        val small = inline(width = 60, height = 8)
        var tallStart = 0
        var smallStart = 0

        host.show(spannedOf {
            append("Hello there. ")
            tallStart = appendView(tall, inline = true)
            append("\n")
            append("Hello there. ")
            smallStart = appendView(small, inline = true)
            append("\n")
            append(arabicText(30))
        })

        // The room above the baseline is 160 and below it 40, the line at least that.
        val tallLine = lineOf(tallStart)
        assertTrue(tallLine.ascent >= 160f)
        assertTrue(tallLine.descent >= 40f)

        // A small view does not make the line any taller than the text needs.
        val smallLine = lineOf(smallStart)
        assertTrue(smallLine.height < 100f)
    }

    // Following the text.

    private fun showLongTextWithViews(first: ViewSpan, last: ViewSpan): Pair<Int, Int> {
        var firstStart = 0
        var lastStart = 0

        host.show(spannedOf {
            append(arabicText(2))
            append("\n")
            firstStart = appendView(first)
            append(arabicText(70))
            append("\n")
            lastStart = appendView(last)
            append(arabicText(2))
        })

        return Pair(firstStart, lastStart)
    }

    @Test
    fun theViewsStayOverTheirRoomsWhileTheTextScrolls() {
        val first = block()
        val last = block()
        val (firstStart, lastStart) = showLongTextWithViews(first, last)
        val maxScroll = host.maxScrollY

        val inViewCoordinates = { span: ViewSpan ->
            onMain {
                val (ox, oy) = host.containerOrigin()
                val rect = boundsOf(span.view!!)
                rect.offset(ox, oy)
                rect
            }
        }

        host.scrollTo(0)
        assertNotNull(first.view)
        assertNull(last.view)

        for (offset in listOf(0, 1, 37, 90, 150)) {
            host.scrollTo(offset)

            val line = lineOf(firstStart)
            val shown = inViewCoordinates(first)

            assertEquals((line.originY - line.ascent).roundToInt() - offset, shown.top)
            assertEquals(shown, onMain { host.view.getSpanBounds(first) })
        }

        host.scrollTo(maxScroll)
        assertNull(first.view)
        assertNotNull(last.view)

        for (offset in listOf(maxScroll, maxScroll - 1, maxScroll - 60, maxScroll - 140)) {
            host.scrollTo(offset)

            val line = lineOf(lastStart)
            val shown = inViewCoordinates(last)

            assertEquals((line.originY - line.ascent).roundToInt() - offset, shown.top)
        }
    }

    @Test
    fun aViewIsMadeWhenItsLineComesNearAndRemovedWhenItLeaves() {
        val first = block()
        val last = block()
        showLongTextWithViews(first, last)

        // Not host.maxScrollY, which scrolls there and so makes the last view.
        val maxScroll = onMain { host.container.height - host.height }
        assertTrue(maxScroll > host.height)

        // Only what is on the screen has a view: the far one is not even made.
        host.scrollTo(0)
        assertEquals(1, first.created.size)
        assertEquals(0, last.created.size)
        assertEquals(listOf<View>(first.view!!), host.spanViews())

        host.scrollTo(maxScroll)
        assertEquals(1, first.created.size)
        assertEquals(1, last.created.size)
        assertNull(first.view)
        assertEquals(listOf<View>(last.view!!), host.spanViews())

        // It is made again on return, a new one.
        host.scrollTo(0)
        assertEquals(2, first.created.size)
        assertNotSame(first.created[0], first.created[1])
        assertSame(first.created[1], first.view)
        assertNull(last.view)
        assertEquals(1, host.spanViews().size)

        // A view that is still there is not made again by every scroll.
        for (offset in 0..40 step 5) {
            host.scrollTo(offset)
        }
        assertEquals(2, first.created.size)
    }

    @Test
    fun aRetainedViewStaysAndIsLaidOutWhileItIsFarAway() {
        val first = block(retain = true)
        val last = block()
        val (firstStart, _) = showLongTextWithViews(first, last)
        val maxScroll = host.maxScrollY

        val original = first.view
        assertNotNull(original)
        val before = rectOf(first)

        host.scrollTo(maxScroll)

        // The same view, still attached, and where it was, which is well above the screen.
        assertSame(original, first.view)
        assertSame(original, host.spanViews().first { it === original })
        assertEquals(before, rectOf(first))
        assertEquals(2, host.spanViews().size)
        assertEquals(1, first.created.size)

        val line = lineOf(firstStart)
        assertEquals((line.originY - line.ascent).roundToInt(), rectOf(first).top)

        host.scrollTo(0)
        assertSame(original, first.view)
        assertEquals(1, first.created.size)
    }

    // New frames and new text.

    @Test
    fun aNewFrameLeavesExactlyTheRightViewsAttached() {
        val first = block()
        val second = block()
        host.show(spannedOf {
            append(arabicText(2))
            append("\n")
            appendView(first)
            append(arabicText(2))
            append("\n")
            appendView(second)
            append(arabicText(2))
        })

        assertEquals(2, host.spanViews().size)
        val firstView = first.view
        val secondView = second.view

        // A narrower text is a new frame.
        onMain { host.view.setPadding(30, 0, 30, 0) }
        host.awaitUntil("the text is framed for the padding") { host.view.composedFrame?.width == 540f }

        assertEquals(2, host.spanViews().size)
        assertEquals(setOf(firstView, secondView), host.spanViews().toSet())
        assertSame(firstView, first.view)
        assertEquals(540, rectOf(first).width())
        assertEquals(540, rectOf(second).width())
        assertEquals(1, first.created.size)

        // So is a change of the line spacing, and the views follow the lines.
        onMain { host.view.extraLineSpacing = 25f }
        host.awaitUntil("the lines are spaced") { (host.view.composedFrame?.lines?.get(0)?.leading ?: 0f) >= 25f }

        assertEquals(2, host.spanViews().size)
        assertEquals(1, first.created.size)
        assertEquals(1, second.created.size)

        for (span in listOf(first, second)) {
            val bounds = onMain { host.view.getSpanBounds(span) }
            assertNotNull(bounds)
            assertEquals(rectOf(span).height(), bounds!!.height())
        }
    }

    @Test
    fun newTextRemovesTheOldViewsAndMakesTheNewOnes() {
        val old = block()
        host.show(spannedOf {
            append(arabicText(2))
            append("\n")
            appendView(old)
            append(arabicText(2))
        })
        assertNotNull(old.view)

        val fresh = block(height = 70)
        host.show(spannedOf {
            append(arabicText(3))
            append("\n")
            appendView(fresh)
            append(arabicText(3))
        })

        host.awaitUntil("the new view is attached") { fresh.view != null }
        assertNull(old.view)
        assertEquals(listOf<View>(fresh.view!!), host.spanViews())
        assertEquals(70, rectOf(fresh).height())
    }

    @Test
    fun noViewIsLeftWhenTheTextIsRemoved() {
        val span = block()
        val inlineSpan = inline()
        host.show(spannedOf {
            append(arabicText(2))
            appendView(inlineSpan, inline = true)
            append("\n")
            appendView(span)
            append(arabicText(2))
        })
        assertEquals(2, host.spanViews().size)

        onMain { host.view.spanned = null }
        host.awaitUntil("the text is gone") { host.spanViews().isEmpty() && host.lineViews().isEmpty() }

        assertNull(span.view)
        assertNull(inlineSpan.view)
        assertEquals(0, host.container.childCount)
    }

    // Touches.

    @Test
    fun aClickOnTheViewReachesIt() {
        val span = block(height = 120)
        val link = RecordingLink()
        host.show(spannedOf {
            append(arabicText(6))
            setSpan(link, 12, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append("\n")
            appendView(span)
            append(arabicText(6))
        })
        onMain { host.view.onSpanClickListener = TTextView.OnSpanClickListener { _, _ -> throw AssertionError("A span was clicked") } }

        val (ox, oy) = host.containerOrigin()
        val rect = rectOf(span)

        host.tap(ox + rect.centerX().toFloat(), oy + rect.centerY().toFloat())
        assertEquals(1, span.created[0].clicks)

        host.tap(ox + rect.left + 5f, oy + rect.top + 5f)
        assertEquals(2, span.created[0].clicks)
    }

    @Test
    fun aClickOnTheMarginOfAViewIsNotOnTheViewOrOnAnySpan() {
        val span = block(height = 120, top = 40, bottom = 40)
        host.show(spannedOf {
            append(arabicText(6))
            append("\n")
            appendView(span)
            append(arabicText(6))
        })
        onMain { host.view.onSpanClickListener = TTextView.OnSpanClickListener { _, _ -> throw AssertionError("A span was clicked") } }

        val (ox, oy) = host.containerOrigin()
        val rect = rectOf(span)

        host.tap(ox + rect.centerX().toFloat(), oy + rect.top - 20f)
        host.tap(ox + rect.centerX().toFloat(), oy + rect.bottom + 20f)
        assertEquals(0, span.created[0].clicks)
    }

    @Test
    fun aTapOnTheTextAroundAViewStillClicksALink() {
        val span = block(height = 120)
        val before = RecordingLink()
        val after = RecordingLink()
        var afterStart = 0

        host.show(spannedOf {
            append(arabicText(6))
            setSpan(before, 12, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append("\n")
            appendView(span)
            afterStart = length + 8
            append(arabicText(6))
            setSpan(after, afterStart, afterStart + 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        })

        for (link in listOf(before, after)) {
            val rects = onMain { host.view.getSpanRects(link) }
            host.tap(rects[0].centerX(), rects[0].centerY())
            assertEquals(1, link.clicks)
        }

        assertEquals(0, span.created[0].clicks)
    }

    @Test
    fun aDragThatStartsOnAViewScrollsTheText() {
        val span = block(height = 200)
        host.show(spannedOf {
            append(arabicText(2))
            append("\n")
            appendView(span)
            append(arabicText(70))
        })

        val (ox, oy) = host.containerOrigin()
        val rect = rectOf(span)
        val x = ox + rect.centerX().toFloat()
        val y = oy + rect.centerY().toFloat()

        host.drag(x, y, y - 150)

        // The view wanted the touch, and lost it to the scroll, which is no click.
        assertEquals(
            listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_CANCEL),
            span.created[0].actions.filter { it != MotionEvent.ACTION_MOVE }
        )
        assertTrue("scrolled to ${host.scrollY}", host.scrollY > 80)
        assertEquals(0, span.created[0].clicks)
    }

    // Geometry.

    @Test
    fun theBoundsOfAViewSpanAreTheRectOfItsViewInViewCoordinates() {
        val span = block(height = 120, top = 30, bottom = 30)
        val inlineSpan = inline(width = 70, height = 40)
        onMain { host.view.setPadding(20, 50, 10, 0) }

        host.show(spannedOf {
            append(arabicText(2))
            append("\n")
            appendView(span)
            append(arabicText(1))
            appendView(inlineSpan, inline = true)
            append(arabicText(30))
        })

        for (offset in listOf(0, 25)) {
            host.scrollTo(offset)
            val (ox, oy) = host.containerOrigin()

            for (viewSpan in listOf(span, inlineSpan)) {
                val expected = rectOf(viewSpan).also { it.offset(ox, oy) }
                val bounds = onMain { host.view.getSpanBounds(viewSpan) }
                val rects = onMain { host.view.getSpanRects(viewSpan) }

                assertEquals(expected, bounds)
                assertEquals(1, rects.size)
            }
        }

        // The block runs from the left to the right edge of the text, whatever the padding.
        val bounds = onMain { host.view.getSpanBounds(span) }!!
        assertEquals(20, bounds.left)
        assertEquals(host.width - 10, bounds.right)
        assertFalse(bounds.isEmpty)
    }
}
