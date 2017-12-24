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

package com.mta.tehreer.graphics;

/**
 * Specifies the wideness of a typeface, in terms of the width of characters in relation to their
 * heights.
 */
public enum TypeWidth {
    ULTRA_CONDENSED(1),
    EXTRA_CONDENSED(2),
    CONDENSED(3),
    SEMI_CONDENSED(4),
    NORMAL(5),
    SEMI_EXPANDED(6),
    EXPANDED(7),
    EXTRA_EXPANDED(8),
    ULTRA_EXPANDED(9);

    /**
     * The integer value of the <code>TypeWidth</code>. Lower value indicates narrower width; higher
     * value indicates wider width.
     */
    final int value;

    TypeWidth(int value) {
        this.value = value;
    }

    int index() {
        return value - 1;
    }

    /**
     * Returns the enum constant of <code>TypeWidth</code> with the specified value.
     *
     * @param value The integer value of the proportion.
     * @return The enum constant with the specified value, or <code>null</code> if
     *         <code>TypeWidth</code> has no constant with the specified value.
     */
    static TypeWidth valueOf(int value) {
        for (TypeWidth width : TypeWidth.values()) {
            if (width.value == value) {
                return width;
            }
        }

        return null;
    }
}
