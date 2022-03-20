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
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BidiLineRunListTest {
    private static final int DEFAULT_SIZE = 2;

    @Mock
    private BidiLine line;
    private BidiLine.RunList sut;

    @Before
    public void setUp() {
        when(line.getRunCount()).thenReturn(DEFAULT_SIZE);

        sut = new BidiLine.RunList(line);
    }

    @Test
    public void testSize() {
        // When
        int size = sut.size();

        // Then
        assertEquals(size, DEFAULT_SIZE);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetForNegativeIndex() {
        // When
        sut.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetForLimitIndex() {
        // When
        sut.get(DEFAULT_SIZE);
    }

    @Test
    public void testGetForFirstIndex() {
        BidiRun anyRun = new BidiRun();
        when(line.getVisualRun(0)).thenReturn(anyRun);

        // When
        BidiRun run = sut.get(0);

        // Then
        assertSame(run, anyRun);
    }

    @Test
    public void testGetForLastIndex() {
        BidiRun anyRun = new BidiRun();
        when(line.getVisualRun(1)).thenReturn(anyRun);

        // When
        BidiRun run = sut.get(1);

        // Then
        assertSame(run, anyRun);
    }
}
