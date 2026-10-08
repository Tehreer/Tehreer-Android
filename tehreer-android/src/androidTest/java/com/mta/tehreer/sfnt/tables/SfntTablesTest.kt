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

package com.mta.tehreer.sfnt.tables

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

import android.graphics.Rect

import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.sfnt.SfntTag
import com.mta.tehreer.util.TypefaceStore

import org.junit.Test

class SfntTablesTest {
    @Test
    fun testHeadTable() {
        val typeface = TypefaceStore.nafeesWeb
        val head = requireNotNull(HeadTable.from(typeface))

        assertEquals(0x00010000, head.version())
        assertEquals(0x5F0F3CF5L, head.magicNumber())
        assertEquals(typeface.unitsPerEm, head.unitsPerEm())

        val box = typeface.boundingBox
        assertEquals(box.left, head.xMin().toInt())
        assertEquals(box.top, head.yMin().toInt())
        assertEquals(box.right, head.xMax().toInt())
        assertEquals(box.bottom, head.yMax().toInt())
    }

    @Test
    fun testHheaTable() {
        val typeface = TypefaceStore.nafeesWeb
        val hhea = requireNotNull(HheaTable.from(typeface))

        assertEquals(0x00010000, hhea.version())
        assertTrue(hhea.ascender() > 0)
        assertTrue(hhea.descender() < 0)
        assertTrue(hhea.numberOfHMetrics() > 0)
        assertTrue(hhea.numberOfHMetrics() <= typeface.glyphCount)
    }

    @Test
    fun testMaxpTable() {
        val typeface = TypefaceStore.nafeesWeb
        val maxp = requireNotNull(MaxpTable.from(typeface))

        assertEquals(typeface.glyphCount, maxp.numGlyphs())
        assertTrue(maxp.maxPoints() > 0)
        assertTrue(maxp.maxContours() > 0)
    }

    @Test
    fun testOS2Table() {
        val typeface = TypefaceStore.nafeesWeb
        val os2 = requireNotNull(OS2Table.from(typeface))

        assertTrue(os2.usWeightClass() >= 1 && os2.usWeightClass() <= 1000)
        assertEquals(10, os2.panose().size)

        // The fields after the panose are the ones that were read from a padded struct before.
        assertEquals(typeface.strikeoutThickness, os2.yStrikeoutSize().toInt())
        assertEquals(typeface.strikeoutPosition, os2.yStrikeoutPosition().toInt())
        assertTrue(os2.usLastCharIndex() >= os2.usFirstCharIndex())
        assertTrue(os2.usWinAscent() > 0)
    }

    @Test
    fun testPostTable() {
        val typeface = TypefaceStore.nafeesWeb
        val post = requireNotNull(PostTable.from(typeface))

        assertTrue(post.underlinePosition() < 0)
        assertTrue(post.underlineThickness() > 0)
        assertEquals(0, post.isFixedPitch())
        assertEquals(typeface.glyphCount, post.numberOfGlyphs())
        assertEquals(".notdef", post.glyphNameAt(0))
    }

    @Test
    fun testNameTable() {
        val typeface = TypefaceStore.nafeesWeb
        val name = requireNotNull(NameTable.from(typeface))


        // The records are those of the macintosh platform followed by those of the windows one.
        assertEquals(22, name.recordCount())

        val mac = name.recordAt(0)
        assertEquals(1, mac.nameId)
        assertEquals(1, mac.platformId)
        assertEquals("Nafees Web Naskh", mac.string())
        assertEquals("en", mac.locale().getLanguage())

        val windows = name.recordAt(11)
        assertEquals(1, windows.nameId)
        assertEquals(3, windows.platformId)
        assertEquals(1033, windows.languageId)
        assertEquals("Nafees Web Naskh", windows.string())
        assertEquals("en_US", windows.locale().toString())

        var hasFamilyName = false
        for (i in 0 until name.recordCount()) {
            val record = name.recordAt(i)
            assertTrue(record.bytes!!.size > 0)

            // The typographic family name is the one that a typeface reports.
            if (record.nameId == 16 && typeface.familyName.equals(record.string())) {
                hasFamilyName = true
            }
        }

        assertTrue(hasFamilyName)

        // A record is a copy, so changing it does not affect the table.
        mac.bytes!![0] = 0
        assertEquals("Nafees Web Naskh", name.recordAt(0).string())
    }

    @Test
    fun testMissingTable() {
        val typeface = TypefaceStore.nafeesWeb
        val data = typeface.getTableData(SfntTag.make("head"))

        assertNotNull(data)
        assertEquals(54, data!!.size)
        assertNull(typeface.getTableData(SfntTag.make("ZZZZ")))
    }
}


