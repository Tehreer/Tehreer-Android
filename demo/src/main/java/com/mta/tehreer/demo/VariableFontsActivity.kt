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
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mta.tehreer.font.FontFile
import com.mta.tehreer.font.VariationAxis
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.widget.TLabel

class VariableFontsActivity : AppCompatActivity() {
    private lateinit var colorPaletteSpinner: Spinner
    private lateinit var variableInstanceSpinner: Spinner
    private lateinit var previewLabel: TLabel

    private var selectedTypeface: Typeface? = null
    private var variationCoordinates: FloatArray? = null

    private val axisAdapter = AxisAdapter()
    private var variationAxes: List<VariationAxis> = emptyList()

    private class InstanceAdapter(
        context: Context,
        fontFile: FontFile
    ) : ArrayAdapter<Typeface>(context, android.R.layout.simple_spinner_item, fontFile.typefaces) {
        init {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        fun getName(position: Int): String {
            val typeface = getItem(position)
            return typeface!!.styleName
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val textView = super.getView(position, convertView, parent) as TextView
            textView.text = getName(position)

            return textView
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val textView = super.getDropDownView(position, convertView, parent) as TextView
            textView.text = getName(position)

            return textView
        }
    }

    private inner class AxisAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return variationAxes.size
        }

        override fun getItem(i: Int): VariationAxis {
            return variationAxes[i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        private fun createAxisView(parent: ViewGroup?): View {
            val inflater = LayoutInflater.from(parent?.context)
            return inflater.inflate(R.layout.item_variation_axis, parent, false)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val mainView: View = convertView ?: createAxisView(parent)
            val axisName = mainView.findViewById<TextView>(R.id.text_view_axis_name)
            val minValue = mainView.findViewById<TextView>(R.id.text_view_min_value)
            val coordinateBar = mainView.findViewById<SeekBar>(R.id.seek_bar_coordinate)
            val maxValue = mainView.findViewById<TextView>(R.id.text_view_max_value)

            val variationAxis = getItem(position)
            axisName.text = variationAxis.name()
            minValue.text = variationAxis.minValue().toString()
            maxValue.text = variationAxis.maxValue().toString()

            coordinateBar.setOnSeekBarChangeListener(null)
            coordinateBar.max = ((variationAxis.maxValue() - variationAxis.minValue()) * 10).toInt()
            coordinateBar.progress = ((variationCoordinates!![position] - variationAxes[position].minValue()) * 10).toInt()
            coordinateBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    val newValue = variationAxes[position].minValue() + i / 10.0f
                    setupCoordinate(position, newValue)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) { }
                override fun onStopTrackingTouch(seekBar: SeekBar) { }
            })

            return mainView
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_variable_fonts)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        variableInstanceSpinner = findViewById(R.id.spinner_variable_instance)
        variableInstanceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val instanceAdapter = adapterView.adapter as InstanceAdapter
                setupTypeface(instanceAdapter.getItem(i)!!)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) { }
        }

        colorPaletteSpinner = findViewById(R.id.spinner_color_palette)
        colorPaletteSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val paletteAdapter = adapterView.adapter as ColorPaletteAdapter
                setupPalette(paletteAdapter.getItem(i).colors())
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) { }
        }

        val variableFontSpinner = findViewById<Spinner>(R.id.spinner_variable_font)
        variableFontSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                val fontAdapter = adapterView.adapter as VariableFontAdapter
                setupFont(fontAdapter.getFontFile(i)!!)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) { }
        }
        variableFontSpinner.adapter = VariableFontAdapter(this)
        variableFontSpinner.setSelection(0)

        val listViewAxis = findViewById<ListView>(R.id.list_view_axis)
        listViewAxis.adapter = axisAdapter

        previewLabel = findViewById(R.id.label_preview)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupFont(fontFile: FontFile) {
        selectedTypeface = fontFile.typefaces[0]
        variationAxes = selectedTypeface?.variationAxes.orEmpty()
        variationCoordinates = selectedTypeface?.variationCoordinates
        axisAdapter.notifyDataSetChanged()
        variableInstanceSpinner.adapter = InstanceAdapter(this, fontFile)
        previewLabel.typeface = selectedTypeface
    }

    private fun setupTypeface(typeface: Typeface) {
        val palettes = typeface.predefinedPalettes ?: emptyList()

        selectedTypeface = typeface
        colorPaletteSpinner.adapter = ColorPaletteAdapter(palettes)
        colorPaletteSpinner.visibility = if (palettes.isEmpty()) View.GONE else View.VISIBLE
        variationCoordinates = selectedTypeface?.variationCoordinates
        axisAdapter.notifyDataSetChanged()
        previewLabel.typeface = selectedTypeface
    }

    private fun setupCoordinate(axisIndex: Int, coordinate: Float) {
        variationCoordinates!![axisIndex] = coordinate
        selectedTypeface = selectedTypeface!!.getVariationInstance(variationCoordinates!!)
        previewLabel.typeface = selectedTypeface
    }

    private fun setupPalette(colors: IntArray) {
        selectedTypeface = selectedTypeface!!.getColorInstance(colors)
        previewLabel.typeface = selectedTypeface
    }
}
