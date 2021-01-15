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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.font.ColorPalette;

import java.util.List;

public class ColorPaletteAdapter extends BaseAdapter {
    private final List<ColorPalette> palettes;

    private static class PaletteDrawable extends Drawable {
        private final float boxSpacing;
        private final int[] colors;
        private final int length;
        private final Paint paint;

        public PaletteDrawable(@NonNull Context context, int[] colors, float length) {
            final float dp = context.getResources().getDisplayMetrics().density;

            this.boxSpacing = 8.0f * dp;
            this.colors = colors;
            this.length = (int) (length * dp);
            this.paint = new Paint();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            for (int i = 0; i < colors.length; i++) {
                final float left = (length + boxSpacing) * i;
                final float right = left + length;

                paint.setColor(colors[i]);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawRect(left, 0, right, length, paint);
            }
        }

        @Override
        public void setAlpha(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public int getIntrinsicWidth() {
            return (int) ((length + boxSpacing) * colors.length);
        }

        @Override
        public int getIntrinsicHeight() {
            return length;
        }
    }

    public ColorPaletteAdapter(List<ColorPalette> palettes) {
        this.palettes = palettes;
    }

    @Override
    public int getCount() {
        return palettes.size();
    }

    @Override
    public @NonNull ColorPalette getItem(int position) {
        return palettes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.item_color_palette, parent, false);
        }

        final FrameLayout parentLayout = (FrameLayout) convertView;
        final ImageView paletteImageView = parentLayout.findViewById(R.id.image_view_color_palette);

        final ColorPalette palette = getItem(position);
        final int[] colors = palette.colors();
        final Drawable drawable = new PaletteDrawable(parent.getContext(), colors, 24);

        paletteImageView.setImageDrawable(drawable);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.item_color_palette_dropdown, parent, false);
        }

        final FrameLayout parentLayout = (FrameLayout) convertView;
        final ImageView paletteImageView = parentLayout.findViewById(R.id.image_view_color_palette);

        final ColorPalette palette = getItem(position);
        final int[] colors = palette.colors();
        final Drawable drawable = new PaletteDrawable(parent.getContext(), colors, 28);

        paletteImageView.setImageDrawable(drawable);

        return convertView;
    }
}
