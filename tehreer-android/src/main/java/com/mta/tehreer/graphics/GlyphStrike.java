/*
 * Copyright (C) 2016-2021 Muhammad Tayyab Akram
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

abstract class GlyphStrike implements Cloneable {
    public Typeface typeface;
    public int pixelWidth;      // 26.6 fixed-point value.
    public int pixelHeight;     // 26.6 fixed-point value.
    public int skewX;           // 16.16 fixed-point value.

    protected GlyphStrike() { }

    protected GlyphStrike(@NonNull GlyphStrike strike) {
        this.typeface = strike.typeface;
        this.pixelWidth = strike.pixelWidth;
        this.pixelHeight = strike.pixelHeight;
        this.skewX = strike.skewX;
    }

    public abstract GlyphStrike clone();

    protected boolean equals(@NonNull GlyphStrike other) {
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

    public static final class Data extends GlyphStrike {
        public Data() { }

        public Data(Data strike) {
            super(strike);
        }

        @Override
        public @NonNull Data clone() {
            return new Data(this);
        }

        public @NonNull Color color(@ColorInt int foregroundColor) {
            Color strike = new Color();
            strike.typeface = typeface;
            strike.pixelWidth = pixelWidth;
            strike.pixelHeight = pixelHeight;
            strike.skewX = skewX;
            strike.foregroundColor = foregroundColor;

            return strike;
        }

        public @NonNull Stroke stroke(int lineRadius, @GlyphAttributes.LineCap int lineCap,
                                      @GlyphAttributes.LineJoin int lineJoin, int miterLimit) {
            Stroke strike = new Stroke();
            strike.typeface = typeface;
            strike.pixelWidth = pixelWidth;
            strike.pixelHeight = pixelHeight;
            strike.skewX = skewX;
            strike.lineRadius = lineRadius;
            strike.lineCap = lineCap;
            strike.lineJoin = lineJoin;
            strike.miterLimit = miterLimit;

            return strike;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Data other = (Data) obj;

            return super.equals(other);
        }
    }

    public static final class Color extends GlyphStrike {
        public @ColorInt int foregroundColor;

        public Color() { }

        public Color(@NonNull Color strike) {
            super(strike);
            this.foregroundColor = strike.foregroundColor;
        }

        @Override
        public @NonNull Color clone() {
            return new Color(this);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Color other = (Color) obj;

            return super.equals(other)
                && foregroundColor == other.foregroundColor;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + foregroundColor;

            return result;
        }
    }

    public static final class Stroke extends GlyphStrike {
        public int lineRadius;
        public @GlyphAttributes.LineCap int lineCap;
        public @GlyphAttributes.LineJoin int lineJoin;
        public int miterLimit;

        public Stroke() { }

        public Stroke(@NonNull Stroke strike) {
            super(strike);
            this.lineRadius = strike.lineRadius;
            this.lineCap = strike.lineCap;
            this.lineJoin = strike.lineJoin;
            this.miterLimit = strike.miterLimit;
        }

        @Override
        public @NonNull Stroke clone() {
            return new Stroke(this);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            Stroke other = (Stroke) obj;

            return super.equals(other)
                && lineRadius == other.lineRadius
                && lineCap == other.lineCap
                && lineJoin == other.lineJoin
                && miterLimit == other.miterLimit;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + lineRadius;
            result = 31 * result + lineCap;
            result = 31 * result + lineJoin;
            result = 31 * result + miterLimit;

            return result;
        }
    }
}
