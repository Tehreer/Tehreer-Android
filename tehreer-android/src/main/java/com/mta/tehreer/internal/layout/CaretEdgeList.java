/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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

package com.mta.tehreer.internal.layout;

import android.support.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.internal.Exceptions;

public final class CaretEdgeList extends FloatList {
    private final @NonNull float[] extentArray;
    private final int offset;
    private final int edgeCount;
    private final float pivotDistance;

    public CaretEdgeList(@NonNull float[] extents, int offset, int size,
                         int startExtra, int endExtra, boolean isBackward, boolean isRTL) {
        boolean isOpposite = (isBackward ^ isRTL);
        this.extentArray = extents;
        this.offset = offset + (isBackward ? 0 : -1);
        this.edgeCount = size + 1;
        this.pivotDistance = edgeAt(isOpposite ? size - endExtra : startExtra);
    }

    private float edgeAt(int index) {
        int relativeIndex = index + offset;
        if (relativeIndex == -1 || relativeIndex == extentArray.length) {
            return 0.0f;
        }

        return extentArray[relativeIndex];
    }

    public float distance(int fromIndex, int toIndex, boolean isOpposite) {
        float firstEdge = edgeAt(fromIndex);
        float lastEdge = edgeAt(toIndex);

        return (isOpposite ? firstEdge - lastEdge : lastEdge - firstEdge);
    }

    @Override
    public int size() {
        return edgeCount;
    }

    @Override
    public float get(int index) {
        if (index < 0 || index >= edgeCount) {
            throw Exceptions.indexOutOfBounds(index, edgeCount);
        }

        return edgeAt(index) - pivotDistance;
    }

    @Override
    public void copyTo(@NonNull float[] array, int atIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatList subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
}
