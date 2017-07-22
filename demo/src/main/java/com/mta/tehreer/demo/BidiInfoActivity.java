/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

package com.mta.tehreer.demo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.SparseIntArray;
import android.widget.TextView;

import com.mta.tehreer.unicode.BaseDirection;
import com.mta.tehreer.unicode.BidiAlgorithm;
import com.mta.tehreer.unicode.BidiLine;
import com.mta.tehreer.unicode.BidiPair;
import com.mta.tehreer.unicode.BidiParagraph;
import com.mta.tehreer.unicode.BidiRun;

public class BidiInfoActivity extends AppCompatActivity {

    public static final String BIDI_TEXT = "bidi_text";

    private static final char LRI = '\u2066';
    private static final char RLI = '\u2067';
    private static final char PDI = '\u2069';

    private String mBidiText;
    private float mDensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bidi_info);

        Intent intent = getIntent();
        mBidiText = String.valueOf(intent.getCharSequenceExtra(BIDI_TEXT));
        mDensity = getResources().getDisplayMetrics().scaledDensity;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        SpannableStringBuilder builder = new SpannableStringBuilder();
        writeBidiText(builder);

        TextView bidiTextView = (TextView) findViewById(R.id.text_view_bidi);
        bidiTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        bidiTextView.setText(builder);
    }

    private Object[] spansFirstHeading() {
        return new Object[] {
            new AbsoluteSizeSpan((int) (20.0f * mDensity + 0.5f)),
            new StyleSpan(Typeface.BOLD)
        };
    }

    private Object[] spansSecondHeading() {
        return new Object[] {
            new AbsoluteSizeSpan((int) (16.0f * mDensity + 0.5f)),
            new StyleSpan(Typeface.BOLD)
        };
    }

    private Object[] spansInlineHeading() {
        return new Object[] {
            new StyleSpan(Typeface.ITALIC),
            new UnderlineSpan()
        };
    }

    private BidiInfoActivity appendText(SpannableStringBuilder builder, CharSequence text) {
        return appendText(builder, text, null);
    }

    private BidiInfoActivity appendText(SpannableStringBuilder builder, CharSequence text, Object[] spans) {
        int start = builder.length();
        int end = start + text.length();

        builder.append(text);

        if (spans != null) {
            for (Object s : spans) {
                builder.setSpan(s, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return this;
    }

    private void writeBidiText(SpannableStringBuilder builder) {
        builder.setSpan(new AbsoluteSizeSpan((int) (16.0f * mDensity + 0.5f)), 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        BidiAlgorithm algorithm = null;

        try {
            algorithm = new BidiAlgorithm(mBidiText);
            writeAlgorithmText(builder, algorithm);
        } finally {
            if (algorithm != null) {
                algorithm.dispose();
            }
        }
    }

    private void writeAlgorithmText(SpannableStringBuilder builder, BidiAlgorithm algorithm) {
        int paragraphIndex = 1;
        int paragraphStart = 0;
        int suggestedEnd = mBidiText.length();

        while (paragraphStart != suggestedEnd) {
            BidiParagraph paragraph = null;

            try {
                paragraph = algorithm.createParagraph(paragraphStart, suggestedEnd, BaseDirection.DEFAULT_LEFT_TO_RIGHT);
                writeParagraphText(builder, paragraph, paragraphIndex);

                paragraphIndex++;
                paragraphStart = paragraph.getCharEnd();
            } finally {
                if (paragraph != null) {
                    paragraph.dispose();
                }
            }
        }
    }

    private void writeParagraphText(SpannableStringBuilder builder, BidiParagraph paragraph, int index) {
        int paragraphStart = paragraph.getCharStart();
        int paragraphEnd = paragraph.getCharEnd();
        int paragraphLength = paragraphEnd - paragraphStart;
        String paragraphText = ((paragraph.getBaseLevel() & 1) == 1 ? RLI : LRI)
                             + mBidiText.substring(paragraphStart, paragraphEnd) + PDI;

        appendText(builder, "Paragraph " + index + "\n", spansFirstHeading());
        appendText(builder, "Paragraph Text:", spansInlineHeading());
        appendText(builder, " “" + paragraphText + "”\n");
        appendText(builder, "Paragraph Range:", spansInlineHeading());
        appendText(builder, " Start=" + paragraphStart + " Length=" + paragraphLength + "\n");
        appendText(builder, "Base Level:", spansInlineHeading());
        appendText(builder, " " + paragraph.getBaseLevel() + "\n\n");

        int counter = 1;
        for (BidiRun bidiRun : paragraph.getLogicalRuns()) {
            writeRunText(builder, bidiRun, counter);
            counter++;
        }

        BidiLine line = null;

        try {
            line = paragraph.createLine(paragraphStart, paragraphEnd);
            writeLineText(builder, line);
            writeMirrorsText(builder, line);
        } finally {
            if (line != null) {
                line.dispose();
            }
        }

        appendText(builder, "\n");
    }

    private void writeRunText(SpannableStringBuilder builder, BidiRun run, int index) {
        int runStart = run.charStart;
        int runEnd = run.charEnd;
        int runLength = runEnd - runStart;
        String runText = (run.isRightToLeft() ? RLI : LRI)
                       + mBidiText.substring(runStart, runEnd) + PDI;

        appendText(builder, "Run " + index + "\n", spansSecondHeading());
        appendText(builder, "Run Text:", spansInlineHeading());
        appendText(builder, " “" + runText + "”\n");
        appendText(builder, "Run Range:", spansInlineHeading());
        appendText(builder, " Start=" + runStart + " Length=" + runLength + "\n");
        appendText(builder, "Embedding Level:", spansInlineHeading());
        appendText(builder, " " + run.embeddingLevel + "\n\n");
    }

    private void writeLineText(SpannableStringBuilder builder, BidiLine line) {
        SparseIntArray visualMap = new SparseIntArray();

        int counter = 1;
        for (BidiRun bidiRun : line.getVisualRuns()) {
            visualMap.put(bidiRun.charStart, counter);
            counter++;
        }

        int runCount = visualMap.size();
        if (runCount > 0) {
            appendText(builder, "Visual Order\n", spansSecondHeading());

            for (int i = 0; i < runCount; i++) {
                int runIndex = visualMap.valueAt(i);
                appendText(builder, "<Run " + runIndex + ">", spansInlineHeading());
                appendText(builder, " ");
            }

            appendText(builder, "\n\n");
        }
    }

    private void writeMirrorsText(SpannableStringBuilder builder, BidiLine line) {
        boolean wroteHeading = false;

        for (BidiPair bidiPair : line.getMirroringPairs()) {
            if (!wroteHeading) {
                wroteHeading = true;
                appendText(builder, "Mirrors\n", spansSecondHeading());
            }

            appendText(builder, "*", spansInlineHeading());
            appendText(builder, " Index=" + bidiPair.charIndex);
            appendText(builder, " Character=‘" + String.valueOf(Character.toChars(bidiPair.actualCodePoint)) + "’");
            appendText(builder, " Mirror=‘" + String.valueOf(Character.toChars(bidiPair.pairingCodePoint)) + "’\n");
        }

        if (wroteHeading) {
            appendText(builder, "\n");
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
}
