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
import com.mta.tehreer.util.Disposable;

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

	private void verifyCharIndex(int charIndex) {
	    int textStart = getCharStart();
	    int textEnd = getCharEnd();

	    if (charIndex < textStart || charIndex >= textEnd) {
            throw new IllegalArgumentException("Char Index: " + charIndex
                                               + ", Text Range: [" + textStart + ".." + textEnd + ")");
        }
	}

	private void verifyCharRange(int fromIndex, int toIndex) {
	    int textStart = getCharStart();
        int textEnd = getCharEnd();

        if (fromIndex < textStart) {
            throw new IllegalArgumentException("From Index: " + fromIndex
                                               + ", Text Range: [" + textStart + ".." + textEnd + ")");
        }
        if (toIndex > textEnd) {
            throw new IllegalArgumentException("To Index: " + toIndex
                                               + ", Text Range: [" + textStart + ".." + textEnd + ")");
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("From Index: " + fromIndex
                                               + ", To Index: " + toIndex);
        }
	}

	private void verifyGlyphIndex(int glyphIndex) {
		if (glyphIndex < 0 || glyphIndex >= getGlyphCount()) {
            throw new IndexOutOfBoundsException("Glyph Index: " + glyphIndex);
        }
	}

	private void verifyGlyphRange(int fromIndex, int toIndex) {
	    if (fromIndex < 0) {
            throw new IllegalArgumentException("From Index: " + fromIndex);
        }
        if (toIndex > getGlyphCount()) {
            throw new IllegalArgumentException("To Index: " + toIndex
                                               + "Glyph Count: " + getGlyphCount());
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("From Index: " + fromIndex
                                               + ", To Index: " + toIndex);
        }
	}

	private void verifyCapacity(String paramName, int expectedCapacity, int actualCapacity) {
	    if (actualCapacity < expectedCapacity) {
            throw new IllegalArgumentException("'" + paramName + "' array cannot hold " + expectedCapacity + " elements");
        }
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
     * Returns the glyph id at the specified index in this album.
     *
     * @param glyphIndex The index of the glyph record.
     * @return The glyph id at the specified index in this album.
     *
     * @throws IndexOutOfBoundsException if <code>glyphIndex</code> is negative, or
     *         <code>glyphIndex</code> is greater than or equal to {@link #getGlyphCount()}
     */
	public int getGlyphId(int glyphIndex) {
		verifyGlyphIndex(glyphIndex);
		return nativeGetGlyphId(nativeAlbum, glyphIndex);
	}

    /**
     * Returns the glyph's x- offset at the specified index in this album.
     *
     * @param glyphIndex The index of the glyph record.
     * @return The glyph's x- offset at the specified index in this album.
     *
     * @throws IndexOutOfBoundsException if <code>glyphIndex</code> is negative, or
     *         <code>glyphIndex</code> is greater than or equal to {@link #getGlyphCount()}
     */
	public int getGlyphXOffset(int glyphIndex) {
		verifyGlyphIndex(glyphIndex);
		return nativeGetGlyphXOffset(nativeAlbum, glyphIndex);
	}

    /**
     * Returns the glyph's y- offset at the specified index in this album.
     *
     * @param glyphIndex The index of the glyph record.
     * @return The glyph's y- offset at the specified index in this album.
     *
     * @throws IndexOutOfBoundsException if <code>glyphIndex</code> is negative, or
     *         <code>glyphIndex</code> is greater than or equal to {@link #getGlyphCount()}
     */
	public int getGlyphYOffset(int glyphIndex) {
		verifyGlyphIndex(glyphIndex);
		return nativeGetGlyphYOffset(nativeAlbum, glyphIndex);
	}

    /**
     * Returns the glyph's advance at the specified index in this album.
     *
     * @param glyphIndex The index of the glyph record.
     * @return The glyph's advance at the specified index in this album.
     *
     * @throws IndexOutOfBoundsException if <code>glyphIndex</code> is negative, or
     *         <code>glyphIndex</code> is greater than or equal to {@link #getGlyphCount()}
     */
	public int getGlyphAdvance(int glyphIndex) {
		verifyGlyphIndex(glyphIndex);
		return nativeGetGlyphAdvance(nativeAlbum, glyphIndex);
	}

    /**
     * Returns the index of the first glyph associated with the character at the specified index in
     * source text.
     *
     * @param charIndex The index of the character in source text.
     * @return The index of the first glyph associated with the character at the specified index in
     *         source text.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> is less than
     *         {@link #getCharStart()}, or greater than or equal to {@link #getCharEnd()}
     */
	public int getCharGlyphIndex(int charIndex) {
        verifyCharIndex(charIndex);
        return nativeGetCharGlyphIndex(nativeAlbum, charIndex);
    }

    /**
     * Copies glyph infos in specified range to passed-in arrays.
     *
     * @param fromIndex The index of the first element (inclusive) to be copied.
     * @param toIndex The index of the last element (exclusive) to be copied.
     * @param scaleFactor The scale factor to apply on glyph offsets and advances before copying.
     * @param glyphIDs The array that receives the glyph ids in specified range.
     * @param xOffsets The array that receives the scaled x- offsets in specified range.
     * @param yOffsets The array that receives the scaled y- offsets in specified range.
     * @param advances The array that receives the scaled advances in specified range.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *           <li><code>fromIndex</code> is negative</li>
     *           <li><code>toIndex</code> is greater than {@link #getGlyphCount()}</li>
     *           <li><code>fromIndex</code> is greater than <code>toIndex</code></li>
     *           <li>Any of the non-null passed-in array does not have the capacity to hold
     *               <code>toIndex - fromIndex</code> elements</li>
     *         </ul>
     */
    public void copyGlyphInfos(int fromIndex, int toIndex, float scaleFactor,
                               int[] glyphIDs, float[] xOffsets, float[] yOffsets, float[] advances) {
        verifyGlyphRange(fromIndex, toIndex);
        int glyphCapacity = toIndex - fromIndex;
        if (glyphIDs != null) {
            verifyCapacity("glyphIDs", glyphCapacity, glyphIDs.length);
        }
        if (xOffsets != null) {
            verifyCapacity("xOffsets", glyphCapacity, xOffsets.length);
        }
        if (yOffsets != null) {
            verifyCapacity("yOffsets", glyphCapacity, yOffsets.length);
        }
        if (advances != null) {
            verifyCapacity("advances", glyphCapacity, advances.length);
        }

        nativeCopyGlyphInfos(nativeAlbum, fromIndex, toIndex, scaleFactor,
                             glyphIDs, xOffsets, yOffsets, advances);
    }

    /**
     * Copies glyph indexes of characters in specified range to passed-in array.
     *
     * @param fromIndex The index of the first character (inclusive) in source text.
     * @param toIndex The index of the last character (exclusive) in source text.
     * @param charGlyphIndexes The array that receives the glyph indexes in specified range.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *           <li><code>fromIndex</code> is less than {@link #getCharStart()}</li>
     *           <li><code>toIndex</code> is greater than {@link #getCharEnd()}</li>
     *           <li><code>fromIndex</code> is greater than <code>toIndex</code></li>
     *           <li><code>charGlyphIndexes</code> array does not have the capacity to hold
     *               <code>toIndex - fromIndex</code> elements</li>
     *         </ul>
     */
    public void copyCharGlyphIndexes(int fromIndex, int toIndex, int[] charGlyphIndexes) {
        verifyCharRange(fromIndex, toIndex);

        if (charGlyphIndexes != null) {
            verifyCapacity("charGlyphIndexes", fromIndex - toIndex, charGlyphIndexes.length);
            nativeCopyCharGlyphIndexes(nativeAlbum, fromIndex, toIndex, charGlyphIndexes);
        }
    }

	@Override
	public void dispose() {
        nativeDispose(nativeAlbum);
    }

    @Override
    public String toString() {
        StringBuilder glyphIdsBuilder = new StringBuilder();
        StringBuilder xOffsetsBuilder = new StringBuilder();
        StringBuilder yOffsetsBuilder = new StringBuilder();
        StringBuilder advancesBuilder = new StringBuilder();

        glyphIdsBuilder.append("[");
        xOffsetsBuilder.append("[");
        yOffsetsBuilder.append("[");
        advancesBuilder.append("[");

        int glyphCount = getGlyphCount();
        for (int i = 0; i < glyphCount; i++) {
            glyphIdsBuilder.append(nativeGetGlyphId(nativeAlbum, i));
            xOffsetsBuilder.append(nativeGetGlyphXOffset(nativeAlbum, i));
            yOffsetsBuilder.append(nativeGetGlyphYOffset(nativeAlbum, i));
            advancesBuilder.append(nativeGetGlyphAdvance(nativeAlbum, i));

            if (i < glyphCount - 1) {
                glyphIdsBuilder.append(", ");
                xOffsetsBuilder.append(", ");
                yOffsetsBuilder.append(", ");
                advancesBuilder.append(", ");
            }
        }

        glyphIdsBuilder.append("]");
        xOffsetsBuilder.append("]");
        yOffsetsBuilder.append("]");
        advancesBuilder.append("]");

        StringBuilder charToGlyphMapBuilder = new StringBuilder();
        charToGlyphMapBuilder.append("[");

        int charStart = getCharStart();
        int charEnd = getCharEnd();
        for (int i = charStart; i < charEnd; i++) {
            charToGlyphMapBuilder.append(nativeGetCharGlyphIndex(nativeAlbum, i));
            if (i < charEnd - 1) {
                charToGlyphMapBuilder.append(", ");
            }
        }

        charToGlyphMapBuilder.append("]");

        return "OpenTypeAlbum{isBackward=" + Boolean.toString(isBackward())
                + ", charStart=" + charStart
                + ", charEnd=" + charEnd
                + ", glyphCount=" + glyphCount
                + ", glyphIds=" + glyphIdsBuilder.toString()
                + ", glyphXOffsets=" + xOffsetsBuilder.toString()
                + ", glyphYOffsets=" + yOffsetsBuilder.toString()
                + ", glyphAdvances=" + advancesBuilder.toString()
                + ", charToGlyphMap=" + charToGlyphMapBuilder.toString()
                + "}";
    }

	private static native long nativeCreate();
	private static native void nativeDispose(long nativeAlbum);

	private static native boolean nativeIsBackward(long nativeAlbum);
	private static native int nativeGetCharStart(long nativeAlbum);
	private static native int nativeGetCharEnd(long nativeAlbum);
	private static native int nativeGetGlyphCount(long nativeAlbum);

	private static native int nativeGetGlyphId(long nativeAlbum, int glyphIndex);
	private static native int nativeGetGlyphXOffset(long nativeAlbum, int glyphIndex);
	private static native int nativeGetGlyphYOffset(long nativeAlbum, int glyphIndex);
	private static native int nativeGetGlyphAdvance(long nativeAlbum, int glyphIndex);
	private static native int nativeGetCharGlyphIndex(long nativeAlbum, int charIndex);

	private static native void nativeCopyGlyphInfos(long nativeAlbum,
	        int fromIndex, int toIndex, float scaleFactor,
            int[] glyphIDs, float[] xOffsets, float[] yOffsets, float[] advances);
	private static native void nativeCopyCharGlyphIndexes(long nativeAlbum,
            int fromIndex, int toIndex, int[] charGlyphIndexes);
}
