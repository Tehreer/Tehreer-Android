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

extern "C" {
#include <SBBase.h>
#include <SBLine.h>
#include <SBRun.h>
}

#include <jni.h>

#include "JavaBridge.h"
#include "BidiLine.h"

using namespace Tehreer;

static void dispose(JNIEnv *env, jobject obj, jlong lineHandle)
{
    SBLineRef bidiLine = reinterpret_cast<SBLineRef>(lineHandle);
    SBLineRelease(bidiLine);
}

static jint getCharStart(JNIEnv *env, jobject obj, jlong lineHandle)
{
    SBLineRef bidiLine = reinterpret_cast<SBLineRef>(lineHandle);
    SBUInteger lineOffset = SBLineGetOffset(bidiLine);

    return static_cast<jint>(lineOffset);
}

static jint getCharEnd(JNIEnv *env, jobject obj, jlong lineHandle)
{
    SBLineRef bidiLine = reinterpret_cast<SBLineRef>(lineHandle);
    SBUInteger lineOffset = SBLineGetOffset(bidiLine);
    SBUInteger lineLength = SBLineGetLength(bidiLine);

    return static_cast<jint>(lineOffset + lineLength);
}

static jint getRunCount(JNIEnv *env, jobject obj, jlong lineHandle)
{
    SBLineRef bidiLine = reinterpret_cast<SBLineRef>(lineHandle);
    SBUInteger runCount = SBLineGetRunCount(bidiLine);

    return static_cast<jint>(runCount);
}

static jobject getVisualRun(JNIEnv *env, jobject obj, jlong lineHandle, jint runIndex)
{
    SBLineRef bidiLine = reinterpret_cast<SBLineRef>(lineHandle);
    const SBRun *runPtr = &SBLineGetRunsPtr(bidiLine)[runIndex];
    jint charStart = static_cast<jint>(runPtr->offset);
    jint charEnd = static_cast<jint>(runPtr->offset + runPtr->length);
    jbyte embeddingLevel = static_cast<jbyte>(runPtr->level);

    return JavaBridge(env).BidiRun_construct(charStart, charEnd, embeddingLevel);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeDispose", "(J)V", (void *)dispose },
    { "nativeGetCharStart", "(J)I", (void *)getCharStart },
    { "nativeGetCharEnd", "(J)I", (void *)getCharEnd },
    { "nativeGetRunCount", "(J)I", (void *)getRunCount },
    { "nativeGetVisualRun", "(JI)Lcom/mta/tehreer/bidi/BidiRun;", (void *)getVisualRun },
};

jint register_com_mta_tehreer_bidi_BidiLine(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/bidi/BidiLine", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
