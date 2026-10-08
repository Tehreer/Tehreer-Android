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

package com.mta.tehreer.unicode

import com.mta.tehreer.internal.JniBridge

internal class BidiMirrorLocator {
    val nativeMirrorLocator: Long = nCreate()

    /** The native locator keeps pointing into the data of the loaded line while it is iterating. */
    private var loadedLine: BidiLine? = null

    fun loadLine(line: BidiLine) {
        loadedLine = line
        nLoadLine(nativeMirrorLocator, line.nativeLine, line.nativeBuffer)
    }

    fun nextPair(): BidiPair? {
        return nGetNextPair(nativeMirrorLocator)
    }

    @Suppress("unused")
    protected fun finalize() {
        nDispose(nativeMirrorLocator)
    }

    private external fun nCreate(): Long
    private external fun nDispose(nativeMirrorLocator: Long)

    private external fun nLoadLine(nativeMirrorLocator: Long, nativeLine: Long, nativeBuffer: Long)
    private external fun nGetNextPair(nativeMirrorLocator: Long): BidiPair?

    private companion object {
        init {
            JniBridge.loadLibrary()
        }
    }
}
