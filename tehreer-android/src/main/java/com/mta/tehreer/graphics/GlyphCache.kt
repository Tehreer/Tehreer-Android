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
package com.mta.tehreer.graphics

import com.mta.tehreer.internal.JniBridge

/**
 * A cache of the images and outlines of glyphs. It keeps the most recently used ones within a
 * capacity that is measured in bytes, and drops the least recently used ones when it is exceeded.
 *
 * A cache can be used from multiple threads. A glyph that is dropped while it is still in use stays
 * alive until its last user lets go of it.
 *
 * A [Renderer] uses the [default] cache unless it is given another one.
 */
class GlyphCache private constructor(
    internal val nativeCache: Long,
    @Suppress("unused") isOwned: Boolean
) {
    private class Finalizable(private val nativeCache: Long) {
        @Suppress("unused")
        protected fun finalize() {
            nDispose(nativeCache)
        }
    }

    private val finalizable = Finalizable(nativeCache)

    /**
     * Creates a cache.
     *
     * @param capacity The size in bytes up to which the cache keeps glyphs.
     *
     * @throws IllegalArgumentException if `capacity` is negative.
     * @throws IllegalStateException if the cache could not be created.
     */
    constructor(capacity: Long) : this(create(capacity), true)

    /**
     * The size in bytes up to which this cache keeps glyphs. Glyphs are dropped right away if the
     * new value is exceeded.
     *
     * @throws IllegalArgumentException if the new value is negative.
     */
    var capacity: Long
        get() = nGetCapacity(nativeCache)
        set(value) {
            require(value >= 0) { "Capacity is negative" }

            nSetCapacity(nativeCache, value)
        }

    /**
     * The number of bytes that this cache holds at the moment.
     */
    val size: Long
        get() = nGetSize(nativeCache)

    /**
     * Drops everything that this cache holds.
     */
    fun clear() {
        nClear(nativeCache)
    }

    private external fun nGetCapacity(nativeCache: Long): Long
    private external fun nSetCapacity(nativeCache: Long, capacity: Long)
    private external fun nGetSize(nativeCache: Long): Long
    private external fun nClear(nativeCache: Long)

    companion object {
        init {
            JniBridge.loadLibrary()
        }

        /**
         * The cache that renderers use by default. It has a capacity of 8 MB, and lives as long as
         * the process does.
         */
        @JvmStatic
        val default: GlyphCache by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            val nativeCache = nGetDefault()
            check(nativeCache != 0L) { "Could not get the default glyph cache" }

            GlyphCache(nativeCache, true)
        }

        private fun create(capacity: Long): Long {
            require(capacity >= 0) { "Capacity is negative" }

            val nativeCache = nCreate(capacity)
            check(nativeCache != 0L) { "Could not create the glyph cache" }

            return nativeCache
        }

        @JvmStatic private external fun nCreate(capacity: Long): Long
        @JvmStatic private external fun nGetDefault(): Long
        @JvmStatic private external fun nDispose(nativeCache: Long)
    }
}
