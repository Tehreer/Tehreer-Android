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

import androidx.annotation.NonNull;

import com.mta.tehreer.test.HashableTestSuite;
import com.mta.tehreer.util.DescriptionBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BidiPairTest extends HashableTestSuite<BidiPair> {
    private int charIndex = 0;
    private int actualCodePoint = '(';
    private int pairingCodePoint = ')';

    public BidiPairTest() {
        super(BidiPair.class);
    }

    @Override
    protected @NonNull BidiPair buildIdentical(BidiPair bidiPair) {
        return new BidiPair(bidiPair.charIndex, bidiPair.actualCodePoint, bidiPair.pairingCodePoint);
    }

    @Before
    public void setUp() {
        subject = new BidiPair(charIndex, actualCodePoint, pairingCodePoint);
    }

    @Test
    public void testToString() {
        String description = DescriptionBuilder
                .of(BidiPair.class)
                .put("charIndex", charIndex)
                .put("actualCodePoint", actualCodePoint)
                .put("pairingCodePoint", pairingCodePoint)
                .build();

        // When
        String string = subject.toString();

        // Then
        assertEquals(string, description);
    }
}
