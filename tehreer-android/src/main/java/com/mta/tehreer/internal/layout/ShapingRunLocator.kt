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

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ReplacementSpan
import android.text.style.MetricAffectingSpan
import com.mta.tehreer.layout.style.TypeSizeSpan
import android.text.style.AbsoluteSizeSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.text.style.ScaleXSpan
import android.text.style.SuperscriptSpan
import android.text.style.SubscriptSpan
import com.mta.tehreer.graphics.*
import com.mta.tehreer.layout.style.TypefaceSpan

private class ShapingRun {
    var runStart = 0
    var runEnd = 0
    var replacement: ReplacementSpan? = null
    var typeface: com.mta.tehreer.graphics.Typeface? = null
    var typeWeight = TypeWeight.REGULAR
    var typeSlope = TypeSlope.PLAIN
    var typeSize = 0f
    var scaleX = 0f
    var baselineShift = 0f
}

private fun resolveInitial(spans: Array<Any>): ShapingRun {
    val shapingRun = ShapingRun()
    shapingRun.typeSize = 16.0f
    shapingRun.scaleX = 1.0f

    resolveSpans(shapingRun, spans)

    return shapingRun
}

private fun resolveSpans(shapingRun: ShapingRun, spans: Array<Any>) {
    for (span in spans) {
        if (span is TypefaceSpan) {
            val typeface = span.typeface
            shapingRun.typeface = typeface
            shapingRun.typeWeight = typeface.weight
            shapingRun.typeSlope = typeface.slope
        } else if (span is TypeSizeSpan) {
            shapingRun.typeSize = span.size
        } else if (span is android.text.style.TypefaceSpan) {
            resolveTypeface(shapingRun, span.family ?: "", TypeWidth.NORMAL)
        } else if (span is AbsoluteSizeSpan) {
            shapingRun.typeSize = span.size.toFloat()
        } else if (span is RelativeSizeSpan) {
            shapingRun.typeSize *= span.sizeChange
        } else if (span is StyleSpan) {
            resolveStyle(shapingRun, span.style)
            updateTypeface(shapingRun)
        } else if (span is TextAppearanceSpan) {
            shapingRun.typeSize = span.textSize.toFloat()
            resolveStyle(shapingRun, span.textStyle)
            val familyName = span.family
            if (familyName != null) {
                resolveTypeface(shapingRun, familyName, TypeWidth.NORMAL)
            } else {
                updateTypeface(shapingRun)
            }
        } else if (span is ScaleXSpan) {
            shapingRun.scaleX = span.scaleX
        } else if (span is SuperscriptSpan) {
            resolveBaselineShift(shapingRun, 0.5f)
        } else if (span is SubscriptSpan) {
            resolveBaselineShift(shapingRun, -0.5f)
        } else if (span is ReplacementSpan) {
            shapingRun.replacement = span
        }
    }

    if (shapingRun.typeSize < 0.0f) {
        shapingRun.typeSize = 0.0f
    }
}

private fun resolveStyle(shapingRun: ShapingRun, newStyle: Int) {
    when (newStyle) {
        Typeface.NORMAL -> shapingRun.typeWeight = TypeWeight.REGULAR
        Typeface.BOLD -> shapingRun.typeWeight = TypeWeight.BOLD
        Typeface.ITALIC -> shapingRun.typeSlope = TypeSlope.ITALIC
        Typeface.BOLD_ITALIC -> {
            shapingRun.typeWeight = TypeWeight.BOLD
            shapingRun.typeSlope = TypeSlope.ITALIC
        }
    }
}

private fun resolveTypeface(
    shapingRun: ShapingRun,
    familyName: String,
    typeWidth: TypeWidth
) {
    val typeFamily = TypefaceManager.getTypeFamily(familyName)
    if (typeFamily != null) {
        shapingRun.typeface = typeFamily.getTypefaceByStyle(
            typeWidth,
            shapingRun.typeWeight,
            shapingRun.typeSlope
        )
    } else {
        shapingRun.typeface = null
    }
}

private fun updateTypeface(shapingRun: ShapingRun) {
    val typeface = shapingRun.typeface
    if (typeface != null) {
        resolveTypeface(shapingRun, typeface.familyName, typeface.width)
    }
}

private fun resolveBaselineShift(shapingRun: ShapingRun, multiplier: Float) {
    val typeface = shapingRun.typeface
    if (typeface != null) {
        val sizeByEm = shapingRun.typeSize / typeface.unitsPerEm
        val ascent = typeface.ascent * sizeByEm
        shapingRun.baselineShift = ascent * multiplier
    }
}

internal class ShapingRunLocator(
    private val spanned: Spanned,
    defaultSpans: List<Any>
) {
    private val initial: ShapingRun

    private var limit = 0
    private var current: ShapingRun? = null
    private var next: ShapingRun? = null

    init {
        initial = resolveInitial(defaultSpans.toTypedArray())
    }

    private fun resolveRun(runStart: Int): ShapingRun? {
        if (runStart < limit) {
            val runEnd = spanned.nextSpanTransition(runStart, limit, MetricAffectingSpan::class.java)
            val spans = spanned.getSpans(runStart, runEnd, MetricAffectingSpan::class.java)

            val shapingRun = ShapingRun()
            shapingRun.runStart = runStart
            shapingRun.runEnd = runEnd
            shapingRun.typeface = initial.typeface
            shapingRun.typeWeight = initial.typeWeight
            shapingRun.typeSlope = initial.typeSlope
            shapingRun.typeSize = initial.typeSize
            shapingRun.scaleX = initial.scaleX

            resolveSpans(shapingRun, spans as Array<Any>)

            return shapingRun
        }

        return null
    }

    fun reset(charStart: Int, charEnd: Int) {
        limit = charEnd
        current = null
        next = resolveRun(charStart)
    }

    fun moveNext(): Boolean {
        val currentRun = next ?: return false
        var nextRun: ShapingRun?

        // Merge runs of similar style.
        while (resolveRun(currentRun.runEnd).also { nextRun = it } != null) {
            if (currentRun.typeface === nextRun!!.typeface
                && currentRun.typeSize.compareTo(nextRun!!.typeSize) == 0
                && currentRun.scaleX.compareTo(nextRun!!.scaleX) == 0
                && currentRun.baselineShift.compareTo(nextRun!!.baselineShift) == 0
                && currentRun.replacement === nextRun!!.replacement
            ) {
                currentRun.runEnd = nextRun!!.runEnd
            } else {
                break
            }
        }

        current = currentRun
        next = nextRun

        return true
    }

    val runStart: Int
        get() = current!!.runStart

    val runEnd: Int
        get() = current!!.runEnd

    val typeface: com.mta.tehreer.graphics.Typeface?
        get() = current!!.typeface

    val typeSize: Float
        get() = current!!.typeSize

    val scaleX: Float
        get() = current!!.scaleX

    val baselineShift: Float
        get() = current!!.baselineShift

    val replacement: ReplacementSpan?
        get() = current!!.replacement
}
