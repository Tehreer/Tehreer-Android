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
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.Size
import android.view.View
import com.mta.tehreer.layout.ComposedLine
import com.mta.tehreer.layout.style.ViewSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A view whose height is a fraction of its width, or whatever [contentHeight] is if that is set,
 * as that of a view with wrapping text or a card that is filled later.
 */
internal class FlexibleView(context: Context) : View(context) {
    var ratio = 0.5f

    private var wantedHeight = -1

    var contentHeight: Int
        get() = wantedHeight
        set(value) {
            wantedHeight = value
            requestLayout()
        }

    /** Changes what the view wants without asking for a layout, as a ComposeView does. */
    fun setContentHeightSilently(value: Int) {
        wantedHeight = value
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val wanted = if (wantedHeight >= 0) wantedHeight else (width * ratio).roundToInt()

        val height = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
            MeasureSpec.getSize(heightMeasureSpec)
        } else {
            wanted
        }

        setMeasuredDimension(width, height)
    }
}

/** A span that leaves its height to a [FlexibleView] and counts what it is asked. */
internal class FlexibleSpan(
    private val kind: Placement = Placement.BLOCK,
    private val roomWidth: Int = 0,
    private val roomMargins: Rect = Rect(),
    private val retain: Boolean = false,
    private val ratio: Float = 0.5f,
    private val hides: Boolean = true
) : ViewSpan() {
    val views = mutableListOf<FlexibleView>()
    val measureCalls = AtomicInteger()

    @Volatile
    var contentHeight = -1

    override val placement: Placement get() = kind
    override val width: Int get() = roomWidth
    override val margins: Rect get() = Rect(roomMargins)
    override val retainWhenOffscreen: Boolean get() = retain
    override val hideWhileResizing: Boolean get() = hides

    override fun createView(context: Context): View =
        FlexibleView(context).also {
            it.ratio = ratio
            it.contentHeight = contentHeight
            views.add(it)
        }

    override fun onMeasure(layoutWidth: Int, view: View): Size {
        measureCalls.incrementAndGet()
        return super.onMeasure(layoutWidth, view)
    }
}

/** The room of a view decided when the frame is made, and what the reader looks at. */
class TTextViewFrameTimeTest {
    private lateinit var host: TextViewHost

    @Before
    fun setUp() {
        host = TextViewHost()
    }

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

    private fun rectOf(span: ViewSpan): Rect = onMain {
        val view = span.view!!
        Rect(view.left, view.top, view.right, view.bottom)
    }

    /** Where the top of the line with [charIndex] is on the screen, whatever the scroll. */
    private fun screenTopOf(charIndex: Int): Float = onMain {
        val frame = host.view.composedFrame!!
        val line = frame.lines[frame.getLineIndexForChar(charIndex)]
        val (_, originY) = host.containerOrigin()

        originY + frame.originY + line.originY - line.ascent
    }

    /** A text with a view in the middle of a long one. */
    private fun showViewInLongText(span: ViewSpan): Pair<Int, Int> {
        var start = 0

        host.show(spannedOf {
            append(arabicText(40))
            append("\n")
            start = appendView(span)
            append(arabicText(40))
        })

        return Pair(start, onMain { host.view.composedFrame!!.charEnd })
    }

    // Views that measure themselves.

    @Test
    fun theFirstFrameHasTheHeightThatTheViewNeedsForTheWidth() {
        val span = FlexibleSpan(ratio = 0.5f, roomMargins = Rect(0, 12, 0, 8))
        var start = 0

        onMain { host.view.spanned = spannedOf {
            append(arabicText(6))
            append("\n")
            start = appendView(span)
            append(arabicText(6))
        } }

        // The very first frame that appears is right: the view is half as tall as the text is wide.
        host.awaitUntil("the first frame") { host.view.composedFrame != null }

        val line = lineOf(start)
        assertEquals(300f + 20f, line.height, 0.001f)
        assertEquals(300, rectOf(span).height())

        // It was made once, for the measuring, and it is the view that is shown.
        assertEquals(1, span.views.size)
        assertSame(span.views[0], span.view)
        assertEquals(1, span.measureCalls.get())
    }

    @Test
    fun theHeightFollowsTheWidthWithoutTypesettingAgain() {
        val span = FlexibleSpan(ratio = 0.5f)
        var start = 0

        host.show(spannedOf {
            append(arabicText(6))
            append("\n")
            start = appendView(span)
            append(arabicText(6))
        })

        val typesetter = onMain { host.view.typesetter }
        val oldFrame = onMain { host.view.composedFrame!! }
        val oldBlock = oldFrame.lines.first { it.charStart == start }
        assertEquals(300, rectOf(span).height())
        assertEquals(600f, oldBlock.runs[0].width, 0.001f)

        onMain { host.view.setPadding(30, 0, 30, 0) }
        host.awaitUntil("the text is framed for the padding") { host.view.composedFrame?.width == 540f }

        // The width is 540, so is the view, and it is a quarter of it that it is tall.
        assertEquals(270, rectOf(span).height())
        assertEquals(540, rectOf(span).width())
        assertEquals(270f, lineOf(start).height, 0.001f)
        assertEquals(540f, lineOf(start).runs[0].width, 0.001f)

        // The same typesetter served both, and the old frame was not touched by the new one.
        assertSame(typesetter, onMain { host.view.typesetter })
        assertEquals(600f, oldBlock.runs[0].width, 0.001f)
        assertEquals(300f, oldBlock.height, 0.001f)

        // The view is the same one.
        assertEquals(1, span.views.size)
    }

    @Test
    fun anInlineViewIsMeasuredToo() {
        val span = FlexibleSpan(ViewSpan.Placement.INLINE, roomWidth = 80, ratio = 0.5f)
        var start = 0

        host.show(spannedOf {
            append("Hello there. ")
            start = appendView(span, inline = true)
            append(" Hello there again.")
            append("\n")
            append(arabicText(20))
        })

        // As wide as it was told to be, and half as tall as that.
        val rect = rectOf(span)
        assertEquals(80, rect.width())
        assertEquals(40, rect.height())
        assertEquals(lineOf(start).originY.roundToInt(), rect.bottom)
    }

    @Test
    fun aBlockIsOnALineOfItsOwnInTheMiddleOfAParagraph() {
        val span = FlexibleSpan(ratio = 0.1f)
        var start = 0

        host.show(spannedOf {
            append(arabicText(2))
            start = length
            append("￼")
            setSpan(span, start, start + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(arabicText(2))
        })

        val line = lineOf(start)
        assertEquals(start, line.charStart)
        assertEquals(start + 1, line.charEnd)
        assertEquals(60f, line.height, 0.001f)
        assertEquals(600f, line.runs[0].width, 0.001f)

        // The text on both sides has lines, which are not the line of the view.
        assertTrue(lineOf(start - 1).charEnd == start)
        assertTrue(lineOf(start + 1).charStart == start + 1)
    }

    @Test
    fun theLineOfAViewIsNotStretchedByTheSpacingOfTheText() {
        val span = FlexibleSpan(ratio = 0.1f, roomMargins = Rect(0, 5, 0, 5))
        var start = 0

        host.show(spannedOf {
            append(arabicText(6))
            append("\n")
            start = appendView(span)
            append(arabicText(6))
        })

        val textHeight = lineOf(0).height

        onMain {
            host.view.lineHeightMultiplier = 2.0f
            host.view.extraLineSpacing = 10.0f
            host.view.isJustificationEnabled = true
        }
        host.awaitUntil("the lines are spaced") { (host.view.composedFrame?.lines?.get(0)?.height ?: 0f) > textHeight * 1.5f }

        assertEquals(70f, lineOf(start).height, 0.001f)
        assertEquals(60, rectOf(span).height())
    }

    // Views that only know their size after they are attached.

    @Test
    fun aViewThatGrowsMovesTheTextBelowAndNotTheTextThatIsOnScreen() {
        val span = FlexibleSpan(ratio = 0.2f)
        val (start, _) = showViewInLongText(span)

        // Look at the text just above the view, which is the only thing that has to stay.
        val anchorChar = start - 30
        val line = lineOf(anchorChar)
        val lineTop = onMain { line.originY - line.ascent }
        host.scrollTo((lineTop - 100).roundToInt())

        val firstVisible = onMain { host.view.firstVisibleCharIndex }
        val screenTop = screenTopOf(anchorChar)
        val below = lineOf(start + 10)
        val belowTop = onMain { below.originY - below.ascent }
        val height = rectOf(span).height()
        assertEquals(120, height)

        // The view finds out that it is taller, on another thread, and says so several times.
        val calls = span.measureCalls.get()
        val typesetter = onMain { host.view.typesetter }
        span.contentHeight = 400
        onMain { (span.view as FlexibleView).contentHeight = 400 }

        Thread { repeat(5) { span.requestResize() } }.start()
        host.awaitUntil("the resize is seen") { span.view!!.visibility == View.INVISIBLE }

        // It is kept out of sight until the frame that has its height is in place.
        assertEquals(View.INVISIBLE, onMain { span.view!!.visibility })

        host.awaitUntil("the frame has the new height") {
            span.view?.visibility == View.VISIBLE && rectOf(span).height() == 400
        }

        assertEquals(400, rectOf(span).height())
        assertEquals(400f, lineOf(start).height, 0.001f)

        // The line below moved by the growth, the ones above did not...
        val newBelow = lineOf(start + 10)
        assertEquals(belowTop + 280f, newBelow.originY - newBelow.ascent, 0.01f)
        assertEquals(lineTop, lineOf(anchorChar).originY - lineOf(anchorChar).ascent, 0.01f)

        // ...and what the reader looks at is where it was.
        assertEquals(firstVisible, onMain { host.view.firstVisibleCharIndex })
        assertEquals(screenTop, screenTopOf(anchorChar), 1.0f)

        // The text was not typeset again.
        assertSame(typesetter, onMain { host.view.typesetter })

        // Many requests are few measurements, and no new view.
        assertTrue("${span.measureCalls.get() - calls} measurements", span.measureCalls.get() - calls <= 3)
        assertEquals(1, span.views.size)
    }

    @Test
    fun aViewThatGrowsAboveTheScreenDoesNotMoveTheScreen() {
        val span = FlexibleSpan(ratio = 0.2f)
        val (start, end) = showViewInLongText(span)

        // The reader is well below the view, which is not attached.
        val target = lineOf(end - 200)
        host.scrollTo(onMain { (target.originY - target.ascent).roundToInt() } - 50)
        val screenTop = screenTopOf(end - 200)
        val firstVisible = onMain { host.view.firstVisibleCharIndex }
        assertTrue(firstVisible > start)
        assertEquals(null, span.view)

        // The view that was made to be measured tells that it needs more room.
        onMain { span.views[0].contentHeight = 500 }
        span.requestResize()
        host.awaitUntil("the frame has the new height") {
            host.view.composedFrame?.lines?.any { it.charStart == start && it.height == 500f } == true
        }

        assertEquals(screenTop, screenTopOf(end - 200), 1.0f)
        assertEquals(firstVisible, onMain { host.view.firstVisibleCharIndex })
        assertEquals(1, span.views.size)
    }

    // Scroll anchoring.

    @Test
    fun aChangeOfWidthKeepsWhatIsOnScreen() {
        val span = FlexibleSpan(ratio = 0.2f)
        val (start, end) = showViewInLongText(span)

        val anchor = end - 300
        val line = lineOf(anchor)
        host.scrollTo(onMain { (line.originY - line.ascent).roundToInt() } - 10)

        val firstVisible = onMain { host.view.firstVisibleCharIndex }
        val before = screenTopOf(firstVisible)
        assertTrue(firstVisible > start)

        // Rotation is one of those: the text gets narrower and the lines are broken again.
        onMain { host.view.setPadding(70, 0, 70, 0) }
        host.awaitUntil("the text is framed for the padding") { host.view.composedFrame?.width == 460f }

        // The line that has the character is where the line was, whether the character begins
        // it or not.
        val newLine = lineOf(firstVisible)
        assertTrue(newLine.charStart <= firstVisible && firstVisible < newLine.charEnd)
        assertEquals(before, screenTopOf(firstVisible), 1.0f)
        assertTrue(host.scrollY > 0)

        // And back. The anchor is a character, so where the lines begin can make it a line off.
        onMain { host.view.setPadding(0, 0, 0, 0) }
        host.awaitUntil("the text is framed again") { host.view.composedFrame?.width == 600f }
        assertEquals(before, screenTopOf(firstVisible), lineOf(firstVisible).height)
    }

    @Test
    fun aChangeOfTheSizeOfTheTextKeepsWhatIsOnScreen() {
        host.show(spannedOf { append(arabicText(70)) })

        val target = lineOf(onMain { host.view.composedFrame!!.charEnd } / 3)
        host.scrollTo(onMain { (target.originY - target.ascent).roundToInt() })

        val firstVisible = onMain { host.view.firstVisibleCharIndex }
        val before = screenTopOf(firstVisible)

        onMain { host.view.textSize = 24.0f }
        host.awaitUntil("the text is smaller") { host.view.composedFrame?.lines?.get(0)?.height ?: 100f < target.height }

        val newLine = lineOf(firstVisible)
        assertTrue(newLine.charStart <= firstVisible && firstVisible < newLine.charEnd)
        assertEquals(before, screenTopOf(firstVisible), 1.0f)
    }

    @Test
    fun aChangeOfTheLineSpacingKeepsWhatIsOnScreen() {
        host.show(spannedOf { append(arabicText(70)) })

        val target = lineOf(onMain { host.view.composedFrame!!.charEnd } / 3)
        host.scrollTo(onMain { (target.originY - target.ascent).roundToInt() } - 30)

        val firstVisible = onMain { host.view.firstVisibleCharIndex }
        val before = screenTopOf(firstVisible)

        onMain { host.view.extraLineSpacing = 20.0f }
        host.awaitUntil("the lines are spaced") { (host.view.composedFrame?.lines?.get(0)?.leading ?: 0f) >= 20f }

        assertEquals(before, screenTopOf(firstVisible), 1.0f)
        assertEquals(firstVisible, onMain { host.view.firstVisibleCharIndex })

        onMain { host.view.isJustificationEnabled = true }
        host.awaitUntil("the lines are justified") { host.view.composedFrame != null }
        assertEquals(before, screenTopOf(firstVisible), 1.0f)
    }

    @Test
    fun aNewTextStartsAtTheTop() {
        host.show(spannedOf { append(arabicText(70)) })
        host.scrollTo(400)
        assertEquals(400, host.scrollY)

        host.show(spannedOf { append(arabicText(60)) })
        assertEquals(0, host.scrollY)
        assertEquals(0, onMain { host.view.firstVisibleCharIndex })

        host.scrollTo(300)
        onMain { host.view.text = arabicText(50) }
        host.awaitUntil("the text is new") { host.view.composedFrame?.charEnd == arabicText(50).length }
        assertEquals(0, host.scrollY)

        host.scrollTo(300)
        onMain { host.view.typesetter = com.mta.tehreer.layout.Typesetter(arabicText(55), host.view.typeface!!, 32f) }
        host.awaitUntil("the typesetter is new") { host.view.composedFrame?.charEnd == arabicText(55).length }
        assertEquals(0, host.scrollY)
    }

    // Restoring the place.

    @Test
    fun scrollToCharPutsItsLineAtTheTop() {
        host.show(spannedOf { append(arabicText(70)) })
        val end = onMain { host.view.composedFrame!!.charEnd }

        for (char in listOf(end / 3, end / 2, 5, end / 3)) {
            onMain { host.view.scrollToCharIndex(char, false) }

            val line = lineOf(char)
            assertEquals(0f, screenTopOf(char), 1.0f)
            assertTrue(line.charStart <= char && char < line.charEnd)
            assertTrue(onMain { host.view.firstVisibleCharIndex } <= char)
        }

        onMain { host.view.scrollToCharIndex(0, false) }
        assertEquals(0, host.scrollY)
    }

    @Test
    fun scrollToCharTakesPaddingIntoAccount() {
        onMain { host.view.setPadding(0, 60, 0, 0) }
        host.show(spannedOf { append(arabicText(70)) })

        val char = onMain { host.view.composedFrame!!.charEnd } / 2
        onMain { host.view.scrollToCharIndex(char, false) }
        assertEquals(60f, screenTopOf(char), 1.0f)

        onMain { host.view.scrollToCharIndex(0, false) }
        assertEquals(0, host.scrollY)
    }

    @Test
    fun scrollingToTheFirstVisibleCharIndexStaysWhereItIs() {
        // The text scrolls through the padding, so the line before the one that is at the top of the
        // text still shows in the padding; it must not count as the first visible one, or every
        // restore of the place would go a line back.
        onMain { host.view.setPadding(0, 60, 0, 0) }
        host.show(spannedOf { append(arabicText(70)) })

        val char = onMain { host.view.composedFrame!!.charEnd } / 2
        onMain { host.view.scrollToCharIndex(char, false) }

        val first = onMain { host.view.firstVisibleCharIndex }
        assertEquals(lineOf(char).charStart, first)

        val scrollY = host.scrollY
        repeat(3) {
            onMain { host.view.scrollToCharIndex(onMain { host.view.firstVisibleCharIndex }, false) }
            assertEquals(first, onMain { host.view.firstVisibleCharIndex })
            assertEquals(scrollY, host.scrollY)
        }
    }

    @Test
    fun scrollToCharBeforeTheTextIsThereWaitsForIt() {
        val restored = spannedOf { append(arabicText(70)) }
        val char = 700

        onMain {
            host.view.spanned = restored
            host.view.scrollToCharIndex(char, false)
        }
        host.awaitUntil("the text is there") { host.view.composedFrame != null && host.lineViews().isNotEmpty() }

        // Not the top that a new text gets.
        assertTrue(host.scrollY > 0)
        assertEquals(0f, screenTopOf(char), 1.0f)
        assertTrue(onMain { host.view.firstVisibleCharIndex } <= char)
    }

    @Test
    fun scrollToCharCanBeAnimated() {
        host.show(spannedOf { append(arabicText(70)) })
        val char = onMain { host.view.composedFrame!!.charEnd } / 2

        onMain { host.view.scrollToCharIndex(char, true) }

        // Nothing draws the view, so the animation is advanced by hand.
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (SystemClock.uptimeMillis() < deadline && abs(screenTopOf(char)) > 1.0f) {
            onMain { host.view.computeScroll() }
            Thread.sleep(16)
        }

        assertEquals(0f, screenTopOf(char), 1.0f)
    }

    @Test
    fun theFirstVisibleCharIndexIsMinusOneWithoutText() {
        assertEquals(-1, onMain { host.view.firstVisibleCharIndex })

        host.show(spannedOf { append(arabicText(3)) })
        assertEquals(0, onMain { host.view.firstVisibleCharIndex })
    }

    @Test
    fun aViewThatChangesWithoutAskingForALayoutIsMeasuredAnew() {
        val span = FlexibleSpan(ratio = 0.2f)
        val (start, _) = showViewInLongText(span)

        val line = lineOf(start)
        host.scrollTo(onMain { (line.originY - line.ascent).roundToInt() } - 100)
        val view = span.view as FlexibleView
        assertEquals(120, rectOf(span).height())

        // No request for a layout: the answers it gave before are what the cache of the view has.
        onMain { view.setContentHeightSilently(330) }
        span.requestResize()
        host.awaitUntil("the frame has the new height") { host.view.composedFrame?.lines?.any { it.charStart == start && it.height == 330f } == true }

        assertEquals(330, rectOf(span).height())
    }

    @Test
    fun aViewThatHasNoRoomYetIsAttachedAndCanGrow() {
        // What a view that only knows its size once it is attached looks like: nothing at first.
        val span = FlexibleSpan(ratio = 0.0f)
        val (start, _) = showViewInLongText(span)
        assertEquals(null, span.view)

        val line = lineOf(start)
        host.scrollTo(onMain { (line.originY - line.ascent).roundToInt() } - 100)

        val view = span.view as FlexibleView
        assertEquals(0, rectOf(span).height())

        // Attached, it can tell.
        onMain { view.contentHeight = 250 }
        span.requestResize()
        host.awaitUntil("the frame has the new height") { host.view.composedFrame?.lines?.any { it.charStart == start && it.height == 250f } == true }

        assertSame(view, span.view)
        assertEquals(250, rectOf(span).height())
    }

    @Test
    fun aViewThatWasMeasuredButIsFarAwayIsNotAttached() {
        val span = FlexibleSpan(ratio = 0.2f)
        val (start, _) = showViewInLongText(span)

        // It is far from the screen: it was made to be measured, and is not in the text.
        assertEquals(null, span.view)
        assertEquals(0, host.spanViews().size)
        assertEquals(1, span.views.size)

        // When it comes near, it is that view, and no other.
        val line = lineOf(start)
        host.scrollTo(onMain { (line.originY - line.ascent).roundToInt() } - 200)
        assertEquals(1, span.views.size)
        assertSame(span.views[0], span.view)
    }
}
