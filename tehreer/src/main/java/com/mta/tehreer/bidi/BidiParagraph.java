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
 * A <code>BidiParagraph</code> object represents a single paragraph of text processed with rules
 * X1-I2 of Unicode Bidirectional Algorithm. It contains the resolved embedding levels of all the
 * characters of a paragraph and provides the facility to query them or iterate over their runs.
 */
public class BidiParagraph implements Disposable {

    private static class Finalizable extends BidiParagraph {

        private Finalizable(BidiParagraph parent) {
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
     * Wraps a bidi paragraph object into a finalizable instance which is guaranteed to be disposed
     * automatically by the GC when no longer in use. After calling this method,
     * <code>dispose()</code> should not be called on either original object or returned object.
     * Calling <code>dispose()</code> on returned object will throw an
     * <code>UnsupportedOperationException</code>.
     * <p>
     * <strong>Note:</strong> The behaviour is undefined if an already disposed object is passed-in
     * as a parameter.
     *
     * @param bidiParagraph The bidi paragraph object to wrap into a finalizable instance.
     *
     * @return The finalizable instance of the passed-in bidi paragraph object.
     */
    public static BidiParagraph finalizable(BidiParagraph bidiParagraph) {
        if (bidiParagraph.getClass() == BidiParagraph.class) {
            return new Finalizable(bidiParagraph);
        }

        if (bidiParagraph.getClass() != Finalizable.class) {
            throw new IllegalArgumentException(Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED);
        }

        return bidiParagraph;
    }

    /**
     * Checks whether a paragraph object is finalizable or not.
     *
     * @param bidiParagraph The paragraph object to check.
     *
     * @return <code>true</code> if the passed-in bidi paragraph object is finalizable,
     *         <code>false</code> otherwise.
     */
    public static boolean isFinalizable(BidiParagraph bidiParagraph) {
        return (bidiParagraph.getClass() == Finalizable.class);
    }

    long nativeBuffer;
	long nativeParagraph;

	BidiParagraph(long nativeBuffer, long nativeParagraph) {
        this.nativeBuffer = BidiBuffer.retain(nativeBuffer);
	    this.nativeParagraph = nativeParagraph;
	}

    private BidiParagraph(BidiParagraph other) {
        this.nativeBuffer = other.nativeBuffer;
        this.nativeParagraph = other.nativeParagraph;
    }

    /**
     * Returns the index to the first character of this paragraph in source text.
     *
     * @return The index to the first character of this paragraph in source text.
     */
    public int getCharStart() {
		return nativeGetCharStart(nativeParagraph);
	}

    /**
     * Returns the index after the last character of this paragraph in source text.
     *
     * @return The index after the last character of this paragraph in source text.
     */
	public int getCharEnd() {
        return nativeGetCharEnd(nativeParagraph);
	}

    /**
     * Returns the base level of this paragraph.
     *
     * @return The base level of this paragraph.
     */
	public byte getBaseLevel() {
		return nativeGetBaseLevel(nativeParagraph);
	}

    /**
     * Notifies the given consumer about each run of this paragraph until all runs have been
     * processed or <code>stop()</code> has been called by the consumer.
     *
     * @param consumer The consumer to notify about each run.
     *
     * @throws NullPointerException if <code>consumer</code> is <code>null</code>.
     */
	public void iterateRuns(BidiRunConsumer consumer) {
        BidiRun bidiRun = nativeGetRun(nativeParagraph, 0);
        int paragraphOffset = bidiRun.getCharStart();
        int levelIndex;

        do {
            consumer.accept(bidiRun);
            if (consumer.isStopped) {
                break;
            }

            levelIndex = bidiRun.getCharEnd() - paragraphOffset;
        } while ((bidiRun = nativeGetRun(nativeParagraph, levelIndex)) != null);
    }

    /**
     * Returns the level of a character at the specified index in source text.
     *
     * @param charIndex The index of a character in source text.
     * @return The level of the character at <code>charIndex</code> in source text.
     *
     * @throws IllegalArgumentException if <code>charIndex</code> does not lie within the range of
     *         this paragraph.
     */
	public byte getCharLevel(int charIndex) {
	    int paragraphStart = getCharStart();
	    int paragraphEnd = getCharEnd();

        if (charIndex < paragraphStart || charIndex >= paragraphEnd) {
            throw new IllegalArgumentException("Char Index: " + charIndex
                                               + ", Paragraph Range: [" + paragraphStart + ".." + paragraphEnd + ")");
        }

	    return nativeGetLevel(nativeParagraph, charIndex - paragraphStart);
	}

    /**
     * Creates a line object of specified range by applying Rules L1-L2 of Unicode Bidirectional
     * Algorithm.
     *
     * @param charStart The index to the first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @return A line object processed with Rules L1-L2 of Unicode Bidirectional Algorithm.
     *
     * @throws IllegalArgumentException if <code>charStart</code> is less than paragraph start, or
     *         <code>charEnd</code> is greater than paragraph end, or <code>charStart</code> is
     *         greater than or equal to <code>charEnd</code>.
     */
	public BidiLine createLine(int charStart, int charEnd) {
        int paragraphStart = getCharStart();
        int paragraphEnd = getCharEnd();

        if (charStart < paragraphStart) {
            throw new IllegalArgumentException("Char Start: " + charStart
                                               + ", Paragraph Range: [" + paragraphStart + ".." + paragraphEnd + ")");
        }
        if (charEnd > paragraphEnd) {
            throw new IllegalArgumentException("Char End: " + charEnd
                                               + ", Paragraph Range: [" + paragraphStart + ".." + paragraphEnd + ")");
        }
        if (charStart >= charEnd) {
            throw new IllegalArgumentException("Bad Range: [" + charStart + ".." + charEnd + ")");
        }

        return new BidiLine(nativeBuffer,
                            nativeCreateLine(nativeParagraph, charStart, charEnd));
    }

    @Override
    public void dispose() {
        nativeDispose(nativeParagraph);
        BidiBuffer.release(nativeBuffer);
    }

    @Override
    public String toString() {
        StringBuilder levelsBuilder = new StringBuilder();
        levelsBuilder.append("[");

        int levelCount = getCharEnd() - getCharStart();
        for (int i = 0; i < levelCount; i++) {
            levelsBuilder.append(nativeGetLevel(nativeParagraph, i));
            if (i < levelCount - 1) {
                levelsBuilder.append(", ");
            }
        }

        levelsBuilder.append("]");

        return "BidiParagraph{charStart=" + getCharStart()
                + ", charEnd=" + getCharEnd()
                + ", baseLevel=" + getBaseLevel()
                + ", charLevels=" + levelsBuilder.toString()
                + "}";
    }

	private static native void nativeDispose(long nativeParagraph);

	private static native int nativeGetCharStart(long nativeParagraph);
	private static native int nativeGetCharEnd(long nativeParagraph);

	private static native byte nativeGetBaseLevel(long nativeParagraph);
	private static native byte nativeGetLevel(long nativeParagraph, int levelIndex);
    private static native BidiRun nativeGetRun(long nativeParagraph, int levelIndex);

	private static native long nativeCreateLine(long nativeParagraph, int charStart, int charEnd);
}
