/*
 * Copyright (C) 2018-2019 Muhammad Tayyab Akram
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
    private final @NonNull FloatList allEdges;
    private final int offset;
    private final int edgeCount;
    private final float pivotDistance;

    public CaretEdgeList(@NonNull FloatList allEdges) {
        this(allEdges, 0, allEdges.size(), 0, 0, false);
    }

    public CaretEdgeList(@NonNull FloatList allEdges, int chunkOffset, int chunkLength,
                         int startExtra, int endExtra, boolean visuallyRTL) {
        this.allEdges = allEdges;
        this.offset = chunkOffset;
        this.edgeCount = chunkLength + 1;
        this.pivotDistance = edgeAt(visuallyRTL ? chunkLength - endExtra : startExtra);
    }

    private float edgeAt(int index) {
        return allEdges.get(index + offset);
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
