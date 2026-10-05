/*
 * Copyright (C) 2023-2026 Muhammad Tayyab Akram
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

import java.util.ArrayList
import kotlin.math.min

internal class RunCollection : ArrayList<TextRun>() {
    /**
     * The block runs (a [ReplacementRun] whose [ReplacementRun.isBlock] is `true`) in this
     * collection, in position order - a small subsequence of [runs][TextRun].
     *
     * Kept separate from the full run list so [findBlockForward]/[findBlockBackward] - called on
     * every break candidate - never have to scan (or allocate a filtered copy of) the whole
     * document's runs to answer "is there a block near here".
     *
     * Populated once, eagerly, by [finalizeBlocks] - NOT lazily. A [Typesetter] (and the
     * [RunCollection] it owns) is explicitly meant to be usable from multiple frames/threads at
     * once (see [ReplacementRun.forFrame]'s "other frames of the same typesetter may be in use"),
     * so a `by lazy(LazyThreadSafetyMode.NONE)` here would be undefined behaviour under a
     * concurrent first read - this collection is built on a single thread
     * (`ShapeResolver.createParagraphsAndRuns()`), but nothing guarantees the *first read* of a
     * lazy field also happens on that thread rather than racing with another one that got a
     * reference to this [RunCollection] first. Computing eagerly, before the collection is ever
     * published to another thread, removes the race entirely: by the time any other thread can
     * see this [RunCollection] at all, [blocks] is already a fully-formed, plain field - nothing
     * left to synchronize on read.
     */
    private var blocks: List<ReplacementRun> = emptyList()

    /**
     * Computes [blocks] eagerly. Must be called exactly once, by
     * `ShapeResolver.createParagraphsAndRuns()` (the sole place a [RunCollection] is built) right
     * before it hands this collection back - after every run has been [add]ed, and before this
     * [RunCollection]/its [Typesetter] is published to any other thread. Calling it again is safe
     * (it simply recomputes the same thing from the current contents) but is not the contract:
     * nothing adds to this collection after `ShapeResolver` returns it.
     */
    fun finalizeBlocks() {
        blocks = filterIsInstance<ReplacementRun>().filter { it.isBlock }
    }

    fun binarySearch(charIndex: Int): Int {
        var low = 0
        var high = size - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val value = this[mid]

            if (charIndex >= value.endIndex) {
                low = mid + 1
            } else if (charIndex < value.startIndex) {
                high = mid - 1
            } else {
                return mid
            }
        }

        return -(low + 1)
    }

    /**
     * The plain extent of `[charStart, charEnd)`, summed across whichever runs it spans - a
     * commutative sum, so this same forward-scanning function is correct whether the caller is
     * building up a range left-to-right or right-to-left. Does not look at blocks at all; callers
     * that care whether a block is in range use [findBlockForward]/[findBlockBackward] instead.
     */
    fun measureChars(charStart: Int, charEnd: Int): Float {
        var startIndex = charStart
        var extent = 0.0f

        if (charEnd > startIndex) {
            var runIndex = binarySearch(startIndex)

            do {
                val textRun = this[runIndex]
                val segmentEnd = min(charEnd, textRun.endIndex)
                extent += textRun.getRangeDistance(startIndex, segmentEnd)

                startIndex = segmentEnd
                runIndex++
            } while (startIndex < charEnd)
        }

        return extent
    }

    /**
     * The block with the smallest [ReplacementRun.startIndex] that is `>= charStart` and
     * `< charEnd` - the leftmost block in the range, which is what [BreakResolver]'s forward
     * search wants. `null` if there is none - allocation-free either way, since [blocks] is
     * small and cached and this only ever binary-searches it, never the full run list.
     */
    fun findBlockForward(charStart: Int, charEnd: Int): ReplacementRun? {
        if (blocks.isEmpty()) {
            return null
        }

        val index = blocks.lowerBound(charStart)
        if (index >= blocks.size) {
            return null
        }

        val block = blocks[index]
        return if (block.startIndex < charEnd) block else null
    }

    /**
     * The block nearest `charEnd` among those with [ReplacementRun.startIndex] `>= charStart`
     * and `< charEnd` - the rightmost block in the range, which is what [BreakResolver]'s
     * backward search wants (see the "nearest the boundary" reasoning that used to live on
     * `measureCharsFromEnd`, which still applies). `null` if there is none.
     */
    fun findBlockBackward(charStart: Int, charEnd: Int): ReplacementRun? {
        if (blocks.isEmpty()) {
            return null
        }

        val index = blocks.lowerBound(charEnd) - 1
        if (index < 0) {
            return null
        }

        val block = blocks[index]
        return if (block.startIndex >= charStart) block else null
    }

    /**
     * The index of the first block whose [ReplacementRun.startIndex] is `>= charIndex`, or
     * [blocks]' size if there is none - a standard binary lower bound over the small, cached,
     * position-sorted block list.
     */
    private fun List<ReplacementRun>.lowerBound(charIndex: Int): Int {
        var low = 0
        var high = size

        while (low < high) {
            val mid = (low + high) ushr 1

            if (this[mid].startIndex < charIndex) {
                low = mid + 1
            } else {
                high = mid
            }
        }

        return low
    }
}
