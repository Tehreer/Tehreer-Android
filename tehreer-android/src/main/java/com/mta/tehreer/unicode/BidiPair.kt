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
 * A `BidiPair` object represents a pair of a unicode code point at a specific index in source text.
 */
class BidiPair @Keep constructor(
    /**
     * The index of actual character in source text.
     */
    @JvmField var charIndex: Int,

    /**
     * The code point of actual character in source text.
     */
    @JvmField var actualCodePoint: Int,

    /**
     * The code point of character forming a pair with actual character.
     */
    @JvmField var pairingCodePoint: Int
) {
    /**
     * Constructs a bidi pair object.
     */
    constructor() : this(0, 0, 0)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        other as BidiPair

        return charIndex == other.charIndex
            && actualCodePoint == other.actualCodePoint
            && pairingCodePoint == other.pairingCodePoint
    }

    override fun hashCode(): Int {
        var result = charIndex
        result = 31 * result + actualCodePoint
        result = 31 * result + pairingCodePoint

        return result
    }

    override fun toString(): String {
        return "BidiPair{charIndex=$charIndex" +
            ", actualCodePoint=$actualCodePoint" +
            ", pairingCodePoint=$pairingCodePoint" +
            "}"
    }
}
