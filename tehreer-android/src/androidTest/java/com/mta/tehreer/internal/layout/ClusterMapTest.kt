/*
 * Copyright (C) 2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.layout

import com.mta.tehreer.collections.IntListTestSuite
import org.junit.Before

internal class ClusterMapTest : IntListTestSuite<ClusterMap>(
    ClusterMap::class.java
) {
    private fun buildList(values: IntArray, difference: Int): ClusterMap {
        return ClusterMap(values, 0, values.size, difference)
    }

    override fun buildIdentical(list: ClusterMap): ClusterMap {
        return buildList(list.toArray(), 0)
    }

    @Before
    fun setUp() {
        val sample = intArrayOf(
            0x0000FFFF, 0x1C72AAA9, 0x38E45553, 0x5555FFFD, 0x71C7AAA7,
            -0x71C6AAAF, -0x55550005, -0x38E3555B, -0x1C71AAB1, -0x00000007
        )
        val difference = 0xFFFF

        values = intArrayOf(
            0x00000000, 0x1C71AAAA, 0x38E35554, 0x5554FFFE, 0x71C6AAA8,
            -0x71C7AAAE, -0x55560004, -0x38E4555A, -0x1C72AAB0, -0x00010006
        )
        subject = buildList(sample, difference)
    }
}