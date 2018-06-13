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
import java.util.List;

public class ScriptClassifier {

    static {
        JniBridge.loadLibrary();
    }

    private final String text;
    private final byte[] scripts;
    private final int runCount;

    public ScriptClassifier(String text) {
        if (text == null) {
            throw new NullPointerException("Text is null");
        }

        this.text = text;
        this.scripts = new byte[text.length()];
        this.runCount = nClassify(text, scripts);
    }

    public String getText() {
        return text;
    }

    public List<ScriptRun> getRuns() {
        ArrayList<ScriptRun> runList = new ArrayList<>(runCount);
        int runStart = 0;

        int length = scripts.length;
        byte current = scripts[0];

        for (int i = 1; i < length; i++) {
            if (scripts[i] == current) {
                continue;
            }

            runList.add(new ScriptRun(runStart, i, Script.valueOf(current)));
            runStart = i;
            current = scripts[i];
        }
        runList.add(new ScriptRun(runStart, length, Script.valueOf(current)));

        return runList;
    }

    private static native int nClassify(String text, byte[] scripts);
}
