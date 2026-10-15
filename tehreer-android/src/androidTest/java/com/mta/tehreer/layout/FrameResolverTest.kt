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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

import android.graphics.Path
import android.graphics.RectF
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.LeadingMarginSpan

import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.util.FontFileStore

import org.junit.Test


class FrameResolverTest {
    private companion object {
        const val TEXT = "The quick brown fox jumps over the lazy dog and keeps running far away.\n" +
            "Second paragraph is rather short.\n" +
            "The third paragraph is long enough to wrap around a couple of times."
        const val DELTA = 0.01f
    }

    private fun styled(text: String): SpannableStringBuilder {
        val typeface = FontFileStore.sudo.typefaces.get(0)
        val spanned = SpannableStringBuilder(text)
        spanned.setSpan(TypefaceSpan(typeface), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spanned.setSpan(TypeSizeSpan(18.0f), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        return spanned
    }

    private fun frameOf(spanned: Spanned, resolver: FrameResolver): ComposedFrame {
        resolver.typesetter = Typesetter(spanned)

        return resolver.createFrame(0, spanned.length)
    }

    private fun resolver(width: Float, height: Float): FrameResolver {
        val resolver = FrameResolver()
        resolver.frameWidth = width
        resolver.frameHeight = height

        return resolver
    }

    @Test
    fun testLinesCoverTheRange() {
        val frame = frameOf(styled(TEXT), resolver(200.0f, 1000.0f))
        val lines: List<ComposedLine> = frame.lines

        assertEquals(0, frame.startIndex)
        assertEquals(TEXT.length, frame.endIndex)
        assertEquals(200.0f, frame.width, DELTA)
        assertTrue(lines.size > 3)

        var charIndex = 0
        var top: Float = 0.0f
        for (line in lines) {
            assertEquals(charIndex, line.startIndex)
            assertEquals(top, line.originY - line.ascent, DELTA)

            charIndex = line.endIndex
            top += line.height
        }

        assertEquals(TEXT.length, charIndex)
        assertEquals(1000.0f, frame.height, DELTA)
    }

    @Test
    fun testLeadingMargins() {
        val spanned = styled(TEXT)
        spanned.setSpan(LeadingMarginSpan.Standard(30, 12), 0, TEXT.length,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val lines: List<ComposedLine> = frameOf(spanned, resolver(200.0f, 1000.0f)).lines

        // The first line of each paragraph is indented more than the others.
        for (line in lines) {
            val isParagraphStart = (line.startIndex == 0 || TEXT[line.startIndex - 1] == '\n')
            assertEquals(if (isParagraphStart) 30.0f else 12.0f, line.originX, DELTA)
        }
    }

    @Test
    fun testAlignmentSpanWinsOverTheResolver() {
        val plain = styled(TEXT)
        val centered = styled(TEXT)
        centered.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, TEXT.length,
                         Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val trailing = resolver(200.0f, 1000.0f)
        trailing.textAlignment = TextAlignment.TRAILING

        val plainLine = frameOf(plain, trailing).lines.get(0)
        val centeredLine = frameOf(centered, trailing).lines.get(0)

        val room: Float = 200.0f - (plainLine.width - plainLine.trailingWhitespaceExtent)
        assertEquals(room, plainLine.originX, DELTA)
        assertEquals(room / 2.0f, centeredLine.originX, DELTA)
    }

    @Test
    fun testMaxLinesAndTruncation() {
        val resolver = resolver(200.0f, 1000.0f)
        resolver.maxLines = 2

        var frame = frameOf(styled(TEXT), resolver)
        assertEquals(2, frame.lines.size)
        assertTrue(frame.endIndex < TEXT.length)

        // A truncated frame still covers all of the text that it was asked for.
        resolver.truncationPlace = TruncationPlace.END
        resolver.isTruncationEnabled = true
        frame = frameOf(styled(TEXT), resolver)
        assertEquals(2, frame.lines.size)
        assertEquals(0, frame.startIndex)
        assertEquals(TEXT.length, frame.endIndex)
    }

    @Test
    fun testFittingChangesTheSizeOfTheFrame() {
        val resolver = resolver(400.0f, 1000.0f)
        resolver.fitsHorizontally = true
        resolver.fitsVertically = true

        val frame = frameOf(styled(TEXT), resolver)
        val last = frame.lines.get(frame.lines.size - 1)

        assertTrue(frame.width <= 400.0f)
        assertEquals(last.originY + last.descent + last.leading, frame.height, DELTA)
    }

    @Test
    fun testLineLookups() {
        val frame = frameOf(styled(TEXT), resolver(200.0f, 1000.0f))
        val lines: List<ComposedLine> = frame.lines

        for (i in 0 until lines.size) {
            val line = lines.get(i)

            assertEquals(i, frame.getLineIndexForChar(line.startIndex))
            assertEquals(i, frame.getLineIndexForChar(line.endIndex - 1))
            assertEquals(i, frame.getLineIndexForPosition(0.0f, line.originY))
        }

        // The end of the frame and the points below it belong to the last line.
        assertEquals(lines.size - 1, frame.getLineIndexForChar(TEXT.length))
        assertEquals(lines.size - 1, frame.getLineIndexForPosition(0.0f, 5000.0f))
    }

    @Test
    fun testSelectionPath() {
        val frame = frameOf(styled(TEXT), resolver(200.0f, 1000.0f))
        val lines: List<ComposedLine> = frame.lines

        val first = lines.get(0)
        val third = lines.get(2)

        val path = frame.generateSelectionPath(first.startIndex + 2, third.endIndex - 2)
        val bounds = RectF()
        path.computeBounds(bounds, true)

        assertEquals(first.originY - first.ascent, bounds.top, DELTA)
        assertEquals(third.originY + third.descent + third.leading, bounds.bottom, DELTA)
        assertTrue(bounds.left >= 0.0f)
        assertTrue(bounds.right <= frame.width)

        assertTrue(frame.generateSelectionPath(4, 4).isEmpty())
    }

    @Test
    fun testSubrangeFrame() {
        val spanned = styled(TEXT)
        val resolver = resolver(200.0f, 1000.0f)
        resolver.typesetter = Typesetter(spanned)

        val frame = resolver.createFrame(10, 60)
        assertEquals(10, frame.startIndex)
        assertEquals(60, frame.endIndex)
        assertEquals(10, frame.lines.get(0).startIndex)
        assertFalse(frame.lines.isEmpty())
    }

    @Test
    fun testPropertiesAreReadFromCore() {
        val resolver = FrameResolver()

        assertFalse(resolver.fitsHorizontally)
        assertFalse(resolver.fitsVertically)
        assertEquals(TextAlignment.LEADING, resolver.textAlignment)
        assertEquals(VerticalAlignment.TOP, resolver.verticalAlignment)
        assertEquals(BreakMode.LINE, resolver.truncationMode)
        assertEquals(TruncationPlace.END, resolver.truncationPlace)
        assertFalse(resolver.isTruncationEnabled)
        assertFalse(resolver.isJustificationEnabled)
        assertEquals(1.0f, resolver.justificationLevel, 0.0f)
        assertEquals(0, resolver.maxLines)
        assertEquals(0.0f, resolver.extraLineSpacing, 0.0f)
        assertEquals(1.0f, resolver.lineHeightMultiplier, 0.0f)

        resolver.frameWidth = 100.0f
        resolver.frameHeight = 50.0f
        resolver.fitsHorizontally = true
        resolver.fitsVertically = true
        resolver.textAlignment = TextAlignment.CENTER
        resolver.verticalAlignment = VerticalAlignment.BOTTOM
        resolver.truncationMode = BreakMode.CHARACTER
        resolver.truncationPlace = TruncationPlace.MIDDLE
        resolver.isTruncationEnabled = true
        resolver.isJustificationEnabled = true
        resolver.justificationLevel = 0.5f
        resolver.maxLines = 3
        resolver.extraLineSpacing = 2.0f
        resolver.lineHeightMultiplier = 1.5f

        assertEquals(100.0f, resolver.frameWidth, 0.0f)
        assertEquals(50.0f, resolver.frameHeight, 0.0f)
        assertTrue(resolver.fitsHorizontally)
        assertTrue(resolver.fitsVertically)
        assertEquals(TextAlignment.CENTER, resolver.textAlignment)
        assertEquals(VerticalAlignment.BOTTOM, resolver.verticalAlignment)
        assertEquals(BreakMode.CHARACTER, resolver.truncationMode)
        assertEquals(TruncationPlace.MIDDLE, resolver.truncationPlace)
        assertTrue(resolver.isTruncationEnabled)
        assertTrue(resolver.isJustificationEnabled)
        assertEquals(0.5f, resolver.justificationLevel, 0.0f)
        assertEquals(3, resolver.maxLines)
        assertEquals(2.0f, resolver.extraLineSpacing, 0.0f)
        assertEquals(1.5f, resolver.lineHeightMultiplier, 0.0f)

        for (alignment in TextAlignment.entries) {
            resolver.textAlignment = alignment
            assertEquals(alignment, resolver.textAlignment)
        }

        for (place in TruncationPlace.entries) {
            resolver.truncationPlace = place
            assertEquals(place, resolver.truncationPlace)
        }

        resolver.isTruncationEnabled = false
        assertFalse(resolver.isTruncationEnabled)
    }
}
