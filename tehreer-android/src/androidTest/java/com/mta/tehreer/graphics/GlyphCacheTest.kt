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

import com.mta.tehreer.util.TypefaceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GlyphCacheTest {
    @Test
    fun testCapacityOfNewCache() {
        val subject = GlyphCache(4096L)

        assertEquals(4096L, subject.capacity)
        assertEquals(0L, subject.size)
    }

    @Test
    fun testDefaultCache() {
        assertSame(GlyphCache.default, GlyphCache.default)
        assertEquals(8L * 1024L * 1024L, GlyphCache.default.capacity)
    }

    @Test
    fun testCapacityProperty() {
        val subject = GlyphCache(4096L)

        subject.capacity = 8192L

        assertEquals(8192L, subject.capacity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNegativeCapacityIsRejected() {
        GlyphCache(-1L)
    }

    @Test
    fun testSizeGrowsAndClearDropsEverything() {
        val subject = GlyphCache(1024L * 1024L)
        val renderer = Renderer()
        renderer.typeface = TypefaceStore.nafeesWeb
        renderer.typeSize = 32.0f
        renderer.glyphCache = subject

        val glyphId = renderer.typeface!!.getGlyphId('ت'.code)
        renderer.getGlyphImage(glyphId)
        val size = subject.size

        assertTrue(size > 0L)

        subject.clear()

        assertEquals(0L, subject.size)
    }

    @Test
    fun testShrinkingCapacityDropsGlyphs() {
        val subject = GlyphCache(1024L * 1024L)
        val renderer = Renderer()
        renderer.typeface = TypefaceStore.nafeesWeb
        renderer.typeSize = 32.0f
        renderer.glyphCache = subject

        renderer.getGlyphImage(renderer.typeface!!.getGlyphId('ت'.code))
        assertTrue(subject.size > 0L)

        subject.capacity = 0L

        assertEquals(0L, subject.size)
    }
}
