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

package com.mta.tehreer.internal.util;

import com.mta.tehreer.opentype.ShapingDirection;
import com.mta.tehreer.opentype.ShapingOrder;

public class Convert {

    private static final ShapingDirection[] NATIVE_TEXT_DIRECTION_MAP;
    private static final ShapingOrder[] NATIVE_TEXT_MODE_MAP;

    static {
        NATIVE_TEXT_DIRECTION_MAP = new ShapingDirection[] {
                ShapingDirection.LEFT_TO_RIGHT,
                ShapingDirection.RIGHT_TO_LEFT
        };

        NATIVE_TEXT_MODE_MAP = new ShapingOrder[] {
                ShapingOrder.FORWARD,
                ShapingOrder.BACKWARD
        };
    }

    public static ShapingDirection toJavaTextDirection(int nativeTextDirection) {
        return NATIVE_TEXT_DIRECTION_MAP[nativeTextDirection];
    }

    public static ShapingOrder toJavaTextMode(int nativeTextMode) {
        return NATIVE_TEXT_MODE_MAP[nativeTextMode];
    }

    public static String toStringTag(int tag) {
        char[] chars = {
            (char) (tag >> 24),
            (char) ((tag >> 16) & 0xFF),
            (char) ((tag >> 8) & 0xFF),
            (char) (tag & 0xFF)
        };

        return new String(chars);
    }
}
