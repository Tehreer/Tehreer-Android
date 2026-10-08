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
import com.mta.tehreer.internal.Description
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.internal.collections.UInt8BufferIntList

/**
 * This class implements UAX #24 available at
 * <a href="http://www.unicode.org/reports/tr24">http://www.unicode.org/reports/tr24</a>.
 */
class ScriptClassifier(
    /**
     * Returns the text that the script classifier object was created for.
     *
     * @return The text that the script classifier object was created for.
     */
    val text: String
) {
    private class Finalizable(private val nativeScripts: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeScripts)
        }
    }

    private val nativeScripts = nClassify(text)
    private val finalizable = Finalizable(nativeScripts)

    /**
     * Returns a list containing the resolved scripts of all characters in source text. The valid
     * script values are available in [Script] class as static constants.
     *
     * @return A list containing the resolved scripts of all characters in source text.
     */
    val charScripts: IntList
        get() = UInt8BufferIntList(this, nativeScripts, text.length)

    /**
     * Returns an iterable of resolved script runs in source text.
     *
     * @return An iterable of resolved script runs in source text.
     */
    val scriptRuns: Iterator<ScriptRun>
        get() = getScriptRuns(0, text.length)

    /**
     * Returns an iterator of resolved script runs within the specified range of source text.
     *
     * @param charStart The index to the first character in source text.
     * @param charEnd The index after the last character in source text.
     * @return An iterator of script runs within the specified range of source text.
     */
    fun getScriptRuns(charStart: Int, charEnd: Int): Iterator<ScriptRun> {
        require(charStart >= 0) { "Char Start: $charStart" }
        require(charEnd <= text.length) { "Char End: $charEnd, Text Length: ${text.length}" }
        require(charEnd > charStart) { "Bad Range: [$charStart, $charEnd)" }

        return RunIterator(charScripts, charStart, charEnd)
    }

    override fun toString(): String {
        return "ScriptClassifier{text=$text" +
            ", charScripts=${Description.forIntList(charScripts)}" +
            ", scriptRuns=${Description.forIterator(scriptRuns)}" +
            "}"
    }

    internal class RunIterator(
        val scripts: IntList,
        start: Int,
        val end: Int
    ) : Iterator<ScriptRun> {
        var index = start

        override fun hasNext(): Boolean {
            return index != end
        }

        override fun next(): ScriptRun {
            if (index == end) {
                throw NoSuchElementException()
            }

            val current = scripts[index]
            val start = index

            index += 1
            while (index < end && scripts[index] == current) {
                index += 1
            }

            return ScriptRun(start, index, current)
        }
    }

    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        /** Resolves the script of each character, which are kept in native memory. */
        @JvmStatic external fun nClassify(text: String): Long
        @JvmStatic external fun nDispose(nativeScripts: Long)
    }
}
