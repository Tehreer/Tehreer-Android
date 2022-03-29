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
import com.mta.tehreer.sut.UnsafeSUTBuilder;
import com.mta.tehreer.util.DisposableUtils;
import com.mta.tehreer.sut.SUTBuilder;

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
    private final UnsafeSUTBuilder<T, F> sutBuilder;

    private Consumer<SUTBuilder<?>> onPreBuildSUT;
    private Consumer<SUTBuilder<?>> onPostBuildSUT;

    protected DisposableTestSuite(@NonNull UnsafeSUTBuilder<T, F> sutBuilder) {
        this(sutBuilder, DefaultMode.NONE);
    }

    protected DisposableTestSuite(@NonNull UnsafeSUTBuilder<T, F> sutBuilder,
                                  @NonNull DefaultMode defaultMode) {
        this.sutBuilder = sutBuilder;
        this.defaultMode = defaultMode;
    }

    protected Class<T> getUnsafeClass() {
        return sutBuilder.getUnsafeClass();
    }

    protected Class<F> getSafeClass() {
        return sutBuilder.getSafeClass();
    }

    protected void setOnPreBuildSUT(@Nullable Consumer<SUTBuilder<?>> onPreBuildSUT) {
        this.onPreBuildSUT = onPreBuildSUT;
    }

    protected void setOnPostBuildSUT(@Nullable Consumer<SUTBuilder<?>> onPostBuildSUT) {
        this.onPostBuildSUT = onPostBuildSUT;
    }

    private void onPreBuildSUT() {
        if (onPreBuildSUT != null) {
            onPreBuildSUT.accept(sutBuilder);
        }
    }

    private void onPostBuildSUT() {
        if (onPostBuildSUT != null) {
            onPostBuildSUT.accept(sutBuilder);
        }
    }

    protected T buildUnsafeSUT() {
        onPreBuildSUT();
        T sut = sutBuilder.buildSUT();
        onPostBuildSUT();

        return sut;
    }

    protected F buildSafeSUT() {
        onPreBuildSUT();
        F sut = sutBuilder.getFinalizableBuilder().buildSUT();
        onPostBuildSUT();

        return sut;
    }

    protected void buildDisposableSUT(@Nullable Consumer<T> consumer) {
        onPreBuildSUT();
        sutBuilder.getDisposableBuilder().buildSUT(consumer);
        onPostBuildSUT();
    }

    protected void buildSafeSUT(@Nullable Consumer<T> consumer) {
        onPreBuildSUT();

        SUTBuilder<F> builder = sutBuilder.getFinalizableBuilder();
        builder.buildSUT((sut) -> {
            if (consumer != null) {
                consumer.accept(sut);
            }
        });

        onPostBuildSUT();
    }

    protected void buildSUT(@Nullable Consumer<T> consumer) {
        switch (defaultMode) {
            case NONE:
                fail("Default mode not specified");
                break;
            case DISPOSABLE:
                buildDisposableSUT(consumer);
                break;
            case SAFE:
                buildSafeSUT(consumer);
                break;
        }
    }

    public static abstract class StaticTestSuite<T extends Disposable, F extends T> extends DisposableTestSuite<T, F> {
        protected StaticTestSuite(UnsafeSUTBuilder<T, F> sutBuilder) {
            super(sutBuilder);
        }

        protected boolean invokeIsFinalizable(T sut) {
            return DisposableUtils.invokeIsFinalizable(getUnsafeClass(), sut);
        }

        protected F invokeFinalizable(T sut) {
            return DisposableUtils.invokeFinalizable(getUnsafeClass(), sut);
        }

        @Test
        public void testIsFinalizableForUnsafeInstance() {
            buildDisposableSUT((sut) -> {
                // When
                boolean isFinalizable = invokeIsFinalizable(sut);

                // Then
                assertFalse(isFinalizable);
            });
        }

        @Test
        public void testIsFinalizableForSafeInstance() {
            // Given
            T sut = buildSafeSUT();

            // When
            boolean isFinalizable = invokeIsFinalizable(sut);

            // Then
            assertTrue(isFinalizable);
        }

        @Test
        public void testFinalizableForUnsafeInstance() {
            // Given
            T sut = buildUnsafeSUT();

            // When
            F object = invokeFinalizable(sut);

            // Then
            assertSame(object.getClass(), getSafeClass());
        }

        @Test
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
            T sut = buildSafeSUT();

            // When
            F object = invokeFinalizable(sut);

            // Then
            assertSame(object, sut);
        }
    }
}
