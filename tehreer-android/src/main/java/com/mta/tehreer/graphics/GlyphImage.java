/*
 * Copyright (C) 2021 Muhammad Tayyab Akram
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

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

final class GlyphImage {
    private final @NonNull Bitmap bitmap;
    private final int left;
    private final int top;

    @Keep
    public GlyphImage(@NonNull Bitmap bitmap, int left, int top) {
        this.left = left;
        this.top = top;
        this.bitmap = bitmap;
    }

    public @NonNull Bitmap bitmap() {
        return bitmap;
    }

    public int left() {
        return left;
    }

    public int top() {
        return top;
    }

    public int right() {
        return left + bitmap.getWidth();
    }

    public int bottom() {
        return top + bitmap.getHeight();
    }
}
