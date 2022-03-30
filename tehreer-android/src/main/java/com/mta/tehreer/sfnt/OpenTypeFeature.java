/*
 * Copyright (C) 2018-2022 Muhammad Tayyab Akram
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

package com.mta.tehreer.sfnt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents an OpenType Layout feature.
 */
public final class OpenTypeFeature {
    private final int tag;
    private final int value;

    /**
     * Returns an open type feature object with the specified tag and value.
     * <p>
     * A tag can be created from string by using {@link SfntTag#make(String)} method.
     *
     * @param tag The tag of the feature that identifies its typographic function and effects.
     * @param value The value of the feature that modifies its behaviour.
     * @return A new open type feature object.
     */
    public static @NonNull OpenTypeFeature of(int tag, int value) {
        return new OpenTypeFeature(tag, value);
    }

    private OpenTypeFeature(int tag, int value) {
        this.tag = tag;
        this.value = value;
    }

    /**
     * Returns the tag of the feature that identifies its typographic function and effects.
     *
     * @return The tag of the feature that identifies its typographic function and effects.
     */
    public int tag() {
        return tag;
    }

    /**
     * Returns the value of the feature that modifies its behaviour.
     *
     * @return The value of the feature that modifies its behaviour.
     */
    public int value() {
        return value;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        OpenTypeFeature other = (OpenTypeFeature) obj;

        return (tag == other.tag
                && value == other.value);
    }

    @Override
    public int hashCode() {
        int result = tag;
        result = 31 * result + value;

        return result;
    }

    @Override
    public String toString() {
        return "OpenTypeFeature{tag=" + SfntTag.toString(tag)
                + ", value=" + String.valueOf(value)
                + "}";
    }
}
