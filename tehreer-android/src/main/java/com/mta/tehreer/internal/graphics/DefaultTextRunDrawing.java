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

package com.mta.tehreer.internal.graphics;

import android.graphics.Canvas;
import android.text.style.ForegroundColorSpan;
import android.text.style.ScaleXSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.internal.layout.ClusterRange;
import com.mta.tehreer.internal.layout.TextRun;

public final class DefaultTextRunDrawing implements TextRunDrawing {
    private final TextRun textRun;

    public DefaultTextRunDrawing(TextRun textRun) {
        this.textRun = textRun;
    }

    private boolean isRTL() {
        return (textRun.getBidiLevel() & 1) == 1;
    }

    private float getLeadingEdge(int fromIndex, int toIndex, boolean isBackward, float caretBoundary) {
        return textRun.getCaretEdge(!isBackward ? fromIndex : toIndex) - caretBoundary;
    }

    private @Nullable ClusterRange getClusterRange(int charIndex, @Nullable ClusterRange exclusion) {
        final int actualStart = textRun.getClusterStart(charIndex);
        final int actualEnd = textRun.getClusterEnd(charIndex);

        final int leadingIndex = textRun.getLeadingGlyphIndex(charIndex);
        final int trailingIndex = textRun.getTrailingGlyphIndex(charIndex);

        ClusterRange cluster = new ClusterRange();
        cluster.actualStart = actualStart;
        cluster.actualEnd = actualEnd;
        cluster.glyphStart = Math.min(leadingIndex, trailingIndex);
        cluster.glyphEnd = Math.max(leadingIndex, trailingIndex) + 1;

        if (exclusion != null) {
            final int minStart = Math.min(exclusion.glyphStart, cluster.glyphEnd);
            final int maxEnd = Math.max(cluster.glyphStart, exclusion.glyphEnd);
            final boolean isBackward = textRun.isBackward();

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

        final boolean isBackward = textRun.isBackward();
        final boolean isRTL = isRTL();

        if (!isRTL) {
            clipLeft = (startClipped ? textRun.getCaretEdge(fromIndex) - caretBoundary : -Float.MAX_VALUE);
            clipRight = (endClipped ? textRun.getCaretEdge(toIndex) - caretBoundary : Float.MAX_VALUE);
        } else {
            clipRight = (startClipped ? textRun.getCaretEdge(fromIndex) - caretBoundary : Float.MAX_VALUE);
            clipLeft = (endClipped ? textRun.getCaretEdge(toIndex) - caretBoundary : -Float.MAX_VALUE);
        }

        canvas.save();
        canvas.clipRect(clipLeft, -Float.MAX_VALUE, clipRight, Float.MAX_VALUE);
        canvas.translate(getLeadingEdge(cluster.actualStart, cluster.actualEnd, isBackward, caretBoundary), 0.0f);

        renderer.drawGlyphs(canvas,
                            textRun.getGlyphIds().subList(cluster.glyphStart, cluster.glyphEnd),
                            textRun.getGlyphOffsets().subList(cluster.glyphStart, cluster.glyphEnd),
                            textRun.getGlyphAdvances().subList(cluster.glyphStart, cluster.glyphEnd));

        canvas.restore();
    }

    @Override
    public void draw(@NonNull Renderer renderer, @NonNull Canvas canvas) {
        renderer.setScaleX(1.0f);

        int defaultFillColor = renderer.getFillColor();

        for (Object span : textRun.getSpans()) {
            if (span instanceof ForegroundColorSpan) {
                renderer.setFillColor(((ForegroundColorSpan) span).getForegroundColor());
            } else if (span instanceof ScaleXSpan) {
                renderer.setScaleX(((ScaleXSpan) span).getScaleX());
            }
        }

        draw(renderer, canvas, textRun.getCharStart(), textRun.getCharEnd());

        renderer.setFillColor(defaultFillColor);
    }

    private void draw(@NonNull Renderer renderer, @NonNull Canvas canvas, int fromIndex, int toIndex) {
        renderer.setTypeface(textRun.getTypeface());
        renderer.setTypeSize(textRun.getTypeSize());
        renderer.setWritingDirection(textRun.getWritingDirection());

        final int lastIndex = toIndex - 1;
        final int actualStart = textRun.getClusterStart(fromIndex);
        final int actualEnd = textRun.getClusterEnd(lastIndex);

        ClusterRange firstCluster = null;
        ClusterRange lastCluster = null;

        if (actualStart < fromIndex) {
            firstCluster = getClusterRange(fromIndex, null);
        }
        if (actualEnd > toIndex) {
            lastCluster = getClusterRange(lastIndex, firstCluster);
        }

        final boolean isBackward = textRun.isBackward();
        final float caretBoundary = textRun.getCaretBoundary(fromIndex, toIndex);
        final int[] glyphRange = textRun.getGlyphRangeForChars(actualStart, actualEnd);

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
        canvas.translate(getLeadingEdge(chunkStart, chunkEnd, isBackward, caretBoundary), 0.0f);

        renderer.drawGlyphs(canvas,
                            textRun.getGlyphIds().subList(glyphStart, glyphEnd),
                            textRun.getGlyphOffsets().subList(glyphStart, glyphEnd),
                            textRun.getGlyphAdvances().subList(glyphStart, glyphEnd));

        canvas.restore();

        if (lastCluster != null) {
            drawEdgeCluster(renderer, canvas, lastCluster, caretBoundary, fromIndex, toIndex);
        }
    }
}
