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

package com.mta.tehreer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.graphics.Renderer;
import com.mta.tehreer.layout.ComposedLine;

class LineView extends View {
    private Renderer mRenderer = new Renderer();
    private ComposedLine mLine;

    private Rect mFrame = new Rect();

    public LineView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mLine != null) {
            float dx = mLine.getOriginX() - mFrame.left;
            float dy = mLine.getOriginY() - mFrame.top;

            mLine.draw(mRenderer, canvas, dx, dy);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mFrame.set(left, top, right, bottom);
    }

    public @NonNull Rect getFrame() {
        return mFrame;
    }

    public @NonNull Renderer getRenderer() {
        return mRenderer;
    }

    public @Nullable ComposedLine getLine() {
        return mLine;
    }

    public void setLine(@Nullable ComposedLine line) {
        mLine = line;
    }
}
