/*
 * Copyright (C) 2021 Muhammad Tayyab Akram
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

import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.widget.SeekBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.widget.TTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class TextViewWidgetActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_view_widget);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final TTextView textView = findViewById(R.id.text_view);
        textView.setTypeface(TypefaceManager.getTypeface(R.id.typeface_noorehuda));
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setSpanned(parseSurah());
        textView.setLineHeightMultiplier(0.80f);
        textView.setJustificationEnabled(true);

        final SeekBar justificationLevelBar = findViewById(R.id.seek_bar_justification_level);
        justificationLevelBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textView.setJustificationLevel(((i / 4.0f) * 0.35f) + 0.65f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private Spanned parseSurah() {
        try {
            final AssetManager assetManager = getAssets();
            final InputStream stream = assetManager.open("AlKahf.json");

            final char[] buffer = new char[1024];
            final StringBuilder out = new StringBuilder();
            final Reader in = new InputStreamReader(stream);
            int length;

            while ((length = in.read(buffer, 0, buffer.length)) > 0) {
                out.append(buffer, 0, length);
            }

            final String jsonString = out.toString();
            final SpannableStringBuilder surah = new SpannableStringBuilder();

            final JSONArray ayahsJson = new JSONArray(jsonString);
            final int ayahCount = ayahsJson.length();

            for (int i = 0; i < ayahCount; i++) {
                final JSONObject ayahJson = ayahsJson.getJSONObject(i);
                final String ayahText = ayahJson.getString("text");
                final JSONArray attrsJson = ayahJson.getJSONArray("attributes");
                final int attrCount = attrsJson.length();

                final int offset = surah.length();
                surah.append(ayahText);

                for (int j = 0; j < attrCount; j++) {
                    final JSONObject attrJson = attrsJson.getJSONObject(j);
                    final int start = attrJson.getInt("start");
                    final int end = attrJson.getInt("end");
                    final String colorString = attrJson.getString("color");
                    final int color = Color.parseColor("#" + colorString);

                    surah.setSpan(new ForegroundColorSpan(color), start + offset,
                                  end + offset, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                surah.append(i == 0 ? "\n" : "  ");
            }

            return surah;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
