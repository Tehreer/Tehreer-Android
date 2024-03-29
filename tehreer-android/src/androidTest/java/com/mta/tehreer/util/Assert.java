/*
 * Copyright (C) 2022 Muhammad Tayyab Akram
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

package com.mta.tehreer.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Assert {
    public static <T extends Throwable> void assertThrows(Class<T> clazz, Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (Throwable throwable) {
            assertTrue(clazz.isInstance(throwable));
        }
    }

    public static <T extends Throwable> void assertThrows(Class<T> clazz, String message, Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (Throwable throwable) {
            assertTrue(clazz.isInstance(throwable));
            assertEquals(throwable.getMessage(), message);
        }
    }
}
