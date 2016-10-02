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

package com.mta.tehreer.text.style;

/**
 * The <code>FontSizeSpan</code> class represents a span for specifying absolute font size.
 */
public class FontSizeSpan extends TehreerSpan {

    private final float size;

    /**
     * Constructs a font size span object.
     *
     * @param size The absolute font size in pixels.
     */
    public FontSizeSpan(float size) {
        this.size = size;
    }

    /**
     * Returns this span's font size.
     *
     * @return The font size of this span.
     */
    public float getSize() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (this != obj) {
            if (obj == null || !(obj instanceof FontSizeSpan)) {
                return false;
            }

            FontSizeSpan other = (FontSizeSpan) obj;
            if (size != other.size) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Float.valueOf(size).hashCode();
    }
}
