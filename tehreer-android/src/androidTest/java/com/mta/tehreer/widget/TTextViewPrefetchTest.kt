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
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import com.mta.tehreer.layout.ComposedLine
import com.mta.tehreer.layout.style.ViewSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.math.roundToInt

/** When the view of a span is made, and when it is hidden: the prefetch distance, and resizes. */
class TTextViewPrefetchTest {
    private lateinit var host: TextViewHost

    @Before
    fun setUp() {
        host = TextViewHost()
    }

    private fun SpannableStringBuilder.appendView(span: ViewSpan): Int {
        val start = length
        append("￼\n")
        setSpan(span, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return start
    }

    private fun lineOf(charIndex: Int): ComposedLine = onMain {
        val frame = host.view.composedFrame!!
        frame.lines[frame.getLineIndexForChar(charIndex)]
    }

    private fun rectOf(span: ViewSpan): Rect = onMain {
        val view = span.view!!
        Rect(view.left, view.top, view.right, view.bottom)
    }

    /** A view in the middle of a long text; returns where its character is. */
    private fun showViewInLongText(span: ViewSpan): Int {
        var start = 0

        host.show(spannedOf {
            append(arabicText(40))
            append("\n")
            start = appendView(span)
            append(arabicText(40))
        })

        return start
    }

    /** Scrolls so that the top of the line with [charIndex] is [distance] px below the top of the view. */
    private fun scrollSoThatLineIsAt(charIndex: Int, distance: Int) {
        val line = lineOf(charIndex)
        host.scrollTo(onMain { (line.originY - line.ascent).roundToInt() } - distance)
    }

    // Prefetch.

    @Test
    fun aViewFarBelowTheScreenIsNotMadeByDefault() {
        val span = FlexibleSpan(ratio = 0.2f)
        val start = showViewInLongText(span)

        assertEquals(0, onMain { host.view.viewSpanPrefetchDistance })

        // 300 px below the bottom of the screen.
        scrollSoThatLineIsAt(start, host.height + 300)

        assertNull(span.view)
        assertEquals(0, host.spanViews().size)
    }

    @Test
    fun aViewWithinThePrefetchDistanceIsAttachedBeforeItIsOnTheScreen() {
        val span = FlexibleSpan(ratio = 0.2f)
        val start = showViewInLongText(span)

        onMain { host.view.viewSpanPrefetchDistance = 500 }
        scrollSoThatLineIsAt(start, host.height + 300)

        // It is attached and laid out where it is, below the screen.
        val view = span.view
        assertNotNull(view)
        assertSame(host.container, view!!.parent)
        assertTrue(host.spanViews().contains(view))
        assertEquals(120, rectOf(span).height())

        val (_, originY) = host.containerOrigin()
        assertTrue(originY + rectOf(span).top >= host.height)
        assertEquals(1, span.views.size)

        // It is the same view, that was made to be measured, and stays while the reader comes near.
        scrollSoThatLineIsAt(start, host.height - 200)
        assertSame(view, span.view)
        assertEquals(1, span.views.size)
    }

    @Test
    fun aViewFurtherThanThePrefetchDistanceIsNotAttached() {
        val span = FlexibleSpan(ratio = 0.2f)
        val start = showViewInLongText(span)

        onMain { host.view.viewSpanPrefetchDistance = 200 }
        scrollSoThatLineIsAt(start, host.height + 300)
        assertNull(span.view)

        scrollSoThatLineIsAt(start, host.height + 150)
        assertNotNull(span.view)

        // Far above the screen it is let go of, as well.
        val line = lineOf(start)
        val bottom = onMain { (line.originY + line.descent + line.leading).roundToInt() }

        host.scrollTo(bottom + 150)
        assertNotNull(span.view)

        host.scrollTo(bottom + 300)
        assertNull(span.view)
    }

    @Test
    fun theDistanceIsAppliedToTheTextThatIsShown() {
        val span = FlexibleSpan(ratio = 0.2f)
        val start = showViewInLongText(span)

        scrollSoThatLineIsAt(start, host.height + 300)
        assertNull(span.view)

        onMain { host.view.viewSpanPrefetchDistance = 500 }
        assertNotNull(span.view)

        onMain { host.view.viewSpanPrefetchDistance = 0 }
        assertNull(span.view)

        // A negative distance is none.
        onMain { host.view.viewSpanPrefetchDistance = -40 }
        assertEquals(0, onMain { host.view.viewSpanPrefetchDistance })
    }

    // Resizes.

    /** The visibility of the view of [span] at every look, until the text has [height] at [start]. */
    private fun visibilitiesUntilResized(span: ViewSpan, start: Int, height: Int): List<Int> {
        val seen = mutableListOf<Int>()

        host.awaitUntil("the frame has the new height") {
            span.view?.let { seen.add(it.visibility) }
            host.view.composedFrame?.lines?.any { it.charStart == start && it.height == height.toFloat() } == true
        }

        span.view?.let { seen.add(it.visibility) }
        return seen
    }

    @Test
    fun aViewThatIsNotOnTheScreenIsNotHiddenWhileItResizes() {
        val span = FlexibleSpan(ratio = 0.2f)
        val start = showViewInLongText(span)

        onMain { host.view.viewSpanPrefetchDistance = 600 }
        scrollSoThatLineIsAt(start, host.height + 300)
        assertNotNull(span.view)

        onMain { (span.view as FlexibleView).contentHeight = 400 }
        span.contentHeight = 400
        span.requestResize()

        val seen = visibilitiesUntilResized(span, start, 400)

        assertFalse("seen $seen", seen.contains(View.INVISIBLE))
        assertEquals(400, rectOf(span).height())
        assertEquals(View.VISIBLE, onMain { span.view!!.visibility })
    }

    @Test
    fun aViewThatIsOnTheScreenIsHiddenWhileItResizesByDefault() {
        val span = FlexibleSpan(ratio = 0.2f)
        val start = showViewInLongText(span)

        scrollSoThatLineIsAt(start, 100)
        assertNotNull(span.view)

        onMain { (span.view as FlexibleView).contentHeight = 400 }
        span.contentHeight = 400
        span.requestResize()

        // It is out of sight until the frame that has the new height is in place, as it always was.
        host.awaitUntil("the resize is seen") { span.view!!.visibility == View.INVISIBLE }
        host.awaitUntil("the frame has the new height") {
            span.view?.visibility == View.VISIBLE && rectOf(span).height() == 400
        }

        assertEquals(400, rectOf(span).height())
    }

    @Test
    fun aSpanThatDoesNotHideKeepsItsViewInSightWhileItResizes() {
        val span = FlexibleSpan(ratio = 0.2f, hides = false)
        val start = showViewInLongText(span)

        scrollSoThatLineIsAt(start, 100)
        assertNotNull(span.view)

        onMain { (span.view as FlexibleView).contentHeight = 400 }
        span.contentHeight = 400
        span.requestResize()

        // The view stays visible, at the old room, until the frame gives it the new one.
        val seen = mutableListOf<Int>()
        host.awaitUntil("the frame has the new height") {
            span.view?.let { seen.add(it.visibility) }
            host.view.composedFrame?.lines?.any { it.charStart == start && it.height == 400f } == true &&
                span.view?.height == 400
        }

        assertFalse("seen $seen", seen.contains(View.INVISIBLE))
        assertEquals(View.VISIBLE, onMain { span.view!!.visibility })
        assertEquals(400, rectOf(span).height())
    }
}
