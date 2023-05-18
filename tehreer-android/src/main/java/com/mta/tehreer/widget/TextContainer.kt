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
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ScrollView
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.util.SmartRunnable
import com.mta.tehreer.layout.ComposedFrame
import com.mta.tehreer.layout.FrameResolver
import com.mta.tehreer.layout.TextAlignment
import com.mta.tehreer.layout.Typesetter
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import java.util.ArrayDeque
import java.util.Queue
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

private data class TextProperties(
    var handler: Handler,
    var layoutID: Any? = null,
    var layoutWidth: Int = 0,
    var typeface: Typeface? = null,
    var text: String? = null,
    var spanned: Spanned? = null,
    var textSize: Float = 16.0f,
    var textColor: Int = Color.BLACK,
    var textAlignment: TextAlignment = TextAlignment.INTRINSIC,
    var extraLineSpacing: Float = 0.0f,
    var lineHeightMultiplier: Float = 1.0f,
    var isJustificationEnabled: Boolean = false,
    var justificationLevel: Float = 1.0f,
    var typesetter: Typesetter? = null,
    var composedFrame: ComposedFrame? = null
)

private typealias OnTaskUpdateListener<T> = (T) -> Unit

internal class TextContainer : ViewGroup {
    private lateinit var properties: TextProperties

    private var lineBoxes = mutableListOf<Rect>()

    private var scrollView: ScrollView? = null
    private var scrollX = 0
    private var scrollY = 0
    private var scrollWidth = 0
    private var scrollHeight = 0

    private val visibleRect = Rect()

    private var isTextLayoutRequested = false
    private var isTypesetterUserDefined = false
    private var isTypesetterResolved = false
    private var isComposedFrameResolved = false

    private val lineViews = mutableListOf<LineView>()
    private val insideViews = mutableListOf<LineView>()
    private val outsideViews = mutableListOf<LineView>()
    private val visibleIndexes = mutableListOf<Int>()

    private val executor: Executor = Executors.newCachedThreadPool()
    private var textTask: TextResolvingTask? = null

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        setup()
    }

    private fun setup() {
        properties = TextProperties(
            handler = Handler(Looper.getMainLooper())
        )
    }

    fun setScrollView(view: ScrollView?) {
        scrollView = view
    }

    fun setScrollPosition(x: Int, y: Int) {
        var scrollChanged = false

        if (scrollX != x) {
            scrollX = x
            scrollChanged = true
        }
        if (scrollY != y) {
            scrollY = y
            scrollChanged = true
        }

        if (scrollChanged) {
            layoutLines()
        }
    }

    fun setVisibleRegion(width: Int, height: Int) {
        if (scrollWidth != width) {
            scrollWidth = width
            requestComposedFrame()
        }

        scrollHeight = height
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = 0

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = 0
        }

        properties.composedFrame?.let {
            heightSize = ceil(it.height).toInt()
        }

        setMeasuredDimension(widthSize, heightSize)

        if (properties.layoutWidth != widthSize) {
            properties.layoutWidth = widthSize
            requestTextLayout()
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isTextLayoutRequested) {
            performTextLayout()
        }

        layoutLines()
    }

    private class TypesettingTask(
        private val properties: TextProperties,
        private val listener: OnTaskUpdateListener<Typesetter?>
    ) : SmartRunnable() {
        private fun notifyUpdateIfNeeded() {
            if (!isCancelled) {
                properties.handler.run {
                    post { listener(properties.typesetter) }
                }
            }
        }

        override fun run() {
            val text = properties.text
            val spanned = properties.spanned

            if (text != null) {
                val typeface = properties.typeface
                val textSize = properties.textSize

                if (typeface != null && text.isNotEmpty()) {
                    properties.typesetter = Typesetter(text, typeface, textSize)
                }
            } else if (spanned != null) {
                if (spanned.isNotEmpty()) {
                    val typeface = properties.typeface
                    val textSize = properties.textSize

                    val defaultSpans = mutableListOf<Any>()

                    if (typeface != null) {
                        defaultSpans.add(TypefaceSpan(typeface))
                    }
                    defaultSpans.add(TypeSizeSpan(textSize))

                    properties.typesetter = Typesetter(spanned, defaultSpans)
                }
            }

            notifyUpdateIfNeeded()
        }
    }

    private class FrameResolvingTask(
        private val properties: TextProperties,
        private val listener: OnTaskUpdateListener<ComposedFrame?>
    ) : SmartRunnable() {
        private fun notifyUpdateIfNeeded() {
            if (!isCancelled) {
                properties.handler.run {
                    post { listener(properties.composedFrame) }
                }
            }
        }

        override fun run() {
            val input = properties.typesetter
            if (input != null) {
                val resolver = FrameResolver()
                resolver.apply {
                    typesetter = input
                    frameBounds =
                        RectF(0.0f, 0.0f, properties.layoutWidth.toFloat(), Float.POSITIVE_INFINITY)
                    fitsHorizontally = false
                    fitsVertically = true
                    textAlignment = properties.textAlignment
                    extraLineSpacing = properties.extraLineSpacing
                    lineHeightMultiplier = properties.lineHeightMultiplier
                    isJustificationEnabled = properties.isJustificationEnabled
                    justificationLevel = properties.justificationLevel
                }

                properties.composedFrame = resolver.createFrame(0, input.spanned.length)
            }

            notifyUpdateIfNeeded()
        }
    }

    private class LineBoxesTask(
        private val properties: TextProperties,
        private val listener: OnTaskUpdateListener<MutableList<Rect>>
    ) : SmartRunnable() {
        private val lineBoxes = mutableListOf<Rect>()

        private fun notifyUpdateIfNeeded() {
            if (!isCancelled) {
                val list = lineBoxes.toMutableList()

                properties.handler.run {
                    post { listener(list) }
                }
            }
        }

        override fun run() {
            val input = properties.composedFrame?.lines
            if (input != null) {
                val renderer = Renderer()
                renderer.typeface = properties.typeface
                renderer.typeSize = properties.textSize
                renderer.fillColor = properties.textColor

                var lineChunk = 0

                for (line in input) {
                    val boundingBox = line.computeBoundingBox(renderer)
                    boundingBox.offset(line.originX, line.originY)

                    lineBoxes.add(
                        Rect(
                            boundingBox.left.roundToInt(),
                            boundingBox.top.roundToInt(),
                            boundingBox.right.roundToInt(),
                            boundingBox.bottom.roundToInt()
                        )
                    )

                    if (isCancelled) {
                        break
                    }

                    if (lineChunk == 64) {
                        notifyUpdateIfNeeded()
                        lineChunk = 0
                    } else {
                        lineChunk += 1
                    }
                }
            }

            notifyUpdateIfNeeded()
        }
    }

    private class TextResolvingTask(
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

    private fun performTextLayout() {
        val context = properties.copy()

        val subTasks: Queue<SmartRunnable> = ArrayDeque()
        if (!isTypesetterResolved) {
            subTasks.add(
                TypesettingTask(context) { typesetter ->
                    updateTypesetter(context.layoutID, typesetter)
                })
        }
        subTasks.add(
            FrameResolvingTask(context) { composedFrame ->
                updateComposedFrame(context.layoutID, composedFrame)
            }
        )
        subTasks.add(
            LineBoxesTask(context) { lineBoxes ->
                updateLineBoxes(context.layoutID, lineBoxes)
            }
        )

        textTask = TextResolvingTask(subTasks)
        executor.execute(textTask)

        isTextLayoutRequested = false
    }

    private fun updateTypesetter(layoutID: Any?, typesetter: Typesetter?) {
        if (layoutID === properties.layoutID) {
            isTypesetterResolved = true
            properties.typesetter = typesetter
        }
    }

    private fun updateComposedFrame(layoutID: Any?, composedFrame: ComposedFrame?) {
        if (layoutID === properties.layoutID) {
            isComposedFrameResolved = true
            properties.composedFrame = composedFrame

            lineBoxes.clear()
            lineViews.clear()
            removeAllViews()

            scrollView?.scrollTo(0, 0)
            layoutLines()
        }
    }

    private fun updateLineBoxes(layoutID: Any?, resolvedBoxes: MutableList<Rect>) {
        if (layoutID === properties.layoutID) {
            lineBoxes = resolvedBoxes
            layoutLines()
        }
    }

    private fun layoutLines() = properties.composedFrame?.let {
        visibleRect.set(scrollX, scrollY, scrollX + scrollWidth, scrollY + scrollHeight)

        insideViews.clear()
        outsideViews.clear()

        // Get outside and inside line views.
        for (lineView in lineViews) {
            if (Rect.intersects(lineView.frame, visibleRect)) {
                insideViews.add(lineView)
            } else {
                outsideViews.add(lineView)
            }
        }

        visibleIndexes.clear()

        // Get line indexes that should be visible.
        for (i in 0 until lineBoxes.size) {
            if (Rect.intersects(lineBoxes[i], visibleRect)) {
                visibleIndexes.add(i)
            }
        }

        val allLines = it.lines

        // Layout the lines.
        for (index in visibleIndexes) {
            val textLine = allLines[index]
            var insideView: LineView? = null
            var lineView: LineView

            for (view in insideViews) {
                if (view.line === textLine) {
                    insideView = view
                    break
                }
            }

            if (insideView != null) {
                lineView = insideView
            } else {
                val outsideCount = outsideViews.size
                if (outsideCount > 0) {
                    lineView = outsideViews[outsideCount - 1]
                    outsideViews.removeAt(outsideCount - 1)
                } else {
                    lineView = LineView(context)
                    lineView.setBackgroundColor(Color.TRANSPARENT)

                    lineViews.add(lineView)
                }

                updateRenderer(lineView.renderer)
                lineView.line = textLine
            }

            if (lineView.parent == null) {
                addView(lineView)
            }
            lineView.bringToFront()

            val lineBox = lineBoxes[index]
            lineView.layout(lineBox.left, lineBox.top, lineBox.right, lineBox.bottom)
        }
    }

    private fun updateRenderer(renderer: Renderer) {
        renderer.fillColor = properties.textColor
        renderer.typeface = properties.typeface
        renderer.typeSize = properties.textSize
    }

    fun hitTestPosition(x: Float, y: Float): Int = properties.composedFrame?.let {
        val adjustedX = x - it.originX
        val adjustedY = y - it.originY
        val lineIndex = it.getLineIndexForPosition(adjustedX, adjustedY)

        val composedLine = it.lines[lineIndex]
        val lineLeft = composedLine.originX
        val lineRight = lineLeft + composedLine.width

        // Check if position exists within the line horizontally.
        if (adjustedX in lineLeft..lineRight) {
            var charIndex = composedLine.computeNearestCharIndex(adjustedX - lineLeft)
            val lastIndex = composedLine.charEnd - 1

            // Make sure to provide character of this line.
            if (charIndex > lastIndex) {
                charIndex = lastIndex
            }

            return charIndex
        }

        return -1
    } ?: -1

    private fun requestTypesetter() {
        isTypesetterResolved = isTypesetterUserDefined
        requestComposedFrame()
    }

    private fun requestComposedFrame() {
        isComposedFrameResolved = false
        requestTextLayout()
    }

    private fun requestTextLayout() {
        textTask?.cancel()

        properties.layoutID = Any()
        isTextLayoutRequested = true

        requestLayout()
    }

    fun setGravity(gravity: Int) {
        val horizontalGravity = gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK

        properties.textAlignment = when (horizontalGravity) {
            Gravity.LEFT -> TextAlignment.LEFT
            Gravity.RIGHT -> TextAlignment.RIGHT
            Gravity.CENTER_HORIZONTAL -> TextAlignment.CENTER
            Gravity.END -> TextAlignment.EXTRINSIC
            else -> TextAlignment.INTRINSIC
        }

        requestComposedFrame()
    }

    val composedFrame: ComposedFrame?
        get() = if (isComposedFrameResolved) properties.composedFrame else null

    var typesetter: Typesetter?
        get() = if (isTypesetterResolved) properties.typesetter else null
        set(typesetter) {
            properties.text = null
            properties.spanned = null
            properties.typesetter = typesetter
            isTypesetterUserDefined = true
            requestTypesetter()
        }

    var spanned: Spanned?
        get() = properties.spanned
        set(spanned) {
            properties.text = null
            properties.spanned = spanned
            isTypesetterUserDefined = false
            requestTypesetter()
        }

    var typeface: Typeface?
        get() = properties.typeface
        set(typeface) {
            properties.typeface = typeface
            requestTypesetter()
        }

    var text: String?
        get() = properties.text
        set(text) {
            properties.text = text ?: ""
            properties.spanned = null
            isTypesetterUserDefined = false
            requestTypesetter()
        }

    var textSize: Float
        get() = properties.textSize
        set(textSize) {
            properties.textSize = max(0.0f, textSize)
            requestTypesetter()
        }

    var textColor: Int
        get() = properties.textColor
        set(textColor) {
            properties.textColor = textColor
            invalidate()
        }

    var extraLineSpacing: Float
        get() = properties.extraLineSpacing
        set(extraLineSpacing) {
            properties.extraLineSpacing = extraLineSpacing
            requestComposedFrame()
        }

    var lineHeightMultiplier: Float
        get() = properties.lineHeightMultiplier
        set(lineHeightMultiplier) {
            properties.lineHeightMultiplier = lineHeightMultiplier
            requestComposedFrame()
        }

    var isJustificationEnabled: Boolean
        get() = properties.isJustificationEnabled
        set(isJustificationEnabled) {
            properties.isJustificationEnabled = isJustificationEnabled
            requestComposedFrame()
        }

    var justificationLevel: Float
        get() = properties.justificationLevel
        set(justificationLevel) {
            properties.justificationLevel = justificationLevel
            requestComposedFrame()
        }
}
