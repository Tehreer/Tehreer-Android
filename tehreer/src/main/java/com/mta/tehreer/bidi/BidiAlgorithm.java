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

package com.mta.tehreer.bidi;

import com.mta.tehreer.internal.util.Constants;
import com.mta.tehreer.util.Disposable;

/**
 * This class implements Unicode Bidirectional Algorithm available at
 * http://www.unicode.org/reports/tr9.
 * <p>
 * A <code>BidiAlgorithm</code> object provides information related to individual paragraphs in
 * source text by applying rule P1. It can be used to create paragraph objects by explicitly
 * specifying the paragraph level or deriving it from rules P2 and P3. Once a paragraph object is
 * created, embedding levels of characters can be queried from it.
 */
public class BidiAlgorithm implements Disposable {

    /**
     * Maximum explicit embedding level.
     */
    public static final byte MAX_LEVEL = 125;

    private static class Finalizable extends BidiAlgorithm {

        private Finalizable(BidiAlgorithm parent) {
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
     * <strong>Note:</strong> The behaviour is undefined if an already disposed object is passed-in
     * as a parameter.
     *
     * @param bidiAlgorithm The bidi algorithm object to wrap into a finalizable instance.
     *
     * @return The finalizable instance of the passed-in bidi algorithm object.
     */
    public static BidiAlgorithm finalizable(BidiAlgorithm bidiAlgorithm) {
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
    public static boolean isFinalizable(BidiAlgorithm bidiAlgorithm) {
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
     * @throws IllegalArgumentException if <code>text</code> is <code>null</code> or empty.
     */
    public BidiAlgorithm(String text) {
        if (text == null || text.length() == 0) {
            throw new IllegalArgumentException("Text is null or empty");
        }

        this.nativeBuffer = BidiBuffer.create(text);
        this.nativeAlgorithm = nativeCreate(nativeBuffer);
        this.text = text;
    }

    private BidiAlgorithm(BidiAlgorithm other) {
        this.nativeBuffer = other.nativeBuffer;
        this.nativeAlgorithm = other.nativeAlgorithm;
        this.text = other.text;
    }

    private void verifyTextRange(int charStart, int charEnd) {
        if (charStart < 0) {
            throw new IllegalArgumentException("Char Start: " + charStart);
        }
        if (charEnd > text.length()) {
            throw new IllegalArgumentException("Char End: " + charEnd
                                               + ", Text Length: " + text.length());
        }
        if (charStart >= charEnd) {
            throw new IllegalArgumentException("Bad Range: [" + charStart + ".." + charEnd + ")");
        }
    }

    private void verifyBaseLevel(byte baseLevel) {
        if (baseLevel < 0 || baseLevel > MAX_LEVEL) {
            throw new IllegalArgumentException("Base Level: " + baseLevel);
        }
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
        verifyTextRange(charStart, charEnd);
        return nativeGetParagraphBoundary(nativeAlgorithm, charStart, charEnd);
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
     * @throws NullPointerException if <code>baseDirection</code> is <code>null</code>.
     */
    public BidiParagraph createParagraph(int charStart, int charEnd, BaseDirection baseDirection) {
        verifyTextRange(charStart, charEnd);

        return new BidiParagraph(nativeBuffer,
                                 nativeCreateParagraph(nativeAlgorithm,
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
     *           <li><code>baseLevel</code> is greater than <code>MAX_LEVEL</code></li>
     *         </ul>
     */
    public BidiParagraph createParagraph(int charStart, int charEnd, byte baseLevel) {
        verifyTextRange(charStart, charEnd);
        verifyBaseLevel(baseLevel);

        return new BidiParagraph(nativeBuffer,
                                 nativeCreateParagraph(nativeAlgorithm,
                                                       charStart, charEnd, baseLevel));
    }

    @Override
    public void dispose() {
        nativeDispose(nativeAlgorithm);
        BidiBuffer.release(nativeBuffer);
    }

    @Override
    public String toString() {
        return "BidiAlgorithm{text=" + text + "}";
    }

    private static native long nativeCreate(long nativeBuffer);
    private static native void nativeDispose(long nativeAlgorithm);

    private static native int nativeGetParagraphBoundary(long nativeAlgorithm, int charStart, int charEnd);
    private static native long nativeCreateParagraph(long nativeAlgorithm, int charStart, int charEnd, int baseLevel);
}
