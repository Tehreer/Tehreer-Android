/*
 * Copyright (C) 2023-2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.demo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import com.mta.tehreer.widget.TTextView
import kotlin.math.roundToInt

/**
 * A text view of ayahs, the one that is tapped stays highlighted until another one is, or until
 * [clearAyahHighlighting] is called.
 */
class QuranTextView : TTextView {
    class AyahSpan : CharacterStyle() {
        override fun updateDrawState(p0: TextPaint?) {}
    }

    private val paint = Paint()
    private var activeAyahSpan: AyahSpan? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    init {
        paint.style = Paint.Style.FILL
        paint.color = 0xFFDDDDDD.toInt()

        val sideMargin = dpToPx(8.0f).roundToInt()
        setPadding(sideMargin, 0, sideMargin, 0)
    }

    private val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            activeAyahSpan = getAyahSpan(event)
            invalidate()

            return activeAyahSpan != null
        }
    })

    override fun dispatchDraw(canvas: Canvas) {
        // Behind the text, so that it stays readable. The rects are in the coordinates of this
        // view, with the scroll applied, so they are moved back to the scrolled content.
        activeAyahSpan?.let { span ->
            for (rect in getSpanRects(span)) {
                canvas.drawRect(
                    rect.left + scrollX, rect.top + scrollY,
                    rect.right + scrollX, rect.bottom + scrollY,
                    paint
                )
            }
        }

        super.dispatchDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun dpToPx(dp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    private fun getAyahSpan(event: MotionEvent): AyahSpan? {
        val spanned = (typesetter?.spanned ?: spanned) ?: return null
        val charIndex = getCharIndexForPosition(event.x, event.y)

        if (charIndex < 0) {
            return null
        }

        return spanned.getSpans(charIndex, charIndex, AyahSpan::class.java).firstOrNull()
    }

    fun clearAyahHighlighting() {
        activeAyahSpan = null
        invalidate()
    }

    var highlightingColor: Int
        get() = paint.color
        set(value) {
            paint.color = value
            invalidate()
        }
}
