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

/**
 * A `TypeFamily` object represents a collection of typefaces that relate to each other.
 *
 * @property familyName The name of this family.
 * @property typefaces The list of typefaces belonging to this family.
 */
class TypeFamily internal constructor(
    val familyName: String,
    internal val familyId: Int,
    @Size(min = 1) val typefaces: List<Typeface>
) {
    init {
        require(typefaces.isNotEmpty()) { "Typefaces list cannot be empty" }
    }

    /**
     * Returns a typeface best matching the specified style, as Tehreer Core picks it by the CSS
     * font matching algorithm.
     *
     * @param typeWidth The typographic width of desired typeface.
     * @param typeWeight The typographic weight of desired typeface.
     * @param typeSlope The typographic slope of desired typeface.
     * @return A typeface best matching the specified style.
     */
    fun getTypefaceByStyle(typeWidth: TypeWidth, typeWeight: TypeWeight, typeSlope: TypeSlope): Typeface {
        val matching = if (familyId != 0) {
            TypefaceManager.getTypefaceByFamilyId(familyId, typeWidth, typeWeight, typeSlope)
        } else {
            TypefaceManager.getTypefaceByStyle(familyName, typeWidth, typeWeight, typeSlope)
        }

        return matching ?: typefaces.first()
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
}
