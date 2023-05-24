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

import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.text.Spanned
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.internal.util.Preconditions
import com.mta.tehreer.internal.util.isEven
import com.mta.tehreer.internal.util.isOdd
import com.mta.tehreer.internal.util.toFloatList
import com.mta.tehreer.internal.util.toIntList
import com.mta.tehreer.internal.util.toPointList
import com.mta.tehreer.sfnt.ShapingEngine
import com.mta.tehreer.sfnt.ShapingOrder
import com.mta.tehreer.sfnt.ShapingResult
import com.mta.tehreer.sfnt.WritingDirection
import com.mta.tehreer.unicode.*

internal object ShapeResolver {
    @JvmStatic
    fun fillRuns(
        text: String, spanned: Spanned,
        defaultSpans: List<Any>,
        paragraphs: MutableList<BidiParagraph?>,
        runs: MutableList<TextRun>
    ) {
        var bidiAlgorithm: BidiAlgorithm? = null
        var shapingEngine: ShapingEngine? = null

        try {
            bidiAlgorithm = BidiAlgorithm(text)
            shapingEngine = ShapingEngine()

            val scriptClassifier = ScriptClassifier(text)
            val locator = ShapingRunLocator(spanned, defaultSpans)

            val baseDirection = BaseDirection.DEFAULT_LEFT_TO_RIGHT

            var paragraphStart = 0
            val suggestedEnd = text.length

            while (paragraphStart != suggestedEnd) {
                val paragraph = bidiAlgorithm.createParagraph(
                    paragraphStart,
                    suggestedEnd,
                    baseDirection
                )

                for (bidiRun in paragraph.logicalRuns) {
                    for (scriptRun in scriptClassifier.getScriptRuns(
                        bidiRun.charStart,
                        bidiRun.charEnd
                    )) {
                        val scriptTag = Script.getOpenTypeTag(scriptRun.script)
                        val writingDirection = ShapingEngine.getScriptDirection(scriptTag)

                        val isOddLevel = bidiRun.embeddingLevel.isOdd()
                        val isBackward =
                            ((isOddLevel && writingDirection == WritingDirection.LEFT_TO_RIGHT)
                                    or (!isOddLevel && writingDirection == WritingDirection.RIGHT_TO_LEFT))
                        val shapingOrder =
                            if (isBackward) ShapingOrder.BACKWARD else ShapingOrder.FORWARD

                        locator.reset(scriptRun.charStart, scriptRun.charEnd)

                        shapingEngine.scriptTag = scriptTag
                        shapingEngine.writingDirection = writingDirection
                        shapingEngine.shapingOrder = shapingOrder

                        resolveTypefaces(
                            text,
                            spanned,
                            runs,
                            locator,
                            shapingEngine,
                            bidiRun.embeddingLevel
                        )
                    }
                }
                paragraphs.add(paragraph)

                paragraphStart = paragraph.charEnd
            }
        } finally {
            shapingEngine?.dispose()
            bidiAlgorithm?.dispose()
        }
    }

    private fun resolveTypefaces(
        text: String, spanned: Spanned,
        runs: MutableList<TextRun>,
        locator: ShapingRunLocator,
        engine: ShapingEngine, bidiLevel: Byte
    ) {
        var paint: Paint? = null
        var metrics: FontMetricsInt? = null

        while (locator.moveNext()) {
            val runStart = locator.runStart
            val runEnd = locator.runEnd

            val typeface = locator.typeface
            Preconditions.checkArgument(
                typeface != null,
                "No typeface is specified for range [$runStart, $runEnd)"
            )

            val typeSize = locator.typeSize
            val sizeByEm = typeSize / typeface!!.unitsPerEm
            val ascent = typeface.ascent * sizeByEm
            val descent = typeface.descent * sizeByEm
            val leading = typeface.leading * sizeByEm

            val replacement = locator.replacement
            var textRun: TextRun

            if (replacement == null) {
                engine.typeface = typeface
                engine.typeSize = typeSize

                var shapingResult: ShapingResult? = null

                try {
                    shapingResult = engine.shapeText(text, runStart, runEnd)

                    val writingDirection = engine.writingDirection
                    val isBackward = shapingResult.isBackward
                    val glyphIds = shapingResult.glyphIds.toArray()
                    val offsets = shapingResult.glyphOffsets.toArray()
                    val advances = shapingResult.glyphAdvances.toArray()
                    val clusterMap = shapingResult.clusterMap.toArray()
                    val caretEdges = shapingResult.getCaretEdges(null)

                    val scaleX = locator.scaleX
                    if (scaleX.compareTo(1.0f) != 0) {
                        for (i in glyphIds.indices) {
                            offsets[i * 2] *= scaleX
                            advances[i] *= scaleX
                        }

                        for (i in caretEdges.indices) {
                            caretEdges[i] *= scaleX
                        }
                    }

                    val baselineShift = locator.baselineShift
                    if (baselineShift.compareTo(0.0f) != 0) {
                        for (i in glyphIds.indices) {
                            offsets[i * 2 + 1] += baselineShift
                        }
                    }

                    textRun = IntrinsicRun(
                        startIndex = runStart,
                        endIndex = runEnd,
                        isBackward = isBackward,
                        bidiLevel = bidiLevel,
                        writingDirection = writingDirection,
                        typeface = typeface,
                        typeSize = typeSize,
                        ascent = ascent,
                        descent = descent,
                        leading = leading,
                        glyphIds = glyphIds.toIntList(),
                        glyphOffsets = offsets.toPointList(),
                        glyphAdvances = advances.toFloatList(),
                        clusterMapArray = clusterMap,
                        caretEdges = caretEdges.toFloatList()
                    )
                } finally {
                    shapingResult?.dispose()
                }
            } else {
                if (paint == null) {
                    paint = Paint()
                }
                if (metrics == null) {
                    metrics = FontMetricsInt()
                }

                metrics.ascent = -(ascent + 0.5f).toInt()
                metrics.descent = (descent + 0.5f).toInt()
                metrics.leading = (leading + 0.5f).toInt()

                val extent = replacement.getSize(paint, spanned, runStart, runEnd, metrics)
                val runLength = runEnd - runStart

                val caretEdges = FloatArray(runLength + 1)

                if (bidiLevel.isEven()) {
                    caretEdges[runLength] = extent.toFloat()
                } else {
                    caretEdges[0] = extent.toFloat()
                }

                textRun = ReplacementRun(
                    spanned, runStart, runEnd, bidiLevel,
                    replacement, paint, typeface, typeSize,
                    metrics.ascent, metrics.descent, metrics.leading,
                    extent, FloatList.of(*caretEdges)
                )
            }

            runs.add(textRun)
        }
    }
}
