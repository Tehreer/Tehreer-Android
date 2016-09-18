/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

import com.mta.tehreer.text.TextDirection;

public class Renderer {

    private static final String TAG = Renderer.class.getSimpleName();

    public enum Cap {
        BUTT(GlyphRasterizer.LINECAP_BUTT),
        ROUND(GlyphRasterizer.LINECAP_ROUND),
        SQUARE(GlyphRasterizer.LINECAP_SQUARE);

        private final int value;

        Cap(int value) {
            this.value = value;
        }
    }

    public enum Join {
        ROUND(GlyphRasterizer.LINEJOIN_ROUND),
        BEVEL(GlyphRasterizer.LINEJOIN_BEVEL),
        MITER(GlyphRasterizer.LINEJOIN_MITER);

        private final int value;

        Join(int value) {
            this.value = value;
        }
    }

    public enum Style {
        FILL,
        STROKE,
        FILL_STROKE,
    }

    private GlyphStrike mGlyphStrike;
    private int mGlyphLineRadius;
    private int mGlyphLineCap;
    private int mGlyphLineJoin;
    private int mGlyphMiterLimit;

    private Paint mPaint;
    private boolean mShouldRender;
    private boolean mShadowLayerSynced;

    private Typeface mTypeface;
    private Style mRenderingStyle;
    private TextDirection mTextDirection;
    private float mTextSize;
    private float mTextScaleX;
    private float mTextScaleY;
    private float mTextSkewX;
    private int mTextColor;
    private float mStrokeWidth;
    private Cap mStrokeCap;
    private Join mStrokeJoin;
    private float mStrokeMiter;
    private int mStrokeColor;
    private float mShadowRadius;
    private float mShadowDx;
    private float mShadowDy;
    private int mShadowColor;

    public Renderer() {
        mGlyphStrike = new GlyphStrike();
        mPaint = new Paint();
        mShadowRadius = 0.0f;
        mShadowDx = 0.0f;
        mShadowDy = 0.0f;
        mShadowColor = Color.TRANSPARENT;

        setRenderingStyle(Style.FILL);
        setTextDirection(TextDirection.LEFT_TO_RIGHT);
        setTextSize(16.0f);
        setTextScaleX(1.0f);
        setTextScaleY(1.0f);
        setTextSkewX(0.0f);
        setTextColor(Color.BLACK);
        setStrokeWidth(1.0f);
        setStrokeCap(Cap.BUTT);
        setStrokeJoin(Join.ROUND);
        setStrokeMiter(1.0f);
        setStrokeColor(Color.BLACK);
    }

    private void updatePixelSizes() {
        int pixelWidth = (int) ((mTextSize * mTextScaleX * 64.0f) + 0.5f);
        int pixelHeight = (int) ((mTextSize * mTextScaleY * 64.0f) + 0.5f);

        // Minimum size supported by Freetype is 64x64.
        mShouldRender = (pixelWidth >= 64 && pixelHeight >= 64);
        mGlyphStrike.pixelWidth = pixelWidth;
        mGlyphStrike.pixelHeight = pixelHeight;
    }

    private void updateTransform() {
        mGlyphStrike.skewX = (int) ((mTextSkewX * 0x10000) + 0.5f);
    }

    private void syncShadowLayer() {
        if (!mShadowLayerSynced) {
            mShadowLayerSynced = true;
            mPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
        }
    }

    public Style getRenderingStyle() {
        return mRenderingStyle;
    }

    public void setRenderingStyle(Style renderingStyle) {
    	if (renderingStyle == null) {
    		throw new NullPointerException("Rendering style is null");
    	}

        mRenderingStyle = renderingStyle;
    }

    public Typeface getTypeface() {
        return mTypeface;
    }

    public void setTypeface(Typeface typeface) {
        mTypeface = typeface;
        mGlyphStrike.typeface = typeface;
    }

    public TextDirection getTextDirection() {
        return mTextDirection;
    }

    public void setTextDirection(TextDirection textDirection) {
        mTextDirection = textDirection;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        if (textSize < 0.0) {
            throw new IllegalArgumentException("Size is negative");
        }

        mTextSize = textSize;
        updatePixelSizes();
    }

    public float getTextScaleX() {
        return mTextScaleX;
    }

    public void setTextScaleX(float textScaleX) {
        if (textScaleX < 0.0f) {
            throw new IllegalArgumentException("Scale value is negative");
        }

        mTextScaleX = textScaleX;
        updatePixelSizes();
    }

    public float getTextScaleY() {
        return mTextScaleY;
    }

    public void setTextScaleY(float textScaleY) {
        if (textScaleY < 0.0f) {
            throw new IllegalArgumentException("Scale value is negative");
        }

        mTextScaleY = textScaleY;
        updatePixelSizes();
    }

    public float getTextSkewX() {
        return mTextSkewX;
    }

    public void setTextSkewX(float textSkewX) {
        mTextSkewX = textSkewX;
        updateTransform();
    }

    public int getStrokeColor() {
        return mStrokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        mStrokeColor = strokeColor;
    }

    public float getStrokeWidth() {
        return mStrokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        if (strokeWidth < 0.0f) {
            throw new IllegalArgumentException("Stroke width is negative");
        }

        mStrokeWidth = strokeWidth;
        mGlyphLineRadius = (int) ((strokeWidth * 64.0f / 2.0f) + 0.5f);
    }

    public Cap getStrokeCap() {
        return mStrokeCap;
    }

    public void setStrokeCap(Cap strokeCap) {
        if (strokeCap == null) {
            throw new NullPointerException("Stroke cap is null");
        }

        mStrokeCap = strokeCap;
        mGlyphLineCap = strokeCap.value;
    }

    public Join getStrokeJoin() {
        return mStrokeJoin;
    }

    public void setStrokeJoin(Join strokeJoin) {
        if (strokeJoin == null) {
            throw new NullPointerException("Stroke join is null");
        }

        mStrokeJoin = strokeJoin;
        mGlyphLineJoin = strokeJoin.value;
    }

    public float getStrokeMiter() {
        return mStrokeMiter;
    }

    public void setStrokeMiter(float strokeMiter) {
        if (strokeMiter < 1.0f) {
            throw new IllegalArgumentException("Stroke miter is less than one");
        }

        mStrokeMiter = strokeMiter;
        mGlyphMiterLimit = (int) ((strokeMiter * 0x10000) + 0.5f);
    }

    public float getShadowRadius() {
        return mShadowRadius;
    }

    public void setShadowRadius(float shadowRadius) {
        if (shadowRadius < 0.0f) {
            throw new IllegalArgumentException("Shadow radius is negative");
        }

        mShadowRadius = shadowRadius;
        mShadowLayerSynced = false;
    }

    public float getShadowDx() {
        return mShadowDx;
    }

    public void setShadowDx(float shadowDx) {
        mShadowDx = shadowDx;
        mShadowLayerSynced = false;
    }

    public float getShadowDy() {
        return mShadowDy;
    }

    public void setShadowDy(float shadowDy) {
        mShadowDy = shadowDy;
        mShadowLayerSynced = false;
    }

    public int getShadowColor() {
        return mShadowColor;
    }

    public void setShadowColor(int shadowColor) {
        mShadowColor = shadowColor;
        mShadowLayerSynced = false;
    }

    private Path getGlyphPath(int glyphId) {
        return GlyphCache.getInstance().getGlyphPath(mGlyphStrike, glyphId);
    }

    public Path generatePath(int glyphId) {
        Path glyphPath = new Path();
        glyphPath.addPath(getGlyphPath(glyphId));

        return glyphPath;
    }

    public Path generatePath(int[] glyphIds,
                             float[] xOffsets, float[] yOffsets, float[] advances,
                             int fromIndex, int toIndex) {
        Path cumulativePath = new Path();

        float penX = 0.0f;

        for (int i = fromIndex; i < toIndex; i++) {
            int glyphId = glyphIds[i];
            float xOffset = xOffsets[i] * mTextScaleX;
            float yOffset = yOffsets[i] * mTextScaleY;
            float advance = advances[i] * mTextScaleX;

            Path glyphPath = getGlyphPath(glyphId);
            cumulativePath.addPath(glyphPath, penX + xOffset, yOffset);

            penX += advance;
        }

        return cumulativePath;
    }

    private void copyBoundingBox(int glyphId, RectF boundingBox) {
        Glyph glyph = GlyphCache.getInstance().getMaskGlyph(mGlyphStrike, glyphId);
        boundingBox.set(glyph.left(), glyph.top(), glyph.right(), glyph.bottom());
    }

    public RectF computeBoundingBox(int glyphId) {
        RectF boundingBox = new RectF();
        copyBoundingBox(glyphId, boundingBox);

        return boundingBox;
    }

    public RectF computeBoundingBox(int[] glyphIds,
                                    float[] xOffsets, float[] yOffsets, float[] advances) {
        return computeBoundingBox(glyphIds, xOffsets, yOffsets, advances, 0, glyphIds.length);
    }

    public RectF computeBoundingBox(int[] glyphIds,
                                    float[] xOffsets, float[] yOffsets, float[] advances,
                                    int fromIndex, int toIndex) {
        RectF glyphBBox = new RectF();
        RectF cumulativeBBox = new RectF();

        float penX = 0.0f;

        for (int i = fromIndex; i < toIndex; i++) {
            int glyphId = glyphIds[i];
            float xOffset = xOffsets[i] * mTextScaleX;
            float yOffset = yOffsets[i] * mTextScaleY;
            float advance = advances[i] * mTextScaleX;

            copyBoundingBox(glyphId, glyphBBox);
            glyphBBox.offset(penX + xOffset, yOffset);
            cumulativeBBox.union(cumulativeBBox);

            penX += advance;
        }

        return cumulativeBBox;
    }

    private void drawGlyphs(Canvas canvas,
                            int[] glyphIds, float[] xOffsets, float[] yOffsets, float[] advances,
                            int fromIndex, int toIndex, boolean strokeMode) {
        GlyphCache cache = GlyphCache.getInstance();

        boolean reverseMode = (mTextDirection == TextDirection.RIGHT_TO_LEFT);
        float penX = 0.0f;

        for (int i = fromIndex; i < toIndex; i++) {
            int pos = (!reverseMode ? i : fromIndex + (toIndex - i) - 1);

            int glyphId = glyphIds[pos];
            float xOffset = xOffsets[pos] * mTextScaleX;
            float yOffset = yOffsets[pos] * mTextScaleY;
            float advance = advances[pos] * mTextScaleX;

            Glyph maskGlyph = (!strokeMode
                               ? cache.getMaskGlyph(mGlyphStrike, glyphId)
                               : cache.getMaskGlyph(mGlyphStrike, glyphId, mGlyphLineRadius,
                                                    mGlyphLineCap, mGlyphLineJoin, mGlyphMiterLimit));
            Bitmap maskBitmap = maskGlyph.bitmap();
            if (maskBitmap != null) {
                int left = (int) (penX + xOffset + maskGlyph.left() + 0.5f);
                int top = (int) (-yOffset - maskGlyph.top() + 0.5f);

                canvas.drawBitmap(maskBitmap, left, top, mPaint);
            }

            penX += advance;
        }
    }

    public void drawGlyphs(Canvas canvas,
                           int[] glyphIds, float[] xOffsets, float[] yOffsets, float[] advances,
                           int fromIndex, int toIndex) {
        if (mShouldRender) {
            syncShadowLayer();

            if (mShadowRadius > 0.0f && canvas.isHardwareAccelerated()) {
                Log.e(TAG, "Canvas is hardware accelerated, shadow will not be rendered");
            }

            if (mRenderingStyle == Style.FILL || mRenderingStyle == Style.FILL_STROKE) {
                mPaint.setColor(mTextColor);
                drawGlyphs(canvas, glyphIds, xOffsets, yOffsets, advances, fromIndex, toIndex, false);
            }

            if (mRenderingStyle == Style.STROKE || mRenderingStyle == Style.FILL_STROKE) {
                mPaint.setColor(mStrokeColor);
                drawGlyphs(canvas, glyphIds, xOffsets, yOffsets, advances, fromIndex, toIndex, true);
            }
        }
    }
}
