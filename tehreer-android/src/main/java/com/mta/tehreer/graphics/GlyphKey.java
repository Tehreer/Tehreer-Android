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

abstract class GlyphKey {
    public Typeface typeface;
    public int pixelWidth;      // 26.6 fixed-point value.
    public int pixelHeight;     // 26.6 fixed-point value.
    public int skewX;           // 16.16 fixed-point value.

    public abstract GlyphKey copy();

    protected void set(@NonNull GlyphKey key) {
        this.typeface = key.typeface;
        this.pixelWidth = key.pixelWidth;
        this.pixelHeight = key.pixelHeight;
        this.skewX = key.skewX;
    }

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
        @Override
        public @NonNull Data copy() {
            Data key = new Data();
            key.set(this);

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

        public void set(GlyphKey.Data key) {
            super.set(key);
        }

        @Override
        public @NonNull Color copy() {
            Color key = new Color();
            key.set(this);
            key.foregroundColor = this.foregroundColor;

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

        public void set(GlyphKey.Data key) {
            super.set(key);
        }

        @Override
        public @NonNull Stroke copy() {
            Stroke key = new Stroke();
            key.set(this);
            key.lineRadius = this.lineRadius;
            key.lineCap = this.lineCap;
            key.lineJoin = this.lineJoin;
            key.miterLimit = this.miterLimit;

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
