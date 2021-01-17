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

import android.graphics.Path;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

final class Glyph {
    public static final int TYPE_MASK = 0x0001;
    public static final int TYPE_COLOR = 0x0002;
    public static final int TYPE_MIXED = 0x0003;

    @IntDef({
        TYPE_MASK,
        TYPE_COLOR,
        TYPE_MIXED
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type { }

    private @Type int mType;
    private @Nullable GlyphImage mImage;
    private @Nullable GlyphOutline mOutline;
    private @Nullable Path mPath;

    public boolean isLoaded() {
        return mType != 0;
    }

    public @Type int getType() {
        return mType;
    }

    public void setType(@Type int type) {
        mType = type;
    }

    public @Nullable GlyphImage getImage() {
        return mImage;
    }

    public void setImage(GlyphImage image) {
        mImage = image;
    }

    public @Nullable GlyphOutline getOutline() {
        return mOutline;
    }

    public void setOutline(GlyphOutline outline) {
        mOutline = outline;
    }

    public @Nullable Path getPath() {
        return mPath;
    }

    public void setPath(Path path) {
        mPath = path;
    }
}
