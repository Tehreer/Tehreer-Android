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

package com.mta.tehreer.layout.style;

import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import com.mta.tehreer.graphics.Typeface;

/**
 * The <code>ConcreteTypefaceSpan</code> class represents a span for specifying particular typeface.
 */
public class TypefaceSpan extends MetricAffectingSpan {

    private final Typeface typeface;

    /**
     * Constructs a concrete typeface span object.
     *
     * @param typeface The typeface object.
     */
    public TypefaceSpan(Typeface typeface) {
        this.typeface = typeface;
    }

    /**
     * Returns this span's typeface.
     *
     * @return The typeface of this span.
     */
    public Typeface getTypeface() {
        return typeface;
    }

    @Override
    public void updateMeasureState(TextPaint textPaint) {
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
    }
}
