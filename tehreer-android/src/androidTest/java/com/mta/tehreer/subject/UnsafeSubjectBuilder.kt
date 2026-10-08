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

package com.mta.tehreer.subject

import com.mta.tehreer.Disposable

abstract class UnsafeSubjectBuilder<T : Disposable>(val unsafeClass: Class<T>) : SubjectBuilder<T> {
    val safeClass: Class<out T> by lazy {
        Class.forName(unsafeClass.name + "\$Finalizable").asSubclass(unsafeClass)
    }

    val disposableBuilder: DisposableSubjectBuilder<T>
        get() = DisposableSubjectBuilder(this)

    val finalizableBuilder: FinalizableSubjectBuilder<T>
        get() = FinalizableSubjectBuilder(this)
}
