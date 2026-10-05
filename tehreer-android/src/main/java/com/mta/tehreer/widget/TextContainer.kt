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
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.annotation.VisibleForTesting
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.RenderingStyle
import com.mta.tehreer.graphics.StrokeCap
import com.mta.tehreer.graphics.StrokeJoin
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.util.SmartRunnable
import com.mta.tehreer.layout.ComposedFrame
import com.mta.tehreer.layout.ComposedLine
import com.mta.tehreer.layout.TextAlignment
import com.mta.tehreer.layout.Typesetter
import com.mta.tehreer.layout.style.ViewSpan
import java.util.ArrayDeque
import java.util.IdentityHashMap
import java.util.Queue
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

/** A character, and how far the top of the screen is below the top of its line. */
private class Anchor(val charIndex: Int, val offset: Float)

internal class TextContainer : ViewGroup {
    internal lateinit var properties: TextProperties

    private var displayedFrame: ComposedFrame? = null
    private var displayedLineBoxes = LineBoxes()

    private var pendingFrame: ComposedFrame? = null
    private var isSwapPending = false
    private var isPendingSwapFirstLoad = false

    private var scrollView: ScrollView? = null
    private var scrollWidth = 0
    private var scrollHeight = 0

    internal val visibleRect = Rect()
    internal val originInScrollView = IntArray(2)

    private var isTextLayoutRequested = false
    internal var isTypesetterUserDefined = false
        private set
    private var isTypesetterResolved = false
    private var isComposedFrameResolved = false

    private var lineViewsByIndex = arrayOfNulls<LineView>(0)
    private val attachedLineViews = ArrayList<LineView>()
    private val reusableLineViews = ArrayList<LineView>()

    internal var viewSlots: List<ViewSlot> = emptyList()
        private set

    internal val spanViews = IdentityHashMap<ViewSpan, View>()
    internal val measuredViews = IdentityHashMap<ViewSpan, View>()
    internal val resizingSpans = IdentityHashMap<ViewSpan, Boolean>()
    internal var isFrameFresh = false

    /** How far from the visible part of the container, in px, a view is already made and attached. */
    var viewSpanPrefetchDistance = 0
        set(value) {
            val distance = value.coerceAtLeast(0)

            if (field != distance) {
                field = distance
                layoutLines()
            }
        }

    private var pendingAnchor: Anchor? = null
    private var isTextNew = true
    private var pendingScrollCharIndex = -1

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

    fun onScrollViewScrolled() {
        layoutLines()
    }

    fun setVisibleRegion(width: Int, height: Int) {
        if (scrollWidth != width) {
            scrollWidth = width
            requestComposedFrame()
        }

        scrollHeight = height
    }

    /**
     * Finds the top-left corner of this container in the coordinates of the scroll view, which
     * is what `MotionEvent` uses, by walking up through every ancestor between the two.
     */
    internal fun locateInScrollView(out: IntArray): Boolean {
        val root = scrollView ?: return false

        var x = 0
        var y = 0
        var view: View = this

        while (true) {
            val parent = view.parent as? View ?: return false

            x += view.left - parent.scrollX
            y += view.top - parent.scrollY

            if (parent === root) {
                break
            }

            view = parent
        }

        out[0] = x
        out[1] = y

        return true
    }

    internal fun updateVisibleRect() {
        val root = scrollView

        if (root != null && locateInScrollView(originInScrollView)) {
            val clipsToPadding = root.clipToPadding

            val left = if (clipsToPadding) root.paddingLeft else 0
            val top = if (clipsToPadding) root.paddingTop else 0
            val right = root.width - if (clipsToPadding) root.paddingRight else 0
            val bottom = root.height - if (clipsToPadding) root.paddingBottom else 0

            val dx = originInScrollView[0]
            val dy = originInScrollView[1]

            visibleRect.set(left - dx, top - dy, right - dx, bottom - dy)
        } else {
            visibleRect.set(0, 0, scrollWidth, scrollHeight)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = 0

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = 0
        }

        displayedFrame?.let {
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

        applyPendingAnchor()
        layoutLines()
    }

    private fun performTextLayout() {
        measureViewSpans()

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

    /**
     * The frame resolves well before its line boxes, and is only displayed once they are ready,
     * so a line view never appears before its own text.
     */
    private fun updateComposedFrame(layoutID: Any?, composedFrame: ComposedFrame?) {
        if (layoutID !== properties.layoutID) {
            return
        }

        isComposedFrameResolved = true

        if (isTextNew) {
            pendingFrame = composedFrame
            isSwapPending = true
            isPendingSwapFirstLoad = true

            isTextNew = false

            if (pendingScrollCharIndex >= 0) {
                pendingAnchor = Anchor(pendingScrollCharIndex, Float.NaN)
            }
            pendingScrollCharIndex = -1
        } else {
            pendingAnchor = pendingAnchor ?: captureAnchor()
            pendingFrame = composedFrame
            isPendingSwapFirstLoad = isSwapPending && isPendingSwapFirstLoad
            isSwapPending = true
        }
    }

    private fun updateLineBoxes(layoutID: Any?, lineBoxes: LineBoxes) {
        if (layoutID !== properties.layoutID) {
            return
        }

        displayedLineBoxes = lineBoxes

        if (!isSwapPending) {
            layoutLines()
            return
        }

        val isFirstLoad = isPendingSwapFirstLoad

        isSwapPending = false
        isPendingSwapFirstLoad = false

        displayedFrame = pendingFrame
        pendingFrame = null

        recycleLineViews()

        viewSlots = displayedFrame?.let { resolveViewSlots(it) } ?: emptyList()
        detachOrphanViews()
        isFrameFresh = true

        if (isFirstLoad) {
            scrollView?.scrollTo(0, 0)
        }

        layoutLines()

        if (pendingAnchor != null) {
            requestLayout()
        }
    }

    private fun markTextNew() {
        isTextNew = true
        pendingScrollCharIndex = -1
        pendingAnchor = null

        pendingFrame = null
        isSwapPending = false
        isPendingSwapFirstLoad = false
    }

    private fun requestTypesetter() {
        isTypesetterResolved = isTypesetterUserDefined
        requestComposedFrame()
    }

    internal fun requestComposedFrame() {
        isComposedFrameResolved = false
        requestTextLayout()
    }

    private fun requestTextLayout() {
        textTask?.cancel()

        properties.layoutID = Any()
        isTextLayoutRequested = true

        requestLayout()
    }

    private fun layoutLines() {
        val frame = displayedFrame ?: return
        updateVisibleRect()

        val lines = frame.lines
        val lineBoxes = displayedLineBoxes
        val boxes = lineBoxes.boxes

        var attachedIndex = attachedLineViews.size - 1

        while (attachedIndex >= 0) {
            val lineView = attachedLineViews[attachedIndex]

            if (!Rect.intersects(boxes[lineView.lineIndex], visibleRect)) {
                lineViewsByIndex[lineView.lineIndex] = null

                val lastIndex = attachedLineViews.size - 1
                attachedLineViews[attachedIndex] = attachedLineViews[lastIndex]
                attachedLineViews.removeAt(lastIndex)

                enqueueReusableLineView(lineView)
            }

            attachedIndex -= 1
        }

        lineBoxes.forEachLineIndex(visibleRect) { lineIndex ->
            if (lineViewsByIndex[lineIndex] == null) {
                val lineView = dequeueReusableLineView()
                lineView.lineIndex = lineIndex
                lineView.line = lines[lineIndex]
                configure(lineView)

                val box = boxes[lineIndex]
                lineView.layout(box.left, box.top, box.right, box.bottom)

                if (lineView.parent == null) {
                    addView(lineView, 0)
                }

                lineViewsByIndex[lineIndex] = lineView
                attachedLineViews.add(lineView)
            }
        }

        layoutViews()
    }

    private fun recycleLineViews() {
        reusableLineViews.addAll(attachedLineViews)
        attachedLineViews.clear()
        lineViewsByIndex = arrayOfNulls(displayedFrame?.lines?.size ?: 0)

        for (lineView in reusableLineViews) {
            removeViewInLayout(lineView)
        }

        requestLayout()
        invalidate()
    }

    private fun dequeueReusableLineView(): LineView {
        val lastIndex = reusableLineViews.size - 1

        if (lastIndex >= 0) {
            return reusableLineViews.removeAt(lastIndex)
        }

        return LineView(context).also { it.setBackgroundColor(Color.TRANSPARENT) }
    }

    private fun enqueueReusableLineView(lineView: LineView) {
        reusableLineViews.add(lineView)
    }

    private fun configure(lineView: LineView) {
        updateRenderer(lineView.renderer)
        lineView.layoutWidth = properties.layoutWidth.toFloat()
        lineView.separatorColor = properties.separatorColor
    }

    private fun updateRenderer(renderer: Renderer) = properties.updateRenderer(renderer)

    private fun updateLineViews() {
        for (lineView in attachedLineViews) {
            configure(lineView)
            lineView.invalidate()
        }
    }

    private fun lineTop(frame: ComposedFrame, line: ComposedLine) =
        frame.originY + line.originY - line.ascent

    private fun lineBottom(frame: ComposedFrame, line: ComposedLine) =
        frame.originY + line.originY + line.descent + line.leading

    /** The first line that reaches below [top], or the last one if it is further up. */
    private fun firstVisibleLineIndex(frame: ComposedFrame, top: Float = visibleRect.top.toFloat()): Int {
        val lines = frame.lines
        val lineBoxes = displayedLineBoxes

        val lineIndex = lineBoxes.firstLineIndex(floor(top).toInt()) {
            lineBottom(frame, lines[it]) > top
        }

        if (lineIndex >= 0) {
            return lineIndex
        }

        for (unboxedIndex in lineBoxes.size until lines.size) {
            if (lineBottom(frame, lines[unboxedIndex]) > top) {
                return unboxedIndex
            }
        }

        return lines.size - 1
    }

    private fun captureAnchor(): Anchor? {
        val frame = displayedFrame ?: return null
        if (frame.lines.isEmpty()) {
            return null
        }

        updateVisibleRect()

        val line = frame.lines[firstVisibleLineIndex(frame)]
        return Anchor(line.charStart, visibleRect.top - lineTop(frame, line))
    }

    private fun scrollToAnchor(frame: ComposedFrame, anchor: Anchor, animate: Boolean) {
        val root = scrollView ?: return
        if (frame.lines.isEmpty()) {
            return
        }

        updateVisibleRect()

        val charIndex = anchor.charIndex.coerceIn(frame.charStart, frame.charEnd - 1)
        val line = frame.lines[frame.getLineIndexForChar(charIndex)]

        val offset = if (anchor.offset.isNaN()) {
            if (root.clipToPadding) 0.0f else -root.paddingTop.toFloat()
        } else {
            anchor.offset
        }

        val distance = (lineTop(frame, line) + offset - visibleRect.top).roundToInt()

        if (animate) {
            root.smoothScrollBy(0, distance)
        } else {
            root.scrollBy(0, distance)
        }
    }

    private fun applyPendingAnchor() {
        if (isSwapPending) {
            return
        }

        val anchor = pendingAnchor ?: return
        val frame = if (isComposedFrameResolved) displayedFrame else return

        pendingAnchor = null

        if (frame != null) {
            scrollToAnchor(frame, anchor, false)
        }
    }

    /** The first character that is on the screen, or -1 if no text is displayed. */
    fun getFirstVisibleCharIndex(): Int {
        val frame = composedFrame ?: return -1
        if (frame.lines.isEmpty()) {
            return -1
        }

        updateVisibleRect()

        val padding = if (scrollView?.clipToPadding == false) scrollView?.paddingTop ?: 0 else 0

        return frame.lines[firstVisibleLineIndex(frame, visibleRect.top + padding + 0.5f)].charStart
    }

    /**
     * Scrolls to the line with [charIndex]. If the text is not displayed yet, it is done as soon as
     * it is, in place of the scroll to the top that a new text gets.
     */
    fun scrollToCharIndex(charIndex: Int, animate: Boolean) {
        val anchor = Anchor(charIndex.coerceAtLeast(0), Float.NaN)
        val frame = displayedFrame

        if (isSwapPending || (frame != null && !isComposedFrameResolved)) {
            pendingAnchor = anchor
            requestLayout()
        } else if (frame != null) {
            scrollToAnchor(frame, anchor, animate)
        } else {
            pendingScrollCharIndex = anchor.charIndex
        }
    }

    /** The source that the frame is made of. */
    internal val frameSpanned: Spanned?
        get() = properties.typesetter?.spanned ?: properties.spanned

    fun onSpanResizeRequested(span: ViewSpan) {
        if (viewSlots.none { it.span === span }) {
            return
        }

        if (span.isMeasured) {
            (spanViews[span] ?: measuredViews[span])?.let { forceLayoutTree(it) }

            if (!measureViewSpan(span)) {
                return
            }
        }

        spanViews[span]?.let {
            updateVisibleRect()

            if (span.hideWhileResizing && Rect.intersects(Rect(it.left, it.top, it.right, it.bottom), visibleRect)) {
                it.visibility = INVISIBLE
                resizingSpans[span] = true
            }
        }

        requestComposedFrame()
    }

    /** Whether the point, in the coordinates of the scroll view, is on the view of a span. */
    fun isInsideViewSpan(x: Float, y: Float): Boolean {
        if (spanViews.isEmpty() || !locateInScrollView(originInScrollView)) {
            return false
        }

        val localX = x - originInScrollView[0]
        val localY = y - originInScrollView[1]

        return spanViews.values.any {
            localX >= it.left && localX < it.right && localY >= it.top && localY < it.bottom
        }
    }

    @VisibleForTesting
    internal fun lineBoxesForTesting(): List<Rect> = displayedLineBoxes.boxes

    @VisibleForTesting
    internal fun visibleRectForTesting(): Rect {
        updateVisibleRect()
        return Rect(visibleRect)
    }

    /**
     * Replaces the line boxes that [layoutLines] culls against, and invalidates the layout ID so
     * that a real [LineBoxesTask] chunk still in flight cannot land afterward and clobber them.
     */
    @VisibleForTesting
    internal fun overrideLineBoxesForTesting(boxes: List<Rect>) {
        properties.layoutID = Any()
        displayedLineBoxes = LineBoxes().also { lineBoxes -> boxes.forEach { lineBoxes.add(it) } }

        recycleLineViews()
        layoutLines()
    }

    /**
     * Returns the char index for [x], [y] given in the coordinates of the scroll view (scroll
     * applied): the one of the nearest line, found even above the first line or below the last
     * one. It is -1 only if the point is on the left or on the right of the text of that line.
     */
    fun getCharIndexForPosition(x: Float, y: Float): Int {
        val frame = composedFrame ?: return -1
        if (frame.lines.isEmpty() || !locateInScrollView(originInScrollView)) {
            return -1
        }

        val frameX = x - originInScrollView[0] - frame.originX
        val frameY = y - originInScrollView[1] - frame.originY

        // The first line that does not end above the position, so that a point above the first
        // line, or in a gap between two lines, is not taken for one below the last line.
        val line = frame.lines.firstOrNull { frameY <= it.originY + it.descent + it.leading }
            ?: frame.lines.last()

        return getCharIndexInLine(line, frameX)
    }

    /**
     * Returns the char index for [x], [y] given in the coordinates of the scroll view, or -1 if
     * there is no text there: above the first line or below the last one, in the gaps to the left
     * and right of a line, or in the padding. It is what a touch is matched with.
     */
    fun getCharIndexUnderPosition(x: Float, y: Float): Int {
        val frame = composedFrame ?: return -1
        if (!locateInScrollView(originInScrollView)) {
            return -1
        }

        val frameX = x - originInScrollView[0] - frame.originX
        val frameY = y - originInScrollView[1] - frame.originY

        val line = frame.lines.firstOrNull {
            val lineTop = it.originY - it.ascent
            val lineBottom = it.originY + it.descent + it.leading

            frameY in lineTop..lineBottom
        } ?: return -1

        val nearestIndex = getCharIndexInLine(line, frameX)
        if (nearestIndex < 0) {
            return -1
        }

        // The index is the nearest to the position, which is the one after the char when the
        // position is on the trailing half of it. The char is the one whose box has it.
        for (charIndex in intArrayOf(nearestIndex, nearestIndex - 1)) {
            if (charIndex < line.charStart) {
                continue
            }

            val rects = computeSelectionRects(frame, charIndex, charIndex + 1)
            if (rects.any { it.contains(frameX + frame.originX, frameY + frame.originY) }) {
                return charIndex
            }
        }

        return -1
    }

    private fun getCharIndexInLine(line: ComposedLine, frameX: Float): Int {
        val lineLeft = line.originX
        val lineRight = lineLeft + line.width

        // Check if position exists within the line horizontally.
        if (frameX < lineLeft || frameX > lineRight) {
            return -1
        }

        // Make sure to provide character of this line.
        return line.computeNearestCharIndex(frameX - lineLeft).coerceAtMost(line.charEnd - 1)
    }

    /** The rects covered by [span] in the coordinates of the scroll view; see TTextView. */
    fun getSpanRects(span: Any): List<RectF> {
        val frame = composedFrame ?: return emptyList()
        val source = typesetter?.spanned ?: spanned ?: return emptyList()

        if (!locateInScrollView(originInScrollView)) {
            return emptyList()
        }

        val dx = originInScrollView[0].toFloat()
        val dy = originInScrollView[1].toFloat()

        return computeSpanRects(frame, source, span).onEach { it.offset(dx, dy) }
    }

    /** The rects covered by the chars from [start] up to [end] in the coordinates of the scroll view. */
    fun getSelectionRects(start: Int, end: Int): List<RectF> {
        val frame = composedFrame ?: return emptyList()

        if (!locateInScrollView(originInScrollView)) {
            return emptyList()
        }

        val dx = originInScrollView[0].toFloat()
        val dy = originInScrollView[1].toFloat()

        return computeSelectionRects(frame, start, end).onEach { it.offset(dx, dy) }
    }

    /** The spanned that the displayed frame was made from, or null while it is not resolved. */
    val frameSource: Spanned?
        get() = if (composedFrame != null) typesetter?.spanned ?: spanned else null

    /** Redraws the lines, which are child views that draw themselves. */
    fun invalidateLines() {
        for (lineView in attachedLineViews) {
            lineView.invalidate()
        }
    }

    fun setGravity(gravity: Int) {
        val horizontalGravity = gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK

        properties.textAlignment = when (horizontalGravity) {
            Gravity.LEFT -> TextAlignment.LEFT
            Gravity.RIGHT -> TextAlignment.RIGHT
            Gravity.CENTER_HORIZONTAL -> TextAlignment.CENTER
            Gravity.END -> TextAlignment.TRAILING
            else -> TextAlignment.LEADING
        }

        requestComposedFrame()
    }

    val composedFrame: ComposedFrame?
        get() = if (isComposedFrameResolved) displayedFrame else null

    var typesetter: Typesetter?
        get() = if (isTypesetterResolved) properties.typesetter else null
        set(typesetter) {
            properties.text = null
            properties.spanned = null
            properties.typesetter = typesetter
            isTypesetterUserDefined = true
            markTextNew()
            requestTypesetter()
        }

    var spanned: Spanned?
        get() = properties.spanned
        set(spanned) {
            properties.text = null
            properties.spanned = spanned
            isTypesetterUserDefined = false
            markTextNew()
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
            markTextNew()
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
            updateLineViews()
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

    var separatorColor: Int
        get() = properties.separatorColor
        set(separatorColor) {
            properties.separatorColor = separatorColor
            updateLineViews()
        }

    var renderingStyle: RenderingStyle
        get() = properties.renderingStyle
        set(renderingStyle) {
            properties.renderingStyle = renderingStyle
            updateLineViews()
        }

    var strokeColor: Int
        get() = properties.strokeColor
        set(strokeColor) {
            properties.strokeColor = strokeColor
            updateLineViews()
        }

    var strokeWidth: Float
        get() = properties.strokeWidth
        set(strokeWidth) {
            properties.strokeWidth = max(0.0f, strokeWidth)
            updateLineViews()
        }

    var strokeCap: StrokeCap
        get() = properties.strokeCap
        set(strokeCap) {
            properties.strokeCap = strokeCap
            updateLineViews()
        }

    var strokeJoin: StrokeJoin
        get() = properties.strokeJoin
        set(strokeJoin) {
            properties.strokeJoin = strokeJoin
            updateLineViews()
        }

    var strokeMiter: Float
        get() = properties.strokeMiter
        set(strokeMiter) {
            properties.strokeMiter = strokeMiter
            updateLineViews()
        }
}
