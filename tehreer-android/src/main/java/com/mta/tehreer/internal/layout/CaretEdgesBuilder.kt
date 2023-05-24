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

import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.collections.IntList

internal class CaretEdgesBuilder {
    private var isBackward = false
    private var isRTL = false
    private lateinit var glyphAdvances: FloatList
    private lateinit var clusterMap: IntList
    private var caretStops: BooleanArray? = null

    fun setBackward(backward: Boolean): CaretEdgesBuilder {
        isBackward = backward
        return this
    }

    fun setRTL(RTL: Boolean): CaretEdgesBuilder {
        isRTL = RTL
        return this
    }

    fun setGlyphAdvances(glyphAdvances: FloatList): CaretEdgesBuilder {
        this.glyphAdvances = glyphAdvances
        return this
    }

    fun setClusterMap(clusterMap: IntList): CaretEdgesBuilder {
        this.clusterMap = clusterMap
        return this
    }

    fun setCaretStops(caretStops: BooleanArray?): CaretEdgesBuilder {
        this.caretStops = caretStops
        return this
    }

    private fun buildCaretAdvances(): FloatArray {
        val codeUnitCount = clusterMap.size()
        val caretAdvances = FloatArray(codeUnitCount + 1)

        var glyphIndex = clusterMap[0] + 1
        var refIndex = glyphIndex
        var totalStops = 0
        var clusterStart = 0

        for (codeUnitIndex in 1..codeUnitCount) {
            val oldIndex = glyphIndex

            if (codeUnitIndex != codeUnitCount) {
                glyphIndex = clusterMap[codeUnitIndex] + 1

                if (caretStops != null && !caretStops!![codeUnitIndex - 1]) {
                    continue
                }

                totalStops += 1
            } else {
                totalStops += 1
                glyphIndex = if (isBackward) 0 else glyphAdvances.size() + 1
            }

            if (glyphIndex != oldIndex) {
                var clusterAdvance = 0f
                var distance = 0f
                var counter = 1

                // Find the advance of current cluster.
                if (isBackward) {
                    while (refIndex > glyphIndex) {
                        clusterAdvance += glyphAdvances[refIndex - 1]
                        refIndex -= 1
                    }
                } else {
                    while (refIndex < glyphIndex) {
                        clusterAdvance += glyphAdvances[refIndex - 1]
                        refIndex += 1
                    }
                }

                // Divide the advance evenly between cluster length.
                while (clusterStart < codeUnitIndex) {
                    var advance = 0f

                    if (caretStops == null || caretStops!![clusterStart] || clusterStart == codeUnitCount - 1) {
                        val previous = distance

                        distance = clusterAdvance * counter / totalStops
                        advance = distance - previous
                        counter += 1
                    }

                    caretAdvances[clusterStart] = advance
                    clusterStart += 1
                }

                totalStops = 0
            }
        }

        return caretAdvances
    }

    fun build(): FloatArray {
        val codeUnitCount = clusterMap.size()
        val caretEdges = buildCaretAdvances()
        var distance = 0f

        if (isRTL) {
            // Last edge should be zero.
            caretEdges[codeUnitCount] = 0f

            // Iterate in reverse direction.
            for (i in codeUnitCount - 1 downTo 0) {
                distance += caretEdges[i]
                caretEdges[i] = distance
            }
        } else {
            var advance = caretEdges[0]

            // First edge should be zero.
            caretEdges[0] = 0f

            for (i in 1..codeUnitCount) {
                distance += advance
                advance = caretEdges[i]
                caretEdges[i] = distance
            }
        }

        return caretEdges
    }
}
