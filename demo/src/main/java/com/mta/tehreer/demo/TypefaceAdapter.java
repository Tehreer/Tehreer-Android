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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;

import java.util.List;

public class TypefaceAdapter extends ArrayAdapter {

    private final List<Typeface> typefaces;

    public TypefaceAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_item);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        TypefaceManager typefaceManager = TypefaceManager.getDefaultManager();
        typefaces = typefaceManager.getAvailableTypefaces();
    }

    @Override
    public int getCount() {
        return typefaces.size();
    }

    @Override
    public Object getItem(int position) {
        return typefaces.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Typeface typeface = typefaces.get(position);
        TextView textView = (TextView) super.getView(position, convertView, parent);
        textView.setText(typeface.getFamilyName());

        return textView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        Typeface typeface = typefaces.get(position);
        TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
        textView.setText(typeface.getFamilyName());

        return textView;
    }
}
