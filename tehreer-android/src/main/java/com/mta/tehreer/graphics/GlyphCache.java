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

import android.graphics.Bitmap;
import android.graphics.Path;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.internal.util.LruCache;

import java.util.HashMap;
import java.util.Map;

final class GlyphCache extends LruCache {
    //
    // GlyphImage:
    //  - 1 pointer for bitmap
    //  - 2 integers for left and right
    //
    // Size: (1 * 4) + (2 * 4) = 12
    //
    private static final int GLYPH_IMAGE_OVERHEAD = 12;

    //
    // Glyph:
    //  - 3 pointers for image, outline and path
    //  - 1 integer for type
    //
    // Size: (3 * 4) + (1 * 4) = 16
    //
    private static final int GLYPH_OVERHEAD = 16;

    private static class DataSegment extends Segment<Integer, Glyph> {
        private static final int ESTIMATED_OVERHEAD = GLYPH_IMAGE_OVERHEAD + GLYPH_OVERHEAD
                                                    + NODE_OVERHEAD;

        public final @NonNull GlyphRasterizer rasterizer;

        public DataSegment(@NonNull LruCache cache, @NonNull GlyphRasterizer rasterizer) {
            super(cache);
            this.rasterizer = rasterizer;
        }

        @Override
        protected int sizeOf(@NonNull Integer key, @NonNull Glyph value) {
            GlyphImage glyphImage = value.getImage();
            int size = (glyphImage != null ? GlyphCache.sizeOf(glyphImage.bitmap()) : 0);

            return size + ESTIMATED_OVERHEAD;
        }
    }

    private static class ImageSegment extends Segment<Integer, GlyphImage> {
        private static final int ESTIMATED_OVERHEAD = GLYPH_IMAGE_OVERHEAD + NODE_OVERHEAD;

        public ImageSegment(@NonNull LruCache cache) {
            super(cache);
        }

        @Override
        protected int sizeOf(@NonNull Integer key, @NonNull GlyphImage value) {
            return GlyphCache.sizeOf(value.bitmap()) + ESTIMATED_OVERHEAD;
        }
    }

    private static class Holder {
        private static final @NonNull GlyphCache INSTANCE;

        static {
            int maxSize = (int) (Runtime.getRuntime().maxMemory() / 8);
            INSTANCE = new GlyphCache(maxSize);
        }
    }

    private final @NonNull HashMap<GlyphKey, Segment<Integer, ?>> segments = new HashMap<>();

    public static @NonNull GlyphCache getInstance() {
        return Holder.INSTANCE;
    }

    private static int sizeOf(@NonNull Bitmap bitmap) {
        int size = bitmap.getWidth() * bitmap.getHeight();

        if (bitmap.getConfig() == Bitmap.Config.ARGB_8888) {
            size *= 4;
        }

        return size;
    }

    public GlyphCache(int capacity) {
        super(capacity);
    }

    @Override
    public void clear() {
        super.clear();

        // Dispose all glyph rasterizers.
        for (Map.Entry<GlyphKey, Segment<Integer, ?>> entry : segments.entrySet()) {
            Segment<Integer, ?> value = entry.getValue();

            if (value instanceof DataSegment) {
                DataSegment segment = (DataSegment) value;
                segment.rasterizer.dispose();
            }
        }

        segments.clear();
    }

    private @NonNull DataSegment secureDataSegment(@NonNull GlyphKey key) {
        DataSegment segment = (DataSegment) segments.get(key);
        if (segment == null) {
            GlyphRasterizer rasterizer = new GlyphRasterizer(key);
            segment = new DataSegment(this, rasterizer);
            segments.put(key.copy(), segment);
        }

        return segment;
    }

    private @NonNull ImageSegment secureImageSegment(@NonNull GlyphKey key) {
        ImageSegment segment = (ImageSegment) segments.get(key);
        if (segment == null) {
            segment = new ImageSegment(this);
            segments.put(key.copy(), segment);
        }

        return segment;
    }

    private @NonNull Glyph secureGlyph(@NonNull DataSegment segment, int glyphId) {
        Glyph glyph = segment.get(glyphId);
        if (glyph == null) {
            glyph = new Glyph();
        }

        return glyph;
    }

    private @Nullable GlyphImage getColoredImage(@NonNull GlyphKey.Color key,
                                                 @NonNull GlyphRasterizer rasterizer,
                                                 int glyphId) {
        final ImageSegment segment;
        GlyphImage coloredImage;

        synchronized (this) {
            segment = secureImageSegment(key);
            coloredImage = segment.get(glyphId);
        }

        if (coloredImage == null) {
            coloredImage = rasterizer.getGlyphImage(glyphId, key.foregroundColor);

            if (coloredImage != null) {
                synchronized (this) {
                    segment.remove(glyphId);
                    segment.put(glyphId, coloredImage);
                }
            }
        }

        return coloredImage;
    }

    public @Nullable GlyphImage getGlyphImage(@NonNull GlyphAttributes attributes, int glyphId) {
        final DataSegment segment;
        final Glyph glyph;

        synchronized (this) {
            segment = secureDataSegment(attributes.dataKey());
            glyph = secureGlyph(segment, glyphId);
        }

        if (!glyph.isLoaded()) {
            int glyphType = segment.rasterizer.getGlyphType(glyphId);
            GlyphImage glyphImage = null;

            if (glyphType != Glyph.TYPE_MIXED) {
                glyphImage = segment.rasterizer.getGlyphImage(glyphId);
            }

            synchronized (this) {
                if (!glyph.isLoaded()) {
                    segment.remove(glyphId);

                    glyph.setType(glyphType);
                    glyph.setImage(glyphImage);

                    segment.put(glyphId, glyph);
                }
            }
        }

        if (glyph.getType() == Glyph.TYPE_MIXED) {
            return getColoredImage(attributes.colorKey(), segment.rasterizer, glyphId);
        }

        return glyph.getImage();
    }

    private @Nullable GlyphImage getStrokeImage(@NonNull GlyphKey.Stroke key,
                                                @NonNull GlyphRasterizer rasterizer,
                                                @NonNull GlyphOutline outline, int glyphId) {
        final ImageSegment segment;
        GlyphImage strokeImage;

        synchronized (this) {
            segment = secureImageSegment(key);
            strokeImage = segment.get(glyphId);
        }

        if (strokeImage == null) {
            strokeImage = rasterizer.getStrokeImage(outline,
                                                    key.lineRadius, key.lineCap,
                                                    key.lineJoin, key.miterLimit);

            if (strokeImage != null) {
                synchronized (this) {
                    segment.remove(glyphId);
                    segment.put(glyphId, strokeImage);
                }
            }
        }

        return strokeImage;
    }

    public @Nullable GlyphImage getStrokeImage(@NonNull GlyphAttributes attributes, int glyphId) {
        final DataSegment segment;
        final Glyph glyph;

        synchronized (this) {
            segment = secureDataSegment(attributes.dataKey());
            glyph = secureGlyph(segment, glyphId);
        }

        GlyphOutline glyphOutline = glyph.getOutline();

        if (glyphOutline == null) {
            glyphOutline = segment.rasterizer.getGlyphOutline(glyphId);

            synchronized (this) {
                if (glyph.getOutline() == null) {
                    segment.remove(glyphId);
                    glyph.setOutline(glyphOutline);
                    segment.put(glyphId, glyph);
                }
            }
        }

        if (glyphOutline != null) {
            return getStrokeImage(attributes.strokeKey(), segment.rasterizer,
                                  glyphOutline, glyphId);
        }

        return null;
    }

    public @NonNull Path getGlyphPath(@NonNull GlyphAttributes attributes, int glyphId) {
        final DataSegment segment;
        final Glyph glyph;

        synchronized (this) {
            segment = secureDataSegment(attributes.dataKey());
            glyph = secureGlyph(segment, glyphId);
        }

        Path glyphPath = glyph.getPath();

        if (glyphPath == null) {
            glyphPath = segment.rasterizer.getGlyphPath(glyphId);

            synchronized (this) {
                if (glyph.getPath() == null) {
                    segment.remove(glyphId);
                    glyph.setPath(glyphPath);
                    segment.put(glyphId, glyph);
                }
            }
        }

        return glyphPath;
    }
}
