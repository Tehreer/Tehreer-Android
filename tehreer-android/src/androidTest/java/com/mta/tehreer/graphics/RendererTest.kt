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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF

import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.sfnt.WritingDirection
import com.mta.tehreer.util.TypefaceStore

import org.junit.Before
import org.junit.Test

class RendererTest {
    private lateinit var subject: Renderer
    private lateinit var typeface: Typeface
    private var typeSize: Float = 0f

    @Before
    fun setUp() {
        subject = Renderer()
        typeface = TypefaceStore.nafeesWeb
        typeSize = 32.0f
    }

    @Test
    fun testInitialValues() {
        assertEquals(subject.fillColor, Color.BLACK)
        assertEquals(subject.renderingStyle, RenderingStyle.FILL)
        assertEquals(subject.writingDirection, WritingDirection.LEFT_TO_RIGHT)
        assertNull(subject.typeface)
        assertEquals(subject.typeSize, 16.0f, 0.0f)
        assertEquals(subject.slantAngle, 0.0f, 0.0f)
        assertEquals(subject.scaleX, 1.0f, 0.0f)
        assertEquals(subject.scaleY, 1.0f, 0.0f)
        assertEquals(subject.strokeColor, Color.BLACK)
        assertEquals(subject.strokeWidth, 1.0f, 0.0f)
        assertEquals(subject.strokeCap, StrokeCap.BUTT)
        assertEquals(subject.strokeJoin, StrokeJoin.ROUND)
        assertEquals(subject.strokeMiter, 1.0f, 0.0f)
        assertEquals(subject.shadowRadius, 0.0f, 0.0f)
        assertEquals(subject.shadowDx, 0.0f, 0.0f)
        assertEquals(subject.shadowDy, 0.0f, 0.0f)
        assertEquals(subject.shadowColor, Color.TRANSPARENT)
    }

    @Test
    fun testFillColorProperty() {
        // Given
        val fillColor = Color.RED

        // When
        subject.fillColor = fillColor

        // Then
        assertEquals(subject.fillColor, fillColor)
    }

    @Test
    fun testRenderingStyleProperty() {
        // Given
        val renderingStyle = RenderingStyle.FILL_STROKE

        // When
        subject.renderingStyle = renderingStyle

        // Then
        assertEquals(subject.renderingStyle, renderingStyle)
    }

    @Test
    fun testWritingDirectionProperty() {
        // Given
        val writingDirection = WritingDirection.RIGHT_TO_LEFT

        // When
        subject.writingDirection = writingDirection

        // Then
        assertEquals(subject.writingDirection, writingDirection)
    }

    @Test
    fun testTypefaceProperty() {
        // When
        subject.typeface = typeface

        // Then
        assertEquals(subject.typeface, typeface)
    }

    @Test
    fun testTypeSizeProperty() {
        // When
        subject.typeSize = typeSize

        // Then
        assertEquals(subject.typeSize, typeSize, 0.0f)
    }

    @Test
    fun testSlantAngleProperty() {
        // Given
        val slantAngle: Float = 1.0f

        // When
        subject.slantAngle = slantAngle

        // Then
        assertEquals(subject.slantAngle, slantAngle, 0.0f)
    }

    @Test
    fun testScaleXProperty() {
        // Given
        val scaleX: Float = 2.5f

        // When
        subject.scaleX = scaleX

        // Then
        assertEquals(subject.scaleX, scaleX, 0.0f)
    }

    @Test
    fun testScaleYProperty() {
        // Given
        val scaleY: Float = 2.5f

        // When
        subject.scaleY = scaleY

        // Then
        assertEquals(subject.scaleY, scaleY, 0.0f)
    }

    @Test
    fun testStrokeColorProperty() {
        // Given
        val strokeColor = Color.BLUE

        // When
        subject.strokeColor = strokeColor

        // Then
        assertEquals(subject.strokeColor, strokeColor)
    }

    @Test
    fun testStrokeWidthProperty() {
        // Given
        val strokeWidth: Float = 2.5f

        // When
        subject.strokeWidth = strokeWidth

        // Then
        assertEquals(subject.strokeWidth, strokeWidth, 0.0f)
    }

    @Test
    fun testStrokeCapProperty() {
        // Given
        val strokeCap = StrokeCap.SQUARE

        // When
        subject.strokeCap = strokeCap

        // Then
        assertEquals(subject.strokeCap, strokeCap)
    }

    @Test
    fun testStrokeJoinProperty() {
        // Given
        val strokeJoin = StrokeJoin.MITER

        // When
        subject.strokeJoin = strokeJoin

        // Then
        assertEquals(subject.strokeJoin, strokeJoin)
    }

    @Test
    fun testStrokeMiterProperty() {
        // Given
        val strokeMiter: Float = 2.5f

        // When
        subject.strokeMiter = strokeMiter

        // Then
        assertEquals(subject.strokeMiter, strokeMiter, 0.0f)
    }

    @Test
    fun testShadowRadiusProperty() {
        // Given
        val shadowRadius: Float = 2.5f

        // When
        subject.shadowRadius = shadowRadius

        // Then
        assertEquals(subject.shadowRadius, shadowRadius, 0.0f)
    }

    @Test
    fun testShadowDxProperty() {
        // Given
        val shadowDx: Float = 2.5f

        // When
        subject.shadowDx = shadowDx

        // Then
        assertEquals(subject.shadowDx, shadowDx, 0.0f)
    }

    @Test
    fun testShadowDyProperty() {
        // Given
        val shadowDy: Float = 2.5f

        // When
        subject.shadowDy = shadowDy

        // Then
        assertEquals(subject.shadowDy, shadowDy, 0.0f)
    }

    @Test
    fun testShadowColorProperty() {
        // Given
        val shadowColor = Color.GRAY

        // When
        subject.shadowColor = shadowColor

        // Then
        assertEquals(subject.shadowColor, shadowColor)
    }

    @Test
    fun testComputeBoundingBoxForSingleGlyph() {
        // Given
        val glyphId = typeface.getGlyphId('ت'.code)
        subject.typeface = typeface
        subject.typeSize = typeSize

        // When
        val bbox = subject.computeBoundingBox(glyphId)

        // Then
        // The y axis points downward, so the glyph is above the baseline.
        assertEquals(bbox, RectF(1.0f, -14.0f, 24.0f, 1.0f))
    }

    @Test
    fun testComputeBoundingBoxForMultipleGlyphs() {
        // Given
        val glyphIds = IntList.of(51, 85, 120, 77, 120)
        val glyphOffsets = PointList.of(*FloatArray(10))
        val glyphAdvancess = FloatList.of(6.0f, 15.0f, 9.0f, 10.0f, 9.0f)

        subject.typeface = typeface
        subject.typeSize = typeSize
        subject.writingDirection = WritingDirection.RIGHT_TO_LEFT

        // When
        val bbox = subject.computeBoundingBox(glyphIds, glyphOffsets, glyphAdvancess)

        // Then
        // The positions are rounded to the nearest pixel, also where they are negative.
        assertEquals(bbox, RectF(-4.0f, -18.0f, 50.0f, 8.0f))
    }

    private fun drawGlyph(glyphId: Int, originX: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.translate(originX, 70.0f)

        subject.drawGlyphs(canvas, IntList.of(glyphId), PointList.of(0.0f, 0.0f), FloatList.of(20.0f))

        return bitmap
    }

    private fun inkedColumns(bitmap: Bitmap): IntArray {
        var first = -1
        var last = -1

        for (x in 0 until bitmap.getWidth()) {
            for (y in 0 until bitmap.getHeight()) {
                if (Color.alpha(bitmap.getPixel(x, y)) != 0) {
                    first = if (first == -1) x else first
                    last = x
                    break
                }
            }
        }

        return intArrayOf(first, last)
    }

    @Test
    fun testDrawGlyphsFillsPixels() {
        // Given
        val glyphId = typeface.getGlyphId('ت'.code)
        subject.typeface = typeface
        subject.typeSize = typeSize
        subject.fillColor = Color.RED

        // When
        val bitmap = drawGlyph(glyphId, 50.0f)

        // Then
        val columns = inkedColumns(bitmap)
        assertTrue(columns[0] >= 45 && columns[1] < 100)

        var hasRed = false
        for (x in 0 until bitmap.width) {
            for (y in 0 until bitmap.height) {
                hasRed = hasRed or (bitmap.getPixel(x, y) == Color.RED)
            }
        }
        assertTrue(hasRed)
    }

    @Test
    fun testDrawGlyphsInRightToLeftMode() {
        // Given
        val glyphId = typeface.getGlyphId('ت'.code)
        subject.typeface = typeface
        subject.typeSize = typeSize

        // When
        val leftToRight = inkedColumns(drawGlyph(glyphId, 100.0f))
        subject.writingDirection = WritingDirection.RIGHT_TO_LEFT
        val rightToLeft = inkedColumns(drawGlyph(glyphId, 100.0f))

        // Then: the pen moves left by the advance before the glyph is drawn.
        assertEquals((leftToRight[0] - rightToLeft[0]).toDouble(), 20.0, 1.0)
        assertEquals((leftToRight[1] - rightToLeft[1]).toDouble(), 20.0, 1.0)
    }

    @Test
    fun testDrawGlyphsInStrokeStyle() {
        // Given
        val glyphId = typeface.getGlyphId('ت'.code)
        subject.typeface = typeface
        subject.typeSize = typeSize
        subject.strokeWidth = 3.0f

        // When
        subject.renderingStyle = RenderingStyle.FILL
        val fill = drawGlyph(glyphId, 50.0f)
        subject.renderingStyle = RenderingStyle.STROKE
        val stroke = drawGlyph(glyphId, 50.0f)

        // Then
        assertFalse(fill.sameAs(stroke))
        assertTrue(inkedColumns(stroke)[0] != -1)
    }

    @Test
    fun testDrawGlyphsOfTinySizeDrawsNothing() {
        // Given
        val glyphId = typeface.getGlyphId('ت'.code)
        subject.typeface = typeface
        subject.typeSize = 0.0f

        // When
        val bitmap = drawGlyph(glyphId, 50.0f)

        // Then
        assertEquals(inkedColumns(bitmap)[0], -1)
    }

    @Test
    fun testGeneratePath() {
        // Given
        val glyphId = typeface.getGlyphId('ت'.code)
        subject.typeface = typeface
        subject.typeSize = typeSize

        // When
        val path = subject.generatePath(glyphId)
        val bounds = RectF()
        path.computeBounds(bounds, true)

        // Then: the path points downward from the baseline.
        assertNotNull(path)
        assertFalse(bounds.isEmpty())
        assertTrue(bounds.top < 0.0f || bounds.bottom > 0.0f)
    }

    @Test
    fun testGenerateCumulativePath() {
        // Given
        val glyphId = typeface.getGlyphId('ت'.code)
        subject.typeface = typeface
        subject.typeSize = typeSize

        // When
        val single = subject.generatePath(glyphId)
        val run = subject.generatePath(IntList.of(glyphId, glyphId),
                                        PointList.of(0.0f, 0.0f, 0.0f, 0.0f), FloatList.of(40.0f, 40.0f))
        val singleBounds = RectF()
        val runBounds = RectF()
        single.computeBounds(singleBounds, true)
        run.computeBounds(runBounds, true)

        // Then: the second glyph is moved by the advance of the first one.
        assertEquals(runBounds.right, singleBounds.right + 40.0f, 1.0f)
        assertEquals(runBounds.left, singleBounds.left, 1.0f)
        assertNotEquals(runBounds.width(), singleBounds.width(), 1.0f)
    }
}


