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

static jint bytesInSizeType(JNIEnv *env, jobject obj)
{
    return sizeof(size_t);
}

static jbyte getInt8FromArray(JNIEnv *env, jobject obj, jlong pointer, jint index)
{
    int8_t *memory = reinterpret_cast<int8_t *>(pointer);
    jbyte value = static_cast<jbyte>(memory[index]);

    return value;
}

static jint getInt32FromArray(JNIEnv *env, jobject obj, jlong pointer, jint index)
{
    int32_t *memory = reinterpret_cast<int32_t *>(pointer);
    jint value = static_cast<jint>(memory[index]);

    return value;
}

static jint getUInt16FromArray(JNIEnv *env, jobject obj, jlong pointer, jint index)
{
    uint16_t *memory = reinterpret_cast<uint16_t *>(pointer);
    jint value = static_cast<jint>(memory[index]);

    return value;
}

static jint getSizeFromArray(JNIEnv *env, jobject obj, jlong pointer, jint index)
{
    size_t *memory = reinterpret_cast<size_t *>(pointer);
    jint value = static_cast<jint>(memory[index]);

    return value;
}

static void copyInt8Array(JNIEnv *env, jobject obj, jlong pointer, jbyteArray destination, jint start, jint length)
{
    int8_t *buffer = reinterpret_cast<int8_t *>(pointer);
    env->SetByteArrayRegion(destination, start, length, buffer);
}

static void copyUInt16Array(JNIEnv *env, jobject obj, jlong pointer, jintArray destination, jint start, jint length)
{
    uint16_t *buffer = reinterpret_cast<uint16_t *>(pointer);
    jint *elements = env->GetIntArrayElements(destination, nullptr) + start;

    for (size_t i = 0; i < length; i++) {
        elements[i] = static_cast<jint>(buffer[i]);
    }

    env->ReleaseIntArrayElements(destination, elements, 0);
}

static void copySizeArray(JNIEnv *env, jobject obj, jlong pointer, jintArray destination, jint start, jint length)
{
    size_t *buffer = reinterpret_cast<size_t *>(pointer);
    jint *elements = env->GetIntArrayElements(destination, nullptr) + start;

    for (size_t i = 0; i < length; i++) {
        elements[i] = static_cast<jint>(buffer[i]);
    }

    env->ReleaseIntArrayElements(destination, elements, 0);
}

static void copyInt32FloatArray(JNIEnv *env, jobject obj, jlong pointer, jfloatArray destination, jint start, jint length, jfloat scale)
{
    int32_t *buffer = reinterpret_cast<int32_t *>(pointer);
    jfloat *elements = env->GetFloatArrayElements(destination, nullptr) + start;

    for (size_t i = 0; i < length; i++) {
        elements[i] = buffer[i] * scale;
    }

    env->ReleaseFloatArrayElements(destination, elements, 0);
}

static JNINativeMethod JNI_METHODS[] = {
    { "bytesInSizeType", "()I", (void *)bytesInSizeType },
    { "getInt8FromArray", "(JI)B", (void *)getInt8FromArray },
    { "getInt32FromArray", "(JI)I", (void *)getInt32FromArray },
    { "getUInt16FromArray", "(JI)I", (void *)getUInt16FromArray },
    { "getSizeFromArray", "(JI)I", (void *)getSizeFromArray },
    { "copyInt8Array", "(J[BII)V", (void *)copyInt8Array },
    { "copyUInt16Array", "(J[III)V", (void *)copyUInt16Array },
    { "copySizeArray", "(J[III)V", (void *)copySizeArray },
    { "copyInt32FloatArray", "(J[FIIF)V", (void *)copyInt32FloatArray },
};

jint register_com_mta_tehreer_internal_Raw(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/internal/Raw", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
