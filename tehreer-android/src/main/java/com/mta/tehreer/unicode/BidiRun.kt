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

package com.mta.tehreer.unicode

import androidx.annotation.Keep

/**
 * A `BidiRun` object represents a sequence of characters which have the same embedding level. The
 * direction of run is considered right-to-left, if its embedding level is odd.
 */
class BidiRun @Keep constructor(
    /**
     * The index to the first character of this run in source text.
     */
    @JvmField var charStart: Int,

    /**
     * The index after the last character of this run in source text.
     */
    @JvmField var charEnd: Int,

    /**
     * The embedding level of this run.
     */
    @JvmField var embeddingLevel: Byte
) {
    /**
     * Constructs a bidi run object.
     */
    constructor() : this(0, 0, 0)

    /**
     * Returns `true` if the embedding level of this run is odd.
     *
     * @return `true` if this run is right-to-left, `false` otherwise.
     */
    val isRightToLeft: Boolean
        get() = (embeddingLevel.toInt() and 1) == 1

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        other as BidiRun

        return charStart == other.charStart
            && charEnd == other.charEnd
            && embeddingLevel == other.embeddingLevel
    }

    override fun hashCode(): Int {
        var result = charStart
        result = 31 * result + charEnd
        result = 31 * result + embeddingLevel

        return result
    }

    override fun toString(): String {
        return "BidiRun{charStart=$charStart" +
            ", charEnd=$charEnd" +
            ", embeddingLevel=$embeddingLevel" +
            "}"
    }
}
