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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.internal.util.LruCache;

import java.util.HashMap;
import java.util.Map;

class GlyphCache extends LruCache {
    private static class Segment extends LruCache.Segment<Integer, Glyph> {
        //
        // HashMap:
        //  - 1 pointer for map entry
        //  - 3 pointers for key, value and next
        //  - 1 integer for hash code
        //
        // LruCache.Node:
        //  - 5 pointers for segment, key, value, previous and next
        //
        // Glyph:
        //  - 3 pointers for outline, bitmap and path
        //  - 3 integers for glyph id, glyph left and glyph top
        //
        // Total:
        //  - 12 pointers
        //  - 4 integers
        //
        // Size: (12 * 4) + (4 * 4) = 64
        //
        private static final int ESTIMATED_OVERHEAD = 64;

        public final @NonNull GlyphRasterizer rasterizer;

        public Segment(@NonNull LruCache cache, @NonNull GlyphRasterizer rasterizer) {
            super(cache);
            this.rasterizer = rasterizer;
        }

        @Override
        protected int sizeOf(@NonNull Integer key, @NonNull Glyph value) {
            GlyphImage glyphImage = value.getImage();
            int innerSize = 0;

            if (glyphImage != null) {
                Bitmap bitmap = glyphImage.bitmap();
                innerSize = bitmap.getWidth() * bitmap.getHeight();
            }

            return innerSize + ESTIMATED_OVERHEAD;
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

    private @NonNull HashMap<GlyphStrike, Segment> segments = new HashMap<>();

    public GlyphCache(int capacity) {
        super(capacity);
    }

    @Override
    public void clear() {
        super.clear();

        // Dispose all glyph rasterizers.
        for (Map.Entry<GlyphStrike, Segment> entry : segments.entrySet()) {
            entry.getValue().rasterizer.dispose();
        }
        segments.clear();
    }

    private @NonNull Segment unsafeGetSegment(@NonNull GlyphStrike strike) {
        Segment segment = segments.get(strike);
        if (segment == null) {
            GlyphRasterizer rasterizer = new GlyphRasterizer(strike);
            segment = new Segment(this, rasterizer);
            segments.put(strike.clone(), segment);
        }

        return segment;
    }

    public @NonNull Glyph unsafeGetGlyph(@NonNull Segment segment, int glyphId) {
        Glyph glyph = segment.get(glyphId);
        if (glyph == null) {
            glyph = new Glyph(glyphId);
        }

        return glyph;
    }

    private @Nullable GlyphImage getColorImage(@NonNull GlyphAttributes attributes,
                                               @NonNull GlyphRasterizer rasterizer,
                                               int glyphId) {
        final GlyphStrike strike = attributes.colorStrike();
        final Segment segment;
        final Glyph glyph;

        synchronized (this) {
            Segment colorSegment = segments.get(strike);
            if (colorSegment == null) {
                colorSegment = new Segment(this, rasterizer);
                segments.put(strike, colorSegment);
            }

            segment = colorSegment;
            glyph = unsafeGetGlyph(segment, glyphId);
        }

        GlyphImage glyphImage = glyph.getImage();

        if (glyphImage == null) {
            glyphImage = rasterizer.getGlyphImage(glyphId, attributes.getForegroundColor());

            synchronized (this) {
                if (glyph.getImage() == null) {
                    segment.remove(glyphId);
                    glyph.setImage(glyphImage);
                    segment.put(glyphId, glyph);
                }
            }
        }

        return glyphImage;
    }

    private @Nullable GlyphImage getStrokeImage(@NonNull GlyphAttributes attributes,
                                                @NonNull GlyphRasterizer rasterizer,
                                                @NonNull Glyph parentGlyph, int glyphId) {
        final GlyphStrike strike = attributes.strokeStrike();
        final Segment segment;
        Glyph glyph;

        synchronized (this) {
            Segment strokeSegment = segments.get(strike);
            if (strokeSegment == null) {
                strokeSegment = new Segment(this, rasterizer);
                segments.put(strike, strokeSegment);
            }

            segment = strokeSegment;
            glyph = unsafeGetGlyph(segment, glyphId);
        }

        GlyphImage glyphImage = glyph.getImage();

        if (glyphImage == null) {
            glyphImage = rasterizer.getStrokeImage(parentGlyph.getNativeOutline(),
                                                   attributes.getFixedLineRadius(),
                                                   attributes.getLineCap(),
                                                   attributes.getLineJoin(),
                                                   attributes.getFixedMiterLimit());

            synchronized (this) {
                if (glyph.getImage() == null) {
                    segment.remove(glyphId);
                    glyph.setImage(glyphImage);
                    segment.put(glyphId, glyph);
                }
            }
        }

        return glyphImage;
    }

    public @Nullable GlyphImage getGlyphImage(@NonNull GlyphAttributes attributes, int glyphId) {
        final Segment segment;
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

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public @Nullable GlyphImage getStrokeImage(@NonNull GlyphAttributes attributes, int glyphId) {
        final Segment segment;
        final Glyph glyph;

        synchronized (this) {
            segment = unsafeGetSegment(attributes.associatedStrike());
            glyph = unsafeGetGlyph(segment, glyphId);
        }

        synchronized(glyph) {
            if (!glyph.containsOutline()) {
                segment.remove(glyphId);

                segment.rasterizer.loadOutline(glyph);
                segment.put(glyphId, glyph);
            }
        }

        return getStrokeImage(attributes, segment.rasterizer, glyph, glyphId);
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public @NonNull Path getGlyphPath(@NonNull GlyphAttributes attributes, int glyphId) {
        final Segment segment;
        final Glyph glyph;

        synchronized (this) {
            segment = unsafeGetSegment(attributes.associatedStrike());
            glyph = unsafeGetGlyph(segment, glyphId);
        }

        synchronized (glyph) {
            if (glyph.path() == null) {
                segment.remove(glyphId);

                segment.rasterizer.loadPath(glyph);
                segment.put(glyphId, glyph);
            }
        }

        return glyph.path();
    }
}
