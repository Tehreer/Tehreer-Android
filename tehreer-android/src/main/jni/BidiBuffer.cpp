/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

#include <cstring>
#include <jni.h>

#include "JavaBridge.h"
#include "BidiBuffer.h"

using namespace Tehreer;

BidiBuffer *BidiBuffer::create(const jchar *charArray, jsize charCount)
{
    return new BidiBuffer(charArray, charCount);
}

BidiBuffer::BidiBuffer(const jchar *charArray, jsize charCount)
{
    m_data = new jchar[charCount];
    m_length = charCount;
    m_retainCount = 1;

    memcpy(m_data, charArray, sizeof(jchar) * charCount);
}

BidiBuffer::~BidiBuffer()
{
    delete [] m_data;
}

void BidiBuffer::retain()
{
    m_retainCount++;
}

void BidiBuffer::release()
{
    if (--m_retainCount == 0) {
        delete this;
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

static void retain(JNIEnv *env, jobject obj, jlong bufferHandle)
{
    BidiBuffer *bidiBuffer = reinterpret_cast<BidiBuffer *>(bufferHandle);
    bidiBuffer->retain();
}

static void release(JNIEnv *env, jobject obj, jlong bufferHandle)
{
    BidiBuffer *bidiBuffer = reinterpret_cast<BidiBuffer *>(bufferHandle);
    bidiBuffer->release();
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeCreate", "(Ljava/lang/String;)J", (void *)create },
    { "nativeRetain", "(J)V", (void *)retain },
    { "nativeRelease", "(J)V", (void *)release },
};

jint register_com_mta_tehreer_bidi_BidiBuffer(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/bidi/BidiBuffer", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
