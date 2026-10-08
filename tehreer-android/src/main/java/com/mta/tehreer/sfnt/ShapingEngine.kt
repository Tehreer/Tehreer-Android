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

package com.mta.tehreer.sfnt

import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.Description
import com.mta.tehreer.internal.JniBridge

/**
 * The `ShapingEngine` class represents text shaping engine.
 */
class ShapingEngine {
    private class Finalizable(private val nativeEngine: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeEngine)
        }
    }

    internal val nativeEngine: Long
    private val finalizable: Finalizable
    private var currentTypeface: Typeface? = null
    private var currentFeatures: Set<OpenTypeFeature> = emptySet()

    /**
     * Constructs a shaping engine object.
     */
    constructor() {
        nativeEngine = nCreate()
        finalizable = Finalizable(nativeEngine)
    }

    /**
     * The typeface which this shaping engine will use for shaping text.
     */
    var typeface: Typeface?
        get() = currentTypeface
        set(value) {
            currentTypeface = value
            nSetTypeface(nativeEngine, value?.nativeTypeface ?: 0)
        }

    /**
     * The type size which this shaping engine will use for shaping text.
     */
    var typeSize: Float
        get() = nGetTypeSize(nativeEngine)
        set(value) {
            require(value >= 0.0f) { "The value of font size is negative" }
            nSetTypeSize(nativeEngine, value)
        }

    /**
     * The script tag which this shaping engine will use for shaping text. The default value is
     * `'DFLT'`.
     *
     * A tag can be created from string by using [SfntTag.make] method.
     */
    var scriptTag: Int
        get() = nGetScriptTag(nativeEngine)
        set(value) = nSetScriptTag(nativeEngine, value)

    /**
     * The language tag which this shaping engine will use for shaping text. The default value is
     * `'dflt'`.
     *
     * A tag can be created from string by using [SfntTag.make] method.
     */
    var languageTag: Int
        get() = nGetLanguageTag(nativeEngine)
        set(value) = nSetLanguageTag(nativeEngine, value)

    /**
     * The user-specified open type feature settings.
     *
     * If the value of a feature is set to zero, it would be disabled provided that it is not a
     * required feature of the chosen script. If the value of a feature is greater than zero, it
     * would be enabled. In case of an alternate feature, this value would be used to pick the
     * alternate glyph at this position.
     */
    var openTypeFeatures: Set<OpenTypeFeature>
        get() = currentFeatures.toSet()
        set(value) {
            currentFeatures = LinkedHashSet(value)

            for (feature in value) {
                nAddOpenTypeFeature(nativeEngine, feature.tag(), feature.value().toShort())
            }

            nApplyOpenTypeFeatures(nativeEngine)
        }

    /**
     * The direction in which this shaping engine will place the resultant glyphs. The default value
     * is [WritingDirection.LEFT_TO_RIGHT].
     *
     * The value must reflect the rendering direction of source script so that cursive and mark
     * glyphs are placed at appropriate locations. It should not be confused with the direction of a
     * bidirectional run as that may not reflect the script direction if overridden explicitly.
     */
    var writingDirection: WritingDirection
        get() = WritingDirection.valueOf(nGetWritingDirection(nativeEngine))!!
        set(value) = nSetWritingDirection(nativeEngine, value.value)

    /**
     * The order in which this shaping engine will process the text. The default value is
     * [ShapingOrder.FORWARD].
     *
     * This allows to shape a bidirectional run whose direction is opposite to that of script. For
     * example, if the direction of a run, 'car' is explicitly set as right-to-left, backward order
     * will automatically read it as 'rac' without reordering the original text.
     */
    var shapingOrder: ShapingOrder
        get() = ShapingOrder.valueOf(nGetShapingOrder(nativeEngine))!!
        set(value) = nSetShapingOrder(nativeEngine, value.value)

    /**
     * Shapes the specified range of text into glyphs.
     *
     * The output glyphs in the `ShapingResult` object flow visually in writing direction. For
     * left-to-right direction, the position of pen is incremented with glyph's advance after
     * rendering it. Similarly, for right-to-left direction, the position of pen is decremented with
     * glyph's advance after rendering it.
     *
     * @param text The text to shape into glyphs.
     * @param fromIndex The index of the first character (inclusive) to be shaped.
     * @param toIndex The index of the last character (exclusive) to be shaped.
     * @return A `ShapingResult` object holding the shaped glyphs.
     *
     * @throws IllegalStateException if current typeface is `null`.
     * @throws IllegalArgumentException if `fromIndex` is negative, or `toIndex` is greater than
     *         `text.length`, or `fromIndex` is greater than `toIndex`
     */
    fun shapeText(text: String, fromIndex: Int, toIndex: Int): ShapingResult {
        check(currentTypeface != null) { "Typeface has not been set" }
        require(fromIndex >= 0) { "From Index: $fromIndex" }
        require(toIndex <= text.length) { "To Index: $toIndex, Text Length: ${text.length}" }
        require(toIndex >= fromIndex) { "Bad Range: [$fromIndex, $toIndex)" }

        val result = ShapingResult()
        nShapeText(nativeEngine, result.nativeResult, text, fromIndex, toIndex)

        return result
    }

    override fun toString(): String {
        return "ShapingEngine{typeface=${Description.forObject(typeface)}" +
            ", typeSize=$typeSize" +
            ", scriptTag=${SfntTag.toString(scriptTag)}" +
            ", languageTag=${SfntTag.toString(languageTag)}" +
            ", openTypeFeatures=$openTypeFeatures" +
            ", writingDirection=$writingDirection" +
            ", shapingOrder=$shapingOrder" +
            "}"
    }

    private external fun nSetTypeface(nativeEngine: Long, nativeTypeface: Long)
    private external fun nGetTypeSize(nativeEngine: Long): Float
    private external fun nSetTypeSize(nativeEngine: Long, typeSize: Float)
    private external fun nGetScriptTag(nativeEngine: Long): Int
    private external fun nSetScriptTag(nativeEngine: Long, scriptTag: Int)
    private external fun nGetLanguageTag(nativeEngine: Long): Int
    private external fun nSetLanguageTag(nativeEngine: Long, languageTag: Int)
    private external fun nAddOpenTypeFeature(nativeEngine: Long, tag: Int, value: Short)
    private external fun nApplyOpenTypeFeatures(nativeEngine: Long)
    private external fun nGetWritingDirection(nativeEngine: Long): Int
    private external fun nSetWritingDirection(nativeEngine: Long, writingDirection: Int)
    private external fun nGetShapingOrder(nativeEngine: Long): Int
    private external fun nSetShapingOrder(nativeEngine: Long, shapingOrder: Int)
    private external fun nShapeText(
        nativeEngine: Long, nativeResult: Long, text: String, fromIndex: Int, toIndex: Int
    )
    companion object {
        init {
            JniBridge.loadLibrary()
        }

        /**
         * Returns the default writing direction of a script.
         *
         * @param scriptTag The tag of the script whose default direction is returned.
         * @return The default writing direction of the script identified by `scriptTag`.
         */
        fun getScriptDirection(scriptTag: Int): WritingDirection {
            return WritingDirection.valueOf(nGetScriptDefaultDirection(scriptTag))!!
        }

        @JvmStatic private external fun nGetScriptDefaultDirection(scriptTag: Int): Int

        @JvmStatic private external fun nCreate(): Long
        @JvmStatic private external fun nDispose(nativeEngine: Long)

    }
}
