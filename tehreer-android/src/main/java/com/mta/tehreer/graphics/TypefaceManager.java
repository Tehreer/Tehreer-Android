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

    private HashMap<Object, Typeface> mTags;
    private ArrayList<Typeface> mTypefaces;
    private boolean mSorted;

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
        mTags = new HashMap<>();
        mTypefaces = new ArrayList<>();
    }

    /**
     * Registers a typeface in <code>TypefaceManager</code>.
     *
     * @param typeface The typeface that will be registered.
     * @param tag An optional tag to identify the typeface.
     *
     * @throws NullPointerException if <code>typeface</code> is null.
     * @throws IllegalArgumentException if <code>typeface</code> is already registered, or
     *         <code>tag</code> is already taken.
     */
    public void registerTypeface(Typeface typeface, Object tag) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        synchronized (this) {
            if (mTypefaces.contains(typeface)) {
                throw new IllegalArgumentException("This typeface is already registered");
            }
            if (mTags.containsKey(tag)) {
                throw new IllegalArgumentException("This tag is already taken");
            }

            mSorted = false;
            mTypefaces.add(typeface);
            mTags.put(tag, typeface);
            typeface.tag = tag;
        }
    }

    /**
     * Unregisters a typeface in <code>TypefaceManager</code>.
     *
     * @param typeface The typeface to unregister.
     *
     * @throws NullPointerException if <code>typeface</code> is null.
     * @throws IllegalArgumentException if <code>typeface</code> is not registered.
     */
    public void unregisterTypeface(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        synchronized (this) {
            int index = mTypefaces.indexOf(typeface);
            if (index < 0) {
                throw new IllegalArgumentException("This typeface is not registered");
            }

            mTypefaces.remove(index);
            mTags.remove(typeface.tag);
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
            return mTags.get(tag);
        }
    }

    /**
     * Returns the tag of a registered typeface.
     *
     * @param typeface The typeface whose tag is returned.
     * @return The tag of the typeface, or <code>null</code> if no tag was specified while
     *         registration.
     *
     * @throws NullPointerException if <code>typeface</code> is null.
     * @throws IllegalArgumentException if <code>typeface</code> is not registered.
     */
    public Object getTypefaceTag(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        synchronized (this) {
            if (!mTypefaces.contains(typeface)) {
                throw new IllegalArgumentException("This typeface is not registered");
            }

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
            for (Typeface typeface : mTypefaces) {
                if (typeface.getFullName().equalsIgnoreCase(fullName)) {
                    return typeface;
                }
            }
        }

        return null;
    }

    /**
     * Returns a list of available type families sorted by their names in ascending order.
     *
     * @return A list of available type families.
     */
    public List<TypeFamily> getAvailableFamilies() {
        Map<String, List<Typeface>> familyMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        synchronized (this) {
            sortTypefaces();

            for (Typeface typeface : mTypefaces) {
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

    /**
     * Returns a list of available typefaces sorted by their family and style names in ascending
     * order.
     *
     * @return A list of available typefaces.
     */
    public List<Typeface> getAvailableTypefaces() {
        synchronized (this) {
            sortTypefaces();

            return Collections.unmodifiableList(new ArrayList<>(mTypefaces));
        }
    }

    private void sortTypefaces() {
        if (!mSorted) {
            Collections.sort(mTypefaces, new TypefaceComparator());
            mSorted = true;
        }
    }
}
