/*
 * Copyright (C) 2016-2021 Muhammad Tayyab Akram
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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.internal.JniBridge;
import com.mta.tehreer.sfnt.WritingDirection;

import static com.mta.tehreer.internal.util.Preconditions.checkArgument;
import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * The <code>Renderer</code> class represents a generic glyph renderer. It can be used to generate
 * glyph paths, measure their bounding boxes and draw them on a <code>Canvas</code> object.
 */
public class Renderer {
    static {
        JniBridge.loadLibrary();
    }

    private static final String TAG = Renderer.class.getSimpleName();

    private class Finalizable {
        @Override
        protected void finalize() throws Throwable {
            try {
                nDispose(mNativeRenderer);
            } finally {
                super.finalize();
            }
        }
    }

    private final long mNativeRenderer = nCreate();
    private final @NonNull Finalizable mFinalizable = new Finalizable();

    private @NonNull Paint mPaint = new Paint();
    private boolean mShadowLayerSynced = true;

    private @ColorInt int mFillColor = Color.BLACK;
    private @NonNull RenderingStyle mRenderingStyle = RenderingStyle.FILL;
    private @NonNull WritingDirection mWritingDirection = WritingDirection.LEFT_TO_RIGHT;
    private Typeface mTypeface = null;
    private float mTypeSize = 16.0f;
    private float mSlantAngle = 0.0f;
    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;
    private @ColorInt int mStrokeColor = Color.BLACK;
    private float mStrokeWidth;
    private StrokeCap mStrokeCap;
    private StrokeJoin mStrokeJoin;
    private float mStrokeMiter;
    private float mShadowRadius = 0.0f;
    private float mShadowDx = 0.0f;
    private float mShadowDy = 0.0f;
    private @ColorInt int mShadowColor = Color.TRANSPARENT;

    /**
     * Constructs a renderer object.
     */
    public Renderer() {
        setStrokeWidth(1.0f);
        setStrokeCap(StrokeCap.BUTT);
        setStrokeJoin(StrokeJoin.ROUND);
        setStrokeMiter(1.0f);
    }

    private void syncShadowLayer() {
        if (!mShadowLayerSynced) {
            mShadowLayerSynced = true;
            mPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
        }
    }

    /**
     * Returns this renderer's fill color for glyphs. The default value is <code>Color.BLACK</code>.
     *
     * @return The fill color of this renderer expressed as ARGB integer.
     */
    public @ColorInt int getFillColor() {
        return mFillColor;
    }

    /**
     * Sets this renderer's fill color for glyphs. The default value is <code>Color.BLACK</code>.
     *
     * @param fillColor The 32-bit value of color expressed as ARGB.
     */
    public void setFillColor(@ColorInt int fillColor) {
        mFillColor = fillColor;
        nSetForegroundColor(mNativeRenderer, fillColor);
    }

    /**
     * Returns this renderer's style, used for controlling how glyphs should appear while drawing.
     * The default value is {@link RenderingStyle#FILL}.
     *
     * @return The style setting of this renderer.
     */
    public @NonNull RenderingStyle getRenderingStyle() {
        return mRenderingStyle;
    }

    /**
     * Sets this renderer's style, used for controlling how glyphs should appear while drawing. The
     * default value is {@link RenderingStyle#FILL}.
     *
     * @param renderingStyle The new style setting for the renderer.
     */
    public void setRenderingStyle(@NonNull RenderingStyle renderingStyle) {
        checkNotNull(renderingStyle);
        mRenderingStyle = renderingStyle;
    }

    /**
     * Returns the direction in which the pen will advance after drawing a glyph. The default value
     * is {@link WritingDirection#LEFT_TO_RIGHT}.
     *
     * @return The current writing direction.
     */
    public @NonNull WritingDirection getWritingDirection() {
        return mWritingDirection;
    }

    /**
     * Sets the direction in which the pen will advance after drawing a glyph. The default value is
     * {@link WritingDirection#LEFT_TO_RIGHT}.
     *
     * @param writingDirection The new writing direction.
     */
    public void setWritingDirection(@NonNull WritingDirection writingDirection) {
        checkNotNull(writingDirection);
        mWritingDirection = writingDirection;
        nSetWritingDirection(mNativeRenderer, writingDirection == WritingDirection.RIGHT_TO_LEFT ? 1 : 0);
    }

    /**
     * Returns this renderer's typeface, used for drawing glyphs.
     *
     * @return The typeface of this renderer.
     */
    public Typeface getTypeface() {
        return mTypeface;
    }

    /**
     * Sets this renderer's typeface, used for drawing glyphs.
     *
     * @param typeface The typeface to use for drawing glyphs.
     */
    public void setTypeface(Typeface typeface) {
        mTypeface = typeface;
        nSetTypeface(mNativeRenderer, typeface);
    }

    /**
     * Returns this renderer's type size, applied on glyphs while drawing.
     *
     * @return The type size of this renderer in pixels.
     */
    public float getTypeSize() {
        return mTypeSize;
    }

    /**
     * Sets this renderer's type size, applied on glyphs while drawing.
     *
     * @param typeSize The new type size in pixels.
     *
     * @throws IllegalArgumentException if <code>typeSize</code> is negative.
     */
    public void setTypeSize(float typeSize) {
        checkArgument(typeSize >= 0.0f, "The value of type size is negative");
        mTypeSize = typeSize;
        nSetTypeSize(mNativeRenderer, typeSize);
    }

    /**
     * Returns this renderer's slant angle for glyphs. The default value is 0.
     *
     * @return The slant angle of this renderer for drawing glyphs.
     */
    public float getSlantAngle() {
        return mSlantAngle;
    }

    /**
     * Sets this renderer's slant angle for glyphs. The default value is 0.
     *
     * @param slantAngle The slant angle for drawing glyphs.
     */
    public void setSlantAngle(float slantAngle) {
        mSlantAngle = slantAngle;
        nSetSkewX(mNativeRenderer, slantAngle);
    }

    /**
     * Returns this renderer's horizontal scale factor for glyphs. The default value is 1.0.
     *
     * @return The horizontal scale factor of this renderer for drawing/measuring glyphs.
     */
    public float getScaleX() {
        return mScaleX;
    }

    /**
     * Sets this renderer's horizontal scale factor for glyphs. The default value is 1.0. Values
     * greater than 1.0 will stretch the glyphs wider. Values less than 1.0 will stretch the glyphs
     * narrower.
     *
     * @param scaleX The horizontal scale factor for drawing/measuring glyphs.
     */
    public void setScaleX(float scaleX) {
        checkArgument(scaleX >= 0.0, "Scale value is negative");
        mScaleX = scaleX;
        nSetScaleX(mNativeRenderer, scaleX);
    }

    /**
     * Returns this renderer's vertical scale factor for glyphs. The default value is 1.0.
     *
     * @return The vertical scale factor of this renderer for drawing/measuring glyphs.
     */
    public float getScaleY() {
        return mScaleY;
    }

    /**
     * Sets this renderer's vertical scale factor for glyphs. The default value is 1.0. Values
     * greater than 1.0 will stretch the glyphs wider. Values less than 1.0 will stretch the glyphs
     * narrower.
     *
     * @param scaleY The vertical scale factor for drawing/measuring glyphs.
     */
    public void setScaleY(float scaleY) {
        checkArgument(scaleY >= 0.0, "Scale value is negative");
        mScaleY = scaleY;
        nSetScaleY(mNativeRenderer, scaleY);
    }

    /**
     * Returns this renderer's stroke color for glyphs. The default value is
     * <code>Color.BLACK</code>.
     *
     * @return The stroke color of this renderer expressed as ARGB integer.
     */
    public @ColorInt int getStrokeColor() {
        return mStrokeColor;
    }

    /**
     * Sets this renderer's stroke color for glyphs. The default value is <code>Color.BLACK</code>.
     *
     * @param strokeColor The 32-bit value of color expressed as ARGB.
     */
    public void setStrokeColor(@ColorInt int strokeColor) {
        mStrokeColor = strokeColor;
    }

    /**
     * Returns this renderer's width for stroking glyphs.
     *
     * @return The stroke width of this renderer in pixels.
     */
    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    /**
     * Sets this renderer's width for stroking glyphs.
     *
     * @param strokeWidth The stroke width in pixels.
     */
    public void setStrokeWidth(float strokeWidth) {
        checkArgument(strokeWidth >= 0.0f, "Stroke width is negative");
        mStrokeWidth = strokeWidth;
        nSetStrokeWidth(mNativeRenderer, strokeWidth);
    }

    /**
     * Returns this renderer's cap, controlling how the start and end of stroked lines and paths are
     * treated. The default value is {@link StrokeCap#BUTT}.
     *
     * @return The stroke cap style of this renderer.
     */
    public @NonNull StrokeCap getStrokeCap() {
        return mStrokeCap;
    }

    /**
     * Sets this renderer's cap, controlling how the start and end of stroked lines and paths are
     * treated. The default value is {@link StrokeCap#BUTT}.
     *
     * @param strokeCap The new stroke cap style.
     */
    public void setStrokeCap(@NonNull StrokeCap strokeCap) {
        checkNotNull(strokeCap);
        mStrokeCap = strokeCap;
        nSetStrokeCap(mNativeRenderer, strokeCap.value);
    }

    /**
     * Returns this renderer's stroke join type. The default value is {@link StrokeJoin#ROUND}.
     *
     * @return The stroke join type of this renderer.
     */
    public @NonNull StrokeJoin getStrokeJoin() {
        return mStrokeJoin;
    }

    /**
     * Sets this renderer's stroke join type. The default value is {@link StrokeJoin#ROUND}.
     *
     * @param strokeJoin The new stroke join type.
     */
    public void setStrokeJoin(@NonNull StrokeJoin strokeJoin) {
        checkNotNull(strokeJoin);
        mStrokeJoin = strokeJoin;
        nSetStrokeJoin(mNativeRenderer, strokeJoin.value);
    }

    /**
     * Returns this renderer's stroke miter value. Used to control the behavior of miter joins when
     * the joins angle is sharp.
     *
     * @return The miter limit of this renderer in pixels.
     */
    public float getStrokeMiter() {
        return mStrokeMiter;
    }

    /**
     * Sets this renderer's stroke miter value. This is used to control the behavior of miter joins
     * when the joins angle is sharp.
     *
     * @param strokeMiter The value of miter limit in pixels.
     *
     * @throws IllegalArgumentException if <code>strokeMiter</code> is less than one.
     */
    public void setStrokeMiter(float strokeMiter) {
        checkArgument(strokeMiter >= 1.0f, "Stroke miter is less than one");
        mStrokeMiter = strokeMiter;
        nSetStrokeMiter(mNativeRenderer, strokeMiter);
    }

    /**
     * Returns this renderer's shadow radius, used when drawing glyphs. The default value is zero.
     *
     * @return The shadow radius of this renderer in pixels.
     */
    public float getShadowRadius() {
        return mShadowRadius;
    }

    /**
     * Sets this renderer's shadow radius. The default value is zero. The shadow is disabled if the
     * radius is set to zero.
     *
     * @param shadowRadius The value of shadow radius in pixels.
     *
     * @throws IllegalArgumentException if <code>shadowRadius</code> is negative.
     */
    public void setShadowRadius(float shadowRadius) {
        checkArgument(shadowRadius >= 0.0f, "Shadow radius is negative");
        mShadowRadius = shadowRadius;
        mShadowLayerSynced = false;
    }

    /**
     * Returns this renderer's horizontal shadow offset.
     *
     * @return The horizontal shadow offset of this renderer in pixels.
     */
    public float getShadowDx() {
        return mShadowDx;
    }

    /**
     * Sets this renderer's horizontal shadow offset.
     *
     * @param shadowDx The value of horizontal shadow offset in pixels.
     */
    public void setShadowDx(float shadowDx) {
        mShadowDx = shadowDx;
        mShadowLayerSynced = false;
    }

    /**
     * Returns this renderer's vertical shadow offset.
     *
     * @return The vertical shadow offset of this renderer in pixels.
     */
    public float getShadowDy() {
        return mShadowDy;
    }

    /**
     * Sets this renderer's vertical shadow offset.
     *
     * @param shadowDy The value of vertical shadow offset in pixels.
     */
    public void setShadowDy(float shadowDy) {
        mShadowDy = shadowDy;
        mShadowLayerSynced = false;
    }

    /**
     * Returns this renderer's shadow color.
     *
     * @return The shadow color of this renderer expressed as ARGB integer.
     */
    public @ColorInt int getShadowColor() {
        return mShadowColor;
    }

    /**
     * Sets this renderer's shadow color.
     *
     * @param shadowColor The 32-bit value of color expressed as ARGB.
     */
    public void setShadowColor(@ColorInt int shadowColor) {
        mShadowColor = shadowColor;
        mShadowLayerSynced = false;
    }

    /**
     * Generates the path of the specified glyph.
     *
     * @param glyphId The ID of glyph whose path is generated.
     * @return The path of the glyph specified by <code>glyphId</code>.
     */
    public @NonNull Path generatePath(int glyphId) {
        return nGetGlyphPath(mNativeRenderer, glyphId);
    }

    /**
     * Generates a cumulative path of specified glyphs.
     *
     * @param glyphIds The list containing the glyph IDs.
     * @param offsets The list containing the glyph offsets.
     * @param advances The list containing the glyph advances.
     * @return The cumulative path of specified glyphs.
     */
    public @NonNull Path generatePath(@NonNull IntList glyphIds,
                                      @NonNull PointList offsets, @NonNull FloatList advances) {
        int size = glyphIds.size();

        return nGetRunPath(mNativeRenderer, glyphIds.toArray(), offsets.toArray(),
                           advances.toArray(), size);
    }

    /**
     * Calculates the bounding box of specified glyph.
     *
     * @param glyphId The ID of glyph whose bounding box is calculated.
     * @return A rectangle that tightly encloses the path of the specified glyph.
     */
    public @NonNull RectF computeBoundingBox(int glyphId) {
        float[] box = new float[4];
        RectF boundingBox = new RectF();

        if (nGetGlyphBoundingBox(mNativeRenderer, glyphId, box)) {
            boundingBox.set(box[0], box[1], box[2], box[3]);
        }

        return boundingBox;
    }

    /**
     * Calculates the bounding box of specified glyphs.
     *
     * @param glyphIds The list containing the glyph IDs.
     * @param offsets The list containing the glyph offsets.
     * @param advances The list containing the glyph advances.
     * @return A rectangle that tightly encloses the paths of specified glyphs.
     */
    public @NonNull RectF computeBoundingBox(@NonNull IntList glyphIds,
                                             @NonNull PointList offsets, @NonNull FloatList advances) {
        float[] box = new float[4];
        boolean hasBox = nGetRunBoundingBox(mNativeRenderer, glyphIds.toArray(), offsets.toArray(),
                                            advances.toArray(), glyphIds.size(), box);

        // A run without any image has no box, which is the empty one that the union starts from.
        if (!hasBox) {
            return new RectF(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY,
                             Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        }

        return new RectF(box[0], box[1], box[2], box[3]);
    }

    private void drawGlyphs(@NonNull Canvas canvas,
                            @NonNull IntList glyphIds, @NonNull PointList offsets, @NonNull FloatList advances,
                            boolean strokeMode) {
        int size = glyphIds.size();
        if (size == 0) {
            return;
        }

        Bitmap[] bitmaps = new Bitmap[size];
        int[] positions = new int[size * 2];

        nGetPlacements(mNativeRenderer, strokeMode ? 1 : 0, glyphIds.toArray(), offsets.toArray(),
                       advances.toArray(), size, bitmaps, positions);

        for (int i = 0; i < size; i++) {
            Bitmap bitmap = bitmaps[i];
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, positions[i * 2], positions[i * 2 + 1], mPaint);
            }
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
    public void drawGlyphs(@NonNull Canvas canvas,
                           @NonNull IntList glyphIds, @NonNull PointList offsets, @NonNull FloatList advances) {
        if (nIsRenderable(mNativeRenderer)) {
            syncShadowLayer();

            if (mShadowRadius > 0.0f && canvas.isHardwareAccelerated()) {
                Log.e(TAG, "Canvas is hardware accelerated, shadow will not be rendered");
            }

            if (mRenderingStyle == RenderingStyle.FILL || mRenderingStyle == RenderingStyle.FILL_STROKE) {
                mPaint.setColor(mFillColor);
                drawGlyphs(canvas, glyphIds, offsets, advances, false);
            }

            if (mRenderingStyle == RenderingStyle.STROKE || mRenderingStyle == RenderingStyle.FILL_STROKE) {
                mPaint.setColor(mStrokeColor);
                drawGlyphs(canvas, glyphIds, offsets, advances, true);
            }
        }
    }

    private static native long nCreate();
    private static native void nDispose(long nativeRenderer);

    private static native void nSetTypeface(long nativeRenderer, Typeface typeface);
    private static native void nSetTypeSize(long nativeRenderer, float typeSize);
    private static native void nSetScaleX(long nativeRenderer, float scaleX);
    private static native void nSetScaleY(long nativeRenderer, float scaleY);
    private static native void nSetSkewX(long nativeRenderer, float skewX);
    private static native void nSetWritingDirection(long nativeRenderer, int writingDirection);
    private static native void nSetForegroundColor(long nativeRenderer, int color);
    private static native void nSetStrokeWidth(long nativeRenderer, float strokeWidth);
    private static native void nSetStrokeCap(long nativeRenderer, int strokeCap);
    private static native void nSetStrokeJoin(long nativeRenderer, int strokeJoin);
    private static native void nSetStrokeMiter(long nativeRenderer, float strokeMiter);

    private static native boolean nIsRenderable(long nativeRenderer);

    private static native Path nGetGlyphPath(long nativeRenderer, int glyphId);
    private static native Path nGetRunPath(long nativeRenderer, int[] glyphIds, float[] offsets, float[] advances, int count);
    private static native boolean nGetGlyphBoundingBox(long nativeRenderer, int glyphId, float[] box);
    private static native boolean nGetRunBoundingBox(long nativeRenderer, int[] glyphIds, float[] offsets, float[] advances, int count, float[] box);
    private static native void nGetPlacements(long nativeRenderer, int kind, int[] glyphIds, float[] offsets, float[] advances, int count, Bitmap[] bitmaps, int[] positions);
}
