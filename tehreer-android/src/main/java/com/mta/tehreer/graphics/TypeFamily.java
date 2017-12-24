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

package com.mta.tehreer.graphics;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A <code>TypeFamily</code> object represents a collection of typefaces that relate to each other.
 */
public class TypeFamily {

    private final String familyName;
    private final List<Typeface> typefaces;
    private List<Typeface> mSortedTypefaces;

    /**
     * Constructs a type family object.
     *
     * @param familyName The name of family.
     * @param typefaces The list of typefaces belonging to family.
     */
    public TypeFamily(String familyName, List<Typeface> typefaces) {
        this.familyName = familyName;
        this.typefaces = (typefaces != null ? typefaces : Collections.<Typeface>emptyList());
    }

    /**
     * Returns the name of this family.
     *
     * @return The name of this family.
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Returns the list of typefaces belonging to this family.
     *
     * @return The list of typefaces belonging to this family.
     */
    public List<Typeface> getTypefaces() {
        return typefaces;
    }

    private static void filterTypeWidth(List<Typeface> typefaces, TypeWidth typeWidth) {
        TypeWidth[] allWidths = TypeWidth.values();
        boolean[] available = new boolean[allWidths.length];

        // Check the availability of each width.
        for (Typeface typeface : typefaces) {
            available[typeface.getWidth().index()] = true;
        }

        int lastIndex = allWidths.length - 1;
        int matchIndex = typeWidth.index();
        int normalIndex = TypeWidth.NORMAL.index();

        for (int i = 0; i <= lastIndex; i++) {
            int position;

            // - If the value is ‘normal’ or lower, check narrower widths first, then wider ones.
            // - Otherwise, check wider widths first, then narrower ones.
            if (matchIndex <= normalIndex) {
                position = matchIndex - i;
                if (position < 0) {
                    position = i;
                }
            } else {
                position = matchIndex + i;
                if (position > lastIndex) {
                    position = lastIndex - i;
                }
            }

            TypeWidth currentWidth = allWidths[position];
            if (available[currentWidth.index()]) {
                // The closest matching width has been determined.
                // Remove typefaces with other widths.
                Iterator<Typeface> iterator = typefaces.iterator();
                while (iterator.hasNext()) {
                    Typeface typeface = iterator.next();
                    if (typeface.getWidth() != currentWidth) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this != obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            TypeFamily other = (TypeFamily) obj;
            if (!familyName.equals(other.familyName)
                    || !typefaces.equals(other.typefaces)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = familyName.hashCode();
        result = 31 * result + typefaces.hashCode();

        return result;
    }

    @Override
    public String toString() {
        return "TypeFamily{familyName=" + familyName
                + ", typefaces=" + typefaces
                + "}";
    }
}
