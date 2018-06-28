/*
 * Copyright (C) 2017-2018 Muhammad Tayyab Akram
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

static jint sizeOfIntPtr(JNIEnv *env, jobject obj)
{
    return sizeof(size_t);
}

static jbyte getInt8Value(JNIEnv *env, jobject obj, jlong pointer)
{
    int8_t *buffer = reinterpret_cast<int8_t *>(pointer);
    jbyte value = static_cast<jbyte>(*buffer);

    return value;
}

static jint getInt32Value(JNIEnv *env, jobject obj, jlong pointer)
{
    int32_t *buffer = reinterpret_cast<int32_t *>(pointer);
    jint value = static_cast<jint>(*buffer);

    return value;
}

static jint getUInt16Value(JNIEnv *env, jobject obj, jlong pointer)
{
    uint16_t *buffer = reinterpret_cast<uint16_t *>(pointer);
    jint value = static_cast<jint>(*buffer);

    return value;
}

static jint getIntPtrValue(JNIEnv *env, jobject obj, jlong pointer)
{
    size_t *buffer = reinterpret_cast<size_t *>(pointer);
    jint value = static_cast<jint>(*buffer);

    return value;
}

static void copyInt8Buffer(JNIEnv *env, jobject obj, jlong pointer, jbyteArray destination, jint start, jint length)
{
    int8_t *buffer = reinterpret_cast<int8_t *>(pointer);
    env->SetByteArrayRegion(destination, start, length, buffer);
}

static void copyUInt16Buffer(JNIEnv *env, jobject obj, jlong pointer, jintArray destination, jint start, jint length)
{
    uint16_t *buffer = reinterpret_cast<uint16_t *>(pointer);
    void *raw = env->GetPrimitiveArrayCritical(destination, nullptr);
    jint *values = static_cast<jint *>(raw) + start;

    for (jint i = 0; i < length; i++) {
        values[i] = static_cast<jint>(buffer[i]);
    }

    env->ReleasePrimitiveArrayCritical(destination, raw, 0);
}

static void copyUIntPtrBuffer(JNIEnv *env, jobject obj, jlong pointer, jintArray destination, jint start, jint length)
{
    size_t *buffer = reinterpret_cast<size_t *>(pointer);
    void *raw = env->GetPrimitiveArrayCritical(destination, nullptr);
    jint *values = static_cast<jint *>(raw) + start;

    for (jint i = 0; i < length; i++) {
        values[i] = static_cast<jint>(buffer[i]);
    }

    env->ReleasePrimitiveArrayCritical(destination, raw, 0);
}

static void copyInt32Buffer(JNIEnv *env, jobject obj, jlong pointer, jfloatArray destination, jint start, jint length, jfloat scale)
{
    int32_t *buffer = reinterpret_cast<int32_t *>(pointer);
    void *raw = env->GetPrimitiveArrayCritical(destination, nullptr);
    jfloat *values = static_cast<jfloat *>(raw) + start;

    for (jint i = 0; i < length; i++) {
        values[i] = buffer[i] * scale;
    }

    env->ReleasePrimitiveArrayCritical(destination, raw, 0);
}

static JNINativeMethod JNI_METHODS[] = {
    { "sizeOfIntPtr", "()I", (void *)sizeOfIntPtr },
    { "getInt8Value", "(J)B", (void *)getInt8Value },
    { "getInt32Value", "(J)I", (void *)getInt32Value },
    { "getUInt16Value", "(J)I", (void *)getUInt16Value },
    { "getIntPtrValue", "(J)I", (void *)getIntPtrValue },
    { "copyInt8Buffer", "(J[BII)V", (void *)copyInt8Buffer },
    { "copyUInt16Buffer", "(J[III)V", (void *)copyUInt16Buffer },
    { "copyUIntPtrBuffer", "(J[III)V", (void *)copyUIntPtrBuffer },
    { "copyInt32Buffer", "(J[FIIF)V", (void *)copyInt32Buffer },
};

jint register_com_mta_tehreer_internal_Raw(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/internal/Raw", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
