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

import android.content.res.AssetManager
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import com.mta.tehreer.font.ColorPalette
import com.mta.tehreer.font.NamedStyle
import com.mta.tehreer.font.VariationAxis
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.collections.FloatBufferList
import com.mta.tehreer.internal.collections.Int32BufferIntList
import com.mta.tehreer.sfnt.SfntTag
import java.io.File
import java.io.InputStream

/**
 * The `Typeface` class specifies the typeface and intrinsic style of a font. This is used in the
 * renderer, along with optionally Renderer settings like typeSize, slantAngle, scaleX, to specify
 * how text appears when drawn (and measured).
 */
class Typeface {
    private class Finalizable(private val nativeTypeface: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeTypeface)
        }
    }

    /**
     * The properties that all the variation and color instances of a typeface share. They are read
     * from the native typeface when asked for, as most of the typefaces do not need them.
     */
    private class DefaultProperties(private val root: Typeface) {
        val variationAxes: List<VariationAxis>? by lazy {
            val handle = root.nativeTypeface

            List(root.nGetVariationAxisCount(handle)) {
                VariationAxis.of(
                    root.nGetVariationAxisTag(handle, it),
                    root.nGetVariationAxisName(handle, it),
                    root.nGetVariationAxisFlags(handle, it),
                    root.nGetVariationAxisDefaultValue(handle, it),
                    root.nGetVariationAxisMinValue(handle, it),
                    root.nGetVariationAxisMaxValue(handle, it)
                )
            }.takeIf { it.isNotEmpty() }
        }

        val namedStyles: List<NamedStyle>? by lazy {
            val handle = root.nativeTypeface
            val axisCount = variationAxes?.size ?: 0

            if (axisCount > 0) {
                List(root.nGetNamedStyleCount(handle)) {
                    NamedStyle.of(
                        root.nGetNamedStyleName(handle, it),
                        FloatBufferList(root, root.nGetNamedStyleCoordinatesPtr(handle, it), axisCount).toArray(),
                        root.nGetNamedStylePostScriptName(handle, it)
                    )
                }.takeIf { it.isNotEmpty() }
            } else {
                null
            }
        }

        val paletteEntryNames: List<String>? by lazy {
            val handle = root.nativeTypeface

            List(root.nGetPaletteEntryCount(handle)) { root.nGetPaletteEntryName(handle, it) }
                .takeIf { it.isNotEmpty() }
        }

        val predefinedPalettes: List<ColorPalette>? by lazy {
            val handle = root.nativeTypeface
            val entryCount = paletteEntryNames?.size ?: 0

            if (entryCount > 0) {
                List(root.nGetPredefinedPaletteCount(handle)) {
                    ColorPalette.of(
                        root.nGetPredefinedPaletteName(handle, it),
                        root.nGetPredefinedPaletteFlags(handle, it),
                        Int32BufferIntList(root, root.nGetPredefinedPaletteColorsPtr(handle, it), entryCount).toArray()
                    )
                }.takeIf { it.isNotEmpty() }
            } else {
                null
            }
        }
    }

    internal val nativeTypeface: Long
    internal var tag: Any? = null

    private val defaults: DefaultProperties
    private val finalizable: Finalizable

    /**
     * Constructs a typeface from the specified asset. The data of the asset is not copied into the
     * memory. Rather, it is directly read from the stream when needed. So the performance of
     * resulting typeface might be slower and should be used with caution.
     *
     * @param assetManager The application's asset manager.
     * @param filePath The path of the font file in the assets directory.
     *
     * @throws RuntimeException if an error occurred while initialization.
     */
    constructor(assetManager: AssetManager, filePath: String) : this(
        nCreateWithAsset(assetManager, filePath).also {
            if (it == 0L) {
                throw RuntimeException("Could not create typeface from specified asset")
            }
        }
    )

    /**
     * Constructs a typeface from the specified file. The data for the font is directly read from
     * the file when needed.
     *
     * @param file The font file.
     *
     * @throws RuntimeException if an error occurred while initialization.
     */
    constructor(file: File) : this(
        nCreateWithFile(file.absolutePath).also {
            if (it == 0L) {
                throw RuntimeException("Could not create typeface from specified file")
            }
        }
    )

    /**
     * Constructs a new typeface from the input stream by copying its data into a native memory
     * buffer. It may take time to create the typeface if the stream holds larger data.
     *
     * @param stream The input stream that contains the data of the font.
     *
     * @throws RuntimeException if an error occurred while initialization.
     */
    constructor(stream: InputStream) : this(
        nCreateFromStream(stream).also {
            if (it == 0L) {
                throw RuntimeException("Could not create typeface from specified stream")
            }
        }
    )

    internal constructor(nativeTypeface: Long) {
        this.nativeTypeface = nativeTypeface
        this.defaults = DefaultProperties(this)
        this.finalizable = Finalizable(nativeTypeface)
    }

    private constructor(parent: Typeface, nativeTypeface: Long) {
        this.nativeTypeface = nativeTypeface
        this.defaults = parent.defaults
        this.finalizable = Finalizable(nativeTypeface)
    }

    /**
     * Returns `true` if this typeface supports OpenType font variations.
     *
     * @return `true` if this typeface supports OpenType font variations.
     */
    val isVariable: Boolean
        get() = variationAxes != null

    /**
     * Returns a variation instance of this typeface with the specified design coordinates.
     *
     * @param coordinates The variation design coordinates.
     * @return A variation instance of this typeface with the specified design coordinates.
     *
     * @throws IllegalStateException if this typeface does not support OpenType font variations.
     * @throws IllegalArgumentException if the number of specified design coordinates does not match
     *                                  the number of variation axes.
     */
    fun getVariationInstance(coordinates: FloatArray): Typeface? {
        val variationAxes = checkNotNull(variationAxes) {
            "This typeface does not support variations."
        }
        require(coordinates.size == variationAxes.size) {
            "The number of coordinates does not match with variation axes."
        }

        return Typeface(this, nGetVariationInstance(nativeTypeface, coordinates))
    }

    /**
     * Returns the variation axes of this typeface if it supports OpenType font variations.
     *
     * @return The variation axes of this typeface if it supports OpenType font variations.
     */
    val variationAxes: List<VariationAxis>?
        get() = defaults.variationAxes

    /**
     * Returns the named instance records of this typeface if it supports OpenType font variations.
     *
     * @return The named instance records of this typeface if it supports OpenType font variations.
     */
    val namedStyles: List<NamedStyle>?
        get() = defaults.namedStyles

    /**
     * Returns the design variation coordinates of this typeface if it supports OpenType font
     * variations.
     *
     * @return The design variation coordinates of this typeface if it supports OpenType font
     *         variations.
     */
    val variationCoordinates: FloatArray?
        get() = variationAxes?.let {
            FloatBufferList(this, nGetVariationCoordinatesPtr(nativeTypeface), it.size).toArray()
        }

    /**
     * Returns the names associated with palette entries if this typeface supports OpenType color
     * palettes.
     *
     * @return The names associated with palette entries if this typeface supports OpenType color
     * palettes.
     */
    val paletteEntryNames: List<String>?
        get() = defaults.paletteEntryNames

    /**
     * Returns the predefined palettes in this typeface if it supports OpenType color palettes.
     *
     * @return The predefined palettes in this typeface if it supports OpenType color palettes.
     */
    val predefinedPalettes: List<ColorPalette>?
        get() = defaults.predefinedPalettes

    /**
     * Returns the colors associated with this typeface if it supports OpenType color palettes.
     *
     * @return The colors associated with this typeface if it supports OpenType color palettes.
     */
    val associatedColors: IntArray?
        get() = paletteEntryNames?.let {
            Int32BufferIntList(this, nGetAssociatedColorsPtr(nativeTypeface), it.size).toArray()
        }

    /**
     * Returns a color instance of this typeface with the specified colors array.
     *
     * @param colors The colors array.
     * @return A color instance of this typeface with the specified colors array.
     *
     * @throws IllegalStateException if this typeface does not support OpenType color palettes.
     * @throws IllegalArgumentException if the number of specified colors does not match the number
     *                                  of colors in `CPAL` table.
     */
    fun getColorInstance(colors: IntArray): Typeface? {
        val paletteEntryNames = checkNotNull(paletteEntryNames) {
            "This typeface does not support color palettes"
        }

        val count = paletteEntryNames.size
        require(colors.size == count) { "Palette should have exactly $count colors" }

        return Typeface(this, nGetColorInstance(nativeTypeface, colors))
    }

    /**
     * Returns the family name of this typeface.
     *
     * @return The family name of this typeface.
     */
    val familyName: String
        get() = nGetFamilyName(nativeTypeface)

    /**
     * Returns the style name of this typeface.
     *
     * @return The style name of this typeface.
     */
    val styleName: String
        get() = nGetStyleName(nativeTypeface)

    /**
     * Returns the full name of this typeface.
     *
     * @return The full name of this typeface.
     */
    val fullName: String
        get() = nGetFullName(nativeTypeface)

    /**
     * Returns the typographic weight of this typeface. The weight value determines the thickness
     * associated with a given character in a typeface.
     *
     * @return The typographic weight of this typeface.
     */
    val weight: TypeWeight
        get() = TypeWeight.valueOf(nGetWeight(nativeTypeface))

    /**
     * Returns the typographic width of this typeface. The width value determines whether a typeface
     * is expanded or condensed when it is displayed.
     *
     * @return The typographic width of this typeface.
     */
    val width: TypeWidth
        get() = TypeWidth.valueOf(nGetWidth(nativeTypeface))

    /**
     * Returns the typographic slope of this typeface. The slope value determines whether a typeface
     * is plain or slanted when it is displayed.
     *
     * @return The typographic slope of this typeface.
     */
    val slope: TypeSlope
        get() = TypeSlope.valueOf(nGetSlope(nativeTypeface))

    /**
     * Generates an array of bytes containing the data of the intended table.
     *
     * @param tableTag The tag of the table as an integer. It can be created from string by using
     *                 [SfntTag.make] method.
     * @return An array of bytes containing the data of the table, or `null` if no such table
     *         exists.
     */
    fun getTableData(tableTag: Int): ByteArray? {
        return nGetTableData(nativeTypeface, tableTag)
    }

    /**
     * Returns the number of font units per EM square for this typeface.
     *
     * @return The number of font units per EM square for this typeface.
     */
    val unitsPerEm: Int
        get() = nGetUnitsPerEm(nativeTypeface)

    /**
     * Returns the typographic ascender of this typeface expressed in font units.
     *
     * @return The typographic ascender of this typeface expressed in font units.
     */
    val ascent: Int
        get() = nGetAscent(nativeTypeface)

    /**
     * Returns the typographic descender of this typeface expressed in font units.
     *
     * @return The typographic descender of this typeface expressed in font units.
     */
    val descent: Int
        get() = nGetDescent(nativeTypeface)

    /**
     * Returns the typographic leading of this typeface expressed in font units.
     *
     * @return The typographic leading of this typeface expressed in font units.
     */
    val leading: Int
        get() = nGetLeading(nativeTypeface)

    /**
     * Returns the number of glyphs in this typeface.
     *
     * @return The number of glyphs in this typeface.
     */
    val glyphCount: Int
        get() = nGetGlyphCount(nativeTypeface)

    /**
     * Returns the glyph id for the specified code point.
     *
     * @param codePoint The code point for which the glyph id is obtained.
     * @return The glyph id for the specified code point.
     */
    fun getGlyphId(codePoint: Int): Int {
        return nGetGlyphId(nativeTypeface, codePoint)
    }

    /**
     * Retrieves the advance for the specified glyph.
     *
     * @param glyphId The glyph id for which to retrieve the advance.
     * @param typeSize The size for which the advance is retrieved.
     * @param vertical The flag which indicates the type of advance, either horizontal or vertical.
     * @return The advance for the specified glyph.
     */
    fun getGlyphAdvance(glyphId: Int, typeSize: Float, vertical: Boolean): Float {
        return nGetGlyphAdvance(nativeTypeface, glyphId, typeSize, vertical)
    }

    /**
     * Generates the path for the specified glyph.
     *
     * @param glyphId The glyph id for which the path is generated.
     * @param typeSize The size for which the glyph path is required.
     * @param matrix The matrix applied to the path. Can be `null` if no transformation is required.
     * @return The path for the specified glyph.
     */
    fun getGlyphPath(glyphId: Int, typeSize: Float, matrix: Matrix?): Path {
        val values = FloatArray(9)
        (matrix ?: Matrix()).getValues(values)

        return nGetGlyphPath(
            nativeTypeface, glyphId, typeSize,
            values[Matrix.MSCALE_X], values[Matrix.MSKEW_X], values[Matrix.MTRANS_X],
            values[Matrix.MSKEW_Y], values[Matrix.MSCALE_Y], values[Matrix.MTRANS_Y]
        )
    }

    /**
     * Returns the font bounding box expressed in font units. The box is large enough to contain any
     * glyph from the font.
     *
     * @return The font bounding box expressed in font units.
     */
    val boundingBox: Rect
        get() = nGetBoundingBox(nativeTypeface)

    /**
     * Returns the position, in font units, of the underline for this typeface.
     *
     * @return The position, in font units, of the underline for this typeface.
     */
    val underlinePosition: Int
        get() = nGetUnderlinePosition(nativeTypeface)

    /**
     * Returns the thickness, in font units, of the underline for this typeface.
     *
     * @return The thickness, in font units, of the underline for this typeface.
     */
    val underlineThickness: Int
        get() = nGetUnderlineThickness(nativeTypeface)

    /**
     * Returns the position, in font units, of the strikeout for this typeface.
     *
     * @return The position, in font units, of the strikeout for this typeface.
     */
    val strikeoutPosition: Int
        get() = nGetStrikeoutPosition(nativeTypeface)

    /**
     * Returns the thickness, in font units, of the strikeout for this typeface.
     *
     * @return The thickness, in font units, of the strikeout for this typeface.
     */
    val strikeoutThickness: Int
        get() = nGetStrikeoutThickness(nativeTypeface)

    override fun toString(): String {
        return "Typeface{familyName=$familyName" +
            ", styleName=$styleName" +
            ", fullName=$fullName" +
            ", weight=$weight" +
            ", width=$width" +
            ", slope=$slope" +
            ", unitsPerEm=$unitsPerEm" +
            ", ascent=$ascent" +
            ", descent=$descent" +
            ", leading=$leading" +
            ", glyphCount=$glyphCount" +
            ", boundingBox=$boundingBox" +
            ", underlinePosition=$underlinePosition" +
            ", underlineThickness=$underlineThickness" +
            ", strikeoutPosition=$strikeoutPosition" +
            ", strikeoutThickness=$strikeoutThickness" +
            "}"
    }

    private external fun nGetFamilyName(nativeTypeface: Long): String
    private external fun nGetStyleName(nativeTypeface: Long): String
    private external fun nGetFullName(nativeTypeface: Long): String
    private external fun nGetWeight(nativeTypeface: Long): Int
    private external fun nGetWidth(nativeTypeface: Long): Int
    private external fun nGetSlope(nativeTypeface: Long): Int
    private external fun nGetVariationAxisCount(nativeTypeface: Long): Int
    private external fun nGetVariationAxisTag(nativeTypeface: Long, index: Int): Int
    private external fun nGetVariationAxisFlags(nativeTypeface: Long, index: Int): Int
    private external fun nGetVariationAxisName(nativeTypeface: Long, index: Int): String
    private external fun nGetVariationAxisMinValue(nativeTypeface: Long, index: Int): Float
    private external fun nGetVariationAxisDefaultValue(nativeTypeface: Long, index: Int): Float
    private external fun nGetVariationAxisMaxValue(nativeTypeface: Long, index: Int): Float
    private external fun nGetNamedStyleCount(nativeTypeface: Long): Int
    private external fun nGetNamedStyleName(nativeTypeface: Long, index: Int): String
    private external fun nGetNamedStylePostScriptName(nativeTypeface: Long, index: Int): String?
    private external fun nGetNamedStyleCoordinatesPtr(nativeTypeface: Long, index: Int): Long
    private external fun nGetVariationInstance(nativeTypeface: Long, coordinates: FloatArray): Long
    private external fun nGetVariationCoordinatesPtr(nativeTypeface: Long): Long
    private external fun nGetPaletteEntryCount(nativeTypeface: Long): Int
    private external fun nGetPaletteEntryName(nativeTypeface: Long, index: Int): String
    private external fun nGetPredefinedPaletteCount(nativeTypeface: Long): Int
    private external fun nGetPredefinedPaletteName(nativeTypeface: Long, index: Int): String
    private external fun nGetPredefinedPaletteFlags(nativeTypeface: Long, index: Int): Int
    private external fun nGetPredefinedPaletteColorsPtr(nativeTypeface: Long, index: Int): Long
    private external fun nGetColorInstance(nativeTypeface: Long, colors: IntArray): Long
    private external fun nGetAssociatedColorsPtr(nativeTypeface: Long): Long
    private external fun nGetTableData(nativeTypeface: Long, tableTag: Int): ByteArray?
    private external fun nGetUnitsPerEm(nativeTypeface: Long): Int
    private external fun nGetAscent(nativeTypeface: Long): Int
    private external fun nGetDescent(nativeTypeface: Long): Int
    private external fun nGetLeading(nativeTypeface: Long): Int
    private external fun nGetGlyphCount(nativeTypeface: Long): Int
    private external fun nGetGlyphId(nativeTypeface: Long, codePoint: Int): Int
    private external fun nGetGlyphAdvance(
        nativeTypeface: Long, glyphId: Int, typeSize: Float, vertical: Boolean
    ): Float
    private external fun nGetGlyphPath(
        nativeTypeface: Long, glyphId: Int, typeSize: Float,
        scaleX: Float, skewX: Float, translateX: Float,
        skewY: Float, scaleY: Float, translateY: Float
    ): Path
    private external fun nGetBoundingBox(nativeTypeface: Long): Rect
    private external fun nGetUnderlinePosition(nativeTypeface: Long): Int
    private external fun nGetUnderlineThickness(nativeTypeface: Long): Int
    private external fun nGetStrikeoutPosition(nativeTypeface: Long): Int
    private external fun nGetStrikeoutThickness(nativeTypeface: Long): Int
    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nCreateWithAsset(assetManager: AssetManager, path: String): Long
        @JvmStatic external fun nCreateWithFile(path: String): Long
        @JvmStatic external fun nCreateFromStream(stream: InputStream): Long

        @JvmStatic external fun nDispose(nativeTypeface: Long)

    }
}
