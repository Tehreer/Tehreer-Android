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

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ClickableSpan
import android.text.style.ReplacementSpan
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.mta.tehreer.layout.style.ViewSpan
import kotlin.math.abs

/**
 * Gives a [TTextView] its span interaction: it follows a touch that starts on a clickable span or
 * on a replacement span, draws the pressed state of a link, and reports the tap when the finger
 * lifts.
 *
 * It never takes a touch away from the scroll view: the events are only observed, so a drag that
 * starts on a link still scrolls, and the press is dropped as soon as the finger moves more than
 * the touch slop (the scroll view starts dragging at the same distance) or the touch is cancelled.
 */
internal class SpanTouchHandler(private val view: TTextView) {
    private val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop
    private val highlightPaint = Paint().apply { style = Paint.Style.FILL }

    private var pressedSpan: Any? = null
    private var downX = 0.0f
    private var downY = 0.0f

    private var isStoppingFling = false

    /**
     * Called when a touch starts, with whether the scroll view took it to stop a fling. Such a
     * touch is not a tap on whatever is under the finger.
     */
    fun onTouchStarted(isStoppingFling: Boolean) {
        this.isStoppingFling = isStoppingFling
    }

    /** Returns whether a span is being pressed. */
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                cancel()

                if (!isStoppingFling) {
                    press(event.x, event.y)
                }

                isStoppingFling = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (pressedSpan != null) {
                    val isSlop = abs(event.x - downX) > touchSlop || abs(event.y - downY) > touchSlop

                    if (isSlop || findSpan(event.x, event.y) !== pressedSpan) {
                        cancel()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                val span = pressedSpan
                cancel()

                if (span != null) {
                    click(span)
                    return true
                }
            }
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_POINTER_DOWN -> {
                cancel()
            }
        }

        return pressedSpan != null
    }

    /** Drops the pressed state, if any. */
    fun cancel() {
        val wasHighlighted = pressedSpan is ClickableSpan

        pressedSpan = null

        if (wasHighlighted) {
            view.invalidateHighlight()
        }
    }

    /**
     * Draws the pressed state of a link. The [canvas] is the one of the scrolled content, so the
     * rects, which are in view coordinates, are moved by the scroll offset.
     */
    fun drawHighlight(canvas: Canvas, color: Int) {
        val span = pressedSpan as? ClickableSpan ?: return

        highlightPaint.color = color

        val scrollX = view.scrollX.toFloat()
        val scrollY = view.scrollY.toFloat()

        for (rect in view.getSpanRects(span)) {
            canvas.drawRect(
                rect.left + scrollX, rect.top + scrollY,
                rect.right + scrollX, rect.bottom + scrollY,
                highlightPaint
            )
        }
    }

    private fun press(x: Float, y: Float) {
        val span = findSpan(x, y) ?: return

        pressedSpan = span
        downX = x
        downY = y

        if (span is ClickableSpan) {
            view.invalidateHighlight()
        }
    }

    private fun click(span: Any) {
        view.performClick()

        val listener = view.onSpanClickListener
        if (listener != null && listener.onSpanClick(view, span)) {
            return
        }

        if (span is ClickableSpan) {
            span.onClick(view)
        }
    }

    /**
     * Finds what a touch at [x], [y] (view coordinates) would press: a replacement span if there
     * is one, otherwise a clickable span. A link is inert under a replacement span, so there are
     * no links on an image.
     */
    private fun findSpan(x: Float, y: Float): Any? {
        val source = view.sourceSpanned ?: return null
        val frame = view.composedFrame ?: return null

        val offset = view.getCharIndexUnderPosition(x, y)
        if (offset < frame.charStart || offset >= frame.charEnd) {
            return null
        }

        // The room of a view is the view's business, so the text under it, and the margins around
        // it, are inert.
        if (view.isInsideViewSpan(x, y)) {
            return null
        }

        val replacementSpans = source.getSpans(offset, offset, ReplacementSpan::class.java)
        if (replacementSpans.any { it is ViewSpan }) {
            return null
        }
        if (replacementSpans.isNotEmpty()) {
            return replacementSpans[0]
        }

        if (!view.linksClickable) {
            return null
        }

        return source.getSpans(offset, offset, ClickableSpan::class.java).firstOrNull()
    }
}
