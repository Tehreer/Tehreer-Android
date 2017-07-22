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

extern "C" {
#include <SBCodepointSequence.h>
#include <SBLine.h>
#include <SBMirrorLocator.h>
}

#include <jni.h>

#include "BidiBuffer.h"
#include "JavaBridge.h"
#include "BidiMirrorLocator.h"

using namespace Tehreer;

static jlong create(JNIEnv *env, jobject obj)
{
    SBMirrorLocatorRef mirrorLocator = SBMirrorLocatorCreate();
    return reinterpret_cast<jlong>(mirrorLocator);
}

static void dispose(JNIEnv *env, jobject obj, jlong locatorHandle)
{
    SBMirrorLocatorRef mirrorLocator = reinterpret_cast<SBMirrorLocatorRef>(locatorHandle);
    SBMirrorLocatorRelease(mirrorLocator);
}

static void loadLine(JNIEnv *env, jobject obj, jlong locatorHandle, jlong lineHandle, jlong bufferHandle)
{
    SBMirrorLocatorRef mirrorLocator = reinterpret_cast<SBMirrorLocatorRef>(locatorHandle);
    SBLineRef bidiLine = reinterpret_cast<SBLineRef>(lineHandle);
    BidiBuffer *bidiBuffer = reinterpret_cast<BidiBuffer *>(bufferHandle);
    void *stringBuffer = static_cast<void *>(bidiBuffer->data());

    SBMirrorLocatorLoadLine(mirrorLocator, bidiLine, stringBuffer);
}

static jobject getNextPair(JNIEnv *env, jobject obj, jlong locatorHandle, jlong bufferHandle)
{
    SBMirrorLocatorRef mirrorLocator = reinterpret_cast<SBMirrorLocatorRef>(locatorHandle);
    if (SBMirrorLocatorMoveNext(mirrorLocator)) {
        BidiBuffer *bidiBuffer = reinterpret_cast<BidiBuffer *>(bufferHandle);
        void *stringBuffer = static_cast<void *>(bidiBuffer->data());
        SBUInteger stringLength = static_cast<SBUInteger>(bidiBuffer->length());

        SBCodepointSequence codepointSequence;
        codepointSequence.stringEncoding = SBStringEncodingUTF16;
        codepointSequence.stringBuffer = stringBuffer;
        codepointSequence.stringLength = stringLength;

        SBMirrorAgentRef mirrorAgent = SBMirrorLocatorGetAgent(mirrorLocator);
        SBUInteger index = mirrorAgent->index;
        SBCodepoint source = SBCodepointSequenceGetCodepointAt(&codepointSequence, &index);
        SBCodepoint mirror = mirrorAgent->mirror;

        jint charIndex = static_cast<jint>(index);
        jint actualCodePoint = static_cast<jint>(source);
        jint pairingCodePoint = static_cast<jint>(mirror);

        return JavaBridge(env).BidiPair_construct(charIndex, actualCodePoint, pairingCodePoint);
    }

    return nullptr;
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeCreate", "()J", (void *)create },
    { "nativeDispose", "(J)V", (void *)dispose },
    { "nativeLoadLine", "(JJJ)V", (void *)loadLine },
    { "nativeGetNextPair", "(JJ)Lcom/mta/tehreer/unicode/BidiPair;", (void *)getNextPair },
};

jint register_com_mta_tehreer_unicode_BidiMirrorLocator(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/unicode/BidiMirrorLocator", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
