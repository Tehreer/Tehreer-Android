/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

import com.mta.tehreer.internal.util.Constants;
import com.mta.tehreer.internal.util.Sustain;
import com.mta.tehreer.opentype.SfntTag;
import com.mta.tehreer.util.Disposable;

import java.io.InputStream;

/**
 * The Typeface class specifies the typeface and intrinsic style of a font. This is used in the
 * renderer, along with optionally Renderer settings like textSize, textSkewX, textScaleX to specify
 * how text appears when drawn (and measured).
 */
public class Typeface implements Disposable {

    private static class Finalizable extends Typeface {

        private Finalizable(Typeface parent) {
            super(parent);
        }

        @Override
        public void dispose() {
            throw new UnsupportedOperationException(Constants.EXCEPTION_FINALIZABLE_OBJECT);
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                super.dispose();
            } finally {
                super.finalize();
            }
        }
    }

    /**
     * Wraps a typeface object into a finalizable instance which is guaranteed to be disposed
     * automatically by the GC when no longer in use. After calling this method,
     * <code>dispose()</code> should not be called on either original object or returned object.
     * Calling <code>dispose()</code> on returned object will throw an
     * <code>UnsupportedOperationException</code>.
     * <p>
     * <strong>Note:</strong> The behaviour is undefined if an already disposed object is passed-in
     * as a parameter.
     *
     * @param typeface The typeface object to wrap into a finalizable instance.
     * @return The finalizable instance of the passed-in typeface object.
     */
    public static Typeface finalizable(Typeface typeface) {
        if (typeface.getClass() == Typeface.class) {
            return new Finalizable(typeface);
        }

        if (typeface.getClass() != Finalizable.class) {
            throw new IllegalArgumentException(Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED);
        }

        return typeface;
    }

    /**
     * Checks whether a typeface object is finalizable or not.
     *
     * @param typeface The typeface object to check.
     * @return <code>true</code> if the passed-in typeface object is finalizable, <code>false</code>
     *         otherwise.
     */
    public static boolean isFinalizable(Typeface typeface) {
        return (typeface.getClass() == Finalizable.class);
    }

    @Sustain
    long nativeTypeface;

    /**
     * Creates a new typeface with the specified asset. The data of the asset is not copied into the
     * memory. Rather, it is directly read from the stream when needed. So the performance of
     * resulting typeface might be slower and should be used with precaution.
     *
     * @param assetManager The application's asset manager.
     * @param path The path of the font file in the assets directory.
     * @return The new typeface, or <code>null</code> if an error occurred while creating it.
     */
    public static Typeface createWithAsset(AssetManager assetManager, String path) {
        long nativeTypeface = nativeCreateWithAsset(assetManager, path);
        if (nativeTypeface != 0) {
            return new Typeface(nativeTypeface);
        }

        return null;
    }

    /**
     * Creates a new typeface with the specified file. The data for the font is directly read from
     * the file when needed.
     *
     * @param path The absolute path of the font file.
     * @return The new typeface, or <code>null</code> if an error occurred while creating it.
     */
    public static Typeface createWithFile(String path) {
        long nativeTypeface = nativeCreateWithFile(path);
        if (nativeTypeface != 0) {
            return new Typeface(nativeTypeface);
        }

        return null;
    }

    /**
     * Create a new typeface from the input stream by copying its data into a native memory buffer.
     * So it may take time to create the typeface if the stream holds larger data.
     *
     * @param stream The input stream that contains the data of the font.
     * @return The new typeface, or <code>null</code> if an error occurred while creating it.
     */
    public static Typeface createFromStream(InputStream stream) {
        long nativeTypeface = nativeCreateFromStream(stream);
        if (nativeTypeface != 0) {
            return new Typeface(nativeTypeface);
        }

        return null;
    }

	private Typeface(long nativeTypeface) {
	    this.nativeTypeface = nativeTypeface;
	}

    private Typeface(Typeface other) {
        this.nativeTypeface = other.nativeTypeface;
    }


    /**
     * Generates an array of bytes containing the data of the intended table.
     *
     * @param tableTag The tag of the table as an integer. It can be created from string by using
     *                 {@link SfntTag#make(String)} method.
     * @return An array of bytes containing the data of the table, or <code>null</code> if no such
     *         table exists.
     */
    public byte[] getTableData(int tableTag) {
        return nativeGetTableData(nativeTypeface, tableTag);
    }

    /**
     * Returns the number of font units per EM square for this typeface.
     *
     * @return The number of font units per EM square for this typeface.
     */
	public int getUnitsPerEm() {
		return nativeGetUnitsPerEm(nativeTypeface);
	}

    /**
     * Returns the typographic ascender of this typeface expressed in font units.
     *
     * @return The typographic ascender of this typeface expressed in font units.
     */
	public int getAscent() {
		return nativeGetAscent(nativeTypeface);
	}

    /**
     * Returns the typographic descender of this typeface expressed in font units.
     *
     * @return The typographic descender of this typeface expressed in font units.
     */
	public int getDescent() {
		return nativeGetDescent(nativeTypeface);
	}

    /**
     * Returns the typographic leading of this typeface expressed in font units.
     *
     * @return The typographic leading of this typeface expressed in font units.
     */
    public int getLeading() {
        return nativeGetLeading(nativeTypeface);
    }

    /**
     * Returns the number of glyphs in this typeface.
     *
     * @return The number of glyphs in this typeface.
     */
	public int getGlyphCount() {
	    return nativeGetGlyphCount(nativeTypeface);
	}

    public int getGlyphId(int codePoint) {
        return nativeGetGlyphId(nativeTypeface, codePoint);
    }

    public float getGlyphAdvance(int glyphId, float typeSize, boolean vertical) {
        return nativeGetGlyphAdvance(nativeTypeface, glyphId, typeSize, vertical);
    }

    public Path getGlyphPath(int glyphId, float typeSize, Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);

        return nativeGetGlyphPath(nativeTypeface, glyphId, typeSize, values);
    }

    /**
     * Returns the font bounding box expressed in font units. The box is large enough to contain any
     * glyph from the font.
     *
     * @return The font bounding box expressed in font units.
     */
	public Rect getBoundingBox() {
	    Rect boundingBox = new Rect();
	    nativeGetBoundingBox(nativeTypeface, boundingBox);

	    return boundingBox;
	}

    /**
     * Returns the position, in font units, of the underline for this typeface.
     *
     * @return The position, in font units, of the underline for this typeface.
     */
	public int getUnderlinePosition() {
	    return nativeGetUnderlinePosition(nativeTypeface);
	}

    /**
     * Returns the thickness, in font units, of the underline for this typeface.
     *
     * @return The thickness, in font units, of the underline for this typeface.
     */
	public int getUnderlineThickness() {
	    return nativeGetUnderlineThickness(nativeTypeface);
	}

    @Override
    public void dispose() {
        nativeDispose(nativeTypeface);
    }

    @Override
    public String toString() {
        return "Typeface{unitsPerEm=" + getUnitsPerEm()
                + ", ascent=" + getAscent()
                + ", descent=" + getDescent()
                + ", leading=" + getLeading()
                + ", glyphCount=" + getGlyphCount()
                + ", boundingBox=" + getBoundingBox().toString()
                + ", underlinePosition=" + getUnderlinePosition()
                + ", underlineThickness=" + getUnderlineThickness()
                + "}";
    }

    private static native long nativeCreateWithAsset(AssetManager assetManager, String path);
    private static native long nativeCreateWithFile(String path);
    private static native long nativeCreateFromStream(InputStream stream);
	private static native void nativeDispose(long nativeTypeface);

    private static native byte[] nativeGetTableData(long nativeTypeface, int tableTag);

	private static native int nativeGetUnitsPerEm(long nativeTypeface);
	private static native int nativeGetAscent(long nativeTypeface);
	private static native int nativeGetDescent(long nativeTypeface);
    private static native int nativeGetLeading(long nativeTypeface);

	private static native int nativeGetGlyphCount(long nativeTypeface);
    private static native int nativeGetGlyphId(long nativeTypeface, int codePoint);
    private static native float nativeGetGlyphAdvance(long nativeTypeface, int glyphId, float typeSize, boolean vertical);
    private static native Path nativeGetGlyphPath(long nativeTypeface, int glyphId, float typeSize, float[] matrix);

	private static native void nativeGetBoundingBox(long nativeTypeface, Rect boundingBox);

	private static native int nativeGetUnderlinePosition(long nativeTypeface);
	private static native int nativeGetUnderlineThickness(long nativeTypeface);
}
