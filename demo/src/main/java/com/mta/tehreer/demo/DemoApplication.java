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

package com.mta.tehreer.demo;

import android.app.Application;

import com.mta.tehreer.graphics.Typeface;
import com.mta.tehreer.graphics.TypefaceManager;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        registerTypeface("TajNastaleeq.ttf", R.id.typeface_taj_nastaleeq);
        registerTypeface("MehrNastaliq.ttf", R.id.typeface_mehr_nastaliq);
        registerTypeface("NafeesWeb.ttf", R.id.typeface_nafees_web);
    }

    private void registerTypeface(String fileName, int tag) {
        // It is better to copy the typeface into sdcard for performance reasons.
        try {
            File file = copyAsset(fileName);
            Typeface typeface = new Typeface(file);
            TypefaceManager.registerTypeface(typeface, tag);
        } catch (Exception e) {
            throw new RuntimeException("Unable to register typeface \"" + fileName + "\"");
        }
    }

    private File copyAsset(String fileName) throws IOException {
        String path = getFilesDir().getAbsolutePath() + File.separator + fileName;
        File file = new File(path);

        if (!file.exists()) {
            InputStream in = null;
            OutputStream out = null;

            try {
                in = getAssets().open(fileName);
                out = new FileOutputStream(path);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            } finally {
                closeSilently(in);
                closeSilently(out);
            }
        }

        return new File(path);
    }

    private void closeSilently(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {
        }
    }
}
