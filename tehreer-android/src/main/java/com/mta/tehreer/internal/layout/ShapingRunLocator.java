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

package com.mta.tehreer.internal.layout;

import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.ReplacementSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;

import com.mta.tehreer.graphics.TypeFamily;
import com.mta.tehreer.graphics.TypeSlope;
import com.mta.tehreer.graphics.TypeWeight;
import com.mta.tehreer.graphics.TypeWidth;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;
import com.mta.tehreer.layout.style.TypeSizeSpan;

import java.util.List;

public class ShapingRunLocator {

    private final Spanned spanned;
    private final ShapingRun initial;

    private int mLimit;
    private ShapingRun mCurrent;
    private ShapingRun mNext;

    private static class ShapingRun {
        int start;
        int end;

        ReplacementSpan replacement;

        Typeface typeface;
        TypeWeight typeWeight;
        TypeSlope typeSlope;
        float typeSize;
        float scaleX;
    }

    public ShapingRunLocator(Spanned spanned, List<Object> defaultSpans) {
        this.spanned = spanned;
        this.initial = resolveInitial(defaultSpans.toArray());
    }

    private static ShapingRun resolveInitial(Object[] spans) {
        ShapingRun shapingRun = new ShapingRun();
        shapingRun.typeWeight = TypeWeight.REGULAR;
        shapingRun.typeSlope = TypeSlope.PLAIN;
        shapingRun.typeSize = 16.0f;
        shapingRun.scaleX = 1.0f;

        resolveSpans(shapingRun, spans);

        return shapingRun;
    }

    private ShapingRun resolveRun(int runStart) {
        if (runStart < mLimit) {
            int runEnd = spanned.nextSpanTransition(runStart, mLimit, MetricAffectingSpan.class);
            MetricAffectingSpan[] spans = spanned.getSpans(runStart, runEnd, MetricAffectingSpan.class);

            ShapingRun shapingRun = new ShapingRun();
            shapingRun.start = runStart;
            shapingRun.end = runEnd;
            shapingRun.typeface = initial.typeface;
            shapingRun.typeWeight = initial.typeWeight;
            shapingRun.typeSlope = initial.typeSlope;
            shapingRun.typeSize = initial.typeSize;
            shapingRun.scaleX = initial.scaleX;

            resolveSpans(shapingRun, spans);

            return shapingRun;
        }

        return null;
    }

    private static void resolveSpans(ShapingRun shapingRun, Object[] spans) {
        for (Object span : spans) {
            if (span instanceof com.mta.tehreer.layout.style.TypefaceSpan) {
                com.mta.tehreer.layout.style.TypefaceSpan typefaceSpan = (com.mta.tehreer.layout.style.TypefaceSpan) span;
                shapingRun.typeface = typefaceSpan.getTypeface();
                shapingRun.typeWeight = shapingRun.typeface.getWeight();
                shapingRun.typeSlope = shapingRun.typeface.getSlope();
            } else if (span instanceof TypeSizeSpan) {
                TypeSizeSpan typeSizeSpan = (TypeSizeSpan) span;
                shapingRun.typeSize = typeSizeSpan.getSize();
            } else if (span instanceof TypefaceSpan) {
                TypefaceSpan typefaceSpan = (TypefaceSpan) span;
                resolveTypeface(shapingRun, typefaceSpan.getFamily(), TypeWidth.NORMAL);
            } else if (span instanceof AbsoluteSizeSpan) {
                AbsoluteSizeSpan absoluteSizeSpan = (AbsoluteSizeSpan) span;
                shapingRun.typeSize = absoluteSizeSpan.getSize();
            } else if (span instanceof RelativeSizeSpan) {
                RelativeSizeSpan relativeSizeSpan = (RelativeSizeSpan) span;
                shapingRun.typeSize *= relativeSizeSpan.getSizeChange();
            } else if (span instanceof StyleSpan) {
                StyleSpan styleSpan = (StyleSpan) span;
                resolveStyle(shapingRun, styleSpan.getStyle());
                updateTypeface(shapingRun);
            } else if (span instanceof TextAppearanceSpan) {
                TextAppearanceSpan appearanceSpan = (TextAppearanceSpan) span;
                shapingRun.typeSize = appearanceSpan.getTextSize();
                resolveStyle(shapingRun, appearanceSpan.getTextStyle());

                String familyName = appearanceSpan.getFamily();
                if (familyName != null) {
                    resolveTypeface(shapingRun, familyName, TypeWidth.NORMAL);
                } else {
                    updateTypeface(shapingRun);
                }
            } else if (span instanceof ReplacementSpan) {
                shapingRun.replacement = (ReplacementSpan) span;
            }
        }

        if (shapingRun.typeSize < 0.0f) {
            shapingRun.typeSize = 0.0f;
        }
    }

    private static void resolveStyle(ShapingRun shapingRun, int newStyle) {
        switch (newStyle) {
        case android.graphics.Typeface.NORMAL:
            shapingRun.typeWeight = TypeWeight.REGULAR;
            break;

        case android.graphics.Typeface.BOLD:
            shapingRun.typeWeight = TypeWeight.BOLD;
            break;

        case android.graphics.Typeface.ITALIC:
            shapingRun.typeSlope = TypeSlope.ITALIC;
            break;

        case android.graphics.Typeface.BOLD_ITALIC:
            shapingRun.typeWeight = TypeWeight.BOLD;
            shapingRun.typeSlope = TypeSlope.ITALIC;
            break;
        }
    }

    private static void resolveTypeface(ShapingRun shapingRun, String familyName, TypeWidth typeWidth) {
        TypeFamily typeFamily = TypefaceManager.getTypefaceFamily(familyName);
        if (typeFamily != null) {
            shapingRun.typeface = typeFamily.getTypefaceByStyle(typeWidth, shapingRun.typeWeight, shapingRun.typeSlope);
        } else {
            shapingRun.typeface = null;
        }
    }

    private static void updateTypeface(ShapingRun shapingRun) {
        Typeface typeface = shapingRun.typeface;
        if (typeface != null) {
            resolveTypeface(shapingRun, typeface.getFamilyName(), typeface.getWidth());
        }
    }

    public void reset(int charStart, int charEnd) {
        mLimit = charEnd;
        mCurrent = null;
        mNext = resolveRun(charStart);
    }

    public boolean moveNext() {
        if (mNext != null) {
            ShapingRun current = mNext;
            ShapingRun next;

            // Merge runs of similar style.
            while ((next = resolveRun(current.end)) != null) {
                if (current.replacement == next.replacement
                        && current.typeface == next.typeface
                        && Float.compare(current.typeSize, next.typeSize) == 0
                        && Float.compare(current.scaleX, next.scaleX) == 0) {
                    current.end = next.end;
                } else {
                    break;
                }
            }

            mCurrent = current;
            mNext = next;
            return true;
        }

        return false;
    }

    public int getRunStart() {
        return mCurrent.start;
    }

    public int getRunEnd() {
        return mCurrent.end;
    }

    public ReplacementSpan getReplacement() {
        return mCurrent.replacement;
    }

    public Typeface getTypeface() {
        return mCurrent.typeface;
    }

    public float getTypeSize() {
        return mCurrent.typeSize;
    }
}
