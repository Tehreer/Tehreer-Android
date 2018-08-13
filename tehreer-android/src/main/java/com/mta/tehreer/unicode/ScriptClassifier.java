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

import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.internal.JniBridge;
import com.mta.tehreer.internal.collections.JByteArrayIntList;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class implements UAX #24 available at
 * <a href="http://www.unicode.org/reports/tr24">http://www.unicode.org/reports/tr24</a>.
 */
public class ScriptClassifier {
    static {
        JniBridge.loadLibrary();
    }

    private final String text;
    private final byte[] scripts;

    /**
     * Constructs a script classifier object for the specified text.
     *
     * @param text The text whose script classification is desired.
     *
     * @throws NullPointerException if <code>text</code> is null.
     */
    public ScriptClassifier(String text) {
        if (text == null) {
            throw new NullPointerException("Text is null");
        }

        this.text = text;
        this.scripts = new byte[text.length()];

        nClassify(text, scripts);
    }

    /**
     * Returns the text that the script classifier object was created for.
     *
     * @return The text that the script classifier object was created for.
     */
    public String getText() {
        return text;
    }

    /**
     * Returns a list containing the resolved scripts of all characters in source text. The valid
     * script values are available in {@link Script} class as static constants.
     *
     * @return A list containing the resolved scripts of all characters in source text.
     */
    public IntList getCharScripts() {
        return new JByteArrayIntList(scripts, 0, scripts.length);
    }

    /**
     * Returns an iterable of resolved script runs in source text.
     *
     * @return An iterable of resolved script runs in source text.
     */
    public Iterable<ScriptRun> getScriptRuns() {
        return getScriptRuns(0, scripts.length);
    }

    /**
     * Returns an iterable of resolved script runs within the specified range of source text.
     *
     * @param charStart The index to the first character in source text.
     * @param charEnd The index after the last character in source text.
     * @return An iterable of script runs within the specified range of source text.
     */
    public Iterable<ScriptRun> getScriptRuns(int charStart, int charEnd) {
        if (charStart < 0) {
            throw new IllegalArgumentException("Char Start: " + charStart);
        }
        if (charEnd > text.length()) {
            throw new IllegalArgumentException("Char End: " + charEnd + ", Text Length: " + text.length());
        }
        if (charStart > charEnd) {
            throw new IllegalArgumentException("Bad Range: [" + charStart + ".." + charEnd + ")");
        }

        return new RunIterable(scripts, charStart, charEnd);
    }

    private static native void nClassify(String text, byte[] scripts);

    private static class RunIterator implements Iterator<ScriptRun> {
        final byte[] scripts;
        final int end;
        int index;

        RunIterator(byte[] scripts, int start, int end) {
            this.scripts = scripts;
            this.end = end;
            this.index = start;
        }

        @Override
        public boolean hasNext() {
            return index != end;
        }

        @Override
        public ScriptRun next() {
            if (index == end) {
                throw new NoSuchElementException();
            }

            byte current = scripts[index];
            int start = index;

            for (index += 1; index < end; index++) {
                if (scripts[index] != current) {
                    break;
                }
            }

            return new ScriptRun(start, index, current);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static class RunIterable implements Iterable<ScriptRun> {
        final byte[] scripts;
        final int start;
        final int end;

        RunIterable(byte[] scripts, int start, int end) {
            this.scripts = scripts;
            this.start = start;
            this.end = end;
        }

        @Override
        public Iterator<ScriptRun> iterator() {
            return new RunIterator(scripts, start, end);
        }
    }
}
