/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

import com.mta.tehreer.internal.util.Sustain;

/**
 * A <code>BidiPair</code> object provides the pair of a unicode code point at a specific index in
 * source text.
 */
public class BidiPair {

    /**
     * The index of actual character in source text.
     */
    private int charIndex;
    /**
     * The code point of actual character in source text.
     */
    private int actualCodePoint;
    /**
     * The code point of character forming a pair with actual character.
     */
    private int pairingCodePoint;

    @Sustain
    public BidiPair(int charIndex, int actualCodePoint, int pairingCodePoint) {
        this.charIndex = charIndex;
        this.actualCodePoint = actualCodePoint;
        this.pairingCodePoint = pairingCodePoint;
    }

    public BidiPair() {
    }
}
