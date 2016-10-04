/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

import java.util.HashMap;
import java.util.Map;

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

    private static final HashMap<Object, Typeface> TYPEFACE_MAP = new HashMap<>();

    /**
     * Registers a typeface against a specified tag in <code>TypefaceManager</code>.
     *
     * @param tag The tag to identify the typeface.
     * @param typeface The finalizable instance of the typeface.
     *
     * @throws NullPointerException if <code>tag</code> is null or <code>typeface</code> is null.
     * @throws IllegalArgumentException if <code>typeface</code> is not finalizable, or
     *         <code>typeface</code> is already registered for a different tag, or another typeface
     *         is registered for specified <code>tag</code>.
     */
    public static void registerTypeface(Object tag, Typeface typeface) {
        if (tag == null) {
            throw new NullPointerException("Tag is null");
        }
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }
        if (!Typeface.isFinalizable(typeface)) {
            throw new IllegalArgumentException("Typeface is not finalizable");
        }

        synchronized (TYPEFACE_MAP) {
            if (TYPEFACE_MAP.containsValue(typeface)) {
                throw new IllegalArgumentException("This typeface is already registered for a different tag");
            }

            if (TYPEFACE_MAP.containsKey(tag)) {
                throw new IllegalArgumentException("A different typeface is already registered for tag: " + tag);
            }

            TYPEFACE_MAP.put(tag, typeface);
        }
    }

    /**
     * Unregisters a typeface in <code>TypefaceManager</code> thus making it available for GC.
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

        synchronized (TYPEFACE_MAP) {
            Object tag = null;

            for (Map.Entry<Object, Typeface> entry : TYPEFACE_MAP.entrySet()) {
                if (entry.getValue().equals(typeface)) {
                    tag = entry.getKey();
                }
            }

            if (tag == null) {
                throw new IllegalArgumentException("This typeface is not registered");
            }

            TYPEFACE_MAP.remove(tag);
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
        synchronized (TYPEFACE_MAP) {
            return TYPEFACE_MAP.get(tag);
        }
    }

    private TypefaceManager() {
    }
}
