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

package com.mta.tehreer.graphics;

import com.mta.tehreer.internal.JNILoader;
import com.mta.tehreer.util.Disposable;

class GlyphRasterizer implements Disposable {

    static {
        JNILoader.load();
    }

    public static final int LINECAP_BUTT = 0;
    public static final int LINECAP_ROUND = 1;
    public static final int LINECAP_SQUARE = 2;

    public static final int LINEJOIN_ROUND = 0;
    public static final int LINEJOIN_BEVEL = 1;
    public static final int LINEJOIN_MITER_VARIABLE = 2;
    public static final int LINEJOIN_MITER_FIXED = 3;
    public static final int LINEJOIN_MITER = LINEJOIN_MITER_VARIABLE;

	long nativeRasterizer;

	GlyphRasterizer(GlyphStrike strike) {
	    nativeRasterizer = nativeCreate(strike.typeface.nativeTypeface,
                                        strike.pixelWidth, strike.pixelHeight,
                                        0x10000, -strike.skewX, 0, 0x10000);
	}

	void loadBitmap(Glyph glyph) {
	    nativeLoadBitmap(nativeRasterizer, glyph);
	}

    void loadOutline(Glyph glyph) {
        nativeLoadOutline(nativeRasterizer, glyph);
    }

    void loadPath(Glyph glyph) {
        nativeLoadPath(nativeRasterizer, glyph);
    }

    Glyph strokeGlyph(Glyph glyph, int lineRadius,
                      int lineCap, int lineJoin, int miterLimit) {
        return nativeStrokeGlyph(nativeRasterizer, glyph, lineRadius, lineCap, lineJoin, miterLimit);
    }

    @Override
    public void dispose() {
        nativeDispose(nativeRasterizer);
    }

	private static native long nativeCreate(long nativeTypeface, int pixelWidth, int pixelHeight,
                                            int transformXX, int transformXY, int transformYX, int transformYY);
    private static native void nativeDispose(long nativeRasterizer);

    private static native void nativeLoadBitmap(long nativeRasterizer, Glyph glyph);
    private static native void nativeLoadOutline(long nativeRasterizer, Glyph glyph);
    private static native void nativeLoadPath(long nativeRasterizer, Glyph glyph);

    private static native Glyph nativeStrokeGlyph(long nativeRasterizer, Glyph glyph, int lineRadius,
                                                  int lineCap, int lineJoin, int miterLimit);
}
