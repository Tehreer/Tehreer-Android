/*
 * Copyright (C) 2016-2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.graphics;

import android.content.res.AssetManager;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.font.ColorPalette;
import com.mta.tehreer.font.NamedStyle;
import com.mta.tehreer.font.VariationAxis;
import com.mta.tehreer.internal.JniBridge;
import com.mta.tehreer.sfnt.SfntTag;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;
import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * The <code>Typeface</code> class specifies the typeface and intrinsic style of a font. This is
 * used in the renderer, along with optionally Renderer settings like typeSize, slantAngle, scaleX,
 * to specify how text appears when drawn (and measured).
 */
public class Typeface {
    static {
        JniBridge.loadLibrary();
    }

    private class Finalizable {
        @Override
        protected void finalize() throws Throwable {
            try {
                dispose();
            } finally {
                super.finalize();
            }
        }
    }

    @Keep
    long nativeTypeface;
    @Nullable Object tag;
    private final @NonNull Finalizable finalizable = new Finalizable();

    private static class DesignCharacteristics {
        @NonNull TypeWeight weight = TypeWeight.REGULAR;
        @NonNull TypeWidth width = TypeWidth.NORMAL;
        @NonNull TypeSlope slope = TypeSlope.PLAIN;
    }

    private static class DefaultProperties {
        @Nullable List<VariationAxis> variationAxes;
        @Nullable List<NamedStyle> namedStyles;

        @Nullable List<String> paletteEntryNames;
        @Nullable List<ColorPalette> predefinedPalettes;
    }

    private static class StandardNames {
        @NonNull String familyName = "";
        @NonNull String styleName = "";
        @NonNull String fullName = "";
    }

    private DefaultProperties defaults;
    private DesignCharacteristics design;
    private StandardNames names;

    /**
     * Constructs a typeface from the specified asset. The data of the asset is not copied into the
     * memory. Rather, it is directly read from the stream when needed. So the performance of
     * resulting typeface might be slower and should be used with caution.
     *
     * @param assetManager The application's asset manager.
     * @param filePath The path of the font file in the assets directory.
     *
     * @throws NullPointerException if <code>assetManager</code> or <code>filePath</code> is null.
     * @throws RuntimeException if an error occurred while initialization.
     */
    public Typeface(@NonNull AssetManager assetManager, @NonNull String filePath) {
        checkNotNull(assetManager, "assetManager");
        checkNotNull(filePath, "filePath");

        long nativeTypeface = nCreateWithAsset(assetManager, filePath);
        if (nativeTypeface == 0) {
            throw new RuntimeException("Could not create typeface from specified asset");
        }

        init(nativeTypeface);
    }

    /**
     * Constructs a typeface from the specified file. The data for the font is directly read from
     * the file when needed.
     *
     * @param file The font file.
     *
     * @throws NullPointerException if <code>file</code> is null.
     * @throws RuntimeException if an error occurred while initialization.
     */
    public Typeface(@NonNull File file) {
        checkNotNull(file, "file");

        long nativeTypeface = nCreateWithFile(file.getAbsolutePath());
        if (nativeTypeface == 0) {
            throw new RuntimeException("Could not create typeface from specified file");
        }

        init(nativeTypeface);
    }

    /**
     * Constructs a new typeface from the input stream by copying its data into a native memory
     * buffer. It may take time to create the typeface if the stream holds larger data.
     *
     * @param stream The input stream that contains the data of the font.
     *
     * @throws NullPointerException if <code>stream</code> is null.
     * @throws RuntimeException if an error occurred while initialization.
     */
    public Typeface(@NonNull InputStream stream) {
        checkNotNull(stream, "stream");

        long nativeTypeface = nCreateFromStream(stream);
        if (nativeTypeface == 0) {
            throw new RuntimeException("Could not create typeface from specified stream");
        }

        init(nativeTypeface);
    }

    @Keep
    Typeface(long nativeTypeface) {
        init(nativeTypeface);
    }

    private void init(long nativeTypeface) {
        this.nativeTypeface = nativeTypeface;
        this.defaults = new DefaultProperties();
        this.design = null;
        this.names = null;

        setupVariations();
        setupPalettes();
        setupDesignCharacteristics();
        setupNames();
    }

    private Typeface(@NonNull Typeface typeface, @NonNull float[] coordinates) {
        this.nativeTypeface = nGetVariationInstance(typeface.nativeTypeface, coordinates);
        this.defaults = typeface.defaults;
        this.design = null;
        this.names = null;

        setupDesignCharacteristics();
        setupNames();
    }

    private Typeface(@NonNull Typeface typeface, @NonNull int[] colors) {
        this.nativeTypeface = nGetColorInstance(typeface.nativeTypeface, colors);
        this.defaults = typeface.defaults;
        this.design = typeface.design;
        this.names = typeface.names;
    }

    private void setupVariations() {
        final int axisCount = nGetVariationAxisCount(nativeTypeface);
        if (axisCount == 0) {
            return;
        }

        final int[] tagsAndFlags = new int[axisCount * 2];
        final float[] values = new float[axisCount * 3];
        final String[] axisNames = new String[axisCount];
        nGetVariationAxes(nativeTypeface, tagsAndFlags, values, axisNames);

        final List<VariationAxis> variationAxes = new ArrayList<>(axisCount);

        for (int i = 0; i < axisCount; i++) {
            variationAxes.add(VariationAxis.of(tagsAndFlags[i * 2], axisNames[i], tagsAndFlags[i * 2 + 1],
                                               values[i * 3 + 1], values[i * 3], values[i * 3 + 2]));
        }

        final int styleCount = nGetNamedStyleCount(nativeTypeface);
        final String[] styleNames = new String[styleCount];
        final String[] postScriptNames = new String[styleCount];
        final float[] coordinates = new float[styleCount * axisCount];
        nGetNamedStyles(nativeTypeface, styleNames, postScriptNames, coordinates);

        final List<NamedStyle> namedStyles = new ArrayList<>(styleCount);

        for (int i = 0; i < styleCount; i++) {
            final float[] styleCoordinates = Arrays.copyOfRange(coordinates, i * axisCount, (i + 1) * axisCount);

            namedStyles.add(NamedStyle.of(styleNames[i], styleCoordinates, postScriptNames[i]));
        }

        defaults.variationAxes = variationAxes;
        defaults.namedStyles = namedStyles;
    }

    private void setupPalettes() {
        final int entryCount = nGetPaletteEntryCount(nativeTypeface);
        if (entryCount == 0) {
            return;
        }

        final String[] entryNames = new String[entryCount];
        nGetPaletteEntryNames(nativeTypeface, entryNames);

        final int paletteCount = nGetPredefinedPaletteCount(nativeTypeface);
        final String[] paletteNames = new String[paletteCount];
        final int[] paletteFlags = new int[paletteCount];
        final int[] paletteColors = new int[paletteCount * entryCount];
        nGetPredefinedPalettes(nativeTypeface, paletteNames, paletteFlags, paletteColors);

        final List<ColorPalette> predefinedPalettes = new ArrayList<>(paletteCount);

        for (int i = 0; i < paletteCount; i++) {
            final int[] colors = Arrays.copyOfRange(paletteColors, i * entryCount, (i + 1) * entryCount);

            predefinedPalettes.add(ColorPalette.of(paletteNames[i], paletteFlags[i], colors));
        }

        defaults.predefinedPalettes = predefinedPalettes;
        defaults.paletteEntryNames = Arrays.asList(entryNames);
    }

    private void setupDesignCharacteristics() {
        design = new DesignCharacteristics();
        design.weight = TypeWeight.valueOf(nGetWeight(nativeTypeface));
        design.width = TypeWidth.valueOf(nGetWidth(nativeTypeface));
        design.slope = TypeSlope.valueOf(nGetSlope(nativeTypeface));
    }

    private void setupNames() {
        names = new StandardNames();
        names.familyName = nGetFamilyName(nativeTypeface);
        names.styleName = nGetStyleName(nativeTypeface);
        names.fullName = nGetFullName(nativeTypeface);
    }

    /**
     * Returns <code>true</code> if this typeface supports OpenType font variations.
     *
     * @return <code>true</code> if this typeface supports OpenType font variations.
     */
    public boolean isVariable() {
        return getVariationAxes() != null;
    }

    /**
     * Returns a variation instance of this typeface with the specified design coordinates.
     *
     * @param coordinates The variation design coordinates.
     * @return A variation instance of this typeface with the specified design coordinates.
     *
     * @throws IllegalStateException if this typeface does not support OpenType font variations.
     * @throws NullPointerException if <code>coordinates</code> parameter is null.
     * @throws IllegalArgumentException if the number of specified design coordinates does not match
     *                                  the number of variation axes.
     */
    public @Nullable Typeface getVariationInstance(@NonNull float[] coordinates) {
        final List<VariationAxis> variationAxes = getVariationAxes();

        if (variationAxes == null) {
            throw new IllegalStateException("This typeface does not support variations.");
        }
        checkNotNull(coordinates, "coordinates");
        checkArgument(coordinates.length == variationAxes.size(), "The number of coordinates does not match with variation axes.");

        return new Typeface(this, coordinates);
    }

    /**
     * Returns the variation axes of this typeface if it supports OpenType font variations.
     *
     * @return The variation axes of this typeface if it supports OpenType font variations.
     */
    public @Nullable List<VariationAxis> getVariationAxes() {
        final List<VariationAxis> variationAxes = defaults.variationAxes;
        if (variationAxes != null && !variationAxes.isEmpty()) {
            return Collections.unmodifiableList(variationAxes);
        }

        return null;
    }

    /**
     * Returns the named instance records of this typeface if it supports OpenType font variations.
     *
     * @return The named instance records of this typeface if it supports OpenType font variations.
     */
    public @Nullable List<NamedStyle> getNamedStyles() {
        final List<NamedStyle> namedStyles = defaults.namedStyles;
        if (namedStyles != null && !namedStyles.isEmpty()) {
            return Collections.unmodifiableList(namedStyles);
        }

        return null;
    }

    /**
     * Returns the design variation coordinates of this typeface if it supports OpenType font
     * variations.
     *
     * @return The design variation coordinates of this typeface if it supports OpenType font
     *         variations.
     */
    public @Nullable float[] getVariationCoordinates() {
        final List<VariationAxis> variationAxes = getVariationAxes();
        if (variationAxes != null) {
            float[] coordinates = new float[variationAxes.size()];
            nGetVariationCoordinates(nativeTypeface, coordinates);

            return coordinates;
        }

        return null;
    }

    /**
     * Returns the names associated with palette entries if this typeface supports OpenType color
     * palettes.
     *
     * @return The names associated with palette entries if this typeface supports OpenType color
     * palettes.
     */
    public @Nullable List<String> getPaletteEntryNames() {
        final List<String> paletteEntryNames = defaults.paletteEntryNames;
        if (paletteEntryNames != null && !paletteEntryNames.isEmpty()) {
            return Collections.unmodifiableList(paletteEntryNames);
        }

        return null;
    }

    /**
     * Returns the predefined palettes in this typeface if it supports OpenType color palettes.
     *
     * @return The predefined palettes in this typeface if it supports OpenType color palettes.
     */
    public @Nullable List<ColorPalette> getPredefinedPalettes() {
        final List<ColorPalette> predefinedPalettes = defaults.predefinedPalettes;
        if (predefinedPalettes != null && !predefinedPalettes.isEmpty()) {
            return Collections.unmodifiableList(predefinedPalettes);
        }

        return null;
    }

    /**
     * Returns the colors associated with this typeface if it supports OpenType color palettes.
     *
     * @return The colors associated with this typeface if it supports OpenType color palettes.
     */
    public @Nullable int[] getAssociatedColors() {
        final List<String> paletteEntryNames = getPaletteEntryNames();
        if (paletteEntryNames != null) {
            int[] colors = new int[paletteEntryNames.size()];
            nGetAssociatedColors(nativeTypeface, colors);

            return colors;
        }

        return null;
    }

    /**
     * Returns a color instance of this typeface with the specified colors array.
     *
     * @param colors The colors array.
     * @return A color instance of this typeface with the specified colors array.
     *
     * @throws IllegalStateException if this typeface does not support OpenType color palettes.
     * @throws NullPointerException if <code>colors</code> parameter is null.
     * @throws IllegalArgumentException if the number of specified colors does not match the number
     *                                  of colors in `CPAL` table.
     */
    public @Nullable Typeface getColorInstance(@NonNull int[] colors) {
        final List<String> paletteEntryNames = getPaletteEntryNames();
        if (paletteEntryNames == null) {
            throw new IllegalStateException("This typeface does not support color palettes");
        }
        checkNotNull(colors, "colors");

        final int count = paletteEntryNames.size();
        checkArgument(colors.length == count, "Palette should have exactly " + count + " colors");

        return new Typeface(this, colors);
    }

    /**
     * Returns the family name of this typeface.
     *
     * @return The family name of this typeface.
     */
    public String getFamilyName() {
        return names.familyName;
    }

    /**
     * Returns the style name of this typeface.
     *
     * @return The style name of this typeface.
     */
    public String getStyleName() {
        return names.styleName;
    }

    /**
     * Returns the full name of this typeface.
     *
     * @return The full name of this typeface.
     */
    public String getFullName() {
        return names.fullName;
    }

    /**
     * Returns the typographic weight of this typeface. The weight value determines the thickness
     * associated with a given character in a typeface.
     *
     * @return The typographic weight of this typeface.
     */
    public @NonNull TypeWeight getWeight() {
        return design.weight;
    }

    /**
     * Returns the typographic width of this typeface. The width value determines whether a typeface
     * is expanded or condensed when it is displayed.
     *
     * @return The typographic width of this typeface.
     */
    public @NonNull TypeWidth getWidth() {
        return design.width;
    }

    /**
     * Returns the typographic slope of this typeface. The slope value determines whether a typeface
     * is plain or slanted when it is displayed.
     *
     * @return The typographic slope of this typeface.
     */
    public @NonNull TypeSlope getSlope() {
        return design.slope;
    }

    /**
     * Generates an array of bytes containing the data of the intended table.
     *
     * @param tableTag The tag of the table as an integer. It can be created from string by using
     *                 {@link SfntTag#make(String)} method.
     * @return An array of bytes containing the data of the table, or <code>null</code> if no such
     *         table exists.
     */
    public @Nullable byte[] getTableData(int tableTag) {
        return nGetTableData(nativeTypeface, tableTag);
    }

    /**
     * Returns the number of font units per EM square for this typeface.
     *
     * @return The number of font units per EM square for this typeface.
     */
	public int getUnitsPerEm() {
		return nGetUnitsPerEm(nativeTypeface);
	}

    /**
     * Returns the typographic ascender of this typeface expressed in font units.
     *
     * @return The typographic ascender of this typeface expressed in font units.
     */
	public int getAscent() {
		return nGetAscent(nativeTypeface);
	}

    /**
     * Returns the typographic descender of this typeface expressed in font units.
     *
     * @return The typographic descender of this typeface expressed in font units.
     */
	public int getDescent() {
		return nGetDescent(nativeTypeface);
	}

    /**
     * Returns the typographic leading of this typeface expressed in font units.
     *
     * @return The typographic leading of this typeface expressed in font units.
     */
    public int getLeading() {
        return nGetLeading(nativeTypeface);
    }

    /**
     * Returns the number of glyphs in this typeface.
     *
     * @return The number of glyphs in this typeface.
     */
	public int getGlyphCount() {
        return nGetGlyphCount(nativeTypeface);
    }

    /**
     * Returns the glyph id for the specified code point.
     *
     * @param codePoint The code point for which the glyph id is obtained.
     * @return The glyph id for the specified code point.
     */
    public int getGlyphId(int codePoint) {
        return nGetGlyphId(nativeTypeface, codePoint);
    }

    /**
     * Retrieves the advance for the specified glyph.
     *
     * @param glyphId The glyph id for which to retrieve the advance.
     * @param typeSize The size for which the advance is retrieved.
     * @param vertical The flag which indicates the type of advance, either horizontal or vertical.
     * @return The advance for the specified glyph.
     */
    public float getGlyphAdvance(int glyphId, float typeSize, boolean vertical) {
        return nGetGlyphAdvance(nativeTypeface, glyphId, typeSize, vertical);
    }

    /**
     * Generates the path for the specified glyph.
     *
     * @param glyphId The glyph id for which the path is generated.
     * @param typeSize The size for which the glyph path is required.
     * @param matrix The matrix applied to the path. Can be <code>null</code> if no transformation
     *               is required.
     * @return The path for the specified glyph.
     */
    public @NonNull Path getGlyphPath(int glyphId, float typeSize, @Nullable Matrix matrix) {
        float[] values = null;
        if (matrix != null) {
            values = new float[9];
            matrix.getValues(values);
        }

        return nGetGlyphPath(nativeTypeface, glyphId, typeSize, values);
    }

    /**
     * Returns the font bounding box expressed in font units. The box is large enough to contain any
     * glyph from the font.
     *
     * @return The font bounding box expressed in font units.
     */
    public @NonNull Rect getBoundingBox() {
	    Rect boundingBox = new Rect();
	    nGetBoundingBox(nativeTypeface, boundingBox);

	    return boundingBox;
	}

    /**
     * Returns the position, in font units, of the underline for this typeface.
     *
     * @return The position, in font units, of the underline for this typeface.
     */
	public int getUnderlinePosition() {
	    return nGetUnderlinePosition(nativeTypeface);
	}

    /**
     * Returns the thickness, in font units, of the underline for this typeface.
     *
     * @return The thickness, in font units, of the underline for this typeface.
     */
	public int getUnderlineThickness() {
	    return nGetUnderlineThickness(nativeTypeface);
	}

    /**
     * Returns the position, in font units, of the strikeout for this typeface.
     *
     * @return The position, in font units, of the strikeout for this typeface.
     */
    public int getStrikeoutPosition() {
        return nGetStrikeoutPosition(nativeTypeface);
    }

    /**
     * Returns the thickness, in font units, of the strikeout for this typeface.
     *
     * @return The thickness, in font units, of the strikeout for this typeface.
     */
    public int getStrikeoutThickness() {
        return nGetStrikeoutThickness(nativeTypeface);
    }

    void dispose() {
        nDispose(nativeTypeface);
    }

    @Override
    public String toString() {
        return "Typeface{familyName=" + getFamilyName()
                + ", styleName=" + getStyleName()
                + ", fullName=" + getFullName()
                + ", weight=" + getWeight()
                + ", width=" + getWidth()
                + ", slope=" + getSlope()
                + ", unitsPerEm=" + getUnitsPerEm()
                + ", ascent=" + getAscent()
                + ", descent=" + getDescent()
                + ", leading=" + getLeading()
                + ", glyphCount=" + getGlyphCount()
                + ", boundingBox=" + getBoundingBox()
                + ", underlinePosition=" + getUnderlinePosition()
                + ", underlineThickness=" + getUnderlineThickness()
                + ", strikeoutPosition=" + getStrikeoutPosition()
                + ", strikeoutThickness=" + getStrikeoutThickness()
                + '}';
    }

    private static native long nCreateWithAsset(AssetManager assetManager, String path);
    private static native long nCreateWithFile(String path);
    private static native long nCreateFromStream(InputStream stream);

	private static native void nDispose(long nativeTypeface);

    private static native String nGetFamilyName(long nativeTypeface);
    private static native String nGetStyleName(long nativeTypeface);
    private static native String nGetFullName(long nativeTypeface);

    private static native int nGetWeight(long nativeTypeface);
    private static native int nGetWidth(long nativeTypeface);
    private static native int nGetSlope(long nativeTypeface);

    private static native int nGetVariationAxisCount(long nativeTypeface);
    private static native void nGetVariationAxes(long nativeTypeface, int[] tagsAndFlags, float[] values, String[] names);
    private static native int nGetNamedStyleCount(long nativeTypeface);
    private static native void nGetNamedStyles(long nativeTypeface, String[] styleNames, String[] postScriptNames, float[] coordinates);
    private static native int nGetPaletteEntryCount(long nativeTypeface);
    private static native void nGetPaletteEntryNames(long nativeTypeface, String[] names);
    private static native int nGetPredefinedPaletteCount(long nativeTypeface);
    private static native void nGetPredefinedPalettes(long nativeTypeface, String[] names, int[] flags, int[] colors);

    private static native long nGetVariationInstance(long nativeTypeface, float[] coordinates);
	private static native void nGetVariationCoordinates(long nativeTypeface, float[] coordinates);

    private static native long nGetColorInstance(long nativeTypeface, int[] colors);
    private static native void nGetAssociatedColors(long nativeTypeface, int[] colors);

    private static native byte[] nGetTableData(long nativeTypeface, int tableTag);

	private static native int nGetUnitsPerEm(long nativeTypeface);
	private static native int nGetAscent(long nativeTypeface);
	private static native int nGetDescent(long nativeTypeface);
    private static native int nGetLeading(long nativeTypeface);

	private static native int nGetGlyphCount(long nativeTypeface);
    private static native int nGetGlyphId(long nativeTypeface, int codePoint);
    private static native float nGetGlyphAdvance(long nativeTypeface, int glyphId, float typeSize, boolean vertical);
    private static native Path nGetGlyphPath(long nativeTypeface, int glyphId, float typeSize, float[] matrix);

	private static native void nGetBoundingBox(long nativeTypeface, Rect boundingBox);

	private static native int nGetUnderlinePosition(long nativeTypeface);
	private static native int nGetUnderlineThickness(long nativeTypeface);

    private static native int nGetStrikeoutPosition(long nativeTypeface);
    private static native int nGetStrikeoutThickness(long nativeTypeface);
}
