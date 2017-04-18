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
    /**
     * The "Ultra-Condensed" width having a value equal to 1.
     */
    ULTRA_CONDENSED(1),
    /**
     * The "Extra-Condensed" width having a value equal to 2.
     */
    EXTRA_CONDENSED(2),
    /**
     * The "Condensed" width having a value equal to 3.
     */
    CONDENSED(3),
    /**
     * The "Semi-Condensed" width having a value equal to 4.
     */
    SEMI_CONDENSED(4),
    /**
     * The "Normal" width having a value equal to 5.
     */
    NORMAL(5),
    /**
     * The "Semi-Expanded" width having a value equal to 6.
     */
    SEMI_EXPANDED(6),
    /**
     * The "Expanded" width having a value equal to 7.
     */
    EXPANDED(7),
    /**
     * The "Extra-Expanded" width having a value equal to 8.
     */
    EXTRA_EXPANDED(8),
    /**
     * The "Ultra-Expanded" width having a value equal to 9.
     */
    ULTRA_EXPANDED(9);

    /**
     * The integer value of the <code>TypeWidth</code>. Lower value indicates narrower width; higher
     * value indicates wider width.
     */
    public final int value;

    TypeWidth(int value) {
        this.value = value;
    }

    /**
     * Returns the enum constant of <code>TypeWidth</code> with the specified value.
     *
     * @param value The integer value of the proportion.
     * @return The enum constant with the specified value, or <code>null</code> if
     *         <code>TypeWidth</code> has no constant with the specified value.
     */
    public static TypeWidth valueOf(int value) {
        for (TypeWidth width : TypeWidth.values()) {
            if (width.value == value) {
                return width;
            }
        }

        return null;
    }
}
