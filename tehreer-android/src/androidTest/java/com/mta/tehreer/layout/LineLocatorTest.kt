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

import android.graphics.RectF
import android.text.SpannableStringBuilder
import android.text.Spanned
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.util.FontFileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LineLocatorTest {
    private companion object {
        const val TEXT = "The quick brown fox jumps over the lazy dog and keeps running far away.\n" +
            "Second paragraph is rather short.\n" +
            "The third paragraph is long enough to wrap around a couple of times."
    }

    private fun frame(): ComposedFrame {
        val typeface = FontFileStore.sudo.typefaces.get(0)
        val spanned = SpannableStringBuilder(TEXT)
        spanned.setSpan(TypefaceSpan(typeface), 0, TEXT.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spanned.setSpan(TypeSizeSpan(18.0f), 0, TEXT.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val resolver = FrameResolver()
        resolver.typesetter = Typesetter(spanned)
        resolver.frameWidth = 200.0f
        resolver.frameHeight = 1000.0f

        return resolver.createFrame(0, spanned.length)
    }

    private fun LineLocator.find(bounds: RectF): List<Int> {
        var indexes = IntArray(2)
        var count = findLines(bounds, indexes)
        if (count > indexes.size) {
            indexes = IntArray(count)
            count = findLines(bounds, indexes)
        }

        return indexes.take(count)
    }

    @Test
    fun testLineBoxesCoverTheLines() {
        val frame = frame()
        val locator = LineLocator(frame, Renderer())

        assertEquals(frame.lines.size, locator.lineCount)

        for ((i, line) in frame.lines.withIndex()) {
            val box = locator.getLineBox(i)
            val top = line.originY - line.ascent

            assertTrue(box.top <= top)
            assertTrue(box.bottom >= top + line.height)
            assertTrue(box.left <= 0.0f && box.right >= frame.width)
        }
    }

    @Test
    fun testFindLinesInRect() {
        val frame = frame()
        val locator = LineLocator(frame, Renderer())
        val all = locator.find(RectF(
            Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
            Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY
        ))

        assertEquals((0 until frame.lines.size).toList(), all.sorted())

        // A buffer that is too small gets the first lines, and the total is told.
        val small = IntArray(2)
        val everything = RectF(
            Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY,
            Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY
        )
        assertEquals(frame.lines.size, locator.findLines(everything, small))
        assertEquals(all.take(2), small.toList())

        val second = frame.lines[1]
        val middle = second.originY - second.ascent + second.height * 0.5f
        val found = locator.find(RectF(0.0f, middle, 200.0f, middle + 1.0f))

        assertTrue(found.contains(1))
        assertTrue(found.size <= 3)

        assertEquals(0, locator.find(RectF(0.0f, 5000.0f, 200.0f, 5100.0f)).size)
    }

    @Test
    fun testFirstLineIndexBelow() {
        val frame = frame()
        val locator = LineLocator(frame, Renderer())
        val line = frame.lines[2]

        assertEquals(0, locator.getFirstLineIndexBelow(-10.0f))
        val index = locator.getFirstLineIndexBelow(line.originY - line.ascent + 1.0f)
        assertTrue(index == 1 || index == 2)
        assertEquals(-1, locator.getFirstLineIndexBelow(1.0e7f))
    }

    @Test(expected = IndexOutOfBoundsException::class)
    fun testBadLineIndex() {
        LineLocator(frame(), Renderer()).getLineBox(1000)
    }
}
