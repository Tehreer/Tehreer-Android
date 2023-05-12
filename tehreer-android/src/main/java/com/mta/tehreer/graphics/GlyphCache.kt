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

package com.mta.tehreer.graphics

import android.graphics.Bitmap
import android.graphics.Path
import androidx.annotation.GuardedBy
import com.mta.tehreer.internal.util.LruCache

private fun sizeOf(bitmap: Bitmap): Int {
    var size = bitmap.width * bitmap.height
    if (bitmap.config == Bitmap.Config.ARGB_8888) {
        size *= 4
    }

    return size
}

internal class GlyphCache(capacity: Int) : LruCache<Int>(capacity) {
    private class DataSegment(
        cache: LruCache<Int>,
        val rasterizer: GlyphRasterizer
    ) : Segment<Int>(cache) {
        override fun sizeOf(key: Int, value: Any?): Int {
            val glyphImage = (value as Glyph).image
            val size = glyphImage?.let { sizeOf(it.bitmap()) } ?: 0

            return size + ESTIMATED_OVERHEAD
        }

        companion object {
            private const val ESTIMATED_OVERHEAD =
                (GLYPH_IMAGE_OVERHEAD + GLYPH_OVERHEAD + NODE_OVERHEAD)
        }
    }

    private class ImageSegment(cache: LruCache<Int>) : Segment<Int>(cache) {
        override fun sizeOf(key: Int, value: Any?): Int {
            return sizeOf((value as GlyphImage).bitmap()) + ESTIMATED_OVERHEAD
        }

        companion object {
            private const val ESTIMATED_OVERHEAD = GLYPH_IMAGE_OVERHEAD + NODE_OVERHEAD
        }
    }

    private object Holder {
        val instance: GlyphCache

        init {
            val maxSize = (Runtime.getRuntime().maxMemory() / 8).toInt()
            instance = GlyphCache(maxSize)
        }
    }

    private val segments = HashMap<GlyphKey, Segment<Int>>()

    override fun clear() {
        super.clear()

        // Dispose all glyph rasterizers.
        for ((_, value) in segments) {
            if (value is DataSegment) {
                value.rasterizer.dispose()
            }
        }

        segments.clear()
    }

    @GuardedBy("this")
    private fun secureDataSegment(key: GlyphKey): DataSegment {
        var segment = segments[key] as DataSegment?
        if (segment == null) {
            segment = DataSegment(this, GlyphRasterizer(key))
            segments[key.copy()] = segment
        }

        return segment
    }

    @GuardedBy("this")
    private fun secureImageSegment(key: GlyphKey): ImageSegment {
        var segment = segments[key] as ImageSegment?
        if (segment == null) {
            segment = ImageSegment(this)
            segments[key.copy()] = segment
        }

        return segment
    }

    @GuardedBy("this")
    private fun secureGlyph(segment: DataSegment, glyphId: Int): Glyph {
        var glyph = segment[glyphId] as Glyph?
        if (glyph == null) {
            glyph = Glyph()
        }

        return glyph
    }

    private fun getColoredImage(
        key: GlyphKey.Color,
        rasterizer: GlyphRasterizer,
        glyphId: Int
    ): GlyphImage? {
        val segment: ImageSegment
        var coloredImage: GlyphImage?

        synchronized(this) {
            segment = secureImageSegment(key)
            coloredImage = segment[glyphId] as GlyphImage?
        }

        if (coloredImage == null) {
            coloredImage = rasterizer.getGlyphImage(glyphId, key.foregroundColor)

            if (coloredImage != null) {
                synchronized(this) {
                    segment.remove(glyphId)
                    segment.put(glyphId, coloredImage)
                }
            }
        }

        return coloredImage
    }

    fun getGlyphImage(attributes: GlyphAttributes, glyphId: Int): GlyphImage? {
        val segment: DataSegment
        val glyph: Glyph

        synchronized(this) {
            segment = secureDataSegment(attributes.dataKey())
            glyph = secureGlyph(segment, glyphId)
        }

        if (!glyph.isLoaded) {
            val glyphType = segment.rasterizer.getGlyphType(glyphId)
            var glyphImage: GlyphImage? = null

            if (glyphType != Glyph.TYPE_MIXED) {
                glyphImage = segment.rasterizer.getGlyphImage(glyphId)
            }

            synchronized(this) {
                if (!glyph.isLoaded) {
                    segment.remove(glyphId)

                    glyph.type = glyphType
                    glyph.image = glyphImage

                    segment.put(glyphId, glyph)
                }
            }
        }

        if (glyph.type == Glyph.TYPE_MIXED) {
            return getColoredImage(attributes.colorKey(), segment.rasterizer, glyphId)
        }

        return glyph.image
    }

    private fun getStrokeImage(
        key: GlyphKey.Stroke,
        rasterizer: GlyphRasterizer,
        outline: GlyphOutline,
        glyphId: Int
    ): GlyphImage? {
        val segment: ImageSegment
        var strokeImage: GlyphImage?

        synchronized(this) {
            segment = secureImageSegment(key)
            strokeImage = segment[glyphId] as GlyphImage?
        }

        if (strokeImage == null) {
            strokeImage = rasterizer.getStrokeImage(
                outline,
                key.lineRadius, key.lineCap, key.lineJoin, key.miterLimit
            )

            if (strokeImage != null) {
                synchronized(this) {
                    segment.remove(glyphId)
                    segment.put(glyphId, strokeImage!!)
                }
            }
        }

        return strokeImage
    }

    fun getStrokeImage(attributes: GlyphAttributes, glyphId: Int): GlyphImage? {
        val segment: DataSegment
        val glyph: Glyph

        synchronized(this) {
            segment = secureDataSegment(attributes.dataKey())
            glyph = secureGlyph(segment, glyphId)
        }

        var glyphOutline = glyph.outline
        if (glyphOutline == null) {
            glyphOutline = segment.rasterizer.getGlyphOutline(glyphId)

            synchronized(this) {
                if (glyph.outline == null) {
                    segment.remove(glyphId)
                    glyph.outline = glyphOutline
                    segment.put(glyphId, glyph)
                }
            }
        }

        if (glyphOutline != null) {
            return getStrokeImage(
                attributes.strokeKey(), segment.rasterizer,
                glyphOutline, glyphId
            )
        }

        return null
    }

    fun getGlyphPath(attributes: GlyphAttributes, glyphId: Int): Path {
        val segment: DataSegment
        val glyph: Glyph

        synchronized(this) {
            segment = secureDataSegment(attributes.dataKey())
            glyph = secureGlyph(segment, glyphId)
        }

        var glyphPath = glyph.path
        if (glyphPath == null) {
            glyphPath = segment.rasterizer.getGlyphPath(glyphId)

            synchronized(this) {
                if (glyph.path == null) {
                    segment.remove(glyphId)
                    glyph.path = glyphPath
                    segment.put(glyphId, glyph)
                }
            }
        }

        return glyphPath
    }

    companion object {
        //
        // GlyphImage:
        //  - 1 pointer for bitmap
        //  - 2 integers for left and right
        //
        // Size: (1 * 4) + (2 * 4) = 12
        //
        private const val GLYPH_IMAGE_OVERHEAD = 12

        //
        // Glyph:
        //  - 3 pointers for image, outline and path
        //  - 1 integer for type
        //
        // Size: (3 * 4) + (1 * 4) = 16
        //
        private const val GLYPH_OVERHEAD = 16

        @JvmStatic
        val instance: GlyphCache
            get() = Holder.instance
    }
}
