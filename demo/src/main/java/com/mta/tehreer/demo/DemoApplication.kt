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

import android.app.Application
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.graphics.TypefaceManager
import java.io.*
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.Throws

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        registerTypeface("TajNastaleeq.ttf", R.id.typeface_taj_nastaleeq)
        registerTypeface("MehrNastaliq.ttf", R.id.typeface_mehr_nastaliq)
        registerTypeface("NafeesWeb.ttf", R.id.typeface_nafees_web)
        registerTypeface("Noorehuda.ttf", R.id.typeface_noorehuda)
    }

    private fun registerTypeface(fileName: String, tag: Int) {
        // It is better to copy the typeface into sdcard for performance reasons.
        try {
            val file = copyAsset(fileName)
            val typeface = Typeface(file)
            TypefaceManager.registerTypeface(typeface, tag)
        } catch (e: Exception) {
            throw RuntimeException("Unable to register typeface \"$fileName\"")
        }
    }

    @Throws(IOException::class)
    private fun copyAsset(fileName: String): File {
        val path = filesDir.absolutePath + File.separator + fileName
        val file = File(path)

        if (!file.exists()) {
            var `in`: InputStream? = null
            var out: OutputStream? = null

            try {
                `in` = assets.open(fileName)
                out = FileOutputStream(path)

                val buffer = ByteArray(1024)
                var length: Int

                while (`in`.read(buffer).also { length = it } > 0) {
                    out.write(buffer, 0, length)
                }
            } finally {
                closeSilently(`in`)
                closeSilently(out)
            }
        }

        return File(path)
    }

    private fun closeSilently(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (ignored: IOException) { }
    }
}
