/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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

#include "Test.h"

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    JNIEnv *env;
    jint result;

    if (jvm->GetEnv((void **)&env, JNI_VERSION_1_6)) {
        return JNI_ERR;
    }

    if (env == nullptr) {
        return JNI_ERR;
    }

    result = register_com_mta_tehreer_internal_Memory(env) == JNI_OK;

    if (!result) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}
