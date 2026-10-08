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

/**
 * A `ScriptRun` object represents a sequence of characters which have the same script.
 */
class ScriptRun(
    /**
     * The index to the first character of this run in source text.
     */
    @JvmField var charStart: Int,

    /**
     * The index after the last character of this run in source text.
     */
    @JvmField var charEnd: Int,

    /**
     * The resolved script of this run.
     */
    @JvmField var script: Int
) {
    /**
     * Constructs a script run object.
     */
    constructor() : this(0, 0, 0)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }

        other as ScriptRun

        return charStart == other.charStart && charEnd == other.charEnd && script == other.script
    }

    override fun hashCode(): Int {
        var result = charStart
        result = 31 * result + charEnd
        result = 31 * result + script

        return result
    }

    override fun toString(): String {
        return "ScriptRun{charStart=$charStart, charEnd=$charEnd, script=$script}"
    }
}
