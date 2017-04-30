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

package com.mta.tehreer.demo;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;

public class GlyphInfoActivity extends AppCompatActivity {

    public static final String TYPEFACE_NAME = "typeface";
    public static final String GLYPH_ID = "glyph_id";

    private Typeface mTypeface;
    private int mGlyphId;

    private ImageView mGlyphImageView;

    private class GlyphDrawable extends Drawable {

        final float density;
        final int minWidth;
        final int minHeight;
        final float fontAscent;
        final float fontDescent;
        final float glyphAdvance;
        final Path glyphPath;
        final RectF glyphBounds;
        final Paint paint;

        GlyphDrawable(int minWidth, int minHeight) {
            this.density = getResources().getDisplayMetrics().density;
            this.minWidth = minWidth;
            this.minHeight = minHeight;

            float typeSize = mTypeface.getUnitsPerEm();
            float displaySize = minHeight / 3.0f;
            float sizeScale = displaySize / typeSize;

            fontAscent = mTypeface.getAscent() * sizeScale;
            fontDescent = mTypeface.getDescent() * sizeScale;
            glyphAdvance = mTypeface.getGlyphAdvance(mGlyphId, typeSize, false) * sizeScale;

            Matrix glyphMatrix = new Matrix();
            glyphMatrix.setScale(sizeScale, sizeScale);
            glyphPath = mTypeface.getGlyphPath(mGlyphId, typeSize, glyphMatrix);

            glyphBounds = new RectF();
            glyphPath.computeBounds(glyphBounds, true);

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTextSize(12.0f * density);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect drawableBounds = getBounds();

            float fontHeight = fontAscent + fontDescent;

            int lsbX = (int) (drawableBounds.left + (drawableBounds.width() - glyphBounds.width()) / 2.0f - glyphBounds.left + 0.5);
            int rsbX = (int) (lsbX + glyphAdvance + 0.5);
            int baseY = (int) (drawableBounds.top + (drawableBounds.height() - fontHeight) / 2.0f + fontAscent + 0.5);
            int ascentY = (int) (baseY - fontAscent + 0.5);
            int descentY = (int) (baseY + fontDescent + 0.5);

            paint.setColor(Color.DKGRAY);
            paint.setStrokeWidth(1.0f);
            paint.setStyle(Paint.Style.STROKE);

            // Draw Vertical Lines.
            canvas.drawLine(lsbX, drawableBounds.top, lsbX, drawableBounds.bottom, paint);
            canvas.drawLine(rsbX, drawableBounds.top, rsbX, drawableBounds.bottom, paint);
            // Draw Horizontal Lines.
            canvas.drawLine(drawableBounds.left, baseY, drawableBounds.right, baseY, paint);
            canvas.drawLine(drawableBounds.left, ascentY, drawableBounds.right, ascentY, paint);
            canvas.drawLine(drawableBounds.left, descentY, drawableBounds.right, descentY, paint);
            // Draw Origin Circle.
            canvas.drawCircle(lsbX, baseY, 4.0f * density, paint);

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);

            // Draw Headings.
            canvas.drawText("Ascent", drawableBounds.left, ascentY - 2.0f * density, paint);
            canvas.drawText("Baseline", drawableBounds.left, baseY - 2.0f * density, paint);
            canvas.drawText("Descent", drawableBounds.left, descentY - 2.0f * density, paint);

            paint.setStrokeWidth(1.0f * density);
            paint.setStyle(Paint.Style.STROKE);

            // Draw Glyph Path.
            canvas.translate(lsbX, baseY);
            canvas.drawPath(glyphPath, paint);
        }

        @Override
        public void setAlpha(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public int getIntrinsicWidth() {
            float padding = 144.0f * density;
            float negativeLSB = Math.min(glyphBounds.left, 0.0f);
            float advanceWidth = glyphAdvance - negativeLSB;
            float pathWidth = glyphBounds.right - negativeLSB;
            int boundaryWidth = (int) (Math.max(advanceWidth, pathWidth) + padding + 0.5f);

            return Math.max(minWidth, boundaryWidth);
        }

        @Override
        public int getIntrinsicHeight() {
            float padding = 32.0f * density;
            int boundaryHeight = (int) (fontAscent + fontDescent + padding + 0.5f);

            return Math.max(minHeight, boundaryHeight);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glyph_info);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        mTypeface = TypefaceManager.getDefaultManager().getTypefaceByName(intent.getStringExtra(TYPEFACE_NAME));
        mGlyphId = intent.getIntExtra(GLYPH_ID, 0);

        mGlyphImageView = (ImageView) findViewById(R.id.image_view_glyph);
        mGlyphImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = findViewById(android.R.id.content).getWidth();
                int height = mGlyphImageView.getHeight();
                Drawable drawable = new GlyphDrawable(width, height);

                mGlyphImageView.setImageDrawable(drawable);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
}
