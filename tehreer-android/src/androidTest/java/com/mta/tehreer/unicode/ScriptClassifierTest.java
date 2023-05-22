/*
 * Copyright (C) 2023 Muhammad Tayyab Akram
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.internal.collections.JByteArrayIntList;
import com.mta.tehreer.util.DescriptionBuilder;

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

public class ScriptClassifierTest {
    private ScriptClassifier subject;

    private final String text = "abcdابجد";

    @Before
    public void setUp() {
        subject = new ScriptClassifier(text);
    }

    @Test
    public void testGetText() {
        assertEquals(subject.getText(), text);
    }

    @Test
    public void testGetCharScripts() {
        IntList charScripts = subject.getCharScripts();

        assertTrue(charScripts instanceof JByteArrayIntList);
    }

    @Test
    public void testGetScriptRunsForInvalidRange() {
        // Invalid Start
        try {
            subject.getScriptRuns(-1, 8);
            fail();
        } catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(), "Char Start: -1");
        }

        // Invalid End
        try {
            subject.getScriptRuns(0, 9);
            fail();
        } catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(), "Char End: 9, Text Length: 8");
        }

        // Empty Range
        try {
            subject.getScriptRuns(0, 0);
            fail();
        } catch (IllegalArgumentException exception) {
            assertEquals(exception.getMessage(), "Bad Range: [0, 0)");
        }
    }

    @Test
    public void testGetScriptRunsForPartialRange() {
        Iterator<ScriptRun> iterator = subject.getScriptRuns(1, 7);
        assertTrue(iterator instanceof ScriptClassifier.RunIterator);

        ScriptClassifier.RunIterator runIterator = (ScriptClassifier.RunIterator) iterator;
        assertEquals(runIterator.index, 1);
        assertEquals(runIterator.end, 7);
    }

    @Test
    public void testGetScriptRunsForFullRange() {
        Iterator<ScriptRun> iterator = subject.getScriptRuns();
        assertTrue(iterator instanceof ScriptClassifier.RunIterator);

        ScriptClassifier.RunIterator runIterator = (ScriptClassifier.RunIterator) iterator;
        assertEquals(runIterator.index, 0);
        assertEquals(runIterator.end, 8);
    }

    @Test
    public void testToString() {
        String description = DescriptionBuilder
                .of(ScriptClassifier.class)
                .put("text", text)
                .put("charScripts", subject.getCharScripts())
                .put("scriptRuns", subject.getScriptRuns())
                .build();

        // When
        String string = subject.toString();

        // Then
        assertEquals(string, description);
    }
}
