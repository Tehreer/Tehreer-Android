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
#include <SBLine.h>
#include <SBRun.h>
}

#include <jni.h>

#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "BidiLine.h"

using namespace Tehreer;

static void dispose(JNIEnv *env, jobject obj, jlong handle)
{
    SBLineRef line = reinterpret_cast<SBLineRef>(handle);
    SBLineRelease(line);
}

static jint getCharStart(JNIEnv *env, jobject obj, jlong handle)
{
    SBLineRef line = reinterpret_cast<SBLineRef>(handle);
    return SBLineGetOffset(line);
}

static jint getCharEnd(JNIEnv *env, jobject obj, jlong handle)
{
    SBLineRef line = reinterpret_cast<SBLineRef>(handle);
    return (SBLineGetOffset(line) + SBLineGetLength(line));
}

static jint getRunCount(JNIEnv *env, jobject obj, jlong handle)
{
    SBLineRef line = reinterpret_cast<SBLineRef>(handle);
    return SBLineGetRunCount(line);
}

static jobject getVisualRun(JNIEnv *env, jobject obj, jlong handle, jint runIndex)
{
    SBLineRef line = reinterpret_cast<SBLineRef>(handle);
    const SBRun *runPtr = &SBLineGetRunsPtr(line)[runIndex];
    JavaBridge bridge(env);

    return bridge.BidiRun_construct(runPtr->offset, runPtr->offset + runPtr->length, runPtr->level);
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
