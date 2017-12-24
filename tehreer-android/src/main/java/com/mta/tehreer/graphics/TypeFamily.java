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
            available[typeface.getWidth().ordinal()] = true;
        }

        int lastIndex = allWidths.length - 1;
        int matchIndex = typeWidth.ordinal();
        int normalIndex = TypeWidth.NORMAL.ordinal();

        for (int i = 0; i <= lastIndex; i++) {
            int position;

            // - If the value is ‘normal’ or lower, check narrower widths first, then wider ones.
            // - Otherwise, check wider widths first, then narrower ones.
            if (matchIndex <= normalIndex) {
                position = (i <= matchIndex ? matchIndex - i : i);
            } else {
                position = matchIndex + i;
                if (position > lastIndex) {
                    position = lastIndex - i;
                }
            }

            TypeWidth currentWidth = allWidths[position];
            if (available[currentWidth.ordinal()]) {
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

    private static void filterTypeSlope(List<Typeface> typefaces, TypeSlope typeSlope) {
        TypeSlope[] allSlopes = TypeSlope.values();
        boolean[] available = new boolean[allSlopes.length];

        // Check the availability of each slope.
        for (Typeface typeface : typefaces) {
            available[typeface.getSlope().ordinal()] = true;
        }

        int slopeCount = allSlopes.length;
        int matchIndex = typeSlope.ordinal();
        boolean forwardLoop = (typeSlope == TypeSlope.ITALIC);

        for (int i = 0; i < slopeCount; i++) {
            // - If the value is ‘italic’, check italic faces first, then oblique, then normal.
            // - If the value is ‘oblique’, check oblique faces first, then italic, then normal.
            // - If the value is ‘normal’, check normal faces first, then oblique, then italic.
            int position = (matchIndex + (forwardLoop ? i : slopeCount - i)) % slopeCount;
            TypeSlope currentSlope = allSlopes[position];
            if (available[currentSlope.ordinal()]) {
                // The closest matching slope has been determined.
                // Remove typefaces with other slopes.
                Iterator<Typeface> iterator = typefaces.iterator();
                while (iterator.hasNext()) {
                    Typeface typeface = iterator.next();
                    if (typeface.getSlope() != currentSlope) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    private static void filterTypeWeight(List<Typeface> typefaces, TypeWeight typeWeight) {
        TypeWeight[] allWeights = TypeWeight.values();
        boolean[] available = new boolean[allWeights.length];

        // Check the availability of each weight.
        for (Typeface typeface : typefaces) {
            available[typeface.getWeight().ordinal()] = true;
        }

        int lastIndex = allWeights.length - 1;
        int matchIndex = typeWeight.ordinal();
        int regularIndex = TypeWeight.REGULAR.ordinal();
        int mediumIndex = TypeWeight.MEDIUM.ordinal();

        for (int i = 0; i <= lastIndex; i++) {
            int position;

            if (matchIndex == regularIndex) {
                position = regularIndex + i;
                if (position == mediumIndex) {
                    matchIndex = mediumIndex;
                }
            } else if (matchIndex <= mediumIndex) {
                position = (i <= matchIndex ? matchIndex - i : i);
            } else {
                position = matchIndex + i;
                if (position > lastIndex) {
                    position = lastIndex - i;
                }
            }

            TypeWeight currentWeight = allWeights[position];
            if (available[currentWeight.ordinal()]) {
                // The closest matching width has been determined.
                // Remove typefaces with other weights.
                Iterator<Typeface> iterator = typefaces.iterator();
                while (iterator.hasNext()) {
                    Typeface typeface = iterator.next();
                    if (typeface.getWeight() != currentWeight) {
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
