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
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.opentype.OpenTypeAlbum;
import com.mta.tehreer.opentype.OpenTypeArtist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenTypeInfoActivity extends AppCompatActivity {

    public static final String TYPEFACE_TAG = "typeface_tag";
    public static final String SCRIPT_TAG = "script_tag";
    public static final String LANGUAGE_TAG = "language_tag";
    public static final String SOURCE_TEXT = "source_text";

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

    private static class CharDetailHolder {
        final TextView indexTextView;
        final TextView characterTextView;
        final ViewGroup glyphInfoLayout;
        final TextView feedbackTextView;
        final List<GlyphDetailHolder> glyphDetailList = new ArrayList<>();

        CharDetailHolder(View layout) {
            indexTextView = (TextView) layout.findViewById(R.id.text_view_index);
            characterTextView = (TextView) layout.findViewById(R.id.text_view_character);
            glyphInfoLayout = (ViewGroup) layout.findViewById(R.id.layout_glyph_detail);
            feedbackTextView = (TextView) layout.findViewById(R.id.text_view_feedback);
            glyphDetailList.add(new GlyphDetailHolder(glyphInfoLayout.getChildAt(1)));
        }
    }

    private static class CharDetailAdapter extends BaseAdapter {

        final Context context;
        final String source;
        final OpenTypeAlbum album;
        final int[] initials;
        final int[] relations;
        final int[] totals;

        CharDetailAdapter(Context context, String source, OpenTypeAlbum album,
                                 int[] initials, int[] relations, int[] totals) {
            this.context = context;
            this.source = source;
            this.album = album;
            this.initials = initials;
            this.relations = relations;
            this.totals = totals;
        }

        @Override
        public int getCount() {
            return source.length();
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
            final CharDetailHolder charDetailHolder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(R.layout.item_char_detail, parent, false);
                charDetailHolder = new CharDetailHolder(convertView);

                convertView.setTag(charDetailHolder);
            } else {
                charDetailHolder = (CharDetailHolder) convertView.getTag();
            }
            charDetailHolder.indexTextView.setText(String.valueOf(i + 1));
            charDetailHolder.characterTextView.setText(String.format("%04X (%c)", (int) source.charAt(i), source.charAt(i)));

            final List<GlyphDetailHolder> glyphDetailList = charDetailHolder.glyphDetailList;

            if (totals[i] > 0) {
                charDetailHolder.feedbackTextView.setVisibility(View.GONE);

                for (int j = 0; j < totals[i]; j++) {
                    if (glyphDetailList.size() <= j) {
                        LayoutInflater inflater = LayoutInflater.from(context);
                        View layout = inflater.inflate(R.layout.item_glyph_detail, charDetailHolder.glyphInfoLayout, false);
                        layout.setPadding(0, 1, 0, 0);

                        charDetailHolder.glyphInfoLayout.addView(layout);
                        glyphDetailList.add(new GlyphDetailHolder(layout));
                    }

                    int glyphIndex = initials[i] + j;

                    GlyphDetailHolder glyphDetailHolder = glyphDetailList.get(j);
                    glyphDetailHolder.rootLayout.setVisibility(View.VISIBLE);
                    glyphDetailHolder.glyphIdTextView.setText(String.format("%06X", album.getGlyphId(glyphIndex)));
                    glyphDetailHolder.offsetTextView.setText("(" + album.getGlyphXOffset(glyphIndex) + ", " + album.getGlyphYOffset(glyphIndex) + ")");
                    glyphDetailHolder.advanceTextView.setText(String.valueOf(album.getGlyphAdvance(glyphIndex)));
                }
            } else {
                charDetailHolder.feedbackTextView.setVisibility(View.VISIBLE);

                int relation = relations[i];
                if (relation > -1) {
                    charDetailHolder.feedbackTextView.setText("Related to Character #" + (relation + 1));
                } else {
                    charDetailHolder.feedbackTextView.setText("No Glyph");
                }
            }

            for (int j = totals[i]; j < glyphDetailList.size(); j++) {
                glyphDetailList.get(j).rootLayout.setVisibility(View.GONE);
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
        CharSequence typefaceTag = intent.getCharSequenceExtra(TYPEFACE_TAG);
        int scriptTag = intent.getIntExtra(SCRIPT_TAG, 0);
        int languageTag = intent.getIntExtra(LANGUAGE_TAG, 0);
        CharSequence sourceText = intent.getCharSequenceExtra(SOURCE_TEXT);

        OpenTypeArtist artist = OpenTypeArtist.finalizable(new OpenTypeArtist());
        artist.setTypeface(TypefaceManager.getTypeface(typefaceTag));
        artist.setScriptTag(scriptTag);
        artist.setLanguageTag(languageTag);
        artist.setText(sourceText.toString());

        int charCount = sourceText.length();
        int[] initials = new int[charCount + 1];
        int[] relations = new int[initials.length];
        int[] totals = new int[initials.length];

        OpenTypeAlbum album = OpenTypeAlbum.finalizable(new OpenTypeAlbum());
        artist.fillAlbum(album);
        album.copyCharGlyphIndexes(0, charCount, initials);

        initials[charCount] = album.getGlyphCount();
        Arrays.fill(relations, -1);

        for (int i = 0; i < initials.length; i++) {
            // Skip, if this character has no glyph or related to some other character.
            if (initials[i] == -1 || relations[i] > -1) {
                continue;
            }

            // Setup relations for this character.
            for (int j = i + 1; j < initials.length; j++) {
                if (initials[j] == initials[i]) {
                    relations[j] = i;
                }
            }

            // Setup totals for this character.
            for (int j = i + 1; j < initials.length; j++) {
                if (initials[j] > initials[i]) {
                    totals[i] = initials[j] - initials[i];
                    break;
                }
            }
        }

        CharDetailAdapter charDetailAdapter = new CharDetailAdapter(this, artist.getText(), album, initials, relations, totals);
        ListView infoListView = (ListView) findViewById(R.id.list_view_info);
        infoListView.setAdapter(charDetailAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
