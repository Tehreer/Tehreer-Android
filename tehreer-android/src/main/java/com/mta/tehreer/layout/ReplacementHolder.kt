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

package com.mta.tehreer.layout

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.text.style.ReplacementSpan
import androidx.annotation.Keep
import com.mta.tehreer.layout.style.ViewSpan

/**
 * Holds a replacement span for Core, which asks it for the room of the span whenever a line is
 * made. The metrics of the typeface are what the span starts from, as it can change them.
 */
@Keep
internal class ReplacementHolder(
    val span: ReplacementSpan,
    private val text: CharSequence,
    private val start: Int,
    private val end: Int,
    private val typefaceAscent: Float,
    private val typefaceDescent: Float,
    private val typefaceLeading: Float
) {
    /** Whether the span has a line of its own. */
    val isBlock: Boolean
        get() = (span as? ViewSpan)?.isBlock == true

    private fun makeMetrics(): FontMetricsInt {
        val metrics = FontMetricsInt()
        metrics.ascent = -(typefaceAscent + 0.5f).toInt()
        metrics.descent = (typefaceDescent + 0.5f).toInt()
        metrics.leading = (typefaceLeading + 0.5f).toInt()

        return metrics
    }

    /** Fills the ascent, the descent and the extent of the room, for a frame of the given width. */
    @Keep
    fun computeRoom(layoutWidth: Float, room: FloatArray) {
        if (span is ViewSpan) {
            val viewRoom = span.computeRoom(layoutWidth)
            room[0] = viewRoom.ascent.toFloat()
            room[1] = viewRoom.descent.toFloat()
            room[2] = viewRoom.extent.toFloat()
        } else {
            val metrics = makeMetrics()
            val extent = span.getSize(Paint(), text, start, end, metrics)

            room[0] = (-metrics.ascent).toFloat()
            room[1] = metrics.descent.toFloat()
            room[2] = extent.toFloat()
        }
    }

    /** Draws the span at the origin of its run, with the baseline at zero. */
    fun draw(canvas: Canvas, ascent: Float, descent: Float) {
        span.draw(
            canvas, text, start, end,
            0.0f, -(ascent + 0.5f).toInt(),
            0, (descent + 0.5f).toInt(),
            Paint()
        )
    }
}
