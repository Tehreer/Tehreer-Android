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

import static org.junit.Assert.assertEquals;

import androidx.annotation.NonNull;

import com.mta.tehreer.test.HashableTestSuite;
import com.mta.tehreer.util.DescriptionBuilder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenTypeFeatureTest extends HashableTestSuite<OpenTypeFeature> {
    private int tag = SfntTag.make("aalt");
    private int value = 2;

    public OpenTypeFeatureTest() {
        super(OpenTypeFeature.class);
    }

    @Override
    protected @NonNull OpenTypeFeature buildIdentical(@NonNull OpenTypeFeature openTypeFeature) {
        return OpenTypeFeature.of(openTypeFeature.tag(), openTypeFeature.value());
    }

    @Before
    public void setUp() {
        sut = OpenTypeFeature.of(tag, value);
    }

    @Test
    public void testToString() {
        String description = DescriptionBuilder
                .of(OpenTypeFeature.class)
                .put("tag", SfntTag.toString(tag))
                .put("value", value)
                .build();

        // When
        String string = sut.toString();

        // Then
        assertEquals(string, description);
    }
}
