/*
 * Copyright (C) 2016-2021 Muhammad Tayyab Akram
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
#include <cstdlib>
#include <cstring>
#include <jni.h>

#include "JavaBridge.h"
#include "BidiBuffer.h"

using namespace Tehreer;

BidiBuffer *BidiBuffer::create(const jchar *charArray, jsize charCount)
{
    const size_t sizeBuffer = sizeof(BidiBuffer);
    const size_t sizeData = sizeof(jchar) * charCount;
    const size_t sizeMemory = sizeBuffer + sizeData;

    const size_t offsetBuffer = 0;
    const size_t offsetData = sizeBuffer;

    auto memory = reinterpret_cast<uint8_t *>(malloc(sizeMemory));
    auto buffer = reinterpret_cast<BidiBuffer *>(memory + offsetBuffer);
    buffer->m_data = reinterpret_cast<jchar *>(memory + offsetData);
    buffer->m_length = charCount;
    buffer->m_retainCount = 1;

    memcpy(buffer->m_data, charArray, sizeData);

    return buffer;
}

void BidiBuffer::retain()
{
    m_retainCount++;
}

void BidiBuffer::release()
{
    if (--m_retainCount == 0) {
        free(this);
    }
}

static jlong create(JNIEnv *env, jobject obj, jstring string)
{
    const jchar *charArray = env->GetStringChars(string, nullptr);
    jsize charCount = env->GetStringLength(string);

    BidiBuffer *bidiBuffer = BidiBuffer::create(charArray, charCount);

    env->ReleaseStringChars(string, charArray);

    return reinterpret_cast<jlong>(bidiBuffer);
}

static jlong retain(JNIEnv *env, jobject obj, jlong bufferHandle)
{
    auto bidiBuffer = reinterpret_cast<BidiBuffer *>(bufferHandle);
    bidiBuffer->retain();

    return reinterpret_cast<jlong>(bidiBuffer);
}

static void release(JNIEnv *env, jobject obj, jlong bufferHandle)
{
    auto bidiBuffer = reinterpret_cast<BidiBuffer *>(bufferHandle);
    bidiBuffer->release();
}

static JNINativeMethod JNI_METHODS[] = {
    { "create", "(Ljava/lang/String;)J", (void *)create },
    { "retain", "(J)J", (void *)retain },
    { "release", "(J)V", (void *)release },
};

jint register_com_mta_tehreer_unicode_BidiBuffer(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/unicode/BidiBuffer", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
