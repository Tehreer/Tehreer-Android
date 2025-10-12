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

import android.graphics.Point
import android.text.Spanned
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.State
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.mta.tehreer.graphics.RenderingStyle
import com.mta.tehreer.graphics.StrokeCap
import com.mta.tehreer.graphics.StrokeJoin
import com.mta.tehreer.graphics.Typeface
import com.mta.tehreer.layout.BreakMode
import com.mta.tehreer.layout.TextAlignment
import com.mta.tehreer.layout.TruncationPlace
import com.mta.tehreer.layout.Typesetter

@Composable
fun StyledText(
    modifier: Modifier = Modifier,
    text: String,
    typeface: Typeface? = null,
    textSize: TextUnit = 16.sp,
    textAlignment: TextAlignment = TextAlignment.LEADING,
    textColor: Color = Color.Black,
    truncationMode: BreakMode = BreakMode.LINE,
    truncationPlace: TruncationPlace? = null,
    maxLines: Int? = null,
    extraLineSpacing: Float = 0.0f,
    lineHeightMultiplier: Float = 1.0f,
    renderingStyle: RenderingStyle = RenderingStyle.FILL,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 1.0f,
    strokeCap: StrokeCap = StrokeCap.BUTT,
    strokeJoin: StrokeJoin = StrokeJoin.ROUND,
    strokeMiter: Float = 1.0f
) {
    StyledText(
        modifier = modifier,
        string = text,
        spanned = null,
        typesetter = null,
        typeface = typeface,
        textSize = textSize,
        textAlignment = textAlignment,
        textColor = textColor,
        truncationMode = truncationMode,
        truncationPlace = truncationPlace,
        maxLines = maxLines,
        extraLineSpacing = extraLineSpacing,
        lineHeightMultiplier = lineHeightMultiplier,
        renderingStyle = renderingStyle,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        strokeCap = strokeCap,
        strokeJoin = strokeJoin,
        strokeMiter = strokeMiter
    )
}

@Composable
fun StyledText(
    modifier: Modifier = Modifier,
    spanned: Spanned,
    typeface: Typeface? = null,
    textSize: TextUnit = 16.sp,
    textAlignment: TextAlignment = TextAlignment.LEADING,
    textColor: Color = Color.Black,
    truncationMode: BreakMode = BreakMode.LINE,
    truncationPlace: TruncationPlace? = null,
    maxLines: Int? = null,
    extraLineSpacing: Float = 0.0f,
    lineHeightMultiplier: Float = 1.0f,
    renderingStyle: RenderingStyle = RenderingStyle.FILL,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 1.0f,
    strokeCap: StrokeCap = StrokeCap.BUTT,
    strokeJoin: StrokeJoin = StrokeJoin.ROUND,
    strokeMiter: Float = 1.0f
) {
    StyledText(
        modifier = modifier,
        string = null,
        spanned = spanned,
        typesetter = null,
        typeface = typeface,
        textSize = textSize,
        textAlignment = textAlignment,
        textColor = textColor,
        truncationMode = truncationMode,
        truncationPlace = truncationPlace,
        maxLines = maxLines,
        extraLineSpacing = extraLineSpacing,
        lineHeightMultiplier = lineHeightMultiplier,
        renderingStyle = renderingStyle,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        strokeCap = strokeCap,
        strokeJoin = strokeJoin,
        strokeMiter = strokeMiter
    )
}

@Composable
fun StyledText(
    modifier: Modifier = Modifier,
    typesetter: Typesetter,
    textAlignment: TextAlignment = TextAlignment.LEADING,
    textColor: Color = Color.Black,
    truncationMode: BreakMode = BreakMode.LINE,
    truncationPlace: TruncationPlace? = null,
    maxLines: Int? = null,
    extraLineSpacing: Float = 0.0f,
    lineHeightMultiplier: Float = 1.0f,
    renderingStyle: RenderingStyle = RenderingStyle.FILL,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 1.0f,
    strokeCap: StrokeCap = StrokeCap.BUTT,
    strokeJoin: StrokeJoin = StrokeJoin.ROUND,
    strokeMiter: Float = 1.0f
) {
    StyledText(
        modifier = modifier,
        string = null,
        spanned = null,
        typesetter = typesetter,
        typeface = null,
        textSize = 16.sp,
        textAlignment = textAlignment,
        textColor = textColor,
        truncationMode = truncationMode,
        truncationPlace = truncationPlace,
        maxLines = maxLines,
        extraLineSpacing = extraLineSpacing,
        lineHeightMultiplier = lineHeightMultiplier,
        renderingStyle = renderingStyle,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        strokeCap = strokeCap,
        strokeJoin = strokeJoin,
        strokeMiter = strokeMiter
    )
}

@Composable
private fun StyledText(
    modifier: Modifier = Modifier,
    string: String? = null,
    spanned: Spanned? = null,
    typesetter: Typesetter? = null,
    typeface: Typeface? = null,
    textSize: TextUnit = 16.sp,
    textAlignment: TextAlignment = TextAlignment.LEADING,
    textColor: Color = Color.Black,
    truncationMode: BreakMode = BreakMode.LINE,
    truncationPlace: TruncationPlace? = null,
    maxLines: Int? = null,
    extraLineSpacing: Float = 0.0f,
    lineHeightMultiplier: Float = 1.0f,
    separatorColor: Color? = null,
    renderingStyle: RenderingStyle = RenderingStyle.FILL,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 1.0f,
    strokeCap: StrokeCap = StrokeCap.BUTT,
    strokeJoin: StrokeJoin = StrokeJoin.ROUND,
    strokeMiter: Float = 1.0f
) {
    val textSizePx: Float = with(LocalDensity.current) {
        textSize.toPx()
    }

    val properties = remember {
        TextProperties(
            string = string,
            spanned = spanned,
            typesetter = typesetter,
            typeface = typeface,
            textSize = textSizePx,
            textAlignment = textAlignment,
            textColor = textColor,
            truncationMode = truncationMode,
            truncationPlace = truncationPlace,
            maxLines = maxLines,
            extraLineSpacing = extraLineSpacing,
            lineHeightMultiplier = lineHeightMultiplier,
            separatorColor = separatorColor,
            renderingStyle = renderingStyle,
            strokeColor = strokeColor,
            strokeWidth = strokeWidth,
            strokeCap = strokeCap,
            strokeJoin = strokeJoin,
            strokeMiter = strokeMiter
        )
    }

    StyledText(
        modifier = modifier,
        properties = rememberUpdatedState(properties)
    )
}

@Composable
private fun StyledText(
    modifier: Modifier = Modifier,
    properties: State<TextProperties>
) {
    val manager = remember { StyledTextManager() }

    // Setup on first compose
    LaunchedEffect(Unit) {
        manager.setupProperties(properties.value)
    }
    // React to properties change
    LaunchedEffect(properties.value) {
        manager.updateProperties(properties.value)
    }

    key(manager.refreshKey) {
        StyledTextLayout(
            manager = manager,
            content = {
                StyledTextCanvas(manager = manager)
            }
        )
    }
}

@Composable
private fun StyledTextLayout(
    manager: StyledTextManager,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = Modifier.onSizeChanged { newSize ->
            manager.refreshLayout(Point(newSize.width, newSize.height))
        },
        measurePolicy = { measurables, constraints ->
            // Determine the container size to request from manager (in pixels).
            val containerWidth = if (constraints.hasBoundedWidth) {
                constraints.maxWidth
            } else {
                Int.MAX_VALUE
            }
            val containerHeight = if (constraints.hasBoundedHeight) {
                constraints.maxHeight
            } else {
                Int.MAX_VALUE
            }

            val frameSize = manager.determineFrameSize(containerWidth, containerHeight)

            val widthPx = frameSize?.x
                ?: (if (constraints.hasBoundedWidth) constraints.maxWidth else 0)
            val heightPx = frameSize?.y
                ?: (if (constraints.hasBoundedHeight) constraints.maxHeight else 0)

            val placeable = measurables.firstOrNull()?.measure(
                Constraints.fixed(widthPx, heightPx)
            )
            layout(widthPx, heightPx) {
                placeable?.place(0, 0)
            }
        }
    )
}

@Composable
private fun StyledTextCanvas(
    manager: StyledTextManager
) {
    Canvas(modifier = Modifier) {
        val frame = manager.textFrame
        if (frame != null) {
            val nativeCanvas = drawContext.canvas.nativeCanvas
            frame.draw(manager.renderer, nativeCanvas, 0f, 0f)
        }
    }
}
