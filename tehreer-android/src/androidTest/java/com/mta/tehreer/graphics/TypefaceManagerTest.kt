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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TypefaceManagerTest {
    private val typeface = FontFileStore.sudo.typefaces.get(0)

    @After
    fun tearDown() {
        runCatching { TypefaceManager.unregisterTypeface(typeface) }
    }

    @Test
    fun testTagLookup() {
        TypefaceManager.registerTypeface(typeface, 4001)

        assertSame(typeface, TypefaceManager.getTypeface(4001))
        assertEquals(4001, TypefaceManager.getTypefaceTag(typeface))
        assertNull(TypefaceManager.getTypeface(4002))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRegisteringTwiceFails() {
        TypefaceManager.registerTypeface(typeface, 4001)
        TypefaceManager.registerTypeface(typeface, 4002)
    }

    @Test
    fun testUnregister() {
        TypefaceManager.registerTypeface(typeface, 4001)
        TypefaceManager.unregisterTypeface(typeface)

        assertNull(TypefaceManager.getTypeface(4001))
        assertTrue(TypefaceManager.getAvailableTypefaces().none { it === typeface })
    }

    @Test
    fun testFamilyMatching() {
        TypefaceManager.registerTypeface(typeface)

        val family = TypefaceManager.getTypeFamily(typeface.familyName.uppercase())!!
        assertTrue(family.typefaces.contains(typeface))
        assertSame(
            typeface,
            TypefaceManager.getTypefaceByStyle(
                typeface.familyName, TypeWidth.NORMAL, TypeWeight.REGULAR, TypeSlope.PLAIN
            )
        )
        assertSame(typeface, TypefaceManager.getTypefaceByName(typeface.fullName))
    }
}
