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

package com.mta.tehreer.font

import com.mta.tehreer.graphics.TypefaceInfo.Companion.assertTypefaceEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

import android.graphics.Rect

import com.mta.tehreer.graphics.TypeSlope
import com.mta.tehreer.graphics.TypeWeight
import com.mta.tehreer.graphics.TypeWidth
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.graphics.TypefaceInfo
import com.mta.tehreer.sfnt.SfntTag
import com.mta.tehreer.util.FontFileStore

import org.junit.Test

import java.util.Arrays

class FontFileTest {
    @Test
    fun testWithSudoFont() {
        val sudo = FontFileStore.sudo
        val typefaces: List<Typeface> = sudo.typefaces

        val variationAxes: List<VariationAxis> = Arrays.asList(
            VariationAxis.of(SfntTag.make("ital"), "Italic", 0, 0.0f, 0.0f, 1.0f),
            VariationAxis.of(SfntTag.make("wght"), "Weight", 0, 400.0f, 200.0f, 700.0f)
        )
        val namedStyles: List<NamedStyle> = Arrays.asList(
            NamedStyle.of("Regular", floatArrayOf(0.0f, 400.0f), null),
            NamedStyle.of("Thin", floatArrayOf(0.0f, 200.0f), null),
            NamedStyle.of("Light", floatArrayOf(0.0f, 309.375f), null),
            NamedStyle.of("Regular", floatArrayOf(0.0f, 387.5f), null),
            NamedStyle.of("Medium", floatArrayOf(0.0f, 543.75f), null),
            NamedStyle.of("Bold", floatArrayOf(0.0f, 700.0f), null),
            NamedStyle.of("Thin Italic", floatArrayOf(1.0f, 200.0f), null),
            NamedStyle.of("Light Italic", floatArrayOf(1.0f, 309.375f), null),
            NamedStyle.of("Regular Italic", floatArrayOf(1.0f, 387.5f), null),
            NamedStyle.of("Medium Italic", floatArrayOf(1.0f, 543.75f), null),
            NamedStyle.of("Bold Italic", floatArrayOf(1.0f, 700.0f), null)
        )

        assertNotNull(typefaces)
        assertEquals(typefaces.size, 11)

        val info = TypefaceInfo()
        info.isVariable = true
        info.variationAxes = variationAxes
        info.namedStyles = namedStyles
        info.familyName = "Sudo"
        info.weight = TypeWeight.REGULAR
        info.width = TypeWidth.NORMAL
        info.slope = TypeSlope.PLAIN
        info.unitsPerEm = 1024
        info.ascent = 832
        info.descent = 192
        info.leading = 0
        info.glyphCount = 1077
        info.boundingBox = Rect(-458, -209, 640, 960)
        info.underlinePosition = -160
        info.underlineThickness = 64
        info.strikeoutPosition = 268
        info.strikeoutThickness = 64

        val regular = TypefaceInfo(info)
        regular.variationCoordinates = floatArrayOf(0.0f, 400.0f)
        regular.styleName = "Regular"
        regular.fullName = "Sudo Regular"

        val thin = TypefaceInfo(info)
        thin.variationCoordinates = floatArrayOf(0.0f, 200.0f)
        thin.styleName = "Thin"
        thin.fullName = "Sudo Thin"
        thin.weight = TypeWeight.EXTRA_LIGHT

        val light = TypefaceInfo(info)
        light.variationCoordinates = floatArrayOf(0.0f, 309.375f)
        light.styleName = "Light"
        light.fullName = "Sudo Light"
        light.weight = TypeWeight.LIGHT

        val regularVariant = TypefaceInfo(info)
        regularVariant.variationCoordinates = floatArrayOf(0.0f, 387.5f)
        regularVariant.styleName = "Regular"
        regularVariant.fullName = "Sudo Regular"
        regularVariant.weight = TypeWeight.REGULAR

        val medium = TypefaceInfo(info)
        medium.variationCoordinates = floatArrayOf(0.0f, 543.75f)
        medium.styleName = "Medium"
        medium.fullName = "Sudo Medium"
        medium.weight = TypeWeight.MEDIUM

        val bold = TypefaceInfo(info)
        bold.variationCoordinates = floatArrayOf(0.0f, 700.0f)
        bold.styleName = "Bold"
        bold.fullName = "Sudo Bold"
        bold.weight = TypeWeight.BOLD

        val thinItalic = TypefaceInfo(info)
        thinItalic.variationCoordinates = floatArrayOf(1.0f, 200.0f)
        thinItalic.styleName = "Thin Italic"
        thinItalic.fullName = "Sudo Thin Italic"
        thinItalic.weight = TypeWeight.EXTRA_LIGHT
        thinItalic.slope = TypeSlope.ITALIC

        val lightItalic = TypefaceInfo(info)
        lightItalic.variationCoordinates = floatArrayOf(1.0f, 309.375f)
        lightItalic.styleName = "Light Italic"
        lightItalic.fullName = "Sudo Light Italic"
        lightItalic.weight = TypeWeight.LIGHT
        lightItalic.slope = TypeSlope.ITALIC

        val regularItalic = TypefaceInfo(info)
        regularItalic.variationCoordinates = floatArrayOf(1.0f, 387.5f)
        regularItalic.styleName = "Regular Italic"
        regularItalic.fullName = "Sudo Regular Italic"
        regularItalic.weight = TypeWeight.REGULAR
        regularItalic.slope = TypeSlope.ITALIC

        val mediumItalic = TypefaceInfo(info)
        mediumItalic.variationCoordinates = floatArrayOf(1.0f, 543.75f)
        mediumItalic.styleName = "Medium Italic"
        mediumItalic.fullName = "Sudo Medium Italic"
        mediumItalic.weight = TypeWeight.MEDIUM
        mediumItalic.slope = TypeSlope.ITALIC

        val boldItalic = TypefaceInfo(info)
        boldItalic.variationCoordinates = floatArrayOf(1.0f, 700.0f)
        boldItalic.styleName = "Bold Italic"
        boldItalic.fullName = "Sudo Bold Italic"
        boldItalic.weight = TypeWeight.BOLD
        boldItalic.slope = TypeSlope.ITALIC

        assertTypefaceEquals(typefaces.get(0), regular)
        assertTypefaceEquals(typefaces.get(1), thin)
        assertTypefaceEquals(typefaces.get(2), light)
        assertTypefaceEquals(typefaces.get(3), regularVariant)
        assertTypefaceEquals(typefaces.get(4), medium)
        assertTypefaceEquals(typefaces.get(5), bold)
        assertTypefaceEquals(typefaces.get(6), thinItalic)
        assertTypefaceEquals(typefaces.get(7), lightItalic)
        assertTypefaceEquals(typefaces.get(8), regularItalic)
        assertTypefaceEquals(typefaces.get(9), mediumItalic)
        assertTypefaceEquals(typefaces.get(10), boldItalic)
    }

    @Test
    fun testWithRocherColorFont() {
        val rocherColor = FontFileStore.rocherColor
        val typefaces: List<Typeface> = rocherColor.typefaces

        val variationAxes: List<VariationAxis> = Arrays.asList(
            VariationAxis.of(SfntTag.make("BVEL"), "Bevel", 0, 100.0f, 0.0f, 100.0f),
            VariationAxis.of(SfntTag.make("SHDW"), "Shadow", 0, 100.0f, 0.0f, 100.0f)
        )
        val namedStyles: List<NamedStyle> = Arrays.asList(
            NamedStyle.of("Regular", floatArrayOf(100.0f, 100.0f), null),
            NamedStyle.of("Shadow", floatArrayOf(100.0f, 50.0f), null),
            NamedStyle.of("Extrude", floatArrayOf(10.0f, 100.0f), null),
            NamedStyle.of("Bevel", floatArrayOf(100.0f, 0.0f), null),
            NamedStyle.of("Outline", floatArrayOf(0.0f, 0.0f), null)
        )
        val paletteEntryNames: List<String> = Arrays.asList("", "", "", "")
        val predefinedPalettes: List<ColorPalette> = Arrays.asList(
            ColorPalette.of("", 0, intArrayOf(0xFF513D32.toInt(), 0xFFF5B944.toInt(), 0xFFE08E37.toInt(), 0xFFF5CA56.toInt())),
            ColorPalette.of("", 0, intArrayOf(0xFF513898.toInt(), 0xFFF9869B.toInt(), 0xFFE86D83.toInt(), 0xFFFC9EAF.toInt())),
            ColorPalette.of("", 0, intArrayOf(0xFF0E7851.toInt(), 0xFF74E977.toInt(), 0xFF4EDB6C.toInt(), 0xFF91F787.toInt())),
            ColorPalette.of("", 0, intArrayOf(0xFFCD5646.toInt(), 0xFFE2F9D9.toInt(), 0xFFB5EADA.toInt(), 0xFFE2F9D9.toInt())),
            ColorPalette.of("", 0, intArrayOf(0xFF386398.toInt(), 0xFF87EFF9.toInt(), 0xFF53C2F9.toInt(), 0xFFB1F6FD.toInt())),
            ColorPalette.of("", 0, intArrayOf(0xFF527078.toInt(), 0xFFF2E667.toInt(), 0xFFB5B47D.toInt(), 0xFFFFF699.toInt())),
            ColorPalette.of("", 0, intArrayOf(0xFF6A4884.toInt(), 0xFFA297F7.toInt(), 0xFF8688F5.toInt(), 0xFFC1AFF7.toInt())),
            ColorPalette.of("", 0, intArrayOf(0xFF5B559D.toInt(), 0xFF66E6D2.toInt(), 0xFF6BC4C7.toInt(), 0xFF84F1E0.toInt())),
            ColorPalette.of("", 0, intArrayOf(0xFF996805.toInt(), 0xFFF4D13E.toInt(), 0xFFD7A619.toInt(), 0xFFFBDB53.toInt())),
            ColorPalette.of("", 0, intArrayOf(0xFF6F8393.toInt(), 0xFFCFD4DB.toInt(), 0xFFAAB6C1.toInt(), 0xFFDDE1E7.toInt())),
            ColorPalette.of("", 0, intArrayOf(0xFF6300BE.toInt(), 0xFF28ACFF.toInt(), 0xFF5EE8FF.toInt(), 0xFF67FCC1.toInt()))
        )

        assertNotNull(typefaces)
        assertEquals(typefaces.size, 5)

        val info = TypefaceInfo()
        info.isVariable = true
        info.variationAxes = variationAxes
        info.namedStyles = namedStyles
        info.paletteEntryNames = paletteEntryNames
        info.predefinedPalettes = predefinedPalettes
        info.familyName = "Rocher Color"
        info.weight = TypeWeight.REGULAR
        info.width = TypeWidth.NORMAL
        info.slope = TypeSlope.PLAIN
        info.unitsPerEm = 2000
        info.ascent = 1960
        info.descent = 590
        info.leading = 0
        info.glyphCount = 2105
        info.boundingBox = Rect(-1664, -629, 2988, 2044)
        info.underlinePosition = -100
        info.underlineThickness = 50
        info.strikeoutPosition = 696
        info.strikeoutThickness = 50

        val regular = TypefaceInfo(info)
        regular.variationCoordinates = floatArrayOf(100.0f, 100.0f)
        regular.associatedColors = intArrayOf(0xFF513D32.toInt(), 0xFFF5B944.toInt(), 0xFFE08E37.toInt(), 0xFFF5CA56.toInt())
        regular.styleName = "Regular"
        regular.fullName = "Rocher Color Regular"

        val shadow = TypefaceInfo(info)
        shadow.variationCoordinates = floatArrayOf(100.0f, 50.0f)
        shadow.associatedColors = intArrayOf(0xFF513D32.toInt(), 0xFFF5B944.toInt(), 0xFFE08E37.toInt(), 0xFFF5CA56.toInt())
        shadow.styleName = "Shadow"
        shadow.fullName = "Rocher Color Shadow"

        val extrude = TypefaceInfo(info)
        extrude.variationCoordinates = floatArrayOf(10.0f, 100.0f)
        extrude.associatedColors = intArrayOf(0xFF513D32.toInt(), 0xFFF5B944.toInt(), 0xFFE08E37.toInt(), 0xFFF5CA56.toInt())
        extrude.styleName = "Extrude"
        extrude.fullName = "Rocher Color Extrude"

        val bevel = TypefaceInfo(info)
        bevel.variationCoordinates = floatArrayOf(100.0f, 0.0f)
        bevel.associatedColors = intArrayOf(0xFF513D32.toInt(), 0xFFF5B944.toInt(), 0xFFE08E37.toInt(), 0xFFF5CA56.toInt())
        bevel.styleName = "Bevel"
        bevel.fullName = "Rocher Color Bevel"

        val outline = TypefaceInfo(info)
        outline.variationCoordinates = floatArrayOf(0.0f, 0.0f)
        outline.associatedColors = intArrayOf(0xFF513D32.toInt(), 0xFFF5B944.toInt(), 0xFFE08E37.toInt(), 0xFFF5CA56.toInt())
        outline.styleName = "Outline"
        outline.fullName = "Rocher Color Outline"

        assertTypefaceEquals(typefaces.get(0), regular)
        assertTypefaceEquals(typefaces.get(1), shadow)
        assertTypefaceEquals(typefaces.get(2), extrude)
        assertTypefaceEquals(typefaces.get(3), bevel)
        assertTypefaceEquals(typefaces.get(4), outline)
    }
}


