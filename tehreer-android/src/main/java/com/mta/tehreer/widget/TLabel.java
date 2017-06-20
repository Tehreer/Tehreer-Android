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
import com.mta.tehreer.text.TextLine;
import com.mta.tehreer.text.TextTruncation;
import com.mta.tehreer.text.TextTypesetter;

import java.util.ArrayList;

/**
 * Displays read-only text to the user.
 */
public class TLabel extends View {

    private static final String TRUNCATION_STRING = "...";

    public static final int TRUNCATION_MODE_NONE = 0;
    public static final int TRUNCATION_MODE_WORD_ELLIPSIS = 1;
    public static final int TRUNCATION_MODE_CHARACTER_ELLIPSIS = 2;

    public static final int TRUNCATION_PLACE_END = 0;
    public static final int TRUNCATION_PLACE_START = 1;
    public static final int TRUNCATION_PLACE_MIDDLE = 2;

    private int mGravity;
    private String mText;
    private int mTruncationMode;
    private int mTruncationPlace;
    private int mMaxLines;

    private Renderer mRenderer;
    private TextTypesetter mTypesetter;
    private TextTruncation mTextTruncation;
    private TextLine mTruncationToken;

    private int mTextWidth;
    private int mTextHeight;

    private ArrayList<TextLine> mTextLines = new ArrayList<>();

    private static float getLineHeight(TextLine textLine) {
        return textLine.getAscent() + textLine.getDescent();
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
            setGravity(values.getInt(R.styleable.TLabel_gravity, Gravity.TOP | Gravity.LEFT));
            setMaxLines(values.getInteger(R.styleable.TLabel_maxLines, 0));
            setShadowRadius(values.getDimension(R.styleable.TLabel_shadowRadius, 0.0f));
            setShadowDx(values.getDimension(R.styleable.TLabel_shadowDx, 0.0f));
            setShadowDy(values.getDimension(R.styleable.TLabel_shadowDy, 0.0f));
            setShadowColor(values.getInteger(R.styleable.TLabel_shadowColor, Color.TRANSPARENT));
            setTruncationMode(values.getInteger(R.styleable.TLabel_truncationMode, TRUNCATION_MODE_NONE));
            setTruncationPlace(values.getInteger(R.styleable.TLabel_truncationPlace, TRUNCATION_PLACE_END));
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

        for (TextLine textLine : mTextLines) {
            float lineHeight = getLineHeight(textLine);
            float penBottom = penTop + lineHeight;

            if (penTop < visibleBottom) {
                if (penBottom > paddingTop) {
                    float translateX = penLeft;
                    float translateY = penTop + textLine.getAscent();

                    switch (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.LEFT:
                        translateX += textLine.getFlushPenOffset(0.0f, mTextWidth);
                        break;

                    case Gravity.RIGHT:
                        translateX += textLine.getFlushPenOffset(1.0f, mTextWidth);
                        break;

                    default:
                        translateX += textLine.getFlushPenOffset(0.5f, mTextWidth);
                        break;
                    }

                    textLine.draw(mRenderer, canvas, translateX, translateY);
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
            TextTypesetter tokenTypesetter = new TextTypesetter(TRUNCATION_STRING, getTypeface(), getTextSize());
            mTruncationToken = tokenTypesetter.createLine(0, TRUNCATION_STRING.length());
        }
    }

    private void updateLines(int layoutWidth, int layoutHeight) {
        mTextLines.clear();

        if (mTypesetter != null) {
            long t1 = System.nanoTime();

            // Get boundary of first line.
            int lineStart = 0;
            int lineEnd = mTypesetter.suggestLineBoundary(lineStart, layoutWidth);

            // Add first line even if layout height is smaller than its height.
            TextLine textLine = mTypesetter.createLine(lineStart, lineEnd);
            mTextLines.add(textLine);

            // Setup text width and height based on first line.
            float textWidth = textLine.getWidth();
            float textHeight = getLineHeight(textLine);

            lineStart = lineEnd;
            int textLength = mTypesetter.getText().length();

            int maxLines = (mMaxLines == 0 ? Integer.MAX_VALUE : mMaxLines);

            // Add remaining lines fitting in layout height.
            while (lineStart < textLength) {
                lineEnd = mTypesetter.suggestLineBoundary(lineStart, layoutWidth);
                textLine = mTypesetter.createLine(lineStart, lineEnd);

                float lineWidth = textLine.getWidth();
                float lineHeight = getLineHeight(textLine);

                if ((textHeight + lineHeight) <= layoutHeight && mTextLines.size() < maxLines) {
                    textWidth = Math.max(textWidth, lineWidth);
                    textHeight += lineHeight;
                    mTextLines.add(textLine);

                    lineStart = lineEnd;
                } else {
                    if (mTextTruncation != null) {
                        ensureTruncationToken();

                        if (layoutWidth > mTruncationToken.getWidth()) {
                            // Replace the last line with truncated one.
                            TextLine lastLine = mTextLines.remove(mTextLines.size() - 1);
                            TextLine truncatedLine = mTypesetter.createTruncatedLine(lastLine.getCharStart(), textLength, layoutWidth, mTextTruncation, mTruncationToken);
                            mTextLines.add(truncatedLine);
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
                mTypesetter = new TextTypesetter(mText, typeface, mRenderer.getTypeSize());
            }

            requestLayout();
            invalidate();
        }
    }

    private void updateTruncation() {
        TextTruncation textTruncation = null;

        switch (mTruncationMode) {
        case TRUNCATION_MODE_WORD_ELLIPSIS:
            switch (mTruncationPlace) {
            case TRUNCATION_PLACE_END:
                textTruncation = TextTruncation.WORD_END;
                break;

            case TRUNCATION_PLACE_START:
                textTruncation = TextTruncation.WORD_START;
                break;

            case TRUNCATION_PLACE_MIDDLE:
                textTruncation = TextTruncation.WORD_MIDDLE;
                break;
            }
            break;

        case TRUNCATION_MODE_CHARACTER_ELLIPSIS:
            switch (mTruncationPlace) {
            case TRUNCATION_PLACE_START:
                textTruncation = TextTruncation.CHARACTER_START;
                break;

            case TRUNCATION_PLACE_MIDDLE:
                textTruncation = TextTruncation.CHARACTER_MIDDLE;
                break;

            default:
                textTruncation = TextTruncation.CHARACTER_END;
                break;
            }
            break;
        }

        if (textTruncation != mTextTruncation) {
            mTextTruncation = textTruncation;
            mTruncationToken = null;
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

    public TextTypesetter getTypesetter() {
        return mTypesetter;
    }

    public void setTypesetter(TextTypesetter typesetter) {
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
     * Returns the truncation mode that should be applied on the last line of the text.
     *
     * @return The current truncation mode.
     */
    public int getTruncationMode() {
        return mTruncationMode;
    }

    /**
     * Sets the truncation mode that should be applied on the last line of the text if it overflows
     * the available area.
     *
     * @param truncationMode A value of <code>TRUNCATION_MODE</code>.
     */
    public void setTruncationMode(int truncationMode) {
        switch (truncationMode) {
        case TRUNCATION_MODE_NONE:
        case TRUNCATION_MODE_WORD_ELLIPSIS:
        case TRUNCATION_MODE_CHARACTER_ELLIPSIS:
            mTruncationMode = truncationMode;
            break;

        default:
            mTruncationMode = TRUNCATION_MODE_NONE;
            break;
        }

        updateTruncation();
    }

    /**
     * Returns the place at which text should be truncated if it overflows the available area.
     *
     * @return The current truncation place.
     */
    public int getTruncationPlace() {
        return mTruncationPlace;
    }

    /**
     * Sets the place at which text should be truncated if it overflows the available area.
     *
     * @param truncationPlace A value of <code>TRUNCATION_PLACE</code>.
     */
    public void setTruncationPlace(int truncationPlace) {
        switch (truncationPlace) {
        case TRUNCATION_PLACE_END:
        case TRUNCATION_PLACE_START:
        case TRUNCATION_PLACE_MIDDLE:
            mTruncationPlace = truncationPlace;
            break;

        default:
            mTruncationPlace = TRUNCATION_PLACE_END;
        }

        updateTruncation();
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
