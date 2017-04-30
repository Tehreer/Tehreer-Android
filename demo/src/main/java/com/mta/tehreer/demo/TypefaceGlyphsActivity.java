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
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.util.FloatList;
import com.mta.tehreer.util.IntList;
import com.mta.tehreer.util.PointList;

public class TypefaceGlyphsActivity extends AppCompatActivity {

    private GridView mGlyphsGridView;
    private Typeface mTypeface;

    private static class GlyphHolder {
        ImageView glyphImageView;
        TextView glyphIdTextView;
    }

    private static class GlyphAdapter extends BaseAdapter {

        final Context context;
        final Renderer renderer;

        GlyphAdapter(Context context, Renderer renderer) {
            this.context = context;
            this.renderer = renderer;
        }

        @Override
        public int getCount() {
            return renderer.getTypeface().getGlyphCount();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            GlyphHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.item_typeface_glyph, parent, false);

                holder = new GlyphHolder();
                holder.glyphImageView = (ImageView) convertView.findViewById(R.id.image_view_glyph);
                holder.glyphIdTextView = (TextView) convertView.findViewById(R.id.text_view_glyph_id);

                convertView.setTag(holder);
            } else {
                holder = (GlyphHolder) convertView.getTag();
            }

            holder.glyphIdTextView.setText(String.format("%06X", i));
            holder.glyphImageView.setImageDrawable(new GlyphDrawable(renderer, i));

            return convertView;
        }
    }

    private static class GlyphDrawable extends Drawable {

        static final PointList OFFSET = PointList.of(new float[] { 0, 0 });
        static final FloatList ADVANCE = FloatList.of(new float[] { 0 });

        final Renderer renderer;
        final IntList glyphId;
        final RectF glyphBounds;
        final float fontAscent;
        final float fontDescent;

        GlyphDrawable(Renderer renderer, int glyphId) {
            Typeface typeface = renderer.getTypeface();
            float sizeByEm = renderer.getTypeSize() / typeface.getUnitsPerEm();

            this.renderer = renderer;
            this.glyphId = IntList.of(new int[] { glyphId });
            this.glyphBounds = renderer.computeBoundingBox(glyphId);
            this.fontAscent = typeface.getAscent() * sizeByEm;
            this.fontDescent = typeface.getDescent() * sizeByEm;
        }

        @Override
        public void draw(Canvas canvas) {
            Rect drawableBounds = getBounds();

            float fontHeight = fontAscent + fontDescent;

            int left = (int) (drawableBounds.left + (drawableBounds.width() - glyphBounds.width()) / 2.0f - glyphBounds.left + 0.5f);
            int top = (int) (drawableBounds.top + (drawableBounds.height() - fontHeight) / 2.0f + fontAscent + 0.5f);

            canvas.translate(left, top);
            renderer.drawGlyphs(canvas, glyphId, OFFSET, ADVANCE);
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
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typeface_glyphs);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mGlyphsGridView = (GridView) findViewById(R.id.grid_view_glyphs);
        mGlyphsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                displayGlyph(i);
            }
        });

        Spinner typefaceSpinner = (Spinner) findViewById(R.id.spinner_typeface);
        typefaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadTypeface((Typeface) adapterView.getAdapter().getItem(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        typefaceSpinner.setAdapter(new TypefaceAdapter(this));
        typefaceSpinner.setSelection(0);
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    private void loadTypeface(Typeface typeface) {
        if (typeface != mTypeface) {
            mTypeface = typeface;

            float density = getResources().getDisplayMetrics().density;
            float typeSize = 28.0f * density;

            Renderer renderer = new Renderer();
            renderer.setTypeface(typeface);
            renderer.setTypeSize(typeSize);

            mGlyphsGridView.setAdapter(new GlyphAdapter(this, renderer));
        }
    }

    private void displayGlyph(int glyphId) {
        Intent intent = new Intent(this, GlyphInfoActivity.class);
        intent.putExtra(GlyphInfoActivity.TYPEFACE_NAME, mTypeface.getFullName());
        intent.putExtra(GlyphInfoActivity.GLYPH_ID, glyphId);

        startActivity(intent);
    }
}
