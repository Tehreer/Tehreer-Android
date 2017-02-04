/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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
#include <cstring>
#include <jni.h>

#include "JavaBridge.h"
#include "Raw.h"

using namespace Tehreer;

static jboolean isEqual(JNIEnv *env, jobject obj, jlong pointer, jlong other, jint size)
{
    jbyte *memory = reinterpret_cast<jbyte *>(pointer);
    jbyte *chunk = reinterpret_cast<jbyte *>(other);
    int result = memcmp(memory, chunk, static_cast<size_t>(size));

    return static_cast<jboolean>(result == 0);
}

static jint getHash(JNIEnv *env, jobject obj, jlong pointer, jint size)
{
    jbyte *memory = reinterpret_cast<jbyte *>(pointer);

    jint result = 1;
    for (size_t i = 0; i < size; i++) {
        result = 31 * result + memory[i];
    }

    return result;
}

static jbyte getByte(JNIEnv *env, jobject obj, jlong pointer, jint index)
{
    jbyte *memory = reinterpret_cast<jbyte *>(pointer);
    jbyte value = memory[index];

    return value;
}

static void putByte(JNIEnv *env, jobject obj, jlong pointer, jint index, jbyte value)
{
    jbyte *memory = reinterpret_cast<jbyte *>(pointer);
    memory[index] = value;
}

static jbyteArray toByteArray(JNIEnv *env, jobject obj, jlong pointer, jint size)
{
    jbyte *memory = reinterpret_cast<jbyte *>(pointer);
    jbyteArray array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, memory);

    return array;
}

static JNINativeMethod JNI_METHODS[] = {
    { "isEqual", "(JJI)Z", (void *)isEqual },
    { "getHash", "(JI)I", (void *)getHash },
    { "getByte", "(JI)B", (void *)getByte },
    { "putByte", "(JIB)V", (void *)putByte },
    { "toByteArray", "(JI)[B", (void *)toByteArray },
};

jint register_com_mta_tehreer_internal_Raw(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/internal/Raw", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
