/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.SparseIntArray;
import android.widget.TextView;

import com.mta.tehreer.bidi.BaseDirection;
import com.mta.tehreer.bidi.BidiAlgorithm;
import com.mta.tehreer.bidi.BidiLine;
import com.mta.tehreer.bidi.BidiPair;
import com.mta.tehreer.bidi.BidiPairConsumer;
import com.mta.tehreer.bidi.BidiParagraph;
import com.mta.tehreer.bidi.BidiRun;
import com.mta.tehreer.bidi.BidiRunConsumer;

public class BidiInfoActivity extends AppCompatActivity {

    public static final String BIDI_TEXT = "bidi_text";

    private static final char LRI = '\u2066';
    private static final char RLI = '\u2067';
    private static final char PDI = '\u2069';

    private static Object[] spansFirstHeading() {
        return new Object[] {
                new RelativeSizeSpan(1.32f),
                new StyleSpan(Typeface.BOLD)
        };
    }

    private static Object[] spansSecondHeading() {
        return new Object[] {
                new RelativeSizeSpan(1.16f),
                new StyleSpan(Typeface.BOLD)
        };
    }

    private static Object[] spansInlineHeading() {
        return new Object[] {
                new BackgroundColorSpan(0xFFE0E0E0),
                new UnderlineSpan()
        };
    };

    private String mBidiText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bidi_info);

        Intent intent = getIntent();
        mBidiText = String.valueOf(intent.getCharSequenceExtra(BIDI_TEXT));

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

    private BidiInfoActivity appendText(final SpannableStringBuilder builder, CharSequence text) {
        return appendText(builder, text, null);
    }

    private BidiInfoActivity appendText(final SpannableStringBuilder builder, CharSequence text, Object[] spans) {
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

    private void writeBidiText(final SpannableStringBuilder builder) {
        builder.setSpan(new AbsoluteSizeSpan(16, true), 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        BidiAlgorithm algorithm = new BidiAlgorithm(mBidiText);
        writeAlgorithmText(builder, algorithm);
        algorithm.dispose();
    }

    private void writeAlgorithmText(final SpannableStringBuilder builder, BidiAlgorithm algorithm) {
        int paragraphIndex = 1;
        int paragraphStart = 0;
        int suggestedEnd = mBidiText.length();

        while (paragraphStart != suggestedEnd) {
            BidiParagraph paragraph = algorithm.createParagraph(paragraphStart, suggestedEnd, BaseDirection.DEFAULT_LEFT_TO_RIGHT);
            writeParagraphText(builder, paragraph, paragraphIndex);

            paragraphIndex++;
            paragraphStart = paragraph.getCharEnd();
            paragraph.dispose();
        }
    }

    private void writeParagraphText(final SpannableStringBuilder builder, BidiParagraph paragraph, int index) {
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

        final int[] counter = { 1 };

        paragraph.iterateRuns(new BidiRunConsumer() {
            @Override
            public void accept(BidiRun bidiRun) {
                writeRunText(builder, bidiRun, counter[0]);
                counter[0]++;
            }
        });

        BidiLine line = paragraph.createLine(paragraphStart, paragraphEnd);
        writeLineText(builder, line);
        writeMirrorsText(builder, line);
        line.dispose();

        appendText(builder, "\n");
    }

    private void writeRunText(final SpannableStringBuilder builder, BidiRun run, int index) {
        int runStart = run.getCharStart();
        int runEnd = run.getCharEnd();
        int runLength = runEnd - runStart;
        String runText = (run.isRightToLeft() ? RLI : LRI)
                       + mBidiText.substring(runStart, runEnd) + PDI;

        appendText(builder, "Run " + index + "\n", spansSecondHeading());
        appendText(builder, "Run Text:", spansInlineHeading());
        appendText(builder, " “" + runText + "”\n");
        appendText(builder, "Run Range:", spansInlineHeading());
        appendText(builder, " Start=" + runStart + " Length=" + runLength + "\n");
        appendText(builder, "Embedding Level:", spansInlineHeading());
        appendText(builder, " " + run.getEmbeddingLevel() + "\n\n");
    }

    private void writeLineText(final SpannableStringBuilder builder, BidiLine line) {
        final SparseIntArray visualMap = new SparseIntArray();
        final int[] counter = { 1 };

        line.iterateVisualRuns(new BidiRunConsumer() {
            @Override
            public void accept(BidiRun bidiRun) {
                visualMap.put(bidiRun.getCharStart(), counter[0]);
                counter[0]++;
            }
        });

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

    private void writeMirrorsText(final SpannableStringBuilder builder, BidiLine line) {
        final boolean[] wroteHeading = { false };

        line.iterateMirrors(new BidiPairConsumer() {
            @Override
            public void accept(BidiPair bidiPair) {
                if (!wroteHeading[0]) {
                    wroteHeading[0] = true;
                    appendText(builder, "Mirrors\n", spansSecondHeading());
                }

                int actualCodePoint = mBidiText.codePointAt(bidiPair.getCharIndex());
                int pairingCodePoint = bidiPair.getPairingCodePoint();

                appendText(builder, "*", spansInlineHeading());
                appendText(builder, " Index=" + bidiPair.getCharIndex());
                appendText(builder, " Character=‘" + String.valueOf(Character.toChars(actualCodePoint)) + "’");
                appendText(builder, " Mirror=‘" + String.valueOf(Character.toChars(pairingCodePoint)) + "’\n");
            }
        });

        if (wroteHeading[0]) {
            appendText(builder, "\n");
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
}
