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

package com.mta.tehreer.bidi;

import com.mta.tehreer.util.Disposable;

class BidiMirrorLocator implements Disposable {

    long nativeMirrorLocator;
    long nativeBuffer;

	public BidiMirrorLocator() {
		nativeMirrorLocator = nativeCreate();
	}

    public void loadLine(BidiLine line) {
        nativeLoadLine(nativeMirrorLocator, line.nativeLine, line.nativeBuffer);
    }

    public BidiPair nextPair() {
        return nativeGetNextPair(nativeMirrorLocator, nativeBuffer);
    }

    @Override
    public void dispose() {
        nativeDispose(nativeMirrorLocator);
    }

	private native long nativeCreate();
	private native void nativeDispose(long nativeMirrorLocator);

	private native void nativeLoadLine(long nativeMirrorLocator, long nativeLine, long nativeBuffer);
	private native BidiPair nativeGetNextPair(long nativeMirrorLocator, long nativeBuffer);
}
