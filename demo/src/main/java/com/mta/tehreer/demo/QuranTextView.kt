/*
 * Copyright (C) 2023 Muhammad Tayyab Akram
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
import android.graphics.Path
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ViewGroup
import com.mta.tehreer.widget.TTextView
import kotlin.math.roundToInt

class QuranTextView : TTextView {
    class AyahSpan : CharacterStyle() {
        override fun updateDrawState(p0: TextPaint?) {}
    }

    private val paint = Paint()
    private var sideMargin = 0
    private var activeAyahSpan: AyahSpan? = null
    private var activeAyahPath: Path? = null

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

        sideMargin = dpToPx(8.0f).roundToInt()

        container?.apply {
            val marginParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            marginParams.apply {
                topMargin = 0
                leftMargin = sideMargin
                rightMargin = sideMargin
                bottomMargin = 0
            }

            layoutParams = marginParams
        }
    }

    private val container: ViewGroup?
        get() = getChildAt(0) as? ViewGroup

    private val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            var handled = false

            activeAyahSpan = getAyahSpan(event)
            activeAyahSpan?.let {
                refreshActiveAyah()
                handled = true
            }

            return handled
        }
    })

    override fun hitTestPosition(x: Float, y: Float): Int {
        return super.hitTestPosition(x - sideMargin, y)
    }

    override fun onDraw(canvas: Canvas) {
        activeAyahPath?.let {
            canvas.drawPath(it, paint)
        }

        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }

        return super.onTouchEvent(event)
    }

    private fun dpToPx(dp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    private fun <T> getSpanForCharacter(charIndex: Int, type: Class<T>): T? {
        val spanned = typesetter?.spanned ?: spanned
        val composedFrame = composedFrame

        if (spanned == null
            || composedFrame == null
            || charIndex < composedFrame.charStart || charIndex >= composedFrame.charEnd
        ) {
            return null
        }

        val spans = spanned.getSpans(charIndex, charIndex, type)
        return spans.firstOrNull()
    }

    private fun <T> getSpanAtPoint(x: Float, y: Float, type: Class<T>) =
        getSpanForCharacter(hitTestPosition(x, y), type)

    private fun <T> getSpanForEvent(event: MotionEvent, type: Class<T>) =
        getSpanAtPoint(event.x + scrollX, event.y + scrollY, type)

    private fun getAyahSpan(event: MotionEvent): AyahSpan? {
        return getSpanForEvent(event, AyahSpan::class.java)
    }

    private fun getSpanPath(span: Any): Path? {
        val spanned = (typesetter?.spanned ?: spanned) ?: return null
        val composedFrame = composedFrame ?: return null

        val spanStart = spanned.getSpanStart(span)
        val spanEnd = spanned.getSpanEnd(span)

        val path = composedFrame.generateSelectionPath(spanStart, spanEnd)
        path.offset(composedFrame.originX, composedFrame.originY)
        path.offset(sideMargin.toFloat(), 0.0f)

        return path
    }

    private fun refreshActiveAyah() {
        activeAyahPath = activeAyahSpan?.let { getSpanPath(it) }
        invalidate()
    }

    var highlightingColor: Int
        get() = paint.color
        set(value) {
            paint.color = value
            invalidate()
        }
}
