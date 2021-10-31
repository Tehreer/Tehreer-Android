/*
 * Copyright (C) 2016-2021 Muhammad Tayyab Akram
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

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.sfnt.WritingDirection;

import java.util.Collections;
import java.util.List;

public final class IntrinsicRun extends AbstractTextRun {
    public final int charStart;
    public final int charEnd;
    public final boolean isBackward;
    public final byte bidiLevel;
    public final @NonNull WritingDirection writingDirection;
    public final @NonNull Typeface typeface;
    public final float typeSize;
    public final float ascent;
    public final float descent;
    public final float leading;
    public final @NonNull int[] glyphIds;
    public final @NonNull float[] glyphOffsets;
    public final @NonNull float[] glyphAdvances;
    public final @NonNull int[] clusterMap;
    public final @NonNull FloatList caretEdges;

    public IntrinsicRun(int charStart, int charEnd, boolean isBackward, byte bidiLevel,
                        @NonNull WritingDirection writingDirection,
                        @NonNull Typeface typeface, float typeSize,
                        float ascent, float descent, float leading,
                        @NonNull int[] glyphIds,
                        @NonNull float[] offsets, @NonNull float[] advances,
                        @NonNull int[] clusterMap,
                        @NonNull FloatList caretEdges) {
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.isBackward = isBackward;
        this.bidiLevel = bidiLevel;
        this.writingDirection = writingDirection;
        this.typeface = typeface;
        this.typeSize = typeSize;
        this.ascent = ascent;
        this.descent = descent;
        this.leading = leading;
        this.glyphIds = glyphIds;
        this.glyphOffsets = offsets;
        this.glyphAdvances = advances;
        this.clusterMap = clusterMap;
        this.caretEdges = caretEdges;
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
    public boolean isBackward() {
        return isBackward;
    }

    @Override
    public byte getBidiLevel() {
        return bidiLevel;
    }

    @Override
    public @NonNull List<Object> getSpans() {
        return Collections.emptyList();
    }

    @Override
    public int getStartExtraLength() {
        return 0;
    }

    @Override
    public int getEndExtraLength() {
        return 0;
    }

    @Override
    public @NonNull Typeface getTypeface() {
        return typeface;
    }

    @Override
    public float getTypeSize() {
        return typeSize;
    }

    @Override
    public @NonNull WritingDirection getWritingDirection() {
        return writingDirection;
    }

    @Override
    public int getGlyphCount() {
        return glyphIds.length;
    }

    @Override
    public @NonNull IntList getGlyphIds() {
        return IntList.of(glyphIds);
    }

    @Override
    public @NonNull PointList getGlyphOffsets() {
        return PointList.of(glyphOffsets);
    }

    @Override
    public @NonNull FloatList getGlyphAdvances() {
        return FloatList.of(glyphAdvances);
    }

    @Override
    public @NonNull IntList getClusterMap() {
        return IntList.of(clusterMap);
    }

    @Override
    public @NonNull FloatList getCaretEdges() {
        return caretEdges;
    }

    @Override
    public float getAscent() {
        return ascent;
    }

    @Override
    public float getDescent() {
        return descent;
    }

    @Override
    public float getLeading() {
        return leading;
    }

    @Override
    public int getClusterStart(int charIndex) {
        final int arrayIndex = charIndex - charStart;
        final int common = clusterMap[arrayIndex];

        for (int i = arrayIndex - 1; i >= 0; i--) {
            if (clusterMap[i] != common) {
                return (i + 1) + charStart;
            }
        }

        return charStart;
    }

    @Override
    public int getClusterEnd(int charIndex) {
        final int arrayIndex = charIndex - charStart;
        final int common = clusterMap[arrayIndex];
        final int length = clusterMap.length;

        for (int i = arrayIndex + 1; i < length; i++) {
            if (clusterMap[i] != common) {
                return i + charStart;
            }
        }

        return length + charStart;
    }

    private int forwardGlyphIndex(int arrayIndex) {
        final int common = clusterMap[arrayIndex];
        final int length = clusterMap.length;

        for (int i = arrayIndex + 1; i < length; i++) {
            final int mapping = clusterMap[i];
            if (mapping != common) {
                return mapping - 1;
            }
        }

        return glyphIds.length - 1;
    }

    private int backwardGlyphIndex(int arrayIndex) {
        final int common = clusterMap[arrayIndex];

        for (int i = arrayIndex - 1; i >= 0; i--) {
            final int mapping = clusterMap[i];
            if (mapping != common) {
                return mapping - 1;
            }
        }

        return glyphIds.length - 1;
    }

    @Override
    public @NonNull @Size(2) int[] getGlyphRangeForChars(int fromIndex, int toIndex) {
        final int firstIndex = fromIndex - charStart;
        final int lastIndex = (toIndex - 1) - charStart;

        final int[] glyphRange = new int[2];

        if (isBackward) {
            glyphRange[0] = clusterMap[lastIndex];
            glyphRange[1] = backwardGlyphIndex(firstIndex) + 1;
        } else {
            glyphRange[0] = clusterMap[firstIndex];
            glyphRange[1] = forwardGlyphIndex(lastIndex) + 1;
        }

        return glyphRange;
    }

    @Override
    public int getLeadingGlyphIndex(int charIndex) {
        final int arrayIndex = charIndex - charStart;

        return isBackward ? backwardGlyphIndex(arrayIndex) : clusterMap[arrayIndex];
    }

    @Override
    public int getTrailingGlyphIndex(int charIndex) {
        final int arrayIndex = charIndex - charStart;

        return isBackward ? clusterMap[arrayIndex] : forwardGlyphIndex(arrayIndex);
    }

    @Override
    public float getCaretBoundary(int fromIndex, int toIndex) {
        return super.getCaretBoundary(fromIndex, toIndex);
    }

    @Override
    public float getCaretEdge(int charIndex) {
        return super.getCaretEdge(charIndex);
    }

    @Override
    public float getCaretEdge(int charIndex, float caretBoundary) {
        return super.getCaretEdge(charIndex, caretBoundary);
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
    public int computeNearestCharIndex(float distance, int fromIndex, int toIndex) {
        return super.computeNearestCharIndex(distance, fromIndex, toIndex);
    }

    @Override
    public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas) {
        throw new UnsupportedOperationException();
    }
}
