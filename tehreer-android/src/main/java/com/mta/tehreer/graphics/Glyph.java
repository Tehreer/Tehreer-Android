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

import android.graphics.Bitmap;
import android.graphics.Path;

import com.mta.tehreer.internal.JNILoader;
import com.mta.tehreer.internal.Sustain;

class Glyph {

    static {
        JNILoader.load();
    }

    @Sustain
    private final int glyphId;
    @Sustain
    private long nativeOutline;
    private int mLeftSideBearing;
    private int mTopSideBearing;
    private Bitmap mBitmap;
    private Path mPath;

    public Glyph(int glyphId) {
        this.glyphId = glyphId;
    }

    public int glyphId() {
        return glyphId;
    }

    public int leftSideBearing() {
        return mLeftSideBearing;
    }

    public int topSideBearing() {
        return mTopSideBearing;
    }

    public int rightSideBearing() {
        return mLeftSideBearing + (mBitmap != null ? mBitmap.getWidth() : 0);
    }

    public int bottomSideBearing() {
        return mTopSideBearing + (mBitmap != null ? mBitmap.getHeight() : 0);
    }

    public Bitmap bitmap() {
        return mBitmap;
    }

    public Path path() {
        return mPath;
    }

    public boolean containsOutline() {
        return (nativeOutline != 0);
    }

    @Sustain
    private void ownBitmap(Bitmap bitmap, int left, int top) {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }

        mBitmap = bitmap;
        mLeftSideBearing = left;
        mTopSideBearing = top;
    }

    @Sustain
    private void ownPath(Path path) {
        mPath = path;
    }

    @Sustain
    private void ownOutline(long nativeOutline) {
        if (this.nativeOutline != 0) {
            nativeDisposeOutline(this.nativeOutline);
        }

        this.nativeOutline = nativeOutline;
    }

    @Override
    public void finalize() throws Throwable {
        try {
            if (nativeOutline != 0) {
                nativeDisposeOutline(nativeOutline);
                nativeOutline = 0;
            }
        } finally {
            super.finalize();
        }
    }

    private static native void nativeDisposeOutline(long nativeOutline);
}
