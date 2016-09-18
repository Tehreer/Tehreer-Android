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

public class BidiLine implements Disposable {

    private static class Finalizable extends BidiLine {

        private Finalizable(BidiLine parent) {
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

    public static BidiLine finalizable(BidiLine bidiLine) {
        if (bidiLine.getClass() == BidiLine.class) {
            return new Finalizable(bidiLine);
        }

        if (bidiLine.getClass() != Finalizable.class) {
            throw new IllegalArgumentException(Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED);
        }

        return bidiLine;
    }

    public static boolean isFinalizable(BidiLine bidiLine) {
        return (bidiLine.getClass() == Finalizable.class);
    }

    long nativeBuffer;
	long nativeLine;

	BidiLine(long nativeBuffer, long nativeLine) {
        this.nativeBuffer = BidiBuffer.retain(nativeBuffer);
	    this.nativeLine = nativeLine;
	}

    private BidiLine(BidiLine other) {
        this.nativeBuffer = other.nativeBuffer;
        this.nativeLine = other.nativeLine;
    }

    /**
     * Returns the index to the first character of this line in source text.
     *
     * @return The index to the first character of this line in source text.
     */
	public int getCharStart() {
		return nativeGetCharStart(nativeLine);
	}

    /**
     * Returns the index after the last character of this line in source text.
     *
     * @return The index after the last character of this line in source text.
     */
	public int getCharEnd() {
		return nativeGetCharEnd(nativeLine);
	}

    /**
     * Returns the number of runs in this line.
     *
     * @return The number of runs in this line.
     */
	public int getRunCount() {
	    return nativeGetRunCount(nativeLine);
	}

    /**
     * Returns the visual run at the specified index in this line.
     *
     * @param runIndex Index of the visual run to return.
     * @return The visual run at the specified index in this line.
     *
     * @throws IndexOutOfBoundsException if <code>runIndex</code> is negative, or greater than or
     *         equal to <code>getRunCount()</code>.
     */
	public BidiRun getVisualRun(int runIndex) {
        int runCount = getRunCount();
        if (runIndex < 0 || runIndex >= runCount) {
            throw new IndexOutOfBoundsException("Run Index: " + runIndex);
        }

        return nativeGetVisualRun(nativeLine, runIndex);
    }

    /**
     * Notifies the given consumer about each visual run of this line until all runs have been
     * processed or <code>stop()</code> has been called by the consumer.
     *
     * @param consumer The consumer to notify about each visual run.
     *
     * @throws NullPointerException if <code>consumer</code> is <code>null</code>.
     */
	public void iterateVisualRuns(BidiRunConsumer consumer) {
        int runCount = getRunCount();

        for (int i = 0; i < runCount; i++) {
            consumer.accept(nativeGetVisualRun(nativeLine, i));

            if (consumer.isStopped) {
                break;
            }
        }
    }

    /**
     * Notifies the given consumer about each mirror of this line until all mirrors have been
     * processed or <code>stop()</code> has been called by the consumer.
     * <p>
     * You can use this method to implement Rule L4 of Unicode Bidirectional Algorithm.
     *
     * @param consumer The consumer to notify about each mirror.
     *
     * @throws NullPointerException if <code>consumer</code> is <code>null</code>.
     */
	public void iterateMirrors(BidiPairConsumer consumer) {
        BidiMirrorLocator locator = null;

        try {
            locator = new BidiMirrorLocator();
            locator.loadLine(this);

            BidiPair bidiPair;
            while ((bidiPair = locator.nextPair()) != null) {
                consumer.accept(bidiPair);

                if (consumer.isStopped) {
                    break;
                }
            }
        } finally {
            if (locator != null) {
                locator.dispose();
            }
        }
	}

    @Override
    public void dispose() {
        nativeDispose(nativeLine);
        BidiBuffer.release(nativeBuffer);
    }

    @Override
    public String toString() {
        StringBuilder runsBuilder = new StringBuilder();
        runsBuilder.append("[");

        int runCount = getRunCount();
        for (int i = 0; i < runCount; i++) {
            runsBuilder.append(nativeGetVisualRun(nativeLine, i).toString());
            if (i < runCount - 1) {
                runsBuilder.append(", ");
            }
        }

        runsBuilder.append("]");

        return "BidiLine{charStart=" + getCharStart()
                + ", charEnd=" + getCharEnd()
                + ", visualRuns=" + runsBuilder.toString()
                + "}";
    }

	private static native void nativeDispose(long nativeLine);

	private static native int nativeGetCharStart(long nativeLine);
	private static native int nativeGetCharEnd(long nativeLine);

	private static native int nativeGetRunCount(long nativeLine);
	private static native BidiRun nativeGetVisualRun(long nativeLine, int runIndex);
}
