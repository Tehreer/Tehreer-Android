/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.unicode;

import android.support.annotation.NonNull;

import com.mta.tehreer.Disposable;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.internal.Constants;
import com.mta.tehreer.internal.JniBridge;
import com.mta.tehreer.internal.collections.UInt8BufferIntList;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;
import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * This class implements Unicode Bidirectional Algorithm available at
 * <a href="http://www.unicode.org/reports/tr9">http://www.unicode.org/reports/tr9</a>.
 * <p>
 * A <code>BidiAlgorithm</code> object provides information related to individual paragraphs in
 * source text by applying rule P1. It can be used to create paragraph objects by explicitly
 * specifying the paragraph level or deriving it from rules P2 and P3. Once a paragraph object is
 * created, embedding levels of characters can be queried from it.
 */
public class BidiAlgorithm implements Disposable {
    static {
        JniBridge.loadLibrary();
    }

    /**
     * Maximum explicit embedding level.
     */
    public static final byte MAX_LEVEL = 125;

    private static final class Finalizable extends BidiAlgorithm {
        Finalizable(@NonNull BidiAlgorithm parent) {
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
     * Wraps a bidi algorithm object into a finalizable instance which is guaranteed to be disposed
     * automatically by the GC when no longer in use. After calling this method,
     * <code>dispose()</code> should not be called on either original object or returned object.
     * Calling <code>dispose()</code> on returned object will throw an
     * <code>UnsupportedOperationException</code>.
     * <p>
     * <strong>Note:</strong> The behavior is undefined if the passed-in object is already disposed
     * or wrapped into another finalizable instance.
     *
     * @param bidiAlgorithm The bidi algorithm object to wrap into a finalizable instance.
     *
     * @return The finalizable instance of the passed-in bidi algorithm object.
     */
    public static @NonNull BidiAlgorithm finalizable(@NonNull BidiAlgorithm bidiAlgorithm) {
        if (bidiAlgorithm.getClass() == BidiAlgorithm.class) {
            return new Finalizable(bidiAlgorithm);
        }

        if (bidiAlgorithm.getClass() != Finalizable.class) {
            throw new IllegalArgumentException(Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED);
        }

        return bidiAlgorithm;
    }

    /**
     * Checks whether a bidi algorithm object is finalizable or not.
     *
     * @param bidiAlgorithm The bidi algorithm object to check.
     *
     * @return <code>true</code> if the passed-in bidi algorithm object is finalizable,
     *         <code>false</code> otherwise.
     */
    public static boolean isFinalizable(@NonNull BidiAlgorithm bidiAlgorithm) {
        return (bidiAlgorithm.getClass() == Finalizable.class);
    }

    long nativeBuffer;
    long nativeAlgorithm;
    private final String text;

    /**
     * Constructs a bidi algorithm object for the given text.
     *
     * @param text The text to apply unicode bidirectional algorithm on.
     *
     * @throws IllegalArgumentException if <code>text</code> is empty.
     */
    public BidiAlgorithm(@NonNull String text) {
        checkNotNull(text, "text");
        checkArgument(text.length() > 0, "Text is empty");

        this.nativeBuffer = BidiBuffer.create(text);
        this.nativeAlgorithm = nCreate(nativeBuffer);
        this.text = text;
    }

    BidiAlgorithm(@NonNull BidiAlgorithm other) {
        this.nativeBuffer = other.nativeBuffer;
        this.nativeAlgorithm = other.nativeAlgorithm;
        this.text = other.text;
    }

    private void checkSubRange(int charStart, int charEnd) {
        checkArgument(charStart >= 0, "Char Start: " + charStart);
        checkArgument(charEnd <= text.length(), "Char End: " + charEnd + ", Text Length: " + text.length());
        checkArgument(charEnd > charStart, "Bad Range: [" + charStart + ", " + charEnd + ')');
    }

    /**
     * Returns a list containing the bidi classes of all characters in source text. The valid bidi
     * class values are available in {@link BidiClass} as static constants.
     *
     * @return A list containing the bidi classes of all characters in source text.
     */
    public @NonNull IntList getCharBidiClasses() {
        return new UInt8BufferIntList(this,
                                      nGetCharBidiClassesPtr(nativeAlgorithm),
                                      text.length());
    }

    /**
     * Returns the boundary of the first paragraph within the given range.
     * <p>
     * The boundary of the paragraph occurs after a character whose bidirectional type is Paragraph
     * Separator (B), or the <code>charEnd</code> if no such character exists before it. The
     * exception to this rule is when a Carriage Return (CR) is followed by a Line Feed (LF). Both
     * CR and LF are paragraph separators, but in that case, the boundary of the paragraph is
     * considered after LF character.
     *
     * @param charStart The index to the first character of the paragraph in source text.
     * @param charEnd The suggested index after the last character of the paragraph in source text.
     * @return The boundary of the first paragraph within the given range.
     *
     * @throws IllegalArgumentException if <code>charStart</code> is negative, or
     *         <code>charEnd</code> is greater than the length of source text, or
     *         <code>charStart</code> is greater than or equal to <code>charEnd</code>.
     */
    public int getParagraphBoundary(int charStart, int charEnd) {
        checkSubRange(charStart, charEnd);

        return nGetParagraphBoundary(nativeAlgorithm, charStart, charEnd);
    }

    /**
     * Creates a paragraph object processed with Unicode Bidirectional Algorithm.
     * <p>
     * This method processes only first paragraph starting at <code>charStart</code> and ending at
     * either <code>charEnd</code> or some character before it, in accordance with Rule P1 of
     * Unicode Bidirectional Algorithm.
     * <p>
     * The paragraph level is determined by applying Rules P2-P3 and embedding levels are resolved
     * by applying Rules X1-I2.
     *
     * @param charStart The index to the first character of the paragraph in source text.
     * @param charEnd The suggested index after the last character of the paragraph in source text.
     * @param baseDirection The base direction of the paragraph.
     * @return A paragraph object processed with Unicode Bidirectional Algorithm.
     *
     * @throws IllegalArgumentException if <code>charStart</code> is negative, or
     *         <code>charEnd</code> is greater than the length of source text, or
     *         <code>charStart</code> is greater than or equal to <code>charEnd</code>.
     */
    public @NonNull BidiParagraph createParagraph(int charStart, int charEnd, @NonNull BaseDirection baseDirection) {
        checkSubRange(charStart, charEnd);

        return new BidiParagraph(nativeBuffer,
                                 nCreateParagraph(nativeAlgorithm,
                                                  charStart, charEnd, baseDirection.value));
    }

    /**
     * Creates a paragraph object processed with Unicode Bidirectional Algorithm.
     * <p>
     * This method processes only first paragraph starting at <code>charStart</code> and ending at
     * either <code>charEnd</code> or some character before it, in accordance with Rule P1 of
     * Unicode Bidirectional Algorithm.
     * <p>
     * The paragraph level is overridden by <code>baseLevel</code> parameter and embedding levels
     * are resolved by applying Rules X1-I2.
     *
     * @param charStart The index to the first character of the paragraph in source text.
     * @param charEnd The suggested index after the last character of the paragraph in source text.
     * @param baseLevel Base level to override.
     * @return A paragraph object processed with Unicode Bidirectional Algorithm.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *         <ul>
     *           <li><code>charStart</code> is negative</li>
     *           <li><code>charEnd</code> is greater than the length of source text</li>
     *           <li><code>charStart</code> is greater than or equal to <code>charEnd</code></li>
     *           <li><code>baseLevel</code> is less than zero</li>
     *           <li><code>baseLevel</code> is greater than {@link #MAX_LEVEL}</li>
     *         </ul>
     */
    public @NonNull BidiParagraph createParagraph(int charStart, int charEnd, byte baseLevel) {
        checkSubRange(charStart, charEnd);
        checkArgument(baseLevel >= 0 && baseLevel <= MAX_LEVEL, "Base Level: " + baseLevel);

        return new BidiParagraph(nativeBuffer,
                                 nCreateParagraph(nativeAlgorithm,
                                                  charStart, charEnd, baseLevel));
    }

    @Override
    public void dispose() {
        nDispose(nativeAlgorithm);
        BidiBuffer.release(nativeBuffer);
    }

    @Override
    public String toString() {
        return "BidiAlgorithm{text=" + text
                + ", charBidiClasses=" + getCharBidiClasses().toString()
                + "}";
    }

    private static native long nCreate(long nativeBuffer);
    private static native void nDispose(long nativeAlgorithm);

    private static native long nGetCharBidiClassesPtr(long nativeAlgorithm);
    private static native int nGetParagraphBoundary(long nativeAlgorithm, int charStart, int charEnd);
    private static native long nCreateParagraph(long nativeAlgorithm, int charStart, int charEnd, int baseLevel);
}
