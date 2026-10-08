/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.layout

import android.graphics.Typeface.BOLD
import android.graphics.Typeface.BOLD_ITALIC
import android.graphics.Typeface.ITALIC
import android.graphics.Typeface.NORMAL
import android.text.style.AbsoluteSizeSpan
import android.text.style.RelativeSizeSpan
import android.text.style.ReplacementSpan
import android.text.style.ScaleXSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TextAppearanceSpan
import com.mta.tehreer.graphics.TypeSlope
import com.mta.tehreer.graphics.TypeWeight
import com.mta.tehreer.graphics.TypeWidth
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.graphics.TypefaceManager
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan

/**
 * The style that the spans of a range of text give to it. The spans are applied one after the other
 * on top of an initial style, and each of them can change what the earlier ones have set, so only
 * this class knows what the spans of Android mean. Core takes the result as attributes.
 */
internal class RunStyle private constructor(
    var typeface: Typeface?,
    private var typeWeight: TypeWeight,
    private var typeSlope: TypeSlope,
    var typeSize: Float,
    var scaleX: Float
) {
    var baselineShift = 0.0f
        private set

    var replacement: ReplacementSpan? = null
        private set

    /** Makes the style of a range from the initial style and the spans that cover the range. */
    fun with(spans: Array<out Any>): RunStyle {
        val style = RunStyle(typeface, typeWeight, typeSlope, typeSize, scaleX)
        style.apply(spans)

        return style
    }

    private fun apply(spans: Array<out Any>) {
        for (span in spans) {
            when (span) {
                is TypefaceSpan -> {
                    typeface = span.typeface
                    typeWeight = span.typeface.weight
                    typeSlope = span.typeface.slope
                }
                is TypeSizeSpan -> {
                    typeSize = span.size
                }
                is android.text.style.TypefaceSpan -> {
                    resolveTypeface(span.family ?: "", TypeWidth.NORMAL)
                }
                is AbsoluteSizeSpan -> {
                    typeSize = span.size.toFloat()
                }
                is RelativeSizeSpan -> {
                    typeSize *= span.sizeChange
                }
                is StyleSpan -> {
                    resolveStyle(span.style)
                    updateTypeface()
                }
                is TextAppearanceSpan -> {
                    typeSize = span.textSize.toFloat()
                    resolveStyle(span.textStyle)

                    val familyName = span.family
                    if (familyName != null) {
                        resolveTypeface(familyName, TypeWidth.NORMAL)
                    } else {
                        updateTypeface()
                    }
                }
                is ScaleXSpan -> {
                    scaleX = span.scaleX
                }
                is SuperscriptSpan -> {
                    resolveBaselineShift(0.5f)
                }
                is SubscriptSpan -> {
                    resolveBaselineShift(-0.5f)
                }
                is ReplacementSpan -> {
                    replacement = span
                }
            }
        }

        if (typeSize < 0.0f) {
            typeSize = 0.0f
        }
    }

    private fun resolveStyle(newStyle: Int) {
        when (newStyle) {
            NORMAL -> typeWeight = TypeWeight.REGULAR
            BOLD -> typeWeight = TypeWeight.BOLD
            ITALIC -> typeSlope = TypeSlope.ITALIC
            BOLD_ITALIC -> {
                typeWeight = TypeWeight.BOLD
                typeSlope = TypeSlope.ITALIC
            }
        }
    }

    private fun resolveTypeface(familyName: String, typeWidth: TypeWidth) {
        val typeFamily = TypefaceManager.getTypeFamily(familyName)
        typeface = typeFamily?.getTypefaceByStyle(typeWidth, typeWeight, typeSlope)
    }

    private fun updateTypeface() {
        typeface?.let { resolveTypeface(it.familyName, it.width) }
    }

    private fun resolveBaselineShift(multiplier: Float) {
        typeface?.let {
            val sizeByEm = typeSize / it.unitsPerEm
            baselineShift = it.ascent * sizeByEm * multiplier
        }
    }

    companion object {
        /** Makes the initial style, which the default spans give to all of the text. */
        fun initial(defaultSpans: List<Any>): RunStyle {
            val style = RunStyle(null, TypeWeight.REGULAR, TypeSlope.PLAIN, 16.0f, 1.0f)
            style.apply(defaultSpans.toTypedArray())

            return style
        }
    }
}
