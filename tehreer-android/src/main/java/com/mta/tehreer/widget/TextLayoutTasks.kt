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

import android.graphics.Color
import android.os.Handler
import android.text.Spanned
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.DrawStyle
import com.mta.tehreer.graphics.StrokeCap
import com.mta.tehreer.graphics.StrokeJoin
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.util.SmartRunnable
import com.mta.tehreer.layout.ComposedFrame
import com.mta.tehreer.layout.FrameResolver
import com.mta.tehreer.layout.LineLocator
import com.mta.tehreer.layout.TextAlignment
import com.mta.tehreer.layout.Typesetter
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import java.util.Queue


internal data class TextProperties(
    var handler: Handler,
    var layoutID: Any? = null,
    var layoutWidth: Int = 0,
    var typeface: Typeface? = null,
    var text: String? = null,
    var spanned: Spanned? = null,
    var textSize: Float = 16.0f,
    var textColor: Int = Color.BLACK,
    var textAlignment: TextAlignment = TextAlignment.LEADING,
    var extraLineSpacing: Float = 0.0f,
    var lineHeightMultiplier: Float = 1.0f,
    var isJustificationEnabled: Boolean = false,
    var justificationLevel: Float = 1.0f,
    var separatorColor: Int = Color.TRANSPARENT,
    var drawStyle: DrawStyle = DrawStyle.FILL,
    var strokeColor: Int = Color.BLACK,
    var strokeRadius: Float = 0.0f,
    var strokeCap: StrokeCap = StrokeCap.BUTT,
    var strokeJoin: StrokeJoin = StrokeJoin.ROUND,
    var strokeMiter: Float = 1.0f,
    var typesetter: Typesetter? = null,
    var resolvedFrame: ComposedFrame? = null
) {
    fun updateRenderer(renderer: Renderer) {
        renderer.typeface = typeface
        renderer.typeSize = textSize
        renderer.foregroundColor = textColor
        renderer.drawStyle = drawStyle
        renderer.strokeColor = strokeColor
        renderer.strokeRadius = strokeRadius
        renderer.strokeCap = strokeCap
        renderer.strokeJoin = strokeJoin
        renderer.strokeMiter = strokeMiter
    }
}

internal typealias OnTaskUpdateListener<T> = (T) -> Unit

internal class TextResolvingTask(
    private val subTasks: Queue<SmartRunnable>
) : SmartRunnable() {
    private var currentTask: SmartRunnable? = null

    @Synchronized
    private fun poll(): SmartRunnable? {
        currentTask = subTasks.poll()
        return currentTask
    }

    override fun run() {
        var runnable: SmartRunnable?

        while (poll().also { runnable = it } != null) {
            runnable?.run()
        }
    }

    @Synchronized
    override fun cancel() {
        super.cancel()

        val iterator = subTasks.iterator()

        while (iterator.hasNext()) {
            val runnable = iterator.next()
            runnable.cancel()

            iterator.remove()
        }

        currentTask?.cancel()
    }
}

internal class TypesettingTask(
    private val properties: TextProperties,
    private val listener: OnTaskUpdateListener<Typesetter?>
) : SmartRunnable() {
    private fun notifyUpdateIfNeeded() {
        if (!isCancelled) {
            properties.handler.post { listener(properties.typesetter) }
        }
    }

    override fun run() {
        val text = properties.text
        val spanned = properties.spanned

        properties.typesetter = null

        if (text != null) {
            val typeface = properties.typeface

            if (typeface != null && text.isNotEmpty()) {
                properties.typesetter = Typesetter(text, typeface, properties.textSize)
            }
        } else if (spanned != null && spanned.isNotEmpty()) {
            val defaultSpans = mutableListOf<Any>()

            properties.typeface?.let { defaultSpans.add(TypefaceSpan(it)) }
            defaultSpans.add(TypeSizeSpan(properties.textSize))

            properties.typesetter = Typesetter(spanned, defaultSpans)
        }

        notifyUpdateIfNeeded()
    }
}

internal class FrameResolvingTask(
    private val properties: TextProperties,
    private val listener: OnTaskUpdateListener<ComposedFrame?>
) : SmartRunnable() {
    private fun notifyUpdateIfNeeded() {
        if (!isCancelled) {
            properties.handler.post { listener(properties.resolvedFrame) }
        }
    }

    override fun run() {
        val input = properties.typesetter
        properties.resolvedFrame = null

        if (input != null) {
            val resolver = FrameResolver()
            resolver.apply {
                typesetter = input
                frameWidth = properties.layoutWidth.toFloat()
                frameHeight = Float.POSITIVE_INFINITY
                fitsHorizontally = false
                fitsVertically = true
                textAlignment = properties.textAlignment
                extraLineSpacing = properties.extraLineSpacing
                lineHeightMultiplier = properties.lineHeightMultiplier
                isJustificationEnabled = properties.isJustificationEnabled
                justificationLevel = properties.justificationLevel
            }

            properties.resolvedFrame = resolver.createFrame(0, input.spanned.length)
        }

        notifyUpdateIfNeeded()
    }
}

internal class LineLocatorTask(
    private val properties: TextProperties,
    private val listener: OnTaskUpdateListener<LineLocator?>
) : SmartRunnable() {
    private fun notifyUpdateIfNeeded(lineLocator: LineLocator?) {
        if (!isCancelled) {
            properties.handler.post { listener(lineLocator) }
        }
    }

    override fun run() {
        var lineLocator: LineLocator? = null

        val frame = properties.resolvedFrame
        if (frame != null) {
            val renderer = Renderer()
            properties.updateRenderer(renderer)

            lineLocator = LineLocator(frame, renderer)
        }

        notifyUpdateIfNeeded(lineLocator)
    }
}
