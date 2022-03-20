/*
 * Copyright (C) 2016-2022 Muhammad Tayyab Akram
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

package com.mta.tehreer.unicode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.Disposable;
import com.mta.tehreer.internal.JniBridge;

class BidiMirrorLocator implements Disposable {
    static {
        JniBridge.loadLibrary();
    }

    long nativeMirrorLocator;

	public BidiMirrorLocator() {
		nativeMirrorLocator = nCreate();
	}

    public void loadLine(@NonNull BidiLine line) {
        nLoadLine(nativeMirrorLocator, line.nativeLine, line.nativeBuffer);
    }

    public @Nullable BidiPair nextPair() {
        return nGetNextPair(nativeMirrorLocator);
    }

    @Override
    public void dispose() {
        nDispose(nativeMirrorLocator);
    }

	private native long nCreate();
	private native void nDispose(long nativeMirrorLocator);

	private native void nLoadLine(long nativeMirrorLocator, long nativeLine, long nativeBuffer);
	private native BidiPair nGetNextPair(long nativeMirrorLocator);
}
