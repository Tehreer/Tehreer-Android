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

package com.mta.tehreer.internal.text;

import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;

import com.mta.tehreer.graphics.TypeFamily;
import com.mta.tehreer.graphics.TypeSlope;
import com.mta.tehreer.graphics.TypeWeight;
import com.mta.tehreer.graphics.TypeWidth;
import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;

import java.util.Arrays;
import java.util.List;

public class ShapingRunIterator {

    private final List<TypeWidth> mWidthOrder = Arrays.asList(TypeWidth.values());
    private final List<TypeWeight> mWeightOrder = Arrays.asList(TypeWeight.values());

    private final Spanned mSpanned;
    private ShapingRun mCurrent;
    private ShapingRun mNext;

    private static class ShapingRun {
        int start;
        int end;

        Typeface typeface;
        TypeWeight typeWeight;
        TypeSlope typeSlope;
        float typeSize;
        float scaleX;
    }

    public ShapingRunIterator(Spanned spanned) {
        mSpanned = spanned;
        mNext = findRun(0);
    }

    private static int nearestIndex(int matchIndex, int loopIndex, int lastIndex) {
        int division = loopIndex / 2;
        int remainder = loopIndex % 2;
        int margin = division + remainder;

        if (margin > matchIndex) {
            return loopIndex;
        }

        if (margin + matchIndex > lastIndex) {
            return lastIndex - loopIndex;
        }

        if (remainder == 0) {
            return matchIndex + margin;
        }

        return matchIndex - margin;
    }

    private static Typeface findTypeface(TypeFamily typeFamily, TypeWidth typeWidth,
                                         TypeWeight typeWeight, TypeSlope typeSlope) {
        for (Typeface typeface : typeFamily.getTypefaces()) {
            if (typeface.getWidth() == typeWidth
                    && typeface.getWeight() == typeWeight
                    && typeface.getSlope() == typeSlope) {
                return typeface;
            }
        }

        return null;
    }

    private Typeface getTypefaceByStyle(TypeFamily typeFamily, TypeWidth typeWidth,
                                        TypeWeight typeWeight, TypeSlope typeSlope) {
        int widthIndex = mWidthOrder.indexOf(typeWidth);
        int widthLast = mWidthOrder.size() - 1;

        int weightIndex = mWeightOrder.indexOf(typeWeight);
        int weightLast = mWeightOrder.size() - 1;

        for (int i = 0; i <= widthLast; i++) {
            int widthNearest = nearestIndex(widthIndex, i, widthLast);
            TypeWidth widthValue = mWidthOrder.get(widthNearest);

            for (int j = 0; j <= weightLast; j++) {
                int weightNearest = nearestIndex(weightIndex, i, weightLast);
                TypeWeight weightValue = mWeightOrder.get(weightNearest);
                TypeSlope slopeValue = typeSlope;

                for (int k = 0; k <= 1; k++) {
                    Typeface typeface = findTypeface(typeFamily, widthValue, weightValue, slopeValue);
                    if (typeface != null) {
                        return typeface;
                    }

                    if (slopeValue == TypeSlope.ITALIC) {
                        slopeValue = TypeSlope.OBLIQUE;
                    } else if (slopeValue == TypeSlope.OBLIQUE) {
                        slopeValue = TypeSlope.ITALIC;
                    } else {
                        break;
                    }
                }
            }
        }

        return null;
    }

    private Typeface getTypefaceByStyle(String familyName, TypeWidth typeWidth,
                                        TypeWeight typeWeight, TypeSlope typeSlope) {
        TypeFamily typeFamily = TypefaceManager.getTypefaceFamily(familyName);
        if (typeFamily != null) {
            return getTypefaceByStyle(typeFamily, typeWidth, typeWeight, typeSlope);
        }

        return null;
    }

    private void mergeStyle(ShapingRun shapingRun, int newStyle) {
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

    private void resolveTypeface(ShapingRun shapingRun, String familyName, TypeWidth typeWidth) {
        shapingRun.typeface = getTypefaceByStyle(familyName, typeWidth,
                                                 shapingRun.typeWeight, shapingRun.typeSlope);
    }

    private void updateTypeface(ShapingRun shapingRun) {
        Typeface typeface = shapingRun.typeface;
        if (typeface != null) {
            resolveTypeface(shapingRun, typeface.getFamilyName(), typeface.getWidth());
        }
    }

    private ShapingRun findRun(int runStart) {
        int length = mSpanned.length();
        if (runStart < length) {
            int runEnd = mSpanned.nextSpanTransition(runStart, length, MetricAffectingSpan.class);
            MetricAffectingSpan[] spanObjects = mSpanned.getSpans(runStart, runEnd, MetricAffectingSpan.class);

            ShapingRun shapingRun = new ShapingRun();
            shapingRun.start = runStart;
            shapingRun.end = runEnd;
            shapingRun.typeWeight = TypeWeight.REGULAR;
            shapingRun.typeSlope = TypeSlope.PLAIN;

            for (MetricAffectingSpan span : spanObjects) {
                if (span instanceof TypefaceSpan) {
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
                    mergeStyle(shapingRun, styleSpan.getStyle());
                    updateTypeface(shapingRun);
                } else if (span instanceof TextAppearanceSpan) {
                    TextAppearanceSpan appearanceSpan = (TextAppearanceSpan) span;
                    shapingRun.typeSize = appearanceSpan.getTextSize();
                    mergeStyle(shapingRun, appearanceSpan.getTextStyle());

                    String familyName = appearanceSpan.getFamily();
                    if (familyName != null) {
                        resolveTypeface(shapingRun, familyName, TypeWidth.NORMAL);
                    } else {
                        updateTypeface(shapingRun);
                    }
                }
            }

            return shapingRun;
        }

        return null;
    }

    public boolean moveNext() {
        if (mNext != null) {
            ShapingRun current = mNext;
            ShapingRun next;

            while ((next = findRun(current.end)) != null) {
                if (current.typeface == next.typeface
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

    public Typeface getTypeface() {
        return mCurrent.typeface;
    }

    public float getTypeSize() {
        return mCurrent.typeSize;
    }
}
