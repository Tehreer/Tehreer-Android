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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.internal.collections.JByteArrayIntList;
import com.mta.tehreer.util.DescriptionBuilder;

import org.junit.Before;
import org.junit.Test;

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

    @Test
    public void testToString() {
        String description = DescriptionBuilder
                .of(ScriptClassifier.class)
                .put("text", text)
                .put("charScripts", sut.getCharScripts())
                .put("scriptRuns", sut.getScriptRuns())
                .build();

        // When
        String string = sut.toString();

        // Then
        assertEquals(string, description);
    }
}
