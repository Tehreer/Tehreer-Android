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

package com.mta.tehreer

import com.mta.tehreer.internal.Constants
import com.mta.tehreer.subject.SubjectBuilder
import com.mta.tehreer.subject.UnsafeSubjectBuilder
import com.mta.tehreer.util.Assert.assertThrows
import com.mta.tehreer.util.DisposableUtils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
abstract class DisposableTestSuite<T : Disposable>(
    private val subjectBuilder: UnsafeSubjectBuilder<T>,
    private val defaultMode: DefaultMode = DefaultMode.NONE
) {
    enum class DefaultMode {
        NONE,
        DISPOSABLE,
        SAFE,
    }

    protected val unsafeClass: Class<T>
        get() = subjectBuilder.unsafeClass

    protected val safeClass: Class<out T>
        get() = subjectBuilder.safeClass

    protected var onPreBuildSubject: ((SubjectBuilder<*>) -> Unit)? = null
    protected var onPostBuildSubject: ((SubjectBuilder<*>) -> Unit)? = null

    private fun notifyPreBuildSubject() {
        onPreBuildSubject?.invoke(subjectBuilder)
    }

    private fun notifyPostBuildSubject() {
        onPostBuildSubject?.invoke(subjectBuilder)
    }

    protected fun buildUnsafeSubject(): T {
        notifyPreBuildSubject()
        val subject = subjectBuilder.buildSubject()
        notifyPostBuildSubject()

        return subject
    }

    protected fun buildSafeSubject(): T {
        notifyPreBuildSubject()
        val subject = subjectBuilder.finalizableBuilder.buildSubject()
        notifyPostBuildSubject()

        return subject
    }

    protected fun buildDisposableSubject(consumer: ((T) -> Unit)?) {
        notifyPreBuildSubject()
        subjectBuilder.disposableBuilder.buildSubject(consumer)
        notifyPostBuildSubject()
    }

    protected fun buildSafeSubject(consumer: ((T) -> Unit)?) {
        notifyPreBuildSubject()

        val builder: SubjectBuilder<T> = subjectBuilder.finalizableBuilder
        builder.buildSubject { subject ->
            consumer?.invoke(subject)
        }

        notifyPostBuildSubject()
    }

    protected fun buildSubject(consumer: ((T) -> Unit)?) {
        when (defaultMode) {
            DefaultMode.NONE -> fail("Default mode not specified")
            DefaultMode.DISPOSABLE -> buildDisposableSubject(consumer)
            DefaultMode.SAFE -> buildSafeSubject(consumer)
        }
    }

    abstract class StaticTestSuite<T : Disposable>(
        subjectBuilder: UnsafeSubjectBuilder<T>
    ) : DisposableTestSuite<T>(subjectBuilder) {
        protected fun invokeIsFinalizable(subject: T): Boolean {
            return DisposableUtils.invokeIsFinalizable(unsafeClass, subject)
        }

        protected fun invokeFinalizable(subject: T): T {
            return DisposableUtils.invokeFinalizable(unsafeClass, subject)
        }

        @Test
        fun testIsFinalizableForUnsafeInstance() {
            buildDisposableSubject { subject ->
                // When
                val isFinalizable = invokeIsFinalizable(subject)

                // Then
                assertFalse(isFinalizable)
            }
        }

        @Test
        fun testIsFinalizableForSafeInstance() {
            // Given
            val subject = buildSafeSubject()

            // When
            val isFinalizable = invokeIsFinalizable(subject)

            // Then
            assertTrue(isFinalizable)
        }

        @Test
        fun testFinalizableForUnsafeInstance() {
            // Given
            val subject = buildUnsafeSubject()

            // When
            val finalizable = invokeFinalizable(subject)

            // Then
            assertSame(finalizable.javaClass, safeClass)
        }

        @Test
        @Ignore
        fun testFinalizableForMockInstance() {
            // Given
            val mock = mock(unsafeClass)

            // Then
            assertThrows(IllegalArgumentException::class.java,
                         Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED) { invokeFinalizable(mock) }
        }

        @Test
        fun testFinalizableForSameInstance() {
            // Given
            val subject = buildSafeSubject()

            // When
            val finalizable = invokeFinalizable(subject)

            // Then
            assertSame(finalizable, subject)
        }
    }
}
