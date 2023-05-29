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
import com.mta.tehreer.internal.util.Preconditions.checkArgument
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

internal class ShapeResolver(
    private val text: String,
    private val spanned: Spanned,
    private val defaultSpans: List<Any>
) {
    fun createParagraphsAndRuns(): Pair<ParagraphCollection, RunCollection> {
        val paragraphs = ParagraphCollection()
        val runs = RunCollection()

        var bidiAlgorithm: BidiAlgorithm? = null
        var shapingEngine: ShapingEngine? = null

        try {
            bidiAlgorithm = BidiAlgorithm(text)
            shapingEngine = ShapingEngine()

            val scriptClassifier = ScriptClassifier(text)
            val runLocator = ShapingRunLocator(spanned, defaultSpans)

            var paragraphStart = 0
            val suggestedEnd = text.length

            while (paragraphStart != suggestedEnd) {
                val paragraph = bidiAlgorithm.createParagraph(
                    paragraphStart,
                    suggestedEnd,
                    BaseDirection.DEFAULT_LEFT_TO_RIGHT
                )

                for (bidiRun in paragraph.logicalRuns) {
                    for (scriptRun in scriptClassifier.getScriptRuns(
                        bidiRun.charStart,
                        bidiRun.charEnd
                    )) {
                        val scriptTag = Script.getOpenTypeTag(scriptRun.script)
                        val writingDirection = ShapingEngine.getScriptDirection(scriptTag)

                        val isRTL = bidiRun.isRightToLeft
                        val isBackward = ((isRTL && writingDirection == WritingDirection.LEFT_TO_RIGHT)
                                      or (!isRTL && writingDirection == WritingDirection.RIGHT_TO_LEFT))
                        val shapingOrder = if (isBackward) ShapingOrder.BACKWARD else ShapingOrder.FORWARD

                        runLocator.reset(scriptRun.charStart, scriptRun.charEnd)

                        shapingEngine.scriptTag = scriptTag
                        shapingEngine.writingDirection = writingDirection
                        shapingEngine.shapingOrder = shapingOrder

                        resolveTypefaces(runs, runLocator, shapingEngine, bidiRun.embeddingLevel)
                    }
                }
                paragraphs.add(paragraph)

                paragraphStart = paragraph.charEnd
            }
        } finally {
            shapingEngine?.dispose()
            bidiAlgorithm?.dispose()
        }

        return Pair(paragraphs, runs)
    }

    private fun resolveTypefaces(
        runs: RunCollection,
        runLocator: ShapingRunLocator,
        shapingEngine: ShapingEngine, bidiLevel: Byte
    ) {
        var paint: Paint? = null
        var metrics: FontMetricsInt? = null

        while (runLocator.moveNext()) {
            val runStart = runLocator.runStart
            val runEnd = runLocator.runEnd

            val typeface = runLocator.typeface
            checkArgument(
                typeface != null,
                "No typeface is specified for range [$runStart, $runEnd)"
            )

            val typeSize = runLocator.typeSize
            val sizeByEm = typeSize / typeface!!.unitsPerEm
            val ascent = typeface.ascent * sizeByEm
            val descent = typeface.descent * sizeByEm
            val leading = typeface.leading * sizeByEm

            val replacement = runLocator.replacement
            var textRun: TextRun

            if (replacement == null) {
                shapingEngine.typeface = typeface
                shapingEngine.typeSize = typeSize

                var shapingResult: ShapingResult? = null

                try {
                    shapingResult = shapingEngine.shapeText(text, runStart, runEnd)

                    val writingDirection = shapingEngine.writingDirection
                    val isBackward = shapingResult.isBackward
                    val glyphIds = shapingResult.glyphIds.toArray()
                    val offsets = shapingResult.glyphOffsets.toArray()
                    val advances = shapingResult.glyphAdvances.toArray()
                    val clusterMap = shapingResult.clusterMap.toArray()
                    val caretEdges = shapingResult.getCaretEdges(null)

                    val scaleX = runLocator.scaleX
                    if (scaleX.compareTo(1.0f) != 0) {
                        for (i in glyphIds.indices) {
                            offsets[i * 2] *= scaleX
                            advances[i] *= scaleX
                        }

                        for (i in caretEdges.indices) {
                            caretEdges[i] *= scaleX
                        }
                    }

                    val baselineShift = runLocator.baselineShift
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
                        clusterMap = clusterMap.toIntList(),
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
                    charSequence = spanned,
                    startIndex = runStart,
                    endIndex = runEnd,
                    bidiLevel = bidiLevel,
                    replacementSpan = replacement,
                    paint = paint,
                    typeface = typeface,
                    typeSize = typeSize,
                    replacementAscent = metrics.ascent,
                    replacementDescent = metrics.descent,
                    replacementLeading = metrics.leading,
                    replacementExtent = extent,
                    caretEdges = caretEdges.toFloatList()
                )
            }

            runs.add(textRun)
        }
    }
}
