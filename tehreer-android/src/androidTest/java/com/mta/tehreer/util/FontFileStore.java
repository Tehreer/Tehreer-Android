/*
 * Copyright (C) 2022 Muhammad Tayyab Akram
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

package com.mta.tehreer.util;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.test.platform.app.InstrumentationRegistry;

import com.mta.tehreer.font.FontFile;

import java.io.IOException;
import java.io.InputStream;

public class FontFileStore {
    private static FontFile rocherColor;
    private static FontFile sudo;

    public static FontFile getRocherColor() {
        if (rocherColor == null) {
            try {
                Context context = InstrumentationRegistry.getInstrumentation().getContext();
                AssetManager assetManager = context.getAssets();
                InputStream stream = assetManager.open("RocherColor.ttf");
                rocherColor =  new FontFile(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return rocherColor;
    }

    public static FontFile getSudo() {
        if (sudo == null) {
            try {
                Context context = InstrumentationRegistry.getInstrumentation().getContext();
                AssetManager assetManager = context.getAssets();
                InputStream stream = assetManager.open("Sudo.ttf");
                sudo =  new FontFile(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sudo;
    }

    private FontFileStore() { }
}
