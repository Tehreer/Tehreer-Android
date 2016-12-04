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
import android.util.TypedValue;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;

public class GlyphInfoActivity extends AppCompatActivity {

    public static final String TYPEFACE_TAG = "typeface_tag";
    public static final String GLYPH_ID = "glyph_id";

    private Typeface mTypeface;
    private int mGlyphId;

    private ImageView mGlyphImageView;

    private class GlyphDrawable extends Drawable {

        final int intrinsicWidth;
        final int intrinsicHeight;
        final float glyphAscent;
        final float glyphHeight;
        final Path glyphPath;
        final RectF glyphBounds;
        final Paint paint;

        GlyphDrawable(int intrinsicWidth, int intrinsicHeight) {
            this.intrinsicWidth = intrinsicWidth;
            this.intrinsicHeight = intrinsicHeight;

            Renderer renderer = new Renderer();
            renderer.setTypeface(mTypeface);
            renderer.setTextSize(mTypeface.getUnitsPerEm());

            float scaledSize = intrinsicHeight / 3.0f;
            float sizeScale = scaledSize / renderer.getTextSize();
            glyphAscent = mTypeface.getAscent() * sizeScale;
            glyphHeight = glyphAscent + (mTypeface.getDescent() * sizeScale);

            Matrix glyphMatrix = new Matrix();
            glyphMatrix.setScale(sizeScale, -sizeScale);

            glyphPath = renderer.generatePath(mGlyphId);
            glyphPath.transform(glyphMatrix);

            glyphBounds = new RectF();
            glyphPath.computeBounds(glyphBounds, true);

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect drawableBounds = getBounds();

            int left = (int) (drawableBounds.left + (drawableBounds.width() - glyphBounds.width()) / 2.0f - glyphBounds.left + 0.5);
            int top = (int) (drawableBounds.top + (drawableBounds.height() - glyphHeight) / 2.0f + glyphAscent + 0.5);

            paint.setStrokeWidth(1.0f);
            canvas.drawLine(left, drawableBounds.top, left, drawableBounds.bottom, paint);
            canvas.drawLine(Math.min(left, drawableBounds.left), top,
                            Math.max(left + glyphBounds.right, drawableBounds.right), top, paint);

            canvas.translate(left, top);

            paint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, getResources().getDisplayMetrics()));
            canvas.drawPath(glyphPath, paint);

            canvas.translate(-left, -top);
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
        public int getIntrinsicHeight() {
            return intrinsicHeight;
        }

        @Override
        public int getIntrinsicWidth() {
            int glyphWidth = (int) (Math.abs(glyphBounds.left) + Math.abs(glyphBounds.right) + 0.5f);
            return Math.max(intrinsicWidth, glyphWidth);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
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
        mTypeface = TypefaceManager.getTypeface(intent.getCharSequenceExtra(TYPEFACE_TAG));
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
