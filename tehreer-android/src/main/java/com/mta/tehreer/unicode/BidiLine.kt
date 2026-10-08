/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.unicode

import com.mta.tehreer.internal.Description
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.util.Preconditions.checkElementIndex

/**
 * A `BidiLine` object represents a single line processed with rules L1-L2 of Unicode Bidirectional
 * Algorithm. Instead of reordering the characters as stated by rule L2, it allows to query and
 * iterate over reordered level runs. The caller is responsible to reorder the characters manually,
 * if required.
 */
class BidiLine {
    private class Finalizable(
        private val nativeBuffer: Long,
        private val nativeLine: Long
    ) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeLine)
            BidiBuffer.release(nativeBuffer)
        }
    }

    internal val nativeBuffer: Long
    internal val nativeLine: Long
    private val finalizable: Finalizable

    internal constructor(nativeBuffer: Long, nativeLine: Long) {
        this.nativeBuffer = BidiBuffer.retain(nativeBuffer)
        this.nativeLine = nativeLine
        this.finalizable = Finalizable(this.nativeBuffer, nativeLine)
    }

    /**
     * Returns the index to the first character of this line in source text.
     *
     * @return The index to the first character of this line in source text.
     */
    val charStart: Int
        get() = nGetCharStart(nativeLine)

    /**
     * Returns the index after the last character of this line in source text.
     *
     * @return The index after the last character of this line in source text.
     */
    val charEnd: Int
        get() = nGetCharEnd(nativeLine)

    internal val runCount: Int
        get() = nGetRunCount(nativeLine)

    internal fun getVisualRun(runIndex: Int): BidiRun {
        return nGetVisualRun(nativeLine, runIndex)
    }

    /**
     * Returns an unmodifiable list of visually ordered runs in this line.
     *
     * **Note:** The returned list might exhibit undefined behavior if the line object is disposed.
     *
     * @return An unmodifiable list of visually ordered runs in this line.
     */
    val visualRuns: List<BidiRun>
        get() = RunList(this)

    /**
     * Returns an iterable of mirroring pairs in this line. You can use the iterable to implement
     * Rule L4 of Unicode Bidirectional Algorithm.
     *
     * **Note:** The returned iterable might exhibit undefined behavior if the line object is
     * disposed.
     *
     * @return An iterable of mirroring pairs in this line.
     */
    val mirroringPairs: Iterable<BidiPair>
        get() = MirrorIterable(this)

    override fun toString(): String {
        return "BidiLine{charStart=$charStart" +
            ", charEnd=$charEnd" +
            ", visualRuns=${Description.forIterable(visualRuns)}" +
            ", mirroringPairs=${Description.forIterable(mirroringPairs)}" +
            "}"
    }

    internal class RunList(val owner: BidiLine) : AbstractList<BidiRun>() {
        override val size: Int = owner.runCount

        override fun get(index: Int): BidiRun {
            checkElementIndex(index, size)

            return owner.getVisualRun(index)
        }
    }

    internal class MirrorIterator(
        val owner: BidiLine,
        private val locator: BidiMirrorLocator = BidiMirrorLocator()
    ) : Iterator<BidiPair> {
        var pair: BidiPair?

        init {
            locator.loadLine(owner)
            pair = locator.nextPair()
        }

        override fun hasNext(): Boolean {
            return pair != null
        }

        override fun next(): BidiPair {
            val current = pair ?: throw NoSuchElementException()
            pair = locator.nextPair()

            return current
        }
    }

    internal class MirrorIterable(val owner: BidiLine) : Iterable<BidiPair> {
        override fun iterator(): Iterator<BidiPair> {
            return MirrorIterator(owner)
        }
    }

    private external fun nGetCharStart(nativeLine: Long): Int
    private external fun nGetCharEnd(nativeLine: Long): Int
    private external fun nGetRunCount(nativeLine: Long): Int
    private external fun nGetVisualRun(nativeLine: Long, runIndex: Int): BidiRun
    companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic private external fun nDispose(nativeLine: Long)

    }
}
