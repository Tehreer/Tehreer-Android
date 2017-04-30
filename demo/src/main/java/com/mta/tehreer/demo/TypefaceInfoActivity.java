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
import com.mta.tehreer.opentype.NameTable;
import com.mta.tehreer.widget.TLabel;

import java.util.ArrayList;
import java.util.List;

public class TypefaceInfoActivity extends AppCompatActivity {

    private static final int WINDOWS_PLATFORM = 3;

    private static final int COPYRIGHT = 0;
    private static final int FONT_FAMILY = 1;
    private static final int FONT_SUBFAMILY = 2;
    private static final int UNIQUE_ID = 3;
    private static final int FULL_NAME = 4;
    private static final int VERSION = 5;
    private static final int POST_SCRIPT_NAME = 6;
    private static final int TRADEMARK = 7;
    private static final int MANUFACTURER = 8;
    private static final int DESIGNER = 9;
    private static final int DESCRIPTION = 10;
    private static final int VENDOR_URL = 11;
    private static final int DESIGNER_URL = 12;
    private static final int LICENSE = 13;
    private static final int LICENSE_URL = 14;
    private static final int TYPOGRAPHIC_FAMILY = 16;
    private static final int TYPOGRAPHIC_SUBFAMILY = 17;
    private static final int MAC_FULL_NAME = 18;
    private static final int SAMPLE_TEXT = 19;
    private static final int POST_SCRIPT_CID_FIND_FONT_NAME = 20;
    private static final int WWS_FAMILY = 21;
    private static final int WWS_SUBFAMILY = 22;
    private static final int LIGHT_BACKGROUND_PALETTE = 23;
    private static final int DARK_BACKGROUND_PALETTE = 24;
    private static final int VARIATIONS_POST_SCRIPT_NAME_PREFIX = 25;

    private int mTypefaceTag;
    private Typeface mTypeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typeface_info);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final DemoApplication demoApplication = (DemoApplication) getApplication();
        ArrayAdapter<String> typefaceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, demoApplication.getTypefaceNames());
        typefaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner typefaceSpinner = (Spinner) findViewById(R.id.spinner_typeface);
        typefaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                loadTypeface(demoApplication.getTypefaceTag(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        typefaceSpinner.setAdapter(typefaceAdapter);
        typefaceSpinner.setSelection(0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private List<String> getNames(NameTable nameTable, int nameId, int platformId) {
        List<String> nameList = new ArrayList<>();
        int recordCount = nameTable.recordCount();

        for (int i = 0; i < recordCount; i++) {
            NameTable.Record record = nameTable.recordAt(i);
            if (record.nameId == nameId && record.platformId == platformId) {
                String nameString = record.string();
                if (nameString != null) {
                    nameList.add(nameString);
                }
            }
        }

        return nameList;
    }

    private void configureName(int layoutResId, NameTable nameTable, int nameId) {
        LinearLayout nameLayout = (LinearLayout) findViewById(layoutResId);
        List<String> nameList = getNames(nameTable, nameId, WINDOWS_PLATFORM);

        if (nameList.size() > 0) {
            StringBuilder nameBuilder = new StringBuilder();
            for (String name : nameList) {
                nameBuilder.append(name);
                nameBuilder.append('\n');
            }
            nameBuilder.deleteCharAt(nameBuilder.length() - 1);

            View nameView = nameLayout.getChildAt(1);
            if (nameView instanceof TextView) {
                TextView textView = (TextView) nameView;
                textView.setText(nameBuilder.toString());
            } else {
                TLabel label = (TLabel) nameView;
                label.setText(nameBuilder.toString());
                label.setTypeface(mTypeface);
            }

            nameLayout.setVisibility(View.VISIBLE);
        } else {
            nameLayout.setVisibility(View.GONE);
        }
    }

    private void loadTypeface(int tag) {
        if (tag != mTypefaceTag) {
            mTypefaceTag = tag;
            mTypeface = TypefaceManager.getDefaultManager().getTypeface(tag);

            NameTable nameTable = new NameTable(mTypeface);
            configureName(R.id.layout_copyright, nameTable, COPYRIGHT);
            configureName(R.id.layout_font_family, nameTable, FONT_FAMILY);
            configureName(R.id.layout_font_subfamily, nameTable, FONT_SUBFAMILY);
            configureName(R.id.layout_unique_id, nameTable, UNIQUE_ID);
            configureName(R.id.layout_full_name, nameTable, FULL_NAME);
            configureName(R.id.layout_version, nameTable, VERSION);
            configureName(R.id.layout_postscript_name, nameTable, POST_SCRIPT_NAME);
            configureName(R.id.layout_trademark, nameTable, TRADEMARK);
            configureName(R.id.layout_manufacturer, nameTable, MANUFACTURER);
            configureName(R.id.layout_designer, nameTable, DESIGNER);
            configureName(R.id.layout_description, nameTable, DESCRIPTION);
            configureName(R.id.layout_vendor_url, nameTable, VENDOR_URL);
            configureName(R.id.layout_designer_url, nameTable, DESIGNER_URL);
            configureName(R.id.layout_license, nameTable, LICENSE);
            configureName(R.id.layout_license_url, nameTable, LICENSE_URL);
            configureName(R.id.layout_typographic_family, nameTable, TYPOGRAPHIC_FAMILY);
            configureName(R.id.layout_typographic_subfamily, nameTable, TYPOGRAPHIC_SUBFAMILY);
            configureName(R.id.layout_mac_full_name, nameTable, MAC_FULL_NAME);
            configureName(R.id.layout_sample_text, nameTable, SAMPLE_TEXT);
            configureName(R.id.layout_postscript_cid_findfont_name, nameTable, POST_SCRIPT_CID_FIND_FONT_NAME);
            configureName(R.id.layout_wws_family, nameTable, WWS_FAMILY);
            configureName(R.id.layout_wws_subfamily, nameTable, WWS_SUBFAMILY);
            configureName(R.id.layout_light_background_palette, nameTable, LIGHT_BACKGROUND_PALETTE);
            configureName(R.id.layout_dark_background_palette, nameTable, DARK_BACKGROUND_PALETTE);
            configureName(R.id.layout_variations_postscript_name_prefix, nameTable, VARIATIONS_POST_SCRIPT_NAME_PREFIX);

            ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view_font_info);
            scrollView.invalidate();
            scrollView.scrollTo(0, 0);
        }
    }
}
