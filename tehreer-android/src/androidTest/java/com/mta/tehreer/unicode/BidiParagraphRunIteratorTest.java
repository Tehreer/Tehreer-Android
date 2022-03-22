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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.NoSuchElementException;

@RunWith(MockitoJUnitRunner.class)
public class BidiParagraphRunIteratorTest {
    @Mock
    private BidiParagraph paragraph;
    private BidiParagraph.RunIterator sut;

    @Before
    public void setUp() {
        sut = new BidiParagraph.RunIterator(paragraph);
    }

    @Test
    public void testHasNextForAvailableRun() {
        // Given
        sut.run = mock(BidiRun.class);

        // When
        boolean hasNext = sut.hasNext();

        // Then
        assertTrue(hasNext);
    }

    @Test
    public void testHasNextForNoAvailableRun() {
        // Given
        sut.run = null;

        // When
        boolean hasNext = sut.hasNext();

        // Then
        assertFalse(hasNext);
    }

    @Test
    public void testNextForAvailableRun() {
        BidiRun firstRun = new BidiRun(0, 4, (byte) 0);
        BidiRun secondRun = new BidiRun(4, 8, (byte) 1);

        when(paragraph.getOnwardRun(4)).thenReturn(secondRun);

        // Given
        sut.run = firstRun;

        // When
        BidiRun bidiRun = sut.next();

        // Then
        assertSame(bidiRun, firstRun);
        assertSame(sut.run, secondRun);
    }

    @Test(expected = NoSuchElementException.class)
    public void testNextForNoAvailableRun() {
        // Given
        sut.run = null;

        // When
        sut.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        sut.remove();
    }
}
