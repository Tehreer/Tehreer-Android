package com.mta.tehreer.graphics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.graphics.Color;
import android.graphics.RectF;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.sfnt.WritingDirection;
import com.mta.tehreer.util.TypefaceStore;

import org.junit.Before;
import org.junit.Test;

public class RendererTest {
    private Renderer sut;
    private Typeface typeface;
    private float typeSize;

    @Before
    public void setUp() {
        sut = new Renderer();
        typeface = TypefaceStore.getNafeesWeb();
        typeSize = 32.0f;
    }

    @Test
    public void testInitialValues() {
        assertEquals(sut.getFillColor(), Color.BLACK);
        assertEquals(sut.getRenderingStyle(), RenderingStyle.FILL);
        assertEquals(sut.getWritingDirection(), WritingDirection.LEFT_TO_RIGHT);
        assertNull(sut.getTypeface());
        assertEquals(sut.getTypeSize(), 16.0f, 0.0f);
        assertEquals(sut.getSlantAngle(), 0.0f, 0.0f);
        assertEquals(sut.getScaleX(), 1.0f, 0.0f);
        assertEquals(sut.getScaleY(), 1.0f, 0.0f);
        assertEquals(sut.getStrokeColor(), Color.BLACK);
        assertEquals(sut.getStrokeWidth(), 1.0f, 0.0f);
        assertEquals(sut.getStrokeCap(), StrokeCap.BUTT);
        assertEquals(sut.getStrokeJoin(), StrokeJoin.ROUND);
        assertEquals(sut.getStrokeMiter(), 1.0f, 0.0f);
        assertEquals(sut.getShadowRadius(), 0.0f, 0.0f);
        assertEquals(sut.getShadowDx(), 0.0f, 0.0f);
        assertEquals(sut.getShadowDy(), 0.0f, 0.0f);
        assertEquals(sut.getShadowColor(), Color.TRANSPARENT);
    }

    @Test
    public void testFillColorProperty() {
        // Given
        int fillColor = Color.RED;

        // When
        sut.setFillColor(fillColor);

        // Then
        assertEquals(sut.getFillColor(), fillColor);
    }

    @Test
    public void testRenderingStyleProperty() {
        // Given
        RenderingStyle renderingStyle = RenderingStyle.FILL_STROKE;

        // When
        sut.setRenderingStyle(renderingStyle);

        // Then
        assertEquals(sut.getRenderingStyle(), renderingStyle);
    }

    @Test
    public void testWritingDirectionProperty() {
        // Given
        WritingDirection writingDirection = WritingDirection.RIGHT_TO_LEFT;

        // When
        sut.setWritingDirection(writingDirection);

        // Then
        assertEquals(sut.getWritingDirection(), writingDirection);
    }

    @Test
    public void testTypefaceProperty() {
        // When
        sut.setTypeface(typeface);

        // Then
        assertEquals(sut.getTypeface(), typeface);
    }

    @Test
    public void testTypeSizeProperty() {
        // When
        sut.setTypeSize(typeSize);

        // Then
        assertEquals(sut.getTypeSize(), typeSize, 0.0f);
    }

    @Test
    public void testSlantAngleProperty() {
        // Given
        float slantAngle = 1.0f;

        // When
        sut.setSlantAngle(slantAngle);

        // Then
        assertEquals(sut.getSlantAngle(), slantAngle, 0.0f);
    }

    @Test
    public void testScaleXProperty() {
        // Given
        float scaleX = 2.5f;

        // When
        sut.setScaleX(scaleX);

        // Then
        assertEquals(sut.getScaleX(), scaleX, 0.0f);
    }

    @Test
    public void testScaleYProperty() {
        // Given
        float scaleY = 2.5f;

        // When
        sut.setScaleY(scaleY);

        // Then
        assertEquals(sut.getScaleY(), scaleY, 0.0f);
    }

    @Test
    public void testStrokeColorProperty() {
        // Given
        int strokeColor = Color.BLUE;

        // When
        sut.setStrokeColor(strokeColor);

        // Then
        assertEquals(sut.getStrokeColor(), strokeColor);
    }

    @Test
    public void testStrokeWidthProperty() {
        // Given
        float strokeWidth = 2.5f;

        // When
        sut.setStrokeWidth(strokeWidth);

        // Then
        assertEquals(sut.getStrokeWidth(), strokeWidth, 0.0f);
    }

    @Test
    public void testStrokeCapProperty() {
        // Given
        StrokeCap strokeCap = StrokeCap.SQUARE;

        // When
        sut.setStrokeCap(strokeCap);

        // Then
        assertEquals(sut.getStrokeCap(), strokeCap);
    }

    @Test
    public void testStrokeJoinProperty() {
        // Given
        StrokeJoin strokeJoin = StrokeJoin.MITER;

        // When
        sut.setStrokeJoin(strokeJoin);

        // Then
        assertEquals(sut.getStrokeJoin(), strokeJoin);
    }

    @Test
    public void testStrokeMiterProperty() {
        // Given
        float strokeMiter = 2.5f;

        // When
        sut.setStrokeMiter(strokeMiter);

        // Then
        assertEquals(sut.getStrokeMiter(), strokeMiter, 0.0f);
    }

    @Test
    public void testShadowRadiusProperty() {
        // Given
        float shadowRadius = 2.5f;

        // When
        sut.setShadowRadius(shadowRadius);

        // Then
        assertEquals(sut.getShadowRadius(), shadowRadius, 0.0f);
    }

    @Test
    public void testShadowDxProperty() {
        // Given
        float shadowDx = 2.5f;

        // When
        sut.setShadowDx(shadowDx);

        // Then
        assertEquals(sut.getShadowDx(), shadowDx, 0.0f);
    }

    @Test
    public void testShadowDyProperty() {
        // Given
        float shadowDy = 2.5f;

        // When
        sut.setShadowDy(shadowDy);

        // Then
        assertEquals(sut.getShadowDy(), shadowDy, 0.0f);
    }

    @Test
    public void testShadowColorProperty() {
        // Given
        int shadowColor = Color.GRAY;

        // When
        sut.setShadowColor(shadowColor);

        // Then
        assertEquals(sut.getShadowColor(), shadowColor);
    }

    @Test
    public void testComputeBoundingBoxForSingleGlyph() {
        // Given
        int glyphId = typeface.getGlyphId('ت');
        sut.setTypeface(typeface);
        sut.setTypeSize(typeSize);

        // When
        RectF bbox = sut.computeBoundingBox(glyphId);

        // Then
        assertEquals(bbox, new RectF(1.0f, 14.0f, 24.0f, 29.0f));
    }

    @Test
    public void testComputeBoundingBoxForMultipleGlyphs() {
        // Given
        IntList glyphIds = IntList.of(51, 85, 120, 77, 120);
        PointList glyphOffsets = PointList.of(new float[10]);
        FloatList glyphAdvancess = FloatList.of(6, 15, 9, 10, 9);

        sut.setTypeface(typeface);
        sut.setTypeSize(typeSize);
        sut.setWritingDirection(WritingDirection.RIGHT_TO_LEFT);

        // When
        RectF bbox = sut.computeBoundingBox(glyphIds, glyphOffsets, glyphAdvancess);

        // Then
        assertEquals(bbox, new RectF(-3.0f, -17.0f, 51.0f, 9.0f));
    }
}
