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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ScrollView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.mta.tehreer.R;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.layout.ComposedFrame;
import com.mta.tehreer.layout.Typesetter;

/**
 * A scrollable, multiline text region.
 */
public class TTextView extends ScrollView {
    private TextContainer mTextContainer;

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

        TypedArray values = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TTextView, defStyleAttr, 0);

        try {
            setGravity(values.getInt(R.styleable.TTextView_gravity, Gravity.TOP | Gravity.START));
            setExtraLineSpacing(values.getDimension(R.styleable.TTextView_extraLineSpacing, 0.0f));
            setLineHeightMultiplier(values.getFloat(R.styleable.TTextView_lineHeightMultiplier, 0.0f));
            setTextColor(values.getColor(R.styleable.TTextView_textColor, Color.BLACK));
            setTextSize(values.getDimension(R.styleable.TTextView_textSize, 16));
            if (values.hasValue(R.styleable.TTextView_typeface)) {
                setTypeface(values.getResourceId(R.styleable.TTextView_typeface, 0));
            }

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
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        mTextContainer.setScrollPosition(l, t);
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
        return mTextContainer.hitTestPosition(x, y);
    }

    /**
     * Sets the horizontal alignment of the text and the vertical gravity that will be used when
     * there is extra space in the Label beyond what is required for the text itself.
     *
     * @param gravity The horizontal and vertical alignment.
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
}
