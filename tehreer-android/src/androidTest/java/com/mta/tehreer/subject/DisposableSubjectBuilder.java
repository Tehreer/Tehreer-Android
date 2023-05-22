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

import androidx.annotation.Nullable;

import java.util.function.Consumer;

public class DisposableSubjectBuilder<T extends com.mta.tehreer.Disposable, F extends T> implements SubjectBuilder<T> {
    private final UnsafeSubjectBuilder<T, F> builder;

    public DisposableSubjectBuilder(UnsafeSubjectBuilder<T, F> builder) {
        this.builder = builder;
    }

    @Override
    public T buildSubject() {
        return builder.buildSubject();
    }

    public void buildSubject(@Nullable Consumer<T> consumer) {
        T subject = null;

        try {
            subject = buildSubject();

            if (consumer != null) {
                consumer.accept (subject);
            }
        } finally {
            if  (subject != null) {
                subject.dispose();
            }
        }
    }
}
