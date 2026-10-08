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

import androidx.annotation.Size
import kotlin.math.abs

/**
 * A `TypeFamily` object represents a collection of typefaces that relate to each other.
 *
 * @property familyName The name of this family.
 * @property typefaces The list of typefaces belonging to this family.
 */
class TypeFamily(
    val familyName: String,
    @Size(min = 1) val typefaces: List<Typeface>
) {
    init {
        require(typefaces.isNotEmpty()) { "Typefaces list cannot be empty" }
    }

    /**
     * Returns a typeface best matching the specified style.
     *
     * @param typeWidth The typographic width of desired typeface.
     * @param typeWeight The typographic weight of desired typeface.
     * @param typeSlope The typographic slope of desired typeface.
     * @return A typeface best matching the specified style.
     */
    fun getTypefaceByStyle(typeWidth: TypeWidth, typeWeight: TypeWeight, typeSlope: TypeSlope): Typeface {
        // BASED ON CSS FONT MATCHING ALGORITHM.
        val iterator = typefaces.iterator()
        var candidate = iterator.next()

        while (iterator.hasNext()) {
            val current = iterator.next()

            val widthGap = widthGap(typeWidth, current.width) - widthGap(typeWidth, candidate.width)
            if (widthGap > 0) {
                continue
            }

            val slopeGap = slopeGap(typeSlope, current.slope) - slopeGap(typeSlope, candidate.slope)
            if (slopeGap > 0) {
                continue
            }

            val weightGap = weightGap(typeWeight, current.weight) - weightGap(typeWeight, candidate.weight)
            if (weightGap > 0) {
                continue
            }

            candidate = current
        }

        return candidate
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        other as TypeFamily

        return familyName == other.familyName && typefaces == other.typefaces
    }

    override fun hashCode(): Int {
        return 31 * familyName.hashCode() + typefaces.hashCode()
    }

    override fun toString(): String {
        return "TypeFamily{familyName=$familyName, typefaces=$typefaces}"
    }

    private companion object {
        fun widthGap(desired: TypeWidth, candidate: TypeWidth): Int {
            return abs(desired.ordinal - candidate.ordinal)
        }

        private val SLOPE_GAPS = intArrayOf(
            // "If the value is `normal`, normal faces are checked first, then oblique faces, then
            // italic faces."
            /*   PLAIN: */ 0, 2, 1,

            // "If the value is `italic`, italic faces are checked first, then oblique, then normal
            // faces."
            /*  ITALIC: */ 2, 0, 1,

            // "If the value is `oblique`, oblique faces are checked first, then italic faces and
            // then normal faces."
            /* OBLIQUE: */ 2, 1, 0,
        )

        fun slopeGap(desired: TypeSlope, candidate: TypeSlope): Int {
            return SLOPE_GAPS[desired.ordinal * 3 + candidate.ordinal]
        }

        private val WEIGHT_GAPS = intArrayOf(
            // "If the desired weight is less than 400, weights below the desired weight are checked
            // in descending order followed by weights above the desired weight in ascending order
            // until a match is found."
            /* 100: */ 0, 1, 2, 3, 4, 5, 6, 7, 8,
            /* 200: */ 1, 0, 2, 3, 4, 5, 6, 7, 8,
            /* 300: */ 2, 1, 0, 3, 4, 5, 6, 7, 8,

            // "If the desired weight is 400, 500 is checked first and then the rule for desired
            // weights less than 400 is used."
            /* 400: */ 4, 3, 2, 0, 1, 5, 6, 7, 8,

            // "If the desired weight is 500, 400 is checked first and then the rule for desired
            // weights less than 400 is used."
            /* 500: */ 4, 3, 2, 1, 0, 5, 6, 7, 8,

            // "If the desired weight is greater than 500, weights above the desired weight are
            // checked in ascending order followed by weights below the desired weight in descending
            // order until a match is found."
            /* 600: */ 8, 7, 6, 5, 4, 0, 1, 2, 3,
            /* 700: */ 8, 7, 6, 5, 4, 3, 0, 1, 2,
            /* 800: */ 8, 7, 6, 5, 4, 3, 2, 0, 1,
            /* 900: */ 8, 7, 6, 5, 4, 3, 2, 1, 0,
        )

        fun weightGap(desired: TypeWeight, candidate: TypeWeight): Int {
            return WEIGHT_GAPS[desired.ordinal * 9 + candidate.ordinal]
        }
    }
}
