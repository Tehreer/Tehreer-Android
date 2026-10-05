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

import android.text.Spanned
import com.mta.tehreer.layout.Typesetter
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.layout.style.ViewSpan
import com.mta.tehreer.util.TypefaceStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The scope last round's double-buffered re-framing missed (see `TextContainer.updateComposedFrame`/
 * `updateLineBoxes`): FIRST LOAD of brand-new text, not just a re-frame of text already on screen.
 * Before this round's fix, `updateComposedFrame`'s `isTextNew` branch resolved `viewSlots` and
 * called `layoutLines()` (which shows the views via `layoutViews()`) immediately, well before
 * `displayedLineBoxes` had anything in it - so a `ViewSpan`'s view could appear on screen before
 * any text line did. The fix unifies first-load and re-frame through the same pending/swap
 * mechanism, so `updateLineBoxes` is the only place that ever resolves `viewSlots`/shows views, for
 * either kind of pending swap.
 *
 * A long document is used so `TypesettingTask`/`FrameResolvingTask`/`LineBoxesTask` take a real,
 * observable amount of wall-clock time on a background thread, giving a watcher thread (polling the
 * view from the test thread, each poll itself hopping to the main thread) a real window to catch a
 * bad intermediate state in, mirroring `TTextViewReframeBufferingTest`'s approach for re-frames.
 */
class TTextViewFirstLoadViewsTest {
    private lateinit var host: TextViewHost

    @Before
    fun setUp() {
        host = TextViewHost()
    }

    /**
     * Polls [sample] on the main thread as fast as it can (each call itself hops to the main
     * thread and blocks until it runs there - no extra sleep is added, to catch as many samples
     * as possible during a short window) until [stop] is cleared.
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

    /**
     * Watches [load] (which must kick off a brand-new text load on the main thread and return
     * immediately, without waiting for it) until the load completes, and asserts a [ViewSpan]'s
     * view is never attached while zero lines are placed - text and views must appear together,
     * or neither does yet, never "views but no text".
     */
    private fun assertNeverShowsViewsBeforeText(span: ViewSpan, load: () -> Unit) {
        val stop = AtomicBoolean(false)
        val observedBadState = AtomicBoolean(false)
        val observedGoodState = AtomicBoolean(false)

        val watcher = watch(stop) {
            val hasViews = host.spanViews().isNotEmpty()
            val hasLines = host.lineViews().isNotEmpty()

            if (hasViews && !hasLines) {
                observedBadState.set(true)
            }
            if (hasViews && hasLines) {
                observedGoodState.set(true)
            }
        }

        onMain { load() }
        host.awaitUntil("the first load completes") {
            host.view.composedFrame != null && host.lineViews().isNotEmpty() && span.view != null
        }

        stop.set(true)
        watcher.join()

        assertFalse(
            "a ViewSpan's view must never be attached/visible while zero lines are placed, " +
                "on first load same as on a re-frame",
            observedBadState.get()
        )
        assertTrue(
            "the view should eventually show together with the text - this run never even " +
                "reached that state, so the assertion above would have been vacuous",
            observedGoodState.get()
        )

        host.awaitFullyFramed()
    }

    @Test
    fun setSpannedNeverShowsViewsBeforeTextOnFirstLoad() {
        val span = TestViewSpan(ViewSpan.Placement.BLOCK, roomHeight = 200)

        assertNeverShowsViewsBeforeText(span) {
            host.view.setSpanned(spannedOf {
                append("￼\n")
                setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                append(arabicText(2200))
            })
        }
    }

    @Test
    fun setTypesetterNeverShowsViewsBeforeTextOnFirstLoad() {
        val span = TestViewSpan(ViewSpan.Placement.BLOCK, roomHeight = 200)

        val spanned = spannedOf {
            append("￼\n")
            setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(arabicText(2200))
        }

        val typeface = TypefaceStore.getNafeesWeb()
        val defaultSpans = listOf<Any>(TypefaceSpan(typeface), TypeSizeSpan(32.0f))
        val typesetter = Typesetter(spanned, defaultSpans)

        assertNeverShowsViewsBeforeText(span) {
            host.view.typesetter = typesetter
        }
    }

    /**
     * A second first-load, of different brand-new text with its own view, after the first has
     * already completed - not a re-frame of the first text, a wholly new one (as opening a second
     * article does) - must go through the same swap-gated path just as reliably as the very first
     * load did.
     */
    @Test
    fun aSecondFirstLoadAlsoNeverShowsViewsBeforeText() {
        val firstSpan = TestViewSpan(ViewSpan.Placement.BLOCK, roomHeight = 200)
        host.show(spannedOf {
            append("￼\n")
            setSpan(firstSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(arabicText(300))
        })
        assertTrue(onMain { host.spanViews().isNotEmpty() })

        val secondSpan = TestViewSpan(ViewSpan.Placement.BLOCK, roomHeight = 200)

        assertNeverShowsViewsBeforeText(secondSpan) {
            host.view.setSpanned(spannedOf {
                append("￼\n")
                setSpan(secondSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                append(arabicText(2200))
            })
        }

        assertTrue(onMain { secondSpan.created.isNotEmpty() })
    }
}
