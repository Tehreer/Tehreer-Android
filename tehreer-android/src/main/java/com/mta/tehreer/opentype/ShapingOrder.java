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

package com.mta.tehreer.opentype;

/**
 * Specifies text's flow mode in memory.
 */
public enum ShapingOrder {
    /**
     * Text flows in forward mode starting from first index (inclusive) to last index (exclusive).
     */
    FORWARD(0),
    /**
     * Text flows in backward mode starting from last index (exclusive) to first index (inclusive).
     */
    BACKWARD(1);

    final int value;

    ShapingOrder(int value) {
        this.value = value;
    }
}
