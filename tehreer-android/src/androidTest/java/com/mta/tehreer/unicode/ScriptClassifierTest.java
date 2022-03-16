/*
 * Copyright (C) 2022 Muhammad Tayyab Akram
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.internal.collections.JByteArrayIntList;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ScriptClassifierTest {
    private ScriptClassifier sut;

    private final String text = "abcdابجد";

    @Before
    public void setUp() {
        sut = new ScriptClassifier(text);
    }

    @Test
    public void testGetText() {
        assertEquals(sut.getText(), text);
    }

    @Test
    public void testGetCharScripts() {
        IntList charScripts = sut.getCharScripts();

        assertTrue(charScripts instanceof JByteArrayIntList);
    }

    @Test
    public void testGetScriptRunsForInvalidRange() {
        // Invalid Start
        try {
            sut.getScriptRuns(-1, 8);
            fail();
        } catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(), "Char Start: -1");
        }

        // Invalid End
        try {
            sut.getScriptRuns(0, 9);
            fail();
        } catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(), "Char End: 9, Text Length: 8");
        }

        // Empty Range
        try {
            sut.getScriptRuns(0, 0);
            fail();
        } catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(), "Bad Range: [0, 0)");
        }
    }

    @Test
    public void testGetScriptRunsForPartialRange() {
        Iterable<ScriptRun> iterable = sut.getScriptRuns(1, 7);
        assertTrue(iterable instanceof ScriptClassifier.RunIterable);

        ScriptClassifier.RunIterable runIterable = (ScriptClassifier.RunIterable) iterable;
        assertEquals(runIterable.start, 1);
        assertEquals(runIterable.end, 7);
    }

    @Test
    public void testGetScriptRunsForFullRange() {
        Iterable<ScriptRun> iterable = sut.getScriptRuns();
        assertTrue(iterable instanceof ScriptClassifier.RunIterable);

        ScriptClassifier.RunIterable runIterable = (ScriptClassifier.RunIterable) iterable;
        assertEquals(runIterable.start, 0);
        assertEquals(runIterable.end, 8);
    }

    public static class RunIteratorTest {
        private ScriptClassifier.RunIterator sut;

        private final @Script.Value byte[] scripts = {
            Script.LATIN, Script.COMMON, Script.ARABIC
        };

        @Before
        public void setUp() {
            sut = new ScriptClassifier.RunIterator(scripts, 0, scripts.length);
        }

        @Test
        public void testHasNextForValidIndexes() {
            sut.index = 0;
            assertTrue(sut.hasNext());

            sut.index = 1;
            assertTrue(sut.hasNext());

            sut.index = 2;
            assertTrue(sut.hasNext());
        }

        @Test
        public void testHasNextForEndingIndex() {
            sut.index = scripts.length;
            assertFalse(sut.hasNext());
        }

        @Test
        public void testNextForFirstRun() {
            ScriptRun scriptRun;

            // Given
            sut.index = 0;

            // When
            scriptRun = sut.next();

            // Then
            assertEquals(sut.index, 1);
            assertEquals(scriptRun.charStart, 0);
            assertEquals(scriptRun.charEnd, 1);
            assertEquals(scriptRun.script, Script.LATIN);
        }

        @Test
        public void testNextForMidRun() {
            ScriptRun scriptRun;

            // Given
            sut.index = 1;

            // When
            scriptRun = sut.next();

            // Then
            assertEquals(sut.index, 2);
            assertEquals(scriptRun.charStart, 1);
            assertEquals(scriptRun.charEnd, 2);
            assertEquals(scriptRun.script, Script.COMMON);
        }

        @Test
        public void testNextForLastRun() {
            ScriptRun scriptRun;

            // Given
            sut.index = 2;

            // When
            scriptRun = sut.next();

            // Then
            assertEquals(sut.index, 3);
            assertEquals(scriptRun.charStart, 2);
            assertEquals(scriptRun.charEnd, 3);
            assertEquals(scriptRun.script, Script.ARABIC);
        }

        @Test(expected = NoSuchElementException.class)
        public void testNextForNoAvailableRun() {
            // Given
            sut.index = 3;

            // When
            sut.next();
        }

        @Test(expected = UnsupportedOperationException.class)
        public void testRemove() {
            sut.remove();
        }
    }

    public static class RunIterableTest {
        private ScriptClassifier.RunIterable sut;

        private final @Script.Value byte[] scripts = {
            Script.LATIN, Script.COMMON, Script.ARABIC
        };

        @Before
        public void setUp() {
            sut = new ScriptClassifier.RunIterable(scripts, 0, 3);
        }

        @Test
        public void testIterator() {
            Iterator<ScriptRun> iterator = sut.iterator();
            assertTrue(iterator instanceof ScriptClassifier.RunIterator);

            ScriptClassifier.RunIterator runIterator = (ScriptClassifier.RunIterator) iterator;
            assertEquals(runIterator.scripts, scripts);
            assertEquals(runIterator.index, 0);
            assertEquals(runIterator.end, 3);
        }
    }
}
