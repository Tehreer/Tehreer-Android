/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

import android.content.Context;
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

    private static class GlyphDrawable extends Drawable {

        final float dp;
        final int minWidth;
        final int minHeight;
        final float ascent;
        final float descent;
        final float advance;
        final Path glyphPath;
        final RectF glyphBounds;
        final Paint paint;

        GlyphDrawable(Context context, Typeface typeface, int glyphId, int minWidth, int minHeight) {
            this.dp = context.getResources().getDisplayMetrics().density;
            this.minWidth = minWidth;
            this.minHeight = minHeight;

            float typeSize = typeface.getUnitsPerEm();
            float displaySize = minHeight / 3.0f;
            float sizeScale = displaySize / typeSize;

            ascent = typeface.getAscent() * sizeScale;
            descent = typeface.getDescent() * sizeScale;
            advance = typeface.getGlyphAdvance(glyphId, typeSize, false) * sizeScale;

            Matrix glyphMatrix = new Matrix();
            glyphMatrix.setScale(sizeScale, sizeScale);
            glyphPath = typeface.getGlyphPath(glyphId, typeSize, glyphMatrix);

            glyphBounds = new RectF();
            glyphPath.computeBounds(glyphBounds, true);

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTextSize(12.0f * dp);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect drawableBounds = getBounds();

            float height = ascent + descent;

            int lsbX = (int) (drawableBounds.left + (drawableBounds.width() - glyphBounds.width()) / 2.0f - glyphBounds.left + 0.5);
            int rsbX = (int) (lsbX + advance + 0.5);
            int baseY = (int) (drawableBounds.top + (drawableBounds.height() - height) / 2.0f + ascent + 0.5);
            int ascentY = (int) (baseY - ascent + 0.5);
            int descentY = (int) (baseY + descent + 0.5);

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
            canvas.drawCircle(lsbX, baseY, 4.0f * dp, paint);

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);

            // Draw Headings.
            canvas.drawText("Ascent", drawableBounds.left, ascentY - 2.0f * dp, paint);
            canvas.drawText("Baseline", drawableBounds.left, baseY - 2.0f * dp, paint);
            canvas.drawText("Descent", drawableBounds.left, descentY - 2.0f * dp, paint);

            paint.setStrokeWidth(1.0f * dp);
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
            float padding = 144.0f * dp;
            float negativeLSB = Math.min(glyphBounds.left, 0.0f);
            float advanceWidth = advance - negativeLSB;
            float pathWidth = glyphBounds.right - negativeLSB;
            int boundaryWidth = (int) (Math.max(advanceWidth, pathWidth) + padding + 0.5f);

            return Math.max(minWidth, boundaryWidth);
        }

        @Override
        public int getIntrinsicHeight() {
            float padding = 32.0f * dp;
            int boundaryHeight = (int) (ascent + descent + padding + 0.5f);

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
        final Typeface typeface = TypefaceManager.getTypefaceByName(intent.getStringExtra(TYPEFACE_NAME));
        final int glyphId = intent.getIntExtra(GLYPH_ID, 0);
        final ImageView glyphInfo = (ImageView) findViewById(R.id.image_view_glyph_info);

        glyphInfo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = findViewById(android.R.id.content).getWidth();
                int height = glyphInfo.getHeight();
                Drawable drawable = new GlyphDrawable(GlyphInfoActivity.this, typeface, glyphId, width, height);

                glyphInfo.setImageDrawable(drawable);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
}
