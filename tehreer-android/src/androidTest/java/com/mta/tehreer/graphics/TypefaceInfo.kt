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

import android.graphics.Rect

import com.mta.tehreer.font.ColorPalette
import com.mta.tehreer.font.NamedStyle
import com.mta.tehreer.font.VariationAxis

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals

class TypefaceInfo() {
    var isVariable: Boolean = false
    var variationAxes: List<VariationAxis>? = null
    var namedStyles: List<NamedStyle>? = null
    var variationCoordinates: FloatArray? = null
    var paletteEntryNames: List<String>? = null
    var predefinedPalettes: List<ColorPalette>? = null
    var associatedColors: IntArray? = null
    var familyName: String? = null
    var styleName: String? = null
    var fullName: String? = null
    var weight: TypeWeight? = null
    var width: TypeWidth? = null
    var slope: TypeSlope? = null
    var unitsPerEm: Int = 0
    var ascent: Int = 0
    var descent: Int = 0
    var leading: Int = 0
    var glyphCount: Int = 0
    var boundingBox: Rect? = null
    var underlinePosition: Int = 0
    var underlineThickness: Int = 0
    var strikeoutPosition: Int = 0
    var strikeoutThickness: Int = 0

    constructor(other: TypefaceInfo) : this() {
        isVariable = other.isVariable
        variationAxes = other.variationAxes
        namedStyles = other.namedStyles
        variationCoordinates = other.variationCoordinates
        paletteEntryNames = other.paletteEntryNames
        predefinedPalettes = other.predefinedPalettes
        associatedColors = other.associatedColors
        familyName = other.familyName
        styleName = other.styleName
        fullName = other.fullName
        weight = other.weight
        width = other.width
        slope = other.slope
        unitsPerEm = other.unitsPerEm
        ascent = other.ascent
        descent = other.descent
        leading = other.leading
        glyphCount = other.glyphCount
        boundingBox = other.boundingBox
        underlinePosition = other.underlinePosition
        underlineThickness = other.underlineThickness
        strikeoutPosition = other.strikeoutPosition
        strikeoutThickness = other.strikeoutThickness
    }

    companion object {
        fun assertTypefaceEquals(typeface: Typeface, info: TypefaceInfo) {
            assertEquals(typeface.isVariable, info.isVariable)
            assertEquals(typeface.variationAxes, info.variationAxes)
            assertEquals(typeface.namedStyles, info.namedStyles)
            assertArrayEquals(typeface.variationCoordinates, info.variationCoordinates, 0.0f)
            assertEquals(typeface.paletteEntryNames, info.paletteEntryNames)
            assertEquals(typeface.predefinedPalettes, info.predefinedPalettes)
            assertArrayEquals(typeface.associatedColors, info.associatedColors)
            assertEquals(typeface.familyName, info.familyName)
            assertEquals(typeface.styleName, info.styleName)
            assertEquals(typeface.fullName, info.fullName)
            assertEquals(typeface.weight, info.weight)
            assertEquals(typeface.width, info.width)
            assertEquals(typeface.slope, info.slope)
            assertEquals(typeface.unitsPerEm, info.unitsPerEm)
            assertEquals(typeface.ascent, info.ascent)
            assertEquals(typeface.descent, info.descent)
            assertEquals(typeface.leading, info.leading)
            assertEquals(typeface.glyphCount, info.glyphCount)
            assertEquals(typeface.boundingBox, info.boundingBox)
            assertEquals(typeface.underlinePosition, info.underlinePosition)
            assertEquals(typeface.underlineThickness, info.underlineThickness)
            assertEquals(typeface.strikeoutPosition, info.strikeoutPosition)
            assertEquals(typeface.strikeoutThickness, info.strikeoutThickness)
        }
    }
}
