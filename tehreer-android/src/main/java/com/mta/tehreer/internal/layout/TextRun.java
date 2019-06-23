/*
 * Copyright (C) 2019 Muhammad Tayyab Akram
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
import com.mta.tehreer.sfnt.WritingDirection;

import java.util.Collections;
import java.util.List;

public abstract class TextRun {
    public abstract int getCharStart();
    public abstract int getCharEnd();

    public abstract byte getBidiLevel();

    public  @NonNull List<Object> getSpans() {
        return Collections.emptyList();
    }

    public int getStartExtraLength() {
        return 0;
    }

    public int getEndExtraLength() {
        return 0;
    }

    public abstract @NonNull Typeface getTypeface();
    public abstract float getTypeSize();

    public @NonNull WritingDirection getWritingDirection() {
        return WritingDirection.LEFT_TO_RIGHT;
    }

    public int getGlyphCount() {
        return 1;
    }

    private int getSpaceGlyphId() {
        return getTypeface().getGlyphId(' ');
    }

    public @NonNull IntList getGlyphIds() {
        return IntList.of(getSpaceGlyphId());
    }

    public @NonNull PointList getGlyphOffsets() {
        return PointList.of(0, 0);
    }

    public @NonNull FloatList getGlyphAdvances() {
        return FloatList.of(getWidth());
    }

    public @NonNull IntList getClusterMap() {
        return IntList.of(new int[getCharEnd() - getCharStart()]);
    }

    public abstract float getAscent();
    public abstract float getDescent();
    public abstract float getLeading();

    public float getWidth() {
        return getRangeDistance(getCharStart(), getCharEnd());
    }

    public float getHeight() {
        return getAscent() + getDescent() + getLeading();
    }

    public int getClusterStart(int charIndex) {
        return getCharStart();
    }

    public int getClusterEnd(int charIndex) {
        return getCharEnd();
    }

    public int getLeadingGlyphIndex(int charIndex) {
        return 0;
    }

    public int getTrailingGlyphIndex(int charIndex) {
        return 1;
    }

    public abstract float getCaretEdge(int charIndex);

    public abstract float getRangeDistance(int fromIndex, int toIndex);

    public abstract int computeNearestCharIndex(float distance);

    public float computeTypographicExtent(int glyphStart, int glyphEnd) {
        FloatList glyphAdvances = getGlyphAdvances();
        float extent = 0.0f;

        for (int i = glyphStart; i < glyphEnd; i++) {
            extent += glyphAdvances.get(i);
        }

        return extent;
    }

    public @NonNull RectF computeBoundingBox(@NonNull Renderer renderer, int glyphStart, int glyphEnd) {
        renderer.setTypeface(getTypeface());
        renderer.setTypeSize(getTypeSize());
        renderer.setWritingDirection(getWritingDirection());

        return renderer.computeBoundingBox(getGlyphIds().subList(glyphStart, glyphEnd),
                                           getGlyphOffsets().subList(glyphStart, glyphEnd),
                                           getGlyphAdvances().subList(glyphStart, glyphEnd));
    }

    public abstract void draw(@NonNull Renderer renderer, @NonNull Canvas canvas);
}
