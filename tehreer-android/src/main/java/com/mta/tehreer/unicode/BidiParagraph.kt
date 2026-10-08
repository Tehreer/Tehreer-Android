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

import com.mta.tehreer.collections.ByteList
import com.mta.tehreer.internal.Description
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.collections.Int8BufferByteList

/**
 * A `BidiParagraph` object represents a single paragraph of text processed with rules X1-I2 of
 * Unicode Bidirectional Algorithm. It contains the resolved embedding levels of all the characters
 * of a paragraph and provides the facility to query them or iterate over their runs.
 */
class BidiParagraph {
    private class Finalizable(
        private val nativeBuffer: Long,
        private val nativeParagraph: Long
    ) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeParagraph)
            BidiBuffer.release(nativeBuffer)
        }
    }

    internal val nativeBuffer: Long
    internal val nativeParagraph: Long
    private val finalizable: Finalizable

    internal constructor(nativeBuffer: Long, nativeParagraph: Long) {
        this.nativeBuffer = BidiBuffer.retain(nativeBuffer)
        this.nativeParagraph = nativeParagraph
        this.finalizable = Finalizable(this.nativeBuffer, nativeParagraph)
    }

    /**
     * Returns the index to the first character of this paragraph in source text.
     *
     * @return The index to the first character of this paragraph in source text.
     */
    val charStart: Int
        get() = nGetCharStart(nativeParagraph)

    /**
     * Returns the index after the last character of this paragraph in source text.
     *
     * @return The index after the last character of this paragraph in source text.
     */
    val charEnd: Int
        get() = nGetCharEnd(nativeParagraph)

    /**
     * Returns the base level of this paragraph.
     *
     * @return The base level of this paragraph.
     */
    val baseLevel: Byte
        get() = nGetBaseLevel(nativeParagraph)

    /**
     * Returns a list containing the levels of all characters in this paragraph.
     *
     * **Note:** The returned list might exhibit undefined behavior if the paragraph object is
     * disposed.
     *
     * @return A list containing the levels of all characters in this paragraph.
     */
    val charLevels: ByteList
        get() = Int8BufferByteList(this, nGetLevelsPtr(nativeParagraph), nGetCharCount(nativeParagraph))

    internal fun getOnwardRun(charIndex: Int): BidiRun? {
        return nGetOnwardRun(nativeParagraph, charIndex)
    }

    /**
     * Returns an iterable of logically ordered runs in this paragraph.
     *
     * **Note:** The returned iterable might exhibit undefined behavior if the paragraph object is
     * disposed.
     *
     * @return An iterable of logically ordered runs in this paragraph.
     */
    val logicalRuns: Iterator<BidiRun>
        get() = RunIterator(this)

    private fun checkSubRange(charStart: Int, charEnd: Int) {
        val paragraphStart = this.charStart
        val paragraphEnd = this.charEnd

        require(charStart >= paragraphStart) {
            "Char Start: $charStart, Paragraph Range: [$paragraphStart, $paragraphEnd)"
        }
        require(charEnd <= paragraphEnd) {
            "Char End: $charEnd, Paragraph Range: [$paragraphStart, $paragraphEnd)"
        }
        require(charEnd > charStart) { "Bad Range: [$charStart, $charEnd)" }
    }

    /**
     * Creates a line object of specified range by applying Rules L1-L2 of Unicode Bidirectional
     * Algorithm.
     *
     * @param charStart The index to the first character of the line in source text.
     * @param charEnd The index after the last character of the line in source text.
     * @return A line object processed with Rules L1-L2 of Unicode Bidirectional Algorithm.
     *
     * @throws IllegalArgumentException if `charStart` is less than paragraph start, or `charEnd` is
     *         greater than paragraph end, or `charStart` is greater than or equal to `charEnd`.
     */
    fun createLine(charStart: Int, charEnd: Int): BidiLine {
        checkSubRange(charStart, charEnd)

        return BidiLine(nativeBuffer, nCreateLine(nativeParagraph, charStart, charEnd))
    }

    override fun toString(): String {
        return "BidiParagraph{charStart=$charStart" +
            ", charEnd=$charEnd" +
            ", baseLevel=$baseLevel" +
            ", charLevels=${Description.forByteList(charLevels)}" +
            ", logicalRuns=${Description.forIterator(logicalRuns)}" +
            "}"
    }

    internal class RunIterator(val owner: BidiParagraph) : Iterator<BidiRun> {
        var run: BidiRun? = owner.getOnwardRun(owner.charStart)

        override fun hasNext(): Boolean {
            return run != null
        }

        override fun next(): BidiRun {
            val current = run ?: throw NoSuchElementException()
            run = owner.getOnwardRun(current.charEnd)

            return current
        }
    }

    private external fun nGetCharStart(nativeParagraph: Long): Int
    private external fun nGetCharEnd(nativeParagraph: Long): Int
    private external fun nGetCharCount(nativeParagraph: Long): Int
    private external fun nGetBaseLevel(nativeParagraph: Long): Byte
    private external fun nGetLevelsPtr(nativeParagraph: Long): Long
    private external fun nGetOnwardRun(nativeParagraph: Long, charIndex: Int): BidiRun?

    private external fun nCreateLine(
        nativeParagraph: Long, charStart: Int, charEnd: Int
    ): Long

    companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic private external fun nDispose(nativeParagraph: Long)

    }
}
