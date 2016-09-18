/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

class GlyphStrike implements Cloneable {

    public Typeface typeface;
    public int pixelWidth;      // 26.6 fixed-point value.
    public int pixelHeight;     // 26.6 fixed-point value.
    public int skewX;           // 16.16 fixed-point value.

    @Override
    public GlyphStrike clone() {
        try {
            return (GlyphStrike) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this != obj) {
            if (obj == null || !(obj instanceof GlyphStrike)) {
                return false;
            }

            GlyphStrike other = (GlyphStrike) obj;
            if ((typeface == null ? other.typeface != null : !typeface.equals(other.typeface))
                    || pixelWidth != other.pixelWidth || pixelHeight != other.pixelHeight
                    || skewX != other.skewX) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + typeface.hashCode();
        result = prime * result + pixelWidth;
        result = prime * result + pixelHeight;
        result = prime * result + skewX;

        return result;
    }
}
