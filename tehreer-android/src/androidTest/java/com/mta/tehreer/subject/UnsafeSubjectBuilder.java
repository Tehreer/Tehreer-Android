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

package com.mta.tehreer.subject;

public abstract class UnsafeSubjectBuilder<T extends com.mta.tehreer.Disposable, F extends T> implements SubjectBuilder<T> {
    protected final Class<T> unsafeClass;
    protected final Class<F> safeClass;

    protected UnsafeSubjectBuilder(Class<T> unsafeClass, Class<F> safeClass) {
        this.unsafeClass = unsafeClass;
        this.safeClass = safeClass;
    }

    public Class<T> getUnsafeClass() {
        return unsafeClass;
    }

    public Class<F> getSafeClass() {
        return safeClass;
    }

    public DisposableSubjectBuilder<T, F> getDisposableBuilder() {
        return new DisposableSubjectBuilder<>(this);
    }

    public FinalizableSubjectBuilder<T, F> getFinalizableBuilder() {
        return new FinalizableSubjectBuilder<>(this);
    }
}
