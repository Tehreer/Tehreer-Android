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
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ReplacementSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.sfnt.ShapingEngine;
import com.mta.tehreer.sfnt.ShapingResult;

public class OpenTypeInfoActivity extends AppCompatActivity {

    public static final String TYPEFACE_NAME = "typeface_name";
    public static final String TYPE_SIZE = "type_size";
    public static final String SCRIPT_TAG = "script_tag";
    public static final String LANGUAGE_TAG = "language_tag";
    public static final String SOURCE_TEXT = "source_text";

    private static class ClusterHolder {

        final ViewGroup charDetail;
        final ViewGroup glyphDetail;

        ClusterHolder(View layout) {
            charDetail = layout.findViewById(R.id.layout_char_detail);
            glyphDetail = layout.findViewById(R.id.layout_glyph_detail);
        }
    }

    private static class CharHolder {
        final TextView character;

        CharHolder(View layout) {
            character = layout.findViewById(R.id.text_view_character);
        }
    }

    private static class GlyphHolder {
        final TextView glyphId;
        final TextView offset;
        final TextView advance;

        GlyphHolder(View layout) {
            glyphId = layout.findViewById(R.id.text_view_glyph_id);
            offset = layout.findViewById(R.id.text_view_offset);
            advance = layout.findViewById(R.id.text_view_advance);
        }
    }

    private static class GlyphSpan extends ReplacementSpan {
        static final PointList OFFSET = PointList.of(new float[] { 0, 0 });
        static final FloatList ADVANCE = FloatList.of(new float[] { 0 });
        static final int PADDING = 4;

        final Renderer renderer;
        final IntList glyphId;
        final float glyphX;
        final int spanWidth;

        GlyphSpan(Renderer renderer, int glyphId) {
            RectF bbox = renderer.computeBoundingBox(glyphId);

            this.renderer = renderer;
            this.glyphId = IntList.of(new int[] { glyphId });
            this.glyphX = -bbox.left + (PADDING / 2);
            this.spanWidth = (int) (bbox.width() + 0.5f) + PADDING;
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fontMetrics) {
            return spanWidth;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, Paint paint) {
            int transX = (int) (x + glyphX + 0.5f);
            int transY = bottom - paint.getFontMetricsInt().descent;

            canvas.translate(transX, transY);
            renderer.drawGlyphs(canvas, glyphId, OFFSET, ADVANCE);
            canvas.translate(-transX, -transY);
        }
    }

    private static class ClusterAdapter extends BaseAdapter {

        final Context context;
        final Renderer renderer;
        final String sourceText;
        final ShapingResult shapingResult;
        final IntList glyphIds;
        final PointList glyphOffsets;
        final FloatList glyphAdvances;
        final IntList clusterMap;
        final IntList clusterInitials;
        final int dp;

        ClusterAdapter(Context context, Renderer renderer, String sourceText,
                       ShapingResult shapingResult, IntList clusterInitials) {
            this.context = context;
            this.renderer = renderer;
            this.sourceText = sourceText;
            this.shapingResult = shapingResult;
            this.glyphIds = shapingResult.getGlyphIds();
            this.glyphOffsets = shapingResult.getGlyphOffsets();
            this.glyphAdvances = shapingResult.getGlyphAdvances();
            this.clusterMap = shapingResult.getClusterMap();
            this.clusterInitials = clusterInitials;
            this.dp = (int) (context.getResources().getDisplayMetrics().density + 0.5f);
        }

        @Override
        public int getCount() {
            return clusterInitials.size() - 1;
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
            final ClusterHolder clusterHolder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.item_cluster_detail, parent, false);

                clusterHolder = new ClusterHolder(convertView);
                convertView.setTag(clusterHolder);

                View firstChar = clusterHolder.charDetail.getChildAt(0);
                firstChar.setTag(new CharHolder(firstChar));

                View firstGlyph = clusterHolder.glyphDetail.getChildAt(0);
                firstGlyph.setTag(new GlyphHolder(firstGlyph));
            } else {
                clusterHolder = (ClusterHolder) convertView.getTag();
            }

            // Find out character range of this cluster.
            int charStart = clusterInitials.get(i);
            int charEnd = clusterInitials.get(i + 1);
            int charCount = charEnd - charStart;

            // Find out glyph range of this cluster.
            int glyphStart = clusterMap.get(charStart);
            int glyphEnd = (charEnd < clusterMap.size() ? clusterMap.get(charEnd) : glyphIds.size());
            int glyphCount = glyphEnd - glyphStart;

            // Setup layouts for all characters in this cluster.
            for (int j = 0; j < charCount; j++) {
                final View layout;

                if (clusterHolder.charDetail.getChildCount() <= j) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    layout = inflater.inflate(R.layout.item_char_detail, clusterHolder.charDetail, false);
                    layout.setTag(new CharHolder(layout));

                    clusterHolder.charDetail.addView(layout);
                } else {
                    layout = clusterHolder.charDetail.getChildAt(j);
                }

                int index = charStart + j;
                char character = sourceText.charAt(index);

                CharHolder charHolder = (CharHolder) layout.getTag();
                charHolder.character.setText(String.format("\u2066%04X (%c)", (int) character, character));

                layout.setPadding(0, 0, 0, dp);
                layout.setVisibility(View.VISIBLE);
            }
            // Hide additional layouts.
            for (int j = charCount; j < clusterHolder.charDetail.getChildCount(); j++) {
                clusterHolder.charDetail.getChildAt(j).setVisibility(View.GONE);
            }
            // Hide last separator, if needed.
            if (charCount >= glyphCount) {
                clusterHolder.charDetail.getChildAt(charCount - 1).setPadding(0, 0, 0, 0);
            }

            // Setup layouts for all glyphs in this cluster.
            for (int j = 0; j < glyphCount; j++) {
                final View layout;

                if (clusterHolder.glyphDetail.getChildCount() <= j) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    layout = inflater.inflate(R.layout.item_glyph_detail, clusterHolder.glyphDetail, false);
                    layout.setTag(new GlyphHolder(layout));

                    clusterHolder.glyphDetail.addView(layout);
                } else {
                    layout = clusterHolder.glyphDetail.getChildAt(j);
                }

                int index = glyphStart + j;
                int glyphId = glyphIds.get(index);
                int xOffset = (int) (glyphOffsets.getX(index) + 0.5f);
                int yOffset = (int) (glyphOffsets.getY(index) + 0.5f);
                int advance = (int) (glyphAdvances.get(index) + 0.5f);

                String glyphString = String.format("%04X (_)", glyphId);
                GlyphSpan glyphSpan = new GlyphSpan(renderer, glyphId);
                SpannableString glyphSpannable = new SpannableString(glyphString);
                glyphSpannable.setSpan(glyphSpan, 6, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                GlyphHolder glyphHolder = (GlyphHolder) layout.getTag();
                glyphHolder.glyphId.setText(glyphSpannable);
                glyphHolder.offset.setText("(" + xOffset + ", " + yOffset + ")");
                glyphHolder.advance.setText(String.valueOf(advance));

                layout.setPadding(0, 0, 0, dp);
                layout.setVisibility(View.VISIBLE);
            }
            // Hide additional layouts.
            for (int j = glyphCount; j < clusterHolder.glyphDetail.getChildCount(); j++) {
                clusterHolder.glyphDetail.getChildAt(j).setVisibility(View.GONE);
            }
            // Hide last separator, if needed.
            if (glyphCount >= charCount) {
                clusterHolder.glyphDetail.getChildAt(glyphCount - 1).setPadding(0, 0, 0, 0);
            }

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opentype_info);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        String typefaceName = intent.getStringExtra(TYPEFACE_NAME);
        int typeSize = intent.getIntExtra(TYPE_SIZE, 0);
        int scriptTag = intent.getIntExtra(SCRIPT_TAG, 0);
        int languageTag = intent.getIntExtra(LANGUAGE_TAG, 0);
        String sourceText = intent.getCharSequenceExtra(SOURCE_TEXT).toString();

        Typeface typeface = TypefaceManager.getTypefaceByName(typefaceName);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float displaySize = 18.0f * displayMetrics.scaledDensity;
        float sizeScale = displaySize / typeSize;

        Renderer renderer = new Renderer();
        renderer.setTypeface(typeface);
        renderer.setTypeSize(typeSize);
        renderer.setScaleX(sizeScale);
        renderer.setScaleY(sizeScale);

        ShapingEngine shapingEngine = ShapingEngine.finalizable(new ShapingEngine());
        shapingEngine.setTypeface(typeface);
        shapingEngine.setTypeSize(typeSize);
        shapingEngine.setScriptTag(scriptTag);
        shapingEngine.setLanguageTag(languageTag);

        ShapingResult shapingResult = ShapingResult.finalizable(shapingEngine.shapeText(sourceText, 0, sourceText.length()));
        IntList clusterMap = shapingResult.getClusterMap();
        int length = clusterMap.size();

        int[] initials = new int[length + 1];
        int cluster = -1;
        int previous = -1;

        for (int i = 0; i < length; i++) {
            int value = clusterMap.get(i);
            if (value != previous) {
                initials[++cluster] = i;
            }

            previous = value;
        }
        initials[++cluster] = length;

        ListView infoListView = findViewById(R.id.list_view_info);
        infoListView.setAdapter(new ClusterAdapter(this, renderer, sourceText, shapingResult,
                                                   IntList.of(initials).subList(0, cluster + 1)));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
