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

import static org.junit.Assert.fail;

import com.mta.tehreer.util.DisposableUtils;

public class FinalizableSubjectBuilder<T extends com.mta.tehreer.Disposable, F extends T> implements SubjectBuilder<F> {
    private final UnsafeSubjectBuilder<T, F> builder;

    public FinalizableSubjectBuilder(UnsafeSubjectBuilder<T, F> builder) {
        this.builder = builder;
    }

    public F buildSubject() {
        T unsafeSubject = builder.buildSubject();
        F subject = null;

        try {
            subject = DisposableUtils.invokeFinalizable(builder.unsafeClass, unsafeSubject);
        } catch (Throwable throwable) {
            fail(throwable.getMessage());
        }

        return subject;
    }
}
