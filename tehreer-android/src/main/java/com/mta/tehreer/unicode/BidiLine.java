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

package com.mta.tehreer.unicode;

import androidx.annotation.NonNull;

import com.mta.tehreer.Disposable;
import com.mta.tehreer.internal.Constants;
import com.mta.tehreer.internal.Description;
import com.mta.tehreer.internal.JniBridge;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static com.mta.tehreer.internal.util.Preconditions.checkElementIndex;

/**
 * A <code>BidiLine</code> object represents a single line processed with rules L1-L2 of Unicode
 * Bidirectional Algorithm. Instead of reordering the characters as stated by rule L2, it allows to
 * query and iterate over reordered level runs. The caller is responsible to reorder the characters
 * manually, if required.
 */
public class BidiLine implements Disposable {
    static {
        JniBridge.loadLibrary();
    }

    static final class Finalizable extends BidiLine {
        Finalizable(@NonNull BidiLine parent) {
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
     * Wraps a bidi line object into a finalizable instance which is guaranteed to be disposed
     * automatically by the GC when no longer in use. After calling this method,
     * <code>dispose()</code> should not be called on either original object or returned object.
     * Calling <code>dispose()</code> on returned object will throw an
     * <code>UnsupportedOperationException</code>.
     * <p>
     * <strong>Note:</strong> The behavior is undefined if the passed-in object is already disposed
     * or wrapped into another finalizable instance.
     *
     * @param bidiLine The bidi line object to wrap into a finalizable instance.
     *
     * @return The finalizable instance of the passed-in bidi line object.
     */
    public static @NonNull BidiLine finalizable(@NonNull BidiLine bidiLine) {
        if (bidiLine.getClass() == BidiLine.class) {
            return new Finalizable(bidiLine);
        }

        if (bidiLine.getClass() != Finalizable.class) {
            throw new IllegalArgumentException(Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED);
        }

        return bidiLine;
    }

    /**
     * Checks whether a bidi line object is finalizable or not.
     *
     * @param bidiLine The bidi line object to check.
     *
     * @return <code>true</code> if the passed-in bidi line objcet is finalizable,
     *         <code>false</code> otherwise.
     */
    public static boolean isFinalizable(@NonNull BidiLine bidiLine) {
        return (bidiLine.getClass() == Finalizable.class);
    }

    long nativeBuffer;
	long nativeLine;

	BidiLine(long nativeBuffer, long nativeLine) {
        this.nativeBuffer = BidiBuffer.retain(nativeBuffer);
	    this.nativeLine = nativeLine;
	}

    BidiLine(@NonNull BidiLine other) {
        this.nativeBuffer = other.nativeBuffer;
        this.nativeLine = other.nativeLine;
    }

    /**
     * Returns the index to the first character of this line in source text.
     *
     * @return The index to the first character of this line in source text.
     */
	public int getCharStart() {
		return nGetCharStart(nativeLine);
	}

    /**
     * Returns the index after the last character of this line in source text.
     *
     * @return The index after the last character of this line in source text.
     */
	public int getCharEnd() {
		return nGetCharEnd(nativeLine);
	}

	int getRunCount() {
	    return nGetRunCount(nativeLine);
    }

    BidiRun getVisualRun(int runIndex) {
	    return nGetVisualRun(nativeLine, runIndex);
    }

    /**
     * Returns an unmodifiable list of visually ordered runs in this line.
     * <p>
     * <strong>Note:</strong> The returned list might exhibit undefined behavior if the line object
     * is disposed.
     *
     * @return An unmodifiable list of visually ordered runs in this line.
     */
    public @NonNull List<BidiRun> getVisualRuns() {
        return new RunList(this);
    }

    /**
     * Returns an iterable of mirroring pairs in this line. You can use the iterable to implement
     * Rule L4 of Unicode Bidirectional Algorithm.
     * <p>
     * <strong>Note:</strong> The returned iterable might exhibit undefined behavior if the line
     * object is disposed.
     *
     * @return An iterable of mirroring pairs in this line.
     */
    public @NonNull Iterable<BidiPair> getMirroringPairs() {
        return new MirrorIterable(this);
    }

    @Override
    public void dispose() {
        nDispose(nativeLine);
        BidiBuffer.release(nativeBuffer);
    }

    @Override
    public @NonNull String toString() {
        return "BidiLine{charStart=" + getCharStart()
                + ", charEnd=" + getCharEnd()
                + ", visualRuns=" + Description.forIterable(getVisualRuns())
                + ", mirroringPairs=" + Description.forIterable(getMirroringPairs())
                + "}";
    }

	private static native void nDispose(long nativeLine);

	private static native int nGetCharStart(long nativeLine);
	private static native int nGetCharEnd(long nativeLine);

	private static native int nGetRunCount(long nativeLine);
	private static native BidiRun nGetVisualRun(long nativeLine, int runIndex);

    static final class RunList extends AbstractList<BidiRun> {
        final BidiLine owner;
        final int size;

        RunList(BidiLine owner) {
            this.owner = owner;
            this.size = owner.getRunCount();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public @NonNull BidiRun get(int index) {
            checkElementIndex(index, size);

            return owner.getVisualRun(index);
        }
    }

    static final class MirrorIterator implements Iterator<BidiPair> {
        final BidiLine owner;
        final BidiMirrorLocator locator;
        BidiPair pair;

        MirrorIterator(BidiLine owner) {
            this(owner, new BidiMirrorLocator());
        }

        MirrorIterator(BidiLine owner, BidiMirrorLocator locator) {
            this.owner = owner;
            this.locator = locator;

            locator.loadLine(owner);

            pair = locator.nextPair();
        }

        @Override
        public boolean hasNext() {
            return (pair != null);
        }

        @Override
        public @NonNull BidiPair next() {
            BidiPair current = pair;
            if (current == null) {
                throw new NoSuchElementException();
            }
            pair = locator.nextPair();

            return current;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                locator.dispose();
            } finally {
                super.finalize();
            }
        }
    }

    static final class MirrorIterable implements Iterable<BidiPair> {
        final BidiLine owner;

        MirrorIterable(BidiLine owner) {
            this.owner = owner;
        }

        @Override
        public @NonNull Iterator<BidiPair> iterator() {
            return new MirrorIterator(owner);
        }
    }
}
