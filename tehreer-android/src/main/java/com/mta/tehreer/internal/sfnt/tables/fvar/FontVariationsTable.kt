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

import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.sfnt.DataTable
import com.mta.tehreer.internal.sfnt.SfntTable
import com.mta.tehreer.sfnt.SfntTag

private const val MAJOR_VERSION = 0
private const val MINOR_VERSION = 2
private const val AXES_ARRAY_OFFSET = 4
private const val AXIS_COUNT = 8
private const val AXIS_SIZE = 10
private const val INSTANCE_COUNT = 12
private const val INSTANCE_SIZE = 14

internal class FontVariationsTable(
    private val data: SfntTable
) {
    fun majorVersion() = data.readUInt16(MAJOR_VERSION)

    fun minorVersion() = data.readUInt16(MINOR_VERSION)

    fun axesArrayOffset() = data.readUInt16(AXES_ARRAY_OFFSET)

    fun axisCount() = data.readUInt16(AXIS_COUNT)

    fun axisSize() = data.readUInt16(AXIS_SIZE)

    fun instanceCount() = data.readUInt16(INSTANCE_COUNT)

    fun instanceSize() = data.readUInt16(INSTANCE_SIZE)

    fun axisRecords(): Array<VariationAxisRecord?> {
        val axesArrayOffset = axesArrayOffset()
        val axisCount = axisCount()
        val axisSize = axisSize()
        val axisRecords = arrayOfNulls<VariationAxisRecord>(axisCount)

        for (i in 0 until axisCount) {
            axisRecords[i] = VariationAxisRecord(data.subTable(axesArrayOffset + i * axisSize))
        }

        return axisRecords
    }

    fun instanceRecords(): Array<InstanceRecord?> {
        val axesArrayOffset = axesArrayOffset()
        val axisCount = axisCount()
        val axisSize = axisSize()
        val axesEndOffset = axesArrayOffset + axisCount * axisSize
        val instanceCount = instanceCount()
        val instanceSize = instanceSize()
        val instanceRecords = arrayOfNulls<InstanceRecord>(instanceCount)

        for (i in 0 until instanceCount) {
            instanceRecords[i] = InstanceRecord(
                data.subTable(axesEndOffset + i * instanceSize),
                axisCount,
                instanceSize
            )
        }

        return instanceRecords
    }

    companion object {
        @JvmStatic
        fun from(typeface: Typeface) = with(typeface) {
            getTableData(SfntTag.make("fvar"))?.let {
                FontVariationsTable(DataTable(it))
            }
        }
    }
}
