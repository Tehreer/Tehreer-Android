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

import com.mta.tehreer.collections.IntList
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.collections.UInt8BufferIntList

/**
 * This class implements Unicode Bidirectional Algorithm available at
 * <a href="http://www.unicode.org/reports/tr9">http://www.unicode.org/reports/tr9</a>.
 *
 * A `BidiAlgorithm` object provides information related to individual paragraphs in source text by
 * applying rule P1. It can be used to create paragraph objects by explicitly specifying the
 * paragraph level or deriving it from rules P2 and P3. Once a paragraph object is created,
 * embedding levels of characters can be queried from it.
 */
class BidiAlgorithm {
    private class Finalizable(
        private val nativeBuffer: Long,
        private val nativeAlgorithm: Long
    ) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeAlgorithm)
            BidiBuffer.release(nativeBuffer)
        }
    }

    internal val nativeBuffer: Long
    internal val nativeAlgorithm: Long
    private val text: String
    private val finalizable: Finalizable

    /**
     * Constructs a bidi algorithm object for the given text.
     *
     * @param text The text to apply unicode bidirectional algorithm on.
     *
     * @throws IllegalArgumentException if `text` is empty.
     */
    constructor(text: String) {
        require(text.isNotEmpty()) { "Text is empty" }

        this.nativeBuffer = BidiBuffer.create(text)
        this.nativeAlgorithm = nCreate(nativeBuffer)
        this.text = text
        this.finalizable = Finalizable(nativeBuffer, nativeAlgorithm)
    }

    private fun checkSubRange(charStart: Int, charEnd: Int) {
        require(charStart >= 0) { "Char Start: $charStart" }
        require(charEnd <= text.length) { "Char End: $charEnd, Text Length: ${text.length}" }
        require(charEnd > charStart) { "Bad Range: [$charStart, $charEnd)" }
    }

    /**
     * Returns a list containing the bidi classes of all characters in source text. The valid bidi
     * class values are available in [BidiClass] as static constants.
     *
     * @return A list containing the bidi classes of all characters in source text.
     */
    val charBidiClasses: IntList
        get() = UInt8BufferIntList(this, nGetCharBidiClassesPtr(nativeAlgorithm), text.length)

    /**
     * Returns the boundary of the first paragraph within the given range.
     *
     * The boundary of the paragraph occurs after a character whose bidirectional type is Paragraph
     * Separator (B), or the `charEnd` if no such character exists before it. The exception to this
     * rule is when a Carriage Return (CR) is followed by a Line Feed (LF). Both CR and LF are
     * paragraph separators, but in that case, the boundary of the paragraph is considered after LF
     * character.
     *
     * @param charStart The index to the first character of the paragraph in source text.
     * @param charEnd The suggested index after the last character of the paragraph in source text.
     * @return The boundary of the first paragraph within the given range.
     *
     * @throws IllegalArgumentException if `charStart` is negative, or `charEnd` is greater than the
     *         length of source text, or `charStart` is greater than or equal to `charEnd`.
     */
    fun getParagraphBoundary(charStart: Int, charEnd: Int): Int {
        checkSubRange(charStart, charEnd)

        return nGetParagraphBoundary(nativeAlgorithm, charStart, charEnd)
    }

    /**
     * Creates a paragraph object processed with Unicode Bidirectional Algorithm.
     *
     * This method processes only first paragraph starting at `charStart` and ending at either
     * `charEnd` or some character before it, in accordance with Rule P1 of Unicode Bidirectional
     * Algorithm.
     *
     * The paragraph level is determined by applying Rules P2-P3 and embedding levels are resolved
     * by applying Rules X1-I2.
     *
     * @param charStart The index to the first character of the paragraph in source text.
     * @param charEnd The suggested index after the last character of the paragraph in source text.
     * @param baseDirection The base direction of the paragraph.
     * @return A paragraph object processed with Unicode Bidirectional Algorithm.
     *
     * @throws IllegalArgumentException if `charStart` is negative, or `charEnd` is greater than the
     *         length of source text, or `charStart` is greater than or equal to `charEnd`.
     */
    fun createParagraph(charStart: Int, charEnd: Int, baseDirection: BaseDirection): BidiParagraph {
        checkSubRange(charStart, charEnd)

        return BidiParagraph(
            nativeBuffer,
            nCreateParagraph(nativeAlgorithm, charStart, charEnd, baseDirection.value)
        )
    }

    /**
     * Creates a paragraph object processed with Unicode Bidirectional Algorithm.
     *
     * This method processes only first paragraph starting at `charStart` and ending at either
     * `charEnd` or some character before it, in accordance with Rule P1 of Unicode Bidirectional
     * Algorithm.
     *
     * The paragraph level is overridden by `baseLevel` parameter and embedding levels are resolved
     * by applying Rules X1-I2.
     *
     * @param charStart The index to the first character of the paragraph in source text.
     * @param charEnd The suggested index after the last character of the paragraph in source text.
     * @param baseLevel Base level to override.
     * @return A paragraph object processed with Unicode Bidirectional Algorithm.
     *
     * @throws IllegalArgumentException if any of the following is true:
     *  - `charStart` is negative
     *  - `charEnd` is greater than the length of source text
     *  - `charStart` is greater than or equal to `charEnd`
     *  - `baseLevel` is less than zero
     *  - `baseLevel` is greater than [MAX_LEVEL]
     */
    fun createParagraph(charStart: Int, charEnd: Int, baseLevel: Byte): BidiParagraph {
        checkSubRange(charStart, charEnd)
        require(baseLevel in 0..MAX_LEVEL) { "Base Level: $baseLevel" }

        return BidiParagraph(
            nativeBuffer,
            nCreateParagraph(nativeAlgorithm, charStart, charEnd, baseLevel.toInt())
        )
    }

    override fun toString(): String {
        return "BidiAlgorithm{text=$text, charBidiClasses=$charBidiClasses}"
    }

    private external fun nGetCharBidiClassesPtr(nativeAlgorithm: Long): Long
    private external fun nGetParagraphBoundary(
        nativeAlgorithm: Long, charStart: Int, charEnd: Int
    ): Int

    private external fun nCreateParagraph(
        nativeAlgorithm: Long, charStart: Int, charEnd: Int, baseLevel: Int
    ): Long

    companion object {
        init {
            JniBridge.loadLibrary()
        }

        /**
         * Maximum explicit embedding level.
         */
        const val MAX_LEVEL: Byte = 125

        @JvmStatic private external fun nCreate(nativeBuffer: Long): Long
        @JvmStatic private external fun nDispose(nativeAlgorithm: Long)

    }
}
