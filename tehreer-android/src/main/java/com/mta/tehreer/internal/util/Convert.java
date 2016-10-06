/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

import com.mta.tehreer.text.TextDirection;
import com.mta.tehreer.text.TextMode;

import java.util.EnumMap;

public class Convert {

    private static final EnumMap<TextDirection, Integer> JAVA_TEXT_DIRECTION_MAP;
    private static final TextDirection[] NATIVE_TEXT_DIRECTION_MAP;
    private static final int NATIVE_TEXT_DIRECTION_OFFSET = 1;

    private static final EnumMap<TextMode, Integer> JAVA_TEXT_MODE_MAP;
    private static final TextMode[] NATIVE_TEXT_MODE_MAP;

    static {
        JAVA_TEXT_DIRECTION_MAP = new EnumMap<>(TextDirection.class);
        JAVA_TEXT_DIRECTION_MAP.put(TextDirection.DEFAULT, 0 - NATIVE_TEXT_DIRECTION_OFFSET);
        JAVA_TEXT_DIRECTION_MAP.put(TextDirection.LEFT_TO_RIGHT, 1 - NATIVE_TEXT_DIRECTION_OFFSET);
        JAVA_TEXT_DIRECTION_MAP.put(TextDirection.RIGHT_TO_LEFT, 2 - NATIVE_TEXT_DIRECTION_OFFSET);

        NATIVE_TEXT_DIRECTION_MAP = new TextDirection[] {
                TextDirection.DEFAULT,
                TextDirection.LEFT_TO_RIGHT,
                TextDirection.RIGHT_TO_LEFT
        };

        JAVA_TEXT_MODE_MAP = new EnumMap<>(TextMode.class);
        JAVA_TEXT_MODE_MAP.put(TextMode.FORWARD, 0);
        JAVA_TEXT_MODE_MAP.put(TextMode.BACKWARD, 1);

        NATIVE_TEXT_MODE_MAP = new TextMode[] {
                TextMode.FORWARD,
                TextMode.BACKWARD
        };
    }

    public static int toNativeTextDirection(TextDirection javaTextDirection) {
        return JAVA_TEXT_DIRECTION_MAP.get(javaTextDirection);
    }

    public static TextDirection toJavaTextDirection(int nativeTextDirection) {
        return NATIVE_TEXT_DIRECTION_MAP[nativeTextDirection + NATIVE_TEXT_DIRECTION_OFFSET];
    }

    public static int toNativeTextMode(TextMode javaTextMode) {
        return JAVA_TEXT_MODE_MAP.get(javaTextMode);
    }

    public static TextMode toJavaTextMode(int nativeTextMode) {
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
