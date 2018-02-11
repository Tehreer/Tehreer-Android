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

    private Typesetter mTypesetter;
    private Spanned mSpanned;
    private ParagraphCollection mParagraphs;
    private RunCollection mRuns;
    private byte[] mBreaks;

    private RectF mFrameRect;
    private TextAlignment mTextAlignment;

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
    }

    public RectF getFrameRect() {
        return mFrameRect;
    }

    public void setFrameRect(RectF frameRect) {
        this.mFrameRect = frameRect;
    }

    public TextAlignment getTextAlignment() {
        return mTextAlignment;
    }

    public void setTextAlignment(TextAlignment textAlignment) {
        this.mTextAlignment = textAlignment;
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
            }
        }

        return 0.0f;
    }

    public ComposedFrame createFrame(int charStart, int charEnd) {
        FrameFiller frameFiller = new FrameFiller();
        int paragraphIndex = mParagraphs.binarySearch(charStart);
        int segmentEnd = charStart;

        // Iterate over all paragraphs in provided range.
        do {
            BidiParagraph paragraph = mParagraphs.get(paragraphIndex);
            segmentEnd = Math.min(charEnd, paragraph.getCharEnd());

            // Get the spans of this paragraph.
            int spanEnd = mSpanned.nextSpanTransition(charStart, segmentEnd, ParagraphStyle.class);
            ParagraphStyle[] spans = mSpanned.getSpans(charStart, spanEnd, ParagraphStyle.class);

            Layout.Alignment alignment = null;

            // Get the top most alignment.
            for (int n = spans.length - 1; n >= 0; n--) {
                if (spans[n] instanceof AlignmentSpan) {
                    alignment = ((AlignmentSpan) spans[n]).getAlignment();
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

        return new ComposedFrame(charStart, segmentEnd, frameFiller.frameLines);
    }

    private class FrameFiller {

        final LineResolver lineResolver = new LineResolver(mSpanned, mParagraphs, mRuns);

        final List<ComposedLine> frameLines = new ArrayList<>();
        final float frameWidth = mFrameRect.width();
        final float frameLeft = mFrameRect.left;
        final float frameBottom = mFrameRect.bottom;

        float lineY = mFrameRect.top;
        boolean filled = false;

        void addParagraphLines(int charStart, int charEnd, float flushFactor) {
            int lineStart = charStart;
            while (lineStart != charEnd) {
                int lineEnd = BreakResolver.suggestForwardBreak(mSpanned, mRuns, mBreaks, lineStart, charEnd, frameWidth, BreakMode.LINE);
                ComposedLine composedLine = lineResolver.createSimpleLine(lineStart, lineEnd);

                float lineX = composedLine.getFlushPenOffset(flushFactor, frameWidth);
                float lineAscent = composedLine.getAscent();
                float lineHeight = lineAscent + composedLine.getDescent();

                if ((lineY + lineHeight) > frameBottom) {
                    filled = true;
                    return;
                }

                composedLine.setOriginX(frameLeft + lineX);
                composedLine.setOriginY(lineY + lineAscent);

                frameLines.add(composedLine);

                lineStart = lineEnd;
                lineY += lineHeight;
            }
        }
    }
}
