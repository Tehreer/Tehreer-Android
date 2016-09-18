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

package com.mta.tehreer.graphics;

import android.content.res.AssetManager;
import android.graphics.Rect;

import com.mta.tehreer.internal.util.Constants;
import com.mta.tehreer.util.Disposable;

import java.io.InputStream;

public class Typeface implements Disposable {

    private static class Finalizable extends Typeface {

        private Finalizable(Typeface parent) {
            super(parent);
        }

        @Override
        public void dispose() {
            throw new UnsupportedOperationException(Constants.EXCEPTION_FINALIZABLE_OBJECT);
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                super.dispose();
            } finally {
                super.finalize();
            }
        }
    }

    public static Typeface finalizable(Typeface typeface) {
        if (typeface.getClass() == Typeface.class) {
            return new Finalizable(typeface);
        }

        if (typeface.getClass() != Finalizable.class) {
            throw new IllegalArgumentException(Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED);
        }

        return typeface;
    }

    public static boolean isFinalizable(Typeface typeface) {
        return (typeface.getClass() == Finalizable.class);
    }

	long nativeTypeface;

    public static Typeface createWithAsset(AssetManager assetManager, String path) {
        long nativeTypeface = nativeCreateWithAsset(assetManager, path);
        if (nativeTypeface != 0) {
            return new Typeface(nativeTypeface);
        }

        return null;
    }

    public static Typeface createWithFile(String path) {
        long nativeTypeface = nativeCreateWithFile(path);
        if (nativeTypeface != 0) {
            return new Typeface(nativeTypeface);
        }

        return null;
    }

    public static Typeface createFromStream(InputStream stream) {
        long nativeTypeface = nativeCreateFromStream(stream);
        if (nativeTypeface != 0) {
            return new Typeface(nativeTypeface);
        }

        return null;
    }

	private Typeface(long nativeTypeface) {
	    this.nativeTypeface = nativeTypeface;
	}

    private Typeface(Typeface other) {
        this.nativeTypeface = other.nativeTypeface;
    }

    public byte[] copyTable(int tag) {
        return nativeCopyTable(nativeTypeface, tag);
    }

	public int getUnitsPerEm() {
		return nativeGetUnitsPerEm(nativeTypeface);
	}

	public int getAscent() {
		return nativeGetAscent(nativeTypeface);
	}

	public int getDescent() {
		return nativeGetDescent(nativeTypeface);
	}

	public int getGlyphCount() {
	    return nativeGetGlyphCount(nativeTypeface);
	}

	public Rect getBoundingBox() {
	    Rect boundingBox = new Rect();
	    nativeGetBoundingBox(nativeTypeface, boundingBox);

	    return boundingBox;
	}

	public int getUnderlinePosition() {
	    return nativeGetUnderlinePosition(nativeTypeface);
	}

	public int getUnderlineThickness() {
	    return nativeGetUnderlineThickness(nativeTypeface);
	}

    @Override
    public void dispose() {
        nativeDispose(nativeTypeface);
    }

    @Override
    public String toString() {
        return "Typeface{unitsPerEm=" + getUnitsPerEm()
                + ", ascent=" + getAscent()
                + ", descent=" + getDescent()
                + ", glyphCount=" + getGlyphCount()
                + ", boundingBox=" + getBoundingBox().toString()
                + ", underlinePosition=" + getUnderlinePosition()
                + ", underlineThickness=" + getUnderlineThickness()
                + "}";
    }

    private static native long nativeCreateWithAsset(AssetManager assetManager, String path);
    private static native long nativeCreateWithFile(String path);
    private static native long nativeCreateFromStream(InputStream stream);
	private static native void nativeDispose(long nativeTypeface);

    private static native byte[] nativeCopyTable(long nativeTypeface, int tag);

	private static native int nativeGetUnitsPerEm(long nativeTypeface);
	private static native int nativeGetAscent(long nativeTypeface);
	private static native int nativeGetDescent(long nativeTypeface);

	private static native int nativeGetGlyphCount(long nativeTypeface);
	private static native void nativeGetBoundingBox(long nativeTypeface, Rect boundingBox);

	private static native int nativeGetUnderlinePosition(long nativeTypeface);
	private static native int nativeGetUnderlineThickness(long nativeTypeface);
}
