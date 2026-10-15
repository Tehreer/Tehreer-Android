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

import com.mta.tehreer.util.FontFileStore
import com.mta.tehreer.util.TypefaceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TypefaceTest {
    private val typeface = FontFileStore.sudo.typefaces[0]

    @Test
    fun testIsVariableFollowsVariationAxes() {
        // Sudo is a variable font, while NafeesWeb is not.
        assertTrue(typeface.isVariable)
        assertTrue(typeface.variationAxes != null)
        assertFalse(TypefaceStore.nafeesWeb.isVariable)
        assertTrue(TypefaceStore.nafeesWeb.variationAxes == null)
    }

    @Test
    fun testVariationInstanceIsVariable() {
        val axes = typeface.variationAxes!!
        val instance = typeface.getVariationInstance(FloatArray(axes.size) { axes[it].defaultValue() })!!

        assertTrue(instance.isVariable)
    }

    @Test
    fun testVariantGlyphIdIsZeroWithoutVariant() {
        // The font has no variation sequences, so the variant is the missing glyph.
        assertEquals(0, typeface.getVariantGlyphId('a'.code, 0xFE00))
        assertNotEquals(0, typeface.getGlyphId('a'.code))
    }

    @Test
    fun testStyleNameIsNotEmpty() {
        assertTrue(typeface.styleName.isNotEmpty())
    }
}
