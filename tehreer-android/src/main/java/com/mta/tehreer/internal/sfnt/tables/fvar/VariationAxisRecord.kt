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

import androidx.annotation.IntDef
import com.mta.tehreer.internal.sfnt.SfntTable

private const val AXIS_TAG = 0
private const val MIN_VALUE = 4
private const val DEFAULT_VALUE = 8
private const val MAX_VALUE = 12
private const val FLAGS = 16
private const val AXIS_NAME_ID = 18

internal class VariationAxisRecord(
    private val data: SfntTable
) {
    @IntDef(flag = true, value = [FLAG_HIDDEN_AXIS])
    @Retention(AnnotationRetention.SOURCE)
    annotation class Flags

    fun axisTag() = data.readInt32(AXIS_TAG)

    fun minValue() = data.readFixed(MIN_VALUE)

    fun defaultValue() = data.readFixed(DEFAULT_VALUE)

    fun maxValue() = data.readFixed(MAX_VALUE)

    @Flags
    fun flags() = data.readUInt16(FLAGS)

    fun axisNameId() = data.readUInt16(AXIS_NAME_ID)

    companion object {
        const val FLAG_HIDDEN_AXIS = 0x0001
    }
}
