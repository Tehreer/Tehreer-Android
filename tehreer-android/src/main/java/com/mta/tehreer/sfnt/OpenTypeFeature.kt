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

package com.mta.tehreer.sfnt

/**
 * Represents an OpenType Layout feature.
 */
class OpenTypeFeature private constructor(
    private val tag: Int,
    private val value: Int
) {
    /**
     * Returns the tag of the feature that identifies its typographic function and effects.
     *
     * @return The tag of the feature that identifies its typographic function and effects.
     */
    fun tag(): Int {
        return tag
    }

    /**
     * Returns the value of the feature that modifies its behaviour.
     *
     * @return The value of the feature that modifies its behaviour.
     */
    fun value(): Int {
        return value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        other as OpenTypeFeature

        return tag == other.tag && value == other.value
    }

    override fun hashCode(): Int {
        return 31 * tag + value
    }

    override fun toString(): String {
        return "OpenTypeFeature{tag=${SfntTag.toString(tag)}, value=$value}"
    }

    companion object {
        /**
         * Returns an open type feature object with the specified tag and value.
         *
         * A tag can be created from string by using [SfntTag.make] method.
         *
         * @param tag The tag of the feature that identifies its typographic function and effects.
         * @param value The value of the feature that modifies its behaviour.
         * @return A new open type feature object.
         */
        @JvmStatic
        fun of(tag: Int, value: Int): OpenTypeFeature {
            return OpenTypeFeature(tag, value)
        }
    }
}
