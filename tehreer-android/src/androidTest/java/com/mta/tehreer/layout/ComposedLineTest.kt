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

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.util.FontFileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComposedLineTest {
    private companion object {
        const val TEXT = "The quick brown fox jumps over the lazy dog and keeps running far away."
        const val DELTA = 0.01f
    }

    private fun typesetter(): Typesetter {
        val typeface = FontFileStore.sudo.typefaces[0]
        val spanned = SpannableStringBuilder(TEXT)
        spanned.setSpan(TypefaceSpan(typeface), 0, TEXT.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spanned.setSpan(TypeSizeSpan(18.0f), 0, TEXT.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        return Typesetter(spanned)
    }

    @Test
    fun testHeightIsSumOfMetrics() {
        val line = typesetter().createSimpleLine(0, TEXT.length)

        assertEquals(line.ascent + line.descent + line.leading, line.height, DELTA)
    }

    @Test
    fun testSimpleLineIsNotTruncated() {
        assertFalse(typesetter().createSimpleLine(0, TEXT.length).isTruncated)
    }

    @Test
    fun testSimpleLineWithLayoutWidth() {
        val line = typesetter().createSimpleLine(0, TEXT.length, 200.0f)

        assertEquals(0, line.startIndex)
        assertEquals(TEXT.length, line.endIndex)
    }

    @Test
    fun testTruncatedLineIsTruncated() {
        val line = typesetter().createTruncatedLine(
            0, TEXT.length, 100.0f, BreakMode.LINE, TruncationPlace.END
        )

        assertTrue(line.isTruncated)
        assertTrue(line.width <= 100.0f + 1.0f)
    }

    @Test
    fun testCharDistances() {
        val line = typesetter().createSimpleLine(0, TEXT.length)

        assertEquals(0.0f, line.getCharDistance(0), DELTA)
        assertEquals(line.width, line.getCharDistance(TEXT.length), 1.0f)
        assertTrue(line.getCharDistance(10) > line.getCharDistance(5))
    }

    @Test
    fun testCharIndexAtDistance() {
        val line = typesetter().createSimpleLine(0, TEXT.length)
        val distance = line.getCharDistance(10)

        assertEquals(10, line.getCharIndexAtDistance(distance))
        assertEquals(0, line.getCharIndexAtDistance(-10.0f))
        assertEquals(TEXT.length, line.getCharIndexAtDistance(line.width + 100.0f))
    }

    @Test
    fun testVisualEdges() {
        val line = typesetter().createSimpleLine(0, TEXT.length)

        val edges = line.getVisualEdges(4, 9)

        assertEquals(2, edges.size)
        assertEquals(line.getCharDistance(4), edges[0], DELTA)
        assertEquals(line.getCharDistance(9), edges[1], DELTA)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testVisualEdgesOutOfRange() {
        typesetter().createSimpleLine(0, 10).getVisualEdges(0, 20)
    }

    @Test
    fun testComputeInkBox() {
        val line = typesetter().createSimpleLine(0, TEXT.length)
        val renderer = Renderer()
        renderer.typeface = FontFileStore.sudo.typefaces[0]
        renderer.typeSize = 18.0f

        val box = line.computeInkBox(renderer)

        assertTrue(box.width() > 0.0f)
        assertTrue(box.height() > 0.0f)
    }
}
