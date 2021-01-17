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

abstract class GlyphKey implements Cloneable {
    public Typeface typeface;
    public int pixelWidth;      // 26.6 fixed-point value.
    public int pixelHeight;     // 26.6 fixed-point value.
    public int skewX;           // 16.16 fixed-point value.

    protected GlyphKey() { }

    protected GlyphKey(@NonNull GlyphKey key) {
        this.typeface = key.typeface;
        this.pixelWidth = key.pixelWidth;
        this.pixelHeight = key.pixelHeight;
        this.skewX = key.skewX;
    }

    public abstract @NonNull GlyphKey clone();

    protected boolean equals(@NonNull GlyphKey other) {
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

    public static final class Data extends GlyphKey {
        public Data() { }

        public Data(Data key) {
            super(key);
        }

        @Override
        public @NonNull Data clone() {
            return new Data(this);
        }

        public @NonNull Color color(@ColorInt int foregroundColor) {
            Color key = new Color();
            key.typeface = typeface;
            key.pixelWidth = pixelWidth;
            key.pixelHeight = pixelHeight;
            key.skewX = skewX;
            key.foregroundColor = foregroundColor;

            return key;
        }

        public @NonNull Stroke stroke(int lineRadius, @GlyphAttributes.LineCap int lineCap,
                                      @GlyphAttributes.LineJoin int lineJoin, int miterLimit) {
            Stroke key = new Stroke();
            key.typeface = typeface;
            key.pixelWidth = pixelWidth;
            key.pixelHeight = pixelHeight;
            key.skewX = skewX;
            key.lineRadius = lineRadius;
            key.lineCap = lineCap;
            key.lineJoin = lineJoin;
            key.miterLimit = miterLimit;

            return key;
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

    public static final class Color extends GlyphKey {
        public @ColorInt int foregroundColor;

        public Color() { }

        public Color(@NonNull Color key) {
            super(key);
            this.foregroundColor = key.foregroundColor;
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

    public static final class Stroke extends GlyphKey {
        public int lineRadius;
        public @GlyphAttributes.LineCap int lineCap;
        public @GlyphAttributes.LineJoin int lineJoin;
        public int miterLimit;

        public Stroke() { }

        public Stroke(@NonNull Stroke key) {
            super(key);
            this.lineRadius = key.lineRadius;
            this.lineCap = key.lineCap;
            this.lineJoin = key.lineJoin;
            this.miterLimit = key.miterLimit;
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
