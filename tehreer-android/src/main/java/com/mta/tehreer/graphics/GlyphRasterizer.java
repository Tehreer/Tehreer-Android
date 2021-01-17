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

    public GlyphRasterizer(@NonNull GlyphKey key) {
	    nativeRasterizer = nCreate(key.typeface.nativeTypeface,
                                   key.pixelWidth, key.pixelHeight,
                                   0x10000, -key.skewX, 0, 0x10000);
	}

    public int getGlyphType(int glyphId) {
        return nGetGlyphType(nativeRasterizer, glyphId);
    }

    public @Nullable GlyphImage getGlyphImage(int glyphId) {
        return getGlyphImage(glyphId, Color.TRANSPARENT);
    }

    public @Nullable GlyphImage getGlyphImage(int glyphId, @ColorInt int foregroundColor) {
        return nGetGlyphImage(nativeRasterizer, glyphId, foregroundColor);
    }

    public @Nullable GlyphImage getStrokeImage(@NonNull GlyphOutline glyphOutline,
                                               int lineRadius,
                                               @GlyphAttributes.LineCap int lineCap,
                                               @GlyphAttributes.LineJoin int lineJoin,
                                               int miterLimit) {
        return nGetStrokeImage(nativeRasterizer, glyphOutline.nativeOutline,
                               lineRadius, lineCap, lineJoin, miterLimit);
    }

    public @Nullable GlyphOutline getGlyphOutline(int glyphId) {
	    long nativeOutline = nGetGlyphOutline(nativeRasterizer, glyphId);
	    if (nativeOutline != 0) {
	        return new GlyphOutline(nativeOutline);
        }

	    return null;
    }

    public @NonNull Path getGlyphPath(int glyphId) {
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

    private static native long nGetGlyphOutline(long nativeRasterizer, int glyphId);
    private static native Path nGetGlyphPath(long nativeRasterizer, int glyphId);
}
