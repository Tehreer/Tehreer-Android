/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.layout

/**
 * Specifies the kinds of boundaries that a [Typesetter] can be searched for.
 */
enum class BoundaryType {
    /** A boundary between two grapheme clusters. */
    GRAPHEME,

    /** A boundary between two word segments, which are words, spaces or punctuations. */
    WORD,

    /** A boundary between two paragraphs, which end with their separator. */
    PARAGRAPH
}

/**
 * Specifies the direction of searching for a boundary.
 */
enum class BoundaryDirection {
    /** Searches for the last boundary before an index. */
    BACKWARD,

    /** Searches for the first boundary after an index. */
    FORWARD
}

/**
 * A range of characters that a boundary search found.
 *
 * @property startIndex The index to the first character of the range.
 * @property endIndex The index after the last character of the range.
 */
data class BoundaryRange(val startIndex: Int, val endIndex: Int)
