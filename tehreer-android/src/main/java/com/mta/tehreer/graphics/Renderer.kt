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
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.internal.JniBridge
import com.mta.tehreer.sfnt.WritingDirection

/**
 * The `Renderer` class represents a generic glyph renderer. It can be used to generate glyph
 * paths and images, measure their ink boxes and draw them on a `Canvas` object.
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

    /**
     * The handle of the renderer of Core.
     */
    internal val nativeHandle: Long
        get() = nativeRenderer

    /**
     * Returns the paint that Core draws with onto the given canvas. Core applies the shadow on it
     * while it draws.
     */
    internal fun preparePaint(canvas: Canvas): Paint {
        if (shadowRadius > 0.0f && canvas.isHardwareAccelerated) {
            Log.e(TAG, "Canvas is hardware accelerated, shadow will not be rendered")
        }

        return paint
    }

    private var cache: GlyphCache? = null

    /**
     * The cache that this renderer finds the images and outlines of glyphs in. The default value is
     * [GlyphCache.default]. A cache can be shared between renderers.
     */
    var glyphCache: GlyphCache
        get() = cache ?: GlyphCache.default
        set(value) {
            // Core retains the cache, and the object of Kotlin is kept as well.
            cache = value
            nSetGlyphCache(nativeRenderer, value.nativeCache)
        }

    /**
     * This renderer's typeface, used for drawing glyphs.
     */
    var typeface: Typeface? = null
        set(value) {
            // Core does not retain the object of Kotlin, so it is kept here.
            field = value
            nSetTypeface(nativeRenderer, value?.nativeTypeface ?: 0)
        }

    /**
     * This renderer's type size in pixels, applied on glyphs while drawing.
     *
     * @throws IllegalArgumentException if the new value is negative.
     */
    var typeSize: Float
        get() = nGetTypeSize(nativeRenderer)
        set(value) {
            require(value >= 0.0f) { "The value of type size is negative" }

            nSetTypeSize(nativeRenderer, value)
        }

    /**
     * This renderer's horizontal scale factor for glyphs. The default value is 1.0. Values greater
     * than 1.0 will stretch the glyphs wider. Values less than 1.0 will stretch the glyphs
     * narrower.
     */
    var scaleX: Float
        get() = nGetScaleX(nativeRenderer)
        set(value) {
            require(value >= 0.0f) { "Scale value is negative" }

            nSetScaleX(nativeRenderer, value)
        }

    /**
     * This renderer's vertical scale factor for glyphs. The default value is 1.0. Values greater
     * than 1.0 will stretch the glyphs wider. Values less than 1.0 will stretch the glyphs
     * narrower.
     */
    var scaleY: Float
        get() = nGetScaleY(nativeRenderer)
        set(value) {
            require(value >= 0.0f) { "Scale value is negative" }

            nSetScaleY(nativeRenderer, value)
        }

    /**
     * This renderer's horizontal skew for glyphs, which is the ratio of the horizontal shift to
     * the height. The default value is 0.
     */
    var skewX: Float
        get() = nGetSkewX(nativeRenderer)
        set(value) {
            nSetSkewX(nativeRenderer, value)
        }

    /**
     * This renderer's number of pixels in a unit of the user space. The default value is 1.0.
     * Glyph images are rendered at this scale.
     *
     * @throws IllegalArgumentException if the new value is not greater than zero.
     */
    var renderScale: Float
        get() = nGetRenderScale(nativeRenderer)
        set(value) {
            require(value > 0.0f) { "Render scale is not positive" }

            nSetRenderScale(nativeRenderer, value)
        }

    /**
     * The direction in which the pen will advance after drawing a glyph. The default value is
     * [WritingDirection.LEFT_TO_RIGHT].
     */
    var writingDirection: WritingDirection
        get() = if (nGetWritingDirection(nativeRenderer) == 1) {
            WritingDirection.RIGHT_TO_LEFT
        } else {
            WritingDirection.LEFT_TO_RIGHT
        }
        set(value) {
            nSetWritingDirection(nativeRenderer, value.value)
        }

    /**
     * This renderer's foreground color for glyphs, expressed as ARGB integer. The default value is
     * `Color.BLACK`.
     */
    @get:ColorInt
    @setparam:ColorInt
    var foregroundColor: Int
        get() = nGetForegroundColor(nativeRenderer)
        set(value) {
            nSetForegroundColor(nativeRenderer, value)
        }

    /**
     * This renderer's radius in pixels for stroking glyphs, which is half of the width of the
     * stroke. The default value is 0.5.
     *
     * @throws IllegalArgumentException if the new value is negative.
     */
    var strokeRadius: Float
        get() = nGetStrokeRadius(nativeRenderer)
        set(value) {
            require(value >= 0.0f) { "Stroke radius is negative" }

            nSetStrokeRadius(nativeRenderer, value)
        }

    /**
     * This renderer's cap, controlling how the start and end of stroked lines and paths are
     * treated. The default value is [StrokeCap.BUTT].
     */
    var strokeCap: StrokeCap
        get() = StrokeCap.entries.first { it.value == nGetStrokeCap(nativeRenderer) }
        set(value) {
            nSetStrokeCap(nativeRenderer, value.value)
        }

    /**
     * This renderer's stroke join type. The default value is [StrokeJoin.ROUND].
     */
    var strokeJoin: StrokeJoin
        get() = StrokeJoin.entries.first { it.value == nGetStrokeJoin(nativeRenderer) }
        set(value) {
            nSetStrokeJoin(nativeRenderer, value.value)
        }

    /**
     * This renderer's stroke miter value in pixels. The default value is 1.0. This is used to control the behavior of miter
     * joins when the joins angle is sharp.
     *
     * @throws IllegalArgumentException if the new value is less than one.
     */
    var strokeMiter: Float
        get() = nGetStrokeMiter(nativeRenderer)
        set(value) {
            require(value >= 1.0f) { "Stroke miter is less than one" }

            nSetStrokeMiter(nativeRenderer, value)
        }

    /**
     * This renderer's stroke color for glyphs, expressed as ARGB integer. The default value is
     * `Color.BLACK`.
     */
    @get:ColorInt
    @setparam:ColorInt
    var strokeColor: Int
        get() = nGetStrokeColor(nativeRenderer)
        set(value) {
            nSetStrokeColor(nativeRenderer, value)
        }

    /**
     * This renderer's shadow radius in pixels, used when drawing glyphs. The default value is zero.
     * The shadow is disabled if the radius is set to zero.
     *
     * @throws IllegalArgumentException if the new value is negative.
     */
    var shadowRadius: Float
        get() = nGetShadowRadius(nativeRenderer)
        set(value) {
            require(value >= 0.0f) { "Shadow radius is negative" }

            nSetShadowRadius(nativeRenderer, value)
        }

    /**
     * This renderer's horizontal shadow offset in pixels.
     */
    var shadowDx: Float
        get() = nGetShadowDx(nativeRenderer)
        set(value) {
            nSetShadowDx(nativeRenderer, value)
        }

    /**
     * This renderer's vertical shadow offset in pixels.
     */
    var shadowDy: Float
        get() = nGetShadowDy(nativeRenderer)
        set(value) {
            nSetShadowDy(nativeRenderer, value)
        }

    /**
     * This renderer's shadow color, expressed as ARGB integer.
     */
    @get:ColorInt
    @setparam:ColorInt
    var shadowColor: Int
        get() = nGetShadowColor(nativeRenderer)
        set(value) {
            nSetShadowColor(nativeRenderer, value)
        }

    /**
     * This renderer's style, used for controlling how glyphs should appear while drawing. The
     * default value is [DrawStyle.FILL].
     */
    var drawStyle: DrawStyle
        get() = when (nGetDrawStyle(nativeRenderer)) {
            1 -> DrawStyle.STROKE
            2 -> DrawStyle.FILL_STROKE
            else -> DrawStyle.FILL
        }
        set(value) {
            nSetDrawStyle(nativeRenderer, when (value) {
                DrawStyle.FILL -> 0
                DrawStyle.STROKE -> 1
                DrawStyle.FILL_STROKE -> 2
            })
        }

    /**
     * Returns the path of the specified glyph.
     *
     * @param glyphId The ID of glyph whose path is returned.
     * @return The path of the glyph specified by `glyphId`.
     */
    fun getGlyphPath(glyphId: Int): Path {
        return nGetGlyphPath(nativeRenderer, glyphId)
    }

    /**
     * Returns the image that fills the specified glyph, rendered in the foreground color of this
     * renderer for the parts that follow it.
     *
     * @param glyphId The ID of glyph whose image is returned.
     * @return The image of the glyph, or `null` if there is no typeface or the glyph has no image,
     *         such as a space.
     */
    fun getGlyphImage(glyphId: Int): GlyphImage? {
        val nativeImage = nCreateGlyphImage(nativeRenderer, glyphId)

        return if (nativeImage != 0L) GlyphImage(nativeImage) else null
    }

    /**
     * Returns the image that strokes the specified glyph with the stroke settings of this renderer.
     *
     * @param glyphId The ID of glyph whose stroke image is returned.
     * @return The image of the stroke, or `null` if there is no typeface or the glyph has no
     *         image, such as a space.
     */
    fun getStrokeImage(glyphId: Int): GlyphImage? {
        val nativeImage = nCreateStrokeImage(nativeRenderer, glyphId)

        return if (nativeImage != 0L) GlyphImage(nativeImage) else null
    }

    /**
     * Calculates the ink box of specified glyph.
     *
     * @param glyphId The ID of glyph whose ink box is calculated.
     * @return A rectangle that tightly encloses the path of the specified glyph.
     */
    fun computeInkBox(glyphId: Int): RectF {
        return nComputeGlyphInkBox(nativeRenderer, glyphId) ?: RectF()
    }

    /**
     * Calculates the ink box of specified glyphs.
     *
     * @param glyphIds The list containing the glyph IDs.
     * @param offsets The list containing the glyph offsets.
     * @param advances The list containing the glyph advances.
     * @return A rectangle that tightly encloses the paths of specified glyphs.
     */
    fun computeInkBox(glyphIds: IntList, offsets: PointList, advances: FloatList): RectF {
        val inkBox = nComputeRunInkBox(
            nativeRenderer, glyphIds.toArray(), offsets.toArray(), advances.toArray(), glyphIds.size()
        )

        // A run without any image has no box, which is the empty one that the union starts from.
        return inkBox ?: RectF(
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
            preparePaint(canvas)

            // Core applies the shadow only while it draws runs, lines and frames.
            val radius = shadowRadius
            val dx = shadowDx
            val dy = shadowDy
            val hasShadow = radius > 0.0f && radius.isFinite() && dx.isFinite() && dy.isFinite()
            if (hasShadow) {
                paint.setShadowLayer(radius, dx, dy, shadowColor)
            }

            if (drawStyle == DrawStyle.FILL || drawStyle == DrawStyle.FILL_STROKE) {
                paint.color = foregroundColor
                drawGlyphs(canvas, glyphIds, offsets, advances, false)
            }
            if (drawStyle == DrawStyle.STROKE || drawStyle == DrawStyle.FILL_STROKE) {
                paint.color = strokeColor
                drawGlyphs(canvas, glyphIds, offsets, advances, true)
            }

            if (hasShadow) {
                paint.clearShadowLayer()
            }
        }
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

    private external fun nSetTypeface(nativeRenderer: Long, nativeTypeface: Long)
    private external fun nGetTypeSize(nativeRenderer: Long): Float
    private external fun nSetTypeSize(nativeRenderer: Long, typeSize: Float)
    private external fun nGetScaleX(nativeRenderer: Long): Float
    private external fun nSetScaleX(nativeRenderer: Long, scaleX: Float)
    private external fun nGetScaleY(nativeRenderer: Long): Float
    private external fun nSetScaleY(nativeRenderer: Long, scaleY: Float)
    private external fun nGetSkewX(nativeRenderer: Long): Float
    private external fun nSetSkewX(nativeRenderer: Long, skewX: Float)
    private external fun nGetRenderScale(nativeRenderer: Long): Float
    private external fun nSetRenderScale(nativeRenderer: Long, renderScale: Float)
    private external fun nSetGlyphCache(nativeRenderer: Long, nativeCache: Long)
    private external fun nGetWritingDirection(nativeRenderer: Long): Int
    private external fun nSetWritingDirection(nativeRenderer: Long, writingDirection: Int)
    private external fun nGetForegroundColor(nativeRenderer: Long): Int
    private external fun nSetForegroundColor(nativeRenderer: Long, color: Int)
    private external fun nGetStrokeRadius(nativeRenderer: Long): Float
    private external fun nSetStrokeRadius(nativeRenderer: Long, strokeRadius: Float)
    private external fun nGetStrokeCap(nativeRenderer: Long): Int
    private external fun nSetStrokeCap(nativeRenderer: Long, strokeCap: Int)
    private external fun nGetStrokeJoin(nativeRenderer: Long): Int
    private external fun nSetStrokeJoin(nativeRenderer: Long, strokeJoin: Int)
    private external fun nGetStrokeMiter(nativeRenderer: Long): Float
    private external fun nSetStrokeMiter(nativeRenderer: Long, strokeMiter: Float)
    private external fun nGetStrokeColor(nativeRenderer: Long): Int
    private external fun nSetStrokeColor(nativeRenderer: Long, strokeColor: Int)
    private external fun nGetShadowRadius(nativeRenderer: Long): Float
    private external fun nSetShadowRadius(nativeRenderer: Long, shadowRadius: Float)
    private external fun nGetShadowDx(nativeRenderer: Long): Float
    private external fun nSetShadowDx(nativeRenderer: Long, shadowDx: Float)
    private external fun nGetShadowDy(nativeRenderer: Long): Float
    private external fun nSetShadowDy(nativeRenderer: Long, shadowDy: Float)
    private external fun nGetShadowColor(nativeRenderer: Long): Int
    private external fun nSetShadowColor(nativeRenderer: Long, shadowColor: Int)
    private external fun nGetDrawStyle(nativeRenderer: Long): Int
    private external fun nSetDrawStyle(nativeRenderer: Long, drawStyle: Int)
    private external fun nIsRenderable(nativeRenderer: Long): Boolean
    private external fun nGetGlyphPath(nativeRenderer: Long, glyphId: Int): Path
    private external fun nCreateGlyphImage(nativeRenderer: Long, glyphId: Int): Long
    private external fun nCreateStrokeImage(nativeRenderer: Long, glyphId: Int): Long
    private external fun nComputeGlyphInkBox(nativeRenderer: Long, glyphId: Int): RectF?
    private external fun nComputeRunInkBox(
        nativeRenderer: Long, glyphIds: IntArray, offsets: FloatArray, advances: FloatArray, count: Int
    ): RectF?
    private external fun nDrawGlyphs(
        nativeRenderer: Long, kind: Int,
        glyphIds: IntArray, offsets: FloatArray, advances: FloatArray, count: Int,
        canvas: Canvas, paint: Paint
    )
    private external fun nGetRunPath(
        nativeRenderer: Long, glyphIds: IntArray, offsets: FloatArray, advances: FloatArray, count: Int
    ): Path

    private companion object {
        val TAG: String = Renderer::class.java.simpleName

        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nCreate(): Long
        @JvmStatic external fun nDispose(nativeRenderer: Long)

    }
}
