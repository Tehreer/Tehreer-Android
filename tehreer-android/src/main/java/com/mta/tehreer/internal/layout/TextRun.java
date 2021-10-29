/*
 * Copyright (C) 2019-2021 Muhammad Tayyab Akram
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
import androidx.annotation.Size;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.sfnt.WritingDirection;

import java.util.List;

public interface TextRun {
    int getCharStart();
    int getCharEnd();
    boolean isBackward();
    byte getBidiLevel();
    @NonNull List<Object> getSpans();

    int getStartExtraLength();
    int getEndExtraLength();

    @NonNull Typeface getTypeface();
    float getTypeSize();
    @NonNull WritingDirection getWritingDirection();

    int getGlyphCount();
    @NonNull IntList getGlyphIds();
    @NonNull PointList getGlyphOffsets();
    @NonNull FloatList getGlyphAdvances();

    @NonNull IntList getClusterMap();
    @NonNull FloatList getCaretEdges();

    float getAscent();
    float getDescent();
    float getLeading();

    float getWidth();
    float getHeight();

    int getClusterStart(int charIndex);
    int getClusterEnd(int charIndex);

    @NonNull @Size(2) int[] getGlyphRangeForChars(int fromIndex, int toIndex);
    int getLeadingGlyphIndex(int charIndex);
    int getTrailingGlyphIndex(int charIndex);

    float getCaretBoundary(int fromIndex, int toIndex);
    float getCaretEdge(int charIndex);

    float getRangeDistance(int fromIndex, int toIndex);
    int computeNearestCharIndex(float distance);

    float computeTypographicExtent(int glyphStart, int glyphEnd);
    @NonNull RectF computeBoundingBox(@NonNull Renderer renderer, int glyphStart, int glyphEnd);

    void draw(@NonNull Renderer renderer, @NonNull Canvas canvas);
}
