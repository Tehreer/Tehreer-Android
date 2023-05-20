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
import android.graphics.Rect
import android.view.View
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.layout.ComposedLine

internal class LineView(context: Context?) : View(context) {
    val renderer = Renderer()
    val frame = Rect()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        line?.run {
            val dx = originX - frame.left
            val dy = originY - frame.top

            draw(renderer, canvas, dx, dy)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        frame.set(left, top, right, bottom)
    }

    private var _line: ComposedLine? = null
    var line: ComposedLine?
        get() = _line
        set(line) {
            _line = line
            invalidate()
        }
}
