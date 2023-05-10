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

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mta.tehreer.sfnt.tables.NameTable
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.widget.TLabel
import java.lang.StringBuilder

class TypefaceInfoActivity : AppCompatActivity() {
    private var selectedTypeface: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_typeface_info)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

    private fun getNames(nameTable: NameTable, nameId: Int, platformId: Int): List<String> {
        val nameList = mutableListOf<String>()
        val recordCount = nameTable.recordCount()

        for (i in 0 until recordCount) {
            val record = nameTable.recordAt(i)
            if (record.nameId == nameId && record.platformId == platformId) {
                val nameString = record.string()
                if (nameString != null) {
                    nameList.add(nameString)
                }
            }
        }

        return nameList
    }

    private fun configureName(layoutResId: Int, nameTable: NameTable, nameId: Int) {
        val nameLayout = findViewById<LinearLayout>(layoutResId)
        val nameList = getNames(nameTable, nameId, WINDOWS_PLATFORM)

        if (nameList.isNotEmpty()) {
            val nameBuilder = StringBuilder()
            for (name in nameList) {
                nameBuilder.append(name)
                nameBuilder.append('\n')
            }
            nameBuilder.deleteCharAt(nameBuilder.length - 1)

            val nameView = nameLayout.getChildAt(1)
            if (nameView is TextView) {
                nameView.text = nameBuilder.toString()
            } else {
                val label = nameView as TLabel
                label.text = nameBuilder.toString()
                label.typeface = selectedTypeface
            }

            nameLayout.visibility = View.VISIBLE
        } else {
            nameLayout.visibility = View.GONE
        }
    }

    private fun loadTypeface(typeface: Typeface) {
        if (typeface !== selectedTypeface) {
            selectedTypeface = typeface

            val nameTable = NameTable(selectedTypeface!!)
            configureName(R.id.layout_copyright, nameTable, COPYRIGHT)
            configureName(R.id.layout_font_family, nameTable, FONT_FAMILY)
            configureName(R.id.layout_font_subfamily, nameTable, FONT_SUBFAMILY)
            configureName(R.id.layout_unique_id, nameTable, UNIQUE_ID)
            configureName(R.id.layout_full_name, nameTable, FULL_NAME)
            configureName(R.id.layout_version, nameTable, VERSION)
            configureName(R.id.layout_postscript_name, nameTable, POST_SCRIPT_NAME)
            configureName(R.id.layout_trademark, nameTable, TRADEMARK)
            configureName(R.id.layout_manufacturer, nameTable, MANUFACTURER)
            configureName(R.id.layout_designer, nameTable, DESIGNER)
            configureName(R.id.layout_description, nameTable, DESCRIPTION)
            configureName(R.id.layout_vendor_url, nameTable, VENDOR_URL)
            configureName(R.id.layout_designer_url, nameTable, DESIGNER_URL)
            configureName(R.id.layout_license, nameTable, LICENSE)
            configureName(R.id.layout_license_url, nameTable, LICENSE_URL)
            configureName(R.id.layout_typographic_family, nameTable, TYPOGRAPHIC_FAMILY)
            configureName(R.id.layout_typographic_subfamily, nameTable, TYPOGRAPHIC_SUBFAMILY)
            configureName(R.id.layout_mac_full_name, nameTable, MAC_FULL_NAME)
            configureName(R.id.layout_sample_text, nameTable, SAMPLE_TEXT)
            configureName(R.id.layout_postscript_cid_findfont_name, nameTable, POST_SCRIPT_CID_FIND_FONT_NAME)
            configureName(R.id.layout_wws_family, nameTable, WWS_FAMILY)
            configureName(R.id.layout_wws_subfamily, nameTable, WWS_SUBFAMILY)
            configureName(R.id.layout_light_background_palette, nameTable, LIGHT_BACKGROUND_PALETTE)
            configureName(R.id.layout_dark_background_palette, nameTable, DARK_BACKGROUND_PALETTE)
            configureName(R.id.layout_variations_postscript_name_prefix, nameTable, VARIATIONS_POST_SCRIPT_NAME_PREFIX)

            val scrollView = findViewById<ScrollView>(R.id.scroll_view_font_info)
            scrollView.invalidate()
            scrollView.scrollTo(0, 0)
        }
    }

    companion object {
        private const val WINDOWS_PLATFORM = 3
        private const val COPYRIGHT = 0
        private const val FONT_FAMILY = 1
        private const val FONT_SUBFAMILY = 2
        private const val UNIQUE_ID = 3
        private const val FULL_NAME = 4
        private const val VERSION = 5
        private const val POST_SCRIPT_NAME = 6
        private const val TRADEMARK = 7
        private const val MANUFACTURER = 8
        private const val DESIGNER = 9
        private const val DESCRIPTION = 10
        private const val VENDOR_URL = 11
        private const val DESIGNER_URL = 12
        private const val LICENSE = 13
        private const val LICENSE_URL = 14
        private const val TYPOGRAPHIC_FAMILY = 16
        private const val TYPOGRAPHIC_SUBFAMILY = 17
        private const val MAC_FULL_NAME = 18
        private const val SAMPLE_TEXT = 19
        private const val POST_SCRIPT_CID_FIND_FONT_NAME = 20
        private const val WWS_FAMILY = 21
        private const val WWS_SUBFAMILY = 22
        private const val LIGHT_BACKGROUND_PALETTE = 23
        private const val DARK_BACKGROUND_PALETTE = 24
        private const val VARIATIONS_POST_SCRIPT_NAME_PREFIX = 25
    }
}
