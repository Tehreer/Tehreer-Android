/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.unicode;

import com.mta.tehreer.internal.Sustain;

/**
 * A <code>BidiPair</code> object represents a pair of a unicode code point at a specific index in
 * source text.
 */
public class BidiPair {
    /**
     * The index of actual character in source text.
     */
    public int charIndex;
    /**
     * The code point of actual character in source text.
     */
    public int actualCodePoint;
    /**
     * The code point of character forming a pair with actual character.
     */
    public int pairingCodePoint;

    /**
     * Constructs a bidi pair object.
     *
     * @param charIndex The index of actual character.
     * @param actualCodePoint The code point of actual character.
     * @param pairingCodePoint The code point of pairing character.
     */
    @Sustain
    public BidiPair(int charIndex, int actualCodePoint, int pairingCodePoint) {
        this.charIndex = charIndex;
        this.actualCodePoint = actualCodePoint;
        this.pairingCodePoint = pairingCodePoint;
    }

    /**
     * Constructs a bidi pair object.
     */
    public BidiPair() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        BidiPair other = (BidiPair) obj;
        return charIndex == other.charIndex
                && actualCodePoint == other.actualCodePoint
                && pairingCodePoint == other.pairingCodePoint;
    }

    @Override
    public int hashCode() {
        int result = charIndex;
        result = 31 * result + actualCodePoint;
        result = 31 * result + pairingCodePoint;

        return result;
    }

    @Override
    public String toString() {
        return "BidiPair{charIndex=" + charIndex
                + ", actualCodePoint=" + actualCodePoint
                + ", pairingCodePoint=" + pairingCodePoint
                + '}';
    }
}
