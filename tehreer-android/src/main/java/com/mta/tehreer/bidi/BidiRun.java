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

import com.mta.tehreer.internal.Sustain;

/**
 * A <code>BidiRun</code> object represents a sequence of characters which have the same embedding
 * level. The direction of run is considered right-to-left, if its embedding level is odd.
 */
public class BidiRun {

    /**
     * The index to the first character of this run in source text.
     */
    public int charStart;
    /**
     * The index after the last character of this run in source text.
     */
    public int charEnd;
    /**
     * The embedding level of this run.
     */
    public byte embeddingLevel;

    @Sustain
    public BidiRun(int charStart, int charEnd, byte embeddingLevel) {
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.embeddingLevel = embeddingLevel;
    }

    public BidiRun() {
    }

    /**
     * Returns <code>true</code> if the embedding level of this run is odd.
     *
     * @return <code>true</code> if this run is right-to-left, <code>false</code> otherwise.
     */
    public boolean isRightToLeft() {
        return (embeddingLevel & 1) == 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this != obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            BidiRun other = (BidiRun) obj;
            if (charStart != other.charStart
                    || charEnd != other.charEnd
                    || embeddingLevel != other.embeddingLevel) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = charStart;
        result = 31 * result + charEnd;
        result = 31 * result + embeddingLevel;

        return result;
    }

    @Override
    public String toString() {
        return "BidiRun{charStart=" + charStart
                + ", charEnd=" + charEnd
                + ", embeddingLevel=" + embeddingLevel
                + "}";
    }
}
