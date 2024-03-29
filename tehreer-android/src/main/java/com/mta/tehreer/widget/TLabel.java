/*
 * Copyright (C) 2016-2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import com.mta.tehreer.R;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.RenderingStyle;
import com.mta.tehreer.graphics.StrokeCap;
import com.mta.tehreer.graphics.StrokeJoin;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.layout.BreakMode;
import com.mta.tehreer.layout.ComposedFrame;
import com.mta.tehreer.layout.ComposedLine;
import com.mta.tehreer.layout.FrameResolver;
import com.mta.tehreer.layout.TextAlignment;
import com.mta.tehreer.layout.TruncationPlace;
import com.mta.tehreer.layout.Typesetter;
import com.mta.tehreer.layout.VerticalAlignment;
import com.mta.tehreer.layout.style.TypeSizeSpan;
import com.mta.tehreer.layout.style.TypefaceSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays read-only text to the user.
 */
public class TLabel extends View {
    private @NonNull Renderer mRenderer = new Renderer();
    private @NonNull FrameResolver mResolver = new FrameResolver();

    private int mGravity = Gravity.TOP | Gravity.START;

    private @Nullable String mText = null;
    private @Nullable Spanned mSpanned = null;
    private @Nullable Typesetter mTypesetter = null;

    private boolean mNeedsTypesetter = false;
    private int mTextWidth = 0;
    private int mTextHeight = 0;

    private @NonNull RectF mLayoutRect = new RectF();
    private @Nullable ComposedFrame mComposedFrame = null;

    public TLabel(Context context) {
        super(context);
        setup(context, null, 0);
    }

    public TLabel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0);
    }

    public TLabel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr);
    }

    private void setup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            setupAttributes(context, attrs, defStyleAttr);
        }
    }

    private void setupAttributes(Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
        TypedArray values = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TLabel, defStyleAttr, 0);

        try {
            RenderingStyle renderingStyle = null;
            switch (values.getInt(R.styleable.TLabel_renderingStyle, 0)) {
            case 0:
                renderingStyle = RenderingStyle.FILL;
                break;

            case 1:
                renderingStyle = RenderingStyle.FILL_STROKE;
                break;

            case 2:
                renderingStyle = RenderingStyle.STROKE;
                break;
            }

            StrokeCap strokeCap = null;
            switch (values.getInt(R.styleable.TLabel_strokeCap, 0)) {
            case 0:
                strokeCap = StrokeCap.BUTT;
                break;

            case 1:
                strokeCap = StrokeCap.ROUND;
                break;

            case 2:
                strokeCap = StrokeCap.SQUARE;
                break;
            }

            StrokeJoin strokeJoin = null;
            switch (values.getInt(R.styleable.TLabel_strokeJoin, 0)) {
            case 0:
                strokeJoin = StrokeJoin.BEVEL;
                break;

            case 1:
                strokeJoin = StrokeJoin.MITER;
                break;

            case 2:
                strokeJoin = StrokeJoin.ROUND;
                break;
            }

            BreakMode truncationMode = null;
            switch (values.getInt(R.styleable.TLabel_truncationMode, 0)) {
            case 0:
                truncationMode = BreakMode.LINE;
                break;

            case 1:
                truncationMode = BreakMode.CHARACTER;
                break;
            }

            TruncationPlace truncationPlace = null;
            switch (values.getInt(R.styleable.TLabel_truncationPlace, 0)) {
            case 1:
                truncationPlace = TruncationPlace.END;
                break;

            case 2:
                truncationPlace = TruncationPlace.MIDDLE;
                break;

            case 3:
                truncationPlace = TruncationPlace.START;
                break;
            }

            setGravity(values.getInt(R.styleable.TLabel_gravity, Gravity.TOP | Gravity.START));
            setMaxLines(values.getInt(R.styleable.TLabel_maxLines, 0));
            setExtraLineSpacing(values.getDimension(R.styleable.TLabel_extraLineSpacing, 0.0f));
            setLineHeightMultiplier(values.getFloat(R.styleable.TLabel_lineHeightMultiplier, 0.0f));
            setShadowRadius(values.getDimension(R.styleable.TLabel_shadowRadius, 0.0f));
            setShadowDx(values.getDimension(R.styleable.TLabel_shadowDx, 0.0f));
            setShadowDy(values.getDimension(R.styleable.TLabel_shadowDy, 0.0f));
            setShadowColor(values.getColor(R.styleable.TLabel_shadowColor, Color.TRANSPARENT));
            setRenderingStyle(renderingStyle);
            setStrokeColor(values.getColor(R.styleable.TLabel_strokeColor, Color.BLACK));
            setStrokeWidth(values.getDimension(R.styleable.TLabel_strokeWidth, 0.0f));
            setStrokeCap(strokeCap);
            setStrokeJoin(strokeJoin);
            setStrokeMiter(values.getDimension(R.styleable.TLabel_strokeMiter, 1.0f));
            setTruncationMode(truncationMode);
            setTruncationPlace(truncationPlace);
            setTextColor(values.getColor(R.styleable.TLabel_textColor, Color.BLACK));
            setTextSize(values.getDimension(R.styleable.TLabel_textSize, 16));
            if (values.hasValue(R.styleable.TLabel_typeface)) {
                setTypeface(values.getResourceId(R.styleable.TLabel_typeface, 0));
            }

            CharSequence text = values.getText(R.styleable.TLabel_text);
            if (text instanceof Spanned) {
                setSpanned((Spanned) text);
            } else if (text != null) {
                setText(text.toString());
            }
        } finally {
            values.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int horizontalPadding = paddingLeft + paddingRight;
        int verticalPadding = paddingTop + paddingBottom;

        float layoutWidth = (widthMode == MeasureSpec.UNSPECIFIED ? Float.POSITIVE_INFINITY : widthSize - horizontalPadding);
        float layoutHeight = (heightMode == MeasureSpec.UNSPECIFIED ? Float.POSITIVE_INFINITY : heightSize - verticalPadding);

        mResolver.setFitsHorizontally(widthMode != MeasureSpec.EXACTLY);
        mResolver.setFitsVertically(heightMode != MeasureSpec.EXACTLY);
        updateFrame(paddingLeft, paddingTop, layoutWidth, layoutHeight);

        setMeasuredDimension(mTextWidth + horizontalPadding, mTextHeight + verticalPadding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long t1 = System.nanoTime();

        canvas.save();

        if (mComposedFrame != null) {
            mComposedFrame.draw(mRenderer, canvas, mComposedFrame.getOriginX(), mComposedFrame.getOriginY());
        }

        canvas.restore();

        long t2 = System.nanoTime();
        Log.i("Tehreer", "Time taken to render label: " + ((t2 - t1) * 1E-6));
    }

    private void updateFrame(float paddingLeft, float paddingTop, float layoutWidth, float layoutHeight) {
        mComposedFrame = null;
        mTextWidth = 0;
        mTextHeight = 0;

        if (mTypesetter != null) {
            long t1 = System.nanoTime();

            mLayoutRect.set(paddingLeft, paddingTop, layoutWidth, layoutHeight);

            mResolver.setTypesetter(mTypesetter);
            mResolver.setFrameBounds(mLayoutRect);

            mComposedFrame = mResolver.createFrame(0, mTypesetter.getSpanned().length());

            mTextWidth = (int) (mComposedFrame.getWidth() + 0.5f);
            mTextHeight = (int) (mComposedFrame.getHeight() + 0.5f);

            long t2 = System.nanoTime();
            Log.i("Tehreer", "Time taken to resolve frame: " + ((t2 - t1) * 1E-6));
        }
    }

    private void updateTypesetter() {
        if (mNeedsTypesetter) {
            return;
        }

        mTypesetter = null;

        long t1 = System.nanoTime();

        if (mText != null) {
            Typeface typeface = getTypeface();
            if (typeface != null && mText.length() > 0) {
                mTypesetter = new Typesetter(mText, typeface, getTextSize());
            }
        } else if (mSpanned != null) {
            if (mSpanned.length() > 0) {
                List<Object> defaultSpans = new ArrayList<>();
                Typeface typeface = getTypeface();
                float textSize = getTextSize();

                if (typeface != null) {
                    defaultSpans.add(new TypefaceSpan(typeface));
                }
                defaultSpans.add(new TypeSizeSpan(textSize));

                mTypesetter = new Typesetter(mSpanned, defaultSpans);
            }
        }

        long t2 = System.nanoTime();
        Log.i("Tehreer", "Time taken to create typesetter: " + ((t2 - t1) * 1E-6));

        requestLayout();
        invalidate();
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
    public int hitTestPosition(float x, float y) {
        float adjustedX = x - mComposedFrame.getOriginX();
        float adjustedY = y - mComposedFrame.getOriginY();

        int lineIndex = mComposedFrame.getLineIndexForPosition(adjustedX, adjustedY);
        ComposedLine composedLine = mComposedFrame.getLines().get(lineIndex);
        float lineLeft = composedLine.getOriginX();
        float lineRight = lineLeft + composedLine.getWidth();

        // Check if position exists within the line horizontally.
        if (adjustedX >= lineLeft && adjustedX <= lineRight) {
            int charIndex = composedLine.computeNearestCharIndex(adjustedX - lineLeft);
            int lastIndex = composedLine.getCharEnd() - 1;

            // Make sure to provide character of this line.
            if (charIndex > lastIndex) {
                charIndex = lastIndex;
            }

            return charIndex;
        }

        return -1;
    }

    /**
     * Returns the horizontal and vertical alignment of this Label.
     *
     * @return The horizontal and vertical alignment of this Label.
     */
    public int getGravity() {
        return mGravity;
    }

    /**
     * Sets the horizontal alignment of the text and the vertical gravity that will be used when
     * there is extra space in the Label beyond what is required for the text itself.
     *
     * @param gravity The horizontal and vertical alignment.
     */
    public void setGravity(int gravity) {
        mGravity = gravity;

        int horizontalGravity = mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        int verticalGravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;

        TextAlignment textAlignment;
        VerticalAlignment verticalAlignment;

        // Resolve horizontal gravity.
        switch (horizontalGravity) {
        case Gravity.LEFT:
            textAlignment = TextAlignment.LEFT;
            break;

        case Gravity.RIGHT:
            textAlignment = TextAlignment.RIGHT;
            break;

        case Gravity.CENTER_HORIZONTAL:
            textAlignment = TextAlignment.CENTER;
            break;

        case Gravity.END:
            textAlignment = TextAlignment.TRAILING;
            break;

        default:
            textAlignment = TextAlignment.LEADING;
            break;
        }

        // Resolve vertical gravity.
        switch (verticalGravity) {
        case Gravity.TOP:
            verticalAlignment = VerticalAlignment.TOP;
            break;

        case Gravity.BOTTOM:
            verticalAlignment = VerticalAlignment.BOTTOM;
            break;

        default:
            verticalAlignment = VerticalAlignment.MIDDLE;
            break;
        }

        mResolver.setTextAlignment(textAlignment);
        mResolver.setVerticalAlignment(verticalAlignment);

        requestLayout();
        invalidate();
    }

    /**
     * Returns the current composed frame that is being displayed.
     *
     * @return The composed frame being displayed.
     */
    public ComposedFrame getComposedFrame() {
        return mComposedFrame;
    }

    /**
     * Returns the typesetter that is being used to compose text lines.
     *
     * @return The current typesetter.
     */
    public Typesetter getTypesetter() {
        return mTypesetter;
    }

    /**
     * Sets the typesetter that should be used to compose text lines. Calling this method will make
     * text and spanned properties <code>null</code>.
     * <p>
     * A typesetter is preferred over spanned as it avoids an extra step of creating typesetter
     * from spanned.
     *
     * @param typesetter A typesetter object.
     *
     * @see #setText(String)
     * @see #setSpanned(Spanned)
     */
    public void setTypesetter(Typesetter typesetter) {
        mText = null;
        mSpanned = null;
        mTypesetter = typesetter;
        mNeedsTypesetter = true;

        requestLayout();
        invalidate();
    }

    /**
     * Returns the current spanned that is being displayed. This property will be <code>null</code>
     * if either text or typesetter is being used instead.
     *
     * @return The spanned being displayed.
     *
     * @see #getTypesetter()
     * @see #getText()
     */
    public Spanned getSpanned() {
        return mSpanned;
    }

    /**
     * Sets the spanned that should be displayed. Calling this method will make text property
     * <code>null</code>.
     * <p>
     * If performance is required, a typesetter should be used directly.
     *
     * @param spanned The spanned to display.
     *
     * @see #setTypesetter(Typesetter)
     * @see #setSpanned(Spanned)
     */
    public void setSpanned(Spanned spanned) {
        mText = null;
        mSpanned = spanned;
        mNeedsTypesetter = false;
        updateTypesetter();
    }

    /**
     * Returns the current typeface in which the text is being displayed.
     *
     * @return The typeface being used for displaying text.
     */
    public Typeface getTypeface() {
        return mRenderer.getTypeface();
    }

    /**
     * Sets the typeface in which the text should be displayed.
     *
     * @param typeface The typeface to use for displaying text.
     */
    public void setTypeface(Typeface typeface) {
        mRenderer.setTypeface(typeface);
        updateTypesetter();
    }

    private void setTypeface(@NonNull Object tag) {
        setTypeface(TypefaceManager.getTypeface(tag));
    }

    /**
     * Returns the current text that is being displayed. This property will be <code>null</code> if
     * either spanned or typesetter is being used instead.
     *
     * @return The text being displayed.
     *
     * @see #getTypesetter()
     * @see #getSpanned()
     */
    public String getText() {
        return mText;
    }

    /**
     * Sets the text that should be displayed. Calling this method will make spanned property
     * <code>null</code>.
     *
     * @param text The text to display.
     *
     * @see #setTypesetter(Typesetter)
     * @see #setSpanned(Spanned)
     */
    public void setText(String text) {
        mText = (text == null ? "" : text);
        mSpanned = null;
        mNeedsTypesetter = false;
        updateTypesetter();
    }

    /**
     * Returns the current text size (in pixels) in which the text is being displayed.
     *
     * @return The text size to use for displaying text.
     */
    public float getTextSize() {
        return mRenderer.getTypeSize();
    }

    /**
     * Set the text size (in pixels) in which the text should be displayed.
     *
     * @param textSize The text size to use for displaying text.
     */
    public void setTextSize(float textSize) {
        mRenderer.setTypeSize(Math.max(0.0f, textSize));
        updateTypesetter();
    }

    /**
     * Returns the current color in which the text is being displayed.
     *
     * @return The color being used for displaying text.
     */
    public @ColorInt int getTextColor() {
        return mRenderer.getFillColor();
    }

    /**
     * Sets the color in which the text should be displayed.
     *
     * @param textColor The color to use for displaying text.
     */
    public void setTextColor(@ColorInt int textColor) {
        mRenderer.setFillColor(textColor);
        invalidate();
    }

    /**
     * Returns the truncation mode that should be used on the last line of the text in case of
     * overflow.
     *
     * @return The current truncation mode.
     */
    public BreakMode getTruncationMode() {
        return mResolver.getTruncationMode();
    }

    /**
     * Sets the truncation mode that should be used on the last line of text in case of overflow.
     *
     * @param truncationMode A value of {@link BreakMode}.
     */
    public void setTruncationMode(BreakMode truncationMode) {
        mResolver.setTruncationMode(truncationMode == null ? BreakMode.LINE : truncationMode);
        requestLayout();
        invalidate();
    }

    /**
     * Returns the truncation place for the last line of the text.
     *
     * @return The current truncation place.
     */
    public TruncationPlace getTruncationPlace() {
        return mResolver.getTruncationPlace();
    }

    /**
     * Sets the truncation place for the last line of the text.
     * <p>
     * The truncation is disabled if the value of <code>truncationPlace</code> is <code>null</code>.
     *
     * @param truncationPlace A value of {@link TruncationPlace}.
     */
    public void setTruncationPlace(TruncationPlace truncationPlace) {
        mResolver.setTruncationPlace(truncationPlace);
        requestLayout();
        invalidate();
    }

    /**
     * Returns the maximum number of lines that should be displayed.
     *
     * @return The maximum number of lines that should be displayed.
     */
    public int getMaxLines() {
        return mResolver.getMaxLines();
    }

    /**
     * Makes the Label at most this many lines tall.
     *
     * @param maxLines The maximum number of lines that should be displayed.
     */
    public void setMaxLines(int maxLines) {
        mResolver.setMaxLines(maxLines);
        requestLayout();
        invalidate();
    }

    /**
     * Returns the extra spacing that should be added after each text line. It is resolved before line
     * height multiplier. The default value is zero.
     *
     * @return The current extra line spacing.
     *
     * @see #getLineHeightMultiplier()
     */
    public float getExtraLineSpacing() {
        return mResolver.getExtraLineSpacing();
    }

    /**
     * Sets the extra spacing that should be added after each text line. It is resolved before line
     * height multiplier. The default value is zero.
     *
     * @param extraLineSpacing The extra line spacing in pixels.
     *
     * @see #setLineHeightMultiplier(float)
     */
    public void setExtraLineSpacing(float extraLineSpacing) {
        mResolver.setExtraLineSpacing(extraLineSpacing);
        requestLayout();
        invalidate();
    }

    /**
     * Returns the height multiplier that should be applied on each text line. It is resolved after
     * extra line spacing. The default value is one.
     *
     * @return The current line height multiplier.
     *
     * @see #getExtraLineSpacing()
     */
    public float getLineHeightMultiplier() {
        return mResolver.getLineHeightMultiplier();
    }

    /**
     * Sets the height multiplier to apply on each text line. It is resolved after extra line
     * spacing. The default value is one.
     *
     * <p>
     * The additional spacing is adjusted in such a way that text remains in the middle of the line.
     *
     * @param lineHeightMultiplier The multiplication factor.
     *
     * @see #setExtraLineSpacing(float)
     */
    public void setLineHeightMultiplier(float lineHeightMultiplier) {
        mResolver.setLineHeightMultiplier(lineHeightMultiplier);
        requestLayout();
        invalidate();
    }

    /**
     * Returns the rendering style, used for controlling how text should appear while drawing. The
     * default value is {@link RenderingStyle#FILL}.
     *
     * @return The style setting for text.
     */
    public RenderingStyle getRenderingStyle() {
        return mRenderer.getRenderingStyle();
    }

    /**
     * Sets the rendering style, used for controlling how text should appear while drawing. The
     * default value is {@link RenderingStyle#FILL}.
     *
     * @param renderingStyle The new style setting for the text.
     */
    public void setRenderingStyle(RenderingStyle renderingStyle) {
        mRenderer.setRenderingStyle(renderingStyle == null ? RenderingStyle.FILL : renderingStyle);
    }

    /**
     * Returns the stroke color for text. The default value is <code>Color.BLACK</code>.
     *
     * @return The stroke color expressed as ARGB integer.
     */
    public @ColorInt int getStrokeColor() {
        return mRenderer.getStrokeColor();
    }

    /**
     * Sets the stroke color text. The default value is <code>Color.BLACK</code>.
     *
     * @param strokeColor The 32-bit value of color expressed as ARGB.
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        mRenderer.setStrokeColor(strokeColor);
        invalidate();
    }

    /**
     * Returns the stroke width for text.
     *
     * @return The stroke width in pixels.
     */
    public @Px float getStrokeWidth() {
        return mRenderer.getStrokeWidth();
    }

    /**
     * Sets the stroke width for text.
     *
     * @param strokeWidth The stroke width in pixels.
     */
    public void setStrokeWidth(@Px float strokeWidth) {
        mRenderer.setStrokeWidth(Math.max(0.0f, strokeWidth));
        invalidate();
    }

    /**
     * Returns the cap, controlling how the start and end of stroked lines and paths are treated.
     * The default value is {@link StrokeCap#BUTT}.
     *
     * @return The stroke cap style for text.
     */
    public StrokeCap getStrokeCap() {
        return mRenderer.getStrokeCap();
    }

    /**
     * Sets the cap, controlling how the start and end of stroked lines and paths are treated. The
     * default value is {@link StrokeCap#BUTT}.
     *
     * @param strokeCap The new stroke cap style for text.
     */
    public void setStrokeCap(StrokeCap strokeCap) {
        mRenderer.setStrokeCap(strokeCap == null ? StrokeCap.BUTT : strokeCap);
        invalidate();
    }

    /**
     * Returns the stroke join type for text. The default value is {@link StrokeJoin#ROUND}.
     *
     * @return The stroke join type.
     */
    public StrokeJoin getStrokeJoin() {
        return mRenderer.getStrokeJoin();
    }

    /**
     * Sets the stroke join type for text. The default value is {@link StrokeJoin#ROUND}.
     *
     * @param strokeJoin The new stroke join type.
     */
    public void setStrokeJoin(StrokeJoin strokeJoin) {
        mRenderer.setStrokeJoin(strokeJoin == null ? StrokeJoin.ROUND : strokeJoin);
        invalidate();
    }

    /**
     * Returns the stroke miter value for text. Used to control the behavior of miter joins when the
     * joins angle is sharp.
     *
     * @return The miter limit in pixels.
     */
    public @Px float getStrokeMiter() {
        return mRenderer.getStrokeMiter();
    }

    /**
     * Sets thee stroke miter value. This is used to control the behavior of miter joins when the
     * joins angle is sharp.
     *
     * @param strokeMiter The value of miter limit in pixels.
     */
    public void setStrokeMiter(@Px float strokeMiter) {
        mRenderer.setStrokeMiter(Math.max(1.0f, strokeMiter));
        invalidate();
    }

    /**
     * Returns the radius of the shadow layer.
     *
     * @return The value of shadow radius.
     */
    public float getShadowRadius() {
        return mRenderer.getShadowRadius();
    }

    /**
     * Sets the radius of the shadow layer. Only works if this Label's layer type is
     * <code>LAYER_TYPE_SOFTWARE</code>.
     * <p>
     * The shadow is disabled if the value of <code>shadowRadius</code> is equal to zero.
     *
     * @param shadowRadius The value of shadow's radius.
     */
    public void setShadowRadius(float shadowRadius) {
        mRenderer.setShadowRadius(Math.max(0.0f, shadowRadius));
        invalidate();
    }

    /**
     * Returns the horizontal offset of the shadow layer.
     *
     * @return The value of shadow's horizontal offset.
     */
    public float getShadowDx() {
        return mRenderer.getShadowDx();
    }

    /**
     * Sets the horizontal offset of the shadow layer.
     *
     * @param shadowDx The value of shadow's horizontal offset.
     */
    public void setShadowDx(float shadowDx) {
        mRenderer.setShadowDx(shadowDx);
        invalidate();
    }

    /**
     * Returns the vertical offset of the shadow layer.
     *
     * @return The value of shadow's vertical offset.
     */
    public float getShadowDy() {
        return mRenderer.getShadowDy();
    }

    /**
     * Sets the vertical offset of the shadow layer.
     *
     * @param shadowDy The value of shadow's vertical offset.
     */
    public void setShadowDy(float shadowDy) {
        mRenderer.setShadowDy(shadowDy);
        invalidate();
    }

    /**
     * Returns the color of the shadow layer.
     *
     * @return The color of the shadow.
     */
    public @ColorInt int getShadowColor() {
        return mRenderer.getShadowColor();
    }

    /**
     * Sets the color of the shadow layer.
     *
     * @param shadowColor The color the shadow.
     */
    public void setShadowColor(@ColorInt int shadowColor) {
        mRenderer.setShadowColor(shadowColor);
        invalidate();
    }
}
