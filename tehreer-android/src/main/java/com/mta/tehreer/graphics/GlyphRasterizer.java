/*
 * Copyright (C) 2016-2021 Muhammad Tayyab Akram
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

package com.mta.tehreer.graphics;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.mta.tehreer.Disposable;
import com.mta.tehreer.internal.JniBridge;

final class GlyphRasterizer implements Disposable {
    static {
        JniBridge.loadLibrary();
    }

	long nativeRasterizer;

	GlyphRasterizer(@NonNull GlyphStrike strike) {
	    nativeRasterizer = nCreate(strike.typeface.nativeTypeface,
                                   strike.pixelWidth, strike.pixelHeight,
                                   0x10000, -strike.skewX, 0, 0x10000);
	}

	void loadBitmap(@NonNull Glyph glyph) {
	    nLoadBitmap(nativeRasterizer, glyph);
	}

    void loadColorBitmap(@NonNull Glyph glyph, @ColorInt int foregroundColor) {
        nLoadColorBitmap(nativeRasterizer, glyph, foregroundColor);
    }

    void loadOutline(@NonNull Glyph glyph) {
        nLoadOutline(nativeRasterizer, glyph);
    }

    void loadPath(@NonNull Glyph glyph) {
        nLoadPath(nativeRasterizer, glyph);
    }

    @NonNull Glyph strokeGlyph(@NonNull Glyph glyph, int lineRadius,
                               @GlyphAttributes.LineCap int lineCap,
                               @GlyphAttributes.LineJoin int lineJoin,
                               int miterLimit) {
        return nStrokeGlyph(nativeRasterizer, glyph, lineRadius, lineCap, lineJoin, miterLimit);
    }

    @Override
    public void dispose() {
        nDispose(nativeRasterizer);
    }

	private static native long nCreate(long nativeTypeface, int pixelWidth, int pixelHeight,
                                       int transformXX, int transformXY, int transformYX, int transformYY);
    private static native void nDispose(long nativeRasterizer);

    private static native void nLoadBitmap(long nativeRasterizer, @NonNull Glyph glyph);
    private static native void nLoadColorBitmap(long nativeRasterizer, @NonNull Glyph glyph, int foregroundColor);
    private static native void nLoadOutline(long nativeRasterizer, @NonNull Glyph glyph);
    private static native void nLoadPath(long nativeRasterizer, @NonNull Glyph glyph);

    private static native Glyph nStrokeGlyph(long nativeRasterizer, @NonNull Glyph glyph,
                                             int lineRadius, int lineCap, int lineJoin, int miterLimit);
}
