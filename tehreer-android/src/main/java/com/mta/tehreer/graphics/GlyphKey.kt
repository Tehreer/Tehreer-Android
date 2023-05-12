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

internal abstract class GlyphKey {
    var typeface: Typeface? = null
    var pixelWidth = 0  // 26.6 fixed-point value.
    var pixelHeight = 0 // 26.6 fixed-point value.
    var skewX = 0       // 16.16 fixed-point value.

    abstract fun copy(): GlyphKey

    protected fun set(key: GlyphKey) {
        typeface = key.typeface
        pixelWidth = key.pixelWidth
        pixelHeight = key.pixelHeight
        skewX = key.skewX
    }

    override fun equals(other: Any?): Boolean {
        if (other !is GlyphKey) return false

        if (typeface != other.typeface) return false
        if (pixelWidth != other.pixelWidth) return false
        if (pixelHeight != other.pixelHeight) return false
        if (skewX != other.skewX) return false

        return true
    }

    override fun hashCode(): Int {
        var result = typeface?.hashCode() ?: 0
        result = 31 * result + pixelWidth
        result = 31 * result + pixelHeight
        result = 31 * result + skewX

        return result
    }

    class Data : GlyphKey() {
        override fun copy(): Data {
            val key = Data()
            key.set(this)

            return key
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Data) return false
            if (!super.equals(other)) return false

            return true
        }
    }

    class Color : GlyphKey() {
        var foregroundColor = 0

        fun set(key: Data) {
            super.set(key)
        }

        override fun copy(): Color {
            val key = Color()
            key.set(this)
            key.foregroundColor = foregroundColor

            return key
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Color) return false
            if (!super.equals(other)) return false

            if (foregroundColor != other.foregroundColor) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + foregroundColor

            return result
        }
    }

    class Stroke : GlyphKey() {
        var lineRadius = 0
        var lineCap = 0
        var lineJoin = 0
        var miterLimit = 0

        fun set(key: Data) {
            super.set(key)
        }

        override fun copy(): Stroke {
            val key = Stroke()
            key.set(this)
            key.lineRadius = lineRadius
            key.lineCap = lineCap
            key.lineJoin = lineJoin
            key.miterLimit = miterLimit

            return key
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Stroke) return false
            if (!super.equals(other)) return false

            if (lineRadius != other.lineRadius) return false
            if (lineCap != other.lineCap) return false
            if (lineJoin != other.lineJoin) return false
            if (miterLimit != other.miterLimit) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + lineRadius
            result = 31 * result + lineCap
            result = 31 * result + lineJoin
            result = 31 * result + miterLimit

            return result
        }
    }
}
