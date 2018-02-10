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
import android.text.Spanned;

import com.mta.tehreer.internal.layout.BreakResolver;
import com.mta.tehreer.internal.layout.ParagraphCollection;
import com.mta.tehreer.internal.layout.RunCollection;

import java.util.ArrayList;

class FrameResolver {

    private Spanned mSpanned;
    private ParagraphCollection mBidiParagraphs;
    private RunCollection mIntrinsicRuns;
    private byte[] mBreaks;

    private RectF mFrameRect;
    private TextAlignment mTextAlignment;

    static FrameResolver with(Spanned spanned, ParagraphCollection paragraphs, RunCollection runs, byte[] breaks) {
        return new FrameResolver(spanned, paragraphs, runs, breaks);
    }

    private FrameResolver(Spanned spanned, ParagraphCollection paragraphs, RunCollection runs, byte[] breaks) {
        mSpanned = spanned;
        mBidiParagraphs = paragraphs;
        mIntrinsicRuns = runs;
        mBreaks = breaks;
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

    public ComposedFrame createFrame(int charStart, int charEnd) {
        LineResolver lineResolver = new LineResolver(mSpanned, mBidiParagraphs, mIntrinsicRuns);

        float flushFactor;
        switch (mTextAlignment) {
        case RIGHT:
            flushFactor = 1.0f;
            break;

        case CENTER:
            flushFactor = 0.5f;
            break;

        default:
            flushFactor = 0.0f;
            break;
        }

        float frameWidth = mFrameRect.width();
        float frameBottom = mFrameRect.bottom;

        ArrayList<ComposedLine> frameLines = new ArrayList<>();
        int lineStart = charStart;
        float lineY = mFrameRect.top;

        while (lineStart != charEnd) {
            int lineEnd = BreakResolver.suggestForwardBreak(mSpanned, mIntrinsicRuns, mBreaks, lineStart, charEnd, frameWidth, BreakMode.LINE);
            ComposedLine composedLine = lineResolver.createSimpleLine(lineStart, lineEnd);

            float lineX = composedLine.getFlushPenOffset(flushFactor, frameWidth);
            float lineAscent = composedLine.getAscent();
            float lineHeight = lineAscent + composedLine.getDescent();

            if ((lineY + lineHeight) > frameBottom) {
                break;
            }

            composedLine.setOriginX(mFrameRect.left + lineX);
            composedLine.setOriginY(lineY + lineAscent);

            frameLines.add(composedLine);

            lineStart = lineEnd;
            lineY += lineHeight;
        }

        return new ComposedFrame(charStart, lineStart, frameLines);
    }
}
