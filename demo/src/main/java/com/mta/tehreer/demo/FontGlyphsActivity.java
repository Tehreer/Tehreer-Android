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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;

public class FontGlyphsActivity extends AppCompatActivity {

    private int[] mTypefaceResIds = { R.string.typeface_taj_nastaleeq, R.string.typeface_nafees_web};
    private String[] mTypefaceNames = { "AlQalam Taj Nastaleeq", "Nafees Web Naskh" };

    private GridView mGlyphsGridView;
    private int mTypefaceResId;

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
                convertView = inflater.inflate(R.layout.item_font_glyph, parent, false);

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

        static final float[] DIMENSION = { 0.0f };

        final Renderer renderer;
        final int[] glyphId;
        final RectF glyphBounds;
        final float glyphAscent;
        final float glyphHeight;

        GlyphDrawable(Renderer renderer, int glyphId) {
            Typeface typeface = renderer.getTypeface();
            float sizeByEm = renderer.getTextSize() / typeface.getUnitsPerEm();

            this.renderer = renderer;
            this.glyphId = new int[] { glyphId };
            this.glyphBounds = renderer.computeBoundingBox(glyphId);
            this.glyphAscent = typeface.getAscent() * sizeByEm;
            this.glyphHeight = glyphAscent + (typeface.getDescent() * sizeByEm);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect drawableBounds = getBounds();

            int left = (int) (drawableBounds.left + (drawableBounds.width() - glyphBounds.width()) / 2.0f - glyphBounds.left + 0.5f);
            int top = (int) (drawableBounds.top + (drawableBounds.height() - glyphHeight) / 2.0f + glyphAscent + 0.5f);

            canvas.translate(left, top);
            renderer.drawGlyphs(canvas, glyphId, DIMENSION, DIMENSION, DIMENSION, 0, 1);
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
        setContentView(R.layout.activity_font_glyphs);

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

        ArrayAdapter<String> typefaceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mTypefaceNames);
        typefaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner typefaceSpinner = (Spinner) findViewById(R.id.spinner_font);
        typefaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadTypeface(mTypefaceResIds[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        typefaceSpinner.setAdapter(typefaceAdapter);
        typefaceSpinner.setSelection(1);
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    private void loadTypeface(int typefaceResId) {
        if (mTypefaceResId != typefaceResId) {
            mTypefaceResId = typefaceResId;

            Resources resources = getResources();
            Typeface typeface = TypefaceManager.getTypeface(resources.getString(typefaceResId));
            float fontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, resources.getDisplayMetrics());

            Renderer renderer = new Renderer();
            renderer.setTypeface(typeface);
            renderer.setTextSize(fontSize);

            mGlyphsGridView.setAdapter(new GlyphAdapter(this, renderer));
        }
    }

    private void displayGlyph(int glyphId) {
        String typefaceTag = getResources().getString(mTypefaceResId);

        Intent intent = new Intent(this, GlyphInfoActivity.class);
        intent.putExtra(GlyphInfoActivity.TYPEFACE_TAG, typefaceTag);
        intent.putExtra(GlyphInfoActivity.GLYPH_ID, glyphId);

        startActivity(intent);
    }
}
