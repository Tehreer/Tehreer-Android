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

import java.util.List;

public class TypeFamily {

    private final String familyName;
    private final List<Typeface> typefaces;

    public TypeFamily(String familyName, List<Typeface> typefaces) {
        this.familyName = familyName;
        this.typefaces = typefaces;
    }

    public String familyName() {
        return familyName;
    }

    public List<Typeface> typefaces() {
        return typefaces;
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
