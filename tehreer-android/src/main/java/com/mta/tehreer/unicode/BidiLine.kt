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

import com.mta.tehreer.Disposable
import com.mta.tehreer.internal.Constants
import com.mta.tehreer.internal.Description
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.util.Preconditions.checkElementIndex

/**
 * A `BidiLine` object represents a single line processed with rules L1-L2 of Unicode Bidirectional
 * Algorithm. Instead of reordering the characters as stated by rule L2, it allows to query and
 * iterate over reordered level runs. The caller is responsible to reorder the characters manually,
 * if required.
 */
open class BidiLine : Disposable {
    private class Finalizable(parent: BidiLine) : BidiLine(parent) {
        override fun dispose() {
            throw UnsupportedOperationException(Constants.EXCEPTION_FINALIZABLE_OBJECT)
        }

        @Suppress("unused")
        protected fun finalize() {
            super.dispose()
        }
    }

    @JvmField internal var nativeBuffer: Long
    @JvmField internal var nativeLine: Long

    internal constructor(nativeBuffer: Long, nativeLine: Long) {
        this.nativeBuffer = BidiBuffer.retain(nativeBuffer)
        this.nativeLine = nativeLine
    }

    private constructor(other: BidiLine) {
        this.nativeBuffer = other.nativeBuffer
        this.nativeLine = other.nativeLine
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

    override fun dispose() {
        nDispose(nativeLine)
        BidiBuffer.release(nativeBuffer)
    }

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

        @Suppress("unused")
        protected fun finalize() {
            locator.dispose()
        }
    }

    internal class MirrorIterable(val owner: BidiLine) : Iterable<BidiPair> {
        override fun iterator(): Iterator<BidiPair> {
            return MirrorIterator(owner)
        }
    }

    companion object {
        init {
            JniBridge.loadLibrary()
        }

        /**
         * Wraps a bidi line object into a finalizable instance which is guaranteed to be disposed
         * automatically by the GC when no longer in use. After calling this method, `dispose()`
         * should not be called on either original object or returned object. Calling `dispose()`
         * on returned object will throw an `UnsupportedOperationException`.
         *
         * **Note:** The behavior is undefined if the passed-in object is already disposed or
         * wrapped into another finalizable instance.
         *
         * @param bidiLine The bidi line object to wrap into a finalizable instance.
         *
         * @return The finalizable instance of the passed-in bidi line object.
         */
        @JvmStatic
        fun finalizable(bidiLine: BidiLine): BidiLine {
            return when (bidiLine.javaClass) {
                BidiLine::class.java -> Finalizable(bidiLine)
                Finalizable::class.java -> bidiLine
                else -> throw IllegalArgumentException(Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED)
            }
        }

        /**
         * Checks whether a bidi line object is finalizable or not.
         *
         * @param bidiLine The bidi line object to check.
         *
         * @return `true` if the passed-in bidi line object is finalizable, `false` otherwise.
         */
        @JvmStatic
        fun isFinalizable(bidiLine: BidiLine): Boolean {
            return bidiLine.javaClass == Finalizable::class.java
        }

        @JvmStatic private external fun nDispose(nativeLine: Long)

        @JvmStatic private external fun nGetCharStart(nativeLine: Long): Int
        @JvmStatic private external fun nGetCharEnd(nativeLine: Long): Int

        @JvmStatic private external fun nGetRunCount(nativeLine: Long): Int
        @JvmStatic private external fun nGetVisualRun(nativeLine: Long, runIndex: Int): BidiRun
    }
}
