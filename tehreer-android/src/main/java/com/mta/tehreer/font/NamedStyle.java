/*
 * Copyright (C) 2021 Muhammad Tayyab Akram
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
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import java.util.Arrays;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;
import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * Represents font named style.
 */
public class NamedStyle {
    private final @NonNull String styleName;
    private final @NonNull float[] coordinates;
    private final @Nullable String postScriptName;

    /**
     * Returns a named style object with the specified values.
     *
     * @param styleName The style name.
     * @param coordinates The variation coordinates.
     * @param postScriptName The post script name.
     * @return A new named style object.
     */
    public static @NonNull NamedStyle of(@NonNull String styleName,
                                         @NonNull @Size(min = 1) float[] coordinates,
                                         @Nullable String postScriptName) {
        checkNotNull(styleName, "styleName");
        checkNotNull(coordinates, "coordinates");
        checkArgument(coordinates.length >= 1, "The coordinates array is empty");

        return new NamedStyle(styleName, coordinates, postScriptName);
    }

    private NamedStyle(@NonNull String styleName, @NonNull float[] coordinates,
                       @Nullable String postScriptName) {
        this.styleName = styleName;
        this.coordinates = coordinates;
        this.postScriptName = postScriptName;
    }

    /**
     * Returns the style name.
     *
     * @return The style name.
     */
    public @NonNull String styleName() {
        return styleName;
    }

    /**
     * Returns the coordinates.
     *
     * @return The coordinates.
     */
    public @NonNull float[] coordinates() {
        return Arrays.copyOf(coordinates, coordinates.length);
    }

    /**
     * Returns the post script name.
     *
     * @return The post script name.
     */
    public @Nullable String postScriptName() {
        return postScriptName;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        NamedStyle other = (NamedStyle) obj;

        return styleName.equals(other.styleName)
            && Arrays.equals(coordinates, other.coordinates)
            && postScriptName != null ? postScriptName.equals(other.postScriptName) : other.postScriptName == null;
    }

    @Override
    public int hashCode() {
        int result = styleName.hashCode();
        result = 31 * result + Arrays.hashCode(coordinates);
        result = 31 * result + (postScriptName != null ? postScriptName.hashCode() : 0);

        return result;
    }
}
