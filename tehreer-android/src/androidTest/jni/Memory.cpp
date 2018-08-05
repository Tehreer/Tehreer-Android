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

#include <cstddef>
#include <cstdlib>
#include <jni.h>

#include "JavaBridge.h"
#include "Memory.h"

using namespace Tehreer;

static jlong allocate(JNIEnv *env, jobject obj, jlong capacity)
{
    return (jlong)malloc((size_t)capacity);
}

static void dispose(JNIEnv *env, jobject obj, jlong pointer)
{
    free((void *)pointer);
}

static jobject buffer(JNIEnv *env, jobject obj, jlong pointer, jlong capacity)
{
    return env->NewDirectByteBuffer((void *)pointer, capacity);
}

static JNINativeMethod JNI_METHODS[] = {
    { "allocate", "(J)J", (void *)allocate },
    { "dispose", "(J)V", (void *)dispose },
    { "buffer", "(JJ)Ljava/nio/ByteBuffer;", (void *)buffer },
};

jint register_com_mta_tehreer_internal_Memory(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/internal/Memory", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
