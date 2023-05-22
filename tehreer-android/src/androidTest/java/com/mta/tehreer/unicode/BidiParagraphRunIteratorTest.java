/*
 * Copyright (C) 2022-2023 Muhammad Tayyab Akram
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
    private BidiParagraph.RunIterator subject;

    @Before
    public void setUp() {
        subject = new BidiParagraph.RunIterator(paragraph);
    }

    @Test
    public void testHasNextForAvailableRun() {
        // Given
        subject.run = mock(BidiRun.class);

        // When
        boolean hasNext = subject.hasNext();

        // Then
        assertTrue(hasNext);
    }

    @Test
    public void testHasNextForNoAvailableRun() {
        // Given
        subject.run = null;

        // When
        boolean hasNext = subject.hasNext();

        // Then
        assertFalse(hasNext);
    }

    @Test
    public void testNextForAvailableRun() {
        BidiRun firstRun = new BidiRun(0, 4, (byte) 0);
        BidiRun secondRun = new BidiRun(4, 8, (byte) 1);

        when(paragraph.getOnwardRun(4)).thenReturn(secondRun);

        // Given
        subject.run = firstRun;

        // When
        BidiRun bidiRun = subject.next();

        // Then
        assertSame(bidiRun, firstRun);
        assertSame(subject.run, secondRun);
    }

    @Test(expected = NoSuchElementException.class)
    public void testNextForNoAvailableRun() {
        // Given
        subject.run = null;

        // When
        subject.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        subject.remove();
    }
}
