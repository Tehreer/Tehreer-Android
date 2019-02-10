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
import android.content.res.AssetManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mta.tehreer.font.FontFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VariableFontAdapter extends ArrayAdapter<String> {
    private static final String VARIABLE_FONTS_PATH = "vfonts";

    private static List<String> getVariableFontFiles(AssetManager assetManager) {
        List<String> fontFiles = new ArrayList<>();

        try {
            String[] fileList = assetManager.list(VARIABLE_FONTS_PATH);
            fontFiles = Arrays.asList(fileList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fontFiles;
    }

    public VariableFontAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_item, getVariableFontFiles(context.getAssets()));
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    public FontFile getFontFile(int position) {
        FontFile fontFile = null;

        try {
            AssetManager assetManager = getContext().getAssets();
            InputStream assetStream = assetManager.open(VARIABLE_FONTS_PATH + '/' + getItem(position));
            fontFile = new FontFile(assetStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fontFile;
    }

    public String getName(int position) {
        String fileName = getItem(position);
        return fileName.substring(0, fileName.indexOf('.'));
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
