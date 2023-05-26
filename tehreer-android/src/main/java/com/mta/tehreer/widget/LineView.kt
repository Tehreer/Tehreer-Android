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

package com.mta.tehreer.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.layout.ComposedLine
import kotlin.math.ceil
import kotlin.math.floor

internal class LineView(context: Context?) : View(context) {
    private val separatorPaint = Paint()

    val frame = Rect()
    val renderer = Renderer()

    init {
        separatorPaint.color = Color.TRANSPARENT
        separatorPaint.strokeWidth = 1.0f
        separatorPaint.style = Paint.Style.STROKE
    }

    private fun drawSeparator(canvas: Canvas, line: ComposedLine) {
        if (separatorColor == Color.TRANSPARENT) {
            return
        }

        val lineTop = line.originY - line.ascent - frame.top
        val lineBottom = lineTop + line.height

        val separatorLeft = floor(0.0f - frame.left)
        val separatorRight = ceil(separatorLeft + layoutWidth)
        val separatorY = floor(lineBottom - (separatorPaint.strokeWidth / 2.0f))

        canvas.drawLine(separatorLeft, separatorY, separatorRight, separatorY, separatorPaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        line?.let {
            drawSeparator(canvas, it)

            val dx = it.originX - frame.left
            val dy = it.originY - frame.top

            it.draw(renderer, canvas, dx, dy)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        frame.set(left, top, right, bottom)
    }

    var layoutWidth: Float = 0.0f

    var separatorColor: Int
        get() = separatorPaint.color
        set(separatorColor) {
            separatorPaint.color = separatorColor
        }

    private var _line: ComposedLine? = null
    var line: ComposedLine?
        get() = _line
        set(line) {
            _line = line
            invalidate()
        }
}
