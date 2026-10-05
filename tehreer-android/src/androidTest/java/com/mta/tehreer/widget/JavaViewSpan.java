/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

import com.mta.tehreer.layout.style.ViewSpan;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;

/** A view span written in Java: what the API looks like from there. */
public class JavaViewSpan extends ViewSpan {
    @NonNull
    @Override
    public Placement getPlacement() {
        return Placement.BLOCK;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @NonNull
    @Override
    public Rect getMargins() {
        return new Rect(0, 8, 0, 4);
    }

    @Override
    public boolean getRetainWhenOffscreen() {
        return false;
    }

    @NonNull
    @Override
    public View createView(@NonNull Context context) {
        return new View(context);
    }
}
