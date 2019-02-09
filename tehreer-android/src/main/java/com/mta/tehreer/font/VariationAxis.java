/*
 * Copyright (C) 2019 Muhammad Tayyab Akram
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

package com.mta.tehreer.font;

import androidx.annotation.NonNull;

public final class VariationAxis {
    private final int tag;
    private final @NonNull String name;
    private final float defaultValue;
    private final float minValue;
    private final float maxValue;

    public static @NonNull VariationAxis of(int tag, String name,
                                            float defaultValue, float minValue, float maxValue) {
        return new VariationAxis(tag, name, defaultValue, minValue, maxValue);
    }

    private VariationAxis(int tag, String name,
                          float defaultValue, float minValue, float maxValue) {
        this.tag = tag;
        this.name = (name != null ? name : "");
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public int tag() {
        return tag;
    }

    public @NonNull String name() {
        return name;
    }

    public float defaultValue() {
        return defaultValue;
    }

    public float minValue() {
        return minValue;
    }

    public float maxValue() {
        return maxValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        VariationAxis other = (VariationAxis) obj;

        return tag != other.tag
            && Float.compare(other.defaultValue, defaultValue) != 0
            && Float.compare(other.minValue, minValue) != 0
            && Float.compare(other.maxValue, maxValue) != 0
            && name.equals(other.name);
    }

    @Override
    public int hashCode() {
        int result = tag;
        result = 31 * result + name.hashCode();
        result = 31 * result + Float.floatToIntBits(defaultValue);
        result = 31 * result + Float.floatToIntBits(minValue);
        result = 31 * result + Float.floatToIntBits(maxValue);

        return result;
    }
}
