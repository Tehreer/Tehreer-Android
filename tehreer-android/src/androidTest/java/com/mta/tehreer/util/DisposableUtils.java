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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import androidx.annotation.NonNull;

import com.mta.tehreer.Disposable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DisposableUtils {
    private static <T> Object invokeMethod(Class<T> clazz, String name,
                                           Class<?>[] parameterTypes,
                                           Object[] arguments) {
        Object object = null;

        try {
            Method method = clazz.getMethod(name, parameterTypes);
            object = method.invoke(null, arguments);
        } catch(InvocationTargetException e) {
            Throwable targetException = e.getTargetException();

            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            } else {
                fail(targetException.getMessage());
            }
        } catch(Exception e) {
            fail("Could not invoke `" + name + "` method on `" + clazz.getSimpleName() + "` class");
        }

        return object;
    }

    public static @NonNull <T extends Disposable, F extends T> F invokeFinalizable(Class<T> clazz, T disposable) {
        // Given
        String name = "finalizable";
        Class<?>[] parameterTypes = new Class[] { clazz };
        Object[] arguments = new Object[] { disposable };

        // When
        Object finalizable = invokeMethod(clazz, name, parameterTypes, arguments);

        // Then
        assertNotNull(finalizable);
        assertTrue(clazz.isInstance(finalizable));

        return (F) finalizable;
    }

    public static <T extends Disposable> boolean invokeIsFinalizable(Class<T> clazz, T disposable) {
        // Given
        String name = "isFinalizable";
        Class<?>[] parameterTypes = new Class[] { clazz };
        Object[] arguments = new Object[] { disposable };

        // When
        Object value = invokeMethod(clazz, name, parameterTypes, arguments);

        // Then
        assertNotNull(value);
        assertTrue(value instanceof Boolean);

        return (boolean) value;
    }

    private DisposableUtils() { }
}
