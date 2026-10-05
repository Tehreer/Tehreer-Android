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

package com.mta.tehreer.widget

import android.graphics.Rect

private const val PAGE_HEIGHT = 512
private const val INITIAL_PAGE_CAPACITY = 8

/** A horizontal band of the text, and the indexes of the lines whose boxes touch it. */
internal class LinePage(
    val band: Int,
    private var indexes: IntArray = IntArray(INITIAL_PAGE_CAPACITY)
) {
    var size = 0
        private set

    operator fun get(position: Int) = indexes[position]

    fun add(lineIndex: Int) {
        if (size == indexes.size) {
            indexes = indexes.copyOf(size * 2)
        }

        indexes[size++] = lineIndex
    }

    fun copy() = LinePage(band, indexes.copyOf()).also { it.size = size }
}

/** The boxes of the lines of a frame, with the pages that tell which lines are in which band. */
internal class LineBoxes {
    val boxes: List<Rect>
        field = ArrayList<Rect>()

    val size
        get() = boxes.size

    internal val pages = ArrayList<LinePage>()

    fun add(box: Rect) {
        val lineIndex = boxes.size
        boxes.add(box)

        for (band in bandOf(box.top)..bandOf(box.bottom)) {
            val pageIndex = firstPageIndex(band)

            if (pageIndex < pages.size && pages[pageIndex].band == band) {
                pages[pageIndex].add(lineIndex)
            } else {
                pages.add(pageIndex, LinePage(band).also { it.add(lineIndex) })
            }
        }
    }

    fun copy() = LineBoxes().also {
        it.boxes.addAll(boxes)
        pages.mapTo(it.pages) { page -> page.copy() }
    }

    /**
     * Calls [action] once for each line that intersects [rect], without allocating.
     *
     * The pages of the top and the bottom of the rect are found by binary search. A line that is
     * in several pages is reported by the first page that the search range reaches.
     */
    inline fun forEachLineIndex(rect: Rect, action: (Int) -> Unit) {
        val topBand = bandOf(rect.top)
        val bottomBand = bandOf(rect.bottom)

        var pageIndex = firstPageIndex(topBand)

        while (pageIndex < pages.size && pages[pageIndex].band <= bottomBand) {
            val page = pages[pageIndex]

            for (position in 0 until page.size) {
                val lineIndex = page[position]
                val box = boxes[lineIndex]

                if (Rect.intersects(box, rect) && maxOf(bandOf(box.top), topBand) == page.band) {
                    action(lineIndex)
                }
            }

            pageIndex++
        }
    }

    /** Returns the first line that is below [y] according to [isBelow], looking only at the pages that reach [y] or lie under it. */
    inline fun firstLineIndex(y: Int, isBelow: (Int) -> Boolean): Int {
        for (pageIndex in firstPageIndex(bandOf(y)) until pages.size) {
            val page = pages[pageIndex]

            for (position in 0 until page.size) {
                if (isBelow(page[position])) {
                    return page[position]
                }
            }
        }

        return -1
    }

    internal fun firstPageIndex(band: Int): Int {
        var low = 0
        var high = pages.size

        while (low < high) {
            val mid = (low + high) ushr 1

            if (pages[mid].band >= band) {
                high = mid
            } else {
                low = mid + 1
            }
        }

        return low
    }

    internal fun bandOf(y: Int) = Math.floorDiv(y, PAGE_HEIGHT)
}
