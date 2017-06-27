/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.mta.tehreer.R;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.text.ComposedLine;
import com.mta.tehreer.text.WrapMode;
import com.mta.tehreer.text.TruncationType;
import com.mta.tehreer.text.Typesetter;

import java.util.ArrayList;

/**
 * Displays read-only text to the user.
 */
public class TLabel extends View {

    private static final String TRUNCATION_STRING = "...";

    private int mGravity;
    private String mText;
    private int mMaxLines;

    private Renderer mRenderer;
    private Typesetter mTypesetter;
    private TruncationType mTruncationType;
    private WrapMode mWrapMode;
    private ComposedLine mTruncationToken;

    private int mTextWidth;
    private int mTextHeight;

    private ArrayList<ComposedLine> mComposedLines = new ArrayList<>();

    private static float getLineHeight(ComposedLine composedLine) {
        return composedLine.getAscent() + composedLine.getDescent();
    }

    public TLabel(Context context) {
        super(context);
        setup(context, null, 0);
    }

    public TLabel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TLabel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr);
    }

    private void setup(Context context, AttributeSet attrs, int defStyleAttr) {
        mRenderer = new Renderer();

        if (attrs != null) {
            setupAttributes(context, attrs, defStyleAttr);
        }
    }

    private void setupAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray values = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TLabel, defStyleAttr, 0);

        try {
            TruncationType truncationType = null;
            switch (values.getInt(R.styleable.TLabel_textTruncation, 0)) {
            case 1:
                truncationType = TruncationType.START;
                break;

            case 2:
                truncationType = TruncationType.MIDDLE;
                break;

            case 3:
                truncationType = TruncationType.END;
                break;
            }

            WrapMode wrapMode = null;
            switch (values.getInt(R.styleable.TLabel_textBreak, 0)) {
            case 0:
                wrapMode = WrapMode.WORD;
                break;

            case 1:
                wrapMode = WrapMode.CHARACTER;
                break;
            }

            setGravity(values.getInt(R.styleable.TLabel_gravity, Gravity.TOP | Gravity.LEFT));
            setMaxLines(values.getInteger(R.styleable.TLabel_maxLines, 0));
            setShadowRadius(values.getDimension(R.styleable.TLabel_shadowRadius, 0.0f));
            setShadowDx(values.getDimension(R.styleable.TLabel_shadowDx, 0.0f));
            setShadowDy(values.getDimension(R.styleable.TLabel_shadowDy, 0.0f));
            setShadowColor(values.getInteger(R.styleable.TLabel_shadowColor, Color.TRANSPARENT));
            setTextTruncation(truncationType);
            setTextBreak(wrapMode);
            setTextColor(values.getInteger(R.styleable.TLabel_textColor, Color.BLACK));
            setTextSize(values.getDimensionPixelSize(R.styleable.TLabel_textSize, 16));
            setText(values.getString(R.styleable.TLabel_text));
            if (values.hasValue(R.styleable.TLabel_typeface)) {
                setTypeface(values.getResourceId(R.styleable.TLabel_typeface, 0));
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

        int horizontalPadding = getPaddingLeft() + getPaddingRight();
        int verticalPadding = getPaddingTop() + getPaddingBottom();

        int layoutWidth = (widthMode == MeasureSpec.UNSPECIFIED
                           ? Integer.MAX_VALUE
                           : widthSize - horizontalPadding);
        int layoutHeight = (heightMode == MeasureSpec.UNSPECIFIED
                            ? Integer.MAX_VALUE
                            : heightSize - verticalPadding);
        updateLines(layoutWidth, layoutHeight);

        int actualWidth;
        int actualHeight;

        if (widthMode == MeasureSpec.EXACTLY) {
            actualWidth = widthSize;
        } else {
            actualWidth = horizontalPadding + mTextWidth;

            if (widthMode == MeasureSpec.AT_MOST) {
                actualWidth = Math.min(widthSize, actualWidth);
            }
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            actualHeight = heightSize;
        } else {
            actualHeight = verticalPadding + mTextHeight;

            if (heightMode == MeasureSpec.AT_MOST) {
                actualHeight = Math.min(heightSize, actualHeight);
            }
        }

        setMeasuredDimension(actualWidth, actualHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long t1 = System.nanoTime();

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int unpaddedWidth = getWidth() - (paddingLeft + paddingRight);
        int unpaddedHeight = getHeight() - (paddingTop + paddingBottom);
        int visibleBottom = paddingTop + unpaddedHeight;

        float penLeft = paddingLeft;
        float penTop = paddingTop;

        switch (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
        case Gravity.LEFT:
            break;

        case Gravity.RIGHT:
            penLeft += unpaddedWidth - mTextWidth;
            break;

        default:
            penLeft += (unpaddedWidth - mTextWidth) / 2.0f;
            break;
        }

        switch (mGravity & Gravity.VERTICAL_GRAVITY_MASK) {
        case Gravity.TOP:
            break;

        case Gravity.BOTTOM:
            penTop += unpaddedHeight - mTextHeight;
            break;

        default:
            penTop += (unpaddedHeight - mTextHeight) / 2.0f;
            break;
        }

        canvas.save();

        for (ComposedLine composedLine : mComposedLines) {
            float lineHeight = getLineHeight(composedLine);
            float penBottom = penTop + lineHeight;

            if (penTop < visibleBottom) {
                if (penBottom > paddingTop) {
                    float translateX = penLeft;
                    float translateY = penTop + composedLine.getAscent();

                    switch (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.LEFT:
                        translateX += composedLine.getFlushPenOffset(0.0f, mTextWidth);
                        break;

                    case Gravity.RIGHT:
                        translateX += composedLine.getFlushPenOffset(1.0f, mTextWidth);
                        break;

                    default:
                        translateX += composedLine.getFlushPenOffset(0.5f, mTextWidth);
                        break;
                    }

                    composedLine.draw(mRenderer, canvas, translateX, translateY);
                }
            } else {
                break;
            }

            penTop = penBottom;
        }

        canvas.restore();

        long t2 = System.nanoTime();
        Log.i("Tehreer", "Time taken to render label: " + ((t2 - t1) / Math.pow(10, 6)));
    }

    private void ensureTruncationToken() {
        if (mTruncationToken == null) {
            Typesetter tokenTypesetter = new Typesetter(TRUNCATION_STRING, getTypeface(), getTextSize());
            mTruncationToken = tokenTypesetter.createLine(0, TRUNCATION_STRING.length());
        }
    }

    private void updateLines(int layoutWidth, int layoutHeight) {
        mComposedLines.clear();

        if (mTypesetter != null) {
            long t1 = System.nanoTime();

            // Get boundary of first line.
            int lineStart = 0;
            int lineEnd = mTypesetter.suggestLineBoundary(lineStart, layoutWidth);

            // Add first line even if layout height is smaller than its height.
            ComposedLine composedLine = mTypesetter.createLine(lineStart, lineEnd);
            mComposedLines.add(composedLine);

            // Setup text width and height based on first line.
            float textWidth = composedLine.getWidth();
            float textHeight = getLineHeight(composedLine);

            lineStart = lineEnd;
            int textLength = mTypesetter.getText().length();

            int maxLines = (mMaxLines == 0 ? Integer.MAX_VALUE : mMaxLines);

            // Add remaining lines fitting in layout height.
            while (lineStart < textLength) {
                lineEnd = mTypesetter.suggestLineBoundary(lineStart, layoutWidth);
                composedLine = mTypesetter.createLine(lineStart, lineEnd);

                float lineWidth = composedLine.getWidth();
                float lineHeight = getLineHeight(composedLine);

                if ((textHeight + lineHeight) <= layoutHeight && mComposedLines.size() < maxLines) {
                    textWidth = Math.max(textWidth, lineWidth);
                    textHeight += lineHeight;
                    mComposedLines.add(composedLine);

                    lineStart = lineEnd;
                } else {
                    if (mTruncationType != null) {
                        ensureTruncationToken();

                        if (layoutWidth > mTruncationToken.getWidth()) {
                            // Replace the last line with truncated one.
                            ComposedLine lastLine = mComposedLines.remove(mComposedLines.size() - 1);
                            ComposedLine truncatedLine = mTypesetter.createTruncatedLine(lastLine.getCharStart(), textLength, layoutWidth, mTruncationType, mWrapMode, mTruncationToken);
                            mComposedLines.add(truncatedLine);
                        }
                    }
                    break;
                }
            }

            mTextWidth = (int) (textWidth + 1.0f);
            mTextHeight = (int) (textHeight + 1.0f);

            long t2 = System.nanoTime();
            Log.i("Tehreer", "Time taken to create lines: " + ((t2 - t1) / Math.pow(10, 6)));
        }
    }

    private void updateTypesetter() {
        if (mText != null) {
            mTypesetter = null;
            mTruncationToken = null;

            Typeface typeface = mRenderer.getTypeface();
            if (typeface != null && mText.length() > 0) {
                mTypesetter = new Typesetter(mText, typeface, mRenderer.getTypeSize());
            }

            requestLayout();
            invalidate();
        }
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
        invalidate();
    }

    public Typesetter getTypesetter() {
        return mTypesetter;
    }

    public void setTypesetter(Typesetter typesetter) {
        mText = null;
        mTruncationToken = null;
        mTypesetter = typesetter;

        requestLayout();
        invalidate();
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

    private void setTypeface(Object tag) {
        setTypeface(TypefaceManager.getDefaultManager().getTypeface(tag));
    }

    /**
     * Returns the current text that is being displayed.
     *
     * @return The text being displayed.
     */
    public String getText() {
        return mText;
    }

    /**
     * Sets the text that should be displayed.
     *
     * @param text The text to display.
     */
    public void setText(String text) {
        mText = (text == null ? "" : text);
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
    public int getTextColor() {
        return mRenderer.getFillColor();
    }

    /**
     * Sets the color in which the text should be displayed.
     *
     * @param textColor The color to use for displaying text.
     */
    public void setTextColor(int textColor) {
        mRenderer.setFillColor(textColor);
        invalidate();
    }

    /**
     * Returns the truncation type that should be applied on the last line of the text.
     *
     * @return The current truncation type.
     */
    public TruncationType getTextTruncation() {
        return mTruncationType;
    }

    /**
     * Sets the truncation type that should be applied on the last line of the text if it overflows
     * the available area.
     *
     * @param truncationType A value of {@link TruncationType}.
     */
    public void setTextTruncation(TruncationType truncationType) {
        mTruncationType = truncationType;
        mTruncationToken = null;
        requestLayout();
        invalidate();
    }

    /**
     * Returns the place at which text should be truncated if it overflows the available area.
     *
     * @return The current truncation place.
     */
    public WrapMode getTextBreak() {
        return mWrapMode;
    }

    /**
     * Sets the place at which text should be truncated if it overflows the available area.
     *
     * @param wrapMode A value of {@link WrapMode}.
     */
    public void setTextBreak(WrapMode wrapMode) {
        mWrapMode = wrapMode;
        mTruncationToken = null;
        requestLayout();
        invalidate();
    }

    /**
     * Returns the maximum number of lines that should be displayed.
     *
     * @return The maximum number of lines that should be displayed.
     */
    public int getMaxLines() {
        return mMaxLines;
    }

    /**
     * Makes the Label at most this many lines tall.
     *
     * @param maxLines The maximum number of lines that should be displayed.
     */
    public void setMaxLines(int maxLines) {
        mMaxLines = maxLines;
        requestLayout();
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
    public int getShadowColor() {
        return mRenderer.getShadowColor();
    }

    /**
     * Sets the color of the shadow layer.
     *
     * @param shadowColor The color the shadow.
     */
    public void setShadowColor(int shadowColor) {
        mRenderer.setShadowColor(shadowColor);
        invalidate();
    }
}
