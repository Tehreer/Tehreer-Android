/*
 * Copyright (C) 2022-2023 Muhammad Tayyab Akram
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

package com.mta.tehreer;

import static com.mta.tehreer.util.Assert.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mta.tehreer.internal.Constants;
import com.mta.tehreer.subject.UnsafeSubjectBuilder;
import com.mta.tehreer.util.DisposableUtils;
import com.mta.tehreer.subject.SubjectBuilder;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Consumer;

@RunWith(MockitoJUnitRunner.class)
public abstract class DisposableTestSuite<T extends Disposable, F extends T> {
    public enum DefaultMode {
        NONE,
        DISPOSABLE,
        SAFE,
    }

    private final DefaultMode defaultMode;
    private final UnsafeSubjectBuilder<T, F> subjectBuilder;

    private Consumer<SubjectBuilder<?>> onPreBuildSubject;
    private Consumer<SubjectBuilder<?>> onPostBuildSubject;

    protected DisposableTestSuite(@NonNull UnsafeSubjectBuilder<T, F> subjectBuilder) {
        this(subjectBuilder, DefaultMode.NONE);
    }

    protected DisposableTestSuite(@NonNull UnsafeSubjectBuilder<T, F> subjectBuilder,
                                  @NonNull DefaultMode defaultMode) {
        this.subjectBuilder = subjectBuilder;
        this.defaultMode = defaultMode;
    }

    protected Class<T> getUnsafeClass() {
        return subjectBuilder.getUnsafeClass();
    }

    protected Class<F> getSafeClass() {
        return subjectBuilder.getSafeClass();
    }

    protected void setOnPreBuildSubject(@Nullable Consumer<SubjectBuilder<?>> onPreBuildSubject) {
        this.onPreBuildSubject = onPreBuildSubject;
    }

    protected void setOnPostBuildSubject(@Nullable Consumer<SubjectBuilder<?>> onPostBuildSubject) {
        this.onPostBuildSubject = onPostBuildSubject;
    }

    private void onPreBuildSubject() {
        if (onPreBuildSubject != null) {
            onPreBuildSubject.accept(subjectBuilder);
        }
    }

    private void onPostBuildSubject() {
        if (onPostBuildSubject != null) {
            onPostBuildSubject.accept(subjectBuilder);
        }
    }

    protected T buildUnsafeSubject() {
        onPreBuildSubject();
        T subject = subjectBuilder.buildSubject();
        onPostBuildSubject();

        return subject;
    }

    protected F buildSafeSubject() {
        onPreBuildSubject();
        F subject = subjectBuilder.getFinalizableBuilder().buildSubject();
        onPostBuildSubject();

        return subject;
    }

    protected void buildDisposableSubject(@Nullable Consumer<T> consumer) {
        onPreBuildSubject();
        subjectBuilder.getDisposableBuilder().buildSubject(consumer);
        onPostBuildSubject();
    }

    protected void buildSafeSubject(@Nullable Consumer<T> consumer) {
        onPreBuildSubject();

        SubjectBuilder<F> builder = subjectBuilder.getFinalizableBuilder();
        builder.buildSubject( (subject) -> {
            if (consumer != null) {
                consumer.accept (subject);
            }
        });

        onPostBuildSubject();
    }

    protected void buildSubject(@Nullable Consumer<T> consumer) {
        switch (defaultMode) {
            case NONE:
                fail("Default mode not specified");
                break;
            case DISPOSABLE:
                buildDisposableSubject(consumer);
                break;
            case SAFE:
                buildSafeSubject(consumer);
                break;
        }
    }

    public static abstract class StaticTestSuite<T extends Disposable, F extends T> extends DisposableTestSuite<T, F> {
        protected StaticTestSuite(UnsafeSubjectBuilder<T, F> subjectBuilder) {
            super (subjectBuilder);
        }

        protected boolean invokeIsFinalizable(T subject) {
            return DisposableUtils.invokeIsFinalizable(getUnsafeClass(), subject);
        }

        protected F invokeFinalizable(T subject) {
            return DisposableUtils.invokeFinalizable(getUnsafeClass(), subject);
        }

        @Test
        public void testIsFinalizableForUnsafeInstance() {
            buildDisposableSubject( (subject) -> {
                // When
                boolean isFinalizable = invokeIsFinalizable (subject);

                // Then
                assertFalse(isFinalizable);
            });
        }

        @Test
        public void testIsFinalizableForSafeInstance() {
            // Given
            T subject = buildSafeSubject();

            // When
            boolean isFinalizable = invokeIsFinalizable (subject);

            // Then
            assertTrue(isFinalizable);
        }

        @Test
        public void testFinalizableForUnsafeInstance() {
            // Given
            T subject = buildUnsafeSubject();

            // When
            F object = invokeFinalizable (subject);

            // Then
            assertSame(object.getClass(), getSafeClass());
        }

        @Test
        @Ignore
        public void testFinalizableForMockInstance() {
            // Given
            T mock = mock(getUnsafeClass());

            // Then
            assertThrows(IllegalArgumentException.class,
                    Constants.EXCEPTION_SUBCLASS_NOT_SUPPORTED,
                    () -> invokeFinalizable(mock) );
        }

        @Test
        public void testFinalizableForSameInstance() {
            // Given
            T subject = buildSafeSubject();

            // When
            F object = invokeFinalizable (subject);

            // Then
            assertSame(object, subject);
        }
    }
}
