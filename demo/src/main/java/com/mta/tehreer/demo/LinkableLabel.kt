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
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import android.text.style.URLSpan
import android.util.AttributeSet
import android.view.MotionEvent
import com.mta.tehreer.widget.TLabel

class LinkableLabel : TLabel {
    private val paint = Paint()
    private var activeLinkSpan: URLSpan? = null
    private var activeLinkPath: Path? = null

    constructor(context: Context?) : super(context) {
        setup()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setup()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        setup()
    }

    private fun setup() {
        paint.style = Paint.Style.FILL
        paint.color = -0x333312
    }

    private fun urlSpanAtIndex(charIndex: Int): URLSpan? {
        if (spanned == null || composedFrame == null) {
            return null
        }
        if (charIndex < composedFrame.charStart || charIndex >= composedFrame.charEnd) {
            return null
        }

        val spans = spanned.getSpans(charIndex, charIndex, URLSpan::class.java)

        return if (spans != null && spans.isNotEmpty()) spans[0] else null
    }

    private fun urlSpanAtPoint(x: Float, y: Float): URLSpan? {
        return urlSpanAtIndex(hitTestPosition(x, y))
    }

    private fun refreshActiveLink() {
        if (spanned != null && composedFrame != null && activeLinkSpan != null) {
            val spanStart = spanned.getSpanStart(activeLinkSpan)
            val spanEnd = spanned.getSpanEnd(activeLinkSpan)

            activeLinkPath = composedFrame.generateSelectionPath(spanStart, spanEnd)
            activeLinkPath?.offset(composedFrame.originX, composedFrame.originY)
        } else {
            activeLinkPath = null
        }

        invalidate()
    }

    private fun clearActiveLink() {
        activeLinkSpan = null
        refreshActiveLink()
    }

    private fun openActiveLink() {
        val uri = Uri.parse(activeLinkSpan?.url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

    override fun onDraw(canvas: Canvas) {
        activeLinkPath?.let {
            canvas.drawPath(it, paint)
        }

        super.onDraw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                activeLinkSpan = urlSpanAtPoint(event.x, event.y)
                if (activeLinkSpan != null) {
                    refreshActiveLink()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (activeLinkSpan != null) {
                    val pointedLinkSpan = urlSpanAtPoint(event.x, event.y)
                    if (pointedLinkSpan !== activeLinkSpan) {
                        clearActiveLink()
                    }

                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (activeLinkSpan != null) {
                    performClick()
                    openActiveLink()
                    clearActiveLink()
                    return true
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                if (activeLinkSpan != null) {
                    clearActiveLink()
                    return true
                }
            }
        }

        return super.onTouchEvent(event)
    }
}
