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
import android.content.Intent
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.Typeface
import java.lang.UnsupportedOperationException

private class GlyphHolder(layout: View) {
    val glyphShape: ImageView = layout.findViewById(R.id.image_view_glyph_shape)
    val glyphId: TextView = layout.findViewById(R.id.text_view_glyph_id)
}

private class GlyphAdapter(
    val context: Context,
    val renderer: Renderer
) : BaseAdapter() {
    override fun getCount(): Int {
        return renderer.typeface.glyphCount
    }

    override fun getItem(i: Int): Any {
        return i
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View? {
        var mainView = convertView
        val holder: GlyphHolder

        if (mainView == null) {
            val inflater = LayoutInflater.from(context)
            mainView = inflater.inflate(R.layout.item_typeface_glyph, parent, false)

            holder = GlyphHolder(mainView)
            mainView.tag = holder
        } else {
            holder = mainView.tag as GlyphHolder
        }

        holder.glyphId.text = String.format("%04X", i)
        holder.glyphShape.setImageDrawable(GlyphDrawable(renderer, i))

        return mainView
    }
}

private class GlyphDrawable(
    renderer: Renderer,
    glyphId: Int
) : Drawable() {
    val renderer: Renderer
    val glyphId: IntList
    val glyphBounds: RectF
    val fontAscent: Float
    val fontDescent: Float

    init {
        val typeface = renderer.typeface
        val sizeByEm = renderer.typeSize / typeface.unitsPerEm

        this.renderer = renderer
        this.glyphId = IntList.of(glyphId)
        this.glyphBounds = renderer.computeBoundingBox(glyphId)
        this.fontAscent = typeface.ascent * sizeByEm
        this.fontDescent = typeface.descent * sizeByEm
    }

    override fun draw(canvas: Canvas) {
        val drawableBounds = bounds
        val fontHeight = fontAscent + fontDescent

        val left =
            (drawableBounds.left + (drawableBounds.width() - glyphBounds.width()) / 2.0f - glyphBounds.left + 0.5f).toInt()
        val top =
            (drawableBounds.top + (drawableBounds.height() - fontHeight) / 2.0f + fontAscent + 0.5f).toInt()

        canvas.translate(left.toFloat(), top.toFloat())
        renderer.drawGlyphs(canvas, glyphId, OFFSET, ADVANCE)
        canvas.translate(-left.toFloat(), -top.toFloat())
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

    companion object {
        val OFFSET = PointList.of(0f, 0f)
        val ADVANCE = FloatList.of(0f)
    }
}

class TypefaceGlyphsActivity : AppCompatActivity() {
    private lateinit var glyphsGridView: GridView
    private var selectedTypeface: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_typeface_glyphs)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        glyphsGridView = findViewById(R.id.grid_view_glyphs)
        glyphsGridView.setOnItemClickListener { _, _, i, _ ->
            displayGlyph(i)
        }

        val typefaceSpinner = findViewById<Spinner>(R.id.spinner_typeface)
        typefaceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                loadTypeface(adapterView.adapter.getItem(i) as Typeface)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) { }
        }
        typefaceSpinner.adapter = TypefaceAdapter(this)
        typefaceSpinner.setSelection(0)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun loadTypeface(typeface: Typeface) {
        if (typeface !== selectedTypeface) {
            selectedTypeface = typeface

            val density = resources.displayMetrics.density
            val typeSize = 28.0f * density

            val renderer = Renderer()
            renderer.typeface = typeface
            renderer.typeSize = typeSize

            glyphsGridView.adapter = GlyphAdapter(this, renderer)
        }
    }

    private fun displayGlyph(glyphId: Int) {
        selectedTypeface?.let {
            val intent = Intent(this, GlyphInfoActivity::class.java)
            intent.putExtra(GlyphInfoActivity.TYPEFACE_NAME, it.fullName)
            intent.putExtra(GlyphInfoActivity.GLYPH_ID, glyphId)

            startActivity(intent)
        }
    }
}
