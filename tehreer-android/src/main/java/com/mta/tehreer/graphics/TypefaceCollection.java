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

    private HashMap<Object, Typeface> mTagToTypefaceMap;
    private TreeMap<Typeface, Object> mTypefaceToTagMap;

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
        mTagToTypefaceMap = new HashMap<>();
        mTypefaceToTagMap = new TreeMap<>(new TypefaceComparator());
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
            if (mTagToTypefaceMap.containsValue(typeface)) {
                throw new IllegalArgumentException("The specified typeface has already been added");
            }
            if (mTagToTypefaceMap.containsKey(tag)) {
                throw new IllegalArgumentException("The specified tag has already been taken");
            }

            mTagToTypefaceMap.put(tag, typeface);
            mTypefaceToTagMap.put(typeface, tag);
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

            mTagToTypefaceMap.remove(tag);
            mTypefaceToTagMap.remove(typeface);
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
            return mTagToTypefaceMap.get(tag);
        }
    }

    public int size() {
        synchronized (this) {
            return mTagToTypefaceMap.size();
        }
    }

    public Object tagOf(Typeface typeface) {
        synchronized (this) {
            return mTypefaceToTagMap.get(typeface);
        }
    }

    public List<TypeFamily> families() {
        Map<String, List<Typeface>> familyMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        synchronized (this) {
            for (Typeface typeface : mTypefaceToTagMap.keySet()) {
                List<Typeface> entryList = familyMap.get(typeface.getFamilyName());
                if (entryList == null) {
                    entryList = new ArrayList<>();
                    familyMap.put(typeface.getFamilyName(), entryList);
                }

                entryList.add(typeface);
            }
        }

        List<TypeFamily> familyList = new ArrayList<>(familyMap.size());

        for (Map.Entry<String, List<Typeface>> entry : familyMap.entrySet()) {
            String familyName = entry.getKey();
            List<Typeface> typefaces = entry.getValue();

            familyList.add(new TypeFamily(familyName, typefaces));
        }

        return Collections.unmodifiableList(familyList);
    }

    public List<Typeface> typefaces() {
        synchronized (this) {
            List<Typeface> list = new ArrayList<>(mTypefaceToTagMap.keySet());
            return Collections.unmodifiableList(list);
        }
    }
}
