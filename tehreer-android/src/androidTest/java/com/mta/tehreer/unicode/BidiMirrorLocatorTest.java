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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BidiMirrorLocatorTest {
    private static final String DEFAULT_TEXT = "یہ ایک (car) ہے۔";

    private BidiLine bidiLine;
    private BidiMirrorLocator subject;

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
        subject = new BidiMirrorLocator();
    }

    @After
    public void tearDown() {
        subject.dispose();
    }

    @Test
    public void testNativePointers() {
        assertNotEquals(subject.nativeMirrorLocator, 0);
    }

    @Test
    public void testNextPair() {
        subject.loadLine(bidiLine);

        // For First Mirror
        {
            // When
            BidiPair pair = subject.nextPair();

            // Then
            assertNotNull(pair);
            assertEquals(pair.charIndex, 11);
            assertEquals(pair.actualCodePoint, ')');
            assertEquals(pair.pairingCodePoint, '(');
        }

        // For Last Mirror
        {
            // When
            BidiPair pair = subject.nextPair();

            // Then
            assertNotNull(pair);
            assertEquals(pair.charIndex, 7);
            assertEquals(pair.actualCodePoint, '(');
            assertEquals(pair.pairingCodePoint, ')');
        }

        // For No Available Mirror
        {
            // When
            BidiPair pair = subject.nextPair();

            // Then
            assertNull(pair);
        }
    }
}
