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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.opentype.OpenTypeAlbum;
import com.mta.tehreer.opentype.OpenTypeArtist;
import com.mta.tehreer.opentype.OpenTypeTag;

public class OpenTypeShapingActivity extends AppCompatActivity {

    private int[] mTypefaceResIds = { R.string.typeface_taj_nastaleeq, R.string.typeface_nafees_web};
    private String[] mTypefaceNames = { "AlQalam Taj Nastaleeq", "Nafees Web Naskh" };

    private OpenTypeArtist mArtist = OpenTypeArtist.finalizable(new OpenTypeArtist());
    private OpenTypeAlbum mAlbum = OpenTypeAlbum.finalizable(new OpenTypeAlbum());

    private Spinner mTypefaceSpinner;
    private EditText mScriptTagField;
    private EditText mLanguageTagField;
    private EditText mTextField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opentype_shaping);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ArrayAdapter<String> typefaceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mTypefaceNames);
        typefaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTypefaceSpinner = (Spinner) findViewById(R.id.spinner_typeface);
        mTypefaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mArtist.setTypeface(TypefaceManager.getTypeface(getResources().getString(mTypefaceResIds[i])));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mTypefaceSpinner.setAdapter(typefaceAdapter);
        mTypefaceSpinner.setSelection(1);

        mScriptTagField = (EditText) findViewById(R.id.field_script_tag);
        mLanguageTagField = (EditText) findViewById(R.id.field_language_tag);
        mTextField = (EditText) findViewById(R.id.field_text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_opentype_shaping, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_shape:
            shape();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void shape() {
        CharSequence scriptTag = mScriptTagField.getText();
        try {
            mArtist.setScriptTag(OpenTypeTag.make(scriptTag.toString()));
        } catch (IllegalArgumentException ignored) {
            Toast.makeText(this, "Write a valid script tag!", Toast.LENGTH_LONG).show();
            return;
        }

        CharSequence languageTag = mLanguageTagField.getText();
        try {
            mArtist.setLanguageTag(OpenTypeTag.make(languageTag.toString()));
        } catch (IllegalArgumentException ignored) {
            Toast.makeText(this, "Write a valid language tag!", Toast.LENGTH_LONG).show();
            return;
        }

        CharSequence text = mTextField.getText();
        mArtist.setText(text.toString());
        mArtist.fillAlbum(mAlbum);

        int glyphCount = mAlbum.getGlyphCount();
        int[] glyphIds = new int[glyphCount];
        float[] xOffsets = new float[glyphCount];
        float[] yOffsets = new float[glyphCount];
        float[] advances = new float[glyphCount];
        mAlbum.copyGlyphInfos(0, glyphCount, 1.0f, glyphIds, xOffsets, yOffsets, advances);

        int charCount = mAlbum.getCharEnd() - mAlbum.getCharStart();
        int[] charGlyphIndexes = new int[charCount];
        mAlbum.copyCharGlyphIndexes(0, charCount, charGlyphIndexes);

        Intent intent = new Intent(this, OpenTypeInfoActivity.class);
        intent.putExtra(OpenTypeInfoActivity.GLYPH_IDS, glyphIds);
        intent.putExtra(OpenTypeInfoActivity.X_OFFSETS, xOffsets);
        intent.putExtra(OpenTypeInfoActivity.Y_OFFSETS, yOffsets);
        intent.putExtra(OpenTypeInfoActivity.ADVANCES, advances);
        intent.putExtra(OpenTypeInfoActivity.CHAR_GLYPH_INDEXES, charGlyphIndexes);

        startActivity(intent);
    }
}
