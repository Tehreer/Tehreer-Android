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

package com.mta.tehreer.bidi;

/**
 * Specifies the base direction of a paragraph.
 */
public enum BaseDirection {
    /**
     * Base direction is left-to-right.
     */
    LEFT_TO_RIGHT(0),
    /**
     * Base direction is right-to-left.
     */
    RIGHT_TO_LEFT(1),
    /**
     * Base direction depends on the first strong directional character of the paragraph according
     * to Unicode Bidirectional Algorithm. If no strong directional character is present, the base
     * direction is left-to-right.
     */
    DEFAULT_LEFT_TO_RIGHT(0xFE),
    /**
     * Base direction depends on the first strong directional character of the paragraph according
     * to Unicode Bidirectional Algorithm. If no strong directional character is present, the base
     * direction is right-to-left.
     */
    DEFAULT_RIGHT_TO_LEFT(0xFD);

    final int value;

    BaseDirection(int value) {
        this.value = value;
    }
}
