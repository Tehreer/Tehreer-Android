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

public class TypefaceManager {

    private static final HashMap<Integer, Typeface> TYPEFACE_MAP = new HashMap<>();

    public static void registerTypeface(int tag, Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }
        if (!Typeface.isFinalizable(typeface)) {
            throw new IllegalArgumentException("Typeface is not finalizable");
        }

        synchronized (TYPEFACE_MAP) {
            if (TYPEFACE_MAP.containsKey(tag)) {
                throw new IllegalArgumentException("A typeface is already registered for tag: " + tag);
            }

            if (TYPEFACE_MAP.containsValue(typeface)) {
                throw new IllegalArgumentException("This typeface is already registered for a different tag");
            }

            TYPEFACE_MAP.put(tag, typeface);
        }
    }

    public static void unregisterTypeface(Typeface typeface) {
        if (typeface == null) {
            throw new NullPointerException("Typeface is null");
        }

        synchronized (TYPEFACE_MAP) {
            Integer tag = null;

            for (Map.Entry<Integer, Typeface> entry : TYPEFACE_MAP.entrySet()) {
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

    public static Typeface getTypeface(int tag) {
        synchronized (TYPEFACE_MAP) {
            return TYPEFACE_MAP.get(tag);
        }
    }
}
