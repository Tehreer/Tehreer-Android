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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Iterator;

@RunWith(MockitoJUnitRunner.class)
public class BidiLineMirrorIterableTest {
    private static final String DEFAULT_TEXT = "یہ ایک (car) ہے۔";

    private BidiLine bidiLine;
    private BidiLine.MirrorIterable subject;

    @Before
    public void setUp() {
        String text = DEFAULT_TEXT;
        BidiAlgorithm bidiAlgorithm = BidiAlgorithm.finalizable(
            new BidiAlgorithm(text)
        );
        BidiParagraph bidiParagraph = BidiParagraph.finalizable(
            bidiAlgorithm.createParagraph(0, text.length(), BaseDirection.DEFAULT_LEFT_TO_RIGHT)
        );
        bidiLine = BidiLine.finalizable(
            bidiParagraph.createLine(0, text.length())
        );
        subject = new BidiLine.MirrorIterable(bidiLine);
    }

    @Test
    public void testIterator() {
        // When
        Iterator<BidiPair> iterator = subject.iterator();

        // Then
        assertTrue(iterator instanceof BidiLine.MirrorIterator);
        assertSame(((BidiLine.MirrorIterator) iterator).owner, bidiLine);
    }
}
