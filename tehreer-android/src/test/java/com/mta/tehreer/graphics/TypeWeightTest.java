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

public class TypeWeightTest {

    @Test
    public void testOrdinals() {
        assertThat(TypeWeight.THIN.ordinal(), is(0));
        assertThat(TypeWeight.EXTRA_LIGHT.ordinal(), is(1));
        assertThat(TypeWeight.LIGHT.ordinal(), is(2));
        assertThat(TypeWeight.REGULAR.ordinal(), is(3));
        assertThat(TypeWeight.MEDIUM.ordinal(), is(4));
        assertThat(TypeWeight.SEMI_BOLD.ordinal(), is(5));
        assertThat(TypeWeight.BOLD.ordinal(), is(6));
        assertThat(TypeWeight.EXTRA_BOLD.ordinal(), is(7));
        assertThat(TypeWeight.HEAVY.ordinal(), is(8));
    }

    @Test
    public void testValues() {
        assertThat(TypeWeight.THIN.value, is(100));
        assertThat(TypeWeight.EXTRA_LIGHT.value, is(200));
        assertThat(TypeWeight.LIGHT.value, is(300));
        assertThat(TypeWeight.REGULAR.value, is(400));
        assertThat(TypeWeight.MEDIUM.value, is(500));
        assertThat(TypeWeight.SEMI_BOLD.value, is(600));
        assertThat(TypeWeight.BOLD.value, is(700));
        assertThat(TypeWeight.EXTRA_BOLD.value, is(800));
        assertThat(TypeWeight.HEAVY.value, is(900));
    }
}
