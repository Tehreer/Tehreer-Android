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

package com.mta.tehreer.internal.text;

import android.text.Spanned;

import java.util.Iterator;

public class TopSpanIterator<T> implements Iterator<T> {

    private final Spanned spanned;
    private final int charEnd;
    private final Class<T> clazz;

    private int mSpanStart = -1;
    private int mSpanEnd = -1;

    private int mNextStart;
    private int mNextEnd;
    private T mNextObject;

    public TopSpanIterator(Spanned spanned, int charStart, int charEnd, Class<T> clazz) {
        this.spanned = spanned;
        this.charEnd = charEnd;
        this.clazz = clazz;

        findNext(charStart);
    }

    public int getSpanStart() {
        return mSpanStart;
    }

    public int getSpanEnd() {
        return mSpanEnd;
    }

    private boolean findNext(int spanStart) {
        mNextStart = spanStart;
        if (mNextStart < charEnd) {
            mNextEnd = spanned.nextSpanTransition(mNextStart, charEnd, clazz);
            T[] spanObjects = spanned.getSpans(mNextStart, mNextEnd, clazz);
            mNextObject = (spanObjects.length == 0 ? null : spanObjects[spanObjects.length - 1]);

            return true;
        }

        return false;
    }

    @Override
    public boolean hasNext() {
        return mNextStart < charEnd;
    }

    @Override
    public T next() {
        mSpanStart = mNextStart;
        mSpanEnd = mNextEnd;
        T spanObject = mNextObject;

        while (findNext(mNextEnd)) {
            if (mNextObject != null && mNextObject.equals(spanObject)) {
                mSpanEnd = mNextEnd;
            } else {
                break;
            }
        }

        return spanObject;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
