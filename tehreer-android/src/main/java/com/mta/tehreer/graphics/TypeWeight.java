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
    /**
     * The "Thin" weight having a value equal to 100.
     */
    THIN(100),
    /**
     * The "Extra-Light" weight having a value equal to 200.
     */
    EXTRA_LIGHT(200),
    /**
     * The "Light" weight having a value equal to 300.
     */
    LIGHT(300),
    /**
     * The "Regular" weight having a value equal to 400.
     */
    REGULAR(400),
    /**
     * The "Medium" weight having a value equal to 500.
     */
    MEDIUM(500),
    /**
     * The "Semi-Bold" weight having a value equal to 600.
     */
    SEMI_BOLD(600),
    /**
     * The "Bold" weight having a value equal to 700.
     */
    BOLD(700),
    /**
     * The "Extra-Bold" weight having a value equal to 800.
     */
    EXTRA_BOLD(800),
    /**
     * The "Heavy" weight having a value equal to 900.
     */
    HEAVY(900);

    /**
     * The integer value of the <code>TypeWeight</code>. Lower value indicates lighter weight;
     * higher value indicates heavier weight.
     */
    public final int value;

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
    public static TypeWeight valueOf(int value) {
        for (TypeWeight weight : TypeWeight.values()) {
            if (weight.value == value) {
                return weight;
            }
        }

        return null;
    }
}
