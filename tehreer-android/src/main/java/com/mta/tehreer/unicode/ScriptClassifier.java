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

import com.mta.tehreer.internal.JniBridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScriptClassifier {

    static {
        JniBridge.loadLibrary();
    }

    private final String text;
    private final byte[] scripts;

    public ScriptClassifier(String text) {
        if (text == null) {
            throw new NullPointerException("Text is null");
        }

        this.text = text;
        this.scripts = new byte[text.length()];
        nClassify(text, scripts);
    }

    public String getText() {
        return text;
    }

    public List<Script> getCharScripts() {
        return new ScriptList(scripts);
    }

    public List<ScriptRun> getScriptRuns() {
        return getScriptRuns(0, scripts.length);
    }

    public List<ScriptRun> getScriptRuns(int charStart, int charEnd) {
        if (charStart < 0) {
            throw new IllegalArgumentException("Char Start: " + charStart);
        }
        if (charEnd > text.length()) {
            throw new IllegalArgumentException("Char End: " + charEnd + ", Text Length: " + text.length());
        }
        if (charStart > charEnd) {
            throw new IllegalArgumentException("Bad Range: [" + charStart + ".." + charEnd + ")");
        }

        int length = charEnd - charStart;
        if (length > 0) {
            ArrayList<ScriptRun> runList = new ArrayList<>();
            int runStart = 0;
            byte current = scripts[charStart];

            for (int i = charStart + 1; i < charEnd; i++) {
                if (scripts[i] == current) {
                    continue;
                }

                runList.add(new ScriptRun(runStart, i, Script.valueOf(current)));
                runStart = i;
                current = scripts[i];
            }
            runList.add(new ScriptRun(runStart, charEnd, Script.valueOf(current)));

            return runList;
        }

        return Collections.emptyList();
    }

    private static native int nClassify(String text, byte[] scripts);
}
