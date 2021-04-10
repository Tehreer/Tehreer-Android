/*
 * Copyright (C) 2020-2021 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.layout;

import android.graphics.Canvas;
import android.text.style.ForegroundColorSpan;
import android.text.style.ScaleXSpan;

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.sfnt.WritingDirection;

import java.util.List;

public class IntrinsicRunSlice extends TextRun {
    private final @NonNull IntrinsicRun intrinsicRun;
    private final int charStart;
    private final int charEnd;
    private final @NonNull List<Object> spans;
    private final int glyphOffset;
    private final int glyphCount;

    public IntrinsicRunSlice(@NonNull IntrinsicRun intrinsicRun, int charStart, int charEnd,
                             @NonNull List<Object> spans) {
        final int[] glyphRange = intrinsicRun.getGlyphRangeForChars(charStart, charEnd);

        this.intrinsicRun = intrinsicRun;
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.spans = spans;
        this.glyphOffset = glyphRange[0];
        this.glyphCount = glyphRange[1] - glyphRange[0];
    }

    @Override
    public int getCharStart() {
        return charStart;
    }

    @Override
    public int getCharEnd() {
        return charEnd;
    }

    @Override
    public byte getBidiLevel() {
        return intrinsicRun.bidiLevel;
    }

    @Override
    public @NonNull List<Object> getSpans() {
        return spans;
    }

    @Override
    public int getStartExtraLength() {
        return charStart - intrinsicRun.getClusterStart(charStart);
    }

    @Override
    public int getEndExtraLength() {
        return intrinsicRun.getClusterEnd(charEnd - 1) - charEnd;
    }

    @Override
    public @NonNull Typeface getTypeface() {
        return intrinsicRun.typeface;
    }

    @Override
    public float getTypeSize() {
        return intrinsicRun.typeSize;
    }

    @Override
    public @NonNull WritingDirection getWritingDirection() {
        return intrinsicRun.writingDirection;
    }

    @Override
    public int getGlyphCount() {
        return glyphCount;
    }

    @Override
    public @NonNull IntList getGlyphIds() {
        return intrinsicRun.getGlyphIds().subList(glyphOffset, glyphOffset + glyphCount);
    }

    @Override
    public @NonNull PointList getGlyphOffsets() {
        return intrinsicRun.getGlyphOffsets().subList(glyphOffset, glyphOffset + glyphCount);
    }

    @Override
    public @NonNull FloatList getGlyphAdvances() {
        return intrinsicRun.getGlyphAdvances().subList(glyphOffset, glyphOffset + glyphCount);
    }

    @Override
    public @NonNull IntList getClusterMap() {
        final int actualStart = intrinsicRun.getClusterStart(charStart);
        final int actualEnd = intrinsicRun.getClusterEnd(charEnd - 1);
        final int actualLength = actualEnd - actualStart;

        return new ClusterMap(intrinsicRun.clusterMap, actualStart, actualLength, glyphOffset);
    }

    @Override
    public float getAscent() {
        return intrinsicRun.ascent;
    }

    @Override
    public float getDescent() {
        return intrinsicRun.descent;
    }

    @Override
    public float getLeading() {
        return intrinsicRun.leading;
    }

    @Override
    public float getWidth() {
        return intrinsicRun.getRangeDistance(charStart, charEnd);
    }

    @Override
    public float getHeight() {
        return (intrinsicRun.ascent + intrinsicRun.descent + intrinsicRun.leading);
    }

    @Override
    public int getClusterStart(int charIndex) {
        return intrinsicRun.getClusterStart(charIndex);
    }

    @Override
    public int getClusterEnd(int charIndex) {
        return intrinsicRun.getClusterEnd(charIndex);
    }

    @Override
    public int getLeadingGlyphIndex(int charIndex) {
        return intrinsicRun.getLeadingGlyphIndex(charIndex) - glyphOffset;
    }

    @Override
    public int getTrailingGlyphIndex(int charIndex) {
        return intrinsicRun.getTrailingGlyphIndex(charIndex) - glyphOffset;
    }

    @Override
    public float getCaretEdge(int charIndex) {
        final float caretBoundary = intrinsicRun.getCaretBoundary(charStart, charEnd);
        final float caretEdge = intrinsicRun.getCaretEdge(charIndex, caretBoundary);

        return caretEdge;
    }

    @Override
    public float getRangeDistance(int fromIndex, int toIndex) {
        return intrinsicRun.getRangeDistance(fromIndex, toIndex);
    }

    @Override
    public int computeNearestCharIndex(float distance) {
        return intrinsicRun.computeNearestCharIndex(distance, charStart, charEnd);
    }

    @Override
    public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas) {
        renderer.setScaleX(1.0f);

        int defaultFillColor = renderer.getFillColor();

        for (Object span : spans) {
            if (span instanceof ForegroundColorSpan) {
                renderer.setFillColor(((ForegroundColorSpan) span).getForegroundColor());
            } else if (span instanceof ScaleXSpan) {
                renderer.setScaleX(((ScaleXSpan) span).getScaleX());
            }
        }

        intrinsicRun.draw(renderer, canvas, charStart, charEnd);

        renderer.setFillColor(defaultFillColor);
    }
}
