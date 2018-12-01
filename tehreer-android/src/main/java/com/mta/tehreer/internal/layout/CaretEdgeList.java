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

import androidx.annotation.NonNull;

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.internal.Exceptions;

public final class CaretEdgeList extends FloatList {
    private final @NonNull float[] extentArray;
    private final int offset;
    private final int edgeCount;
    private final float pivotDistance;

    public CaretEdgeList(@NonNull float[] charExtents, boolean backward) {
        this(charExtents, 0, charExtents.length, 0, 0, backward, false);
    }

    public CaretEdgeList(@NonNull float[] charExtents, int chunkOffset, int chunkLength,
                         int startExtra, int endExtra, boolean backward, boolean visuallyRTL) {
        this.extentArray = charExtents;
        this.offset = chunkOffset + (backward ? 0 : -1);
        this.edgeCount = chunkLength + 1;
        this.pivotDistance = edgeAt(visuallyRTL ? chunkLength - endExtra : startExtra);
    }

    private float edgeAt(int index) {
        int relativeIndex = index + offset;
        if (relativeIndex == -1 || relativeIndex == extentArray.length) {
            return 0.0f;
        }

        return extentArray[relativeIndex];
    }

    public float distance(int fromIndex, int toIndex, boolean visuallyRTL) {
        float firstEdge = edgeAt(fromIndex);
        float lastEdge = edgeAt(toIndex);

        return (visuallyRTL ? firstEdge - lastEdge : lastEdge - firstEdge);
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
