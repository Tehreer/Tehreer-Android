/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.font

import android.content.res.AssetManager
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.internal.JniBridge
import java.io.File
import java.io.InputStream

/**
 * A `FontFile` object represents the file of a specific font format.
 */
class FontFile private constructor(private val nativeFontFile: Long) {
    private class Finalizer(private val nativeFontFile: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nRelease(nativeFontFile)
        }
    }

    private val finalizer = Finalizer(nativeFontFile)

    /**
     * Constructs a font file instance representing the specified asset. The data of the asset is
     * not copied to an in-memory buffer. Rather, it is directly read from a stream of the asset
     * when needed. So the typefaces obtained from resulting font file might be slower and should be
     * used with caution.
     *
     * @param assetManager The application's asset manager.
     * @param filePath The path of the font in the assets directory.
     *
     * @throws RuntimeException if an error occurred while initialization.
     */
    constructor(assetManager: AssetManager, filePath: String) : this(
        nCreateFromAsset(assetManager, filePath).also {
            if (it == 0L) {
                throw RuntimeException("Could not create typeface from specified asset")
            }
        }
    )

    /**
     * Constructs a font file instance representing the specified file path. The data of the font
     * is directly read from a stream of the file when needed.
     *
     * @param file The file describing the path of the font.
     *
     * @throws RuntimeException if an error occurred while initialization.
     */
    constructor(file: File) : this(
        nCreateFromPath(file.absolutePath).also {
            if (it == 0L) {
                throw RuntimeException("Could not create typeface from specified file")
            }
        }
    )

    /**
     * Constructs a font file instance from the specified input stream by copying its data into a
     * native memory buffer. It may take time to create the instance if the stream holds larger
     * data.
     *
     * @param stream The input stream that contains the data of the font.
     *
     * @throws RuntimeException if an error occurred while initialization.
     */
    constructor(stream: InputStream) : this(
        nCreateFromStream(stream).also {
            if (it == 0L) {
                throw RuntimeException("Could not create typeface from specified stream")
            }
        }
    )

    /**
     * Returns named typefaces of this font file.
     *
     * @return Named typefaces of this font file.
     */
    val typefaces: List<Typeface> by lazy {
        val allTypefaces = ArrayList<Typeface>()

        for (i in 0 until nGetFaceCount(nativeFontFile)) {
            val firstTypeface = Typeface(nCreateTypeface(nativeFontFile, i))
            val namedStyles = firstTypeface.namedStyles

            if (namedStyles.isNullOrEmpty()) {
                allTypefaces.add(firstTypeface)
            } else {
                for (namedStyle in namedStyles) {
                    allTypefaces.add(firstTypeface.getVariationInstance(namedStyle.coordinates())!!)
                }
            }
        }

        allTypefaces
    }

    private external fun nGetFaceCount(nativeFontFile: Long): Int

    private external fun nCreateTypeface(nativeFontFile: Long, faceIndex: Int): Long

    private companion object {
        init {
            JniBridge.loadLibrary()
        }

        @JvmStatic external fun nCreateFromAsset(assetManager: AssetManager, path: String): Long
        @JvmStatic external fun nCreateFromPath(path: String): Long
        @JvmStatic external fun nCreateFromStream(stream: InputStream): Long
        @JvmStatic external fun nRelease(nativeFontFile: Long)

    }
}
