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
import android.view.View
import android.view.ViewGroup
import com.mta.tehreer.layout.ComposedFrame
import com.mta.tehreer.layout.style.ViewSpan
import java.util.IdentityHashMap
import kotlin.math.roundToInt

/** The place that the view of a [ViewSpan] takes in the current frame, in container coordinates. */
internal class ViewSlot(val span: ViewSpan, val rect: Rect)

internal fun TextContainer.resolveViewSlots(frame: ComposedFrame): List<ViewSlot> {
    val source = frameSpanned ?: return emptyList()
    val slots = mutableListOf<ViewSlot>()

    for (span in source.getSpans(frame.charStart, frame.charEnd, ViewSpan::class.java)) {
        val start = source.getSpanStart(span)
        if (start < frame.charStart || start >= frame.charEnd) {
            continue
        }

        val line = frame.lines[frame.getLineIndexForChar(start)]
        val run = line.runs.firstOrNull { start >= it.charStart && start < it.charEnd } ?: continue
        val rect = computeViewRect(frame, line, run, span)

        slots.add(
            ViewSlot(
                span,
                Rect(rect.left.roundToInt(), rect.top.roundToInt(), rect.right.roundToInt(), rect.bottom.roundToInt())
            )
        )
    }

    slots.sortBy { it.rect.top }

    return slots
}

internal fun TextContainer.detachOrphanViews() {
    val liveSpans = IdentityHashMap<ViewSpan, Boolean>()
    for (slot in viewSlots) {
        liveSpans[slot.span] = true
    }

    for ((span, view) in spanViews.entries.toList()) {
        if (!liveSpans.containsKey(span)) {
            detachView(span, view)
        }
    }

    measuredViews.keys.retainAll { liveSpans.containsKey(it) }
    resizingSpans.keys.retainAll { liveSpans.containsKey(it) }
}

private fun TextContainer.detachView(span: ViewSpan, view: View) {
    spanViews.remove(span)
    span.view = null
    removeView(view)
}

/**
 * Whether a view at [rect] is on the screen, or no further from it than
 * [TextContainer.viewSpanPrefetchDistance]. A view with no room yet counts too when it is at the
 * place of the screen, or it would never be attached and never grow.
 */
private fun TextContainer.isNearScreen(rect: Rect): Boolean {
    val margin = viewSpanPrefetchDistance
    val top = visibleRect.top - margin
    val bottom = visibleRect.bottom + margin

    if (!rect.isEmpty) {
        return rect.left < visibleRect.right && rect.right > visibleRect.left &&
            rect.top < bottom && rect.bottom > top
    }

    return rect.left <= visibleRect.right && rect.right >= visibleRect.left &&
        rect.top <= bottom && rect.bottom >= top
}

internal fun TextContainer.layoutViews() {
    for (slot in viewSlots) {
        val span = slot.span
        val rect = slot.rect
        var view = spanViews[span]

        if (!span.retainWhenOffscreen && !isNearScreen(rect)) {
            if (view != null) {
                detachView(span, view)
            }

            continue
        }

        if (view == null) {
            view = measuredViews.remove(span) ?: span.createView(context)

            spanViews[span] = view
            span.view = view

            addView(view, ViewGroup.LayoutParams(rect.width(), rect.height()))
        }

        view.measure(
            View.MeasureSpec.makeMeasureSpec(rect.width(), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(rect.height(), View.MeasureSpec.EXACTLY)
        )
        view.layout(rect.left, rect.top, rect.right, rect.bottom)

        if (isFrameFresh && resizingSpans.remove(span) != null) {
            view.visibility = View.VISIBLE
        }
    }

    isFrameFresh = false
}

/**
 * Measures the views of the spans that leave their height to the view, at the width that the text
 * has, and hands the heights to the spans, which the frame reads.
 */
internal fun TextContainer.measureViewSpans() {
    val source = (if (isTypesetterUserDefined) properties.typesetter?.spanned else properties.spanned) ?: return

    for (span in source.getSpans(0, source.length, ViewSpan::class.java)) {
        span.attachTo(this)

        if (span.isMeasured) {
            measureViewSpan(span)
        }
    }
}

internal fun TextContainer.measureViewSpan(span: ViewSpan): Boolean {
    val view = spanViews[span] ?: measuredViews.getOrPut(span) { span.createView(context) }

    val height = span.onMeasure(properties.layoutWidth, view).height.coerceAtLeast(0)
    val isChanged = span.measuredHeight != height

    span.measuredHeight = height

    return isChanged
}

internal fun forceLayoutTree(view: View) {
    view.forceLayout()

    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            forceLayoutTree(view.getChildAt(i))
        }
    }
}
