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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class OpenTypeInfoActivity extends AppCompatActivity {

    public static final String GLYPH_IDS = "glyph_ids";
    public static final String X_OFFSETS = "x_offsets";
    public static final String Y_OFFSETS = "y_offsets";
    public static final String ADVANCES = "advances";
    public static final String CHAR_GLYPH_INDEXES = "char_glyph_indexes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opentype_info);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        int[] glyphIds = intent.getIntArrayExtra(GLYPH_IDS);
        float[] xOffsets = intent.getFloatArrayExtra(X_OFFSETS);
        float[] yOffsets = intent.getFloatArrayExtra(Y_OFFSETS);
        float[] advances = intent.getFloatArrayExtra(ADVANCES);
        int[] charGlyphIndexes = intent.getIntArrayExtra(CHAR_GLYPH_INDEXES);

        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int glyphId : glyphIds) {
            builder.append(glyphId).append(", ");
        }
        if (glyphIds.length > 0) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append("]");

        TextView textViewGlyphIds = (TextView) findViewById(R.id.text_view_glyph_ids);
        textViewGlyphIds.setText(builder.toString());

        builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < xOffsets.length; i++) {
            builder.append("(")
                    .append((int) xOffsets[i]).append(", ")
                    .append((int) yOffsets[i]).append(")")
                    .append(", ");
        }
        if (xOffsets.length > 0) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append("]");

        TextView textViewOffsets = (TextView) findViewById(R.id.text_view_glyph_offsets);
        textViewOffsets.setText(builder.toString());

        builder = new StringBuilder();
        builder.append("[");
        for (float advance : advances) {
            builder.append((int) advance).append(", ");
        }
        if (advances.length > 0) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append("]");

        TextView textViewAdvances = (TextView) findViewById(R.id.text_view_glyph_advances);
        textViewAdvances.setText(builder.toString());

        builder = new StringBuilder();
        builder.append("[");
        for (int index : charGlyphIndexes) {
            builder.append(index).append(", ");
        }
        if (charGlyphIndexes.length > 0) {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append("]");

        TextView textViewCharGlyphIndexes = (TextView) findViewById(R.id.text_view_char_glyph_indexes);
        textViewCharGlyphIndexes.setText(builder.toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
