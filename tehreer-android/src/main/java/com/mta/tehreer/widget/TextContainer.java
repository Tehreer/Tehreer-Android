/*
 * Copyright (C) 2021 Muhammad Tayyab Akram
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.layout.ComposedFrame;
import com.mta.tehreer.layout.ComposedLine;
import com.mta.tehreer.layout.FrameResolver;
import com.mta.tehreer.layout.TextAlignment;
import com.mta.tehreer.layout.Typesetter;
import com.mta.tehreer.layout.style.TypeSizeSpan;
import com.mta.tehreer.layout.style.TypefaceSpan;

import java.util.ArrayList;
import java.util.List;

class TextContainer extends View {
    private @Nullable Typeface mTypeface = null;
    private @Nullable String mText = null;
    private @Nullable Spanned mSpanned = null;
    private @Nullable Typesetter mTypesetter = null;
    private @FloatRange(from = 0.0) float mTextSize = 16.0f;
    private @ColorInt int mTextColor = Color.BLACK;
    private @NonNull TextAlignment mTextAlignment = TextAlignment.INTRINSIC;
    private float mExtraLineSpacing = 0.0f;
    private float mLineHeightMultiplier = 0.0f;

    private boolean mNeedsTypesetter = false;
    private int mTextWidth = 0;
    private int mTextHeight = 0;

    private @NonNull RectF mLayoutRect = new RectF();
    private @Nullable ComposedFrame mComposedFrame = null;

    private int mScrollLeft;
    private int mScrollTop;
    private int mScrollWidth;
    private int mScrollHeight;

    public TextContainer(Context context) {
        super(context);
        setup(context, null, 0);
    }

    public TextContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0);
    }

    public TextContainer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr);
    }

    private void setup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        setBackgroundColor(Color.RED);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);

        float layoutWidth = (widthMode == View.MeasureSpec.UNSPECIFIED ? Float.POSITIVE_INFINITY : widthSize);
        float layoutHeight = (heightMode == View.MeasureSpec.UNSPECIFIED ? Float.POSITIVE_INFINITY : heightSize);

        updateFrame(0.0f, 0.0f, layoutWidth, layoutHeight);

        setMeasuredDimension(mTextWidth, mTextHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        mScrollLeft = l;
        mScrollTop = l;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long t1 = System.nanoTime();

        canvas.save();

        if (mComposedFrame != null) {
            Renderer renderer = new Renderer();
            renderer.setTypeface(mTypeface);
            renderer.setTypeSize(mTextSize);
            renderer.setFillColor(mTextColor);

            mComposedFrame.draw(renderer, canvas, mComposedFrame.getOriginX(), mComposedFrame.getOriginY());
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

            FrameResolver resolver = new FrameResolver();
            resolver.setTypesetter(mTypesetter);
            resolver.setFrameBounds(mLayoutRect);
            resolver.setFitsHorizontally(false);
            resolver.setFitsVertically(true);
            resolver.setTextAlignment(mTextAlignment);
            resolver.setExtraLineSpacing(mExtraLineSpacing);
            resolver.setLineHeightMultiplier(mLineHeightMultiplier);
            resolver.setTypesetter(mTypesetter);

            mComposedFrame = resolver.createFrame(0, mTypesetter.getSpanned().length());

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

    public void setVisibleRegion(int width, int height) {
        mScrollWidth = width;
        mScrollHeight = height;
    }

    public void setGravity(int gravity) {
        int horizontalGravity = gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;

        TextAlignment textAlignment;

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
                textAlignment = TextAlignment.EXTRINSIC;
                break;

            default:
                textAlignment = TextAlignment.INTRINSIC;
                break;
        }

        mTextAlignment = textAlignment;

        requestLayout();
        invalidate();
    }

    public ComposedFrame getComposedFrame() {
        return mComposedFrame;
    }

    public Typesetter getTypesetter() {
        return mTypesetter;
    }

    public void setTypesetter(Typesetter typesetter) {
        mText = null;
        mSpanned = null;
        mTypesetter = typesetter;
        mNeedsTypesetter = true;

        requestLayout();
        invalidate();
    }

    public Spanned getSpanned() {
        return mSpanned;
    }

    public void setSpanned(Spanned spanned) {
        mText = null;
        mSpanned = spanned;
        mNeedsTypesetter = false;
        updateTypesetter();
    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    public void setTypeface(Typeface typeface) {
        mTypeface = typeface;
        updateTypesetter();
    }

    private void setTypeface(@NonNull Object tag) {
        setTypeface(TypefaceManager.getTypeface(tag));
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = (text == null ? "" : text);
        mSpanned = null;
        mNeedsTypesetter = false;
        updateTypesetter();
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        mTextSize = Math.max(0.0f, textSize);
        updateTypesetter();
    }

    public @ColorInt int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        mTextColor = textColor;
        invalidate();
    }

    public float getExtraLineSpacing() {
        return mExtraLineSpacing;
    }

    public void setExtraLineSpacing(float extraLineSpacing) {
        mExtraLineSpacing = extraLineSpacing;
        requestLayout();
        invalidate();
    }

    public float getLineHeightMultiplier() {
        return mLineHeightMultiplier;
    }

    public void setLineHeightMultiplier(float lineHeightMultiplier) {
        mLineHeightMultiplier = lineHeightMultiplier;
        requestLayout();
        invalidate();
    }
}
