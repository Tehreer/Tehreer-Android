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
package com.mta.tehreer.graphics

import android.graphics.Bitmap
import android.graphics.Color
import com.mta.tehreer.util.FontFileStore
import com.mta.tehreer.util.TypefaceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GlyphImageTest {
    private lateinit var renderer: Renderer
    private lateinit var typeface: Typeface

    @Before
    fun setUp() {
        typeface = TypefaceStore.nafeesWeb
        renderer = Renderer()
        renderer.typeface = typeface
        renderer.typeSize = 32.0f
    }

    private fun image(): GlyphImage {
        return renderer.getGlyphImage(typeface.getGlyphId('ت'.code))!!
    }

    @Test
    fun testAlphaImageProperties() {
        val subject = image()

        assertEquals(GlyphImageFormat.ALPHA, subject.format)
        assertTrue(subject.width > 0)
        assertTrue(subject.height > 0)
        assertTrue(subject.top > 0)
    }

    @Test
    fun testBitmapOfAlphaImage() {
        val subject = image()

        val bitmap = subject.bitmap

        assertEquals(Bitmap.Config.ALPHA_8, bitmap.config)
        assertEquals(subject.width, bitmap.width)
        assertEquals(subject.height, bitmap.height)
    }

    @Test
    fun testBitmapIsMadeOnce() {
        val subject = image()

        assertSame(subject.bitmap, subject.bitmap)
    }

    @Test
    fun testBitmapHasInk() {
        val bitmap = image().bitmap
        var isInked = false

        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                if (Color.alpha(bitmap.getPixel(x, y)) != 0) {
                    isInked = true
                }
            }
        }

        assertTrue(isInked)
    }

    @Test
    fun testImageOfCachedGlyphSharesBitmapWithDrawing() {
        val glyphId = typeface.getGlyphId('ت'.code)

        val first = renderer.getGlyphImage(glyphId)!!
        val second = renderer.getGlyphImage(glyphId)!!

        // The cache gives the same image of Core, so its bitmap is the same too.
        assertSame(first.bitmap, second.bitmap)
    }

    @Test
    fun testColorImage() {
        val file = FontFileStore.rocherColor
        val color = file.typefaces[0]
        val colorRenderer = Renderer()
        colorRenderer.typeface = color
        colorRenderer.typeSize = 48.0f

        var found: GlyphImage? = null
        var glyphId = 1
        while (found == null && glyphId < minOf(color.glyphCount, 200)) {
            val candidate = colorRenderer.getGlyphImage(glyphId++)
            if (candidate != null && candidate.format == GlyphImageFormat.ARGB) {
                found = candidate
            }
        }

        val image = found!!
        assertEquals(Bitmap.Config.ARGB_8888, image.bitmap.config)
        assertEquals(image.width, image.bitmap.width)
    }

    @Test
    fun testStrokeImage() {
        renderer.strokeRadius = 2.0f

        val stroke = renderer.getStrokeImage(typeface.getGlyphId('ت'.code))!!

        assertEquals(GlyphImageFormat.ALPHA, stroke.format)
        assertEquals(Bitmap.Config.ALPHA_8, stroke.bitmap.config)
    }
}
