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

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.mta.tehreer.widget.TTextView
import com.mta.tehreer.graphics.TypefaceManager
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.lang.StringBuilder

class TextViewWidgetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_view_widget)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val textView = findViewById<TTextView>(R.id.text_view)
        textView.setGravity(Gravity.CENTER_HORIZONTAL)
        textView.typeface = TypefaceManager.getTypeface(R.id.typeface_noorehuda)
        textView.spanned = parseSurah()
        textView.lineHeightMultiplier = 0.80f
        textView.isJustificationEnabled = true

        val justificationLevelBar = findViewById<SeekBar>(R.id.seek_bar_justification_level)
        justificationLevelBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                textView.justificationLevel = ((i / 4.0f) * 0.35f) + 0.65f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) { }
            override fun onStopTrackingTouch(seekBar: SeekBar) { }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun parseSurah(): Spanned? {
        try {
            val stream = assets.open("AlKahf.json")
            val buffer = CharArray(1024)
            val out = StringBuilder()
            val `in`: Reader = InputStreamReader(stream)
            var length: Int

            while (`in`.read(buffer, 0, buffer.size).also { length = it } > 0) {
                out.append(buffer, 0, length)
            }

            val jsonString = out.toString()
            val surah = SpannableStringBuilder()

            val ayahsJson = JSONArray(jsonString)
            val ayahCount = ayahsJson.length()

            for (i in 0 until ayahCount) {
                val ayahJson = ayahsJson.getJSONObject(i)
                val ayahText = ayahJson.getString("text")
                val attrsJson = ayahJson.getJSONArray("attributes")
                val attrCount = attrsJson.length()

                val offset = surah.length
                surah.append(ayahText)

                for (j in 0 until attrCount) {
                    val attrJson = attrsJson.getJSONObject(j)
                    val start = attrJson.getInt("start")
                    val end = attrJson.getInt("end")
                    val colorString = attrJson.getString("color")
                    val color = Color.parseColor("#$colorString")

                    surah.setSpan(
                        ForegroundColorSpan(color),
                        start + offset,
                        end + offset,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                surah.append(if (i == 0) "\n" else "  ")
            }

            return surah
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return null
    }
}
