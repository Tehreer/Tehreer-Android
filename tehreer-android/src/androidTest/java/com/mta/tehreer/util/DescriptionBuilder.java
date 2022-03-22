/*
 * Copyright (C) 2022 Muhammad Tayyab Akram
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

package com.mta.tehreer.util;

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.ByteList;
import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.internal.Description;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class DescriptionBuilder<T> {
    public static @NonNull <T> DescriptionBuilder<T> of(Class<T> clazz) {
        return new DescriptionBuilder<>(clazz);
    }

    private final Class<T> clazz;
    private final Map<String, Object> properties = new LinkedHashMap<>();

    private DescriptionBuilder(@NonNull Class<T> clazz) {
        this.clazz = clazz;
    }

    public @NonNull DescriptionBuilder<T> put(@NonNull String name, Object value) {
        properties.put(name, value);
        return this;
    }

    private @NonNull String getDescription(Object object) {
        if (object instanceof byte[]) {
            return Description.forByteArray((byte[]) object);
        }
        if (object instanceof int[]) {
            return Description.forIntList(IntList.of((int[]) object));
        }
        if (object instanceof float[]) {
            return Description.forFloatList(FloatList.of((float[]) object));
        }
        if (object instanceof Object[]) {
            return Description.forIterable(Arrays.asList((Object[]) object));
        }
        if (object instanceof ByteList) {
            return Description.forByteList((ByteList) object);
        }
        if (object instanceof IntList) {
            return Description.forIntList((IntList) object);
        }
        if (object instanceof FloatList) {
            return Description.forFloatList((FloatList) object);
        }
        if (object instanceof PointList) {
            return Description.forPointList((PointList) object);
        }
        if (object instanceof Iterator<?>) {
            return Description.forIterator((Iterator<?>) object);
        }
        if (object instanceof Iterable<?>) {
            return Description.forIterable((Iterable<?>) object);
        }

        return Objects.toString(object);
    }

    public @NonNull String build() {
        StringBuilder builder = new StringBuilder();
        builder.append(clazz.getSimpleName())
               .append("{");

        boolean isFirst = true;

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String name = entry.getKey();
            String value = getDescription(entry.getValue());

            builder.append(isFirst ? "" : ", ");
            builder.append(name).append("=").append(value);

            isFirst = false;
        }

        builder.append("}");

        return builder.toString();
    }
}
