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
#include <cstdint>
#include <cstring>
#include <jni.h>

#include "JavaBridge.h"
#include "Raw.h"

using namespace Tehreer;

static jboolean isEqual(JNIEnv *env, jobject obj, jlong pointer, jlong other, jint bufferSize)
{
    jbyte *memory = reinterpret_cast<jbyte *>(pointer);
    jbyte *chunk = reinterpret_cast<jbyte *>(other);
    int result = memcmp(memory, chunk, static_cast<size_t>(bufferSize));

    return static_cast<jboolean>(result == 0);
}

static jint getHash(JNIEnv *env, jobject obj, jlong pointer, jint bufferSize)
{
    jbyte *memory = reinterpret_cast<jbyte *>(pointer);

    jint result = 1;
    for (size_t i = 0; i < bufferSize; i++) {
        result = 31 * result + memory[i];
    }

    return result;
}

static jbyte getInt8(JNIEnv *env, jobject obj, jlong pointer, jint arrayIndex)
{
    int8_t *memory = reinterpret_cast<int8_t *>(pointer);
    jbyte value = memory[arrayIndex];

    return value;
}

static jint getUInt16(JNIEnv *env, jobject obj, jlong pointer, jint arrayIndex)
{
    uint16_t *memory = reinterpret_cast<uint16_t *>(pointer);
    jint value = memory[arrayIndex];

    return value;
}

static void putInt8(JNIEnv *env, jobject obj, jlong pointer, jint arrayIndex, jbyte value)
{
    int8_t *memory = reinterpret_cast<int8_t *>(pointer);
    memory[arrayIndex] = static_cast<int8_t>(value);
}

static void putUInt16(JNIEnv *env, jobject obj, jlong pointer, jint arrayIndex, jint value)
{
    uint16_t *memory = reinterpret_cast<uint16_t *>(pointer);
    memory[arrayIndex] = static_cast<uint16_t>(value);
}

static jbyteArray toInt8Array(JNIEnv *env, jobject obj, jlong arrayIndex, jint size)
{
    int8_t *memory = reinterpret_cast<int8_t *>(arrayIndex);
    jbyteArray array = env->NewByteArray(size);
    env->SetByteArrayRegion(array, 0, size, memory);

    return array;
}

static jintArray toUInt16Array(JNIEnv *env, jobject obj, jlong arrayIndex, jint size)
{
    uint16_t *memory = reinterpret_cast<uint16_t *>(arrayIndex);
    jintArray array = env->NewIntArray(size);
    jint *elements = env->GetIntArrayElements(array, nullptr);

    for (size_t i = 0; i < size; i++) {
        elements[i] = memory[i];
    }

    env->ReleaseIntArrayElements(array, elements, 0);

    return array;
}

static JNINativeMethod JNI_METHODS[] = {
    { "isEqual", "(JJI)Z", (void *)isEqual },
    { "getHash", "(JI)I", (void *)getHash },
    { "getInt8", "(JI)B", (void *)getInt8 },
    { "getUInt16", "(JI)I", (void *)getUInt16 },
    { "putInt8", "(JIB)V", (void *)putInt8 },
    { "putUInt16", "(JII)V", (void *)putUInt16 },
    { "toInt8Array", "(JI)[B", (void *)toInt8Array },
    { "toUInt16Array", "(JI)[I", (void *)toUInt16Array },
};

jint register_com_mta_tehreer_internal_Raw(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/internal/Raw", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
