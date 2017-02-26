/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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
 * Specifies if the glyph being drawn is filled, stroked, or both.
 */
public enum RenderingStyle {
    /**
     * Glyphs drawn with this style will be filled, ignoring all stroke-related settings in the
     * renderer.
     */
    FILL,
    /**
     * Glyphs drawn with this style will be both filled and stroked at the same time, respecting
     * the stroke-related settings in the renderer.
     */
    FILL_STROKE,
    /**
     * Glyphs drawn with this style will be stroked, respecting the stroke-related settings in
     * the renderer.
     */
    STROKE,
}
