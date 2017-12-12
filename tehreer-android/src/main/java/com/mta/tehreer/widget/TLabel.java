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
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.mta.tehreer.R;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.layout.BreakMode;
import com.mta.tehreer.layout.ComposedLine;
import com.mta.tehreer.layout.TruncationPlace;
import com.mta.tehreer.layout.Typesetter;
import com.mta.tehreer.layout.style.TypeSizeSpan;
import com.mta.tehreer.layout.style.TypefaceSpan;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays read-only text to the user.
 */
public class TLabel extends View {

    private int mGravity = Gravity.TOP | Gravity.START;
    private int mMaxLines = 0;

    private Renderer mRenderer = new Renderer();
    private BreakMode mTruncationMode = BreakMode.LINE;
    private TruncationPlace mTruncationPlace = TruncationPlace.END;

    private String mText = "";
    private Spanned mSpanned = null;
    private Typesetter mTypesetter = null;

    private boolean mRtlText = false;
    private int mTextWidth = 0;
    private int mTextHeight = 0;

    private ArrayList<ComposedLine> mComposedLines = new ArrayList<>();

    private static float getLineHeight(ComposedLine composedLine) {
        return composedLine.getAscent() + composedLine.getDescent();
    }

    public TLabel(Context context) {
        super(context);
        setup(context, null, 0);
    }

    public TLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0);
    }

    public TLabel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr);
    }

    private void setup(Context context, AttributeSet attrs, int defStyleAttr) {
        if (attrs != null) {
            setupAttributes(context, attrs, defStyleAttr);
        }
    }

    private void setupAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray values = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TLabel, defStyleAttr, 0);

        try {
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
            setMaxLines(values.getInteger(R.styleable.TLabel_maxLines, 0));
            setShadowRadius(values.getDimension(R.styleable.TLabel_shadowRadius, 0.0f));
            setShadowDx(values.getDimension(R.styleable.TLabel_shadowDx, 0.0f));
            setShadowDy(values.getDimension(R.styleable.TLabel_shadowDy, 0.0f));
            setShadowColor(values.getInteger(R.styleable.TLabel_shadowColor, Color.TRANSPARENT));
            setTruncationMode(truncationMode);
            setTruncationPlace(truncationPlace);
            setTextColor(values.getInteger(R.styleable.TLabel_textColor, Color.BLACK));
            setTextSize(values.getDimensionPixelSize(R.styleable.TLabel_textSize, 16));
            if (values.hasValue(R.styleable.TLabel_typeface)) {
                setTypeface(values.getResourceId(R.styleable.TLabel_typeface, 0));
            }
            setSpanned(Html.fromHtml(values.getString(R.styleable.TLabel_text)));
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
        float flushFactor = 0.0f;

        int relativeGravity = mGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        int horizontalGravity = mGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
        int verticalGravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;

        // Resolve relative layout direction.
        if (relativeGravity == Gravity.START) {
            horizontalGravity = !mRtlText ? Gravity.LEFT : Gravity.RIGHT;
        } else if (relativeGravity == Gravity.END) {
            horizontalGravity = !mRtlText ? Gravity.RIGHT : Gravity.LEFT;
        }

        // Resolve initial pen left and flush factor.
        if (horizontalGravity == Gravity.RIGHT) {
            penLeft += unpaddedWidth - mTextWidth;
            flushFactor = 1.0f;
        } else if (horizontalGravity != Gravity.LEFT) {
            penLeft += (unpaddedWidth - mTextWidth) / 2.0f;
            flushFactor = 0.5f;
        }

        // Resolve initial pen top.
        if (verticalGravity == Gravity.BOTTOM) {
            penTop += unpaddedHeight - mTextHeight;
        } else if (verticalGravity != Gravity.TOP) {
            penTop += (unpaddedHeight - mTextHeight) / 2.0f;
        }

        canvas.save();

        for (ComposedLine composedLine : mComposedLines) {
            float lineHeight = getLineHeight(composedLine);
            float penBottom = penTop + lineHeight;

            if (penTop < visibleBottom) {
                if (penBottom > paddingTop) {
                    float lineX = penLeft + composedLine.getFlushPenOffset(flushFactor, mTextWidth);
                    float lineY = penTop + composedLine.getAscent();

                    composedLine.draw(mRenderer, canvas, lineX, lineY);
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

    private void updateLines(int layoutWidth, int layoutHeight) {
        mRtlText = false;
        mComposedLines.clear();

        if (mTypesetter != null) {
            long t1 = System.nanoTime();

            int textLength = mTypesetter.getSpanned().length();
            int maxLines = (mMaxLines == 0 ? Integer.MAX_VALUE : mMaxLines);

            // Get boundary of first line.
            int lineStart = 0;
            int lineEnd = mTypesetter.suggestForwardBreak(lineStart, textLength, layoutWidth, BreakMode.LINE);

            // Add first line even if layout height is smaller than its height.
            ComposedLine composedLine = mTypesetter.createSimpleLine(lineStart, lineEnd);
            mRtlText = (composedLine.getParagraphLevel() & 1) == 1;
            mComposedLines.add(composedLine);

            // Setup text width and height based on first line.
            float textWidth = composedLine.getWidth();
            float textHeight = getLineHeight(composedLine);

            lineStart = lineEnd;

            // Add remaining lines fitting in layout height.
            while (lineStart < textLength) {
                lineEnd = mTypesetter.suggestForwardBreak(lineStart, textLength, layoutWidth, BreakMode.LINE);
                composedLine = mTypesetter.createSimpleLine(lineStart, lineEnd);

                float lineWidth = composedLine.getWidth();
                float lineHeight = getLineHeight(composedLine);

                if ((textHeight + lineHeight) <= layoutHeight && mComposedLines.size() < maxLines) {
                    textWidth = Math.max(textWidth, lineWidth);
                    textHeight += lineHeight;
                    mComposedLines.add(composedLine);

                    lineStart = lineEnd;
                } else {
                    if (mTruncationPlace != null) {
                        ComposedLine lastLine = mComposedLines.get(mComposedLines.size() - 1);

                        // Replace the last line with truncated one.
                        ComposedLine truncatedLine = mTypesetter.createTruncatedLine(lastLine.getCharStart(), textLength, layoutWidth, mTruncationMode, mTruncationPlace);
                        mComposedLines.set(mComposedLines.size() - 1, truncatedLine);
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

            Typeface typeface = getTypeface();
            float textSize = getTextSize();

            if (mSpanned != null) {
                List<Object> defaultSpans = new ArrayList<>();
                if (typeface != null) {
                    defaultSpans.add(new TypefaceSpan(typeface));
                }
                defaultSpans.add(new TypeSizeSpan(textSize));

                mTypesetter = new Typesetter(mSpanned, defaultSpans);
            } else {
                if (typeface != null && mText.length() > 0) {
                    mTypesetter = new Typesetter(mText, typeface, mRenderer.getTypeSize());
                }
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
        setTypeface(TypefaceManager.getTypeface(tag));
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
        mSpanned = null;
        updateTypesetter();
    }

    public Spanned getSpanned() {
        return mSpanned;
    }

    public void setSpanned(Spanned spanned) {
        mText = "";
        mSpanned = spanned;
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
     * Returns the truncation mode that should be used on the last line of the text in case of
     * overflow.
     *
     * @return The current truncation mode.
     */
    public BreakMode getTruncationMode() {
        return mTruncationMode;
    }

    /**
     * Sets the truncation mode that should be used on the last line of text in case of overflow.
     *
     * @param truncationMode A value of {@link BreakMode}.
     */
    public void setTruncationMode(BreakMode truncationMode) {
        mTruncationMode = (truncationMode == null ? BreakMode.LINE : truncationMode);
        requestLayout();
        invalidate();
    }

    /**
     * Returns the truncation place for the last line of the text.
     *
     * @return The current truncation place.
     */
    public TruncationPlace getTruncationPlace() {
        return mTruncationPlace;
    }

    /**
     * Sets the truncation place for the last line of the text.
     * <p>
     * The truncation is disabled if the value of <code>truncationPlace</code> is <code>null</code>.
     *
     * @param truncationPlace A value of {@link TruncationPlace}.
     */
    public void setTruncationPlace(TruncationPlace truncationPlace) {
        mTruncationPlace = truncationPlace;
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
