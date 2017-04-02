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
import java.util.Set;
import java.util.TreeSet;

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

    private static final List<TypeFamily> TYPE_FAMILIES = new ArrayList<>();
    private static final Map<Object, Typeface> TYPEFACE_TAGS = new HashMap<>();

    private static class FamilyNameComparator implements Comparator {
        @Override
        public int compare(Object obj1, Object obj2) {
            TypeFamily family = (TypeFamily) obj1;
            String name = (String) obj2;

            return family.getFamilyName().compareToIgnoreCase(name);
        }
    }

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

    private static TypeFamily getTypeFamily(String familyName) {
        Comparator comparator = new FamilyNameComparator();
        int index = Collections.binarySearch(TYPE_FAMILIES, familyName, comparator);
        if (index < 0) {
            TypeFamily typeFamily = new TypeFamily(familyName, new ArrayList<Typeface>());
            TYPE_FAMILIES.add(-(index + 1), typeFamily);
            return typeFamily;
        }

        return TYPE_FAMILIES.get(index);
    }

    private static void addTypeface(TypeFamily typeFamily, Typeface typeface) {
        List<Typeface> list = typeFamily.typefaces;
        Comparator<Typeface> comparator = new TypefaceComparator();
        int index = Collections.binarySearch(list, typeface, comparator) + 1;
        if (index < 0) {
            index *= -1;
        }

        list.add(index, typeface);
    }

    /**
     * Registers a typeface against a specified tag in <code>TypefaceManager</code>.
     *
     * @param typeface The finalizable instance of the typeface.
     * @param tag The tag to identify the typeface.
     *
     * @throws NullPointerException if either <code>typeface</code> or <code>tag</code> is null.
     * @throws IllegalArgumentException if <code>typeface</code> is already registered for a
     *         different tag, or another typeface is registered for specified <code>tag</code>.
     */
    public static void registerTypeface(Typeface typeface, Object tag) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }
        if (tag == null) {
            throw new NullPointerException("Tag is null");
        }

        synchronized (TypefaceManager.class) {
            if (typeface.faceTag != null) {
                throw new IllegalArgumentException("This typeface is already registered for a different tag");
            }
            if (TYPEFACE_TAGS.containsKey(tag)) {
                throw new IllegalArgumentException("Another typeface is already registered for specified tag");
            }

            TYPEFACE_TAGS.put(tag, typeface);
            typeface.faceTag = tag;

            TypeFamily typeFamily = getTypeFamily(typeface.getFamilyName());
            addTypeface(typeFamily, typeface);
            typeface.typeFamily = typeFamily;
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
    public static void unregisterTypeface(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        synchronized (TypefaceManager.class) {
            Object faceTag = typeface.faceTag;
            if (faceTag == null) {
                throw new IllegalArgumentException("This typeface is not registered");
            }

            typeface.faceTag = null;
            TYPEFACE_TAGS.remove(faceTag);

            TypeFamily typeFamily = typeface.typeFamily;
            typeface.typeFamily = null;
            typeFamily.typefaces.remove(typeface);
        }
    }

    public static List<TypeFamily> getAllFamilies() {
        synchronized (TypefaceManager.class) {
            return Collections.unmodifiableList(TYPE_FAMILIES);
        }
    }

    public static List<Typeface> getAllTypefaces() {
        synchronized (TypefaceManager.class) {
            Comparator<Typeface> comparator = new TypefaceComparator();
            Set<Typeface> typefaces = new TreeSet<>(comparator);
            typefaces.addAll(TYPEFACE_TAGS.values());

            return Collections.unmodifiableList(new ArrayList<>(typefaces));
        }
    }

    /**
     * Returns a list containing the tags of registered typefaces.
     *
     * @return A list containing the tags of registered typefaces, or an empty list if no typeface
     *         is registered.
     */
    public static Set<Object> getTakenTags() {
        synchronized (TypefaceManager.class) {
            return Collections.unmodifiableSet(TYPEFACE_TAGS.keySet());
        }
    }

    /**
     * Returns the typeface registered against the specified tag.
     *
     * @param tag The tag to identify the typeface.
     * @return The registered typeface, or <code>null</code> if no typeface is registered against
     *         the specified tag.
     */
    public static Typeface getTypeface(Object tag) {
        synchronized (TypefaceManager.class) {
            return TYPEFACE_TAGS.get(tag);
        }
    }

    private TypefaceManager() {
    }
}
