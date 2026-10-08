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

import androidx.annotation.IntDef
import androidx.annotation.Size

/**
 * Represents font color palette.
 */
class ColorPalette private constructor(
    private val name: String,
    @Flags private val flags: Int,
    private val colors: IntArray
) {
    @IntDef(flag = true, value = [USABLE_WITH_LIGHT_BACKGROUND, USABLE_WITH_DARK_BACKGROUND])
    @Retention(AnnotationRetention.SOURCE)
    annotation class Flags

    /**
     * Returns the display name.
     *
     * @return The display name.
     */
    fun name(): String {
        return name
    }

    /**
     * Returns the property flags.
     *
     * @return The property flags.
     */
    @Flags
    fun flags(): Int {
        return flags
    }

    /**
     * Returns the colors array.
     *
     * @return The colors array.
     */
    fun colors(): IntArray {
        return colors.copyOf()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        other as ColorPalette

        return name == other.name && flags == other.flags && colors.contentEquals(other.colors)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + flags
        result = 31 * result + colors.contentHashCode()

        return result
    }

    companion object {
        const val USABLE_WITH_LIGHT_BACKGROUND = 0x0001
        const val USABLE_WITH_DARK_BACKGROUND = 0x0002

        /**
         * Returns a palette object with the specified values.
         *
         * @param name The display name.
         * @param flags The property flags.
         * @param colors The colors array.
         * @return A new palette object.
         */
        @JvmStatic
        fun of(name: String, @Flags flags: Int, @Size(min = 1) colors: IntArray): ColorPalette {
            require(colors.isNotEmpty()) { "The colors array is empty" }

            return ColorPalette(name, flags, colors.copyOf())
        }
    }
}
