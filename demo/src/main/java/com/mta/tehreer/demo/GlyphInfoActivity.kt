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

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.mta.tehreer.graphics.TypefaceManager
import com.mta.tehreer.graphics.Typeface
import java.lang.UnsupportedOperationException
import kotlin.math.max
import kotlin.math.min

private class GlyphInfoDrawable(
    context: Context,
    typeface: Typeface,
    glyphId: Int,
    minWidth: Int,
    minHeight: Int
) : Drawable() {
    val dp: Float
    val minWidth: Int
    val minHeight: Int
    val ascent: Float
    val descent: Float
    val advance: Float
    val glyphPath: Path
    val glyphBounds: RectF
    val paint: Paint

    init {
        this.dp = context.resources.displayMetrics.density
        this.minWidth = minWidth
        this.minHeight = minHeight

        val typeSize = typeface.unitsPerEm.toFloat()
        val displaySize = minHeight / 3.0f
        val sizeScale = displaySize / typeSize

        ascent = typeface.ascent * sizeScale
        descent = typeface.descent * sizeScale
        advance = typeface.getGlyphAdvance(glyphId, typeSize, false) * sizeScale

        val glyphMatrix = Matrix()
        glyphMatrix.setScale(sizeScale, sizeScale)
        glyphPath = typeface.getGlyphPath(glyphId, typeSize, glyphMatrix)

        glyphBounds = RectF()
        glyphPath.computeBounds(glyphBounds, true)

        paint = Paint()
        paint.isAntiAlias = true
        paint.textSize = 12.0f * dp
    }

    override fun draw(canvas: Canvas) {
        val drawableBounds = bounds

        val height = ascent + descent

        val lsbX =
            (drawableBounds.left + (drawableBounds.width() - glyphBounds.width()) / 2.0f - glyphBounds.left + 0.5).toInt()
        val rsbX = (lsbX + advance + 0.5).toInt()
        val baseY =
            (drawableBounds.top + (drawableBounds.height() - height) / 2.0f + ascent + 0.5).toInt()
        val ascentY = (baseY - ascent + 0.5).toInt()
        val descentY = (baseY + descent + 0.5).toInt()

        paint.color = Color.DKGRAY
        paint.strokeWidth = 1.0f
        paint.style = Paint.Style.STROKE

        // Draw Vertical Lines.
        canvas.drawLine(
            lsbX.toFloat(),
            drawableBounds.top.toFloat(),
            lsbX.toFloat(),
            drawableBounds.bottom.toFloat(),
            paint
        )
        canvas.drawLine(
            rsbX.toFloat(),
            drawableBounds.top.toFloat(),
            rsbX.toFloat(),
            drawableBounds.bottom.toFloat(),
            paint
        )
        // Draw Horizontal Lines.
        canvas.drawLine(
            drawableBounds.left.toFloat(),
            baseY.toFloat(),
            drawableBounds.right.toFloat(),
            baseY.toFloat(),
            paint
        )
        canvas.drawLine(
            drawableBounds.left.toFloat(),
            ascentY.toFloat(),
            drawableBounds.right.toFloat(),
            ascentY.toFloat(),
            paint
        )
        canvas.drawLine(
            drawableBounds.left.toFloat(),
            descentY.toFloat(),
            drawableBounds.right.toFloat(),
            descentY.toFloat(),
            paint
        )
        // Draw Origin Circle.
        canvas.drawCircle(lsbX.toFloat(), baseY.toFloat(), 4.0f * dp, paint)

        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL

        // Draw Headings.
        canvas.drawText("Ascent", drawableBounds.left.toFloat(), ascentY - 2.0f * dp, paint)
        canvas.drawText("Baseline", drawableBounds.left.toFloat(), baseY - 2.0f * dp, paint)
        canvas.drawText("Descent", drawableBounds.left.toFloat(), descentY - 2.0f * dp, paint)

        paint.strokeWidth = 1.0f * dp
        paint.style = Paint.Style.STROKE

        // Draw Glyph Path.
        canvas.translate(lsbX.toFloat(), baseY.toFloat())
        canvas.drawPath(glyphPath, paint)
    }

    override fun setAlpha(i: Int) {
        throw UnsupportedOperationException()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        throw UnsupportedOperationException()
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int {
        val padding = 144.0f * dp
        val negativeLSB = min(glyphBounds.left, 0.0f)
        val advanceWidth = advance - negativeLSB
        val pathWidth = glyphBounds.right - negativeLSB
        val boundaryWidth = (max(advanceWidth, pathWidth) + padding + 0.5f).toInt()

        return max(minWidth, boundaryWidth)
    }

    override fun getIntrinsicHeight(): Int {
        val padding = 32.0f * dp
        val boundaryHeight = (ascent + descent + padding + 0.5f).toInt()

        return max(minHeight, boundaryHeight)
    }
}

class GlyphInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glyph_info)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val typefaceName = intent.getStringExtra(TYPEFACE_NAME).orEmpty()
        val glyphId = intent.getIntExtra(GLYPH_ID, 0)
        val typeface = TypefaceManager.getTypefaceByName(typefaceName)!!

        val glyphInfo = findViewById<View>(R.id.image_view_glyph_info) as ImageView
        glyphInfo.viewTreeObserver.addOnGlobalLayoutListener {
            val width = findViewById<View>(android.R.id.content).width
            val height = glyphInfo.height
            val drawable = GlyphInfoDrawable(this@GlyphInfoActivity, typeface, glyphId, width, height)
            glyphInfo.setImageDrawable(drawable)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val TYPEFACE_NAME = "typeface"
        const val GLYPH_ID = "glyph_id"
    }
}
