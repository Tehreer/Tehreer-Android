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

package com.mta.tehreer.graphics

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.sfnt.WritingDirection

/**
 * The `Renderer` class represents a generic glyph renderer. It can be used to generate glyph
 * paths, measure their bounding boxes and draw them on a `Canvas` object.
 */
class Renderer {
    private class Finalizable(private val nativeRenderer: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeRenderer)
        }
    }

    private val nativeRenderer = nCreate()
    private val finalizable = Finalizable(nativeRenderer)

    private val paint = Paint()
    private var isShadowLayerSynced = true

    /**
     * Returns the handle of the renderer of Core. It is only for the layout package.
     *
     * @hidden
     */
    @get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    val nativeHandle: Long
        get() = nativeRenderer

    /**
     * Sets the properties of the renderer of Core again, as measuring a run with it changes them.
     *
     * @hidden
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun syncNative() {
        nSetTypeface(nativeRenderer, typeface?.nativeTypeface ?: 0)
        nSetTypeSize(nativeRenderer, typeSize)
        nSetScaleX(nativeRenderer, scaleX)
        nSetScaleY(nativeRenderer, scaleY)
        nSetSkewX(nativeRenderer, slantAngle)
        nSetWritingDirection(nativeRenderer, writingDirection.value)
    }

    private fun syncShadowLayer() {
        if (!isShadowLayerSynced) {
            isShadowLayerSynced = true
            paint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor)
        }
    }

    /**
     * This renderer's fill color for glyphs, expressed as ARGB integer. The default value is
     * `Color.BLACK`.
     */
    @get:ColorInt
    @setparam:ColorInt
    var fillColor: Int = Color.BLACK
        set(value) {
            field = value
            nSetForegroundColor(nativeRenderer, value)
        }

    /**
     * This renderer's style, used for controlling how glyphs should appear while drawing. The
     * default value is [RenderingStyle.FILL].
     */
    var renderingStyle = RenderingStyle.FILL

    /**
     * The direction in which the pen will advance after drawing a glyph. The default value is
     * [WritingDirection.LEFT_TO_RIGHT].
     */
    var writingDirection = WritingDirection.LEFT_TO_RIGHT
        set(value) {
            field = value
            nSetWritingDirection(nativeRenderer, value.value)
        }

    /**
     * This renderer's typeface, used for drawing glyphs.
     */
    var typeface: Typeface? = null
        set(value) {
            field = value
            nSetTypeface(nativeRenderer, value?.nativeTypeface ?: 0)
        }

    /**
     * This renderer's type size in pixels, applied on glyphs while drawing.
     *
     * @throws IllegalArgumentException if the new value is negative.
     */
    var typeSize = 16.0f
        set(value) {
            require(value >= 0.0f) { "The value of type size is negative" }

            field = value
            nSetTypeSize(nativeRenderer, value)
        }

    /**
     * This renderer's slant angle for glyphs. The default value is 0.
     */
    var slantAngle = 0.0f
        set(value) {
            field = value
            nSetSkewX(nativeRenderer, value)
        }

    /**
     * This renderer's horizontal scale factor for glyphs. The default value is 1.0. Values greater
     * than 1.0 will stretch the glyphs wider. Values less than 1.0 will stretch the glyphs
     * narrower.
     */
    var scaleX = 1.0f
        set(value) {
            require(value >= 0.0f) { "Scale value is negative" }

            field = value
            nSetScaleX(nativeRenderer, value)
        }

    /**
     * This renderer's vertical scale factor for glyphs. The default value is 1.0. Values greater
     * than 1.0 will stretch the glyphs wider. Values less than 1.0 will stretch the glyphs
     * narrower.
     */
    var scaleY = 1.0f
        set(value) {
            require(value >= 0.0f) { "Scale value is negative" }

            field = value
            nSetScaleY(nativeRenderer, value)
        }

    /**
     * This renderer's stroke color for glyphs, expressed as ARGB integer. The default value is
     * `Color.BLACK`.
     */
    @get:ColorInt
    @setparam:ColorInt
    var strokeColor: Int = Color.BLACK

    /**
     * This renderer's width in pixels for stroking glyphs.
     */
    var strokeWidth = 0.0f
        set(value) {
            require(value >= 0.0f) { "Stroke width is negative" }

            field = value
            nSetStrokeWidth(nativeRenderer, value)
        }

    /**
     * This renderer's cap, controlling how the start and end of stroked lines and paths are
     * treated. The default value is [StrokeCap.BUTT].
     */
    var strokeCap = StrokeCap.BUTT
        set(value) {
            field = value
            nSetStrokeCap(nativeRenderer, value.value)
        }

    /**
     * This renderer's stroke join type. The default value is [StrokeJoin.ROUND].
     */
    var strokeJoin = StrokeJoin.ROUND
        set(value) {
            field = value
            nSetStrokeJoin(nativeRenderer, value.value)
        }

    /**
     * This renderer's stroke miter value in pixels. This is used to control the behavior of miter
     * joins when the joins angle is sharp.
     *
     * @throws IllegalArgumentException if the new value is less than one.
     */
    var strokeMiter = 0.0f
        set(value) {
            require(value >= 1.0f) { "Stroke miter is less than one" }

            field = value
            nSetStrokeMiter(nativeRenderer, value)
        }

    /**
     * This renderer's shadow radius in pixels, used when drawing glyphs. The default value is zero.
     * The shadow is disabled if the radius is set to zero.
     *
     * @throws IllegalArgumentException if the new value is negative.
     */
    var shadowRadius = 0.0f
        set(value) {
            require(value >= 0.0f) { "Shadow radius is negative" }

            field = value
            isShadowLayerSynced = false
        }

    /**
     * This renderer's horizontal shadow offset in pixels.
     */
    var shadowDx = 0.0f
        set(value) {
            field = value
            isShadowLayerSynced = false
        }

    /**
     * This renderer's vertical shadow offset in pixels.
     */
    var shadowDy = 0.0f
        set(value) {
            field = value
            isShadowLayerSynced = false
        }

    /**
     * This renderer's shadow color, expressed as ARGB integer.
     */
    @get:ColorInt
    @setparam:ColorInt
    var shadowColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            isShadowLayerSynced = false
        }

    // The settings of Core have to follow the ones that differ from its own defaults.
    init {
        strokeWidth = 1.0f
        strokeCap = StrokeCap.BUTT
        strokeJoin = StrokeJoin.ROUND
        strokeMiter = 1.0f
    }

    /**
     * Generates the path of the specified glyph.
     *
     * @param glyphId The ID of glyph whose path is generated.
     * @return The path of the glyph specified by `glyphId`.
     */
    fun generatePath(glyphId: Int): Path {
        return nGetGlyphPath(nativeRenderer, glyphId)
    }

    /**
     * Generates a cumulative path of specified glyphs.
     *
     * @param glyphIds The list containing the glyph IDs.
     * @param offsets The list containing the glyph offsets.
     * @param advances The list containing the glyph advances.
     * @return The cumulative path of specified glyphs.
     */
    fun generatePath(glyphIds: IntList, offsets: PointList, advances: FloatList): Path {
        return nGetRunPath(
            nativeRenderer, glyphIds.toArray(), offsets.toArray(), advances.toArray(), glyphIds.size()
        )
    }

    /**
     * Calculates the bounding box of specified glyph.
     *
     * @param glyphId The ID of glyph whose bounding box is calculated.
     * @return A rectangle that tightly encloses the path of the specified glyph.
     */
    fun computeBoundingBox(glyphId: Int): RectF {
        return nGetGlyphBoundingBox(nativeRenderer, glyphId) ?: RectF()
    }

    /**
     * Calculates the bounding box of specified glyphs.
     *
     * @param glyphIds The list containing the glyph IDs.
     * @param offsets The list containing the glyph offsets.
     * @param advances The list containing the glyph advances.
     * @return A rectangle that tightly encloses the paths of specified glyphs.
     */
    fun computeBoundingBox(glyphIds: IntList, offsets: PointList, advances: FloatList): RectF {
        val boundingBox = nGetRunBoundingBox(
            nativeRenderer, glyphIds.toArray(), offsets.toArray(), advances.toArray(), glyphIds.size()
        )

        // A run without any image has no box, which is the empty one that the union starts from.
        return boundingBox ?: RectF(
            Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
            Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY
        )
    }

    private fun drawGlyphs(
        canvas: Canvas, glyphIds: IntList, offsets: PointList, advances: FloatList, strokeMode: Boolean
    ) {
        val size = glyphIds.size()

        if (size > 0) {
            nDrawGlyphs(
                nativeRenderer, if (strokeMode) 1 else 0,
                glyphIds.toArray(), offsets.toArray(), advances.toArray(), size, canvas, paint
            )
        }
    }

    /**
     * Draws specified glyphs onto the given canvas. The shadow will not be drawn if the canvas is
     * hardware accelerated.
     *
     * @param canvas The canvas onto which to draw the glyphs.
     * @param glyphIds The list containing the glyph IDs.
     * @param offsets The list containing the glyph offsets.
     * @param advances The list containing the glyph advances.
     */
    fun drawGlyphs(canvas: Canvas, glyphIds: IntList, offsets: PointList, advances: FloatList) {
        if (nIsRenderable(nativeRenderer)) {
            syncShadowLayer()

            if (shadowRadius > 0.0f && canvas.isHardwareAccelerated) {
                Log.e(TAG, "Canvas is hardware accelerated, shadow will not be rendered")
            }

            if (renderingStyle == RenderingStyle.FILL || renderingStyle == RenderingStyle.FILL_STROKE) {
                paint.color = fillColor
                drawGlyphs(canvas, glyphIds, offsets, advances, false)
            }
            if (renderingStyle == RenderingStyle.STROKE || renderingStyle == RenderingStyle.FILL_STROKE) {
                paint.color = strokeColor
                drawGlyphs(canvas, glyphIds, offsets, advances, true)
            }
        }
    }

    private external fun nSetTypeface(nativeRenderer: Long, nativeTypeface: Long)
    private external fun nSetTypeSize(nativeRenderer: Long, typeSize: Float)
    private external fun nSetScaleX(nativeRenderer: Long, scaleX: Float)
    private external fun nSetScaleY(nativeRenderer: Long, scaleY: Float)
    private external fun nSetSkewX(nativeRenderer: Long, skewX: Float)
    private external fun nSetWritingDirection(nativeRenderer: Long, writingDirection: Int)
    private external fun nSetForegroundColor(nativeRenderer: Long, color: Int)
    private external fun nSetStrokeWidth(nativeRenderer: Long, strokeWidth: Float)
    private external fun nSetStrokeCap(nativeRenderer: Long, strokeCap: Int)
    private external fun nSetStrokeJoin(nativeRenderer: Long, strokeJoin: Int)
    private external fun nSetStrokeMiter(nativeRenderer: Long, strokeMiter: Float)
    private external fun nIsRenderable(nativeRenderer: Long): Boolean
    private external fun nGetGlyphPath(nativeRenderer: Long, glyphId: Int): Path
    private external fun nGetRunPath(
        nativeRenderer: Long, glyphIds: IntArray, offsets: FloatArray, advances: FloatArray, count: Int
    ): Path
    private external fun nGetGlyphBoundingBox(nativeRenderer: Long, glyphId: Int): RectF?
    private external fun nGetRunBoundingBox(
        nativeRenderer: Long, glyphIds: IntArray, offsets: FloatArray, advances: FloatArray, count: Int
    ): RectF?
    private external fun nDrawGlyphs(
        nativeRenderer: Long, kind: Int,
        glyphIds: IntArray, offsets: FloatArray, advances: FloatArray, count: Int,
        canvas: Canvas, paint: Paint
    )
    private companion object {
        val TAG: String = Renderer::class.java.simpleName

        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nCreate(): Long
        @JvmStatic external fun nDispose(nativeRenderer: Long)

    }
}
