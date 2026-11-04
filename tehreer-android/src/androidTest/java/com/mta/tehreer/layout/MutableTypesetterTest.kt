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
import android.graphics.Paint
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.RelativeSizeSpan
import android.text.style.ReplacementSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.util.FontFileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TMutableTextTest {
    private companion object {
        const val DELTA = 0.01f
    }

    private val typeface = FontFileStore.sudo.typefaces[0]

    private fun styled(string: String): TMutableText {
        val subject = TMutableText(string)
        subject.setSpan(TypefaceSpan(typeface), 0, subject.length)
        subject.setSpan(TypeSizeSpan(20.0f), 0, subject.length)

        return subject
    }

    @Test
    fun testEmptyText() {
        val subject = TMutableText()

        assertTrue(subject.isMutable)
        assertEquals(0, subject.length)
        assertEquals("", subject.string)
    }

    @Test
    fun testAppend() {
        val subject = TMutableText("Hello")

        subject.append(" world")

        assertEquals("Hello world", subject.string)
        assertEquals(11, subject.length)
    }

    @Test
    fun testInsert() {
        val subject = TMutableText("Hello world")

        subject.insert(5, " wide")
        subject.insert(subject.length, "!")

        assertEquals("Hello wide world!", subject.string)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInsertPastTheEnd() {
        TMutableText("abc").insert(4, "x")
    }

    @Test
    fun testDelete() {
        val subject = TMutableText("Hello wide world")

        subject.delete(5, 10)

        assertEquals("Hello world", subject.string)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDeleteOutOfRange() {
        TMutableText("abc").delete(1, 4)
    }

    @Test
    fun testReplace() {
        val subject = TMutableText("Hello wide world")

        subject.replace(6, 10, "narrow")

        assertEquals("Hello narrow world", subject.string)
    }

    @Test
    fun testSetString() {
        val subject = styled("Hello")

        subject.setString("Bye")

        assertEquals("Bye", subject.string)
        assertEquals(3, subject.length)
    }

    @Test
    fun testBatchEditing() {
        val subject = TMutableText("a")

        subject.beginEditing()
        subject.append("b")
        subject.append("c")
        subject.insert(0, "_")
        subject.endEditing()

        assertEquals("_abc", subject.string)
    }

    @Test
    fun testSetSpan() {
        val subject = styled("Hello wide world")

        subject.setSpan(ForegroundColorSpan(Color.GREEN), 6, 10)
        subject.setSpan(UnderlineSpan(), 6, 10)
        subject.setSpan(StrikethroughSpan(), 0, 5)

        val run = subject.getSpanRun(6)!!
        assertEquals(4, run.length)
        assertEquals(Color.GREEN, run.spans.filterIsInstance<ForegroundColorSpan>().single().foregroundColor)
        assertEquals(1, run.spans.filterIsInstance<UnderlineSpan>().size)

        val first = subject.getSpanRun(0)!!
        assertEquals(5, first.length)
        assertEquals(1, first.spans.filterIsInstance<StrikethroughSpan>().size)
        assertTrue(first.spans.filterIsInstance<ForegroundColorSpan>().isEmpty())
    }

    @Test
    fun testSetSpanReplacesTheAttribute() {
        val subject = styled("Hello")

        subject.setSpan(ForegroundColorSpan(Color.RED), 0, 5)
        subject.setSpan(ForegroundColorSpan(Color.BLUE), 2, 4)

        assertEquals(2, subject.getSpanRun(0)!!.length)
        assertEquals(Color.BLUE, subject.getSpanRun(2)!!.spans.filterIsInstance<ForegroundColorSpan>().single().foregroundColor)
        assertEquals(Color.RED, subject.getSpanRun(4)!!.spans.filterIsInstance<ForegroundColorSpan>().single().foregroundColor)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetSpanOutOfRange() {
        TMutableText("abc").setSpan(UnderlineSpan(), 0, 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetUnknownSpan() {
        TMutableText("abc").setSpan(Any(), 0, 3)
    }

    @Test
    fun testRemoveSpan() {
        val subject = styled("Hello wide world")
        subject.setSpan(UnderlineSpan(), 0, 16)

        subject.removeSpan(UnderlineSpan(), 6, 10)

        assertEquals(1, subject.getSpanRun(0)!!.spans.filterIsInstance<UnderlineSpan>().size)
        assertEquals(6, subject.getSpanRun(0)!!.length)
        assertTrue(subject.getSpanRun(6)!!.spans.filterIsInstance<UnderlineSpan>().isEmpty())
        assertEquals(1, subject.getSpanRun(10)!!.spans.filterIsInstance<UnderlineSpan>().size)
    }

    @Test
    fun testRelativeSizeIsResolvedAgainstCurrentSize() {
        val subject = styled("Hello wide world")

        subject.setSpan(RelativeSizeSpan(2.0f), 6, 10)

        assertEquals(20.0f, subject.getSpanRun(0)!!.spans.filterIsInstance<TypeSizeSpan>().single().size, DELTA)
        assertEquals(40.0f, subject.getSpanRun(6)!!.spans.filterIsInstance<TypeSizeSpan>().single().size, DELTA)
    }

    @Test
    fun testAlignmentAndMargins() {
        val subject = styled("Hello")

        subject.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE), 0, 5)
        subject.setSpan(LeadingMarginSpan.Standard(10, 3), 0, 5)

        val spans = subject.getSpanRun(0)!!.spans
        assertEquals(Layout.Alignment.ALIGN_OPPOSITE, spans.filterIsInstance<AlignmentSpan>().single().alignment)
        val margin = spans.filterIsInstance<LeadingMarginSpan>().single()
        assertEquals(10, margin.getLeadingMargin(true))
        assertEquals(3, margin.getLeadingMargin(false))
    }

    @Test
    fun testReplacementSpan() {
        val subject = styled("ab")
        val replacement = object : ReplacementSpan() {
            override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?) = 30
            override fun draw(canvas: android.graphics.Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {}
        }

        subject.setSpan(replacement, 1, 2)

        assertSame(replacement, subject.getSpanRun(1)!!.spans.filterIsInstance<ReplacementSpan>().single())
        assertTrue(subject.getSpanRun(0)!!.spans.filterIsInstance<ReplacementSpan>().isEmpty())

        val line = Typesetter(subject).createSimpleLine(0, 2)
        assertEquals(30.0f, line.runs.single { it.startIndex == 1 }.width, DELTA)
    }

    @Test
    fun testReplacementSurvivesEditsBeforeIt() {
        val subject = styled("ab")
        val replacement = object : ReplacementSpan() {
            override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?) = 30
            override fun draw(canvas: android.graphics.Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {}
        }
        subject.setSpan(replacement, 1, 2)

        subject.insert(0, "xyz")

        assertSame(replacement, subject.getSpanRun(4)!!.spans.filterIsInstance<ReplacementSpan>().single())
        assertEquals(30.0f, Typesetter(subject).createSimpleLine(0, 5).runs.single { it.startIndex == 4 }.width, DELTA)
    }

    @Test
    fun testTypesetterOfEditedText() {
        val subject = styled("Hello")
        subject.append(" world")
        subject.setSpan(TypefaceSpan(typeface), 5, 11)
        subject.setSpan(TypeSizeSpan(20.0f), 5, 11)

        val line = Typesetter(subject).createSimpleLine(0, 11)

        assertEquals(11, line.endIndex)
        assertTrue(line.width > 0.0f)

        val renderer = Renderer()
        renderer.typeface = typeface
        renderer.typeSize = 20.0f
        assertTrue(line.computeInkBox(renderer).width() > 0.0f)
    }

    @Test
    fun testSpannedConstructor() {
        val spanned = SpannableString("Hello")
        spanned.setSpan(TypefaceSpan(typeface), 0, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spanned.setSpan(TypeSizeSpan(20.0f), 0, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spanned.setSpan(ForegroundColorSpan(Color.RED), 1, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val subject = TMutableText(spanned)

        assertTrue(subject.isMutable)
        assertEquals("Hello", subject.string)
        assertEquals(2, subject.getSpanRun(1)!!.length)
        assertNull(subject.getSpanRun(0)!!.spans.firstOrNull { it is ForegroundColorSpan })
        assertFalse(subject.getSpanRun(1)!!.spans.isEmpty())
    }
}
