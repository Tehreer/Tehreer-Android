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
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

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

class TextContainer extends ViewGroup {
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

    private Rect mVisibleRect = new Rect();
    private int mScrollX;
    private int mScrollY;
    private int mScrollWidth;
    private int mScrollHeight;

    private ArrayList<LineView> mLineViews = new ArrayList<>();
    private ArrayList<LineView> mInsideViews = new ArrayList<>();
    private ArrayList<LineView> mOutsideViews = new ArrayList<>();

    private ArrayList<Rect> mLineBoxes = new ArrayList<>();
    private ArrayList<Integer> mVisibleIndexes = new ArrayList<>();

    private boolean mNeedsLayout = false;

    private final Handler handler = new Handler(Looper.getMainLooper());

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
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(widthSize, mTextHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mNeedsLayout) {
            layoutLines();
            mNeedsLayout = false;
        }
    }

    private void layoutLines() {
        if (mComposedFrame == null) {
            return;
        }

        mVisibleRect.set(mScrollX, mScrollY, mScrollX + mScrollWidth, mScrollY + mScrollHeight);

        mInsideViews.clear();
        mOutsideViews.clear();

        // Get outside and inside line views.
        for (LineView lineView : mLineViews) {
            if (Rect.intersects(lineView.getFrame(), mVisibleRect)) {
                mInsideViews.add(lineView);
            } else {
                mOutsideViews.add(lineView);
            }
        }

        mVisibleIndexes.clear();

        // Get line indexes that should be visible.
        for (int i = 0, count = mLineBoxes.size(); i < count; i++) {
            if (Rect.intersects(mLineBoxes.get(i), mVisibleRect)) {
                mVisibleIndexes.add(i);
            }
        }

        List<ComposedLine> allLines = mComposedFrame.getLines();

        // Layout the lines.
        for (int i = 0, count = mVisibleIndexes.size(); i < count; i++) {
            int index = mVisibleIndexes.get(i);
            ComposedLine textLine = allLines.get(index);
            LineView insideView = null;
            LineView lineView;

            for (int j = 0, size = mInsideViews.size(); j < size; j++) {
                LineView view = mInsideViews.get(j);
                if (view.getLine() == textLine) {
                    insideView = view;
                    break;
                }
            }

            if (insideView != null) {
                lineView = insideView;
            } else {
                int outsideCount = mOutsideViews.size();
                if (outsideCount > 0) {
                    lineView = mOutsideViews.get(outsideCount - 1);
                    mOutsideViews.remove(outsideCount - 1);
                } else {
                    lineView = new LineView(getContext());
                    lineView.setBackgroundColor(Color.TRANSPARENT);
                    mLineViews.add(lineView);
                }

                updateRenderer(lineView.getRenderer());

                lineView.setLine(textLine);
            }

            if (lineView.getParent() == null) {
                addView(lineView);
            }

            lineView.bringToFront();

            Rect lineBox = mLineBoxes.get(index);
            lineView.layout(lineBox.left, lineBox.top, lineBox.right, lineBox.bottom);
        }
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

            mLineBoxes.clear();

            Renderer renderer = new Renderer();
            updateRenderer(renderer);

            for (ComposedLine line : mComposedFrame.getLines()) {
                RectF box = line.computeBoundingBox(renderer);
                box.offset(line.getOriginX(), line.getOriginY());

                Rect small = new Rect((int) box.left, (int) box.top, (int) box.right, (int) box.bottom);

                mLineBoxes.add(small);
            }

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

    private void updateRenderer(@NonNull Renderer renderer) {
        renderer.setFillColor(mTextColor);
        renderer.setTypeface(mTypeface);
        renderer.setTypeSize(mTextSize);
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

    public void setScrollPosition(int scrollX, int scrollY) {
        boolean needsLayout = false;

        if (scrollX != mScrollX) {
            mScrollX = scrollX;
            needsLayout = true;
        }
        if (scrollY != mScrollY) {
            mScrollY = scrollY;
            needsLayout = true;
        }

        if (needsLayout) {
            mNeedsLayout = true;
            requestLayout();
        }
    }

    public void setVisibleRegion(final int width, int height) {
        mScrollWidth = width;
        mScrollHeight = height;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateFrame(0, 0, width, Float.POSITIVE_INFINITY);
                mNeedsLayout = true;
                requestLayout();
            }
        }, 1);
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
