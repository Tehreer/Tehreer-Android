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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LineBoxesTest {
    private fun makeLineBoxes(lineCount: Int) = LineBoxes().also {
        for (index in 0 until lineCount) {
            val overflow = if (index % 7 == 0) 900 else 0
            it.add(Rect(0, index * 30, 320, index * 30 + 30 + overflow))
        }
    }

    private fun LineBoxes.lineIndexes(rect: Rect): List<Int> {
        val found = mutableListOf<Int>()
        forEachLineIndex(rect) { found.add(it) }

        return found.sorted()
    }

    @Test
    fun lineIndexesMatchAScanOfAllBoxes() {
        val lineBoxes = makeLineBoxes(2000)

        for (top in -100..61_000 step 733) {
            val rect = Rect(0, top, 320, top + 480)
            val expected = lineBoxes.boxes.indices.filter { Rect.intersects(lineBoxes.boxes[it], rect) }

            assertEquals("at $top", expected, lineBoxes.lineIndexes(rect))
        }
    }

    @Test
    fun noLineIsFoundWithoutBoxes() {
        assertTrue(LineBoxes().lineIndexes(Rect(0, 0, 320, 480)).isEmpty())
    }

    @Test
    fun firstLineBelowAYSkipsLinesThatEndAbove() {
        val lineBoxes = makeLineBoxes(2000)

        assertEquals(0, lineBoxes.firstLineIndex(100) { lineBoxes.boxes[it].bottom > 100 })
        assertEquals(70, lineBoxes.firstLineIndex(3015) { lineBoxes.boxes[it].bottom > 3015 })
        assertEquals(-1, lineBoxes.firstLineIndex(1_000_000) { true })
    }

    @Test
    fun aCopyIsNotAffectedByLaterBoxes() {
        val lineBoxes = makeLineBoxes(100)
        val copy = lineBoxes.copy()

        lineBoxes.add(Rect(0, 100_000, 320, 100_030))

        assertEquals(100, copy.size)
        assertEquals(emptyList<Int>(), copy.lineIndexes(Rect(0, 100_000, 320, 100_030)))
    }
}
