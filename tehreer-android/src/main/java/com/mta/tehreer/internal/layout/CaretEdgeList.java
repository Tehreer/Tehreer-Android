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
    private final float extraDistance;
    public final float coveredDistance;

    public CaretEdgeList(float[] extents, int offset, int size, boolean reversed) {
        this.extentArray = extents;
        this.beforeOffset = offset - 1;
        this.edgeCount = size + 1;
        this.reversed = reversed;
        this.extraDistance = (offset == 0 ? 0.0f : extentArray[beforeOffset]);
        this.coveredDistance = extentArray[beforeOffset + size] - extraDistance;
    }

    public float distance(int fromIndex, int toIndex) {
        float firstEdge = (fromIndex == 0 ? 0.0f : extentArray[fromIndex + beforeOffset] - extraDistance);
        float lastEdge = (toIndex == 0 ? 0.0f : extentArray[toIndex + beforeOffset] - extraDistance);

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

        float caretEdge = (index == 0 ? 0.0f : (extentArray[index + beforeOffset] - extraDistance));
        if (reversed) {
            caretEdge = coveredDistance - caretEdge;
        }

        return caretEdge;
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
