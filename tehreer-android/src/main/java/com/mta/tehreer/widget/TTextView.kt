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

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.text.Spanned
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.widget.ScrollView
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.Px
import com.mta.tehreer.R
import com.mta.tehreer.graphics.RenderingStyle
import com.mta.tehreer.graphics.StrokeCap
import com.mta.tehreer.graphics.StrokeJoin
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.graphics.TypefaceManager
import com.mta.tehreer.layout.ComposedFrame
import com.mta.tehreer.layout.Typesetter
import com.mta.tehreer.layout.style.ViewSpan
import kotlin.math.roundToInt

/**
 * A scrollable, multiline text region.
 *
 * The standard padding of a view applies to the text, and, unlike a plain `ScrollView`,
 * `clipToPadding` is `false` by default so that the text scrolls through the padding rather than
 * being cut off at its edge. Set `android:clipToPadding` or call [setClipToPadding] to change it.
 *
 * Clickable spans (including `URLSpan`) in the text respond to a tap, and any span can be reported
 * to an [OnSpanClickListener]. The position of a touch is found with [getCharIndexForPosition], and
 * the place a span occupies with [getSpanBounds] and [getSpanRects]; all three work in the
 * coordinates of this view, which are the ones a `MotionEvent` uses.
 *
 * A [ViewSpan] in the text puts a real view in it.
 */
open class TTextView : ScrollView {
    /**
     * Interface definition for a callback to be invoked when a span of the text is clicked.
     */
    fun interface OnSpanClickListener {
        /**
         * Called when a span is clicked. The spans that can be clicked are the clickable spans
         * (unless [linksClickable] is turned off) and the replacement spans, such as inline
         * images. A link under a replacement span is not clickable.
         *
         * @param view The text view containing the span.
         * @param span The span that was clicked.
         * @return `true` if the click was consumed. Otherwise, a clickable span will handle the
         *         click itself, by calling its `onClick()`.
         */
        fun onSpanClick(view: TTextView, span: Any): Boolean
    }

    private lateinit var textContainer: TextContainer
    private lateinit var spanTouchHandler: SpanTouchHandler

    /**
     * The listener that is called when a span is clicked, or `null` if there is none. It is called
     * before the click is handled by a clickable span, which can therefore be intercepted, and it
     * is the only way to know about a click on a replacement span, such as an inline image.
     */
    var onSpanClickListener: OnSpanClickListener? = null

    /**
     * Whether the clickable spans of the text, including `URLSpan`, respond to a tap. When they do,
     * a link is highlighted while it is pressed and its `onClick()` is called (after the
     * [OnSpanClickListener]) when the finger is lifted. A `URLSpan` opens its URL. The default
     * value is `true`.
     */
    var linksClickable = true
        set(value) {
            field = value

            if (!value) {
                spanTouchHandler.cancel()
            }
        }

    /**
     * The color that a link is highlighted with while it is pressed. It is drawn behind the text,
     * so it should be translucent. The default value is the `textColorHighlight` of the theme, or a
     * translucent blue if there is none.
     */
    @get:ColorInt
    @setparam:ColorInt
    var highlightColor = DEFAULT_HIGHLIGHT_COLOR
        set(value) {
            field = value
            invalidateHighlight()
        }

    constructor(context: Context) : super(context) {
        setup(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr) {
        setup(context, attrs, defStyleAttr, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
        super(context, attrs, defStyleAttr, defStyleRes) {
        setup(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun setup(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        textContainer = TextContainer(context)
        textContainer.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        textContainer.setScrollView(this)
        addView(textContainer)

        spanTouchHandler = SpanTouchHandler(this)

        // The text scrolls through the padding, so the padding is not a clip region unless it is
        // asked to be.
        val clipValues = context.obtainStyledAttributes(attrs, CLIP_TO_PADDING_ATTRS, defStyleAttr, defStyleRes)
        try {
            if (!clipValues.hasValue(0)) {
                clipToPadding = false
            }
        } finally {
            clipValues.recycle()
        }

        val highlightValue = TypedValue()
        if (context.theme.resolveAttribute(android.R.attr.textColorHighlight, highlightValue, true)
            && highlightValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
            && highlightValue.type <= TypedValue.TYPE_LAST_COLOR_INT
        ) {
            highlightColor = highlightValue.data
        }

        val values = context.theme.obtainStyledAttributes(attrs, R.styleable.TTextView, defStyleAttr, defStyleRes)
        try {
            applyAttributes(values)
        } finally {
            values.recycle()
        }
    }

    private fun applyAttributes(values: TypedArray) {
        setGravity(values.getInt(R.styleable.TTextView_gravity, Gravity.TOP or Gravity.START))
        extraLineSpacing = values.getDimension(R.styleable.TTextView_extraLineSpacing, 0.0f)
        lineHeightMultiplier = values.getFloat(R.styleable.TTextView_lineHeightMultiplier, 0.0f)
        textColor = values.getColor(R.styleable.TTextView_textColor, Color.BLACK)
        textSize = values.getDimension(R.styleable.TTextView_textSize, 16f)
        if (values.hasValue(R.styleable.TTextView_typeface)) {
            typeface = TypefaceManager.getTypeface(values.getResourceId(R.styleable.TTextView_typeface, 0))
        }

        renderingStyle = when (values.getInt(R.styleable.TTextView_renderingStyle, 0)) {
            1 -> RenderingStyle.FILL_STROKE
            2 -> RenderingStyle.STROKE
            else -> RenderingStyle.FILL
        }
        strokeColor = values.getColor(R.styleable.TTextView_strokeColor, Color.BLACK)
        strokeWidth = values.getDimension(R.styleable.TTextView_strokeWidth, 0.0f)
        strokeCap = when (values.getInt(R.styleable.TTextView_strokeCap, 0)) {
            1 -> StrokeCap.ROUND
            2 -> StrokeCap.SQUARE
            else -> StrokeCap.BUTT
        }
        strokeJoin = when (values.getInt(R.styleable.TTextView_strokeJoin, 0)) {
            0 -> StrokeJoin.BEVEL
            1 -> StrokeJoin.MITER
            else -> StrokeJoin.ROUND
        }
        strokeMiter = values.getDimension(R.styleable.TTextView_strokeMiter, 1.0f)

        val text = values.getText(R.styleable.TTextView_text)
        if (text is Spanned) {
            spanned = text
        } else if (text != null) {
            this.text = text.toString()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        textContainer.setVisibleRegion(w, h)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        // The viewport may have changed (its size, its padding) without the text container being
        // laid out again.
        textContainer.onScrollViewScrolled()
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        textContainer.onScrollViewScrolled()
    }

    override fun onDetachedFromWindow() {
        spanTouchHandler.cancel()
        super.onDetachedFromWindow()
    }

    /**
     * Redraws this view, and with it the lines of text, which are child views that draw themselves.
     * A span that draws differently after a change of its own (a bitmap that is hidden, say) is
     * shown as changed by calling this method.
     */
    override fun invalidate() {
        super.invalidate()

        // It can be called by the super constructors.
        if (this::textContainer.isInitialized) {
            textContainer.invalidateLines()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        // Behind the text, so that it stays readable.
        spanTouchHandler.drawHighlight(canvas, highlightColor)
        super.dispatchDraw(canvas)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val intercepted = super.onInterceptTouchEvent(ev)

        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            // The scroll view takes the touch that stops a fling; that is not a tap on a span.
            spanTouchHandler.onTouchStarted(intercepted)
        }

        return intercepted
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // The touch is only observed, never taken away from the scroll view.
        val pressed = spanTouchHandler.onTouchEvent(ev)

        return super.onTouchEvent(ev) || pressed
    }

    /**
     * Only redraws this view, not the lines of text; that is enough for the pressed state of a
     * link, which is drawn behind them.
     */
    internal fun invalidateHighlight() {
        super.invalidate()
    }

    /**
     * Returns whether the point, in the coordinates of this view, is on the view of a [ViewSpan].
     */
    internal fun isInsideViewSpan(x: Float, y: Float): Boolean {
        return textContainer.isInsideViewSpan(x, y)
    }

    /**
     * Returns the spanned that the displayed lines were made from, whether it was set directly or
     * is the source of the typesetter. It is `null` until the lines are displayed.
     */
    internal val sourceSpanned: Spanned?
        get() = textContainer.frameSource

    /**
     * Returns the index of the character at the specified position: the one of the nearest line,
     * which is found even above the first line or below the last one. It returns -1 if there is no
     * text, or if the position is on the left or on the right of the text of that line.
     *
     * The position is in the coordinates of this view, which is what `MotionEvent.getX()` and
     * `getY()` give; scroll, padding and the place of the text inside this view are taken care of.
     *
     * @param x The x- coordinate of position in this view.
     * @param y The y- coordinate of position in this view.
     * @return The index of the character at the specified position, or -1 if there is no character
     *         there.
     */
    fun getCharIndexForPosition(x: Float, y: Float): Int {
        return textContainer.getCharIndexForPosition(x, y)
    }

    /**
     * Returns the index of the character that is exactly under the position, or -1 if there is no
     * text there: above the first line or below the last one, in the gaps to the left and right of
     * a line, or in the padding.
     */
    internal fun getCharIndexUnderPosition(x: Float, y: Float): Int {
        return textContainer.getCharIndexUnderPosition(x, y)
    }

    /**
     * Returns the index of the first character of the first line that is on the screen, or -1 if
     * no text is displayed. Together with [scrollToCharIndex] it saves and restores the place where
     * the reader is.
     *
     * When the lines are made again, because of a change of the width, of the size of the text, of
     * the line spacing or of the size of a [ViewSpan], the text view keeps what is on the screen
     * where it is on its own. A new text, set with [text], [spanned] or [typesetter], starts at the
     * top.
     *
     * @return The index of the first visible character.
     */
    val firstVisibleCharIndex: Int
        get() = textContainer.getFirstVisibleCharIndex()

    /**
     * Scrolls so that the line with the specified character is at the top of the text. If the
     * text is not displayed yet, as it is not right after it was set, the scroll is done as soon
     * as it is, instead of the scroll to the top that a new text gets.
     *
     * @param charIndex The index of a character.
     * @param animate Whether to scroll smoothly. It is ignored while the text is not displayed.
     */
    fun scrollToCharIndex(charIndex: Int, animate: Boolean) {
        textContainer.scrollToCharIndex(charIndex, animate)
    }

    /**
     * Returns the rectangles that the chars from `start` up to `end` cover, one for each line they
     * occupy, in the coordinates of this view (scroll applied, so they can be compared with touch
     * positions). They follow the convention of a text selection: when the range continues over
     * several lines, the first rectangle goes on to the edge of the text, and the last one starts
     * from the other edge.
     *
     * The rectangles are returned even if they are scrolled out of the view. An empty list is
     * returned when the range is not in the displayed text or the text is not laid out yet.
     *
     * @param start The index of the first char of the range.
     * @param end The index after the last char of the range.
     * @return The rectangles that the range covers, in the order of the lines.
     */
    fun getSelectionRects(start: Int, end: Int): List<RectF> {
        return textContainer.getSelectionRects(start, end)
    }

    /**
     * Returns the rectangles that the specified span covers, one for each line it occupies, in
     * the coordinates of this view (scroll applied, so they can be compared with touch positions).
     * The rectangles of a span that continues over several lines follow the convention of a text
     * selection: the first goes on to the edge of the text, and the last starts from the other
     * edge. A replacement span (an inline image) has the box that it draws in.
     *
     * The rectangles are returned even if they are scrolled out of the view. An empty list is
     * returned when the span is not in the displayed text or the text is not laid out yet.
     *
     * @param span A span of the text being displayed.
     * @return The rectangles that the span covers, in the order of the lines.
     */
    fun getSpanRects(span: Any): List<RectF> {
        return textContainer.getSpanRects(span)
    }

    /**
     * Returns the smallest rectangle, rounded to whole pixels, that covers the specified span in
     * the coordinates of this view (scroll applied).
     *
     * @param span A span of the text being displayed.
     * @return The bounds of the span, or `null` if the span is not in the displayed text or the
     *         text is not laid out yet.
     *
     * @see getSpanRects
     */
    fun getSpanBounds(span: Any): Rect? {
        val rects = getSpanRects(span)
        if (rects.isEmpty()) {
            return null
        }

        val union = RectF(rects[0])
        for (i in 1 until rects.size) {
            union.union(rects[i])
        }

        return Rect(
            union.left.roundToInt(), union.top.roundToInt(),
            union.right.roundToInt(), union.bottom.roundToInt()
        )
    }

    /**
     * How far from the screen, in pixels, the view of a [ViewSpan] is already made and attached.
     * The default value is 0.
     *
     * It sets how far above and below the screen, in pixels, the view of a [ViewSpan] is made,
     * attached and laid out, rather than when its line reaches the screen. It is what makes a view
     * that needs some time to be ready (a `ComposeView` is composed once it is attached, and tells
     * its size after that) ready before the reader scrolls to it, so that it does not appear after
     * a blank; and a picture that a view loads is there earlier. The views further than that are
     * not kept (unless the span retains its view), so the distance decides how many views exist at
     * a time. The default value is 0, which is the screen only. Negative values are taken as 0.
     */
    var viewSpanPrefetchDistance: Int
        get() = textContainer.viewSpanPrefetchDistance
        set(value) {
            textContainer.viewSpanPrefetchDistance = value
        }

    /**
     * Sets the horizontal alignment of the text. Only the horizontal part of the gravity is used:
     * the text always starts at the top, as it scrolls.
     *
     * @param gravity The horizontal alignment.
     */
    fun setGravity(gravity: Int) {
        textContainer.setGravity(gravity)
    }

    /**
     * Returns the current composed frame that is being displayed.
     *
     * @return The composed frame being displayed.
     */
    val composedFrame: ComposedFrame?
        get() = textContainer.composedFrame

    /**
     * The typesetter that is being used to compose text lines. Setting it will make text and
     * spanned properties `null`.
     *
     * A typesetter is preferred over spanned as it avoids an extra step of creating typesetter
     * from spanned.
     *
     * @see text
     * @see spanned
     */
    var typesetter: Typesetter?
        get() = textContainer.typesetter
        set(value) {
            textContainer.typesetter = value
        }

    /**
     * The spanned that is being displayed. This property will be `null` if either text or
     * typesetter is being used instead. Setting it will make text property `null`.
     *
     * If performance is required, a typesetter should be used directly.
     *
     * @see typesetter
     * @see text
     */
    var spanned: Spanned?
        get() = textContainer.spanned
        set(value) {
            textContainer.spanned = value
        }

    /**
     * The typeface in which the text is being displayed.
     */
    var typeface: Typeface?
        get() = textContainer.typeface
        set(value) {
            textContainer.typeface = value
        }

    /**
     * The text that is being displayed. This property will be `null` if either spanned or
     * typesetter is being used instead. Setting it will make spanned property `null`.
     *
     * @see typesetter
     * @see spanned
     */
    var text: String?
        get() = textContainer.text
        set(value) {
            textContainer.text = value
        }

    /**
     * The text size (in pixels) in which the text is being displayed.
     */
    var textSize: Float
        get() = textContainer.textSize
        set(value) {
            textContainer.textSize = value
        }

    /**
     * The color in which the text is being displayed.
     */
    @get:ColorInt
    @setparam:ColorInt
    var textColor: Int
        get() = textContainer.textColor
        set(value) {
            textContainer.textColor = value
        }

    /**
     * The extra spacing in pixels that should be added after each text line. It is resolved before
     * line height multiplier. The default value is zero.
     *
     * @see lineHeightMultiplier
     */
    var extraLineSpacing: Float
        get() = textContainer.extraLineSpacing
        set(value) {
            textContainer.extraLineSpacing = value
        }

    /**
     * The height multiplier that should be applied on each text line. It is resolved after extra
     * line spacing. The default value is one.
     *
     * The additional spacing is adjusted in such a way that text remains in the middle of the line.
     *
     * @see extraLineSpacing
     */
    var lineHeightMultiplier: Float
        get() = textContainer.lineHeightMultiplier
        set(value) {
            textContainer.lineHeightMultiplier = value
        }

    /**
     * Whether or not to justify the text lines. The default value is `false`.
     */
    var isJustificationEnabled: Boolean
        get() = textContainer.isJustificationEnabled
        set(value) {
            textContainer.isJustificationEnabled = value
        }

    /**
     * The justification level which can range from 0.0 to 1.0. A lower value increases the
     * tightness between words while a higher value decreases it. The default value is `1.0f`.
     */
    @get:FloatRange(from = 0.0, to = 1.0)
    @set:FloatRange(from = 0.0, to = 1.0)
    var justificationLevel: Float
        get() = textContainer.justificationLevel
        set(value) {
            textContainer.justificationLevel = value
        }

    /**
     * The color being used to display a separator line below each rendered text line. The default
     * value is `Color.TRANSPARENT`.
     */
    @get:ColorInt
    @setparam:ColorInt
    var separatorColor: Int
        get() = textContainer.separatorColor
        set(value) {
            textContainer.separatorColor = value
        }

    /**
     * The rendering style, used for controlling how text should appear while drawing. The default
     * value is [RenderingStyle.FILL].
     */
    var renderingStyle: RenderingStyle
        get() = textContainer.renderingStyle
        set(value) {
            textContainer.renderingStyle = value
        }

    /**
     * The stroke color for text, expressed as ARGB integer. The default value is `Color.BLACK`.
     */
    @get:ColorInt
    @setparam:ColorInt
    var strokeColor: Int
        get() = textContainer.strokeColor
        set(value) {
            textContainer.strokeColor = value
        }

    /**
     * The stroke width in pixels for text.
     */
    @get:Px
    @setparam:Px
    var strokeWidth: Float
        get() = textContainer.strokeWidth
        set(value) {
            textContainer.strokeWidth = value
        }

    /**
     * The cap, controlling how the start and end of stroked lines and paths are treated. The
     * default value is [StrokeCap.BUTT].
     */
    var strokeCap: StrokeCap
        get() = textContainer.strokeCap
        set(value) {
            textContainer.strokeCap = value
        }

    /**
     * The stroke join type for text. The default value is [StrokeJoin.ROUND].
     */
    var strokeJoin: StrokeJoin
        get() = textContainer.strokeJoin
        set(value) {
            textContainer.strokeJoin = value
        }

    /**
     * The stroke miter limit in pixels. This is used to control the behavior of miter joins when
     * the joins angle is sharp. The default value is 1.
     */
    @get:Px
    @setparam:Px
    var strokeMiter: Float
        get() = textContainer.strokeMiter
        set(value) {
            textContainer.strokeMiter = value
        }

    private companion object {
        val CLIP_TO_PADDING_ATTRS = intArrayOf(android.R.attr.clipToPadding)
        const val DEFAULT_HIGHLIGHT_COLOR = 0x6633B5E5
    }
}
