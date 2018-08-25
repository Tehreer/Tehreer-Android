/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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
import android.support.annotation.NonNull;

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
            Bitmap maskBitmap = value.bitmap();
            int innerSize = 0;

            if (maskBitmap != null) {
                innerSize = maskBitmap.getWidth() * maskBitmap.getHeight();
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

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public @NonNull Glyph getMaskGlyph(@NonNull GlyphStrike strike, int glyphId) {
        final Segment segment;
        final Glyph glyph;

        synchronized (this) {
            segment = unsafeGetSegment(strike);
            glyph = unsafeGetGlyph(segment, glyphId);
        }

        synchronized (glyph) {
            if (glyph.bitmap() == null) {
                segment.remove(glyphId);

                segment.rasterizer.loadBitmap(glyph);
                segment.put(glyphId, glyph);
            }
        }

        return glyph;
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public @NonNull Glyph getMaskGlyph(@NonNull GlyphStrike strike, int glyphId, int lineRadius,
                                       int lineCap, int lineJoin, int miterLimit) {
        final Segment segment;
        final Glyph glyph;

        synchronized (this) {
            segment = unsafeGetSegment(strike);
            glyph = unsafeGetGlyph(segment, glyphId);
        }

        synchronized(glyph) {
            if (!glyph.containsOutline()) {
                segment.remove(glyphId);

                segment.rasterizer.loadOutline(glyph);
                segment.put(glyphId, glyph);
            }
        }

        return segment.rasterizer.strokeGlyph(glyph, lineRadius, lineCap, lineJoin, miterLimit);
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public @NonNull Path getGlyphPath(@NonNull GlyphStrike strike, int glyphId) {
        final Segment segment;
        final Glyph glyph;

        synchronized (this) {
            segment = unsafeGetSegment(strike);
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
