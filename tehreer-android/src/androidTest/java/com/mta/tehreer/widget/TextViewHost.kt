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
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Looper
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ReplacementSpan
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.test.platform.app.InstrumentationRegistry
import com.mta.tehreer.layout.ComposedLine
import com.mta.tehreer.util.TypefaceStore
import java.util.concurrent.atomic.AtomicReference

/** Runs [block] on the main thread and returns what it returns. */
internal fun <T> onMain(block: () -> T): T {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        return block()
    }

    val result = AtomicReference<Any?>()
    val failure = AtomicReference<Throwable?>()

    InstrumentationRegistry.getInstrumentation().runOnMainSync {
        try {
            result.set(block())
        } catch (t: Throwable) {
            failure.set(t)
        }
    }

    failure.get()?.let { throw it }

    @Suppress("UNCHECKED_CAST")
    return result.get() as T
}

/** Clicks recorded, instead of doing something. */
internal class RecordingLink : ClickableSpan() {
    var clicks = 0
        private set

    override fun onClick(widget: View) {
        clicks += 1
    }

    override fun updateDrawState(ds: TextPaint) { }
}

/** A drawn box, sized like the reader's inline images: no ascent, all descent. */
internal class BoxSpan(val boxWidth: Int, val boxHeight: Int) : ReplacementSpan() {
    private val paint = Paint().apply { color = Color.GREEN }

    override fun getSize(
        paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?
    ): Int {
        fm?.apply {
            ascent = 0
            descent = boxHeight
            leading = 0
        }

        return boxWidth
    }

    override fun draw(
        canvas: Canvas, text: CharSequence, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        canvas.drawRect(0f, 0f, boxWidth.toFloat(), boxHeight.toFloat(), this.paint)
    }
}

/**
 * A [TTextView] that is measured and laid out by hand, as it is not attached to a window. The text
 * is laid out on a worker thread, so [show] waits for it, and drags and taps are made with
 * [MotionEvent]s dispatched to the view.
 */
internal class TextViewHost(
    val width: Int = 600,
    val height: Int = 800,
    context: Context = InstrumentationRegistry.getInstrumentation().targetContext
) {
    val view: TTextView = onMain { TTextView(context) }

    /** The private text container, which is where a hosted view keeps it, however it is wrapped. */
    val container: ViewGroup = view.getChildAt(0) as ViewGroup

    val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    init {
        onMain {
            view.typeface = TypefaceStore.getNafeesWeb()
            view.textSize = 32.0f
            view.setTextColor(Color.BLACK)
        }
    }

    /** Puts [wrapper] between the view and the container, as the reader does with its column. */
    fun wrapContainer(wrapper: ViewGroup, params: ViewGroup.LayoutParams) {
        onMain {
            view.removeView(container)
            wrapper.addView(container)
            view.addView(wrapper, params)
        }
    }

    fun relayout() {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, width, height)
    }

    /** Shows [text] and waits until its lines are on screen. */
    fun show(text: Spanned) {
        onMain { view.setSpanned(text) }
        awaitLines()
    }

    fun awaitLines() {
        val deadline = SystemClock.uptimeMillis() + 20_000

        while (SystemClock.uptimeMillis() < deadline) {
            val isReady = onMain {
                relayout()
                view.composedFrame != null && lineViews().isNotEmpty()
            }

            if (isReady) {
                onMain { relayout() }
                return
            }

            Thread.sleep(20)
        }

        throw AssertionError("The text was not laid out in time")
    }

    /** Waits until the line boxes of every line of the frame have arrived, as they come in chunks. */
    fun awaitFullyFramed() {
        awaitUntil("the line boxes of the whole document have arrived") {
            val frame = view.composedFrame

            frame != null && (container as TextContainer).lineBoxesForTesting().size >= frame.lines.size
        }
    }

    /**
     * Lays the view out again and again, which is what makes an asynchronous change arrive, until
     * [condition], which runs on the main thread, holds.
     */
    fun awaitUntil(what: String, condition: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 20_000

        while (SystemClock.uptimeMillis() < deadline) {
            if (onMain { relayout(); condition() }) {
                return
            }

            Thread.sleep(20)
        }

        throw AssertionError("Timed out waiting until $what")
    }

    fun scrollTo(y: Int) = onMain { view.scrollTo(0, y) }

    val scrollY: Int
        get() = onMain { view.scrollY }

    val maxScrollY: Int
        get() = onMain {
            view.scrollTo(0, Int.MAX_VALUE / 2)
            view.scrollY
        }

    val lines: List<ComposedLine>
        get() = onMain { view.composedFrame!!.lines }

    /** The line views in the container, wherever they are. */
    fun lineViews(): List<LineView> = onMain {
        (0 until container.childCount).mapNotNull { container.getChildAt(it) as? LineView }
    }

    /** The children of the container that are not lines of text, that is, the views of spans. */
    fun spanViews(): List<View> = onMain {
        (0 until container.childCount).map { container.getChildAt(it) }.filter { it !is LineView }
    }

    /** The top left of the container in the coordinates of the view (scroll applied). */
    fun containerOrigin(): Pair<Int, Int> = onMain {
        var x = 0
        var y = 0
        var current: View = container

        while (current !== view) {
            val parent = current.parent as View
            x += current.left - parent.scrollX
            y += current.top - parent.scrollY
            current = parent
        }

        Pair(x, y)
    }

    /** The lines that intersect the view, judging by the frame alone. */
    fun expectedVisibleLines(): List<ComposedLine> {
        val (_, originY) = containerOrigin()

        return lines.filter {
            val top = originY + it.originY - it.ascent
            val bottom = originY + it.originY + it.descent + it.leading

            bottom > 0 && top < height
        }
    }

    /** Where the middle of [line] is, in the coordinates of the view. */
    fun centerOf(line: ComposedLine): Pair<Float, Float> {
        val (originX, originY) = containerOrigin()

        return Pair(
            originX + line.originX + line.width / 2,
            originY + line.originY - line.ascent / 2 + line.descent / 2
        )
    }

    fun render(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.WHITE)
        onMain {
            // The canvas of a child is moved by the scroll of the parent that draws it; there is
            // no parent here.
            canvas.translate(-view.scrollX.toFloat(), -view.scrollY.toFloat())
            view.draw(canvas)
        }

        return bitmap
    }

    private var downTime = 0L

    fun touch(action: Int, x: Float, y: Float, at: Long = SystemClock.uptimeMillis()): Boolean {
        if (action == MotionEvent.ACTION_DOWN) {
            downTime = at
        }

        val event = MotionEvent.obtain(downTime, at, action, x, y, 0)

        try {
            return onMain { view.dispatchTouchEvent(event) }
        } finally {
            event.recycle()
        }
    }

    fun tap(x: Float, y: Float) {
        touch(MotionEvent.ACTION_DOWN, x, y)
        touch(MotionEvent.ACTION_UP, x, y)
    }

    /** Drags from [fromY] to [toY] in ten quick steps and lifts the finger. */
    fun drag(x: Float, fromY: Float, toY: Float) {
        val start = SystemClock.uptimeMillis()

        touch(MotionEvent.ACTION_DOWN, x, fromY, start)
        for (step in 1..10) {
            touch(MotionEvent.ACTION_MOVE, x, fromY + (toY - fromY) * step / 10, start + step * 10L)
        }
        touch(MotionEvent.ACTION_UP, x, toY, start + 110L)
    }
}

/** The words of the Arabic alphabet, which the test font has, as text that wraps. */
internal const val ARABIC_WORDS = "ابجد هوز حطي كلمن سعفص قرشت ثخذ ضظغ "

internal fun arabicText(wordGroups: Int): String = ARABIC_WORDS.repeat(wordGroups)

internal fun spannedOf(build: SpannableStringBuilder.() -> Unit): SpannableStringBuilder =
    SpannableStringBuilder().apply(build)
