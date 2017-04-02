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

    private static final Map<Object, Typeface> TYPEFACE_TAGS = new HashMap<>();
    private static final Set<TypeFamily> TYPE_FAMILIES = new TreeSet<>(new Comparator<TypeFamily>() {
        @Override
        public int compare(TypeFamily typeFamily, TypeFamily t1) {
            return typeFamily.getFamilyName().compareTo(t1.getFamilyName());
        }
    });

    private static TypeFamily getFamilyForName(String familyName) {
        for (TypeFamily family : TYPE_FAMILIES) {
            if (family.getFamilyName().equalsIgnoreCase(familyName)) {
                return family;
            }
        }

        return new TypeFamily(familyName, new ArrayList<Typeface>());
    }

    private static TypeFamily getTypefaceFamily(Typeface typeface) {
        for (TypeFamily family : TYPE_FAMILIES) {
            if (family.typefaces.contains(typeface)) {
                return family;
            }
        }

        return null;
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
            if (TYPEFACE_TAGS.containsValue(typeface)) {
                throw new IllegalArgumentException("This typeface is already registered for a different tag");
            }
            if (TYPEFACE_TAGS.containsKey(tag)) {
                throw new IllegalArgumentException("Another typeface is already registered for specified tag");
            }

            TYPEFACE_TAGS.put(tag, typeface);

            TypeFamily typeFamily = getFamilyForName(typeface.getFamilyName());
            typeFamily.typefaces.add(typeface);
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
            Object tag = null;

            for (Map.Entry<Object, Typeface> entry : TYPEFACE_TAGS.entrySet()) {
                if (entry.getValue().equals(typeface)) {
                    tag = entry.getKey();
                }
            }

            if (tag == null) {
                throw new IllegalArgumentException("This typeface is not registered");
            }

            TYPEFACE_TAGS.remove(tag);

            TypeFamily typeFamily = getTypefaceFamily(typeface);
            typeFamily.typefaces.remove(typeface);
        }
    }

    /**
     * Returns a list containing the tags of registered typefaces.
     *
     * @return A list containing the tags of registered typefaces, or an empty list if no typeface
     *         is registered.
     */
    public static List<Object> getTakenTags() {
        synchronized (TypefaceManager.class) {
            return new ArrayList<>(TYPEFACE_TAGS.keySet());
        }
    }

    public static List<TypeFamily> getAllFamilies() {
        synchronized (TypefaceManager.class) {
            return new ArrayList<>(TYPE_FAMILIES);
        }
    }

    public static List<Typeface> getAllTypefaces() {
        synchronized (TypefaceManager.class) {
            Set<Typeface> sortedTypefaces = new TreeSet<>(new Comparator<Typeface>() {
                @Override
                public int compare(Typeface typeface, Typeface t1) {
                    int result = typeface.getFamilyName().compareTo(t1.getFamilyName());
                    if (result == 0) {
                        return typeface.getFaceName().compareTo(t1.getFaceName());
                    }

                    return result;
                }
            });
            sortedTypefaces.addAll(TYPEFACE_TAGS.values());

            return new ArrayList<>(sortedTypefaces);
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
