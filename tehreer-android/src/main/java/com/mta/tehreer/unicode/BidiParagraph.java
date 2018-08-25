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
import com.mta.tehreer.collections.ByteList;
import com.mta.tehreer.internal.Constants;
import com.mta.tehreer.internal.Description;
import com.mta.tehreer.internal.JniBridge;
import com.mta.tehreer.internal.collections.Int8BufferByteList;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A <code>BidiParagraph</code> object represents a single paragraph of text processed with rules
 * X1-I2 of Unicode Bidirectional Algorithm. It contains the resolved embedding levels of all the
 * characters of a paragraph and provides the facility to query them or iterate over their runs.
 */
public class BidiParagraph implements Disposable {
    static {
        JniBridge.loadLibrary();
    }

    private static final class Finalizable extends BidiParagraph {
        Finalizable(@NonNull BidiParagraph parent) {
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
     * <strong>Note:</strong> The behavior is undefined if the passed-in object is already disposed
     * or wrapped into another finalizable instance.
     *
     * @param bidiParagraph The bidi paragraph object to wrap into a finalizable instance.
     *
     * @return The finalizable instance of the passed-in bidi paragraph object.
     */
    public static @NonNull BidiParagraph finalizable(@NonNull BidiParagraph bidiParagraph) {
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
    public static boolean isFinalizable(@NonNull BidiParagraph bidiParagraph) {
        return (bidiParagraph.getClass() == Finalizable.class);
    }

    long nativeBuffer;
	long nativeParagraph;

	BidiParagraph(long nativeBuffer, long nativeParagraph) {
        this.nativeBuffer = BidiBuffer.retain(nativeBuffer);
	    this.nativeParagraph = nativeParagraph;
	}

    BidiParagraph(@NonNull BidiParagraph other) {
        this.nativeBuffer = other.nativeBuffer;
        this.nativeParagraph = other.nativeParagraph;
    }

    /**
     * Returns the index to the first character of this paragraph in source text.
     *
     * @return The index to the first character of this paragraph in source text.
     */
    public int getCharStart() {
		return nGetCharStart(nativeParagraph);
	}

    /**
     * Returns the index after the last character of this paragraph in source text.
     *
     * @return The index after the last character of this paragraph in source text.
     */
	public int getCharEnd() {
        return nGetCharEnd(nativeParagraph);
	}

    /**
     * Returns the base level of this paragraph.
     *
     * @return The base level of this paragraph.
     */
	public byte getBaseLevel() {
		return nGetBaseLevel(nativeParagraph);
	}

    /**
     * Returns a list containing the levels of all characters in this paragraph.
     * <p>
     * <strong>Note:</strong> The returned list might exhibit undefined behavior if the paragraph
     * object is disposed.
     *
     * @return A list containing the levels of all characters in this paragraph.
     */
	public @NonNull ByteList getCharLevels() {
	    return new Int8BufferByteList(this,
                                      nGetLevelsPtr(nativeParagraph),
                                      nGetCharCount(nativeParagraph));
	}

    /**
     * Returns an iterable of logically ordered runs in this paragraph.
     * <p>
     * <strong>Note:</strong> The returned iterable might exhibit undefined behavior if the
     * paragraph object is disposed.
     *
     * @return An iterable of logically ordered runs in this paragraph.
     */
    public @NonNull Iterable<BidiRun> getLogicalRuns() {
        return new RunIterable();
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
	public @NonNull BidiLine createLine(int charStart, int charEnd) {
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
                            nCreateLine(nativeParagraph, charStart, charEnd));
    }

    @Override
    public void dispose() {
        nDispose(nativeParagraph);
        BidiBuffer.release(nativeBuffer);
    }

    @Override
    public String toString() {
        return "BidiParagraph{charStart=" + getCharStart()
                + ", charEnd=" + getCharEnd()
                + ", baseLevel=" + getBaseLevel()
                + ", charLevels=" + Description.forByteList(getCharLevels())
                + ", logicalRuns=" + Description.forIterable(getLogicalRuns())
                + "}";
    }

	private static native void nDispose(long nativeParagraph);

	private static native int nGetCharStart(long nativeParagraph);
	private static native int nGetCharEnd(long nativeParagraph);
    private static native int nGetCharCount(long nativeParagraph);

	private static native byte nGetBaseLevel(long nativeParagraph);
	private static native long nGetLevelsPtr(long nativeParagraph);
    private static native BidiRun nGetOnwardRun(long nativeParagraph, int charIndex);

	private static native long nCreateLine(long nativeParagraph, int charStart, int charEnd);

    private class RunIterator implements Iterator<BidiRun> {
        BidiRun run;

        RunIterator() {
            run = nGetOnwardRun(nativeParagraph, getCharStart());
        }

        @Override
        public boolean hasNext() {
            return run != null;
        }

        @Override
        public @NonNull BidiRun next() {
            BidiRun current = run;
            if (current == null) {
                throw new NoSuchElementException();
            }
            run = nGetOnwardRun(nativeParagraph, current.charEnd);

            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class RunIterable implements Iterable<BidiRun> {
        @Override
        public @NonNull Iterator<BidiRun> iterator() {
            return new RunIterator();
        }
    }
}
