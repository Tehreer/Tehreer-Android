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

import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;

public class ScriptClassifierRunIteratorTest {
    private ScriptClassifier.RunIterator sut;

    private final @Script.Value
    byte[] scripts = {
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
