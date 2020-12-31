/*
 * Copyright (C) 2016-2020 Muhammad Tayyab Akram
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class GlyphStrike implements Cloneable {
    public Typeface typeface;
    public int pixelWidth;      // 26.6 fixed-point value.
    public int pixelHeight;     // 26.6 fixed-point value.
    public int skewX;           // 16.16 fixed-point value.

    public GlyphStrike color(@ColorInt int foregroundColor) {
        Color strike = new Color();
        strike.typeface = typeface;
        strike.pixelWidth = pixelWidth;
        strike.pixelHeight = pixelHeight;
        strike.skewX = skewX;
        strike.foregroundColor = foregroundColor;

        return strike;
    }

    @Override
    public @NonNull GlyphStrike clone() {
        try {
            return (GlyphStrike) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlyphStrike)) {
            return false;
        }

        GlyphStrike other = (GlyphStrike) obj;

        return (typeface != null ? typeface.equals(other.typeface) : other.typeface == null)
            && pixelWidth == other.pixelWidth
            && pixelHeight == other.pixelHeight
            && skewX == other.skewX;
    }

    @Override
    public int hashCode() {
        int result = typeface != null ? typeface.hashCode() : 0;
        result = 31 * result + pixelWidth;
        result = 31 * result + pixelHeight;
        result = 31 * result + skewX;

        return result;
    }

    private static final class Color extends GlyphStrike {
        public @ColorInt int foregroundColor;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            if (!super.equals(obj)) {
                return false;
            }

            Color other = (Color) obj;

            return foregroundColor == other.foregroundColor;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + foregroundColor;

            return result;
        }
    }
}
