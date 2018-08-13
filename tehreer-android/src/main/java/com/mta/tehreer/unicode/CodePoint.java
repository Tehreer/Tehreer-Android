/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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

import com.mta.tehreer.internal.JniBridge;

public class CodePoint {
    static {
        JniBridge.loadLibrary();
    }

    private CodePoint() {
    }

    /**
     * Returns the bidirectional class of the specified code point.
     *
     * @param codePoint The code point whose bidirectional class is returned.
     * @return The bidirectional class of the specified code point.
     */
    public static int getBidiClass(int codePoint) {
        return Unicode.getCodePointBidiClass(codePoint);
    }

    /**
     * Returns the general category of the specified code point.
     *
     * @param codePoint The code point whose general category is returned.
     * @return The general category of the specified code point.
     */
    public static int getGeneralCategory(int codePoint) {
        return Unicode.getCodePointGeneralCategory(codePoint);
    }

    /**
     * Returns the script of the specified code point.
     *
     * @param codePoint The code point whose script is returned.
     * @return The script of the specified code point.
     */
    public static int getScript(int codePoint) {
        return Unicode.getCodePointScript(codePoint);
    }

    /**
     * Returns the mirror of the specified code point. If the specified code point does not have a
     * mirror, then zero is returned.
     *
     * @param codePoint The code point whose mirror is returned.
     * @return The mirror of the specified code point if available, zero otherwise.
     */
    public static int getMirror(int codePoint) {
        return Unicode.getCodePointMirror(codePoint);
    }
}
