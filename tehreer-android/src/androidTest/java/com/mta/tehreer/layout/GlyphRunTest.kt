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

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ScaleXSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.layout.style.DecorationColorSpan
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.sfnt.WritingDirection
import com.mta.tehreer.util.FontFileStore
import com.mta.tehreer.util.TypefaceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GlyphRunTest {
    private companion object {
        const val TEXT = "plain red back under strike deco scaled"
        const val DELTA = 0.01f
    }

    private fun line(): ComposedLine {
        val typeface = FontFileStore.sudo.typefaces[0]
        val spanned = SpannableStringBuilder(TEXT)
        spanned.setSpan(TypefaceSpan(typeface), 0, TEXT.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spanned.setSpan(TypeSizeSpan(18.0f), 0, TEXT.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val red = TEXT.indexOf("red")
        spanned.setSpan(ForegroundColorSpan(Color.RED), red, red + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val back = TEXT.indexOf("back")
        spanned.setSpan(BackgroundColorSpan(Color.YELLOW), back, back + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val under = TEXT.indexOf("under")
        spanned.setSpan(UnderlineSpan(), under, under + 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val strike = TEXT.indexOf("strike")
        spanned.setSpan(StrikethroughSpan(), strike, strike + 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val deco = TEXT.indexOf("deco")
        spanned.setSpan(UnderlineSpan(), deco, deco + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanned.setSpan(DecorationColorSpan(Color.BLUE), deco, deco + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val scaled = TEXT.indexOf("scaled")
        spanned.setSpan(ScaleXSpan(2.0f), scaled, scaled + 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return Typesetter(spanned).createSimpleLine(0, TEXT.length)
    }

    private fun runAt(line: ComposedLine, word: String): GlyphRun {
        val index = TEXT.indexOf(word)

        return line.runs.first { index >= it.startIndex && index < it.endIndex }
    }

    @Test
    fun testForegroundColor() {
        val line = line()

        assertEquals(Color.RED, runAt(line, "red").foregroundColor)
        assertNull(runAt(line, "plain").foregroundColor)
    }

    @Test
    fun testBackgroundColor() {
        val line = line()

        assertEquals(Color.YELLOW, runAt(line, "back").backgroundColor)
        assertNull(runAt(line, "plain").backgroundColor)
    }

    @Test
    fun testDecorations() {
        val line = line()

        assertTrue(runAt(line, "under").hasUnderline)
        assertFalse(runAt(line, "under").hasStrikethrough)
        assertTrue(runAt(line, "strike").hasStrikethrough)
        assertFalse(runAt(line, "plain").hasUnderline)
        assertFalse(runAt(line, "plain").hasStrikethrough)
    }

    @Test
    fun testDecorationColor() {
        val line = line()

        assertEquals(Color.BLUE, runAt(line, "deco").decorationColor)
        assertNull(runAt(line, "under").decorationColor)
    }

    @Test
    fun testScales() {
        val line = line()

        assertEquals(2.0f, runAt(line, "scaled").scaleX, DELTA)
        assertEquals(1.0f, runAt(line, "scaled").scaleY, DELTA)
        assertEquals(1.0f, runAt(line, "plain").scaleX, DELTA)
    }

    @Test
    fun testLeftToRightRunIsNotBackward() {
        val run = line().runs[0]

        assertEquals(WritingDirection.LEFT_TO_RIGHT, run.writingDirection)
        assertFalse(run.isBackward)
    }

    @Test
    fun testRightToLeftRunIsNotBackward() {
        val text = "تحریر"
        val spanned = SpannableStringBuilder(text)
        spanned.setSpan(TypefaceSpan(TypefaceStore.nafeesWeb), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spanned.setSpan(TypeSizeSpan(18.0f), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val run = Typesetter(spanned).createSimpleLine(0, text.length).runs[0]

        // The glyphs of the run are written the way its characters go, so it is not shaped backward.
        assertEquals(WritingDirection.RIGHT_TO_LEFT, run.writingDirection)
        assertFalse(run.isBackward)
    }

    @Test
    fun testHeightIsSumOfMetrics() {
        val run = line().runs[0]

        assertEquals(run.ascent + run.descent + run.leading, run.height, DELTA)
    }

    @Test
    fun testClusterAndDistanceNames() {
        val line = line()
        val run = line.runs[0]

        assertEquals(run.startIndex, run.getClusterStart(run.startIndex))
        assertEquals(run.startIndex + 1, run.getClusterEnd(run.startIndex))
        assertEquals(0.0f, run.getCharDistance(run.startIndex), DELTA)
        assertEquals(run.startIndex, run.getCharIndexAtDistance(-5.0f))
    }

    @Test
    fun testComputeInkBox() {
        val run = line().runs[0]
        val renderer = Renderer()
        renderer.typeface = FontFileStore.sudo.typefaces[0]
        renderer.typeSize = 18.0f

        val box = run.computeInkBox(renderer)

        assertTrue(box.width() > 0.0f)
        assertTrue(box.height() > 0.0f)
    }
}
