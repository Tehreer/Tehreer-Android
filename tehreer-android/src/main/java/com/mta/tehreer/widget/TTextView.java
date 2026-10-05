/*
 * Copyright (C) 2021-2026 Muhammad Tayyab Akram
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
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;

import com.mta.tehreer.R;
import com.mta.tehreer.graphics.RenderingStyle;
import com.mta.tehreer.graphics.StrokeCap;
import com.mta.tehreer.graphics.StrokeJoin;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.layout.ComposedFrame;
import com.mta.tehreer.layout.Typesetter;
import com.mta.tehreer.layout.style.ViewSpan;

import java.util.List;

/**
 * A scrollable, multiline text region.
 *
 * <p>
 * The standard padding of a view applies to the text, and, unlike a plain <code>ScrollView</code>,
 * <code>clipToPadding</code> is <code>false</code> by default so that the text scrolls through the
 * padding rather than being cut off at its edge. Set <code>android:clipToPadding</code> or call
 * {@link #setClipToPadding(boolean)} to change it.
 *
 * <p>
 * Clickable spans (including <code>URLSpan</code>) in the text respond to a tap, and any span can
 * be reported to an {@link OnSpanClickListener}. The position of a touch is found with
 * {@link #getCharIndexForPosition(float, float)}, and the place a span occupies with
 * {@link #getSpanBounds(Object)} and {@link #getSpanRects(Object)}; all three work in the
 * coordinates of this view, which are the ones a <code>MotionEvent</code> uses.
 *
 * <p>
 * A {@link ViewSpan} in the text puts a real view in it.
 */
public class TTextView extends ScrollView {
    /**
     * Interface definition for a callback to be invoked when a span of the text is clicked.
     */
    public interface OnSpanClickListener {
        /**
         * Called when a span is clicked. The spans that can be clicked are the clickable spans
         * (unless {@link #setLinksClickable(boolean)} is turned off) and the replacement spans,
         * such as inline images. A link under a replacement span is not clickable.
         *
         * @param view The text view containing the span.
         * @param span The span that was clicked.
         * @return <code>true</code> if the click was consumed. Otherwise, a clickable span will
         *         handle the click itself, by calling its <code>onClick()</code>.
         */
        boolean onSpanClick(TTextView view, Object span);
    }

    private static final int[] CLIP_TO_PADDING_ATTRS = { android.R.attr.clipToPadding };
    private static final int DEFAULT_HIGHLIGHT_COLOR = 0x6633B5E5;

    private TextContainer mTextContainer;
    private SpanTouchHandler mSpanTouchHandler;

    private OnSpanClickListener mOnSpanClickListener;
    private boolean mLinksClickable = true;
    private int mHighlightColor = DEFAULT_HIGHLIGHT_COLOR;

    public TTextView(Context context) {
        super(context);
        setup(context, null, 0, 0);
    }

    public TTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs, 0, 0);
    }

    public TTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup(context, attrs, defStyleAttr, defStyleRes);
    }

    private void setup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mTextContainer = new TextContainer(context);
        mTextContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mTextContainer.setScrollView(this);
        addView(mTextContainer);

        mSpanTouchHandler = new SpanTouchHandler(this);

        // The text scrolls through the padding, so the padding is not a clip region unless it is
        // asked to be.
        TypedArray clipValues = context.obtainStyledAttributes(attrs, CLIP_TO_PADDING_ATTRS, defStyleAttr, defStyleRes);
        try {
            if (!clipValues.hasValue(0)) {
                setClipToPadding(false);
            }
        } finally {
            clipValues.recycle();
        }

        TypedValue highlightValue = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.textColorHighlight, highlightValue, true)
                && highlightValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && highlightValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            mHighlightColor = highlightValue.data;
        }

        TypedArray values = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TTextView, defStyleAttr, defStyleRes);

        try {
            setGravity(values.getInt(R.styleable.TTextView_gravity, Gravity.TOP | Gravity.START));
            setExtraLineSpacing(values.getDimension(R.styleable.TTextView_extraLineSpacing, 0.0f));
            setLineHeightMultiplier(values.getFloat(R.styleable.TTextView_lineHeightMultiplier, 0.0f));
            setTextColor(values.getColor(R.styleable.TTextView_textColor, Color.BLACK));
            setTextSize(values.getDimension(R.styleable.TTextView_textSize, 16));
            if (values.hasValue(R.styleable.TTextView_typeface)) {
                setTypeface(values.getResourceId(R.styleable.TTextView_typeface, 0));
            }

            RenderingStyle renderingStyle = null;
            switch (values.getInt(R.styleable.TTextView_renderingStyle, 0)) {
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
            switch (values.getInt(R.styleable.TTextView_strokeCap, 0)) {
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
            switch (values.getInt(R.styleable.TTextView_strokeJoin, 0)) {
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

            setRenderingStyle(renderingStyle);
            setStrokeColor(values.getColor(R.styleable.TTextView_strokeColor, Color.BLACK));
            setStrokeWidth(values.getDimension(R.styleable.TTextView_strokeWidth, 0.0f));
            setStrokeCap(strokeCap);
            setStrokeJoin(strokeJoin);
            setStrokeMiter(values.getDimension(R.styleable.TTextView_strokeMiter, 1.0f));

            CharSequence text = values.getText(R.styleable.TTextView_text);
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTextContainer.setVisibleRegion(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        // The viewport may have changed (its size, its padding) without the text container being
        // laid out again.
        mTextContainer.onScrollViewScrolled();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        mTextContainer.onScrollViewScrolled();
    }

    @Override
    protected void onDetachedFromWindow() {
        mSpanTouchHandler.cancel();
        super.onDetachedFromWindow();
    }

    /**
     * Redraws this view, and with it the lines of text, which are child views that draw themselves.
     * A span that draws differently after a change of its own (a bitmap that is hidden, say) is
     * shown as changed by calling this method.
     */
    @Override
    public void invalidate() {
        super.invalidate();

        // It can be called by the super constructors.
        if (mTextContainer != null) {
            mTextContainer.invalidateLines();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // Behind the text, so that it stays readable.
        mSpanTouchHandler.drawHighlight(canvas, mHighlightColor);
        super.dispatchDraw(canvas);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = super.onInterceptTouchEvent(ev);

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            // The scroll view takes the touch that stops a fling; that is not a tap on a span.
            mSpanTouchHandler.onTouchStarted(intercepted);
        }

        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // The touch is only observed, never taken away from the scroll view.
        boolean pressed = mSpanTouchHandler.onTouchEvent(ev);
        return super.onTouchEvent(ev) || pressed;
    }

    /**
     * Only redraws this view, not the lines of text; that is enough for the pressed state of a
     * link, which is drawn behind them.
     */
    void invalidateHighlight() {
        super.invalidate();
    }

    /**
     * Returns whether the point, in the coordinates of this view, is on the view of a
     * {@link ViewSpan}.
     */
    boolean isInsideViewSpan(float x, float y) {
        return mTextContainer.isInsideViewSpan(x, y);
    }

    /**
     * Returns the spanned that the displayed lines were made from, whether it was set directly or
     * is the source of the typesetter. It is <code>null</code> until the lines are displayed.
     */
    @Nullable Spanned getSourceSpanned() {
        return mTextContainer.getFrameSource();
    }

    /**
     * Returns the index of the character at the specified position: the one of the nearest line,
     * which is found even above the first line or below the last one. It returns -1 if there is no
     * text, or if the position is on the left or on the right of the text of that line.
     *
     * <p>
     * The position is in the coordinates of this view, which is what
     * <code>MotionEvent.getX()</code> and <code>getY()</code> give; scroll, padding and the place
     * of the text inside this view are taken care of.
     *
     * @param x The x- coordinate of position in this view.
     * @param y The y- coordinate of position in this view.
     * @return The index of the character at the specified position, or -1 if there is no character
     *         there.
     */
    public int getCharIndexForPosition(float x, float y) {
        return mTextContainer.getCharIndexForPosition(x, y);
    }

    /**
     * Returns the index of the character that is exactly under the position, or -1 if there is no
     * text there: above the first line or below the last one, in the gaps to the left and right of
     * a line, or in the padding.
     */
    int getCharIndexUnderPosition(float x, float y) {
        return mTextContainer.getCharIndexUnderPosition(x, y);
    }

    /**
     * Returns the index of the first character of the first line that is on the screen, or -1 if
     * no text is displayed. Together with {@link #scrollToCharIndex(int, boolean)} it saves and
     * restores the place where the reader is.
     *
     * <p>
     * When the lines are made again, because of a change of the width, of the size of the text, of
     * the line spacing or of the size of a {@link ViewSpan}, the text view keeps what is on the
     * screen where it is on its own. A new text, set with {@link #setText(String)},
     * {@link #setSpanned(Spanned)} or {@link #setTypesetter(Typesetter)}, starts at the top.
     *
     * @return The index of the first visible character.
     */
    public int getFirstVisibleCharIndex() {
        return mTextContainer.getFirstVisibleCharIndex();
    }

    /**
     * Scrolls so that the line with the specified character is at the top of the text. If the
     * text is not displayed yet, as it is not right after it was set, the scroll is done as soon
     * as it is, instead of the scroll to the top that a new text gets.
     *
     * @param charIndex The index of a character.
     * @param animate Whether to scroll smoothly. It is ignored while the text is not displayed.
     */
    public void scrollToCharIndex(int charIndex, boolean animate) {
        mTextContainer.scrollToCharIndex(charIndex, animate);
    }

    /**
     * Returns the rectangles that the chars from <code>start</code> up to <code>end</code> cover,
     * one for each line they occupy, in the coordinates of this view (scroll applied, so they can
     * be compared with touch positions). They follow the convention of a text selection: when the
     * range continues over several lines, the first rectangle goes on to the edge of the text, and
     * the last one starts from the other edge.
     *
     * <p>
     * The rectangles are returned even if they are scrolled out of the view. An empty list is
     * returned when the range is not in the displayed text or the text is not laid out yet.
     *
     * @param start The index of the first char of the range.
     * @param end The index after the last char of the range.
     * @return The rectangles that the range covers, in the order of the lines.
     */
    public @NonNull List<RectF> getSelectionRects(int start, int end) {
        return mTextContainer.getSelectionRects(start, end);
    }

    /**
     * Returns the rectangles that the specified span covers, one for each line it occupies, in
     * the coordinates of this view (scroll applied, so they can be compared with touch positions).
     * The rectangles of a span that continues over several lines follow the convention of a text
     * selection: the first goes on to the edge of the text, and the last starts from the other
     * edge. A replacement span (an inline image) has the box that it draws in.
     *
     * <p>
     * The rectangles are returned even if they are scrolled out of the view. An empty list is
     * returned when the span is not in the displayed text or the text is not laid out yet.
     *
     * @param span A span of the text being displayed.
     * @return The rectangles that the span covers, in the order of the lines.
     */
    public @NonNull List<RectF> getSpanRects(@NonNull Object span) {
        return mTextContainer.getSpanRects(span);
    }

    /**
     * Returns the smallest rectangle, rounded to whole pixels, that covers the specified span in
     * the coordinates of this view (scroll applied).
     *
     * @param span A span of the text being displayed.
     * @return The bounds of the span, or <code>null</code> if the span is not in the displayed
     *         text or the text is not laid out yet.
     *
     * @see #getSpanRects(Object)
     */
    public @Nullable Rect getSpanBounds(@NonNull Object span) {
        List<RectF> rects = getSpanRects(span);
        if (rects.isEmpty()) {
            return null;
        }

        RectF union = new RectF(rects.get(0));
        for (int i = 1; i < rects.size(); i++) {
            union.union(rects.get(i));
        }

        return new Rect(Math.round(union.left), Math.round(union.top),
                        Math.round(union.right), Math.round(union.bottom));
    }

    /**
     * Returns the listener that is called when a span is clicked.
     *
     * @return The current listener, or <code>null</code> if there is none.
     */
    public @Nullable OnSpanClickListener getOnSpanClickListener() {
        return mOnSpanClickListener;
    }

    /**
     * Sets the listener that is called when a span is clicked. It is called before the click is
     * handled by a clickable span, which can therefore be intercepted, and it is the only way to
     * know about a click on a replacement span, such as an inline image.
     *
     * @param listener The listener to call, or <code>null</code> to remove it.
     */
    public void setOnSpanClickListener(@Nullable OnSpanClickListener listener) {
        mOnSpanClickListener = listener;
    }

    /**
     * Returns whether the clickable spans of the text respond to a tap. The default value is
     * <code>true</code>.
     */
    public boolean getLinksClickable() {
        return mLinksClickable;
    }

    /**
     * Sets whether the clickable spans of the text, including <code>URLSpan</code>, respond to a
     * tap. When they do, a link is highlighted while it is pressed and its <code>onClick()</code>
     * is called (after the {@link OnSpanClickListener}) when the finger is lifted. A
     * <code>URLSpan</code> opens its URL. The default value is <code>true</code>.
     *
     * @param linksClickable Whether the clickable spans should respond to a tap.
     */
    public void setLinksClickable(boolean linksClickable) {
        mLinksClickable = linksClickable;

        if (!linksClickable) {
            mSpanTouchHandler.cancel();
        }
    }

    /**
     * Returns the color that a link is highlighted with while it is pressed.
     */
    public @ColorInt int getHighlightColor() {
        return mHighlightColor;
    }

    /**
     * Sets the color that a link is highlighted with while it is pressed. It is drawn behind the
     * text, so it should be translucent. The default value is the <code>textColorHighlight</code>
     * of the theme, or a translucent blue if there is none.
     *
     * @param highlightColor The highlight color.
     */
    public void setHighlightColor(@ColorInt int highlightColor) {
        mHighlightColor = highlightColor;
        invalidateHighlight();
    }

    /**
     * Returns how far from the screen, in pixels, the view of a {@link ViewSpan} is already made
     * and attached. The default value is 0.
     */
    public int getViewSpanPrefetchDistance() {
        return mTextContainer.getViewSpanPrefetchDistance();
    }

    /**
     * Sets how far above and below the screen, in pixels, the view of a {@link ViewSpan} is made,
     * attached and laid out, rather than when its line reaches the screen. It is what makes a view
     * that needs some time to be ready (a <code>ComposeView</code> is composed once it is attached,
     * and tells its size after that) ready before the reader scrolls to it, so that it does not
     * appear after a blank; and a picture that a view loads is there earlier. The views further
     * than that are not kept (unless the span retains its view), so the distance decides how many
     * views exist at a time. The default value is 0, which is the screen only.
     *
     * @param viewSpanPrefetchDistance The distance in pixels; negative values are taken as 0.
     */
    public void setViewSpanPrefetchDistance(int viewSpanPrefetchDistance) {
        mTextContainer.setViewSpanPrefetchDistance(viewSpanPrefetchDistance);
    }

    /**
     * Sets the horizontal alignment of the text. Only the horizontal part of the gravity is used:
     * the text always starts at the top, as it scrolls.
     *
     * @param gravity The horizontal alignment.
     */
    public void setGravity(int gravity) {
        mTextContainer.setGravity(gravity);
    }

    /**
     * Returns the current composed frame that is being displayed.
     *
     * @return The composed frame being displayed.
     */
    public ComposedFrame getComposedFrame() {
        return mTextContainer.getComposedFrame();
    }

    /**
     * Returns the typesetter that is being used to compose text lines.
     *
     * @return The current typesetter.
     */
    public Typesetter getTypesetter() {
        return mTextContainer.getTypesetter();
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
        mTextContainer.setTypesetter(typesetter);
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
        return mTextContainer.getSpanned();
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
        mTextContainer.setSpanned(spanned);
    }

    /**
     * Returns the current typeface in which the text is being displayed.
     *
     * @return The typeface being used for displaying text.
     */
    public Typeface getTypeface() {
        return mTextContainer.getTypeface();
    }

    /**
     * Sets the typeface in which the text should be displayed.
     *
     * @param typeface The typeface to use for displaying text.
     */
    public void setTypeface(Typeface typeface) {
        mTextContainer.setTypeface(typeface);
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
        return mTextContainer.getText();
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
        mTextContainer.setText(text);
    }

    /**
     * Returns the current text size (in pixels) in which the text is being displayed.
     *
     * @return The text size to use for displaying text.
     */
    public float getTextSize() {
        return mTextContainer.getTextSize();
    }

    /**
     * Set the text size (in pixels) in which the text should be displayed.
     *
     * @param textSize The text size to use for displaying text.
     */
    public void setTextSize(float textSize) {
        mTextContainer.setTextSize(textSize);
    }

    /**
     * Returns the current color in which the text is being displayed.
     *
     * @return The color being used for displaying text.
     */
    public @ColorInt int getTextColor() {
        return mTextContainer.getTextColor();
    }

    /**
     * Sets the color in which the text should be displayed.
     *
     * @param textColor The color to use for displaying text.
     */
    public void setTextColor(@ColorInt int textColor) {
        mTextContainer.setTextColor(textColor);
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
        return mTextContainer.getExtraLineSpacing();
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
        mTextContainer.setExtraLineSpacing(extraLineSpacing);
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
        return mTextContainer.getLineHeightMultiplier();
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
        mTextContainer.setLineHeightMultiplier(lineHeightMultiplier);
    }

    /**
     * Returns whether or not to justify the text lines. The default value is <code>false</code>.
     */
    public boolean isJustificationEnabled() {
        return mTextContainer.isJustificationEnabled();
    }

    /**
     * Sets whether or not to justify the text lines. The default value is <code>false</code>.
     *
     * @param justificationEnabled A boolean value specifying the justification enabled state.
     */
    public void setJustificationEnabled(boolean justificationEnabled) {
        mTextContainer.setJustificationEnabled(justificationEnabled);
    }

    /**
     * Returns the justification level which can range from 0.0 to 1.0. A lower value increases the
     * tightness between words while a higher value decreases it. The default value is
     * <code>1.0f</code>.
     *
     * @return The current justification level.
     */
    public @FloatRange(from = 0.0, to = 1.0) float getJustificationLevel() {
        return mTextContainer.getJustificationLevel();
    }

    /**
     * Sets the justification level which can range from 0.0 to 1.0. A lower value increases the
     * tightness between words while a higher value decreases it. The default value is
     * <code>1.0f</code>.
     *
     * @param justificationLevel Justification level.
     */
    public void setJustificationLevel(@FloatRange(from = 0.0, to = 1.0) float justificationLevel) {
        mTextContainer.setJustificationLevel(justificationLevel);
    }

    /**
     * Returns the color being used to display a separator line below each rendered text line. The
     * default value is <code>Color.TRANSPARENT</code>.
     *
     * @return The current separator color.
     */
    public @ColorInt int getSeparatorColor() {
        return mTextContainer.getSeparatorColor();
    }

    /**
     * Sets the color to display a separator line below each rendered text line. The default value
     * is <code>Color.TRANSPARENT</code>.
     *
     * @param separatorColor The separator color.
     */
    public void setSeparatorColor(@ColorInt int separatorColor) {
        mTextContainer.setSeparatorColor(separatorColor);
    }

    /**
     * Returns the rendering style, used for controlling how text should appear while drawing. The
     * default value is {@link RenderingStyle#FILL}.
     *
     * @return The rendering style of the text.
     */
    public RenderingStyle getRenderingStyle() {
        return mTextContainer.getRenderingStyle();
    }

    /**
     * Sets the rendering style, used for controlling how text should appear while drawing. The
     * default value is {@link RenderingStyle#FILL}.
     *
     * @param renderingStyle The new style setting for the text.
     */
    public void setRenderingStyle(RenderingStyle renderingStyle) {
        mTextContainer.setRenderingStyle(renderingStyle == null ? RenderingStyle.FILL : renderingStyle);
    }

    /**
     * Returns the stroke color for text. The default value is <code>Color.BLACK</code>.
     *
     * @return The stroke color expressed as ARGB integer.
     */
    public @ColorInt int getStrokeColor() {
        return mTextContainer.getStrokeColor();
    }

    /**
     * Sets the stroke color for text. The default value is <code>Color.BLACK</code>.
     *
     * @param strokeColor The 32-bit value of color expressed as ARGB.
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        mTextContainer.setStrokeColor(strokeColor);
    }

    /**
     * Returns the stroke width for text.
     *
     * @return The stroke width in pixels.
     */
    public @Px float getStrokeWidth() {
        return mTextContainer.getStrokeWidth();
    }

    /**
     * Sets the stroke width for text.
     *
     * @param strokeWidth The stroke width in pixels.
     */
    public void setStrokeWidth(@Px float strokeWidth) {
        mTextContainer.setStrokeWidth(strokeWidth);
    }

    /**
     * Returns the cap, controlling how the start and end of stroked lines and paths are treated.
     * The default value is {@link StrokeCap#BUTT}.
     *
     * @return The stroke cap style for text.
     */
    public StrokeCap getStrokeCap() {
        return mTextContainer.getStrokeCap();
    }

    /**
     * Sets the cap, controlling how the start and end of stroked lines and paths are treated. The
     * default value is {@link StrokeCap#BUTT}.
     *
     * @param strokeCap The new stroke cap style for text.
     */
    public void setStrokeCap(StrokeCap strokeCap) {
        mTextContainer.setStrokeCap(strokeCap == null ? StrokeCap.BUTT : strokeCap);
    }

    /**
     * Returns the stroke join type for text. The default value is {@link StrokeJoin#ROUND}.
     *
     * @return The stroke join type.
     */
    public StrokeJoin getStrokeJoin() {
        return mTextContainer.getStrokeJoin();
    }

    /**
     * Sets the stroke join type for text. The default value is {@link StrokeJoin#ROUND}.
     *
     * @param strokeJoin The new stroke join type for text.
     */
    public void setStrokeJoin(StrokeJoin strokeJoin) {
        mTextContainer.setStrokeJoin(strokeJoin == null ? StrokeJoin.ROUND : strokeJoin);
    }

    /**
     * Returns the stroke miter limit in pixels. This is used to control the behavior of miter joins
     * when the joins angle is sharp. The default value is 1.
     *
     * @return The stroke miter limit in pixels.
     */
    public @Px float getStrokeMiter() {
        return mTextContainer.getStrokeMiter();
    }

    /**
     * Sets the stroke miter limit in pixels. This is used to control the behavior of miter joins
     * when the joins angle is sharp. The default value is 1.
     *
     * @param strokeMiter The stroke miter limit in pixels.
     */
    public void setStrokeMiter(@Px float strokeMiter) {
        mTextContainer.setStrokeMiter(strokeMiter);
    }
}
