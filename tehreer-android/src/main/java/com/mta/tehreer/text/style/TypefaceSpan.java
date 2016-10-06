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

import com.mta.tehreer.graphics.Typeface;

/**
 * The <code>TypefaceSpan</code> class represents a span for specifying custom typeface.
 */
public class TypefaceSpan extends TehreerSpan {

    private final Typeface typeface;

    /**
     * Constructs a typeface span object.
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
    public boolean equals(Object obj) {
        if (this != obj) {
            if (obj == null || !(obj instanceof TypefaceSpan)) {
                return false;
            }

            TypefaceSpan other = (TypefaceSpan) obj;
            if ((typeface == null ? other.typeface != null : !typeface.equals(other.typeface))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return typeface.hashCode();
    }
}
