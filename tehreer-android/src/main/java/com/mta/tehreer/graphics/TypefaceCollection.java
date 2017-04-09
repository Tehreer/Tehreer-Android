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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The <code>TypefaceCollection</code> class represents a collection of typefaces.
 */
public class TypefaceCollection {

    private static class TypefaceComparator implements Comparator<Typeface> {
        @Override
        public int compare(Typeface obj1, Typeface obj2) {
            int result = obj1.getFamilyName().compareToIgnoreCase(obj2.getFamilyName());
            if (result == 0) {
                return obj1.getFaceName().compareToIgnoreCase(obj2.getFaceName());
            }

            return result;
        }
    }

    private static TypefaceCollection sShared;
    private Map<Object, Typeface> mEntries;

    public static TypefaceCollection shared() {
        if (sShared == null) {
            synchronized (TypefaceCollection.class) {
                if (sShared == null) {
                    sShared = new TypefaceCollection();
                }
            }
        }

        return sShared;
    }

    public TypefaceCollection() {
        mEntries = new HashMap<>();
    }

    /**
     * Adds a typeface against a specified tag in this collection.
     *
     * @param typeface The typeface that will be added into this collection.
     * @param tag The tag to identify the typeface.
     *
     * @throws NullPointerException if either <code>typeface</code> or <code>tag</code> is null.
     * @throws IllegalArgumentException if <code>typeface</code> is already added, or
     *         <code>tag</code> is unavailable.
     */
    public void add(Typeface typeface, Object tag) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }
        if (tag == null) {
            throw new NullPointerException("Tag is null");
        }

        synchronized (this) {
            if (mEntries.containsValue(typeface)) {
                throw new IllegalArgumentException("The specified typeface has already been added");
            }
            if (mEntries.containsKey(tag)) {
                throw new IllegalArgumentException("The specified tag has already been taken");
            }

            mEntries.put(tag, typeface);
        }
    }

    /**
     * Removes a typeface from this collection.
     *
     * @param typeface The typeface to be removed from this collection.
     *
     * @throws NullPointerException if <code>typeface</code> is null.
     * @throws IllegalArgumentException if <code>typeface</code> is not available in this
     *         collection.
     */
    public void remove(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        synchronized (this) {
            Object tag = tagOf(typeface);
            if (tag == null) {
                throw new IllegalArgumentException("The specified typeface is not available in the collection");
            }

            mEntries.remove(tag);
        }
    }

    /**
     * Returns the typeface that is identified by the specified tag object in this collection.
     *
     * @param tag The tag object that identifies the typeface.
     * @return The typeface if available, <code>null</code> otherwise.
     */
    public Typeface get(Object tag) {
        synchronized (this) {
            return mEntries.get(tag);
        }
    }

    public Object tagOf(Typeface typeface) {
        synchronized (this) {
            for (Map.Entry<Object, Typeface> entry : mEntries.entrySet()) {
                Typeface value = entry.getValue();
                if (value == typeface) {
                    return entry.getKey();
                }
            }
        }

        return null;
    }

    public List<Typeface> list() {
        List<Typeface> typefaces = new ArrayList<>(mEntries.size());

        synchronized (this) {
            typefaces.addAll(mEntries.values());
        }

        Comparator<Typeface> sortOrder = new TypefaceComparator();
        Collections.sort(typefaces, sortOrder);

        return Collections.unmodifiableList(typefaces);

    }

    public List<TypeFamily> families() {
        Map<String, List<Typeface>> familyMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        synchronized (this) {
            for (Typeface typeface : mEntries.values()) {
                List<Typeface> entryList = familyMap.get(typeface.getFamilyName());
                if (entryList == null) {
                    entryList = new ArrayList<>();
                    familyMap.put(typeface.getFamilyName(), entryList);
                }

                entryList.add(typeface);
            }
        }

        List<TypeFamily> familyList = new ArrayList<>(familyMap.size());
        Comparator<Typeface> sortOrder = new TypefaceComparator();

        for (Map.Entry<String, List<Typeface>> entry : familyMap.entrySet()) {
            String familyName = entry.getKey();
            List<Typeface> typefaces = entry.getValue();
            Collections.sort(typefaces, sortOrder);

            familyList.add(new TypeFamily(familyName, typefaces));
        }

        return Collections.unmodifiableList(familyList);
    }
}
