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

#ifndef _TEHREER__FLOAT_COLLECTOR_H
#define _TEHREER__FLOAT_COLLECTOR_H

#include <jni.h>

namespace Tehreer {

/* Hands floats one by one to a `FloatCollector` of Java, instead of filling an array. */
class FloatCollector {
public:
    FloatCollector(JNIEnv *env, jobject collector)
        : m_env(env)
        , m_collector(collector)
        , m_add(env->GetMethodID(env->GetObjectClass(collector), "add", "(F)V"))
    {
    }

    void add(jfloat value) const { m_env->CallVoidMethod(m_collector, m_add, value); }

private:
    JNIEnv *m_env;
    jobject m_collector;
    jmethodID m_add;
};

}

#endif
