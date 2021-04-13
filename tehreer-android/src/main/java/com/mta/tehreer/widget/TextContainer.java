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
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.util.SmartRunnable;
import com.mta.tehreer.layout.ComposedFrame;
import com.mta.tehreer.layout.ComposedLine;
import com.mta.tehreer.layout.FrameResolver;
import com.mta.tehreer.layout.TextAlignment;
import com.mta.tehreer.layout.Typesetter;
import com.mta.tehreer.layout.style.TypeSizeSpan;
import com.mta.tehreer.layout.style.TypefaceSpan;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

    private int mLayoutWidth = 0;
    private @Nullable ComposedFrame mComposedFrame = null;

    private ScrollView mScrollView;
    private int mScrollX;
    private int mScrollY;
    private int mScrollWidth;
    private int mScrollHeight;

    private Rect mVisibleRect = new Rect();

    private boolean mIsTextLayoutRequested = false;
    private boolean mIsTypesetterUserDefined = false;
    private boolean mIsTypesetterResolved = false;
    private boolean mIsComposedFrameResolved = false;

    private ArrayList<LineView> mLineViews = new ArrayList<>();
    private ArrayList<LineView> mInsideViews = new ArrayList<>();
    private ArrayList<LineView> mOutsideViews = new ArrayList<>();

    private List<Rect> mLineBoxes = new ArrayList<>();
    private ArrayList<Integer> mVisibleIndexes = new ArrayList<>();

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Executor mExecutor = Executors.newCachedThreadPool();

    private TextResolvingTask mTextTask;
    private Object mLayoutID;

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

    public void setScrollView(ScrollView scrollView) {
        mScrollView = scrollView;
    }

    public void setScrollPosition(int scrollX, int scrollY) {
        boolean scrollChanged = false;

        if (scrollX != mScrollX) {
            mScrollX = scrollX;
            scrollChanged = true;
        }
        if (scrollY != mScrollY) {
            mScrollY = scrollY;
            scrollChanged = true;
        }

        if (scrollChanged) {
            layoutLines();
        }
    }

    public void setVisibleRegion(int width, int height) {
        if (width != mScrollWidth) {
            mScrollWidth = width;
            requestComposedFrame();
        }

        mScrollHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = 0;

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = 0;
        }

        if (mComposedFrame != null) {
            heightSize = (int) Math.ceil(mComposedFrame.getHeight());
        }

        setMeasuredDimension(widthSize, heightSize);

        if (mLayoutWidth != widthSize) {
            mLayoutWidth = widthSize;
            requestTextLayout();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mIsTextLayoutRequested) {
            performTextLayout();
        }

        layoutLines();
    }

    private static class TextContext {
        Handler handler;

        Object layoutID;
        int layoutWidth;
        Typeface typeface;
        String text;
        Spanned spanned;
        float textSize;
        @ColorInt int textColor;
        TextAlignment textAlignment;
        float extraLineSpacing;
        float lineHeightMultiplier;

        Typesetter typesetter;
        ComposedFrame composedFrame;
    }

    private interface OnTaskUpdateListener<T> {
        void onUpdate(T object);
    }

    private static class TypesettingTask extends SmartRunnable {
        private final @NonNull TextContext context;
        private final @NonNull OnTaskUpdateListener<Typesetter> listener;

        public TypesettingTask(TextContext context, OnTaskUpdateListener<Typesetter> listener) {
            this.context = context;
            this.listener = listener;
        }

        private void notifyUpdateIfNeeded() {
            if (!isCancelled()) {
                context.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onUpdate(context.typesetter);
                    }
                });
            }
        }

        @Override
        public void run() {
            final String text = context.text;
            final Spanned spanned = context.spanned;

            if (text != null) {
                final Typeface typeface = context.typeface;
                final float textSize = context.textSize;

                if (typeface != null && text.length() > 0) {
                    context.typesetter = new Typesetter(text, typeface, textSize);
                }
            } else if (spanned != null) {
                if (spanned.length() > 0) {
                    final Typeface typeface = context.typeface;
                    final float textSize = context.textSize;

                    List<Object> defaultSpans = new ArrayList<>();

                    if (typeface != null) {
                        defaultSpans.add(new TypefaceSpan(typeface));
                    }
                    defaultSpans.add(new TypeSizeSpan(textSize));

                    context.typesetter = new Typesetter(spanned, defaultSpans);
                }
            }

            notifyUpdateIfNeeded();
        }
    }

    private static class FrameResolvingTask extends SmartRunnable {
        private final @NonNull TextContext context;
        private final @NonNull OnTaskUpdateListener<ComposedFrame> listener;

        public FrameResolvingTask(TextContext context, OnTaskUpdateListener<ComposedFrame> listener) {
            this.context = context;
            this.listener = listener;
        }

        private void notifyUpdateIfNeeded() {
            if (!isCancelled()) {
                context.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onUpdate(context.composedFrame);
                    }
                });
            }
        }

        @Override
        public void run() {
            final Typesetter typesetter = context.typesetter;

            if (typesetter != null) {
                FrameResolver resolver = new FrameResolver();
                resolver.setTypesetter(typesetter);
                resolver.setFrameBounds(new RectF(0.0f, 0.0f, context.layoutWidth, Float.POSITIVE_INFINITY));
                resolver.setFitsHorizontally(false);
                resolver.setFitsVertically(true);
                resolver.setTextAlignment(context.textAlignment);
                resolver.setExtraLineSpacing(context.extraLineSpacing);
                resolver.setLineHeightMultiplier(context.lineHeightMultiplier);

                context.composedFrame = resolver.createFrame(0, typesetter.getSpanned().length());
            }

            notifyUpdateIfNeeded();
        }
    }

    private static class LineBoxesTask extends SmartRunnable {
        private final @NonNull TextContext context;
        private final @NonNull OnTaskUpdateListener<List<Rect>> listener;

        private final @NonNull List<Rect> lineBoxes = new ArrayList<>();

        public LineBoxesTask(TextContext context, OnTaskUpdateListener<List<Rect>> listener) {
            this.context = context;
            this.listener = listener;
        }

        private void notifyUpdateIfNeeded() {
            if (!isCancelled()) {
                final List<Rect> array = new ArrayList<>(lineBoxes);

                context.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onUpdate(array);
                    }
                });
            }
        }

        @Override
        public void run() {
            final ComposedFrame composedFrame = context.composedFrame;
            final List<ComposedLine> lines = composedFrame != null ? composedFrame.getLines() : null;

            if (lines != null) {
                Renderer renderer = new Renderer();
                renderer.setTypeface(context.typeface);
                renderer.setTypeSize(context.textSize);
                renderer.setFillColor(context.textColor);

                int lineChunk = 0;

                for (int i = 0, count = lines.size(); i < count; i++) {
                    final ComposedLine line = lines.get(i);

                    RectF boundingBox = line.computeBoundingBox(renderer);
                    boundingBox.offset(line.getOriginX(), line.getOriginY());

                    lineBoxes.add(new Rect((int) (boundingBox.left + 0.5f), (int) (boundingBox.top + 0.5f),
                                           (int) (boundingBox.right + 0.5f), (int) (boundingBox.bottom + 0.5f)));

                    if (isCancelled()) {
                        break;
                    }

                    if (lineChunk == 64) {
                        notifyUpdateIfNeeded();
                        lineChunk = 0;
                    } else {
                        lineChunk += 1;
                    }
                }
            }

            notifyUpdateIfNeeded();
        }
    }

    private static class TextResolvingTask extends SmartRunnable {
        private final @NonNull Queue<SmartRunnable> subTasks;
        private SmartRunnable currentTask;

        public TextResolvingTask(Queue<SmartRunnable> subTasks) {
            this.subTasks = subTasks;
        }

        private synchronized SmartRunnable poll() {
            currentTask = subTasks.poll();
            return currentTask;
        }

        @Override
        public void run() {
            SmartRunnable runnable;

            while ((runnable = poll()) != null) {
                runnable.run();
            }
        }

        @Override
        public synchronized void cancel() {
            super.cancel();

            Iterator<SmartRunnable> iterator = subTasks.iterator();

            while (iterator.hasNext()) {
                SmartRunnable runnable = iterator.next();
                runnable.cancel();

                iterator.remove();
            }

            if (currentTask != null) {
                currentTask.cancel();
            }
        }
    }

    private void performTextLayout() {
        final TextContext context = new TextContext();
        context.handler = mHandler;
        context.layoutID = mLayoutID;
        context.layoutWidth = mLayoutWidth;
        context.typeface = mTypeface;
        context.text = mText;
        context.spanned = mSpanned;
        context.textSize = mTextSize;
        context.textAlignment = mTextAlignment;
        context.textColor = mTextColor;
        context.extraLineSpacing = mExtraLineSpacing;
        context.lineHeightMultiplier = mLineHeightMultiplier;
        context.typesetter = mTypesetter;

        Queue<SmartRunnable> subTasks = new ArrayDeque<>();

        if (!mIsTypesetterResolved) {
            subTasks.add(new TypesettingTask(context, new OnTaskUpdateListener<Typesetter>() {
                @Override
                public void onUpdate(Typesetter typesetter) {
                    updateTypesetter(context.layoutID, typesetter);
                }
            }));
        }

        subTasks.add(new FrameResolvingTask(context, new OnTaskUpdateListener<ComposedFrame>() {
            @Override
            public void onUpdate(ComposedFrame composedFrame) {
                updateComposedFrame(context.layoutID, composedFrame);
            }
        }));

        subTasks.add(new LineBoxesTask(context, new OnTaskUpdateListener<List<Rect>>() {
            @Override
            public void onUpdate(List<Rect> lineBoxes) {
                updateLineBoxes(context.layoutID, lineBoxes);
            }
        }));

        mTextTask = new TextResolvingTask(subTasks);
        mExecutor.execute(mTextTask);

        mIsTextLayoutRequested = false;
    }

    private void updateTypesetter(Object layoutID, Typesetter typesetter) {
        if (layoutID == mLayoutID) {
            mIsTypesetterResolved = true;
            mTypesetter = typesetter;
        }
    }

    private void updateComposedFrame(Object layoutID, ComposedFrame composedFrame) {
        if (layoutID == mLayoutID) {
            mIsComposedFrameResolved = true;
            mComposedFrame = composedFrame;

            mLineBoxes.clear();
            mLineViews.clear();
            removeAllViews();

            mScrollView.scrollTo(0, 0);
            layoutLines();
        }
    }

    private void updateLineBoxes(Object layoutID, List<Rect> lineBoxes) {
        if (layoutID == mLayoutID) {
            mLineBoxes = lineBoxes;
            layoutLines();
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

    private void requestTypesetter() {
        mIsTypesetterResolved = mIsTypesetterUserDefined;
        requestComposedFrame();
    }

    private void requestComposedFrame() {
        mIsComposedFrameResolved = false;
        requestTextLayout();
    }

    private void requestTextLayout() {
        if (mTextTask != null) {
            mTextTask.cancel();
        }

        mLayoutID = new Object();
        mIsTextLayoutRequested = true;

        requestLayout();
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

        requestComposedFrame();
    }

    public ComposedFrame getComposedFrame() {
        return mIsComposedFrameResolved ? mComposedFrame : null;
    }

    public Typesetter getTypesetter() {
        return mIsTypesetterResolved ? mTypesetter : null;
    }

    public void setTypesetter(Typesetter typesetter) {
        mText = null;
        mSpanned = null;
        mTypesetter = typesetter;
        mIsTypesetterUserDefined = true;

        requestTypesetter();
    }

    public Spanned getSpanned() {
        return mSpanned;
    }

    public void setSpanned(Spanned spanned) {
        mText = null;
        mSpanned = spanned;
        mIsTypesetterUserDefined = false;

        requestTypesetter();
    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    public void setTypeface(Typeface typeface) {
        mTypeface = typeface;
        requestTypesetter();
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = (text == null ? "" : text);
        mSpanned = null;
        mIsTypesetterUserDefined = false;

        requestTypesetter();
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        mTextSize = Math.max(0.0f, textSize);
        requestTypesetter();
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
        requestComposedFrame();
    }

    public float getLineHeightMultiplier() {
        return mLineHeightMultiplier;
    }

    public void setLineHeightMultiplier(float lineHeightMultiplier) {
        mLineHeightMultiplier = lineHeightMultiplier;
        requestComposedFrame();
    }
}
