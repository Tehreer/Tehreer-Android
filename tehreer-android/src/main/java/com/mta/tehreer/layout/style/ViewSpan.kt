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

package com.mta.tehreer.layout.style

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.text.style.ReplacementSpan
import android.util.Size
import android.view.View
import android.view.ViewGroup
import com.mta.tehreer.widget.TextContainer
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

/**
 * A replacement span whose room in the text is filled by a real [View], which a [TTextView] keeps
 * in place while the text scrolls. Any view can be used, a `ComposeView` included.
 *
 * The span goes on a single character, normally the object replacement character (U+FFFC). A
 * [BLOCK][Placement.BLOCK] span has a line of its own, wherever it is, and is usually put in a
 * paragraph of its own (`"￼\n"`); an [INLINE][Placement.INLINE] span sits in a line of text like a
 * very large letter.
 *
 * The span only says what it needs: [createView] makes the view, and the size of the room comes
 * from [width], [height], [margins] and [baselineOffset]. The text view creates the view when its
 * line comes near the screen, lays it out over the room and, once the line is far away again,
 * removes it (unless [retainWhenOffscreen]) so a page full of views stays cheap. "Near" is the
 * screen, or as far from it as [TTextView.setViewSpanPrefetchDistance] says, so that a view that
 * takes time to be ready (a `ComposeView` that has to be composed) is ready before it is seen. The view is a
 * child of the text view, so it gets its own touches, and a drag that starts on it still scrolls
 * the text unless the view consumes it.
 *
 * The height can be fixed, or decided by the view. With the default [height] of [WRAP_CONTENT] the
 * text view makes the view, and asks [onMeasure] how tall it is for the width that the text has,
 * before the lines are made, so the first frame is already right and nobody has to know the
 * width in advance. A view that only learns its height once it is on screen (a `ComposeView`)
 * tells so by calling [requestResize].
 *
 * The properties that describe the room are read on a background thread while the text is being
 * typeset, so they must not touch views, and they must not change while the text is displayed
 * (call [requestResize] after changing them). The span is not shared between two ranges of a text,
 * or between two texts shown at the same time.
 *
 * Java subclasses override the getters, for example `getHeight()`.
 */
abstract class ViewSpan : ReplacementSpan() {
    /** Where a [ViewSpan] goes in the text. */
    enum class Placement {
        /**
         * The view has a line of its own and is as wide as the text, at whatever place the text
         * begins. This is the default.
         */
        BLOCK,

        /**
         * The view sits in a line of text, at the width [ViewSpan.width], with its bottom edge
         * [ViewSpan.baselineOffset] below the baseline. It makes the line taller if it is taller
         * than the text.
         */
        INLINE
    }

    /** The view that is attached to the text view right now, or `null` if there is none. */
    var view: View? = null
        internal set

    /** Where the view goes, on a line of its own or inside a line of text. */
    open val placement: Placement
        get() = Placement.BLOCK

    /**
     * The width of the room in pixels, for an [INLINE][Placement.INLINE] span. A [BLOCK][Placement.BLOCK]
     * span is as wide as the text.
     */
    open val width: Int
        get() = 0

    /**
     * The height of the room, and of the view, in pixels; or [WRAP_CONTENT], which is the default,
     * to have it decided by the view, see [onMeasure].
     */
    open val height: Int
        get() = WRAP_CONTENT

    /**
     * How far the bottom edge of an [INLINE][Placement.INLINE] view is below the baseline of the
     * text, in pixels, between 0 and [height]. With 0 the view stands on the baseline.
     */
    open val baselineOffset: Int
        get() = 0

    /**
     * The space above and below a [BLOCK][Placement.BLOCK] view, in pixels; only [Rect.top] and
     * [Rect.bottom] are used. It is part of the line, so the text around it is pushed away.
     * An [INLINE][Placement.INLINE] view has no margins.
     */
    open val margins: Rect
        get() = Rect()

    /**
     * Whether the view is kept, laid out where it belongs and with its state, while it is far
     * from the screen. By default it is dropped, and made again by [createView] when it comes back.
     */
    open val retainWhenOffscreen: Boolean
        get() = false

    /**
     * Whether the view is kept out of sight while the text is framed again after it asked for a
     * resize, which is the default. A view whose room is going to change is then never seen with
     * a room that is not its own, at the price of a blank for as long as the frame takes; and a
     * view that is not on the screen is never hidden, as nobody would see the difference.
     *
     * A span whose view is likely to be on the screen when it resizes, and that would rather
     * be seen at its old size for a moment than not at all (a footer that gets a list of
     * related stories), returns `false`: the view stays where it is, is clipped to the old room
     * if it has grown, and gets the new one as soon as the frame is in place.
     */
    open val hideWhileResizing: Boolean
        get() = true

    /** Makes the view. It is called on the main thread, when the view is needed. */
    abstract fun createView(context: Context): View

    /**
     * Tells how big the [view] wants to be when the text is [layoutWidth] pixels wide. It is called
     * on the main thread whenever the text is laid out again, for a span whose [height] is
     * [WRAP_CONTENT], before the lines are made; whatever height is returned is the height of the
     * room. The width is not used yet: a [BLOCK][Placement.BLOCK] view is always as wide as the
     * text, and an [INLINE][Placement.INLINE] view as wide as [width].
     *
     * The default measures the view at that width with as much height as it wants. It is right for
     * any view whose height is known as soon as it is measured; override it to do differently.
     * The view has no parent yet, and may never be shown.
     */
    open fun onMeasure(layoutWidth: Int, view: View): Size {
        val viewWidth = if (isBlock) layoutWidth else width.coerceAtLeast(0)

        view.measure(
            View.MeasureSpec.makeMeasureSpec(viewWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        return Size(viewWidth, view.measuredHeight)
    }

    /**
     * Tells that the view has changed its size by itself, or that a property of the room has: the
     * text is framed again with the new size, without being typeset again, and the lines below the
     * view move. What the reader is looking at does not, and the view is kept out of sight until
     * the new frame is in place so it does not jump. It can be called from any thread at any time;
     * calls that come together are made one.
     */
    fun requestResize() {
        if (isResizePending.compareAndSet(false, true)) {
            mainHandler.post {
                isResizePending.set(false)

                for (host in liveHosts()) {
                    host.onSpanResizeRequested(this)
                }
            }
        }
    }

    /**
     * Reports the room of this span to the typesetter. The metrics contract of Tehreer differs
     * from the one of Android's `TextView`: the ascent that is given back is the room above the
     * baseline, as a positive number, not the negative distance to the top of the text. Here it
     * is done from the properties above, so a subclass does not have to.
     *
     * This is only the first, provisional answer, which is what the line breaking sees: a block
     * is no wider than a point, and is put on a line of its own by the line breaking. The room
     * that the lines have is worked out again for every frame, see [computeRoom].
     */
    final override fun getSize(
        paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?
    ): Int {
        val room = computeRoom(0.0f)

        fm?.let {
            it.ascent = room.ascent
            it.descent = room.descent
            it.leading = 0
        }

        return room.extent
    }

    /** Nothing is drawn: the room is filled by the view. */
    override fun draw(
        canvas: Canvas, text: CharSequence, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) { }

    internal class Room(val ascent: Int, val descent: Int, val extent: Int)

    internal val isBlock: Boolean
        get() = placement == Placement.BLOCK

    /** The height that the text view measured, or -1 if it has not. */
    @Volatile
    internal var measuredHeight = -1

    /** Whether the text view has to ask [onMeasure] for the height. */
    internal val isMeasured: Boolean
        get() = height < 0

    /**
     * The room of the line box, in the numbers of a text run, when the frame is [layoutWidth] wide.
     * It is called every time a line with this span is made, so the frame has the width of the
     * text and the height that the view was measured to, and needs no typesetting again.
     */
    internal fun computeRoom(layoutWidth: Float): Room {
        val fixed = height
        val height = if (fixed >= 0) fixed else measuredHeight.coerceAtLeast(0)

        if (isBlock) {
            val margins = margins
            val extent = if (layoutWidth.isFinite()) layoutWidth.roundToInt() else 0

            return Room(margins.top.coerceAtLeast(0) + height, margins.bottom.coerceAtLeast(0), extent)
        }

        val offset = baselineOffset.coerceIn(0, height)
        return Room(height - offset, offset, width.coerceAtLeast(0))
    }

    private val hosts = mutableListOf<WeakReference<TextContainer>>()

    /** Remembers the container that shows this span, to tell it about [requestResize]. */
    internal fun attachTo(host: TextContainer) {
        synchronized(hosts) {
            hosts.removeAll { it.get() == null }

            if (hosts.none { it.get() === host }) {
                hosts.add(WeakReference(host))
            }
        }
    }

    private fun liveHosts(): List<TextContainer> = synchronized(hosts) {
        hosts.mapNotNull { it.get() }
    }

    private val isResizePending = AtomicBoolean(false)

    companion object {
        /** The height of a span that is decided by the view, see [onMeasure]. */
        const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT

        private val mainHandler = Handler(Looper.getMainLooper())
    }
}
