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
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ReplacementSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mta.tehreer.collections.IntList
import com.mta.tehreer.collections.PointList
import com.mta.tehreer.collections.FloatList
import com.mta.tehreer.sfnt.ShapingResult
import com.mta.tehreer.sfnt.ShapingEngine
import com.mta.tehreer.graphics.TypefaceManager
import com.mta.tehreer.graphics.Renderer

class OpenTypeInfoActivity : AppCompatActivity() {
    private class ClusterHolder(layout: View) {
        val charDetail: ViewGroup = layout.findViewById(R.id.layout_char_detail)
        val glyphDetail: ViewGroup = layout.findViewById(R.id.layout_glyph_detail)
    }

    private class CharHolder(layout: View) {
        val character: TextView = layout.findViewById(R.id.text_view_character)
    }

    private class GlyphHolder(layout: View) {
        val glyphId: TextView = layout.findViewById(R.id.text_view_glyph_id)
        val offset: TextView = layout.findViewById(R.id.text_view_offset)
        val advance: TextView = layout.findViewById(R.id.text_view_advance)
    }

    private class GlyphSpan(renderer: Renderer, glyphId: Int) : ReplacementSpan() {
        val renderer: Renderer
        val glyphId: IntList
        val glyphX: Float
        val spanWidth: Int

        init {
            val bbox = renderer.computeBoundingBox(glyphId)

            this.renderer = renderer
            this.glyphId = IntList.of(glyphId)
            this.glyphX = -bbox.left + PADDING / 2.0f
            this.spanWidth = (bbox.width() + 0.5f).toInt() + PADDING
        }

        override fun getSize(
            paint: Paint,
            text: CharSequence, start: Int, end: Int,
            fontMetrics: Paint.FontMetricsInt?
        ): Int {
            return spanWidth
        }

        override fun draw(
            canvas: Canvas,
            text: CharSequence, start: Int, end: Int,
            x: Float, top: Int, y: Int, bottom: Int, paint: Paint
        ) {
            val transX = (x + glyphX + 0.5f).toInt()
            val transY = bottom - paint.fontMetricsInt.descent

            canvas.translate(transX.toFloat(), transY.toFloat())
            renderer.drawGlyphs(canvas, glyphId, OFFSET, ADVANCE)
            canvas.translate(-transX.toFloat(), -transY.toFloat())
        }

        companion object {
            const val PADDING = 4

            val OFFSET = PointList.of(0f, 0f)
            val ADVANCE = FloatList.of(0f)
        }
    }

    private class ClusterAdapter(
        val context: Context,
        val renderer: Renderer,
        val sourceText: String,
        val shapingResult: ShapingResult,
        val clusterInitials: IntList
    ) : BaseAdapter() {
        val glyphIds = shapingResult.glyphIds
        val glyphOffsets = shapingResult.glyphOffsets
        val glyphAdvances = shapingResult.glyphAdvances
        val clusterMap = shapingResult.clusterMap
        val dp = (context.resources.displayMetrics.density + 0.5f).toInt()

        override fun getCount(): Int {
            return clusterInitials.size() - 1
        }

        override fun getItem(i: Int): Any {
            return i
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View? {
            var mainView = convertView
            val clusterHolder: ClusterHolder
            if (mainView == null) {
                val inflater = LayoutInflater.from(context)
                mainView = inflater.inflate(R.layout.item_cluster_detail, parent, false)

                clusterHolder = ClusterHolder(mainView)
                mainView.tag = clusterHolder

                val firstChar = clusterHolder.charDetail.getChildAt(0)
                firstChar.tag = CharHolder(firstChar)

                val firstGlyph = clusterHolder.glyphDetail.getChildAt(0)
                firstGlyph.tag = GlyphHolder(firstGlyph)
            } else {
                clusterHolder = mainView.tag as ClusterHolder
            }

            // Find out character range of this cluster.
            val charStart = clusterInitials[i]
            val charEnd = clusterInitials[i + 1]
            val charCount = charEnd - charStart

            // Find out glyph range of this cluster.
            val glyphStart = clusterMap[charStart]
            val glyphEnd = if (charEnd < clusterMap.size()) clusterMap[charEnd] else glyphIds.size()
            val glyphCount = glyphEnd - glyphStart

            // Setup layouts for all characters in this cluster.
            for (j in 0 until charCount) {
                val layout: View

                if (clusterHolder.charDetail.childCount <= j) {
                    val inflater = LayoutInflater.from(context)
                    layout = inflater.inflate(R.layout.item_char_detail, clusterHolder.charDetail, false)
                    layout.tag = CharHolder(layout)

                    clusterHolder.charDetail.addView(layout)
                } else {
                    layout = clusterHolder.charDetail.getChildAt(j)
                }

                val index = charStart + j
                val character = sourceText[index]

                val charHolder = layout.tag as CharHolder
                charHolder.character.text = String.format("\u2066%04X (%c)", character.code, character)

                layout.setPadding(0, 0, 0, dp)
                layout.visibility = View.VISIBLE
            }
            // Hide additional layouts.
            for (j in charCount until clusterHolder.charDetail.childCount) {
                clusterHolder.charDetail.getChildAt(j).visibility = View.GONE
            }
            // Hide last separator, if needed.
            if (charCount >= glyphCount) {
                clusterHolder.charDetail.getChildAt(charCount - 1).setPadding(0, 0, 0, 0)
            }

            // Setup layouts for all glyphs in this cluster.
            for (j in 0 until glyphCount) {
                val layout: View

                if (clusterHolder.glyphDetail.childCount <= j) {
                    val inflater = LayoutInflater.from(context)
                    layout = inflater.inflate(R.layout.item_glyph_detail, clusterHolder.glyphDetail, false)
                    layout.tag = GlyphHolder(layout)

                    clusterHolder.glyphDetail.addView(layout)
                } else {
                    layout = clusterHolder.glyphDetail.getChildAt(j)
                }

                val index = glyphStart + j
                val glyphId = glyphIds[index]
                val xOffset = (glyphOffsets.getX(index) + 0.5f).toInt()
                val yOffset = (glyphOffsets.getY(index) + 0.5f).toInt()
                val advance = (glyphAdvances[index] + 0.5f).toInt()

                val glyphString = String.format("%04X (_)", glyphId)
                val glyphSpan = GlyphSpan(renderer, glyphId)
                val glyphSpannable = SpannableString(glyphString)
                glyphSpannable.setSpan(glyphSpan, 6, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                val glyphHolder = layout.tag as GlyphHolder
                glyphHolder.glyphId.text = glyphSpannable
                glyphHolder.offset.text = "($xOffset, $yOffset)"
                glyphHolder.advance.text = advance.toString()

                layout.setPadding(0, 0, 0, dp)
                layout.visibility = View.VISIBLE
            }
            // Hide additional layouts.
            for (j in glyphCount until clusterHolder.glyphDetail.childCount) {
                clusterHolder.glyphDetail.getChildAt(j).visibility = View.GONE
            }
            // Hide last separator, if needed.
            if (glyphCount >= charCount) {
                clusterHolder.glyphDetail.getChildAt(glyphCount - 1).setPadding(0, 0, 0, 0)
            }

            return mainView
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opentype_info)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val typefaceName = intent.getStringExtra(TYPEFACE_NAME).orEmpty()
        val typeSize = intent.getIntExtra(TYPE_SIZE, 0)
        val scriptTag = intent.getIntExtra(SCRIPT_TAG, 0)
        val languageTag = intent.getIntExtra(LANGUAGE_TAG, 0)
        val sourceText = intent.getCharSequenceExtra(SOURCE_TEXT).toString()
        val writingDirection = ShapingEngine.getScriptDirection(scriptTag)

        val typeface = TypefaceManager.getTypefaceByName(typefaceName)
        val displayMetrics = resources.displayMetrics
        val displaySize = 18.0f * displayMetrics.scaledDensity
        val sizeScale = displaySize / typeSize

        val renderer = Renderer()
        renderer.typeface = typeface
        renderer.typeSize = typeSize.toFloat()
        renderer.scaleX = sizeScale
        renderer.scaleY = sizeScale

        val shapingEngine = ShapingEngine.finalizable(ShapingEngine())
        shapingEngine.typeface = typeface
        shapingEngine.typeSize = typeSize.toFloat()
        shapingEngine.scriptTag = scriptTag
        shapingEngine.languageTag = languageTag
        shapingEngine.writingDirection = writingDirection

        val shapingResult = ShapingResult.finalizable(shapingEngine.shapeText(sourceText, 0, sourceText.length))
        val clusterMap = shapingResult.clusterMap
        val length = clusterMap.size()

        val initials = IntArray(length + 1)
        var cluster = -1
        var previous = -1

        for (i in 0 until length) {
            val value = clusterMap[i]
            if (value != previous) {
                initials[++cluster] = i
            }

            previous = value
        }
        initials[++cluster] = length

        val infoListView = findViewById<ListView>(R.id.list_view_info)
        infoListView.adapter = ClusterAdapter(
            this, renderer, sourceText, shapingResult,
            IntList.of(*initials).subList(0, cluster + 1)
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val TYPEFACE_NAME = "typeface_name"
        const val TYPE_SIZE = "type_size"
        const val SCRIPT_TAG = "script_tag"
        const val LANGUAGE_TAG = "language_tag"
        const val SOURCE_TEXT = "source_text"
    }
}
