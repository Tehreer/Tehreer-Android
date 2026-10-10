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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.layout.style.DecorationColorSpan
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan
import com.mta.tehreer.util.FontFileStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawTest {
    private fun styled(text: String): SpannableStringBuilder {
        val typeface = FontFileStore.sudo.typefaces.get(0)
        val spanned = SpannableStringBuilder(text)
        spanned.setSpan(TypefaceSpan(typeface), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        spanned.setSpan(TypeSizeSpan(24.0f), 0, text.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        return spanned
    }

    private fun frameOf(spanned: Spanned): ComposedFrame {
        val resolver = FrameResolver()
        resolver.typesetter = Typesetter(spanned)
        resolver.frameBounds = RectF(0.0f, 0.0f, 300.0f, 100.0f)

        return resolver.createFrame(0, spanned.length)
    }

    private fun draw(frame: ComposedFrame, renderer: Renderer): Bitmap {
        val bitmap = Bitmap.createBitmap(300, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        frame.draw(renderer, Canvas(bitmap), 0.0f, 0.0f)

        return bitmap
    }

    private fun count(bitmap: Bitmap, color: Int): Int {
        var total = 0
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                if (bitmap.getPixel(x, y) == color) {
                    total++
                }
            }
        }

        return total
    }

    @Test
    fun testGlyphsAreDrawnInRendererColor() {
        val renderer = Renderer()
        renderer.fillColor = Color.RED

        val bitmap = draw(frameOf(styled("Hello")), renderer)

        assertTrue(count(bitmap, Color.RED) > 20)
    }

    @Test
    fun testForegroundColorSpanOverridesRendererColor() {
        val spanned = styled("Hello")
        spanned.setSpan(ForegroundColorSpan(Color.BLUE), 0, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val renderer = Renderer()
        renderer.fillColor = Color.RED

        val bitmap = draw(frameOf(spanned), renderer)

        assertTrue(count(bitmap, Color.BLUE) > 20)
        assertEquals(0, count(bitmap, Color.RED))
    }

    @Test
    fun testBackgroundColorIsFilled() {
        val spanned = styled("Hello")
        spanned.setSpan(BackgroundColorSpan(Color.GREEN), 0, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        val bitmap = draw(frameOf(spanned), Renderer())

        assertTrue(count(bitmap, Color.GREEN) > 200)
    }

    @Test
    fun testUnderlineUsesDecorationColor() {
        val plain = styled("Hello")
        val underlined = styled("Hello")
        underlined.setSpan(UnderlineSpan(), 0, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        underlined.setSpan(DecorationColorSpan(Color.MAGENTA), 0, 5, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        assertEquals(0, count(draw(frameOf(plain), Renderer()), Color.MAGENTA))
        assertTrue(count(draw(frameOf(underlined), Renderer()), Color.MAGENTA) > 20)
    }

    @Test
    fun testLineIsDrawnAtItsOrigin() {
        val frame = frameOf(styled("Hello"))
        val renderer = Renderer()
        renderer.fillColor = Color.BLACK

        val bitmap = Bitmap.createBitmap(300, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        frame.lines[0].draw(renderer, Canvas(bitmap), 0.0f, 0.0f)

        assertEquals(count(draw(frame, renderer), Color.BLACK), count(bitmap, Color.BLACK))
    }
}
