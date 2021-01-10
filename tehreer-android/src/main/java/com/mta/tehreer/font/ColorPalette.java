/*
 * Copyright (C) 2020-2021 Muhammad Tayyab Akram
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
import androidx.annotation.Size;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;
import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

public final class ColorPalette {
    public static final int USABLE_WITH_LIGHT_BACKGROUND = 0x0001;
    public static final int USABLE_WITH_DARK_BACKGROUND = 0x0002;

    /** @hide */
    @IntDef(
        flag = true,
        value = {
            USABLE_WITH_LIGHT_BACKGROUND,
            USABLE_WITH_DARK_BACKGROUND
        })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Flags { }

    private final @NonNull String name;
    private final @Flags int flags;
    private final @NonNull int[] colors;

    /**
     * Returns a palette object with the specified values.
     *
     * @param name The display name.
     * @param flags The property flags.
     * @param colors The colors array.
     * @return A new palette object.
     */
    public static @NonNull ColorPalette of(@NonNull String name, @Flags int flags,
                                           @NonNull @Size(min = 1) int[] colors) {
        checkNotNull(name, "name");
        checkNotNull(colors, "colors");
        checkArgument(colors.length >= 1, "The colors array is empty");

        return new ColorPalette(name, flags, Arrays.copyOf(colors, colors.length));
    }

    private ColorPalette(@NonNull String name, @Flags int flags, @NonNull int[] colors) {
        this.name = name;
        this.flags = flags;
        this.colors = colors;
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
     * Returns the property flags.
     *
     * @return The property flags.
     */
    public @Flags int flags() {
        return flags;
    }

    /**
     * Returns the colors array.
     *
     * @return The colors array.
     */
    public @NonNull int[] colors() {
        return Arrays.copyOf(colors, colors.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ColorPalette other = (ColorPalette) obj;

        return name.equals(other.name)
            && flags == other.flags
            && Arrays.equals(colors, other.colors);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + flags;
        result = 31 * result + Arrays.hashCode(colors);

        return result;
    }
}
