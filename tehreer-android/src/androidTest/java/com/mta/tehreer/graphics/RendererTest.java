/*
 * Copyright (C) 2022-2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.graphics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.sfnt.WritingDirection;
import com.mta.tehreer.util.TypefaceStore;

import org.junit.Before;
import org.junit.Test;

public class RendererTest {
    private Renderer subject;
    private Typeface typeface;
    private float typeSize;

    @Before
    public void setUp() {
        subject = new Renderer();
        typeface = TypefaceStore.getNafeesWeb();
        typeSize = 32.0f;
    }

    @Test
    public void testInitialValues() {
        assertEquals(subject.getFillColor(), Color.BLACK);
        assertEquals(subject.getRenderingStyle(), RenderingStyle.FILL);
        assertEquals(subject.getWritingDirection(), WritingDirection.LEFT_TO_RIGHT);
        assertNull(subject.getTypeface());
        assertEquals(subject.getTypeSize(), 16.0f, 0.0f);
        assertEquals(subject.getSlantAngle(), 0.0f, 0.0f);
        assertEquals(subject.getScaleX(), 1.0f, 0.0f);
        assertEquals(subject.getScaleY(), 1.0f, 0.0f);
        assertEquals(subject.getStrokeColor(), Color.BLACK);
        assertEquals(subject.getStrokeWidth(), 1.0f, 0.0f);
        assertEquals(subject.getStrokeCap(), StrokeCap.BUTT);
        assertEquals(subject.getStrokeJoin(), StrokeJoin.ROUND);
        assertEquals(subject.getStrokeMiter(), 1.0f, 0.0f);
        assertEquals(subject.getShadowRadius(), 0.0f, 0.0f);
        assertEquals(subject.getShadowDx(), 0.0f, 0.0f);
        assertEquals(subject.getShadowDy(), 0.0f, 0.0f);
        assertEquals(subject.getShadowColor(), Color.TRANSPARENT);
    }

    @Test
    public void testFillColorProperty() {
        // Given
        int fillColor = Color.RED;

        // When
        subject.setFillColor(fillColor);

        // Then
        assertEquals(subject.getFillColor(), fillColor);
    }

    @Test
    public void testRenderingStyleProperty() {
        // Given
        RenderingStyle renderingStyle = RenderingStyle.FILL_STROKE;

        // When
        subject.setRenderingStyle(renderingStyle);

        // Then
        assertEquals(subject.getRenderingStyle(), renderingStyle);
    }

    @Test
    public void testWritingDirectionProperty() {
        // Given
        WritingDirection writingDirection = WritingDirection.RIGHT_TO_LEFT;

        // When
        subject.setWritingDirection(writingDirection);

        // Then
        assertEquals(subject.getWritingDirection(), writingDirection);
    }

    @Test
    public void testTypefaceProperty() {
        // When
        subject.setTypeface(typeface);

        // Then
        assertEquals(subject.getTypeface(), typeface);
    }

    @Test
    public void testTypeSizeProperty() {
        // When
        subject.setTypeSize(typeSize);

        // Then
        assertEquals(subject.getTypeSize(), typeSize, 0.0f);
    }

    @Test
    public void testSlantAngleProperty() {
        // Given
        float slantAngle = 1.0f;

        // When
        subject.setSlantAngle(slantAngle);

        // Then
        assertEquals(subject.getSlantAngle(), slantAngle, 0.0f);
    }

    @Test
    public void testScaleXProperty() {
        // Given
        float scaleX = 2.5f;

        // When
        subject.setScaleX(scaleX);

        // Then
        assertEquals(subject.getScaleX(), scaleX, 0.0f);
    }

    @Test
    public void testScaleYProperty() {
        // Given
        float scaleY = 2.5f;

        // When
        subject.setScaleY(scaleY);

        // Then
        assertEquals(subject.getScaleY(), scaleY, 0.0f);
    }

    @Test
    public void testStrokeColorProperty() {
        // Given
        int strokeColor = Color.BLUE;

        // When
        subject.setStrokeColor(strokeColor);

        // Then
        assertEquals(subject.getStrokeColor(), strokeColor);
    }

    @Test
    public void testStrokeWidthProperty() {
        // Given
        float strokeWidth = 2.5f;

        // When
        subject.setStrokeWidth(strokeWidth);

        // Then
        assertEquals(subject.getStrokeWidth(), strokeWidth, 0.0f);
    }

    @Test
    public void testStrokeCapProperty() {
        // Given
        StrokeCap strokeCap = StrokeCap.SQUARE;

        // When
        subject.setStrokeCap(strokeCap);

        // Then
        assertEquals(subject.getStrokeCap(), strokeCap);
    }

    @Test
    public void testStrokeJoinProperty() {
        // Given
        StrokeJoin strokeJoin = StrokeJoin.MITER;

        // When
        subject.setStrokeJoin(strokeJoin);

        // Then
        assertEquals(subject.getStrokeJoin(), strokeJoin);
    }

    @Test
    public void testStrokeMiterProperty() {
        // Given
        float strokeMiter = 2.5f;

        // When
        subject.setStrokeMiter(strokeMiter);

        // Then
        assertEquals(subject.getStrokeMiter(), strokeMiter, 0.0f);
    }

    @Test
    public void testShadowRadiusProperty() {
        // Given
        float shadowRadius = 2.5f;

        // When
        subject.setShadowRadius(shadowRadius);

        // Then
        assertEquals(subject.getShadowRadius(), shadowRadius, 0.0f);
    }

    @Test
    public void testShadowDxProperty() {
        // Given
        float shadowDx = 2.5f;

        // When
        subject.setShadowDx(shadowDx);

        // Then
        assertEquals(subject.getShadowDx(), shadowDx, 0.0f);
    }

    @Test
    public void testShadowDyProperty() {
        // Given
        float shadowDy = 2.5f;

        // When
        subject.setShadowDy(shadowDy);

        // Then
        assertEquals(subject.getShadowDy(), shadowDy, 0.0f);
    }

    @Test
    public void testShadowColorProperty() {
        // Given
        int shadowColor = Color.GRAY;

        // When
        subject.setShadowColor(shadowColor);

        // Then
        assertEquals(subject.getShadowColor(), shadowColor);
    }

    @Test
    public void testComputeBoundingBoxForSingleGlyph() {
        // Given
        int glyphId = typeface.getGlyphId('ت');
        subject.setTypeface(typeface);
        subject.setTypeSize(typeSize);

        // When
        RectF bbox = subject.computeBoundingBox(glyphId);

        // Then
        // The y axis points downward, so the glyph is above the baseline.
        assertEquals(bbox, new RectF(1.0f, -14.0f, 24.0f, 1.0f));
    }

    @Test
    public void testComputeBoundingBoxForMultipleGlyphs() {
        // Given
        IntList glyphIds = IntList.of(51, 85, 120, 77, 120);
        PointList glyphOffsets = PointList.of(new float[10]);
        FloatList glyphAdvancess = FloatList.of(6, 15, 9, 10, 9);

        subject.setTypeface(typeface);
        subject.setTypeSize(typeSize);
        subject.setWritingDirection(WritingDirection.RIGHT_TO_LEFT);

        // When
        RectF bbox = subject.computeBoundingBox(glyphIds, glyphOffsets, glyphAdvancess);

        // Then
        // The positions are rounded to the nearest pixel, also where they are negative.
        assertEquals(bbox, new RectF(-4.0f, -18.0f, 50.0f, 8.0f));
    }

    private Bitmap drawGlyph(int glyphId, float originX) {
        Bitmap bitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(originX, 70.0f);

        subject.drawGlyphs(canvas, IntList.of(glyphId), PointList.of(0.0f, 0.0f), FloatList.of(20.0f));

        return bitmap;
    }

    private int[] inkedColumns(Bitmap bitmap) {
        int first = -1;
        int last = -1;

        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (Color.alpha(bitmap.getPixel(x, y)) != 0) {
                    first = (first == -1 ? x : first);
                    last = x;
                    break;
                }
            }
        }

        return new int[] { first, last };
    }

    @Test
    public void testDrawGlyphsFillsPixels() {
        // Given
        int glyphId = typeface.getGlyphId('ت');
        subject.setTypeface(typeface);
        subject.setTypeSize(typeSize);
        subject.setFillColor(Color.RED);

        // When
        Bitmap bitmap = drawGlyph(glyphId, 50.0f);

        // Then
        int[] columns = inkedColumns(bitmap);
        assertTrue(columns[0] >= 45 && columns[1] < 100);

        boolean hasRed = false;
        for (int x = 0; x < bitmap.getWidth() && !hasRed; x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                hasRed |= (bitmap.getPixel(x, y) == Color.RED);
            }
        }
        assertTrue(hasRed);
    }

    @Test
    public void testDrawGlyphsInRightToLeftMode() {
        // Given
        int glyphId = typeface.getGlyphId('ت');
        subject.setTypeface(typeface);
        subject.setTypeSize(typeSize);

        // When
        int[] leftToRight = inkedColumns(drawGlyph(glyphId, 100.0f));
        subject.setWritingDirection(WritingDirection.RIGHT_TO_LEFT);
        int[] rightToLeft = inkedColumns(drawGlyph(glyphId, 100.0f));

        // Then: the pen moves left by the advance before the glyph is drawn.
        assertEquals(leftToRight[0] - rightToLeft[0], 20, 1);
        assertEquals(leftToRight[1] - rightToLeft[1], 20, 1);
    }

    @Test
    public void testDrawGlyphsInStrokeStyle() {
        // Given
        int glyphId = typeface.getGlyphId('ت');
        subject.setTypeface(typeface);
        subject.setTypeSize(typeSize);
        subject.setStrokeWidth(3.0f);

        // When
        subject.setRenderingStyle(RenderingStyle.FILL);
        Bitmap fill = drawGlyph(glyphId, 50.0f);
        subject.setRenderingStyle(RenderingStyle.STROKE);
        Bitmap stroke = drawGlyph(glyphId, 50.0f);

        // Then
        assertFalse(fill.sameAs(stroke));
        assertTrue(inkedColumns(stroke)[0] != -1);
    }

    @Test
    public void testDrawGlyphsOfTinySizeDrawsNothing() {
        // Given
        int glyphId = typeface.getGlyphId('ت');
        subject.setTypeface(typeface);
        subject.setTypeSize(0.0f);

        // When
        Bitmap bitmap = drawGlyph(glyphId, 50.0f);

        // Then
        assertEquals(inkedColumns(bitmap)[0], -1);
    }

    @Test
    public void testGeneratePath() {
        // Given
        int glyphId = typeface.getGlyphId('ت');
        subject.setTypeface(typeface);
        subject.setTypeSize(typeSize);

        // When
        Path path = subject.generatePath(glyphId);
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);

        // Then: the path points downward from the baseline.
        assertNotNull(path);
        assertFalse(bounds.isEmpty());
        assertTrue(bounds.top < 0.0f || bounds.bottom > 0.0f);
    }

    @Test
    public void testGenerateCumulativePath() {
        // Given
        int glyphId = typeface.getGlyphId('ت');
        subject.setTypeface(typeface);
        subject.setTypeSize(typeSize);

        // When
        Path single = subject.generatePath(glyphId);
        Path run = subject.generatePath(IntList.of(glyphId, glyphId),
                                        PointList.of(0.0f, 0.0f, 0.0f, 0.0f), FloatList.of(40.0f, 40.0f));
        RectF singleBounds = new RectF();
        RectF runBounds = new RectF();
        single.computeBounds(singleBounds, true);
        run.computeBounds(runBounds, true);

        // Then: the second glyph is moved by the advance of the first one.
        assertEquals(runBounds.right, singleBounds.right + 40.0f, 1.0f);
        assertEquals(runBounds.left, singleBounds.left, 1.0f);
        assertNotEquals(runBounds.width(), singleBounds.width(), 1.0f);
    }
}
