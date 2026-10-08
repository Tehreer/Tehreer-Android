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
import android.graphics.RectF
import android.text.Spanned
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.mta.tehreer.R
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.RenderingStyle
import com.mta.tehreer.graphics.StrokeCap
import com.mta.tehreer.graphics.StrokeJoin
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.graphics.TypefaceManager
import com.mta.tehreer.layout.BreakMode
import com.mta.tehreer.layout.ComposedFrame
import com.mta.tehreer.layout.FrameResolver
import com.mta.tehreer.layout.TextAlignment
import com.mta.tehreer.layout.TruncationPlace
import com.mta.tehreer.layout.Typesetter
import com.mta.tehreer.layout.VerticalAlignment
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan

/**
 * Displays read-only text to the user.
 */
open class TLabel : View {
    private val renderer = Renderer()
    private val resolver = FrameResolver()

    private var currentText: String? = null
    private var currentSpanned: Spanned? = null
    private var currentTypesetter: Typesetter? = null

    private var needsTypesetter = false
    private var textWidth = 0
    private var textHeight = 0

    private val layoutRect = RectF()

    constructor(context: Context) : super(context) {
        setup(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setup(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr) {
        setup(context, attrs, defStyleAttr)
    }

    private fun setup(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs != null) {
            val values = context.theme.obtainStyledAttributes(attrs, R.styleable.TLabel, defStyleAttr, 0)

            try {
                applyAttributes(values)
            } finally {
                values.recycle()
            }
        }
    }

    private fun applyAttributes(values: TypedArray) {
        gravity = values.getInt(R.styleable.TLabel_gravity, Gravity.TOP or Gravity.START)
        maxLines = values.getInt(R.styleable.TLabel_maxLines, 0)
        extraLineSpacing = values.getDimension(R.styleable.TLabel_extraLineSpacing, 0.0f)
        lineHeightMultiplier = values.getFloat(R.styleable.TLabel_lineHeightMultiplier, 0.0f)
        shadowRadius = values.getDimension(R.styleable.TLabel_shadowRadius, 0.0f)
        shadowDx = values.getDimension(R.styleable.TLabel_shadowDx, 0.0f)
        shadowDy = values.getDimension(R.styleable.TLabel_shadowDy, 0.0f)
        shadowColor = values.getColor(R.styleable.TLabel_shadowColor, Color.TRANSPARENT)
        renderingStyle = when (values.getInt(R.styleable.TLabel_renderingStyle, 0)) {
            1 -> RenderingStyle.FILL_STROKE
            2 -> RenderingStyle.STROKE
            else -> RenderingStyle.FILL
        }
        strokeColor = values.getColor(R.styleable.TLabel_strokeColor, Color.BLACK)
        strokeWidth = values.getDimension(R.styleable.TLabel_strokeWidth, 0.0f)
        strokeCap = when (values.getInt(R.styleable.TLabel_strokeCap, 0)) {
            1 -> StrokeCap.ROUND
            2 -> StrokeCap.SQUARE
            else -> StrokeCap.BUTT
        }
        strokeJoin = when (values.getInt(R.styleable.TLabel_strokeJoin, 0)) {
            0 -> StrokeJoin.BEVEL
            1 -> StrokeJoin.MITER
            else -> StrokeJoin.ROUND
        }
        strokeMiter = values.getDimension(R.styleable.TLabel_strokeMiter, 1.0f)
        truncationMode = when (values.getInt(R.styleable.TLabel_truncationMode, 0)) {
            1 -> BreakMode.CHARACTER
            else -> BreakMode.LINE
        }
        truncationPlace = when (values.getInt(R.styleable.TLabel_truncationPlace, 0)) {
            1 -> TruncationPlace.END
            2 -> TruncationPlace.MIDDLE
            3 -> TruncationPlace.START
            else -> null
        }
        textColor = values.getColor(R.styleable.TLabel_textColor, Color.BLACK)
        textSize = values.getDimension(R.styleable.TLabel_textSize, 16f)
        if (values.hasValue(R.styleable.TLabel_typeface)) {
            typeface = TypefaceManager.getTypeface(values.getResourceId(R.styleable.TLabel_typeface, 0))
        }

        val text = values.getText(R.styleable.TLabel_text)
        if (text is Spanned) {
            spanned = text
        } else if (text != null) {
            this.text = text.toString()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val horizontalPadding = paddingLeft + paddingRight
        val verticalPadding = paddingTop + paddingBottom

        val layoutWidth = if (widthMode == MeasureSpec.UNSPECIFIED) {
            Float.POSITIVE_INFINITY
        } else {
            (widthSize - horizontalPadding).toFloat()
        }
        val layoutHeight = if (heightMode == MeasureSpec.UNSPECIFIED) {
            Float.POSITIVE_INFINITY
        } else {
            (heightSize - verticalPadding).toFloat()
        }

        resolver.fitsHorizontally = (widthMode != MeasureSpec.EXACTLY)
        resolver.fitsVertically = (heightMode != MeasureSpec.EXACTLY)
        updateFrame(paddingLeft.toFloat(), paddingTop.toFloat(), layoutWidth, layoutHeight)

        setMeasuredDimension(textWidth + horizontalPadding, textHeight + verticalPadding)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val t1 = System.nanoTime()

        canvas.save()

        composedFrame?.let { it.draw(renderer, canvas, it.originX, it.originY) }

        canvas.restore()

        val t2 = System.nanoTime()
        Log.i("Tehreer", "Time taken to render label: " + ((t2 - t1) * 1E-6))
    }

    private fun updateFrame(paddingLeft: Float, paddingTop: Float, layoutWidth: Float, layoutHeight: Float) {
        composedFrame = null
        textWidth = 0
        textHeight = 0

        currentTypesetter?.let { typesetter ->
            val t1 = System.nanoTime()

            layoutRect.set(paddingLeft, paddingTop, paddingLeft + layoutWidth, paddingTop + layoutHeight)

            resolver.typesetter = typesetter
            resolver.frameBounds = layoutRect

            val frame = resolver.createFrame(0, typesetter.spanned.length)
            composedFrame = frame

            textWidth = (frame.width + 0.5f).toInt()
            textHeight = (frame.height + 0.5f).toInt()

            val t2 = System.nanoTime()
            Log.i("Tehreer", "Time taken to resolve frame: " + ((t2 - t1) * 1E-6))
        }
    }

    private fun updateTypesetter() {
        if (needsTypesetter) {
            return
        }

        currentTypesetter = null

        val t1 = System.nanoTime()

        val text = currentText
        val spanned = currentSpanned
        val typeface = typeface

        if (text != null) {
            if (typeface != null && text.isNotEmpty()) {
                currentTypesetter = Typesetter(text, typeface, textSize)
            }
        } else if (spanned != null) {
            if (spanned.isNotEmpty()) {
                val defaultSpans = ArrayList<Any>()

                if (typeface != null) {
                    defaultSpans.add(TypefaceSpan(typeface))
                }
                defaultSpans.add(TypeSizeSpan(textSize))

                currentTypesetter = Typesetter(spanned, defaultSpans)
            }
        }

        val t2 = System.nanoTime()
        Log.i("Tehreer", "Time taken to create typesetter: " + ((t2 - t1) * 1E-6))

        requestLayout()
        invalidate()
    }

    /**
     * Performs hit testing. Returns the index of character representing the specified position, or
     * -1 if there is no character at this position.
     *
     * @param x The x- coordinate of position.
     * @param y The y- coordinate of position.
     * @return The index of character representing the specified position, or -1 if there is no
     *         character at this position.
     */
    fun hitTestPosition(x: Float, y: Float): Int {
        val frame = composedFrame!!
        val adjustedX = x - frame.originX
        val adjustedY = y - frame.originY

        val lineIndex = frame.getLineIndexForPosition(adjustedX, adjustedY)
        val composedLine = frame.lines[lineIndex]
        val lineLeft = composedLine.originX
        val lineRight = lineLeft + composedLine.width

        // Check if position exists within the line horizontally.
        if (adjustedX in lineLeft..lineRight) {
            val charIndex = composedLine.computeNearestCharIndex(adjustedX - lineLeft)
            val lastIndex = composedLine.charEnd - 1

            // Make sure to provide character of this line.
            return minOf(charIndex, lastIndex)
        }

        return -1
    }

    /**
     * The horizontal and vertical alignment of this Label. Setting it changes the horizontal
     * alignment of the text and the vertical gravity that will be used when there is extra space in
     * the Label beyond what is required for the text itself.
     */
    var gravity = Gravity.TOP or Gravity.START
        set(value) {
            field = value

            val horizontalGravity = value and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK
            val verticalGravity = value and Gravity.VERTICAL_GRAVITY_MASK

            // Resolve horizontal gravity.
            resolver.textAlignment = when (horizontalGravity) {
                Gravity.LEFT -> TextAlignment.LEFT
                Gravity.RIGHT -> TextAlignment.RIGHT
                Gravity.CENTER_HORIZONTAL -> TextAlignment.CENTER
                Gravity.END -> TextAlignment.TRAILING
                else -> TextAlignment.LEADING
            }

            // Resolve vertical gravity.
            resolver.verticalAlignment = when (verticalGravity) {
                Gravity.TOP -> VerticalAlignment.TOP
                Gravity.BOTTOM -> VerticalAlignment.BOTTOM
                else -> VerticalAlignment.MIDDLE
            }

            requestLayout()
            invalidate()
        }

    /**
     * Returns the current composed frame that is being displayed.
     *
     * @return The composed frame being displayed.
     */
    var composedFrame: ComposedFrame? = null
        private set

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
        get() = currentTypesetter
        set(value) {
            currentText = null
            currentSpanned = null
            currentTypesetter = value
            needsTypesetter = true

            requestLayout()
            invalidate()
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
        get() = currentSpanned
        set(value) {
            currentText = null
            currentSpanned = value
            needsTypesetter = false
            updateTypesetter()
        }

    /**
     * The typeface in which the text is being displayed.
     */
    var typeface: Typeface?
        get() = renderer.typeface
        set(value) {
            renderer.typeface = value
            updateTypesetter()
        }

    /**
     * The text that is being displayed. This property will be `null` if either spanned or
     * typesetter is being used instead. Setting it will make spanned property `null`.
     *
     * @see typesetter
     * @see spanned
     */
    var text: String?
        get() = currentText
        set(value) {
            currentText = value ?: ""
            currentSpanned = null
            needsTypesetter = false
            updateTypesetter()
        }

    /**
     * The text size (in pixels) in which the text is being displayed.
     */
    var textSize: Float
        get() = renderer.typeSize
        set(value) {
            renderer.typeSize = maxOf(0.0f, value)
            updateTypesetter()
        }

    /**
     * The color in which the text is being displayed.
     */
    @get:ColorInt
    @setparam:ColorInt
    var textColor: Int
        get() = renderer.fillColor
        set(value) {
            renderer.fillColor = value
            invalidate()
        }

    /**
     * The truncation mode that should be used on the last line of text in case of overflow.
     */
    var truncationMode: BreakMode
        get() = resolver.truncationMode
        set(value) {
            resolver.truncationMode = value
            requestLayout()
            invalidate()
        }

    /**
     * The truncation place for the last line of the text. The truncation is disabled if the value
     * is `null`.
     */
    var truncationPlace: TruncationPlace?
        get() = resolver.truncationPlace
        set(value) {
            resolver.truncationPlace = value
            requestLayout()
            invalidate()
        }

    /**
     * The maximum number of lines that should be displayed. Makes the Label at most this many
     * lines tall.
     */
    var maxLines: Int
        get() = resolver.maxLines
        set(value) {
            resolver.maxLines = value
            requestLayout()
            invalidate()
        }

    /**
     * The extra spacing in pixels that should be added after each text line. It is resolved before
     * line height multiplier. The default value is zero.
     *
     * @see lineHeightMultiplier
     */
    var extraLineSpacing: Float
        get() = resolver.extraLineSpacing
        set(value) {
            resolver.extraLineSpacing = value
            requestLayout()
            invalidate()
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
        get() = resolver.lineHeightMultiplier
        set(value) {
            resolver.lineHeightMultiplier = value
            requestLayout()
            invalidate()
        }

    /**
     * The rendering style, used for controlling how text should appear while drawing. The default
     * value is [RenderingStyle.FILL].
     */
    var renderingStyle: RenderingStyle
        get() = renderer.renderingStyle
        set(value) {
            renderer.renderingStyle = value
        }

    /**
     * The stroke color for text, expressed as ARGB integer. The default value is `Color.BLACK`.
     */
    @get:ColorInt
    @setparam:ColorInt
    var strokeColor: Int
        get() = renderer.strokeColor
        set(value) {
            renderer.strokeColor = value
            invalidate()
        }

    /**
     * The stroke width in pixels for text.
     */
    @get:Px
    @setparam:Px
    var strokeWidth: Float
        get() = renderer.strokeWidth
        set(value) {
            renderer.strokeWidth = maxOf(0.0f, value)
            invalidate()
        }

    /**
     * The cap, controlling how the start and end of stroked lines and paths are treated. The
     * default value is [StrokeCap.BUTT].
     */
    var strokeCap: StrokeCap
        get() = renderer.strokeCap
        set(value) {
            renderer.strokeCap = value
            invalidate()
        }

    /**
     * The stroke join type for text. The default value is [StrokeJoin.ROUND].
     */
    var strokeJoin: StrokeJoin
        get() = renderer.strokeJoin
        set(value) {
            renderer.strokeJoin = value
            invalidate()
        }

    /**
     * The stroke miter value for text in pixels. Used to control the behavior of miter joins when
     * the joins angle is sharp.
     */
    @get:Px
    @setparam:Px
    var strokeMiter: Float
        get() = renderer.strokeMiter
        set(value) {
            renderer.strokeMiter = maxOf(1.0f, value)
            invalidate()
        }

    /**
     * The radius of the shadow layer. Only works if this Label's layer type is
     * `LAYER_TYPE_SOFTWARE`.
     *
     * The shadow is disabled if the value of the radius is equal to zero.
     */
    var shadowRadius: Float
        get() = renderer.shadowRadius
        set(value) {
            renderer.shadowRadius = maxOf(0.0f, value)
            invalidate()
        }

    /**
     * The horizontal offset of the shadow layer.
     */
    var shadowDx: Float
        get() = renderer.shadowDx
        set(value) {
            renderer.shadowDx = value
            invalidate()
        }

    /**
     * The vertical offset of the shadow layer.
     */
    var shadowDy: Float
        get() = renderer.shadowDy
        set(value) {
            renderer.shadowDy = value
            invalidate()
        }

    /**
     * The color of the shadow layer.
     */
    @get:ColorInt
    @setparam:ColorInt
    var shadowColor: Int
        get() = renderer.shadowColor
        set(value) {
            renderer.shadowColor = value
            invalidate()
        }
}
