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

import androidx.annotation.NonNull;

import com.mta.tehreer.test.HashableTestSuite;
import com.mta.tehreer.util.DescriptionBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BidiRunTest extends HashableTestSuite<BidiRun> {
    private int startIndex = 0;
    private int endIndex = 4;
    private byte embeddingLevel = 1;

    public BidiRunTest() {
        super(BidiRun.class);
    }

    @Override
    protected @NonNull BidiRun buildIdentical(BidiRun bidiRun) {
        return new BidiRun(bidiRun.charStart, bidiRun.charEnd, bidiRun.embeddingLevel);
    }

    @Before
    public void setUp() {
        sut = new BidiRun(startIndex, endIndex, embeddingLevel);
    }

    @Test
    public void testIsRightToLeftForEvenLevel() {
        // Given
        sut.embeddingLevel = 0;

        // When
        boolean isRightToLeft = sut.isRightToLeft();

        // Then
        assertFalse(isRightToLeft);
    }

    @Test
    public void testIsRightToLeftForOddLevel() {
        // Given
        sut.embeddingLevel = 1;

        // When
        boolean isRightToLeft = sut.isRightToLeft();

        // Then
        assertTrue(isRightToLeft);
    }

    @Test
    public void testToString() {
        String description = DescriptionBuilder
                .of(BidiRun.class)
                .put("charStart", startIndex)
                .put("charEnd", endIndex)
                .put("embeddingLevel", embeddingLevel)
                .build();

        // When
        String string = sut.toString();

        // Then
        assertEquals(string, description);
    }
}
