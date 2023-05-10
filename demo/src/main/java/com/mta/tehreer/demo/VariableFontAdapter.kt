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

import android.R
import android.content.Context
import android.content.res.AssetManager
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.mta.tehreer.font.FontFile
import java.io.IOException

private const val VARIABLE_FONTS_PATH = "vfonts"

private fun getVariableFontFiles(assetManager: AssetManager): List<String> {
    var fontFiles: List<String> = listOf()

    try {
        val files = assetManager.list(VARIABLE_FONTS_PATH)
        fontFiles = files?.toList().orEmpty()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return fontFiles
}

class VariableFontAdapter(context: Context) : ArrayAdapter<String>(
    context,
    R.layout.simple_spinner_item,
    getVariableFontFiles(context.assets)
) {
    init {
        setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
    }

    fun getFontFile(position: Int): FontFile? {
        var fontFile: FontFile? = null

        try {
            val assetStream = context.assets.open(VARIABLE_FONTS_PATH + '/' + getItem(position))
            fontFile = FontFile(assetStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return fontFile
    }

    private fun getName(position: Int): String {
        val fileName = getItem(position).orEmpty()
        return fileName.substring(0, fileName.indexOf('.'))
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
