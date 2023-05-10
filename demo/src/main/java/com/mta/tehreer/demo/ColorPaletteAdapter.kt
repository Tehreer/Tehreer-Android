/*
 * Copyright (C) 2023 Muhammad Tayyab Akram
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

package com.mta.tehreer.demo

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import com.mta.tehreer.font.ColorPalette
import java.lang.UnsupportedOperationException

private class PaletteDrawable(context: Context, colors: IntArray, length: Float) : Drawable() {
    private val boxSpacing: Float
    private val colors: IntArray
    private val length: Int
    private val paint: Paint

    init {
        val dp = context.resources.displayMetrics.density

        this.boxSpacing = 8.0f * dp
        this.colors = colors
        this.length = (length * dp).toInt()
        this.paint = Paint()
    }

    override fun draw(canvas: Canvas) {
        for (i in colors.indices) {
            val left = (length + boxSpacing) * i
            val right = left + length

            paint.color = colors[i]
            paint.style = Paint.Style.FILL

            canvas.drawRect(left, 0f, right, length.toFloat(), paint)
        }
    }

    override fun setAlpha(i: Int) {
        throw UnsupportedOperationException()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        throw UnsupportedOperationException()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int {
        return ((length + boxSpacing) * colors.size).toInt()
    }

    override fun getIntrinsicHeight(): Int {
        return length
    }
}

class ColorPaletteAdapter(private val palettes: List<ColorPalette>) : BaseAdapter() {
    override fun getCount(): Int {
        return palettes.size
    }

    override fun getItem(position: Int): ColorPalette {
        return palettes[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mainView = convertView
        if (mainView == null) {
            val inflater = LayoutInflater.from(parent.context)
            mainView = inflater.inflate(R.layout.item_color_palette, parent, false)
        }

        val parentLayout = mainView as FrameLayout
        val paletteImageView = parentLayout.findViewById<ImageView>(R.id.image_view_color_palette)

        val palette = getItem(position)
        val drawable = PaletteDrawable(parent.context, palette.colors(), 24f)

        paletteImageView.setImageDrawable(drawable)

        return mainView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var mainView = convertView
        if (mainView == null) {
            val inflater = LayoutInflater.from(parent.context)
            mainView = inflater.inflate(R.layout.item_color_palette_dropdown, parent, false)
        }

        val parentLayout = mainView as FrameLayout
        val paletteImageView = parentLayout.findViewById<ImageView>(R.id.image_view_color_palette)

        val palette = getItem(position)
        val drawable = PaletteDrawable(parent.context, palette.colors(), 28f)

        paletteImageView.setImageDrawable(drawable)

        return mainView
    }
}
