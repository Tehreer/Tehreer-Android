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

import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

public class ScriptClassifierRunIterableTest {
    private ScriptClassifier.RunIterable sut;

    private final @Script.Value
    byte[] scripts = {
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
