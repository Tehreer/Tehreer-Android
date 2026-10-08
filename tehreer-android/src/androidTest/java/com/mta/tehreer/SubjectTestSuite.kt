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

import com.mta.tehreer.subject.SubjectBuilder

abstract class SubjectTestSuite<T>(private val subjectBuilder: SubjectBuilder<T>) {
    protected var onPreBuildSubject: ((SubjectBuilder<*>) -> Unit)? = null
    protected var onPostBuildSubject: ((SubjectBuilder<*>) -> Unit)? = null

    protected fun buildSubject(): T {
        onPreBuildSubject?.invoke(subjectBuilder)
        val subject = subjectBuilder.buildSubject()
        onPostBuildSubject?.invoke(subjectBuilder)

        return subject
    }

    protected fun buildSubject(consumer: (T) -> Unit) {
        consumer(buildSubject())
    }
}
