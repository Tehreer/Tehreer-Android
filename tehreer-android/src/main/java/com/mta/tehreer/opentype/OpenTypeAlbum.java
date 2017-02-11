/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

package com.mta.tehreer.opentype;

import com.mta.tehreer.internal.util.Constants;
import com.mta.tehreer.internal.util.RawInt32Floats;
import com.mta.tehreer.internal.util.RawInt32Points;
import com.mta.tehreer.internal.util.RawSizeValues;
import com.mta.tehreer.internal.util.RawUInt16Values;
import com.mta.tehreer.util.Disposable;
import com.mta.tehreer.util.FloatList;
import com.mta.tehreer.util.IntList;
import com.mta.tehreer.util.PointList;

/**
 * An <code>OpenTypeAlbum</code> object is a container for the results of text shaping. It is filled
 * by <code>OpenTypeArtist</code> to provide the information related to characters, their glyphs,
 * offsets, and advances. It can be safely accessed from multiple threads but only one thread should
 * be allowed to manipulate it at a time.
 */
public class OpenTypeAlbum implements Disposable {

    private static class Finalizable extends OpenTypeAlbum {

        private Finalizable(OpenTypeAlbum parent) {
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
     * Wraps an open type album object into a finalizable instance which is guaranteed to be
     * disposed automatically by the GC when no longer in use. After calling this method,
     * <code>dispose()</code> should not be called on either original object or returned object.
     * Calling <code>dispose()</code> on returned object will throw an
     * <code>UnsupportedOperationException</code>.
     * <p>
     * <strong>Note:</strong> The behaviour is undefined if an already disposed object is passed-in
     * as a parameter.
     *
     * @param openTypeAlbum The open type album object to wrap into a finalizable instance.
     * @return The finalizable instance of the passed-in open type album object.
     */
    public static OpenTypeAlbum finalizable(OpenTypeAlbum openTypeAlbum) {
        if (openTypeAlbum.getClass() == OpenTypeAlbum.class) {
            return new Finalizable(openTypeAlbum);
        }

        if (openTypeAlbum.getClass() != Finalizable.class) {
            throw new IllegalArgumentException(Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED);
        }

        return openTypeAlbum;
    }

    /**
     * Checks whether an open type album object is finalizable or not.
     *
     * @param openTypeAlbum The open type album object to check.
     * @return <code>true</code> if the passed-in open type album object is finalizable,
     *         <code>false</code> otherwise.
     */
    public static boolean isFinalizable(OpenTypeAlbum openTypeAlbum) {
        return (openTypeAlbum.getClass() == Finalizable.class);
    }

	long nativeAlbum;

    /**
     * Constructs an open type album object.
     */
	public OpenTypeAlbum() {
	    nativeAlbum = nativeCreate();
	}

    private OpenTypeAlbum(OpenTypeAlbum other) {
        this.nativeAlbum = other.nativeAlbum;
    }

    /**
     * Returns <code>true</code> if the text flows backward for this album.
     *
     * @return <code>true</code> if the text flows backward for this album, <code>false</code>
     *         otherwise.
     */
	public boolean isBackward() {
	    return nativeIsBackward(nativeAlbum);
	}

    /**
     * Returns the index to the first character of this album in source text.
     *
     * @return The index to the first character of this album in source text.
     */
	public int getCharStart() {
        return nativeGetCharStart(nativeAlbum);
    }

    /**
     * Returns the index after the last character of this album in source text.
     *
     * @return The index after the last character of this album in source text.
     */
    public int getCharEnd() {
        return nativeGetCharEnd(nativeAlbum);
    }

    /**
     * Returns the number of glyphs in this album.
     *
     * @return The number of glyphs in this album.
     */
	public int getGlyphCount() {
		return nativeGetGlyphCount(nativeAlbum);
	}

    /**
     * Returns a list of glyph codes in this album.
     *
     * <strong>Note:</strong> The returned list might exhibit undefined behavior if the album object
     * is disposed.
     *
     * @return A list of glyph codes in this album.
     */
    public IntList getGlyphCodes() {
        return new RawUInt16Values(nativeGetGlyphCodesPtr(nativeAlbum),
                                   nativeGetGlyphCount(nativeAlbum));
    }

    /**
     * Returns a list of glyph offsets in this album.
     *
     * <strong>Note:</strong> The returned list might exhibit undefined behavior if the album object
     * is disposed.
     *
     * @return A list of glyph offsets in this album.
     */
    public PointList getGlyphOffsets() {
        return new RawInt32Points(nativeGetGlyphOffsetsPtr(nativeAlbum),
                                  nativeGetGlyphCount(nativeAlbum),
                                  nativeGetSizeByEm(nativeAlbum));
    }

    /**
     * Returns a list of glyph advances in this album.
     *
     * <strong>Note:</strong> The returned list might exhibit undefined behavior if the album object
     * is disposed.
     *
     * @return A list of glyph advances in this album.
     */
    public FloatList getGlyphAdvances() {
        return new RawInt32Floats(nativeGetGlyphAdvancesPtr(nativeAlbum),
                                  nativeGetGlyphCount(nativeAlbum),
                                  nativeGetSizeByEm(nativeAlbum));
    }

    public IntList getCharToGlyphMap() {
        return new RawSizeValues(nativeGetCharToGlyphMapPtr(nativeAlbum),
                                 nativeGetCharCount(nativeAlbum));
    }

	@Override
	public void dispose() {
        nativeDispose(nativeAlbum);
    }

    @Override
    public String toString() {
        return "OpenTypeAlbum{isBackward=" + Boolean.toString(isBackward())
                + ", charStart=" + getCharStart()
                + ", charEnd=" + getCharEnd()
                + ", glyphCount=" + getGlyphCount()
                + ", glyphCodes=" + getGlyphCodes().toString()
                + ", glyphOffsets=" + getGlyphOffsets().toString()
                + ", glyphAdvances=" + getGlyphAdvances().toString()
                + ", charToGlyphMap=" + getCharToGlyphMap().toString()
                + "}";
    }

	private static native long nativeCreate();
	private static native void nativeDispose(long nativeAlbum);

	private static native boolean nativeIsBackward(long nativeAlbum);
    private static native float nativeGetSizeByEm(long nativeAlbum);
	private static native int nativeGetCharStart(long nativeAlbum);
	private static native int nativeGetCharEnd(long nativeAlbum);
    private static native int nativeGetCharCount(long nativeAlbum);
	private static native int nativeGetGlyphCount(long nativeAlbum);

    private static native long nativeGetGlyphCodesPtr(long nativeAlbum);
    private static native long nativeGetGlyphOffsetsPtr(long nativeAlbum);
    private static native long nativeGetGlyphAdvancesPtr(long nativeAlbum);
    private static native long nativeGetCharToGlyphMapPtr(long nativeAlbum);
}
