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
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.sfnt.WritingDirection;

import java.util.Collections;
import java.util.List;

public final class ReplacementRun implements TextRun {
    public final @NonNull CharSequence charSequence;
    public final int charStart;
    public final int charEnd;
    public final byte bidiLevel;
    public final @NonNull ReplacementSpan replacement;
    public final @NonNull Paint paint;
    public final @NonNull Typeface typeface;
    public final float typeSize;
    public final int ascent;
    public final int descent;
    public final int leading;
    public final int extent;
    public final @NonNull FloatList caretEdges;

    public ReplacementRun(@NonNull CharSequence charSequence,
                          int charStart, int charEnd, byte bidiLevel,
                          @NonNull ReplacementSpan replacement, @NonNull Paint paint,
                          @NonNull Typeface typeface, float typeSize,
                          int ascent, int descent, int leading,
                          int extent, @NonNull FloatList caretEdges) {
        this.charSequence = charSequence;
        this.charStart = charStart;
        this.charEnd = charEnd;
        this.bidiLevel = bidiLevel;
        this.replacement = replacement;
        this.paint = paint;
        this.typeface = typeface;
        this.typeSize = typeSize;
        this.ascent = ascent;
        this.descent = descent;
        this.leading = leading;
        this.extent = extent;
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
        return false;
    }

    @Override
    public byte getBidiLevel() {
        return bidiLevel;
    }

    private boolean isRTL() {
        return (bidiLevel & 1) == 1;
    }

    @Override
    public @NonNull List<Object> getSpans() {
        return Collections.singletonList((Object) replacement);
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
        return WritingDirection.LEFT_TO_RIGHT;
    }

    @Override
    public int getGlyphCount() {
        return 1;
    }

    private int getSpaceGlyphId() {
        return typeface.getGlyphId(' ');
    }

    @Override
    public @NonNull IntList getGlyphIds() {
        return IntList.of(getSpaceGlyphId());
    }

    @Override
    public @NonNull PointList getGlyphOffsets() {
        return PointList.of(0, 0);
    }

    @Override
    public @NonNull FloatList getGlyphAdvances() {
        return FloatList.of(getWidth());
    }

    @Override
    public @NonNull IntList getClusterMap() {
        return IntList.of(new int[charEnd - charStart]);
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
    public float getWidth() {
        return extent;
    }

    @Override
    public float getHeight() {
        return ascent + descent + leading;
    }

    @Override
    public int getClusterStart(int charIndex) {
        return charStart;
    }

    @Override
    public int getClusterEnd(int charIndex) {
        return charEnd;
    }

    @Override
    public @NonNull int[] getGlyphRangeForChars(int fromIndex, int toIndex) {
        return new int[] { 0, 1 };
    }

    @Override
    public int getLeadingGlyphIndex(int charIndex) {
        return 0;
    }

    @Override
    public int getTrailingGlyphIndex(int charIndex) {
        return 1;
    }

    @Override
    public float getCaretBoundary(int fromIndex, int toIndex) {
        return 0;
    }

    @Override
    public float getCaretEdge(int charIndex) {
        return caretEdges.get(charIndex - charStart);
    }

    @Override
    public float getRangeDistance(int fromIndex, int toIndex) {
        final int firstIndex = fromIndex - charStart;
        final int lastIndex = toIndex - charStart;

        return CaretUtils.getRangeDistance(caretEdges, isRTL(), firstIndex, lastIndex);
    }

    @Override
    public int computeNearestCharIndex(float distance) {
        return CaretUtils.computeNearestIndex(caretEdges, isRTL(), distance) + charStart;
    }

    @Override
    public float computeTypographicExtent(int glyphStart, int glyphEnd) {
        return getWidth();
    }

    @Override
    public @NonNull RectF computeBoundingBox(@NonNull Renderer renderer, int glyphStart, int glyphEnd) {
        return new RectF(0.0f, 0.0f, getWidth(), getHeight());
    }

    @Override
    public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas) {
        replacement.draw(canvas, charSequence, charStart, charEnd,
                         0, -ascent, 0, descent, paint);
    }
}
