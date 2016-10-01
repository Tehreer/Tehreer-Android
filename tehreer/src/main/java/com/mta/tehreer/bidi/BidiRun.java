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
 * A <code>BidiRun</code> object represents a sequence of characters which have the same embedding
 * level. The direction of run is considered right-to-left, if its embedding level is odd.
 */
public class BidiRun {

    private int mCharStart;
    private int mCharEnd;
    private byte mEmbeddingLevel;

    BidiRun(int charStart, int charEnd, byte embeddingLevel) {
        mCharStart = charStart;
        mCharEnd = charEnd;
        mEmbeddingLevel = embeddingLevel;
    }

    /**
     * Returns the index to the first character of this run in source text.
     *
     * @return The index to the first character of this run in source text.
     */
    public int getCharStart() {
        return mCharStart;
    }

    /**
     * Returns the index after the last character of this run in source text.
     *
     * @return The index after the last character of this run in source text.
     */
    public int getCharEnd() {
        return mCharEnd;
    }

    /**
     * Returns the embedding level of this run.
     *
     * @return The embedding level of this run.
     */
    public byte getEmbeddingLevel() {
        return mEmbeddingLevel;
    }

    /**
     * Returns <code>true</code> if this run is right-to-left.
     *
     * @return <code>true</code> if this run is right-to-left, <code>false</code> otherwise.
     */
    public boolean isRightToLeft() {
        return (mEmbeddingLevel & 1) == 1;
    }

    @Override
    public String toString() {
        return "BidiRun{charStart=" + mCharStart
                + ", charEnd=" + mCharEnd
                + ", embeddingLevel=" + mEmbeddingLevel
                + "}";
    }
}
