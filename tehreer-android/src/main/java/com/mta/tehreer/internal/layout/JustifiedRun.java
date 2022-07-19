/*
 * Copyright (C) 2021-2022 Muhammad Tayyab Akram
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
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.graphics.DefaultTextRunDrawing;
import com.mta.tehreer.internal.graphics.TextRunDrawing;
import com.mta.tehreer.sfnt.WritingDirection;

import java.util.List;

public class JustifiedRun extends AbstractTextRun {
    private final @NonNull TextRun textRun;
    private final @NonNull FloatList justifiedAdvances;
    private final @NonNull FloatList justifiedEdges;
    private float caretBoundary = 0.0f;

    public JustifiedRun(@NonNull TextRun textRun, @NonNull FloatList justifiedAdvances) {
        boolean isRTL = (textRun.getBidiLevel() & 1) == 1;

        this.textRun = textRun;
        this.justifiedAdvances = justifiedAdvances;
        this.justifiedEdges = new CaretEdgesBuilder()
                .setBackward(textRun.isBackward())
                .setRTL(isRTL)
                .setGlyphAdvances(justifiedAdvances)
                .setClusterMap(textRun.getClusterMap())
                .setCaretStops(null)
                .build();

        if (isRTL) {
            if (textRun.getStartExtraLength() > 0) {
                caretBoundary = getCaretBoundary(textRun.getCharStart(), textRun.getCharEnd());
            }
        }
    }

    @Override
    public int getCharStart() {
        return textRun.getCharStart();
    }

    @Override
    public int getCharEnd() {
        return textRun.getCharEnd();
    }

    @Override
    public boolean isBackward() {
        return textRun.isBackward();
    }

    @Override
    public byte getBidiLevel() {
        return textRun.getBidiLevel();
    }

    @Override
    public @NonNull List<Object> getSpans() {
        return textRun.getSpans();
    }

    @Override
    public int getStartExtraLength() {
        return textRun.getStartExtraLength();
    }

    @Override
    public int getEndExtraLength() {
        return textRun.getEndExtraLength();
    }

    @Override
    public @NonNull Typeface getTypeface() {
        return textRun.getTypeface();
    }

    @Override
    public float getTypeSize() {
        return textRun.getTypeSize();
    }

    @Override
    public @NonNull WritingDirection getWritingDirection() {
        return textRun.getWritingDirection();
    }

    @Override
    public int getGlyphCount() {
        return textRun.getGlyphCount();
    }

    @Override
    public @NonNull IntList getGlyphIds() {
        return textRun.getGlyphIds();
    }

    @Override
    public @NonNull PointList getGlyphOffsets() {
        return textRun.getGlyphOffsets();
    }

    @Override
    public @NonNull FloatList getGlyphAdvances() {
        return justifiedAdvances;
    }

    @Override
    public @NonNull IntList getClusterMap() {
        return textRun.getClusterMap();
    }

    @Override
    public @NonNull FloatList getCaretEdges() {
        return justifiedEdges;
    }

    @Override
    public float getAscent() {
        return textRun.getAscent();
    }

    @Override
    public float getDescent() {
        return textRun.getDescent();
    }

    @Override
    public float getLeading() {
        return textRun.getLeading();
    }

    @Override
    public float getWidth() {
        return super.getWidth();
    }

    @Override
    public float getHeight() {
        return textRun.getHeight();
    }

    @Override
    public int getClusterStart(int charIndex) {
        return textRun.getClusterStart(charIndex);
    }

    @Override
    public int getClusterEnd(int charIndex) {
        return textRun.getClusterEnd(charIndex);
    }

    @Override
    public @NonNull int[] getGlyphRangeForChars(int fromIndex, int toIndex) {
        return textRun.getGlyphRangeForChars(fromIndex, toIndex);
    }

    @Override
    public int getLeadingGlyphIndex(int charIndex) {
        return textRun.getLeadingGlyphIndex(charIndex);
    }

    @Override
    public int getTrailingGlyphIndex(int charIndex) {
        return textRun.getTrailingGlyphIndex(charIndex);
    }

    @Override
    public float getCaretBoundary(int fromIndex, int toIndex) {
        return super.getCaretBoundary(fromIndex, toIndex) - caretBoundary;
    }

    @Override
    public float getCaretEdge(int charIndex) {
        return super.getCaretEdge(charIndex);
    }

    @Override
    public float getRangeDistance(int fromIndex, int toIndex) {
        return super.getRangeDistance(fromIndex, toIndex);
    }

    @Override
    public int computeNearestCharIndex(float distance) {
        return super.computeNearestCharIndex(distance);
    }

    @Override
    public float computeTypographicExtent(int glyphStart, int glyphEnd) {
        return super.computeTypographicExtent(glyphStart, glyphEnd);
    }

    @Override
    public @NonNull RectF computeBoundingBox(@NonNull Renderer renderer, int glyphStart, int glyphEnd) {
        return super.computeBoundingBox(renderer, glyphStart, glyphEnd);
    }

    @Override
    public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas) {
        TextRunDrawing drawing = new DefaultTextRunDrawing(this);
        drawing.draw(renderer, canvas);
    }
}
