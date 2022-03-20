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
public class BidiLineMirrorIteratorTest {
    @Mock
    private BidiLine line;
    @Mock
    private BidiMirrorLocator locator;
    private BidiLine.MirrorIterator sut;

    @Before
    public void setUp() {
        sut = new BidiLine.MirrorIterator(line, locator);
    }

    @Test
    public void testHasNextForAvailablePair() {
        // Given
        sut.pair = mock(BidiPair.class);

        // When
        boolean hasNext = sut.hasNext();

        // Then
        assertTrue(hasNext);
    }

    @Test
    public void testHasNextForNoAvailablePair() {
        // Given
        sut.pair = null;

        // When
        boolean hasNext = sut.hasNext();

        // Then
        assertFalse(hasNext);
    }

    @Test
    public void testNextForAvailablePair() {
        BidiPair firstPair = new BidiPair(0, '(', ')');
        BidiPair secondPair = new BidiPair(1, ')', '(');

        when(locator.nextPair()).thenReturn(secondPair);

        // Given
        sut.pair = firstPair;

        // When
        BidiPair bidiPair = sut.next();

        // Then
        assertSame(bidiPair, firstPair);
        assertSame(sut.pair, secondPair);
    }

    @Test(expected = NoSuchElementException.class)
    public void testNextForNoAvailablePair() {
        // Given
        sut.pair = null;

        // When
        sut.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        sut.remove();
    }
}
