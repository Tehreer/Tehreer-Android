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

package com.mta.tehreer.sfnt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mta.tehreer.collections.IntListTestSuite;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GlyphIdListTest extends IntListTestSuite {
    @Before
    public void setUp() {
        int[] sample = {
            0x00000000, 0x1C71C71C, 0x38E38E38, 0x55555554, 0x71C71C70,
            0x8E38E38C, 0xAAAAAAA8, 0xC71C71C4, 0xE38E38E0, 0xFFFFFFFC
        };

        ShapingResult shapingResult = mock(ShapingResult.class);
        when(shapingResult.getGlyphCount()).thenReturn(sample.length);
        when(shapingResult.getGlyphId(anyInt())).thenAnswer((invocation) -> {
            int index = invocation.getArgument(0);

            assertTrue(index >= 0);
            assertTrue(index < sample.length);

            return sample[index];
        });

        doAnswer((invocation -> {
            int offset = invocation.getArgument(0);
            int length = invocation.getArgument(1);
            int[] destination = invocation.getArgument(2);
            int index = invocation.getArgument(3);

            assertTrue(offset >= 0);
            assertTrue(length >= 0);
            assertNotNull(destination);
            assertTrue(index >= 0);
            assertTrue(offset + length <= sample.length);
            assertTrue(index + length <= destination.length);

            System.arraycopy(sample, offset, destination, index, length);

            return null;
        })).when(shapingResult).copyGlyphIds(anyInt(), anyInt(), any(), anyInt());

        this.expected = sample;
        this.actual = new ShapingResult.GlyphIdList(shapingResult);
    }
}
