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

/**
 * Represents font variation axis.
 */
class VariationAxis private constructor(
    private val tag: Int,
    private val name: String,
    @Flags private val flags: Int,
    private val defaultValue: Float,
    private val minValue: Float,
    private val maxValue: Float
) {
    @IntDef(flag = true, value = [FLAG_HIDDEN_AXIS])
    @Retention(AnnotationRetention.SOURCE)
    annotation class Flags

    /**
     * Returns the tag identifying the design variation.
     *
     * @return The tag identifying the design variation.
     */
    fun tag(): Int {
        return tag
    }

    /**
     * Returns the display name.
     *
     * @return The display name.
     */
    fun name(): String {
        return name
    }

    /**
     * Returns the axis qualifiers.
     *
     * @return The axis qualifiers.
     */
    @Flags
    fun flags(): Int {
        return flags
    }

    /**
     * Returns the default coordinate value.
     *
     * @return The default coordinate value.
     */
    fun defaultValue(): Float {
        return defaultValue
    }

    /**
     * Returns the minimum coordinate value.
     *
     * @return The minimum coordinate value.
     */
    fun minValue(): Float {
        return minValue
    }

    /**
     * Returns the maximum coordinate value.
     *
     * @return The maximum coordinate value.
     */
    fun maxValue(): Float {
        return maxValue
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        other as VariationAxis

        return tag == other.tag
            && name == other.name
            && flags == other.flags
            && defaultValue.compareTo(other.defaultValue) == 0
            && minValue.compareTo(other.minValue) == 0
            && maxValue.compareTo(other.maxValue) == 0
    }

    override fun hashCode(): Int {
        var result = tag
        result = 31 * result + name.hashCode()
        result = 31 * result + flags
        result = 31 * result + defaultValue.toBits()
        result = 31 * result + minValue.toBits()
        result = 31 * result + maxValue.toBits()

        return result
    }

    companion object {
        /**
         * The axis should not be exposed directly in user interfaces.
         */
        const val FLAG_HIDDEN_AXIS = 0x0001

        /**
         * Returns a variation axis object with the specified values.
         *
         * @param tag Tag identifying the design variation.
         * @param name The display name.
         * @param flags Axis qualifiers.
         * @param defaultValue The default coordinate value.
         * @param minValue The minimum coordinate value.
         * @param maxValue The maximum coordinate value.
         * @return A new variation axis object.
         */
        @JvmStatic
        fun of(
            tag: Int, name: String, @Flags flags: Int,
            defaultValue: Float, minValue: Float, maxValue: Float
        ): VariationAxis {
            return VariationAxis(tag, name, flags, defaultValue, minValue, maxValue)
        }
    }
}
