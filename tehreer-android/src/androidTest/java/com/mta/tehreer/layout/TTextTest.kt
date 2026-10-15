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
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LeadingMarginSpan
import android.text.style.ScaleXSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan
import com.mta.tehreer.layout.style.DecorationColorSpan
import com.mta.tehreer.layout.style.OpenTypeFeaturesSpan
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.sfnt.OpenTypeFeature
import com.mta.tehreer.sfnt.SfntTag
import com.mta.tehreer.util.FontFileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TTextTest {
    private companion object {
        const val STRING = "Hello wide world"
        const val DELTA = 0.01f
    }

    private val typeface = FontFileStore.sudo.typefaces[0]

    private fun spanned(): SpannableStringBuilder {
        val spanned = SpannableStringBuilder(STRING)
        spanned.setSpan(TypefaceSpan(typeface), 0, STRING.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spanned.setSpan(TypeSizeSpan(20.0f), 0, STRING.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spanned.setSpan(ForegroundColorSpan(Color.RED), 6, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanned.setSpan(UnderlineSpan(), 6, 10, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return spanned
    }

    private fun spansOf(run: SpanRun): List<Class<*>> = run.spans.map { it.javaClass }

    @Test
    fun testStringText() {
        val subject = TText(STRING)

        assertEquals(STRING.length, subject.length)
        assertEquals(STRING, subject.string)
        assertEquals(STRING, subject.toString())
        assertFalse(subject.isMutable)
    }

    @Test
    fun testStringTextHasNoSpans() {
        val run = TText(STRING).getSpanRun(0)!!

        assertTrue(run.spans.isEmpty())
        assertEquals(STRING.length, run.length)
    }

    @Test
    fun testEmptyText() {
        val subject = TText("")

        assertEquals(0, subject.length)
        assertEquals("", subject.string)
        assertNull(subject.getSpanRun(0))
    }

    @Test
    fun testSubstring() {
        val subject = TText(STRING)

        assertEquals("wide", subject.getString(6, 10))
        assertEquals("", subject.getString(3, 3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSubstringOutOfRange() {
        TText(STRING).getString(0, STRING.length + 1)
    }

    @Test
    fun testSpannedText() {
        val subject = TText(spanned())

        assertEquals(STRING, subject.string)
        assertFalse(subject.isMutable)
    }

    @Test
    fun testSpanRunsOfSpannedText() {
        val subject = TText(spanned())

        val first = subject.getSpanRun(0)!!
        assertEquals(6, first.length)
        assertTrue(spansOf(first).contains(TypefaceSpan::class.java))
        assertTrue(spansOf(first).contains(TypeSizeSpan::class.java))
        assertFalse(spansOf(first).contains(ForegroundColorSpan::class.java))

        val second = subject.getSpanRun(6)!!
        assertEquals(4, second.length)
        assertTrue(spansOf(second).contains(ForegroundColorSpan::class.java))
        assertTrue(spansOf(second).contains(UnderlineSpan::class.java))

        val color = second.spans.filterIsInstance<ForegroundColorSpan>().single()
        assertEquals(Color.RED, color.foregroundColor)

        val size = second.spans.filterIsInstance<TypeSizeSpan>().single()
        assertEquals(20.0f, size.size, DELTA)
        assertSame(typeface, second.spans.filterIsInstance<TypefaceSpan>().single().typeface)

        assertEquals(STRING.length - 10, subject.getSpanRun(10)!!.length)
        assertNull(subject.getSpanRun(STRING.length))
        assertNull(subject.getSpanRun(-1))
    }

    @Test
    fun testToSpannedRoundTrip() {
        val subject = TText(spanned())

        val spanned = subject.toSpanned()

        assertEquals(STRING, spanned.toString())
        val colors = spanned.getSpans(0, spanned.length, ForegroundColorSpan::class.java)
        assertEquals(1, colors.size)
        assertEquals(6, spanned.getSpanStart(colors[0]))
        assertEquals(10, spanned.getSpanEnd(colors[0]))
        assertEquals(Color.RED, colors[0].foregroundColor)

        val underlines = spanned.getSpans(0, spanned.length, UnderlineSpan::class.java)
        assertEquals(1, underlines.size)
        assertEquals(6, spanned.getSpanStart(underlines[0]))
        assertEquals(10, spanned.getSpanEnd(underlines[0]))

        // The spans that cover the whole text are not split where other spans start.
        val sizes = spanned.getSpans(0, spanned.length, TypeSizeSpan::class.java)
        assertEquals(1, sizes.size)
        assertEquals(0, spanned.getSpanStart(sizes[0]))
        assertEquals(STRING.length, spanned.getSpanEnd(sizes[0]))
    }

    @Test
    fun testToSpannedOfAllKnownAttributes() {
        val spanned = spanned()
        spanned.setSpan(BackgroundColorSpan(Color.YELLOW), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanned.setSpan(StrikethroughSpan(), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanned.setSpan(DecorationColorSpan(Color.BLUE), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanned.setSpan(ScaleXSpan(1.5f), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val features = setOf(OpenTypeFeature.of(SfntTag.make("liga"), 0))
        spanned.setSpan(OpenTypeFeaturesSpan(features), 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val result = TText(spanned).toSpanned()

        assertEquals(Color.YELLOW, result.getSpans(0, 5, BackgroundColorSpan::class.java).single().backgroundColor)
        assertEquals(1, result.getSpans(0, 5, StrikethroughSpan::class.java).size)
        assertEquals(Color.BLUE, result.getSpans(0, 5, DecorationColorSpan::class.java).single().color)
        assertEquals(1.5f, result.getSpans(0, 5, ScaleXSpan::class.java).single().scaleX, DELTA)
        assertEquals(features, result.getSpans(0, 5, OpenTypeFeaturesSpan::class.java).single().features)
    }

    @Test
    fun testParagraphAttributesAreMadeOnceForParagraph() {
        val spanned = spanned()
        spanned.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, STRING.length, Spanned.SPAN_PARAGRAPH)
        spanned.setSpan(LeadingMarginSpan.Standard(12, 4), 0, STRING.length, Spanned.SPAN_PARAGRAPH)

        val result = TText(spanned).toSpanned()

        val alignments = result.getSpans(0, result.length, AlignmentSpan::class.java)
        assertEquals(1, alignments.size)
        assertEquals(Layout.Alignment.ALIGN_CENTER, alignments[0].alignment)

        val margins = result.getSpans(0, result.length, LeadingMarginSpan::class.java)
        assertEquals(1, margins.size)
        assertEquals(12, margins[0].getLeadingMargin(true))
        assertEquals(4, margins[0].getLeadingMargin(false))
    }

    @Test
    fun testCopyIsImmutableAndEqual() {
        val subject = TText(spanned())

        val copy = subject.copy()

        assertNotSame(subject, copy)
        assertFalse(copy.isMutable)
        assertEquals(subject.string, copy.string)
        assertEquals(subject.getSpanRun(6)!!.length, copy.getSpanRun(6)!!.length)
        assertSame(typeface, copy.getSpanRun(0)!!.spans.filterIsInstance<TypefaceSpan>().single().typeface)
    }

    @Test
    fun testMutableCopyIsIndependent() {
        val subject = TText(spanned())

        val copy = subject.mutableCopy()
        copy.append("!")

        assertTrue(copy.isMutable)
        assertEquals(STRING + "!", copy.string)
        assertEquals(STRING, subject.string)
    }

    @Test
    fun testTypesetterFromText() {
        val text = TText(spanned())

        val typesetter = Typesetter(text)
        val line = typesetter.createSimpleLine(0, STRING.length)

        assertEquals(STRING.length, line.endIndex)
        assertEquals(STRING, typesetter.spanned.toString())
        assertSame(typeface, line.runs[0].typeface)
    }

    @Test
    fun testTypesetterFromTextKeepsTextAsItWas() {
        val text = TText(spanned()).mutableCopy()
        val typesetter = Typesetter(text)

        text.append(" and more")
        val line = typesetter.createSimpleLine(0, STRING.length)

        assertEquals(STRING.length, line.endIndex)
        assertEquals(STRING.length, typesetter.createSimpleLine(0, STRING.length).endIndex)
        assertNotNull(line.runs.firstOrNull())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testTypesetterFromEmptyTextFails() {
        Typesetter(TText(""))
    }
}
