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

public class Description {

    private static final String NULL = "null";

    public static String forByteArray(byte[] array) {
        if (array != null) {
            Description description = new Description();
            description.begin();
            for (byte value : array) {
                description.append(String.valueOf(value));
            }
            description.end();

            return description.toString();
        }

        return NULL;
    }

    private final StringBuilder builder = new StringBuilder();
    private boolean begun = false;

    private Description() {
    }

    private void begin() {
        builder.append("[");
        begun = true;
    }

    private void append(String value) {
        if (!begun) {
            builder.append(", ");
        }
        builder.append(value);
        begun = false;
    }

    private void end() {
        builder.append("]");
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
