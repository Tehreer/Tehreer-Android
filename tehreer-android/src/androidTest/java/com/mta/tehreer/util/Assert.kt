/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

package com.mta.tehreer.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

object Assert {
    fun <T : Throwable> assertThrows(clazz: Class<T>, runnable: Runnable) {
        try {
            runnable.run()
            fail()
        } catch (throwable: Throwable) {
            assertTrue(clazz.isInstance(throwable))
        }
    }

    fun <T : Throwable> assertThrows(clazz: Class<T>, message: String, runnable: Runnable) {
        try {
            runnable.run()
            fail()
        } catch (throwable: Throwable) {
            assertTrue(clazz.isInstance(throwable))
            assertEquals(throwable.message, message)
        }
    }
}
