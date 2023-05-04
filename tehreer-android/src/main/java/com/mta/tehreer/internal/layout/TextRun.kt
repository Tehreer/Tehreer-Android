/*
 * Copyright (C) 2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.layout

import android.graphics.Canvas
import com.mta.tehreer.sfnt.WritingDirection
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.collections.FloatList
import android.graphics.RectF
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface

internal interface TextRun {
    val charStart: Int
    val charEnd: Int
    val isBackward: Boolean
    val bidiLevel: Byte
    val spans: List<Any?>

    val startExtraLength: Int
    val endExtraLength: Int

    val typeface: Typeface
    val typeSize: Float
    val writingDirection: WritingDirection

    val glyphCount: Int
    val glyphIds: IntList
    val glyphOffsets: PointList
    val glyphAdvances: FloatList

    val clusterMap: IntList
    val caretEdges: FloatList

    val ascent: Float
    val descent: Float
    val leading: Float

    val width: Float
    val height: Float

    fun getClusterStart(charIndex: Int): Int
    fun getClusterEnd(charIndex: Int): Int

    fun getGlyphRangeForChars(fromIndex: Int, toIndex: Int): IntArray
    fun getLeadingGlyphIndex(charIndex: Int): Int
    fun getTrailingGlyphIndex(charIndex: Int): Int

    fun getCaretBoundary(fromIndex: Int, toIndex: Int): Float
    fun getCaretEdge(charIndex: Int): Float

    fun getRangeDistance(fromIndex: Int, toIndex: Int): Float
    fun computeNearestCharIndex(distance: Float): Int

    fun computeTypographicExtent(glyphStart: Int, glyphEnd: Int): Float
    fun computeBoundingBox(renderer: Renderer, glyphStart: Int, glyphEnd: Int): RectF

    fun draw(renderer: Renderer, canvas: Canvas)
}
