/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

package com.mta.tehreer.bidi;

/**
 * A <code>BidiPair</code> object provides the pair of a unicode code point at a specific index in
 * source text.
 */
public class BidiPair {

    private int mCharIndex;
    private int mPairingCodePoint;

    BidiPair(int charIndex, int pairingCodePoint) {
        mCharIndex = charIndex;
        mPairingCodePoint = pairingCodePoint;
    }

    /**
     * Returns the index of pairing character in source text.
     *
     * @return The index of pairing character in source text.
     */
    public int getCharIndex() {
        return mCharIndex;
    }

    /**
     * Returns the pairing code point.
     *
     * @return The pairing code point.
     */
    public int getPairingCodePoint() {
        return mPairingCodePoint;
    }
}
