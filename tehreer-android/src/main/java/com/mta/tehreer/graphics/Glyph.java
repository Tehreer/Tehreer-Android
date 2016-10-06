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

import android.graphics.Bitmap;
import android.graphics.Path;

class Glyph {

    private int mGlyphId;
    private int mLeft;
    private int mTop;
    private Bitmap mBitmap;
    private Path mPath;
    private long mNativeOutline;

    public Glyph(int glyphId) {
        mGlyphId = glyphId;
    }

    public int glyphId() {
        return mGlyphId;
    }

    public int left() {
        return mLeft;
    }

    public int top() {
        return mTop;
    }

    public int right() {
        return mLeft + (mBitmap != null ? mBitmap.getWidth() : 0);
    }

    public int bottom() {
        return mTop + (mBitmap != null ? mBitmap.getHeight() : 0);
    }

    public Bitmap bitmap() {
        return mBitmap;
    }

    public Path path() {
        return mPath;
    }

    public boolean containsOutline() {
        return (mNativeOutline != 0);
    }

    @SuppressWarnings("unused")
    private void ownBitmap(Bitmap bitmap, int left, int top) {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }

        mBitmap = bitmap;
        mLeft = left;
        mTop = top;
    }

    @SuppressWarnings("unused")
    private void ownPath(Path path) {
        mPath = path;
    }

    @SuppressWarnings("unused")
    private void ownOutline(long nativeOutline) {
        if (mNativeOutline != 0) {
            nativeDisposeOutline(mNativeOutline);
        }

        mNativeOutline = nativeOutline;
    }

    @Override
    public void finalize() throws Throwable {
        try {
            if (mNativeOutline != 0) {
                nativeDisposeOutline(mNativeOutline);
                mNativeOutline = 0;
            }
        } finally {
            super.finalize();
        }
    }

    private static native void nativeDisposeOutline(long nativeOutline);
}
