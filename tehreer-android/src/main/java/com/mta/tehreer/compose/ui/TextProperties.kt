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

package com.mta.tehreer.compose.ui

import android.text.Spanned
import android.text.SpannedString
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.graphics.RenderingStyle
import com.mta.tehreer.graphics.StrokeCap
import com.mta.tehreer.graphics.StrokeJoin
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.layout.BreakMode
import com.mta.tehreer.layout.FrameResolver
import com.mta.tehreer.layout.TextAlignment
import com.mta.tehreer.layout.TruncationPlace
import com.mta.tehreer.layout.Typesetter
import com.mta.tehreer.layout.style.TypeSizeSpan
import com.mta.tehreer.layout.style.TypefaceSpan

internal sealed class TextSource {
    object None : TextSource()
    data class SpannedSource(val spanned: Spanned, val defaultSpans: List<Any>) : TextSource()
    data class TypesetterSource(val typesetter: Typesetter) : TextSource()
}

internal data class TextProperties(
    var string: String? = null,
    var spanned: Spanned? = null,
    var typesetter: Typesetter? = null,
    var typeface: Typeface? = null,
    var textSize: Float = 16f,
    var textAlignment: TextAlignment = TextAlignment.LEADING,
    var textColor: Color = Color.Black,
    var truncationMode: BreakMode = BreakMode.LINE,
    var truncationPlace: TruncationPlace? = null,
    var isJustificationEnabled: Boolean = false,
    var justificationLevel: Float = 1.0f,
    var maxLines: Int? = null,
    var extraLineSpacing: Float = 0f,
    var lineHeightMultiplier: Float = 1.0f,
    var separatorColor: Color? = null,
    var renderingStyle: RenderingStyle = RenderingStyle.FILL,
    var strokeColor: Color = Color.Black,
    var strokeWidth: Float = 1f,
    var strokeCap: StrokeCap = StrokeCap.BUTT,
    var strokeJoin: StrokeJoin = StrokeJoin.ROUND,
    var strokeMiter: Float = 1f
) {
    fun textSource(): TextSource {
        typesetter?.let { return TextSource.TypesetterSource(it) }

        val typeface = typeface ?: return TextSource.None

        spanned?.let { s ->
            if (s.isNotEmpty()) {
                val defaults = listOf(
                    TypefaceSpan(typeface) as Any,
                    TypeSizeSpan(textSize)
                )
                return TextSource.SpannedSource(s, defaults)
            }
        }

        string?.let { s ->
            if (s.isNotEmpty()) {
                val defaults = listOf(
                    TypefaceSpan(typeface) as Any,
                    TypeSizeSpan(textSize)
                )
                return TextSource.SpannedSource(SpannedString(s), defaults)
            }
        }

        return TextSource.None
    }

    fun updateFrameResolver(resolver: FrameResolver) {
        resolver.typesetter = typesetter
        resolver.textAlignment = textAlignment
        resolver.truncationMode = truncationMode
        resolver.truncationPlace = truncationPlace
        resolver.isJustificationEnabled = isJustificationEnabled
        resolver.justificationLevel = justificationLevel
        resolver.maxLines = maxLines ?: 0
        resolver.extraLineSpacing = extraLineSpacing
        resolver.lineHeightMultiplier = lineHeightMultiplier
    }

    fun updateRenderer(renderer: Renderer) {
        renderer.renderingStyle = renderingStyle
        renderer.fillColor = textColor.toArgb()
        renderer.strokeColor = strokeColor.toArgb()
        renderer.strokeWidth = strokeWidth
        renderer.strokeCap = strokeCap
        renderer.strokeJoin = strokeJoin
        renderer.strokeMiter = strokeMiter
    }
}
