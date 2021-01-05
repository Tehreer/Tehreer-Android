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

import android.graphics.Color;
import android.graphics.Path;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

	int getGlyphType(int glyphId) {
        return nGetGlyphType(nativeRasterizer, glyphId);
    }

    @Nullable GlyphImage getGlyphImage(int glyphId) {
        return getGlyphImage(glyphId, Color.TRANSPARENT);
    }

    @Nullable GlyphImage getGlyphImage(int glyphId, @ColorInt int foregroundColor) {
        return nGetGlyphImage(nativeRasterizer, glyphId, foregroundColor);
    }

    @Nullable GlyphImage getStrokeImage(long nativeOutline, int lineRadius,
                                       @GlyphAttributes.LineCap int lineCap,
                                       @GlyphAttributes.LineJoin int lineJoin,
                                       int miterLimit) {
        return nGetStrokeImage(nativeRasterizer, nativeOutline,
                               lineRadius, lineCap, lineJoin, miterLimit);
    }

    void loadOutline(@NonNull Glyph glyph) {
        nLoadOutline(nativeRasterizer, glyph);
    }

    @NonNull Path getGlyphPath(int glyphId) {
        return nGetGlyphPath(nativeRasterizer, glyphId);
    }

    @Override
    public void dispose() {
        nDispose(nativeRasterizer);
    }

	private static native long nCreate(long nativeTypeface, int pixelWidth, int pixelHeight,
                                       int transformXX, int transformXY, int transformYX, int transformYY);
    private static native void nDispose(long nativeRasterizer);

    private static native int nGetGlyphType(long nativeRasterizer, int glyphId);
    private static native GlyphImage nGetGlyphImage(long nativeRasterizer, int glyphId, int forgroundColor);
    private static native GlyphImage nGetStrokeImage(long nativeRasterizer, long nativeOutline,
                                                     int lineRadius, int lineCap, int lineJoin, int miterLimit);

    private static native void nLoadOutline(long nativeRasterizer, @NonNull Glyph glyph);
    private static native Path nGetGlyphPath(long nativeRasterizer, int glyphId);
}
