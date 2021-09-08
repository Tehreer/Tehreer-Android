/*
 * Copyright (C) 2017-2021 Muhammad Tayyab Akram
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

import androidx.annotation.NonNull;

/**
 * Specifies the slope of a typeface.
 */
public enum TypeSlope {
    /**
     * The plain slope indicating upright characters.
     */
    PLAIN,
    /**
     * The italic slope indicating truly slanted characters which appear as they were designed.
     */
    ITALIC,
    /**
     * The oblique slope indicating artificially slanted characters.
     */
    OBLIQUE;

    private static final TypeSlope[] ALL_VALUES = TypeSlope.values();

    static @NonNull TypeSlope valueOf(int value) {
        return ALL_VALUES[value];
    }

    static @NonNull TypeSlope fromItal(float ital) {
        return ital >= 1.0f ? ITALIC : PLAIN;
    }

    static @NonNull TypeSlope fromSlnt(float slnt) {
        return slnt != 0.0f ? OBLIQUE : PLAIN;
    }
}
