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
 * The <code>TypefaceManager</code> class provides management activities related to typefaces.
 * <p>
 * In general, a typeface is not automatically cached due to the fact that it might be only used in
 * specific scenarios. Caching every typeface that is created can lead to severe memory penalties.
 * <p>
 * The typefaces that are intended to be shared across multiple screens should be registered in
 * <code>TypefaceManager</code> so that they can be easily accessed afterwards.
 */
public class TypefaceManager {

    private static class TypefaceComparator implements Comparator<Typeface> {
        @Override
        public int compare(Typeface obj1, Typeface obj2) {
            int result = obj1.getFamilyName().compareToIgnoreCase(obj2.getFamilyName());
            if (result == 0) {
                return obj1.getStyleName().compareToIgnoreCase(obj2.getStyleName());
            }

            return result;
        }
    }

    private static TypefaceManager sInstance;

    private HashMap<Object, Typeface> mTypefaceMap;

    public static TypefaceManager getDefaultManager() {
        if (sInstance == null) {
            synchronized (TypefaceManager.class) {
                if (sInstance == null) {
                    sInstance = new TypefaceManager();
                }
            }
        }

        return sInstance;
    }

    private TypefaceManager() {
        mTypefaceMap = new HashMap<>();
    }

    /**
     * Registers a typeface against a specified tag in <code>TypefaceManager</code>.
     *
     * @param typeface The typeface that will be registered.
     * @param tag The tag to identify the typeface.
     *
     * @throws NullPointerException if either <code>typeface</code> or <code>tag</code> is null.
     * @throws IllegalArgumentException if <code>typeface</code> is already registered for a
     *         different tag, or another typeface is registered for specified <code>tag</code>.
     */
    public void registerTypeface(Typeface typeface, Object tag) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }
        if (tag == null) {
            throw new NullPointerException("Tag is null");
        }

        synchronized (this) {
            if (mTypefaceMap.containsValue(typeface)) {
                throw new IllegalArgumentException("The specified typeface has already been added");
            }
            if (mTypefaceMap.containsKey(tag)) {
                throw new IllegalArgumentException("The specified tag has already been taken");
            }

            mTypefaceMap.put(tag, typeface);
        }
    }

    /**
     * Unregisters a typeface in <code>TypefaceManager</code>.
     *
     * @param typeface The typeface to unregister.
     *
     * @throws NullPointerException if <code>typeface</code> is null.
     * @throws IllegalArgumentException if <code>typeface</code> is not registered in
     *         <code>TypefaceManager</code>.
     */
    public void unregisterTypeface(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        synchronized (this) {
            Object tag = getTypefaceTag(typeface);
            if (tag == null) {
                throw new IllegalArgumentException("The specified typeface is not available in the collection");
            }

            mTypefaceMap.remove(tag);
            typeface.tag = null;
        }
    }

    /**
     * Returns the typeface registered against the specified tag.
     *
     * @param tag The tag object that identifies the typeface.
     * @return The registered typeface, or <code>null</code> if no typeface is registered against
     *         the specified tag.
     *
     * @throws NullPointerException if <code>tag</code> is null.
     */
    public Typeface getTypeface(Object tag) {
        if (tag == null) {
            throw new NullPointerException("Tag is null");
        }

        synchronized (this) {
            return mTypefaceMap.get(tag);
        }
    }

    /**
     * Returns the tag of a registered typeface.
     *
     * @param typeface The typeface whose tag is returned.
     * @return The tag of specified typeface, or <code>null</code> if <code>typeface</code> is not
     *         registered.
     *
     * @throws NullPointerException if <code>typeface</code> is null.
     */
    public Object getTypefaceTag(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        synchronized (this) {
            return typeface.tag;
        }
    }

    /**
     * Looks for a registered typeface having specified full name.
     *
     * @param fullName The full name of the typeface.
     * @return The typeface having specified full name, or <code>null</code> if no such typeface is
     *         registered.
     */
    public Typeface getTypefaceByName(String fullName) {
        synchronized (this) {
            for (Typeface typeface : mTypefaceMap.values()) {
                if (typeface.getFullName().equalsIgnoreCase(fullName)) {
                    return typeface;
                }
            }
        }

        return null;
    }

    /**
     * Returns a list of available type families.
     *
     * @return A list of available type families.
     */
    public List<TypeFamily> getAvailableFamilies() {
        Map<String, List<Typeface>> familyMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        synchronized (this) {
            for (Typeface typeface : mTypefaceMap.values()) {
                List<Typeface> entryList = familyMap.get(typeface.getFamilyName());
                if (entryList == null) {
                    entryList = new ArrayList<>();
                    familyMap.put(typeface.getFamilyName(), entryList);
                }

                entryList.add(typeface);
            }
        }

        List<TypeFamily> familyList = new ArrayList<>(familyMap.size());
        TypefaceComparator comparator = new TypefaceComparator();

        for (Map.Entry<String, List<Typeface>> entry : familyMap.entrySet()) {
            String familyName = entry.getKey();
            List<Typeface> typefaces = entry.getValue();
            Collections.sort(typefaces, comparator);

            familyList.add(new TypeFamily(familyName, typefaces));
        }

        return Collections.unmodifiableList(familyList);
    }

    /**
     * Returns a list of available typefaces sorted by their family and style names in ascending
     * order.
     *
     * @return A list of available typefaces.
     */
    public List<Typeface> getAvailableTypefaces() {
        synchronized (this) {
            List<Typeface> typefaces = new ArrayList<>(mTypefaceMap.values());
            TypefaceComparator comparator = new TypefaceComparator();
            Collections.sort(typefaces, comparator);

            return Collections.unmodifiableList(typefaces);
        }
    }
}
