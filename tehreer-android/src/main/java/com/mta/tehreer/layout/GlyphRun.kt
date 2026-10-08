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

import android.graphics.Canvas
import android.graphics.RectF
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.collections.FloatBufferList
import com.mta.tehreer.internal.collections.FloatBufferPointList
import com.mta.tehreer.internal.collections.UInt16BufferIntList
import com.mta.tehreer.internal.collections.UIntPtrBufferIntList
import com.mta.tehreer.sfnt.WritingDirection
import java.util.NavigableMap

/**
 * A glyph run represents a consecutive sequence of glyphs sharing the same attributes and
 * direction.
 */
class GlyphRun internal constructor(
    /** The line that has this run, which keeps the run of Core alive. */
    private val owner: ComposedLine,
    private val nativeRun: Long,
    private val typefaces: Map<Long, Typeface>,
    private val holders: NavigableMap<Int, ReplacementHolder>
) {
    /**
     * Returns the index to the first character of this run in source text.
     */
    val charStart: Int
        get() = nGetCharStart(nativeRun)

    /**
     * Returns the index after the last character of this run in source text.
     */
    val charEnd: Int
        get() = nGetCharEnd(nativeRun)

    /**
     * Returns the extra excluded length at the start of the cluster map.
     *
     * If the first cluster of this run begins within the extra range, then its rendering will be
     * clipped from the start. The amount of clipping would be equal to the perceived trailing caret
     * position of last excluded character.
     *
     * For example, consider three characters `f`, `i` and another `i` form a cluster having a
     * single ligature, `fii` and the run starts from the second `i` with `f` and `i` being extra
     * characters. In this case, the ligature would be divided into three equal parts and the first
     * two parts would be clipped.
     */
    val startExtraLength: Int
        get() = nGetStartExtraLength(nativeRun)

    /**
     * Returns the extra excluded length at the end of the cluster map.
     *
     * If the last cluster of this run finishes within the excluded range, then its rendering will
     * be clipped from the end. The amount of clipping would be equal to the perceived leading caret
     * position of first excluded character.
     *
     * For example, consider three characters `f`, `i` and another `i` form a cluster having a
     * single ligature, `fii` and the run consists of just `f` with both `i` being extra
     * characters. In this case, the ligature would be divided into three equal parts and the last
     * two parts would be clipped.
     */
    val endExtraLength: Int
        get() = nGetEndExtraLength(nativeRun)

    /**
     * Returns the bidirectional level of this run.
     */
    val bidiLevel: Byte
        get() = nGetBidiLevel(nativeRun).toByte()

    /**
     * Returns the typeface of this run.
     */
    val typeface: Typeface
        get() = typefaces.getValue(nGetTypeface(nativeRun))

    /**
     * Returns the type size of this run.
     */
    val typeSize: Float
        get() = nGetTypeSize(nativeRun)

    private val scaleX: Float
        get() = nGetScaleX(nativeRun)

    /**
     * Returns the writing direction of this run.
     */
    val writingDirection: WritingDirection
        get() = if (nGetWritingDirection(nativeRun) == 1) {
            WritingDirection.RIGHT_TO_LEFT
        } else {
            WritingDirection.LEFT_TO_RIGHT
        }

    private val isBackward: Boolean
        get() = nIsBackward(nativeRun)

    /** Returns the replacement span of this run, if it is made for one. */
    internal val replacement: ReplacementHolder?
        get() = if (nHasReplacement(nativeRun)) holders.floorEntry(charStart)?.value else null

    /**
     * Returns the number of glyphs in this run.
     */
    val glyphCount: Int
        get() = nGetGlyphCount(nativeRun)

    /**
     * Returns the glyph IDs of this run.
     */
    val glyphIds: IntList
        get() = UInt16BufferIntList(this, nGetGlyphIdsPtr(nativeRun), glyphCount)

    /**
     * Returns the glyph offsets of this run.
     */
    val glyphOffsets: PointList
        get() = FloatBufferPointList(this, nGetGlyphOffsetsPtr(nativeRun), glyphCount)

    /**
     * Returns the glyph advances of this run.
     */
    val glyphAdvances: FloatList
        get() = FloatBufferList(this, nGetGlyphAdvancesPtr(nativeRun), glyphCount)

    /**
     * Returns the indexes, mapping each character of this run to corresponding glyph.
     */
    val clusterMap: IntList
        get() = UIntPtrBufferIntList(this, nGetClusterMapPtr(nativeRun), nGetClusterMapCount(nativeRun))

    /**
     * Returns the x- origin of this run in parent line.
     */
    val originX: Float
        get() = nGetOriginX(nativeRun)

    /**
     * Returns the y- origin of this run in parent line.
     */
    val originY: Float
        get() = nGetOriginY(nativeRun)

    /**
     * Returns the ascent of this run. The ascent is the distance from the top of the `GlyphRun` to
     * the baseline. It is always either positive or zero.
     */
    val ascent: Float
        get() = nGetAscent(nativeRun)

    /**
     * Returns the descent of this run. The descent is the distance from the baseline to the bottom
     * of the `GlyphRun`. It is always either positive or zero.
     */
    val descent: Float
        get() = nGetDescent(nativeRun)

    /**
     * Returns the leading of this run. The leading is the distance that should be placed between
     * two lines.
     */
    val leading: Float
        get() = nGetLeading(nativeRun)

    /**
     * Returns the typographic width of this run.
     */
    val width: Float
        get() = nGetWidth(nativeRun)

    /**
     * Returns the typographic height of this run.
     */
    val height: Float
        get() = nGetHeight(nativeRun)

    private fun checkCharIndex(charIndex: Int) {
        val charStart = charStart
        val charEnd = charEnd

        require(charIndex in charStart until charEnd) {
            "Char Index: $charIndex, Run Range: [$charStart, $charEnd)"
        }
    }

    private fun checkCaretIndex(charIndex: Int) {
        val charStart = charStart
        val charEnd = charEnd

        require(charIndex in charStart..charEnd) {
            "Char Index: $charIndex, Run Range: [$charStart, $charEnd]"
        }
    }

    private fun checkGlyphRange(glyphStart: Int, glyphEnd: Int) {
        val glyphCount = glyphCount

        require(glyphStart >= 0) { "Glyph Start: $glyphStart" }
        require(glyphEnd <= glyphCount) { "Glyph End: $glyphEnd, Glyph Count: $glyphCount" }
        require(glyphStart <= glyphEnd) { "Bad Range: [$glyphStart, $glyphEnd)" }
    }

    /**
     * Returns the index to the first character of specified cluster in source string. In most
     * cases, it would be the same index as the specified one. But if the character occurs within a
     * cluster, then a previous index would be returned; whether the run logically flows forward or
     * backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index to the first character of specified cluster in source string.
     *
     * @throws IllegalArgumentException if `charIndex` is less than run start or greater than or
     *         equal to run end.
     */
    fun getActualClusterStart(charIndex: Int): Int {
        checkCharIndex(charIndex)

        return nGetClusterStart(nativeRun, charIndex)
    }

    /**
     * Returns the index after the last character of specified cluster in source string. In most
     * cases, it would be an index after the specified one. But if the character occurs within a
     * cluster, then a farther index would be returned; whether the run logically flows forward or
     * backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index after the last character of specified cluster in source string.
     *
     * @throws IllegalArgumentException if `charIndex` is less than run start or greater than or
     *         equal to run end.
     */
    fun getActualClusterEnd(charIndex: Int): Int {
        checkCharIndex(charIndex)

        return nGetClusterEnd(nativeRun, charIndex)
    }

    /**
     * Returns the index of leading glyph related to the specified cluster. It will come after the
     * trailing glyph, if the characters of this run logically flow backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index of leading glyph related to the specified cluster.
     *
     * @throws IllegalArgumentException if `charIndex` is less than run start or greater than or
     *         equal to run end.
     */
    fun getLeadingGlyphIndex(charIndex: Int): Int {
        checkCharIndex(charIndex)

        return nGetLeadingGlyphIndex(nativeRun, charIndex)
    }

    /**
     * Returns the index of trailing glyph related to the specified cluster. It will come before the
     * leading glyph, if the characters of this run logically flow backward.
     *
     * @param charIndex The index of a character in source string.
     * @return The index of trailing glyph related to the specified cluster.
     *
     * @throws IllegalArgumentException if `charIndex` is less than run start or greater than or
     *         equal to run end.
     */
    fun getTrailingGlyphIndex(charIndex: Int): Int {
        checkCharIndex(charIndex)

        return nGetTrailingGlyphIndex(nativeRun, charIndex)
    }

    /**
     * Returns the distance of specified character from the start of the run assumed at zero.
     *
     * @param charIndex The index of a character in source string.
     * @return The distance of specified character from the start of the run assumed at zero.
     *
     * @throws IllegalArgumentException if `charIndex` is less than run start or greater than run
     *         end.
     */
    fun computeCharDistance(charIndex: Int): Float {
        checkCaretIndex(charIndex)

        return nGetDistance(nativeRun, charIndex)
    }

    /**
     * Determines the index of character nearest to the specified distance.
     *
     * The process involves iterating over the clusters of this glyph run. If a cluster consists of
     * multiple characters, its total advance is evenly distributed among the number of characters
     * it contains. The advance of each character is added to track the covered distance. This way
     * leading and trailing characters are determined close to the specified distance. Afterwards,
     * the index of nearer character is returned.
     *
     * If `distance` is negative, then run's starting index is returned. If it is beyond run's
     * extent, then ending index is returned. The indices will be reversed in case of right-to-left
     * run.
     *
     * @param distance The distance for which to determine the character index. It should be offset
     *                 from zero origin.
     * @return The index of character nearest to the specified distance. It will be an absolute
     *         index in source string.
     *
     * @see charStart
     * @see charEnd
     */
    fun computeNearestCharIndex(distance: Float): Int {
        return nGetIndexOfCodeUnit(nativeRun, distance)
    }

    internal fun computeBoundingBox(renderer: Renderer): RectF {
        return computeBoundingBox(renderer, 0, glyphCount)
    }

    /**
     * Calculates the bounding box for the given glyph range in this run. The bounding box is a
     * rectangle that encloses the paths of this run's glyphs in the given range, as tightly as
     * possible.
     *
     * @param renderer The renderer to use for calculating the bounding box. This is required
     *                 because the renderer could have settings in it that would cause changes in
     *                 the bounding box.
     * @param glyphStart The index to the first glyph being measured.
     * @param glyphEnd The index after the last glyph being measured.
     * @return A rectangle that tightly encloses the paths of this run's glyphs in the given range.
     *
     * @throws IllegalArgumentException if `glyphStart` is negative, or `glyphEnd` is greater than
     *         total number of glyphs in the run, or `glyphStart` is greater than `glyphEnd`.
     */
    fun computeBoundingBox(renderer: Renderer, glyphStart: Int, glyphEnd: Int): RectF {
        checkGlyphRange(glyphStart, glyphEnd)

        val box = nGetBoundingBox(nativeRun, glyphStart, glyphEnd, renderer.nativeHandle)
        renderer.syncNative()

        return box
    }

    // region Drawing

    private class ClusterRange(
        val actualStart: Int,
        val actualEnd: Int,
        var glyphStart: Int,
        var glyphEnd: Int
    )

    private val isRTL: Boolean
        get() = (nGetBidiLevel(nativeRun) and 1) == 1

    private fun getLeadingEdge(fromIndex: Int, toIndex: Int): Float {
        return nGetDistance(nativeRun, if (!isBackward) fromIndex else toIndex)
    }

    private fun getClusterRange(charIndex: Int, exclusion: ClusterRange?): ClusterRange? {
        val leadingIndex = nGetLeadingGlyphIndex(nativeRun, charIndex)
        val trailingIndex = nGetTrailingGlyphIndex(nativeRun, charIndex)

        val cluster = ClusterRange(
            nGetClusterStart(nativeRun, charIndex),
            nGetClusterEnd(nativeRun, charIndex),
            minOf(leadingIndex, trailingIndex),
            maxOf(leadingIndex, trailingIndex) + 1
        )

        if (exclusion != null) {
            val minStart = minOf(exclusion.glyphStart, cluster.glyphEnd)
            val maxEnd = maxOf(cluster.glyphStart, exclusion.glyphEnd)

            cluster.glyphStart = if (!isBackward) maxEnd else cluster.glyphStart
            cluster.glyphEnd = if (isBackward) minStart else cluster.glyphEnd
        }

        return if (cluster.glyphStart < cluster.glyphEnd) cluster else null
    }

    private fun drawGlyphs(renderer: Renderer, canvas: Canvas, glyphStart: Int, glyphEnd: Int) {
        renderer.drawGlyphs(
            canvas,
            glyphIds.subList(glyphStart, glyphEnd),
            glyphOffsets.subList(glyphStart, glyphEnd),
            glyphAdvances.subList(glyphStart, glyphEnd)
        )
    }

    private fun drawEdgeCluster(renderer: Renderer, canvas: Canvas, cluster: ClusterRange) {
        val charStart = charStart
        val charEnd = charEnd
        val startClipped = cluster.actualStart < charStart
        val endClipped = cluster.actualEnd > charEnd

        val clipLeft: Float
        val clipRight: Float

        if (!isRTL) {
            clipLeft = if (startClipped) nGetDistance(nativeRun, charStart) else -Float.MAX_VALUE
            clipRight = if (endClipped) nGetDistance(nativeRun, charEnd) else Float.MAX_VALUE
        } else {
            clipRight = if (startClipped) nGetDistance(nativeRun, charStart) else Float.MAX_VALUE
            clipLeft = if (endClipped) nGetDistance(nativeRun, charEnd) else -Float.MAX_VALUE
        }

        canvas.save()
        canvas.clipRect(clipLeft, -Float.MAX_VALUE, clipRight, Float.MAX_VALUE)
        canvas.translate(getLeadingEdge(cluster.actualStart, cluster.actualEnd), 0.0f)

        drawGlyphs(renderer, canvas, cluster.glyphStart, cluster.glyphEnd)

        canvas.restore()
    }

    /**
     * Draws this run completely onto the given `canvas` using the given `renderer`.
     *
     * @param renderer The renderer to use for drawing this run.
     * @param canvas The canvas onto which to draw this run.
     */
    fun draw(renderer: Renderer, canvas: Canvas) {
        val replacement = replacement
        if (replacement != null) {
            replacement.draw(canvas, ascent, descent)
            return
        }

        renderer.typeface = typeface
        renderer.typeSize = typeSize
        renderer.scaleX = scaleX
        renderer.writingDirection = writingDirection

        val defaultFillColor = renderer.fillColor
        if (nHasForegroundColor(nativeRun)) {
            renderer.fillColor = nGetForegroundColor(nativeRun)
        }

        val isBackward = isBackward
        val firstIndex = charStart
        val lastIndex = charEnd - 1

        var firstCluster: ClusterRange? = null
        var lastCluster: ClusterRange? = null

        if (startExtraLength > 0) {
            firstCluster = getClusterRange(firstIndex, null)
        }
        if (endExtraLength > 0) {
            lastCluster = getClusterRange(lastIndex, firstCluster)
        }

        var glyphStart = 0
        var glyphEnd = glyphCount

        var chunkStart = firstIndex
        var chunkEnd = lastIndex + 1

        if (firstCluster != null) {
            drawEdgeCluster(renderer, canvas, firstCluster)

            // Exclude first cluster characters.
            chunkStart = firstCluster.actualEnd
            // Exclude first cluster glyphs.
            glyphStart = if (!isBackward) firstCluster.glyphEnd else glyphStart
            glyphEnd = if (isBackward) firstCluster.glyphStart else glyphEnd
        }
        if (lastCluster != null) {
            // Exclude last cluster characters.
            chunkEnd = lastCluster.actualStart
            // Exclude last cluster glyphs.
            glyphEnd = if (!isBackward) lastCluster.glyphStart else glyphEnd
            glyphStart = if (isBackward) lastCluster.glyphEnd else glyphStart
        }

        canvas.save()
        canvas.translate(getLeadingEdge(chunkStart, chunkEnd), 0.0f)

        drawGlyphs(renderer, canvas, glyphStart, glyphEnd)

        canvas.restore()

        if (lastCluster != null) {
            drawEdgeCluster(renderer, canvas, lastCluster)
        }

        renderer.fillColor = defaultFillColor
    }

    // endregion

    override fun toString(): String {
        return "GlyphRun{charStart=$charStart" +
            ", charEnd=$charEnd" +
            ", bidiLevel=$bidiLevel" +
            ", writingDirection=$writingDirection" +
            ", glyphCount=$glyphCount" +
            ", glyphIds=$glyphIds" +
            ", glyphOffsets=$glyphOffsets" +
            ", glyphAdvances=$glyphAdvances" +
            ", clusterMap=$clusterMap" +
            ", originX=$originX" +
            ", originY=$originY" +
            ", ascent=$ascent" +
            ", descent=$descent" +
            ", leading=$leading" +
            ", width=$width" +
            ", height=$height" +
            "}"
    }

    private external fun nGetCharStart(nativeRun: Long): Int
    private external fun nGetCharEnd(nativeRun: Long): Int
    private external fun nGetStartExtraLength(nativeRun: Long): Int
    private external fun nGetEndExtraLength(nativeRun: Long): Int
    private external fun nGetBidiLevel(nativeRun: Long): Int
    private external fun nGetWritingDirection(nativeRun: Long): Int
    private external fun nIsBackward(nativeRun: Long): Boolean
    private external fun nHasForegroundColor(nativeRun: Long): Boolean
    private external fun nGetForegroundColor(nativeRun: Long): Int
    private external fun nGetTypeSize(nativeRun: Long): Float
    private external fun nGetScaleX(nativeRun: Long): Float
    private external fun nGetAscent(nativeRun: Long): Float
    private external fun nGetDescent(nativeRun: Long): Float
    private external fun nGetLeading(nativeRun: Long): Float
    private external fun nGetOriginX(nativeRun: Long): Float
    private external fun nGetOriginY(nativeRun: Long): Float
    private external fun nGetWidth(nativeRun: Long): Float
    private external fun nGetHeight(nativeRun: Long): Float
    private external fun nGetTypeface(nativeRun: Long): Long
    private external fun nHasReplacement(nativeRun: Long): Boolean
    private external fun nGetGlyphCount(nativeRun: Long): Int
    private external fun nGetClusterMapCount(nativeRun: Long): Int
    private external fun nGetGlyphIdsPtr(nativeRun: Long): Long
    private external fun nGetGlyphOffsetsPtr(nativeRun: Long): Long
    private external fun nGetGlyphAdvancesPtr(nativeRun: Long): Long
    private external fun nGetClusterMapPtr(nativeRun: Long): Long
    private external fun nGetClusterStart(nativeRun: Long, charIndex: Int): Int
    private external fun nGetClusterEnd(nativeRun: Long, charIndex: Int): Int
    private external fun nGetLeadingGlyphIndex(nativeRun: Long, charIndex: Int): Int
    private external fun nGetTrailingGlyphIndex(nativeRun: Long, charIndex: Int): Int
    private external fun nGetDistance(nativeRun: Long, charIndex: Int): Float
    private external fun nGetIndexOfCodeUnit(nativeRun: Long, distance: Float): Int
    private external fun nGetBoundingBox(
        nativeRun: Long, glyphStart: Int, glyphEnd: Int, nativeRenderer: Long
    ): RectF
    private companion object {
        init {
            JniBridge.loadLibrary()
        }

    }
}
