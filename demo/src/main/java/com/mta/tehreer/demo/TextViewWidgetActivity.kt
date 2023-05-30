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
import android.util.TypedValue
import android.view.Gravity
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.mta.tehreer.graphics.TypefaceManager
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader

private const val MIN_TEXT_SIZE = 20f
private const val MAX_TEXT_SIZE = 56f

class TextViewWidgetActivity : AppCompatActivity() {
    private lateinit var textView: QuranTextView
    private lateinit var textSizeBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_view_widget)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        textSizeBar = findViewById(R.id.seek_bar_text_size)
        textSizeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) { }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                clearAyahHighlighting()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                updateTextSize()
            }
        })

        textView = findViewById(R.id.text_view)
        textView.apply {
            updateTextSize()
            setGravity(Gravity.CENTER_HORIZONTAL)
            typeface = TypefaceManager.getTypeface(R.id.typeface_noorehuda)
            spanned = parseSurah()
            lineHeightMultiplier = 0.80f
            isJustificationEnabled = true
            separatorColor = Color.GRAY
            highlightingColor = resources.getColor(R.color.colorHighlight)
        }
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
                surah.setSpan(
                    QuranTextView.AyahSpan(),
                    offset, surah.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

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

    private fun spToPx(sp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

    private fun clearAyahHighlighting() {
        textView.clearAyahHighlighting()
    }

    private fun updateTextSize() {
        val ratio = (textSizeBar.progress.toFloat() / textSizeBar.max.toFloat())
        val multiplier = MAX_TEXT_SIZE - MIN_TEXT_SIZE
        val sp = (ratio * multiplier) + MIN_TEXT_SIZE

        textView.textSize = spToPx(sp)
    }
}
