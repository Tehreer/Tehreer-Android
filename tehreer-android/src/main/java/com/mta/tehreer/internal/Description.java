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

package com.mta.tehreer.internal;

import com.mta.tehreer.collections.ByteList;
import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.internal.collections.ArrayByteList;

import java.util.Iterator;

public class Description {

    private static final String NULL = "null";

    public static String forByteArray(byte[] array) {
        return forByteList(new ArrayByteList(array, 0, array.length));
    }

    public static String forByteList(ByteList list) {
        if (list != null) {
            Description description = new Description();
            description.begin();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                description.append(String.valueOf(list.get(i)));
            }
            description.end();

            return description.toString();
        }

        return NULL;
    }

    public static String forIntList(IntList list) {
        if (list != null) {
            Description description = new Description();
            description.begin();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                description.append(String.valueOf(list.get(i)));
            }
            description.end();

            return description.toString();
        }

        return NULL;
    }

    public static String forFloatList(FloatList list) {
        if (list != null) {
            Description description = new Description();
            description.begin();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                description.append(String.valueOf(list.get(i)));
            }
            description.end();

            return description.toString();
        }

        return NULL;
    }

    public static String forPointList(PointList list) {
        if (list != null) {
            Description description = new Description();
            description.begin();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                description.append(String.valueOf(list.getX(i)));
                description.append(String.valueOf(list.getY(i)));
            }
            description.end();

            return description.toString();
        }

        return NULL;
    }

    public static <T> String forIterator(Iterator<T> iterator) {
        if (iterator != null) {
            Description description = new Description();
            description.begin();
            while (iterator.hasNext()) {
                T element = iterator.next();
                description.append(element.toString());
            }
            description.end();

            return description.toString();
        }

        return NULL;
    }

    public static <T> String forIterable(Iterable<T> iterable) {
        return forIterator(iterable.iterator());
    }

    private final StringBuilder builder = new StringBuilder();
    private boolean begun = false;

    private Description() {
    }

    private void begin() {
        builder.append("[");
        begun = true;
    }

    private void append(String value) {
        if (!begun) {
            builder.append(", ");
        }
        builder.append(value);
        begun = false;
    }

    private void end() {
        builder.append("]");
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
