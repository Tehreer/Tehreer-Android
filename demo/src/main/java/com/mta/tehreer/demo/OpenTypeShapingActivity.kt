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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.sfnt.SfntTag

class OpenTypeShapingActivity : AppCompatActivity() {
    private lateinit var typeSizeField: EditText
    private lateinit var scriptTagField: EditText
    private lateinit var languageTagField: EditText
    private lateinit var textField: EditText

    private var selectedTypeface: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opentype_shaping)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val typefaceSpinner = findViewById<Spinner>(R.id.spinner_typeface)
        typefaceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                selectedTypeface = adapterView.adapter.getItem(i) as Typeface
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) { }
        }
        typefaceSpinner.adapter = TypefaceAdapter(this)
        typefaceSpinner.setSelection(0)

        typeSizeField = findViewById(R.id.field_type_size)
        scriptTagField = findViewById(R.id.field_script_tag)
        languageTagField = findViewById(R.id.field_language_tag)
        textField = findViewById(R.id.field_text)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_opentype_shaping, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_shape -> {
                shape()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun shape() {
        val scriptTag = scriptTagField.text.toString()
        try {
            SfntTag.make(scriptTag)
        } catch (ignored: IllegalArgumentException) {
            Toast.makeText(this, "Write a valid script tag!", Toast.LENGTH_LONG).show()
            return
        }

        val languageTag = languageTagField.text.toString()
        try {
            SfntTag.make(languageTag)
        } catch (ignored: IllegalArgumentException) {
            Toast.makeText(this, "Write a valid language tag!", Toast.LENGTH_LONG).show()
            return
        }

        val intent = Intent(this, OpenTypeInfoActivity::class.java)
        intent.putExtra(OpenTypeInfoActivity.TYPEFACE_NAME, selectedTypeface?.fullName)
        intent.putExtra(OpenTypeInfoActivity.TYPE_SIZE, typeSizeField.text.toString().toInt())
        intent.putExtra(OpenTypeInfoActivity.SCRIPT_TAG, SfntTag.make(scriptTag))
        intent.putExtra(OpenTypeInfoActivity.LANGUAGE_TAG, SfntTag.make(languageTag))
        intent.putExtra(OpenTypeInfoActivity.SOURCE_TEXT, textField.text)
        startActivity(intent)
    }
}
