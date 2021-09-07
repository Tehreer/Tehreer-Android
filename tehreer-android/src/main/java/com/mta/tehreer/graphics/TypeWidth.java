/*
 * Copyright (C) 2017-2021 Muhammad Tayyab Akram
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

import androidx.annotation.NonNull;

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

    private static final TypeWidth[] ALL_VALUES = TypeWidth.values();

    /**
     * The integer value of the <code>TypeWidth</code>. Lower value indicates narrower width; higher
     * value indicates wider width.
     */
    final int value;

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
    static @NonNull TypeWidth valueOf(int value) {
        final int index = value - 1;

        return ALL_VALUES[Math.max(0, Math.min(8, index))];
    }

    static @NonNull TypeWidth fromWdth(float wdth) {
        int value;

        if (wdth < 50) {
            value = 1;
        } else if (wdth < 125) {
            value = (int) (((wdth - 50) / 12.5) + 1);
        } else if (wdth < 200) {
            value = (int) (((wdth - 125) / 25) + 7);
        } else {
            value = 9;
        }

        return valueOf(value);
    }
}
