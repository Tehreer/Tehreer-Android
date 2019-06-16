/*
 * Copyright (C) 2016-2019 Muhammad Tayyab Akram
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
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.collections.IntList;
import com.mta.tehreer.collections.PointList;
import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.internal.util.Clusters;
import com.mta.tehreer.sfnt.WritingDirection;

public class IntrinsicRun {
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

    public boolean isRTL() {
        return (bidiLevel & 1) == 1;
    }

    public int glyphCount() {
        return glyphIds.length;
    }

    public @NonNull IntList getGlyphIds() {
        return IntList.of(glyphIds);
    }

    public @NonNull PointList getGlyphOffsets() {
        return PointList.of(glyphOffsets);
    }

    public @NonNull FloatList getGlyphAdvances() {
        return FloatList.of(glyphAdvances);
    }

    public @NonNull IntList getClusterMap() {
        return IntList.of(clusterMap);
    }

    public int getClusterStart(int charIndex) {
        return Clusters.actualClusterStart(clusterMap, charIndex - charStart) + charStart;
    }

    public int getClusterEnd(int charIndex) {
        return Clusters.actualClusterEnd(clusterMap, charIndex - charStart) + charStart;
    }

    public @NonNull @Size(2) int[] getGlyphRangeForChars(int fromIndex, int toIndex) {
        int[] glyphRange = new int[2];
        Clusters.loadGlyphRange(clusterMap, fromIndex - charStart, toIndex - charStart,
                                isBackward, glyphIds.length, glyphRange);

        return glyphRange;
    }

    public int getLeadingGlyphIndex(int charIndex) {
        return Clusters.leadingGlyphIndex(clusterMap, charIndex - charStart,
                                          isBackward, glyphIds.length);
    }

    public int getTrailingGlyphIndex(int charIndex) {
        return Clusters.trailingGlyphIndex(clusterMap, charIndex - charStart,
                                           isBackward, glyphIds.length);
    }

    public float getCaretBoundary(int fromIndex, int toIndex) {
        final int boundaryIndex = (isRTL() ? toIndex : fromIndex);
        final int arrayIndex = boundaryIndex - charStart;

        return caretEdges.get(arrayIndex);
    }

    public float getCaretEdge(int charIndex, float caretBoundary) {
        return caretEdges.get(charIndex - charStart) - caretBoundary;
    }

    private float getLeadingEdge(int fromIndex, int toIndex, float caretBoundary) {
        return getCaretEdge(!isBackward ? fromIndex : toIndex, caretBoundary);
    }

    public float measureChars(int fromIndex, int toIndex) {
        float firstEdge = caretEdges.get(fromIndex - charStart);
        float lastEdge = caretEdges.get(toIndex - charStart);

        return isRTL() ? firstEdge - lastEdge : lastEdge - firstEdge;
    }

    public int computeNearestCharIndex(float distance, int fromIndex, int toIndex) {
        final boolean isRTL = isRTL();
        final float caretBoundary = getCaretBoundary(fromIndex, toIndex);

        int leadingCharIndex = -1;
        int trailingCharIndex = -1;

        float leadingCaretEdge = 0.0f;
        float trailingCaretEdge = 0.0f;

        int index = (isRTL ? toIndex : fromIndex);
        int next = (isRTL ? -1 : 1);

        while (index <= toIndex && index >= fromIndex) {
            float caretEdge = getCaretEdge(index, caretBoundary);

            if (caretEdge <= distance) {
                leadingCharIndex = index;
                leadingCaretEdge = caretEdge;
            } else {
                trailingCharIndex = index;
                trailingCaretEdge = caretEdge;
                break;
            }

            index += next;
        }

        if (leadingCharIndex == -1) {
            // No char is covered by the input distance.
            return fromIndex;
        }

        if (trailingCharIndex == -1) {
            // Whole range is covered by the input distance.
            return toIndex;
        }

        if (distance <= (leadingCaretEdge + trailingCaretEdge) / 2.0f) {
            // Input distance is closer to first edge.
            return leadingCharIndex;
        }

        // Input distance is closer to second edge.
        return trailingCharIndex;
    }

    private @Nullable ClusterRange getClusterRange(int charIndex, @Nullable ClusterRange exclusion) {
        int actualStart = getClusterStart(charIndex);
        int actualEnd = getClusterEnd(charIndex);

        int leadingIndex = getLeadingGlyphIndex(charIndex);
        int trailingIndex = getTrailingGlyphIndex(charIndex);

        ClusterRange cluster = new ClusterRange();
        cluster.actualStart = actualStart;
        cluster.actualEnd = actualEnd;
        cluster.glyphStart = Math.min(leadingIndex, trailingIndex);
        cluster.glyphEnd = Math.max(leadingIndex, trailingIndex) + 1;

        if (exclusion != null) {
            int minStart = Math.min(exclusion.glyphStart, cluster.glyphEnd);
            int maxEnd = Math.max(cluster.glyphStart, exclusion.glyphEnd);

            cluster.glyphStart = (!isBackward ? maxEnd : cluster.glyphStart);
            cluster.glyphEnd = (isBackward ? minStart : cluster.glyphEnd);
        }
        if (cluster.glyphStart < cluster.glyphEnd) {
            return cluster;
        }

        return null;
    }

    private void drawEdgeCluster(@NonNull Renderer renderer, @NonNull Canvas canvas,
                                 @NonNull ClusterRange cluster, float caretBoundary,
                                 int fromIndex, int toIndex) {
        final boolean startClipped = (cluster.actualStart < fromIndex);
        final boolean endClipped = (cluster.actualEnd > toIndex);

        final float clipLeft;
        final float clipRight;

        if (!isRTL()) {
            clipLeft = (startClipped ? getCaretEdge(fromIndex, caretBoundary) : Float.NEGATIVE_INFINITY);
            clipRight = (endClipped ? getCaretEdge(toIndex, caretBoundary) : Float.POSITIVE_INFINITY);
        } else {
            clipRight = (startClipped ? getCaretEdge(fromIndex, caretBoundary) : Float.POSITIVE_INFINITY);
            clipLeft = (endClipped ? getCaretEdge(toIndex, caretBoundary) : Float.NEGATIVE_INFINITY);
        }

        canvas.save();
        canvas.clipRect(clipLeft, Float.NEGATIVE_INFINITY, clipRight, Float.POSITIVE_INFINITY);
        canvas.translate(getLeadingEdge(cluster.actualStart, cluster.actualEnd, caretBoundary), 0.0f);

        renderer.drawGlyphs(canvas,
                            getGlyphIds().subList(cluster.glyphStart, cluster.glyphEnd),
                            getGlyphOffsets().subList(cluster.glyphStart, cluster.glyphEnd),
                            getGlyphAdvances().subList(cluster.glyphStart, cluster.glyphEnd));

        canvas.restore();
    }

    public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas, int fromIndex, int toIndex) {
        renderer.setTypeface(typeface);
        renderer.setTypeSize(typeSize);
        renderer.setWritingDirection(writingDirection);

        final int lastIndex = toIndex - 1;
        final int actualStart = getClusterStart(fromIndex);
        final int actualEnd = getClusterEnd(lastIndex);

        ClusterRange firstCluster = null;
        ClusterRange lastCluster = null;

        if (actualStart < fromIndex) {
            firstCluster = getClusterRange(fromIndex, null);
        }
        if (actualEnd > toIndex) {
            lastCluster = getClusterRange(lastIndex, firstCluster);
        }

        final float caretBoundary = getCaretBoundary(fromIndex, toIndex);
        final int glyphRange[] = getGlyphRangeForChars(actualStart, actualEnd);

        int glyphStart = glyphRange[0];
        int glyphEnd = glyphRange[1];

        int chunkStart = fromIndex;
        int chunkEnd = toIndex;

        if (firstCluster != null) {
            drawEdgeCluster(renderer, canvas, firstCluster, caretBoundary, fromIndex, toIndex);

            // Exclude first cluster characters.
            chunkStart = firstCluster.actualEnd;
            // Exclude first cluster glyphs.
            glyphStart = (!isBackward ? firstCluster.glyphEnd : glyphStart);
            glyphEnd = (isBackward ? firstCluster.glyphStart : glyphEnd);
        }
        if (lastCluster != null) {
            // Exclude last cluster characters.
            chunkEnd = lastCluster.actualStart;
            // Exclude last cluster glyphs.
            glyphEnd = (!isBackward ? lastCluster.glyphStart : glyphEnd);
            glyphStart = (isBackward ? lastCluster.glyphEnd : glyphStart);
        }

        canvas.save();
        canvas.translate(getLeadingEdge(chunkStart, chunkEnd, caretBoundary), 0.0f);

        renderer.drawGlyphs(canvas,
                            getGlyphIds().subList(glyphStart, glyphEnd),
                            getGlyphOffsets().subList(glyphStart, glyphEnd),
                            getGlyphAdvances().subList(glyphStart, glyphEnd));

        canvas.restore();

        if (lastCluster != null) {
            drawEdgeCluster(renderer, canvas, lastCluster, caretBoundary, fromIndex, toIndex);
        }
    }
}
