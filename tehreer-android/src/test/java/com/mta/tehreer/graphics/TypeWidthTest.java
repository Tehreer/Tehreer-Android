/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.graphics;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TypeWidthTest {

    @Test
    public void testOrdinals() {
        assertThat(TypeWidth.ULTRA_CONDENSED.ordinal(), is(0));
        assertThat(TypeWidth.EXTRA_CONDENSED.ordinal(), is(1));
        assertThat(TypeWidth.CONDENSED.ordinal(), is(2));
        assertThat(TypeWidth.SEMI_CONDENSED.ordinal(), is(3));
        assertThat(TypeWidth.NORMAL.ordinal(), is(4));
        assertThat(TypeWidth.SEMI_EXPANDED.ordinal(), is(5));
        assertThat(TypeWidth.EXPANDED.ordinal(), is(6));
        assertThat(TypeWidth.EXTRA_EXPANDED.ordinal(), is(7));
        assertThat(TypeWidth.ULTRA_EXPANDED.ordinal(), is(8));
    }

    @Test
    public void testValues() {
        assertThat(TypeWidth.ULTRA_CONDENSED.value, is(1));
        assertThat(TypeWidth.EXTRA_CONDENSED.value, is(2));
        assertThat(TypeWidth.CONDENSED.value, is(3));
        assertThat(TypeWidth.SEMI_CONDENSED.value, is(4));
        assertThat(TypeWidth.NORMAL.value, is(5));
        assertThat(TypeWidth.SEMI_EXPANDED.value, is(6));
        assertThat(TypeWidth.EXPANDED.value, is(7));
        assertThat(TypeWidth.EXTRA_EXPANDED.value, is(8));
        assertThat(TypeWidth.ULTRA_EXPANDED.value, is(9));
    }
}
