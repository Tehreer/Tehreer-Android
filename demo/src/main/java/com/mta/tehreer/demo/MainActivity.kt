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

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bidiAlgorithmButton = findViewById<Button>(R.id.button_bidi_algorithm)
        bidiAlgorithmButton.setOnClickListener {
            val intent = Intent(this@MainActivity, BidiAlgorithmActivity::class.java)
            startActivity(intent)
        }

        val typefaceInfoButton = findViewById<Button>(R.id.button_typeface_info)
        typefaceInfoButton.setOnClickListener {
            val intent = Intent(this@MainActivity, TypefaceInfoActivity::class.java)
            startActivity(intent)
        }

        val typefaceGlyphsButton = findViewById<Button>(R.id.button_typeface_glyphs)
        typefaceGlyphsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, TypefaceGlyphsActivity::class.java)
            startActivity(intent)
        }

        val opentypeShapingButton = findViewById<Button>(R.id.button_opentype_shaping)
        opentypeShapingButton.setOnClickListener {
            val intent = Intent(this@MainActivity, OpenTypeShapingActivity::class.java)
            startActivity(intent)
        }

        val labelWidgetButton = findViewById<Button>(R.id.button_label_widget)
        labelWidgetButton.setOnClickListener {
            val intent = Intent(this@MainActivity, LabelWidgetActivity::class.java)
            startActivity(intent)
        }

        val textViewWidgetButton = findViewById<Button>(R.id.button_text_view_widget)
        textViewWidgetButton.setOnClickListener {
            val intent = Intent(this@MainActivity, TextViewWidgetActivity::class.java)
            startActivity(intent)
        }

        val variableFontsButton = findViewById<Button>(R.id.button_variable_fonts)
        variableFontsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, VariableFontsActivity::class.java)
            startActivity(intent)
        }

        val openSourceLicensesButton = findViewById<Button>(R.id.button_open_source_licenses)
        openSourceLicensesButton.setOnClickListener {
            val intent = Intent(this@MainActivity, OpenSourceLicensesActivity::class.java)
            startActivity(intent)
        }
    }
}
