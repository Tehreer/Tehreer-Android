/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.layout;

import android.graphics.RectF;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.AlignmentSpan;
import android.text.style.ParagraphStyle;

import com.mta.tehreer.internal.layout.BreakResolver;
import com.mta.tehreer.internal.layout.ParagraphCollection;
import com.mta.tehreer.internal.layout.RunCollection;
import com.mta.tehreer.unicode.BidiParagraph;

import java.util.ArrayList;
import java.util.List;

public class FrameResolver {

    private LineResolver mLineResolver = new LineResolver();
    private Typesetter mTypesetter;
    private Spanned mSpanned;
    private ParagraphCollection mParagraphs;
    private RunCollection mRuns;
    private byte[] mBreaks;

    private RectF mFrameRect = new RectF(0, 0, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    private TextAlignment mTextAlignment = TextAlignment.INTRINSIC;
    private VerticalAlignment mVerticalAlignment = VerticalAlignment.TOP;
    private BreakMode mTruncationMode = BreakMode.LINE;
    private TruncationPlace mTruncationPlace = TruncationPlace.END;
    private int mMaxLines = 0;

    public FrameResolver() {
    }

    public Typesetter getTypesetter() {
        return mTypesetter;
    }

    public void setTypesetter(Typesetter typesetter) {
        mTypesetter = typesetter;
        mSpanned = typesetter.getSpanned();
        mParagraphs = typesetter.getParagraphs();
        mRuns = typesetter.getRuns();
        mBreaks = typesetter.getBreaks();
        mLineResolver.reset(mSpanned, mParagraphs, mRuns);
    }

    public RectF getFrameRect() {
        return new RectF(mFrameRect);
    }

    public void setFrameRect(RectF frameRect) {
        if (frameRect == null) {
            throw new NullPointerException("Frame rect is null");
        }

        mFrameRect.set(frameRect);
    }

    public TextAlignment getTextAlignment() {
        return mTextAlignment;
    }

    public void setTextAlignment(TextAlignment textAlignment) {
        mTextAlignment = textAlignment;
    }

    public VerticalAlignment getVerticalAlignment() {
        return mVerticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        mVerticalAlignment = verticalAlignment;
    }

    public BreakMode getTruncationMode() {
        return mTruncationMode;
    }

    public void setTruncationMode(BreakMode truncationMode) {
        mTruncationMode = truncationMode;
    }

    public TruncationPlace getTruncationPlace() {
        return mTruncationPlace;
    }

    public void setTruncationPlace(TruncationPlace truncationPlace) {
        mTruncationPlace = truncationPlace;
    }

    public int getMaxLines() {
        return mMaxLines;
    }

    public void setMaxLines(int maxLines) {
        mMaxLines = maxLines;
    }

    private float getFlushFactor(Layout.Alignment layoutAlignment, byte paragraphLevel) {
        boolean isLTR = ((paragraphLevel & 1) == 0);

        if (layoutAlignment != null) {
            switch (layoutAlignment) {
            case ALIGN_NORMAL:
                return (isLTR ? 0.0f : 1.0f);

            case ALIGN_CENTER:
                return 0.5f;

            case ALIGN_OPPOSITE:
                return (isLTR ? 1.0f : 0.0f);
            }
        } else if (mTextAlignment != null) {
            switch (mTextAlignment) {
            case LEFT:
                return 0.0f;

            case RIGHT:
                return 1.0f;

            case CENTER:
                return 0.5f;

            case INTRINSIC:
                return (isLTR ? 0.0f : 1.0f);

            case EXTRINSIC:
                return (isLTR ? 1.0f : 0.0f);
            }
        }

        return 0.0f;
    }

    public ComposedFrame createFrame(int charStart, int charEnd) {
        FrameFiller frameFiller = new FrameFiller();
        int paragraphIndex = mParagraphs.binarySearch(charStart);
        int segmentEnd;

        // Iterate over all paragraphs in provided range.
        do {
            BidiParagraph paragraph = mParagraphs.get(paragraphIndex);
            segmentEnd = Math.min(charEnd, paragraph.getCharEnd());

            // Get the spans of this paragraph.
            int spanEnd = mSpanned.nextSpanTransition(charStart, segmentEnd, ParagraphStyle.class);
            ParagraphStyle[] spans = mSpanned.getSpans(charStart, spanEnd, ParagraphStyle.class);

            Layout.Alignment alignment = null;

            // Get the top most alignment.
            for (int i = spans.length - 1; i >= 0; i--) {
                if (spans[i] instanceof AlignmentSpan) {
                    alignment = ((AlignmentSpan) spans[i]).getAlignment();
                    break;
                }
            }

            // Calculate flush factor from alignment.
            float flushFactor = getFlushFactor(alignment, paragraph.getBaseLevel());

            // Fill the lines of this paragraph.
            frameFiller.addParagraphLines(charStart, segmentEnd, flushFactor);

            if (frameFiller.filled) {
                break;
            }

            charStart = segmentEnd;
            paragraphIndex++;
        } while (charStart < charEnd);

        frameFiller.handleTruncation(charEnd);

        return new ComposedFrame(charStart, segmentEnd, frameFiller.frameLines);
    }

    private class FrameFiller {

        final List<ComposedLine> frameLines = new ArrayList<>();
        final float frameWidth = mFrameRect.width();
        final float frameLeft = mFrameRect.left;
        final float frameBottom = mFrameRect.bottom;

        float lineY = mFrameRect.top;
        boolean filled = false;

        float lastFlushFactor = 0.0f;

        void addParagraphLines(int charStart, int charEnd, float flushFactor) {
            int maxLines = (mMaxLines > 0 ? mMaxLines : Integer.MAX_VALUE);

            int lineStart = charStart;
            while (lineStart != charEnd) {
                int lineEnd = BreakResolver.suggestForwardBreak(mSpanned, mRuns, mBreaks, lineStart, charEnd, frameWidth, BreakMode.LINE);
                ComposedLine composedLine = mLineResolver.createSimpleLine(lineStart, lineEnd);

                float lineX = composedLine.getFlushPenOffset(flushFactor, frameWidth);
                float lineAscent = composedLine.getAscent();
                float lineHeight = lineAscent + composedLine.getDescent();

                // Make sure that at least one line is added even if frame is smaller in height.
                if ((lineY + lineHeight) > frameBottom && frameLines.size() > 1) {
                    filled = true;
                    return;
                }

                composedLine.setOriginX(frameLeft + lineX);
                composedLine.setOriginY(lineY + lineAscent);

                frameLines.add(composedLine);
                lastFlushFactor = flushFactor;

                // Stop the filling process if maximum lines have been added.
                if (frameLines.size() == maxLines) {
                    filled = true;
                    return;
                }

                lineStart = lineEnd;
                lineY += lineHeight;
            }
        }

        void handleTruncation(int frameEnd) {
            if (mTruncationPlace != null) {
                int lastIndex = frameLines.size() - 1;
                ComposedLine lastLine = frameLines.get(lastIndex);

                // Create the truncated line.
                ComposedLine truncatedLine = mTypesetter.createTruncatedLine(lastLine.getCharStart(), frameEnd, frameWidth, mTruncationMode, mTruncationPlace);
                float lineX = truncatedLine.getFlushPenOffset(lastFlushFactor, frameWidth);
                float lineAscent = truncatedLine.getAscent();

                truncatedLine.setOriginX(frameLeft + lineX);
                truncatedLine.setOriginY(lineY + lineAscent);

                // Replace the last line with truncated one.
                frameLines.set(lastIndex, truncatedLine);
            }
        }
    }
}
