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

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.PointListTestSuite;
import com.mta.tehreer.sfnt.ShapingResult.GlyphOffsetList;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GlyphOffsetListTest extends PointListTestSuite<GlyphOffsetList> {
    public GlyphOffsetListTest() {
        super(GlyphOffsetList.class);
    }

    private @NonNull GlyphOffsetList buildList(@NonNull float[] values) {
        ShapingResult shapingResult = mock(ShapingResult.class);
        when(shapingResult.getGlyphCount()).thenReturn(values.length / 2);
        when(shapingResult.getGlyphXOffset(anyInt())).thenAnswer((invocation) -> {
            int index = invocation.getArgument(0);

            assertTrue(index >= 0);
            assertTrue(index < values.length / 2);

            return values[index * 2];
        });
        when(shapingResult.getGlyphYOffset(anyInt())).thenAnswer((invocation) -> {
            int index = invocation.getArgument(0);

            assertTrue(index >= 0);
            assertTrue(index < values.length / 2);

            return values[index * 2 + 1];
        });

        doAnswer((invocation -> {
            int offset = invocation.getArgument(0);
            int length = invocation.getArgument(1);
            float[] destination = invocation.getArgument(2);
            int index = invocation.getArgument(3);

            assertTrue(offset >= 0);
            assertTrue(length >= 0);
            assertNotNull(destination);
            assertTrue(index >= 0);
            assertTrue(offset + length <= values.length / 2);
            assertTrue(index + (length * 2) <= destination.length);

            System.arraycopy(values, offset * 2, destination, index, length * 2);

            return null;
        })).when(shapingResult).copyGlyphOffsets(anyInt(), anyInt(), any(), anyInt());

        return new GlyphOffsetList(shapingResult);
    }

    @Override
    protected @NonNull GlyphOffsetList buildIdentical(@NonNull GlyphOffsetList list) {
        return buildList(list.toArray());
    }

    @Before
    public void setUp() {
        values = new float[] {
            -1.0f, -0.9f,  -0.8f, -0.7f,  -0.6f, -0.5f,  -0.4f, -0.3f,  -0.2f, -0.1f,
            0.0f, 0.1f,  0.2f, 0.3f,  0.4f, 0.5f,  0.6f, 0.7f,  0.8f, 0.9f,  1.0f, 1.1f
        };
        sut = buildList(values);
    }
}
