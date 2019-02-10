/*
 * Copyright (C) 2019 Muhammad Tayyab Akram
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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.mta.tehreer.font.FontFile;
import com.mta.tehreer.font.VariationAxis;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.widget.TLabel;

import java.util.Collections;
import java.util.List;

public class VariableFontsActivity extends AppCompatActivity {
    private Spinner mVariableInstanceSpinner;
    private TLabel mPreviewLabel;

    private FontFile mFontFile;
    private Typeface mTypeface;
    private float[] mVariationCoordinates;

    private AxisAdapter mAxisAdapter = new AxisAdapter();
    private List<VariationAxis> mVariationAxes = Collections.emptyList();

    private static class InstanceAdapter extends ArrayAdapter<Typeface> {
        public InstanceAdapter(Context context, FontFile fontFile) {
            super(context, android.R.layout.simple_spinner_item, fontFile.getTypefaces());
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        public String getName(int position) {
            Typeface typeface = getItem(position);
            return typeface.getStyleName();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = (TextView) super.getView(position, convertView, parent);
            textView.setText(getName(position));

            return textView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
            textView.setText(getName(position));

            return textView;
        }
    }

    private class AxisAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mVariationAxes.size();
        }

        @Override
        public VariationAxis getItem(int i) {
            return mVariationAxes.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.item_variation_axis, parent, false);
            }

            final TextView axisName = convertView.findViewById(R.id.text_view_axis_name);
            final TextView minValue = convertView.findViewById(R.id.text_view_min_value);
            final SeekBar coordinateBar = convertView.findViewById(R.id.seek_bar_coordinate);
            final TextView maxValue = convertView.findViewById(R.id.text_view_max_value);

            VariationAxis variationAxis = getItem(position);
            axisName.setText(variationAxis.name());
            minValue.setText(String.valueOf(variationAxis.minValue()));
            maxValue.setText(String.valueOf(variationAxis.maxValue()));

            coordinateBar.setOnSeekBarChangeListener(null);
            coordinateBar.setMax((int) ((variationAxis.maxValue() - variationAxis.minValue()) * 10));
            coordinateBar.setProgress((int) ((mVariationCoordinates[position] - mVariationAxes.get(position).minValue()) * 10));
            coordinateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    float newValue = mVariationAxes.get(position).minValue() + (i / 10.0f);
                    setupCoordinate(position, newValue);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_variable_fonts);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mVariableInstanceSpinner = findViewById(R.id.spinner_variable_instance);
        mVariableInstanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                InstanceAdapter instanceAdapter = (InstanceAdapter) adapterView.getAdapter();
                setupTypeface(instanceAdapter.getItem(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        Spinner variableFontSpinner = findViewById(R.id.spinner_variable_font);
        variableFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                VariableFontAdapter fontAdapter = (VariableFontAdapter) adapterView.getAdapter();
                setupFont(fontAdapter.getFontFile(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });
        variableFontSpinner.setAdapter(new VariableFontAdapter(this));
        variableFontSpinner.setSelection(0);

        ListView listViewAxis = findViewById(R.id.list_view_axis);
        listViewAxis.setAdapter(mAxisAdapter);

        mPreviewLabel = findViewById(R.id.label_preview);
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    private void setupFont(FontFile fontFile) {
        mFontFile = fontFile;
        mTypeface = fontFile.getTypefaces().get(0);
        mVariationAxes = mTypeface.getVariationAxes();
        mVariationCoordinates = mTypeface.getVariationCoordinates();
        mAxisAdapter.notifyDataSetChanged();
        mVariableInstanceSpinner.setAdapter(new InstanceAdapter(this, fontFile));
        mPreviewLabel.setTypeface(mTypeface);
    }

    private void setupTypeface(Typeface typeface) {
        mTypeface = typeface;
        mVariationCoordinates = mTypeface.getVariationCoordinates();
        mAxisAdapter.notifyDataSetChanged();
        mPreviewLabel.setTypeface(mTypeface);
    }

    private void setupCoordinate(int axisIndex, float coordinate) {
        mVariationCoordinates[axisIndex] = coordinate;
        setupTypeface(mTypeface.getVariationInstance(mVariationCoordinates));
    }
}
