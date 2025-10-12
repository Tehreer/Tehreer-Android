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
import android.graphics.RectF
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mta.tehreer.graphics.Renderer
import com.mta.tehreer.layout.ComposedFrame
import com.mta.tehreer.layout.FrameResolver
import com.mta.tehreer.layout.Typesetter
import java.util.UUID
import kotlin.math.ceil

internal class StyledTextManager {
    val renderer = Renderer()
    private val resolver = FrameResolver()

    var textFrame by mutableStateOf<ComposedFrame?>(null)
        private set

    var refreshKey by mutableStateOf(UUID.randomUUID().toString())
        private set

    private val typesetter: Typesetter?
        get() = resolver.typesetter

    fun setupProperties(properties: TextProperties) {
        val typesetter = getUpdatedTypesetter(properties)

        properties.updateFrameResolver(resolver)
        properties.updateRenderer(renderer)

        resolver.typesetter = typesetter
        resolver.fitsHorizontally = true
        resolver.fitsVertically = true
    }

    fun updateProperties(properties: TextProperties) {
        setupProperties(properties)
        refreshLayout()
    }

    fun refreshLayout(proposedSize: Point? = null) {
        if (proposedSize != null) {
            updateTextFrame(
                proposedSize.x.toFloat(),
                proposedSize.y.toFloat()
            )
        } else {
            refreshKey = UUID.randomUUID().toString()
            textFrame = null
        }
    }

    fun determineFrameSize(containerWidth: Int, containerHeight: Int): Point? {
        val typesetter = typesetter ?: return null

        if (containerWidth <= 0f || containerHeight <= 0f) {
            return Point(0, 0)
        }

        resolver.frameBounds = RectF(
            0f, 0f, containerWidth.toFloat(), containerHeight.toFloat()
        )

        val spanned = typesetter.spanned
        if (spanned.isEmpty()) {
            return null
        }

        val resolvedFrame = resolver.createFrame(
            0, spanned.length
        )

        return Point(
            ceil(resolvedFrame.width).toInt(),
            ceil(resolvedFrame.height).toInt()
        )
    }

    private fun getUpdatedTypesetter(properties: TextProperties): Typesetter? {
        return when (val src = properties.textSource()) {
            is TextSource.None -> null
            is TextSource.TypesetterSource -> src.typesetter
            is TextSource.SpannedSource -> {
                Typesetter(src.spanned, src.defaultSpans)
            }
        }
    }

    private fun updateTextFrame(containerWidth: Float, containerHeight: Float) {
        val typesetter = typesetter ?: run {
            textFrame = null
            return
        }

        if (containerWidth <= 0f || containerHeight <= 0f) {
            textFrame = null
            return
        }

        resolver.frameBounds = RectF(
            0f, 0f, containerWidth, containerHeight
        )

        val spanned = typesetter.spanned
        textFrame = resolver.createFrame(0, spanned.length)
    }
}
