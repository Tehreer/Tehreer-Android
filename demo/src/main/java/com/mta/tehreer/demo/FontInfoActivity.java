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

import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.opentype.SfntNames;

import java.util.Locale;
import java.util.Map;

public class FontInfoActivity extends AppCompatActivity {

    private int[] mTypefaceResIds = { R.string.typeface_taj_nastaleeq, R.string.typeface_nafees_web};
    private String[] mTypefaceNames = { "AlQalam Taj Nastaleeq", "Nafees Web Naskh" };
    private int mTypefaceResId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_info);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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

    private void configureName(int layoutResId, Map<Locale, String> nameMap) {
        LinearLayout nameLayout = (LinearLayout) findViewById(layoutResId);
        if (nameMap.size() > 0) {
            StringBuilder nameBuilder = new StringBuilder();
            for (Map.Entry<Locale, String> entry : nameMap.entrySet()) {
                // Display names corresponding to windows platform only.
                if (Build.VERSION.SDK_INT >= 21) {
                    Locale locale = entry.getKey();
                    String extension = locale.getExtension(Locale.PRIVATE_USE_EXTENSION);
                    if (!extension.equals(SfntNames.WINDOWS_PLATFORM)) {
                        continue;
                    }
                } else {
                    Locale locale = entry.getKey();
                    String variant = locale.getVariant();
                    if (!variant.endsWith(SfntNames.WINDOWS_PLATFORM)) {
                        continue;
                    }
                }

                nameBuilder.append(entry.getValue());
                nameBuilder.append('\n');
            }
            nameBuilder.deleteCharAt(nameBuilder.length() - 1);

            TextView nameTextView = (TextView) nameLayout.getChildAt(1);
            nameTextView.setText(nameBuilder.toString());

            nameLayout.setVisibility(View.VISIBLE);
        } else {
            nameLayout.setVisibility(View.GONE);
        }
    }

    private void loadTypeface(int typefaceResId) {
        if (mTypefaceResId != typefaceResId) {
            mTypefaceResId = typefaceResId;

            Resources resources = getResources();
            Typeface typeface = TypefaceManager.getTypeface(resources.getString(typefaceResId));
            SfntNames sfntNames = SfntNames.forTypeface(typeface);

            configureName(R.id.layout_copyright, sfntNames.getCopyright());
            configureName(R.id.layout_font_family, sfntNames.getFontFamily());
            configureName(R.id.layout_font_subfamily, sfntNames.getFontSubfamily());
            configureName(R.id.layout_unique_id, sfntNames.getUniqueId());
            configureName(R.id.layout_full_name, sfntNames.getFullName());
            configureName(R.id.layout_version, sfntNames.getVersion());
            configureName(R.id.layout_postscript_name, sfntNames.getPostscriptName());
            configureName(R.id.layout_trademark, sfntNames.getTrademark());
            configureName(R.id.layout_manufacturer, sfntNames.getManufacturer());
            configureName(R.id.layout_designer, sfntNames.getDesigner());
            configureName(R.id.layout_description, sfntNames.getDescription());
            configureName(R.id.layout_vendor_url, sfntNames.getVendorUrl());
            configureName(R.id.layout_designer_url, sfntNames.getDesignerUrl());
            configureName(R.id.layout_license, sfntNames.getLicense());
            configureName(R.id.layout_license_url, sfntNames.getLicenseUrl());
            configureName(R.id.layout_typographic_family, sfntNames.getTypographicFamily());
            configureName(R.id.layout_typographic_subfamily, sfntNames.getTypographicSubfamily());
            configureName(R.id.layout_mac_full_name, sfntNames.getMacFullName());
            configureName(R.id.layout_sample_text, sfntNames.getSampleText());
            configureName(R.id.layout_postscript_cid_findfont_name, sfntNames.getPostscriptCIDFindFontName());
            configureName(R.id.layout_wws_family, sfntNames.getWwsFamily());
            configureName(R.id.layout_wws_subfamily, sfntNames.getWwsSubfamily());
            configureName(R.id.layout_light_background_palette, sfntNames.getLightBackgroundPalette());
            configureName(R.id.layout_dark_background_palette, sfntNames.getDarkBackgroundPalette());
            configureName(R.id.layout_variations_postscript_name_prefix, sfntNames.getVariationsPostscriptNamePrefix());

            ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view_font_info);
            scrollView.scrollTo(0, 0);
        }
    }
}
