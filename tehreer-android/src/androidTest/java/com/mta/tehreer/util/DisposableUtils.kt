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

import com.mta.tehreer.Disposable

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

import java.lang.reflect.InvocationTargetException

object DisposableUtils {
    private fun <T> invokeMethod(clazz: Class<T>, name: String, parameterTypes: Array<Class<*>>, arguments: Array<Any>): Any? {
        var result: Any? = null

        try {
            val method = clazz.getMethod(name, *parameterTypes)
            result = method.invoke(null, *arguments)
        } catch (e: InvocationTargetException) {
            val targetException = e.targetException

            if (targetException is RuntimeException) {
                throw targetException
            } else {
                fail(targetException.message)
            }
        } catch (e: Exception) {
            fail("Could not invoke `" + name + "` method on `" + clazz.simpleName + "` class")
        }

        return result
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T : Disposable> invokeFinalizable(clazz: Class<T>, disposable: T): T {
        // Given
        val name = "finalizable"
        val parameterTypes: Array<Class<*>> = arrayOf(clazz)
        val arguments: Array<Any> = arrayOf(disposable)

        // When
        val finalizable = invokeMethod(clazz, name, parameterTypes, arguments)

        // Then
        assertNotNull(finalizable)
        assertTrue(clazz.isInstance(finalizable))

        return finalizable as T
    }

    @JvmStatic
    fun <T : Disposable> invokeIsFinalizable(clazz: Class<T>, disposable: T): Boolean {
        // Given
        val name = "isFinalizable"
        val parameterTypes: Array<Class<*>> = arrayOf(clazz)
        val arguments: Array<Any> = arrayOf(disposable)

        // When
        val value = invokeMethod(clazz, name, parameterTypes, arguments)

        // Then
        assertNotNull(value)
        assertTrue(value is Boolean)

        return value as Boolean
    }
}
