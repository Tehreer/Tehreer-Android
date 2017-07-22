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
 * Specifies the thickness of a typeface, in terms of lightness or heaviness of the strokes.
 */
public enum TypeWeight {
    THIN(100),
    EXTRA_LIGHT(200),
    LIGHT(300),
    REGULAR(400),
    MEDIUM(500),
    SEMI_BOLD(600),
    BOLD(700),
    EXTRA_BOLD(800),
    HEAVY(900);

    /**
     * The integer value of the <code>TypeWeight</code>. Lower value indicates lighter weight;
     * higher value indicates heavier weight.
     */
    final int value;

    TypeWeight(int value) {
        this.value = value;
    }

    /**
     * Returns the enum constant of <code>TypeWeight</code> with the specified value.
     *
     * @param value The integer value of the weight.
     * @return The enum constant with the specified value, or <code>null</code> if
     *         <code>TypeWeight</code> has no constant with the specified value.
     */
    static TypeWeight valueOf(int value) {
        for (TypeWeight weight : TypeWeight.values()) {
            if (weight.value == value) {
                return weight;
            }
        }

        return null;
    }
}
