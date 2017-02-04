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
#include <jni.h>

#include "JavaBridge.h"
#include "Raw.h"

using namespace Tehreer;

static jbyte getInt8(JNIEnv *env, jobject obj, jlong pointer, jint index)
{
    int8_t *memory = reinterpret_cast<int8_t *>(pointer);
    jbyte value = memory[index];

    return value;
}

static jint getInt32(JNIEnv *env, jobject obj, jlong pointer, jint index)
{
    int32_t *memory = reinterpret_cast<int32_t *>(pointer);
    jint value = memory[index];

    return value;
}

static jint getUInt16(JNIEnv *env, jobject obj, jlong pointer, jint index)
{
    uint16_t *memory = reinterpret_cast<uint16_t *>(pointer);
    jint value = memory[index];

    return value;
}

static jbyteArray arrayForInt8Values(JNIEnv *env, jobject obj, jlong pointer, jint count)
{
    int8_t *memory = reinterpret_cast<int8_t *>(pointer);
    jbyteArray array = env->NewByteArray(count);
    env->SetByteArrayRegion(array, 0, count, memory);

    return array;
}

static jintArray arrayForUInt16Values(JNIEnv *env, jobject obj, jlong pointer, jint count)
{
    uint16_t *memory = reinterpret_cast<uint16_t *>(pointer);
    jintArray array = env->NewIntArray(count);
    jint *elements = env->GetIntArrayElements(array, nullptr);

    for (size_t i = 0; i < count; i++) {
        elements[i] = memory[i];
    }

    env->ReleaseIntArrayElements(array, elements, 0);

    return array;
}

static jfloatArray arrayForInt32Floats(JNIEnv *env, jobject obj, jlong pointer, jint count, jfloat scale)
{
    int32_t *memory = reinterpret_cast<int32_t *>(pointer);
    jfloatArray array = env->NewFloatArray(count);
    jfloat *elements = env->GetFloatArrayElements(array, nullptr);

    for (size_t i = 0; i < count; i++) {
        elements[i] = memory[i] * scale;
    }

    env->ReleaseFloatArrayElements(array, elements, 0);

    return array;
}

static jfloatArray arrayForInt32Points(JNIEnv *env, jobject obj, jlong pointer, jint count, jfloat scale)
{
    int32_t *memory = reinterpret_cast<int32_t *>(pointer);
    jint length = count * 2;
    jfloatArray array = env->NewFloatArray(length);
    jfloat *elements = env->GetFloatArrayElements(array, nullptr);

    for (size_t i = 0; i < length; i++) {
        elements[i] = memory[i] * scale;
    }

    env->ReleaseFloatArrayElements(array, elements, 0);

    return array;
}

static JNINativeMethod JNI_METHODS[] = {
    { "getInt8", "(JI)B", (void *)getInt8 },
    { "getInt32", "(JI)I", (void *)getInt32 },
    { "getUInt16", "(JI)I", (void *)getUInt16 },
    { "arrayForInt8Values", "(JI)[B", (void *)arrayForInt8Values },
    { "arrayForUInt16Values", "(JI)[I", (void *)arrayForUInt16Values },
    { "arrayForInt32Floats", "(JIF)[F", (void *)arrayForInt32Floats },
    { "arrayForInt32Points", "(JIF)[F", (void *)arrayForInt32Points },
};

jint register_com_mta_tehreer_internal_Raw(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/internal/Raw", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
