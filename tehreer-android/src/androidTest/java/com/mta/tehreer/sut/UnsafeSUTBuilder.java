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

package com.mta.tehreer.sut;

public abstract class UnsafeSUTBuilder<T extends com.mta.tehreer.Disposable, F extends T> implements SUTBuilder<T> {
    protected final Class<T> unsafeClass;
    protected final Class<F> safeClass;

    protected UnsafeSUTBuilder(Class<T> unsafeClass, Class<F> safeClass) {
        this.unsafeClass = unsafeClass;
        this.safeClass = safeClass;
    }

    public Class<T> getUnsafeClass() {
        return unsafeClass;
    }

    public Class<F> getSafeClass() {
        return safeClass;
    }

    public DisposableSUTBuilder<T, F> getDisposableBuilder() {
        return new DisposableSUTBuilder<>(this);
    }

    public FinalizableSUTBuilder<T, F> getFinalizableBuilder() {
        return new FinalizableSUTBuilder<>(this);
    }
}
