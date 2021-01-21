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

package com.mta.tehreer.graphics;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

final class GlyphAttributes {
    public static final int LINECAP_BUTT = 0;
    public static final int LINECAP_ROUND = 1;
    public static final int LINECAP_SQUARE = 2;

    public static final int LINEJOIN_ROUND = 0;
    public static final int LINEJOIN_BEVEL = 1;
    public static final int LINEJOIN_MITER_VARIABLE = 2;
    public static final int LINEJOIN_MITER_FIXED = 3;

    @IntDef({
        LINECAP_BUTT,
        LINECAP_ROUND,
        LINECAP_SQUARE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface LineCap { }

    @IntDef({
        LINEJOIN_ROUND,
        LINEJOIN_BEVEL,
        LINEJOIN_MITER_VARIABLE,
        LINEJOIN_MITER_FIXED
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface LineJoin { }

    private final @NonNull GlyphKey.Data dataKey = new GlyphKey.Data();
    private final @NonNull GlyphKey.Color colorKey = new GlyphKey.Color();
    private final @NonNull GlyphKey.Stroke strokeKey = new GlyphKey.Stroke();

    public void setTypeface(Typeface typeface) {
        dataKey.typeface = typeface;
    }

    public void setPixelWidth(float pixelWidth) {
        dataKey.pixelWidth = (int) ((pixelWidth * 64.0f) + 0.5f);
    }

    public void setPixelHeight(float pixelHeight) {
        dataKey.pixelHeight = (int) ((pixelHeight * 64.0f) + 0.5f);
    }

    public void setSkewX(float skewX) {
        dataKey.skewX = (int) ((skewX * 0x10000) + 0.5f);
    }

    public void setForegroundColor(@ColorInt int foregroundColor) {
        colorKey.foregroundColor = foregroundColor;
    }

    public void setLineRadius(float lineRadius) {
        strokeKey.lineRadius = (int) ((lineRadius * 64.0f) + 0.5f);
    }

    public void setLineCap(@LineCap int lineCap) {
        strokeKey.lineCap = lineCap;
    }

    public void setLineJoin(@LineJoin int lineJoin) {
        strokeKey.lineJoin = lineJoin;
    }

    public void setMiterLimit(float miterLimit) {
        strokeKey.miterLimit = (int) ((miterLimit * 0x10000) + 0.5f);
    }

    public boolean isRenderable() {
        // Minimum size supported by FreeType is 64x64.
        return (dataKey.pixelWidth >= 64 && dataKey.pixelHeight >= 64);
    }

    public @NonNull GlyphKey.Data dataKey() {
        return dataKey;
    }

    public @NonNull GlyphKey.Color colorKey() {
        colorKey.set(dataKey);
        return colorKey;
    }

    public @NonNull GlyphKey.Stroke strokeKey() {
        strokeKey.set(dataKey);
        return strokeKey;
    }
}
