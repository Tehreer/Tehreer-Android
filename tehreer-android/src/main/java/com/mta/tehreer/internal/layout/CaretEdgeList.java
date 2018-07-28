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

import com.mta.tehreer.collections.FloatList;
import com.mta.tehreer.internal.Exceptions;

public final class CaretEdgeList extends FloatList {
    private final float[] extentArray;
    private final int beforeOffset;
    private final int edgeCount;
    private final boolean reversed;
    private final float pivotDistance;

    public CaretEdgeList(float[] extents, int offset, int size, int startExtra, int endExtra, boolean reversed) {
        int pivotIndex = (reversed ? size - endExtra : startExtra) + offset - 1;
        this.extentArray = extents;
        this.beforeOffset = offset - 1;
        this.edgeCount = size + 1;
        this.reversed = reversed;
        this.pivotDistance = (pivotIndex == -1 ? 0.0f : extentArray[pivotIndex]);
    }

    public float distance(int fromIndex, int toIndex) {
        int relativeFromIndex = fromIndex + beforeOffset;
        int relativeToIndex = toIndex + beforeOffset;

        float firstEdge = (relativeFromIndex == -1 ? 0.0f : extentArray[relativeFromIndex]);
        float lastEdge = (relativeToIndex == -1 ? 0.0f : extentArray[relativeToIndex]);

        return lastEdge - firstEdge;
    }

    public boolean reversed() {
        return reversed;
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

        int relativeIndex = index + beforeOffset;
        float relativeExtent = (relativeIndex == -1 ? 0.0f : extentArray[relativeIndex]);

        return (reversed ? pivotDistance - relativeExtent : relativeExtent - pivotDistance);
    }

    @Override
    public void copyTo(float[] array, int atIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FloatList subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
}
