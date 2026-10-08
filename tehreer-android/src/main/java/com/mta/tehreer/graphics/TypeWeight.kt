/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.graphics

/**
 * Specifies the thickness of a typeface, in terms of lightness or heaviness of the strokes.
 */
enum class TypeWeight(
    /**
     * The integer value of the `TypeWeight`. Lower value indicates lighter weight; higher value
     * indicates heavier weight.
     */
    internal val value: Int
) {
    THIN(100),
    EXTRA_LIGHT(200),
    LIGHT(300),
    REGULAR(400),
    MEDIUM(500),
    SEMI_BOLD(600),
    BOLD(700),
    EXTRA_BOLD(800),
    HEAVY(900);

    internal companion object {
        /**
         * Returns the enum constant of `TypeWeight` with the specified value.
         *
         * @param value The integer value of the weight.
         * @return The enum constant that is closest to the specified value.
         */
        fun valueOf(value: Int): TypeWeight {
            val index = ((value / 100.0f) - 0.5f).toInt()

            return entries[index.coerceIn(0, 8)]
        }
    }
}
