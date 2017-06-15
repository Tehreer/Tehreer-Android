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

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.opentype.ShapingEngine;
import com.mta.tehreer.opentype.ShapingResult;
import com.mta.tehreer.util.FloatList;
import com.mta.tehreer.util.IntList;
import com.mta.tehreer.util.PointList;

import java.util.ArrayList;
import java.util.List;

public class OpenTypeInfoActivity extends AppCompatActivity {

    public static final String TYPEFACE_NAME = "typeface_name";
    public static final String TYPE_SIZE = "type_size";
    public static final String SCRIPT_TAG = "script_tag";
    public static final String LANGUAGE_TAG = "language_tag";
    public static final String SOURCE_TEXT = "source_text";

    private static final PointList OFFSET = PointList.of(new float[] { 0, 0 });
    private static final FloatList ADVANCE = FloatList.of(new float[] { 0 });
    private static final int PADDING = 4;

    private static class GlyphSpan extends ReplacementSpan {

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

    private static class CharDetailHolder {
        final View rootLayout;
        final TextView characterTextView;

        CharDetailHolder(View layout) {
            rootLayout = layout;
            characterTextView = (TextView) layout.findViewById(R.id.text_view_character);
        }
    }

    private static class GlyphDetailHolder {
        final View rootLayout;
        final TextView glyphIdTextView;
        final TextView offsetTextView;
        final TextView advanceTextView;

        GlyphDetailHolder(View layout) {
            rootLayout = layout;
            glyphIdTextView = (TextView) layout.findViewById(R.id.text_view_glyph_id);
            offsetTextView = (TextView) layout.findViewById(R.id.text_view_offset);
            advanceTextView = (TextView) layout.findViewById(R.id.text_view_advance);
        }
    }

    private static class ClusterDetailHolder {
        final ViewGroup charInfoLayout;
        final ViewGroup glyphInfoLayout;
        final List<CharDetailHolder> charDetailList = new ArrayList<>();
        final List<GlyphDetailHolder> glyphDetailList = new ArrayList<>();

        ClusterDetailHolder(View layout) {
            charInfoLayout = (ViewGroup) layout.findViewById(R.id.layout_char_detail);
            glyphInfoLayout = (ViewGroup) layout.findViewById(R.id.layout_glyph_detail);
            charDetailList.add(new CharDetailHolder(charInfoLayout.getChildAt(0)));
            glyphDetailList.add(new GlyphDetailHolder(glyphInfoLayout.getChildAt(0)));
        }
    }

    private static class CharDetailAdapter extends BaseAdapter {

        final Context context;
        final Renderer renderer;
        final String source;
        final ShapingResult result;
        final int[] initials;
        final int clusters;
        final IntList glyphIds;
        final PointList glyphOffsets;
        final FloatList glyphAdvances;
        final IntList clusterMap;

        CharDetailAdapter(Context context, Renderer renderer, String source, ShapingResult result,
                          int[] initials, int clusters) {
            this.context = context;
            this.renderer = renderer;
            this.source = source;
            this.result = result;
            this.initials = initials;
            this.clusters = clusters;
            this.glyphIds = result.getGlyphIds();
            this.glyphOffsets = result.getGlyphOffsets();
            this.glyphAdvances = result.getGlyphAdvances();
            this.clusterMap = result.getClusterMap();
        }

        @Override
        public int getCount() {
            return clusters;
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
            final ClusterDetailHolder clusterDetailHolder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.item_cluster_detail, parent, false);
                clusterDetailHolder = new ClusterDetailHolder(convertView);

                convertView.setTag(clusterDetailHolder);
            } else {
                clusterDetailHolder = (ClusterDetailHolder) convertView.getTag();
            }

            // Find out character range of this cluster.
            int charStart = initials[i];
            int charEnd = initials[i + 1];
            int charCount = charEnd - charStart;

            // Find out glyph range of this cluster.
            int glyphStart = clusterMap.get(charStart);
            int glyphEnd = (charEnd < clusterMap.size() ? clusterMap.get(charEnd) : glyphIds.size());
            int glyphCount = glyphEnd - glyphStart;

            final List<CharDetailHolder> charDetailList = clusterDetailHolder.charDetailList;

            // Setup layouts for all characters in this cluster.
            for (int j = 0; j < charCount; j++) {
                if (charDetailList.size() <= j) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View layout = inflater.inflate(R.layout.item_char_detail, clusterDetailHolder.charInfoLayout, false);

                    clusterDetailHolder.charInfoLayout.addView(layout, j);
                    charDetailList.add(new CharDetailHolder(layout));
                }

                int index = charStart + j;
                char character = source.charAt(index);

                CharDetailHolder charDetailHolder = charDetailList.get(j);
                charDetailHolder.rootLayout.setVisibility(View.VISIBLE);
                charDetailHolder.rootLayout.setPadding(0, 0, 0, 1);
                charDetailHolder.characterTextView.setText(String.format("\u2066%04X (%c)", (int) character, character));
            }
            // Hide additional layouts.
            for (int j = charCount; j < charDetailList.size(); j++) {
                charDetailList.get(j).rootLayout.setVisibility(View.GONE);
            }
            // Hide last separator, if needed.
            if (charCount >= glyphCount) {
                charDetailList.get(charCount - 1).rootLayout.setPadding(0, 0, 0, 0);
            }

            final List<GlyphDetailHolder> glyphDetailList = clusterDetailHolder.glyphDetailList;

            // Setup layouts for all glyphs in this cluster.
            for (int j = 0; j < glyphCount; j++) {
                if (glyphDetailList.size() <= j) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View layout = inflater.inflate(R.layout.item_glyph_detail, clusterDetailHolder.glyphInfoLayout, false);

                    clusterDetailHolder.glyphInfoLayout.addView(layout, j);
                    glyphDetailList.add(new GlyphDetailHolder(layout));
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

                GlyphDetailHolder glyphDetailHolder = glyphDetailList.get(j);
                glyphDetailHolder.rootLayout.setVisibility(View.VISIBLE);
                glyphDetailHolder.rootLayout.setPadding(0, 0, 0, 1);
                glyphDetailHolder.glyphIdTextView.setText(glyphSpannable);
                glyphDetailHolder.offsetTextView.setText("(" + xOffset + ", " + yOffset + ")");
                glyphDetailHolder.advanceTextView.setText(String.valueOf(advance));
            }
            // Hide additional layouts.
            for (int j = glyphCount; j < glyphDetailList.size(); j++) {
                glyphDetailList.get(j).rootLayout.setVisibility(View.GONE);
            }
            // Hide last separator, if needed.
            if (glyphCount >= charCount) {
                glyphDetailList.get(glyphCount - 1).rootLayout.setPadding(0, 0, 0, 0);
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

        Typeface typeface = TypefaceManager.getDefaultManager().getTypefaceByName(typefaceName);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float displaySize = 14.0f * displayMetrics.scaledDensity;
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

        CharDetailAdapter charDetailAdapter = new CharDetailAdapter(this, renderer, sourceText, shapingResult, initials, cluster);
        ListView infoListView = (ListView) findViewById(R.id.list_view_info);
        infoListView.setAdapter(charDetailAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
