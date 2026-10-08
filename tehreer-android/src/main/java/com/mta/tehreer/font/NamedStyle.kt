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

package com.mta.tehreer.font

import androidx.annotation.Size

/**
 * Represents font named style.
 */
class NamedStyle private constructor(
    private val styleName: String,
    private val coordinates: FloatArray,
    private val postScriptName: String?
) {
    /**
     * Returns the style name.
     *
     * @return The style name.
     */
    fun styleName(): String {
        return styleName
    }

    /**
     * Returns the coordinates.
     *
     * @return The coordinates.
     */
    fun coordinates(): FloatArray {
        return coordinates.copyOf()
    }

    /**
     * Returns the post script name.
     *
     * @return The post script name.
     */
    fun postScriptName(): String? {
        return postScriptName
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        other as NamedStyle

        return styleName == other.styleName
            && coordinates.contentEquals(other.coordinates)
            && postScriptName == other.postScriptName
    }

    override fun hashCode(): Int {
        var result = styleName.hashCode()
        result = 31 * result + coordinates.contentHashCode()
        result = 31 * result + (postScriptName?.hashCode() ?: 0)

        return result
    }

    companion object {
        /**
         * Returns a named style object with the specified values.
         *
         * @param styleName The style name.
         * @param coordinates The variation coordinates.
         * @param postScriptName The post script name.
         * @return A new named style object.
         */
        @JvmStatic
        fun of(
            styleName: String,
            @Size(min = 1) coordinates: FloatArray,
            postScriptName: String?
        ): NamedStyle {
            require(coordinates.isNotEmpty()) { "The coordinates array is empty" }

            return NamedStyle(styleName, coordinates, postScriptName)
        }
    }
}
