/*
 * Copyright (C) 2019-2023 Muhammad Tayyab Akram
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

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * Represents font variation axis.
 */
public final class VariationAxis {
    /**
     * The axis should not be exposed directly in user interfaces.
     */
    public static final int FLAG_HIDDEN_AXIS = 0x0001;

    /** @hidden */
    @IntDef (
        flag = true,
        value = {
            FLAG_HIDDEN_AXIS
        })
    @Retention (RetentionPolicy.SOURCE)
    public @interface Flags { }

    private final int tag;
    private final @NonNull String name;
    private final @Flags int flags;
    private final float defaultValue;
    private final float minValue;
    private final float maxValue;

    /**
     * Returns a variation axis object with the specified values.
     *
     * @param tag Tag identifying the design variation.
     * @param name The display name.
     * @param flags Axis qualifiers.
     * @param defaultValue The default coordinate value.
     * @param minValue The minimum coordinate value.
     * @param maxValue The maximum coordinate value.
     * @return A new variation axis object.
     */
    public static @NonNull VariationAxis of(int tag, @NonNull String name, @Flags int flags,
                                            float defaultValue, float minValue, float maxValue) {
        checkNotNull(name, "name");

        return new VariationAxis(tag, name, flags, defaultValue, minValue, maxValue);
    }

    private VariationAxis(int tag, @Nullable  String name, @Flags int flags,
                          float defaultValue, float minValue, float maxValue) {
        this.tag = tag;
        this.name = name;
        this.flags = flags;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Returns the tag identifying the design variation.
     *
     * @return The tag identifying the design variation.
     */
    public int tag() {
        return tag;
    }

    /**
     * Returns the display name.
     *
     * @return The display name.
     */
    public @NonNull String name() {
        return name;
    }

    /**
     * Returns the axis qualifiers.
     *
     * @return The axis qualifiers.
     */
    public @Flags int flags() {
        return flags;
    }

    /**
     * Returns the default coordinate value.
     *
     * @return The default coordinate value.
     */
    public float defaultValue() {
        return defaultValue;
    }

    /**
     * Returns the minimum coordinate value.
     *
     * @return The minimum coordinate value.
     */
    public float minValue() {
        return minValue;
    }

    /**
     * Returns the maximum coordinate value.
     *
     * @return The maximum coordinate value.
     */
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

        return tag == other.tag
            && name.equals(other.name)
            && flags == other.flags
            && Float.compare(other.defaultValue, defaultValue) == 0
            && Float.compare(other.minValue, minValue) == 0
            && Float.compare(other.maxValue, maxValue) == 0;
    }

    @Override
    public int hashCode() {
        int result = tag;
        result = 31 * result + name.hashCode();
        result = 31 * result + flags;
        result = 31 * result + Float.floatToIntBits(defaultValue);
        result = 31 * result + Float.floatToIntBits(minValue);
        result = 31 * result + Float.floatToIntBits(maxValue);

        return result;
    }
}
