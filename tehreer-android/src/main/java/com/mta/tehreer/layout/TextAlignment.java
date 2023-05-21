/*
 * Copyright (C) 2016-2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.layout;

/**
 * Specifies the horizontal text alignment.
 */
public enum TextAlignment {
    /**
     * Aligns text to the left side of the line.
     */
    LEFT,
    /**
     * Aligns text to the right side of the line.
     */
    RIGHT,
    /**
     * Aligns text to the center of the line.
     */
    CENTER,
    /**
     * Aligns text to the left side of the line if its paragraph level is even.
     */
    LEADING,
    /**
     * Aligns text to the right side of the line if its paragraph level is even.
     */
    TRAILING,
}
