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

    private static int sizeOf(@NonNull Bitmap bitmap) {
        int size = bitmap.getWidth() * bitmap.getHeight();

        if (bitmap.getConfig() == Bitmap.Config.ARGB_8888) {
            size *= 4;
        }

        return size;
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

    private static class Holder {
        private static final @NonNull GlyphCache INSTANCE;

        static {
            int maxSize = (int) (Runtime.getRuntime().maxMemory() / 8);
            INSTANCE = new GlyphCache(maxSize);
        }
    }

    public static @NonNull GlyphCache getInstance() {
        return Holder.INSTANCE;
    }

    private @NonNull HashMap<GlyphStrike, Segment<Integer, ?>> segments = new HashMap<>();

    public GlyphCache(int capacity) {
        super(capacity);
    }

    @Override
    public void clear() {
        super.clear();

        // Dispose all glyph rasterizers.
        for (Map.Entry<GlyphStrike, Segment<Integer, ?>> entry : segments.entrySet()) {
            Segment<Integer, ?> value = entry.getValue();

            if (value instanceof DataSegment) {
                DataSegment segment = (DataSegment) value;
                segment.rasterizer.dispose();
            }
        }

        segments.clear();
    }

    private @NonNull DataSegment unsafeGetSegment(@NonNull GlyphStrike.Data strike) {
        DataSegment segment = (DataSegment) segments.get(strike);
        if (segment == null) {
            GlyphRasterizer rasterizer = new GlyphRasterizer(strike);
            segment = new DataSegment(this, rasterizer);
            segments.put(strike.clone(), segment);
        }

        return segment;
    }

    public @NonNull Glyph unsafeGetGlyph(@NonNull DataSegment segment, int glyphId) {
        Glyph glyph = segment.get(glyphId);
        if (glyph == null) {
            glyph = new Glyph();
        }

        return glyph;
    }

    private @Nullable GlyphImage getColorImage(@NonNull GlyphAttributes attributes,
                                               @NonNull GlyphRasterizer rasterizer,
                                               int glyphId) {
        final GlyphStrike.Color strike = attributes.colorStrike();
        final ImageSegment segment;
        GlyphImage glyphImage;

        synchronized (this) {
            ImageSegment colorSegment = (ImageSegment) segments.get(strike);
            if (colorSegment == null) {
                colorSegment = new ImageSegment(this);
                segments.put(strike, colorSegment);
            }

            segment = colorSegment;
            glyphImage = segment.get(glyphId);
        }

        if (glyphImage == null) {
            glyphImage = rasterizer.getGlyphImage(glyphId, strike.foregroundColor);

            if (glyphImage != null) {
                synchronized (this) {
                    segment.remove(glyphId);
                    segment.put(glyphId, glyphImage);
                }
            }
        }

        return glyphImage;
    }

    private @Nullable GlyphImage getStrokeImage(@NonNull GlyphAttributes attributes,
                                                @NonNull GlyphRasterizer rasterizer,
                                                @NonNull GlyphOutline glyphOutline, int glyphId) {
        final GlyphStrike.Stroke strike = attributes.strokeStrike();
        final ImageSegment segment;
        GlyphImage glyphImage;

        synchronized (this) {
            ImageSegment strokeSegment = (ImageSegment) segments.get(strike);
            if (strokeSegment == null) {
                strokeSegment = new ImageSegment(this);
                segments.put(strike, strokeSegment);
            }

            segment = strokeSegment;
            glyphImage = segment.get(glyphId);
        }

        if (glyphImage == null) {
            glyphImage = rasterizer.getStrokeImage(glyphOutline,
                                                   strike.lineRadius, strike.lineCap,
                                                   strike.lineJoin, strike.miterLimit);

            if (glyphImage != null) {
                synchronized (this) {
                    segment.remove(glyphId);
                    segment.put(glyphId, glyphImage);
                }
            }
        }

        return glyphImage;
    }

    public @Nullable GlyphImage getGlyphImage(@NonNull GlyphAttributes attributes, int glyphId) {
        final DataSegment segment;
        final Glyph glyph;

        synchronized (this) {
            segment = unsafeGetSegment(attributes.associatedStrike());
            glyph = unsafeGetGlyph(segment, glyphId);
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
            return getColorImage(attributes, segment.rasterizer, glyphId);
        }

        return glyph.getImage();
    }

    public @Nullable GlyphImage getStrokeImage(@NonNull GlyphAttributes attributes, int glyphId) {
        final DataSegment segment;
        final Glyph glyph;

        synchronized (this) {
            segment = unsafeGetSegment(attributes.associatedStrike());
            glyph = unsafeGetGlyph(segment, glyphId);
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
            return getStrokeImage(attributes, segment.rasterizer, glyphOutline, glyphId);
        }

        return null;
    }

    public @NonNull Path getGlyphPath(@NonNull GlyphAttributes attributes, int glyphId) {
        final DataSegment segment;
        final Glyph glyph;

        synchronized (this) {
            segment = unsafeGetSegment(attributes.associatedStrike());
            glyph = unsafeGetGlyph(segment, glyphId);
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
