/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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

/**
 * A <code>ScriptRun</code> object represents a sequence of characters which have the same script.
 */
public class ScriptRun {
    /**
     * The index to the first character of this run in source text.
     */
    public int charStart;
    /**
     * The index after the last character of this run in source text.
     */
    public int charEnd;
    /**
     * The resolved script of this run.
     */
    public int script;

    /**
     * Constructs a script run object.
     *
     * @param charStart The index to the first character of run.
     * @param charEnd The index after the last character of run.
     * @param script The resolved script of run.
     */
    public ScriptRun(int charStart, int charEnd, int script) {
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.script = script;
    }

    /**
     * Constructs a script run object.
     */
    public ScriptRun() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this != obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            ScriptRun other = (ScriptRun) obj;
            if (charStart != other.charStart
                    || charEnd != other.charEnd
                    || script != other.script) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = charStart;
        result = 31 * result + charEnd;
        result = 31 * result + script;

        return result;
    }
}
