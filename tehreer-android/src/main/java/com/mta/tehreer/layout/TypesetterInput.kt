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

import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.layout.ShapingRunLocator
import com.mta.tehreer.internal.util.Preconditions.checkArgument

/**
 * The runs and the colors of a text, which Core is given to typeset it. A run is a range of the
 * text that has a typeface, a size, and a scale and a baseline shift of its own, or a replacement.
 */
internal class TypesetterInput(
    text: CharSequence,
    spanned: Spanned,
    defaultSpans: List<Any>
) {
    val runBounds: IntArray
    val typefaces: Array<Typeface>
    val typeSizes: FloatArray
    val scaleXs: FloatArray
    val baselineShifts: FloatArray
    val holders: Array<Any?>
    val holderLeadings: FloatArray
    val holderBlocks: BooleanArray
    val colorBounds: IntArray
    val colors: IntArray

    /** The typefaces of the runs, by the handles that their runs have. */
    val typefaceMap = HashMap<Long, Typeface>()

    val runCount: Int
        get() = typefaces.size

    val colorCount: Int
        get() = colors.size

    init {
        val locator = ShapingRunLocator(spanned, defaultSpans)
        val bounds = ArrayList<Int>()
        val faces = ArrayList<Typeface>()
        val sizes = ArrayList<Float>()
        val scales = ArrayList<Float>()
        val shifts = ArrayList<Float>()
        val holderList = ArrayList<Any?>()
        val leadings = ArrayList<Float>()
        val blocks = ArrayList<Boolean>()

        locator.reset(0, text.length)

        while (locator.moveNext()) {
            val runStart = locator.runStart
            val runEnd = locator.runEnd

            val typeface = locator.typeface
            checkArgument(
                typeface != null,
                "No typeface is specified for range [$runStart, $runEnd)"
            )

            val typeSize = locator.typeSize
            val sizeByEm = typeSize / typeface!!.unitsPerEm
            val handle = typeface.coreHandle

            typefaceMap[handle] = typeface

            bounds.add(runStart)
            bounds.add(runEnd)
            faces.add(typeface)
            sizes.add(typeSize)
            scales.add(locator.scaleX)
            shifts.add(locator.baselineShift)

            val replacement = locator.replacement
            if (replacement != null) {
                val holder = ReplacementHolder(
                    replacement, spanned, runStart, runEnd,
                    typeface.ascent * sizeByEm, typeface.descent * sizeByEm, typeface.leading * sizeByEm
                )

                holderList.add(holder)
                leadings.add(holder.leading)
                blocks.add(holder.isBlock)
            } else {
                holderList.add(null)
                leadings.add(0.0f)
                blocks.add(false)
            }
        }

        runBounds = bounds.toIntArray()
        typefaces = faces.toTypedArray()
        typeSizes = sizes.toFloatArray()
        scaleXs = scales.toFloatArray()
        baselineShifts = shifts.toFloatArray()
        holders = holderList.toTypedArray()
        holderLeadings = leadings.toFloatArray()
        holderBlocks = blocks.toBooleanArray()

        // The color of a span replaces those that came before it where they overlap.
        val colorRanges = ArrayList<Int>()
        val colorValues = ArrayList<Int>()

        for (span in spanned.getSpans(0, spanned.length, ForegroundColorSpan::class.java)) {
            val spanStart = spanned.getSpanStart(span)
            val spanEnd = spanned.getSpanEnd(span)

            if (spanStart < spanEnd) {
                colorRanges.add(spanStart)
                colorRanges.add(spanEnd)
                colorValues.add(span.foregroundColor)
            }
        }

        colorBounds = colorRanges.toIntArray()
        colors = colorValues.toIntArray()
    }
}
