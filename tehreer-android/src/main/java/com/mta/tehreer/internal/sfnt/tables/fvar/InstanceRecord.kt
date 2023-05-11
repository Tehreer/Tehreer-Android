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

package com.mta.tehreer.internal.sfnt.tables.fvar

import com.mta.tehreer.internal.sfnt.SfntTable

private const val SUBFAMILY_NAME_ID = 0
private const val FLAGS = 2
private const val COORDINATES = 4

internal class InstanceRecord(
    data: SfntTable,
    axisCount: Int,
    instanceSize: Int
) {
    private val data: SfntTable
    private val axisCount: Int
    private val postScriptNameIDOffset: Int

    init {
        val coordsEndOffset = COORDINATES + axisCount * 4

        this.data = data
        this.axisCount = axisCount
        this.postScriptNameIDOffset = if (instanceSize - coordsEndOffset < 2) -1 else coordsEndOffset
    }

    fun subfamilyNameID() = data.readUInt16(SUBFAMILY_NAME_ID)

    fun flags() = data.readUInt16(FLAGS)

    fun coordinates(): FloatArray {
        val coordinates = FloatArray(axisCount)

        for (i in 0 until axisCount) {
            coordinates[i] = data.readFixed(COORDINATES + i * 4)
        }

        return coordinates
    }

    fun postScriptNameID(): Int {
        return if (postScriptNameIDOffset != -1) {
            data.readInt32(COORDINATES + axisCount * 4)
        } else -1
    }
}
