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

import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.MotionEvent
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Links, clickable spans, replacement spans and the geometry of spans in [TTextView]. */
class TTextViewSpanTest {
    private companion object {
        /** Over white, it is (255, 127, 127), which nothing else drawn in the tests can be. */
        const val HIGHLIGHT = 0x80FF0000.toInt()
    }

    private lateinit var host: TextViewHost

    private val link = RecordingLink()
    private val box = BoxSpan(200, 100)
    private val clicked = mutableListOf<Any>()

    @Before
    fun setUp() {
        host = TextViewHost()
        onMain { host.view.highlightColor = HIGHLIGHT }
    }

    /**
     * Two paragraphs with a link in the first one and a box, which is where the reader's image is,
     * on a line of its own in between.
     */
    private fun showLinkAndBox(): SpannableStringBuilder {
        val text = spannedOf {
            append(arabicText(6))
            setSpan(link, 12, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append("\n")

            val boxStart = length
            append("￼\n")
            setSpan(box, boxStart, boxStart + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            append(arabicText(30))
        }

        host.show(text)
        return text
    }

    private fun centerOf(span: Any): Pair<Float, Float> {
        val rects = onMain { host.view.getSpanRects(span) }
        assertTrue(rects.isNotEmpty())

        return Pair(rects[0].centerX(), rects[0].centerY())
    }

    private fun tapOn(span: Any) {
        val (x, y) = centerOf(span)
        host.tap(x, y)
    }

    private fun listen(consume: Boolean = false) {
        onMain {
            host.view.setOnSpanClickListener { _, span ->
                clicked.add(span)
                consume
            }
        }
    }

    // Taps and links.

    @Test
    fun tappingALinkClicksIt() {
        showLinkAndBox()

        tapOn(link)
        assertEquals(1, link.clicks)

        tapOn(link)
        assertEquals(2, link.clicks)
    }

    @Test
    fun tappingOutsideALinkDoesNotClickIt() {
        showLinkAndBox()

        // The same line, well away from the link, and another line.
        val rect = onMain { host.view.getSpanRects(link) }[0]
        host.tap(rect.centerX(), rect.centerY() + host.lines[0].height * 2)
        host.tap(host.width - 40f, rect.centerY())
        host.tap(20f, 20f)

        assertEquals(0, link.clicks)
    }

    @Test
    fun aLinkIsFoundAfterScrollingAndInsidePadding() {
        onMain { host.view.setPadding(40, 90, 40, 30) }
        showLinkAndBox()

        for (offset in listOf(0, 20, 45)) {
            host.scrollTo(offset)

            val before = link.clicks
            tapOn(link)
            assertEquals("scrolled by $offset", before + 1, link.clicks)
        }
    }

    @Test
    fun theListenerSeesALinkFirstAndCanTakeTheClick() {
        showLinkAndBox()

        listen(consume = true)
        tapOn(link)
        assertEquals(listOf<Any>(link), clicked)
        assertEquals(0, link.clicks)

        listen(consume = false)
        tapOn(link)
        assertEquals(listOf<Any>(link, link), clicked)
        assertEquals(1, link.clicks)
    }

    @Test
    fun linksCanBeTurnedOff() {
        showLinkAndBox()
        assertTrue(host.view.linksClickable)

        onMain { host.view.linksClickable = false }
        listen()
        tapOn(link)

        assertEquals(0, link.clicks)
        assertTrue(clicked.isEmpty())

        // A press did not even highlight it.
        val (x, y) = centerOf(link)
        host.touch(MotionEvent.ACTION_DOWN, x, y)
        assertFalse(isHighlighted(link))
        host.touch(MotionEvent.ACTION_CANCEL, x, y)
    }

    @Test
    fun aUrlSpanOpensItsUrl() {
        val started = mutableListOf<Intent>()
        val context = object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun startActivity(intent: Intent) {
                started.add(intent)
            }
        }

        val urlHost = TextViewHost(context = context)
        val url = URLSpan("https://example.com/tehreer")
        urlHost.show(spannedOf {
            append(arabicText(6))
            setSpan(url, 12, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        })

        val rect = onMain { urlHost.view.getSpanRects(url) }[0]
        urlHost.tap(rect.centerX(), rect.centerY())

        assertEquals(1, started.size)
        assertEquals(Intent.ACTION_VIEW, started[0].action)
        assertEquals("https://example.com/tehreer", started[0].data.toString())
    }

    @Test
    fun cancellingAPressDoesNotClick() {
        showLinkAndBox()

        val (x, y) = centerOf(link)
        host.touch(MotionEvent.ACTION_DOWN, x, y)
        assertTrue(isHighlighted(link))

        host.touch(MotionEvent.ACTION_CANCEL, x, y)
        assertFalse(isHighlighted(link))
        assertEquals(0, link.clicks)
    }

    @Test
    fun movingOffALinkCancelsThePress() {
        showLinkAndBox()

        val rect = onMain { host.view.getSpanRects(link) }[0]
        host.touch(MotionEvent.ACTION_DOWN, rect.centerX(), rect.centerY())
        host.touch(MotionEvent.ACTION_MOVE, rect.centerX(), rect.centerY() + host.touchSlop / 2)
        host.touch(MotionEvent.ACTION_MOVE, rect.left - 3, rect.centerY())
        host.touch(MotionEvent.ACTION_UP, rect.left - 3, rect.centerY())

        assertEquals(0, link.clicks)
    }

    @Test
    fun aTapThatWobblesWithinTheSlopIsStillATap() {
        showLinkAndBox()

        val (x, y) = centerOf(link)
        host.touch(MotionEvent.ACTION_DOWN, x, y)
        host.touch(MotionEvent.ACTION_MOVE, x + 1, y + 1)
        host.touch(MotionEvent.ACTION_UP, x + 1, y + 1)

        assertEquals(1, link.clicks)
    }

    @Test
    fun aDragThatStartsOnALinkScrollsAndDoesNotClick() {
        // Room above the text, so that the link is still on screen after the drag.
        onMain { host.view.setPadding(0, 400, 0, 0) }
        showLinkAndBox()

        val (x, y) = centerOf(link)
        assertEquals(0, host.scrollY)

        host.drag(x, y, y - 250)

        assertTrue("scrolled to ${host.scrollY}", host.scrollY > 100)
        assertEquals(0, link.clicks)
        assertFalse(isHighlighted(link))
    }

    @Test
    fun aTouchThatStopsAFlingIsNotATap() {
        onMain { host.view.setPadding(0, 400, 0, 0) }
        showLinkAndBox()

        val (x, y) = centerOf(link)
        host.drag(x, y, y - 100)
        assertTrue(host.scrollY > 0)

        // The fling is still running, as nothing is there to advance it.
        val (linkX, linkY) = centerOf(link)
        host.tap(linkX, linkY)
        assertEquals(0, link.clicks)

        // It has been stopped, so the next one is a tap.
        val (nextX, nextY) = centerOf(link)
        host.tap(nextX, nextY)
        assertEquals(1, link.clicks)
    }

    // Replacement spans.

    @Test
    fun tappingAnImageReachesTheListenerAndNoLink() {
        val text = showLinkAndBox()

        // A link that also covers the image is inert there.
        val overImage = RecordingLink()
        val boxStart = text.getSpanStart(box)
        onMain { text.setSpan(overImage, boxStart - 3, boxStart + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
        host.show(text)

        listen()
        tapOn(box)

        assertEquals(listOf<Any>(box), clicked)
        assertEquals(0, overImage.clicks)
        assertEquals(0, link.clicks)
    }

    @Test
    fun anImageWithoutAListenerIsInert() {
        showLinkAndBox()

        tapOn(box)

        assertEquals(0, link.clicks)
    }

    @Test
    fun anImageIsNotHighlighted() {
        showLinkAndBox()

        val (x, y) = centerOf(box)
        host.touch(MotionEvent.ACTION_DOWN, x, y)
        val bitmap = host.render()
        host.touch(MotionEvent.ACTION_CANCEL, x, y)

        // Still the green of the box, untouched by the highlight.
        assertEquals(Color.GREEN, bitmap.getPixel(x.toInt(), y.toInt()))
        assertEquals(0, countHighlighted(bitmap, onMain { host.view.getSpanBounds(box) }!!))
    }

    // The pressed state.

    private fun countHighlighted(bitmap: Bitmap, rect: Rect): Int {
        var count = 0

        for (y in maxOf(rect.top, 0) until minOf(rect.bottom, bitmap.height)) {
            for (x in maxOf(rect.left, 0) until minOf(rect.right, bitmap.width)) {
                val pixel = bitmap.getPixel(x, y)
                if (Color.red(pixel) > 240 && Color.green(pixel) in 110..145 && Color.blue(pixel) in 110..145) {
                    count++
                }
            }
        }

        return count
    }

    private fun countHighlighted(bitmap: Bitmap, rect: RectF) = countHighlighted(
        bitmap, Rect(rect.left.toInt(), rect.top.toInt(), rect.right.toInt() + 1, rect.bottom.toInt() + 1)
    )

    /** Whether the first rect of [span] is filled with the highlight, drawn under the glyphs. */
    private fun isHighlighted(span: Any): Boolean {
        val rect = onMain { host.view.getSpanRects(span) }[0]
        return countHighlighted(host.render(), rect) > rect.width() * rect.height() / 4
    }

    @Test
    fun aPressedLinkIsHighlightedBehindTheText() {
        showLinkAndBox()

        assertFalse(isHighlighted(link))

        val (x, y) = centerOf(link)
        host.touch(MotionEvent.ACTION_DOWN, x, y)
        assertTrue(isHighlighted(link))

        // Under the text: there are still black glyph pixels inside the rect.
        val rect = onMain { host.view.getSpanRects(link) }[0]
        val bitmap = host.render()
        var dark = 0
        for (py in rect.top.toInt() until rect.bottom.toInt()) {
            for (px in rect.left.toInt() until rect.right.toInt()) {
                if (Color.red(bitmap.getPixel(px, py)) < 60) dark++
            }
        }
        assertTrue(dark > 50)

        host.touch(MotionEvent.ACTION_UP, x, y)
        assertFalse(isHighlighted(link))
    }

    @Test
    fun theHighlightFollowsTheScrollAndTheOffsetOfTheText() {
        onMain { host.view.setPadding(40, 90, 40, 30) }
        showLinkAndBox()

        // Scroll a little, so that the link, which is on the first line, is still visible.
        host.scrollTo(10)
        val (x, y) = centerOf(link)
        host.touch(MotionEvent.ACTION_DOWN, x, y)

        assertTrue(isHighlighted(link))

        host.touch(MotionEvent.ACTION_CANCEL, x, y)
    }

    @Test
    fun aLinkThatWrapsIsHighlightedOnBothLines() {
        val plain = spannedOf { append(arabicText(20)) }
        host.show(plain)

        // A link from the middle of a line to the middle of the next one.
        val first = host.lines[2]
        val second = host.lines[3]
        val wrapping = RecordingLink()
        val start = (first.charStart + first.charEnd) / 2
        val end = (second.charStart + second.charEnd) / 2

        host.show(spannedOf {
            append(arabicText(20))
            setSpan(wrapping, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        })

        val rects = onMain { host.view.getSpanRects(wrapping) }
        assertEquals(2, rects.size)

        // Consecutive lines, right to left: the first one goes on to the left edge of the
        // text, the second one starts from the right edge.
        assertEquals(rects[0].bottom, rects[1].top, 1.0f)
        val (containerX, _) = host.containerOrigin()
        assertEquals(containerX.toFloat(), rects[0].left, 1.0f)
        assertEquals((containerX + host.container.width).toFloat(), rects[1].right, 1.0f)
        assertTrue(rects[0].right > rects[0].left + 10)
        assertTrue(rects[1].right > rects[1].left + 10)

        // Both are hit.
        host.tap(rects[1].centerX(), rects[1].centerY())
        assertEquals(1, wrapping.clicks)
        host.tap(rects[0].centerX(), rects[0].centerY())
        assertEquals(2, wrapping.clicks)

        // Both are highlighted while pressed.
        host.touch(MotionEvent.ACTION_DOWN, rects[0].centerX(), rects[0].centerY())
        val bitmap = host.render()
        for (rect in rects) {
            assertTrue("not highlighted at $rect", countHighlighted(bitmap, rect) > rect.width() * rect.height() / 4)
        }
        host.touch(MotionEvent.ACTION_CANCEL, rects[0].centerX(), rects[0].centerY())
    }

    // Geometry.

    private fun inkBounds(bitmap: Bitmap, matches: (Int) -> Boolean): Rect? {
        val bounds = Rect(Int.MAX_VALUE, Int.MAX_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if (matches(bitmap.getPixel(x, y))) {
                    bounds.left = minOf(bounds.left, x)
                    bounds.top = minOf(bounds.top, y)
                    bounds.right = maxOf(bounds.right, x + 1)
                    bounds.bottom = maxOf(bounds.bottom, y + 1)
                }
            }
        }

        return if (bounds.isEmpty) null else bounds
    }

    private fun isRed(pixel: Int) = Color.red(pixel) > 200 && Color.green(pixel) < 80 && Color.blue(pixel) < 80

    @Test
    fun spanBoundsMatchTheDrawnGlyphs() {
        onMain { host.view.setPadding(40, 90, 40, 30) }

        val red = ForegroundColorSpan(Color.RED)
        val marked = RecordingLink()
        host.show(spannedOf {
            append(arabicText(6))
            setSpan(red, 12, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(marked, 12, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        })

        for (offset in listOf(0, 25)) {
            host.scrollTo(offset)

            val bounds = onMain { host.view.getSpanBounds(marked) }!!
            val ink = inkBounds(host.render(), ::isRed)!!

            // The glyphs are inside the span, which may be a little wider than the ink (side
            // bearings) and is as tall as the line.
            assertTrue("$ink is not inside $bounds", Rect(bounds).apply { inset(-2, -2) }.contains(ink))
            assertTrue("$ink is much narrower than $bounds", ink.width() * 100 >= bounds.width() * 75)
            assertTrue("$ink is much shorter than $bounds", ink.height() * 100 >= bounds.height() * 30)

            // The same rect is what the one rect of the span is.
            val rects = onMain { host.view.getSpanRects(marked) }
            assertEquals(1, rects.size)
            assertEquals(bounds.left.toFloat(), rects[0].left, 1.0f)
            assertEquals(bounds.bottom.toFloat(), rects[0].bottom, 1.0f)
        }
    }

    @Test
    fun spanBoundsAreInViewCoordinates() {
        showLinkAndBox()

        val at0 = onMain { host.view.getSpanBounds(link) }!!

        host.scrollTo(37)
        val at37 = onMain { host.view.getSpanBounds(link) }!!
        assertEquals(at0.top - 37, at37.top)
        assertEquals(at0.height(), at37.height())
        assertEquals(at0.left, at37.left)

        // The text is right to left and sits against the right edge, which moves in by the
        // padding; what is before the link is the same.
        val padded = TextViewHost()
        onMain { padded.view.setPadding(40, 90, 40, 30) }
        padded.show(spannedOf {
            append(arabicText(6))
            setSpan(link, 12, 20, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        })

        val inPadding = onMain { padded.view.getSpanBounds(link) }!!
        assertEquals(at0.top + 90, inPadding.top)
        assertEquals(at0.left - 40, inPadding.left)
        assertEquals(at0.width(), inPadding.width())
    }

    @Test
    fun spanBoundsOfAnImageAreTheImage() {
        showLinkAndBox()

        val bounds = onMain { host.view.getSpanBounds(box) }!!
        assertEquals(200, bounds.width())
        assertEquals(100, bounds.height())

        // Right where the green is, give or take a pixel of anti-aliasing at an edge.
        val ink = inkBounds(host.render()) { it == Color.GREEN }!!
        assertTrue("$ink is not inside $bounds", Rect(bounds).apply { inset(-1, -1) }.contains(ink))
        assertTrue("$ink is not filling $bounds", ink.width() >= bounds.width() - 2 && ink.height() >= bounds.height() - 2)

        // The middle of the box is on the box.
        val (x, y) = centerOf(box)
        assertEquals(box, boxSpanAt(x, y))
    }

    private fun boxSpanAt(x: Float, y: Float): Any? {
        val offset = onMain { host.view.getCharIndexForPosition(x, y) }
        val text = onMain { host.view.spanned }!!

        return text.getSpans(offset, offset, BoxSpan::class.java).firstOrNull()
    }

    @Test
    fun spanBoundsAreNullWhenThereIsNothingToShow() {
        val stranger = RecordingLink()
        assertNull(onMain { host.view.getSpanBounds(stranger) })
        assertTrue(onMain { host.view.getSpanRects(stranger) }.isEmpty())

        showLinkAndBox()
        assertNull(onMain { host.view.getSpanBounds(stranger) })
        assertNotNull(onMain { host.view.getSpanBounds(link) })
    }

    @Test
    fun spansOnLeftToRightTextWork() {
        val latin = RecordingLink()
        host.show(spannedOf {
            append("abc def ghi jkl mno pqr stu vwx yz ".repeat(10))
            setSpan(latin, 8, 15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        })

        val rects = onMain { host.view.getSpanRects(latin) }
        assertEquals(1, rects.size)

        host.tap(rects[0].centerX(), rects[0].centerY())
        assertEquals(1, latin.clicks)

        host.tap(rects[0].right + 40, rects[0].centerY())
        assertEquals(1, latin.clicks)
    }
}
