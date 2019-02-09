/*
 * Copyright (C) 2016-2019 Muhammad Tayyab Akram
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

import com.mta.tehreer.font.VariationAxis;
import com.mta.tehreer.internal.JniBridge;
import com.mta.tehreer.internal.sfnt.DataTable;
import com.mta.tehreer.internal.sfnt.tables.fvar.FontVariationsTable;
import com.mta.tehreer.internal.sfnt.tables.fvar.VariationAxisRecord;
import com.mta.tehreer.sfnt.SfntTag;
import com.mta.tehreer.sfnt.tables.NameTable;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private @Nullable List<VariationAxis> variationAxes;
    private @NonNull String familyName = "";
    private @NonNull String styleName = "";
    private @NonNull String fullName = "";

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

        setupVariations();
	    setupNames();
	}

    private void setupVariations() {
        final byte[] fvarData = getTableData(SfntTag.make("fvar"));
        if (fvarData == null) {
            return;
        }

        NameTable nameTable = NameTable.from(this);
        FontVariationsTable fvarTable = new FontVariationsTable(new DataTable(fvarData));
        VariationAxisRecord[] axisRecords = fvarTable.axisRecords();

        variationAxes = new ArrayList<>(axisRecords.length);

        for (VariationAxisRecord axisRecord : axisRecords) {
            final int flags = axisRecord.flags();
            if ((flags & VariationAxisRecord.FLAG_HIDDEN_AXIS) != 0) {
                continue;
            }

            final int axisTag = axisRecord.axisTag();
            final int axisNameId = axisRecord.axisNameId();
            final float defaultValue = axisRecord.defaultValue();
            final float minValue = axisRecord.minValue();
            final float maxValue = axisRecord.maxValue();

            final int nameRecordIndex = searchEnglishNameRecordIndex(axisNameId);
            String axisName = "";

            if (nameRecordIndex > -1) {
                NameTable.Record nameRecord = nameTable.recordAt(nameRecordIndex);
                axisName = nameRecord.string();
            }

            variationAxes.add(VariationAxis.of(axisTag, axisName,
                                               defaultValue, minValue, maxValue));
        }
    }

	private void setupNames() {
        final int[] nameRecordIndexes = new int[3];
        nGetNameRecordIndexes(nativeTypeface, nameRecordIndexes);

        final int familyNameIndex = nameRecordIndexes[0];
        final int styleNameIndex = nameRecordIndexes[1];
        final int fullNameIndex = nameRecordIndexes[2];

        NameTable nameTable = NameTable.from(this);

        if (familyNameIndex != -1) {
            String recordString = nameTable.recordAt(familyNameIndex).string();
            if (recordString != null) {
                familyName = recordString;
            }
        }
        if (styleNameIndex != -1) {
            String recordString = nameTable.recordAt(styleNameIndex).string();
            if (recordString != null) {
                styleName = nameTable.recordAt(styleNameIndex).string();
            }
        }
        if (fullNameIndex != -1) {
            String recordString = nameTable.recordAt(fullNameIndex).string();
            if (recordString != null) {
                fullName = nameTable.recordAt(fullNameIndex).string();
            }
        } else {
            if (!familyName.isEmpty()) {
                fullName = familyName;
                if (!styleName.isEmpty()) {
                    fullName += ' ' + styleName;
                }
            } else {
                fullName = styleName;
            }
        }
    }

    public @Nullable List<VariationAxis> getVariationAxes() {
        if (variationAxes != null) {
            return Collections.unmodifiableList(variationAxes);
        }

        return null;
    }

    /**
     * Returns the family name of this typeface.
     *
     * @return The family name of this typeface.
     */
    public String getFamilyName() {
        return familyName;
    }

    /**
     * Returns the style name of this typeface.
     *
     * @return The style name of this typeface.
     */
    public String getStyleName() {
        return styleName;
    }

    /**
     * Returns the full name of this typeface.
     *
     * @return The full name of this typeface.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the typographic weight of this typeface. The weight value determines the thickness
     * associated with a given character in a typeface.
     *
     * @return The typographic weight of this typeface.
     */
    public @NonNull TypeWeight getWeight() {
        final int weight = nGetWeight(nativeTypeface);

        return TypeWeight.valueOf(weight);
    }

    /**
     * Returns the typographic width of this typeface. The width value determines whether a typeface
     * is expanded or condensed when it is displayed.
     *
     * @return The typographic width of this typeface.
     */
    public @NonNull TypeWidth getWidth() {
        final int width = nGetWidth(nativeTypeface);

        return TypeWidth.valueOf(width);
    }

    /**
     * Returns the typographic slope of this typeface. The slope value determines whether a typeface
     * is plain or slanted when it is displayed.
     *
     * @return The typographic slope of this typeface.
     */
    public @NonNull TypeSlope getSlope() {
        final int slope = nGetSlope(nativeTypeface);

        return TypeSlope.valueOf(slope);
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

    int searchEnglishNameRecordIndex(int nameId) {
        return nSearchEnglishNameRecordIndex(nativeTypeface, nameId);
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
                + ", boundingBox=" + getBoundingBox().toString()
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

    private static native byte[] nGetTableData(long nativeTypeface, int tableTag);
    private static native int nSearchEnglishNameRecordIndex(long nativeTypeface, int nameId);
    private static native void nGetNameRecordIndexes(long nativeTypeface, int[] indexes);

    private static native int nGetWeight(long nativeTypeface);
    private static native int nGetWidth(long nativeTypeface);
    private static native int nGetSlope(long nativeTypeface);

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
