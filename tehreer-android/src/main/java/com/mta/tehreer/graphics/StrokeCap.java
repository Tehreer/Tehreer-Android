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

/**
 * Specifies the treatment for the beginning and ending of stroked lines and paths.
 */
public enum StrokeCap {
    /**
     * The stroke ends with the path, and does not project beyond it.
     */
    BUTT(GlyphAttributes.LINECAP_BUTT),
    /**
     * The stroke projects out as a semicircle, with the center at the end of the path.
     */
    ROUND(GlyphAttributes.LINECAP_ROUND),
    /**
     * The stroke projects out as a square, with the center at the end of the path.
     */
    SQUARE(GlyphAttributes.LINECAP_SQUARE);

    final @GlyphAttributes.LineCap int value;

    StrokeCap(@GlyphAttributes.LineCap int value) {
        this.value = value;
    }
}
