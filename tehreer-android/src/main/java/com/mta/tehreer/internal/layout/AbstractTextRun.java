/*
 * Copyright (C) 2021 Muhammad Tayyab Akram
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

import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.graphics.Renderer;

public abstract class AbstractTextRun implements TextRun {
    public boolean isRTL() {
        return (getBidiLevel() & 1) == 1;
    }

    @Override
    public float getWidth() {
        return getRangeDistance(getCharStart(), getCharEnd());
    }

    @Override
    public float getHeight() {
        return getAscent() + getDescent() + getLeading();
    }

    @Override
    public float getCaretBoundary(int fromIndex, int toIndex) {
        final int charStart = getCharStart();
        final int firstIndex = fromIndex - charStart;
        final int lastIndex = toIndex - charStart;

        return CaretUtils.getLeftMargin(getCaretEdges(), isRTL(), firstIndex, lastIndex);
    }

    @Override
    public float getCaretEdge(int charIndex) {
        return getCaretEdge(charIndex, 0.0f);
    }

    protected float getCaretEdge(int charIndex, float caretBoundary) {
        return getCaretEdges().get(charIndex - getCharStart()) - caretBoundary;
    }

    @Override
    public float getRangeDistance(int fromIndex, int toIndex) {
        final int charStart = getCharStart();
        final int firstIndex = fromIndex - charStart;
        final int lastIndex = toIndex - charStart;

        return CaretUtils.getRangeDistance(getCaretEdges(), isRTL(), firstIndex, lastIndex);
    }

    @Override
    public int computeNearestCharIndex(float distance) {
        return computeNearestCharIndex(distance, getCharStart(), getCharEnd());
    }

    protected int computeNearestCharIndex(float distance, int fromIndex, int toIndex) {
        final int charStart = getCharStart();
        final int firstIndex = fromIndex - charStart;
        final int lastIndex = toIndex - charStart;

        int nearestIndex = CaretUtils.computeNearestIndex(getCaretEdges(), isRTL(),
                firstIndex, lastIndex, distance);

        return nearestIndex + charStart;
    }

    @Override
    public float computeTypographicExtent(int glyphStart, int glyphEnd) {
        FloatList glyphAdvances = getGlyphAdvances();
        float extent = 0.0f;

        for (int i = glyphStart; i < glyphEnd; i++) {
            extent += glyphAdvances.get(i);
        }

        return extent;
    }

    @Override
    public @NonNull RectF computeBoundingBox(@NonNull Renderer renderer, int glyphStart, int glyphEnd) {
        renderer.setTypeface(getTypeface());
        renderer.setTypeSize(getTypeSize());
        renderer.setWritingDirection(getWritingDirection());

        return renderer.computeBoundingBox(getGlyphIds().subList(glyphStart, glyphEnd),
                                           getGlyphOffsets().subList(glyphStart, glyphEnd),
                                           getGlyphAdvances().subList(glyphStart, glyphEnd));
    }
}
