/*
 * Copyright (C) 2019 Muhammad Tayyab Akram
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

package com.mta.tehreer.font;

import android.content.res.AssetManager;

import androidx.annotation.NonNull;

import com.mta.tehreer.graphics.Typeface;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mta.tehreer.internal.util.Preconditions.checkNotNull;

/**
 * A <code>FontFile</code> object represents the file of a specific font format.
 */
public final class FontFile {
    long nativeFontFile;
    private final Finalizer finalizer = new Finalizer();
    private List<Typeface> mTypefaces;

    private class Finalizer {
        @Override
        protected void finalize() throws Throwable {
            try {
                release();
            } finally {
                super.finalize();
            }
        }
    }

    /**
     * Constructs a font file instance representing the specified asset. The data of the asset is
     * not copied to an in-memory buffer. Rather, it is directly read from a stream of the asset
     * when needed. So the typefaces obtained from resulting font file might be slower and should be
     * used with caution.
     *
     * @param assetManager The application's asset manager.
     * @param filePath The path of the font in the assets directory.
     *
     * @throws NullPointerException if <code>assetManager</code> or <code>filePath</code> is null.
     * @throws RuntimeException if an error occurred while initialization.
     */
    public FontFile(@NonNull AssetManager assetManager, @NonNull String filePath) {
        checkNotNull(assetManager, "assetManager");
        checkNotNull(filePath, "filePath");

        nativeFontFile = nCreateFromAsset(assetManager, filePath);
        if (nativeFontFile == 0) {
            throw new RuntimeException("Could not create typeface from specified asset");
        }
    }

    /**
     * Constructs a font file instance representing the specified file path. The data of the font
     * is directly read from a stream of the file when needed.
     *
     * @param file The file describing the path of the font.
     *
     * @throws NullPointerException if <code>file</code> is null.
     * @throws RuntimeException if an error occurred while initialization.
     */
    public FontFile(@NonNull File file) {
        checkNotNull(file, "file");

        nativeFontFile = nCreateFromPath(file.getAbsolutePath());
        if (nativeFontFile == 0) {
            throw new RuntimeException("Could not create typeface from specified file");
        }
    }

    /**
     * Constructs a font file instance from the specified input stream by copying its data into a
     * native memory buffer. It may take time to create the instance if the stream holds larger
     * data.
     *
     * @param stream The input stream that contains the data of the font.
     *
     * @throws NullPointerException if <code>stream</code> is null.
     * @throws RuntimeException if an error occurred while initialization.
     */
    public FontFile(@NonNull InputStream stream) {
        checkNotNull(stream, "stream");

        nativeFontFile = nCreateFromStream(stream);
        if (nativeFontFile == 0) {
            throw new RuntimeException("Could not create typeface from specified stream");
        }
    }

    private void loadTypefaces() {
        List<Typeface> allTypefaces = new ArrayList<>();
        int faceCount = nGetFaceCount(nativeFontFile);

        for (int i = 0; i < faceCount; i++) {
            Typeface firstTypeface = nCreateTypeface(nativeFontFile, i, 0);
            int instanceStart = allTypefaces.size();
            int instanceCount = nGetInstanceCount(firstTypeface);

            allTypefaces.add(firstTypeface);

            for (int j = 1; j < instanceCount; j++) {
                Typeface instanceTypeface = nCreateTypeface(nativeFontFile, i, j);
                float[] instanceCoords = instanceTypeface.getVariationCoordinates();

                if (instanceCoords != null) {
                    // Remove existing duplicate instances.
                    for (int k = allTypefaces.size() - 1; k >= instanceStart; k--) {
                        Typeface referenceTypeface = allTypefaces.get(k);
                        float[] referenceCoords = referenceTypeface.getVariationCoordinates();

                        if (Arrays.equals(instanceCoords, referenceCoords)) {
                            allTypefaces.remove(k);
                        }
                    }
                }

                allTypefaces.add(instanceTypeface);
            }
        }

        mTypefaces = allTypefaces;
    }

    /**
     * Returns named typefaces of this font file.
     *
     * @return Named typefaces of this font file.
     */
    public List<Typeface> getTypefaces() {
        if (mTypefaces == null) {
            synchronized (this) {
                if (mTypefaces == null) {
                    loadTypefaces();
                }
            }
        }

        return mTypefaces;
    }

    void release() {
        nRelease(nativeFontFile);
    }

    private static native long nCreateFromAsset(AssetManager assetManager, String path);
    private static native long nCreateFromPath(String path);
    private static native long nCreateFromStream(InputStream stream);
    private static native void nRelease(long nativeFontFile);

    private static native int nGetFaceCount(long nativeFontFile);
    private static native int nGetInstanceCount(Typeface typeface);

    private static native Typeface nCreateTypeface(long nativeFontFile, int faceIndex, int instanceIndex);
}
