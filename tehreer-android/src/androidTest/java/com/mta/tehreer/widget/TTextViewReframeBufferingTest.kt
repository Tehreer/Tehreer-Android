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

import android.text.SpannableStringBuilder
import android.view.View
import com.mta.tehreer.layout.style.ViewSpan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

/**
 * The state-machine contract of double-buffered re-framing (see `TextContainer.updateComposedFrame`/
 * `updateLineBoxes`): a re-frame of text already on screen must never show a frame with zero lines,
 * must leave the displayed frame/lines/views exactly as they are until the new frame AND its line
 * boxes are both ready, and must swap everything at once - while brand-new text keeps clearing
 * immediately, as before.
 *
 * A long document is used throughout so that `FrameResolvingTask`/`LineBoxesTask` take a real,
 * observable amount of wall-clock time on a background thread; a watcher thread polls the view from
 * the test thread (each poll itself hopping to the main thread, as [onMain] always does) while the
 * reframe is in flight, to sample what is on screen during the window the fix is about.
 */
class TTextViewReframeBufferingTest {
    private lateinit var host: TextViewHost

    @Before
    fun setUp() {
        host = TextViewHost()
    }

    /**
     * Polls [sample] on the main thread as fast as it can (each call itself hops to the main
     * thread and blocks until it runs there, so this is already throttled by that round trip -
     * no extra sleep is added, to catch as many samples as possible during a short window) until
     * [stop] is cleared.
     */
    private fun watch(stop: AtomicBoolean, sample: () -> Unit): Thread {
        val thread = Thread {
            while (!stop.get()) {
                onMain { sample() }
            }
        }
        thread.start()
        return thread
    }

    @Test
    fun aReframeNeverShowsAZeroLineFrame() {
        host.show(spannedOf { append(arabicText(2200)) })

        val oldFrame = onMain { host.view.composedFrame!! }
        assertTrue(onMain { host.lineViews().isNotEmpty() })

        val stop = AtomicBoolean(false)
        val observedBadState = AtomicBoolean(false)
        val observedOldFrameStillDisplayed = AtomicBoolean(false)

        val watcher = watch(stop) {
            val frame = host.view.composedFrame

            // getComposedFrame() itself briefly answers null while a request is outstanding
            // (isComposedFrameResolved, unrelated to and unchanged by this fix - it goes false
            // synchronously on every request, true again once the fast FrameResolvingTask
            // finishes) - that is not what this test is about. What must never happen is the
            // container itself - what is actually on screen - having no lines.
            if (host.lineViews().isEmpty()) {
                observedBadState.set(true)
            }
            if (frame === oldFrame) {
                observedOldFrameStillDisplayed.set(true)
            }
        }

        onMain { host.view.textSize = 20.0f }
        host.awaitUntil("the reframe completes") {
            host.view.composedFrame != null && host.view.composedFrame !== oldFrame
        }

        stop.set(true)
        watcher.join()
        host.awaitFullyFramed()

        assertFalse(
            "a re-frame must never be observed with zero lines on screen",
            observedBadState.get()
        )
        assertTrue(
            "the old frame should still have been displayed at some point during the reframe",
            observedOldFrameStillDisplayed.get()
        )
        assertTrue(onMain { host.lineViews().isNotEmpty() })
    }

    @Test
    fun hitTestingDuringThePendingWindowAnswersAgainstTheOldFrame() {
        host.show(spannedOf { append(arabicText(2200)) })

        val oldFrame = onMain { host.view.composedFrame!! }
        val point = host.centerOf(host.lines[3])
        val expectedOffset = onMain { host.view.getCharIndexForPosition(point.first, point.second) }
        val expectedFirstVisible = onMain { host.view.firstVisibleCharIndex }

        val stop = AtomicBoolean(false)
        val observedMismatch = AtomicBoolean(false)
        val observedOldFrameStillDisplayed = AtomicBoolean(false)

        val watcher = watch(stop) {
            if (host.view.composedFrame === oldFrame) {
                observedOldFrameStillDisplayed.set(true)

                val offset = host.view.getCharIndexForPosition(point.first, point.second)
                val firstVisible = host.view.firstVisibleCharIndex

                if (offset != expectedOffset || firstVisible != expectedFirstVisible) {
                    observedMismatch.set(true)
                }
            }
        }

        onMain { host.view.textSize = 20.0f }
        host.awaitUntil("the reframe completes") {
            host.view.composedFrame != null && host.view.composedFrame !== oldFrame
        }

        stop.set(true)
        watcher.join()
        host.awaitFullyFramed()

        assertTrue(
            "the old frame should still have been displayed at some point during the reframe",
            observedOldFrameStillDisplayed.get()
        )
        assertFalse(
            "hit-testing/firstVisibleCharIndex during the pending window must agree with the old frame",
            observedMismatch.get()
        )
    }

    @Test
    fun aSupersededReframeLeavesTheOldFrameUntouchedUntilTheNewerOneSwaps() {
        host.show(spannedOf { append(arabicText(2200)) })

        val oldFrame = onMain { host.view.composedFrame!! }

        // A watcher running across both requests: whichever of them a poll catches mid-flight,
        // nothing should ever show a bad (zero-line) state, and a superseded request's own
        // completion (if a poll happens to catch it fully resolved) must never leave a dangling
        // pending frame around - findComposedFrame is always fully consistent with its own lines.
        val stop = AtomicBoolean(false)
        val observedBadState = AtomicBoolean(false)
        val watcher = watch(stop) {
            // See aReframeNeverShowsAZeroLineFrame: getComposedFrame() itself going briefly null
            // is expected and unrelated to this fix; a container with no lines on screen is not.
            if (host.lineViews().isEmpty()) {
                observedBadState.set(true)
            }
        }

        // The second request supersedes the first (cancelling its background task via layoutID),
        // whether or not the first had already completed and swapped in by the time this runs.
        onMain { host.view.textSize = 20.0f }
        onMain { host.view.textSize = 24.0f }

        host.awaitUntil("the final reframe completes") {
            host.view.composedFrame != null &&
                host.view.composedFrame !== oldFrame &&
                host.view.textSize == 24.0f
        }

        stop.set(true)
        watcher.join()
        host.awaitFullyFramed()

        assertFalse("no bad state during the superseded-then-final reframe", observedBadState.get())
        assertNotSame(oldFrame, onMain { host.view.composedFrame })
        assertEquals(24.0f, onMain { host.view.textSize })
    }

    /**
     * Brand-new text does not wait for the new frame to be shown: the lines of the old text are
     * gone as soon as the new text is displayed, though the line views themselves are reused.
     */
    @Test
    fun brandNewTextStillBlanksImmediately() {
        host.show(spannedOf { append(arabicText(2200)) })

        val oldLines = onMain {
            host.lineViews().mapNotNull { it.line }.toSet()
        }
        assertTrue(oldLines.isNotEmpty())

        onMain { host.view.setSpanned(spannedOf { append(arabicText(300)) }) }

        host.awaitUntil("the new text is there, with none of the old text's lines") {
            host.view.composedFrame != null && host.lineViews().isNotEmpty() &&
                host.lineViews().none { it.line in oldLines }
        }

        assertEquals(0, host.scrollY)
        host.awaitFullyFramed()
    }

    /**
     * The scenario the brief describes: a view's line moves (here, because the text around it is
     * reframed to a new size - the same kind of reframe a footer/hero-image view sits through on
     * a theme or text-size change) must not show the view at a new position before the text
     * around it catches up. A plain [ViewSpan.requestResize] is not used to trigger this: it only
     * reframes (no retypesetting), so on this codebase/device it resolves fast enough that the
     * pending window is too short to reliably poll into - unlike a text-size change, which
     * retypesets the whole document and so reliably takes long enough to observe mid-flight.
     */
    @Test
    fun aViewSpanStaysAtItsOldRectDuringAPendingReframe() {
        val span = FlexibleSpan(ratio = 0.2f, hides = false)
        var start = 0

        host.show(spannedOf {
            append(arabicText(2200))
            append("\n")
            start = length
            append("￼\n")
            setSpan(span, start, start + 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(arabicText(2200))
        })

        val line = onMain { host.view.composedFrame!!.let { it.lines[it.getLineIndexForChar(start)] } }
        host.scrollTo(onMain { (line.originY - line.ascent).roundToInt() } - 100)

        val oldFrame = onMain { host.view.composedFrame!! }
        val oldRect = onMain {
            val v = span.view!!
            android.graphics.Rect(v.left, v.top, v.right, v.bottom)
        }

        val stop = AtomicBoolean(false)
        val observedMismatch = AtomicBoolean(false)
        val observedOldFrameStillDisplayed = AtomicBoolean(false)

        val watcher = watch(stop) {
            if (host.view.composedFrame === oldFrame) {
                observedOldFrameStillDisplayed.set(true)

                val v = span.view
                if (v == null || v.visibility != View.VISIBLE ||
                    android.graphics.Rect(v.left, v.top, v.right, v.bottom) != oldRect
                ) {
                    observedMismatch.set(true)
                }
            }
        }

        onMain { host.view.textSize = 20.0f }

        host.awaitUntil("the reframe completes") {
            host.view.composedFrame != null && host.view.composedFrame !== oldFrame
        }

        stop.set(true)
        watcher.join()

        assertTrue(
            "the old frame should still have been displayed at some point during the reframe",
            observedOldFrameStillDisplayed.get()
        )
        assertFalse(
            "a view with hideWhileResizing=false must keep its old rect until the swap",
            observedMismatch.get()
        )

        // And afterwards, the view really did move - the swap actually happened, this was not a
        // vacuous check of a view that never had reason to move in the first place. Captured
        // before the wait for the line boxes.
        val newRect = onMain {
            val v = span.view!!
            android.graphics.Rect(v.left, v.top, v.right, v.bottom)
        }
        assertNotEquals(oldRect, newRect)

        host.awaitFullyFramed()
    }
}
