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
#include <SBParagraph.h>
}

#include <jni.h>

#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "Range.h"
#include "BidiParagraph.h"

using namespace Tehreer;

static void dispose(JNIEnv *env, jobject obj, jlong handle)
{
    SBParagraphRef paragraph = reinterpret_cast<SBParagraphRef>(handle);
    SBParagraphRelease(paragraph);
}

static jint getCharStart(JNIEnv *env, jobject obj, jlong handle)
{
    SBParagraphRef paragraph = reinterpret_cast<SBParagraphRef>(handle);
    return SBParagraphGetOffset(paragraph);
}

static jint getCharEnd(JNIEnv *env, jobject obj, jlong handle)
{
    SBParagraphRef paragraph = reinterpret_cast<SBParagraphRef>(handle);
    return (SBParagraphGetOffset(paragraph) + SBParagraphGetLength(paragraph));
}

static jbyte getBaseLevel(JNIEnv *env, jobject obj, jlong handle)
{
    SBParagraphRef paragraph = reinterpret_cast<SBParagraphRef>(handle);
    return SBParagraphGetBaseLevel(paragraph);
}

static jbyte getLevel(JNIEnv *env, jobject obj, jlong handle, jint levelIndex)
{
    SBParagraphRef paragraph = reinterpret_cast<SBParagraphRef>(handle);
    return SBParagraphGetLevelsPtr(paragraph)[levelIndex];
}

static jobject getRun(JNIEnv *env, jobject obj, jlong handle, jint levelIndex)
{
    SBParagraphRef paragraph = reinterpret_cast<SBParagraphRef>(handle);
    SBUInteger length = SBParagraphGetLength(paragraph);

    if (levelIndex < length) {
        const SBLevel *levels = SBParagraphGetLevelsPtr(paragraph);
        SBLevel runLevel = levels[levelIndex];
        SBUInteger nextIndex = levelIndex;

        while (++nextIndex < length) {
            if (levels[nextIndex] != runLevel) {
                break;
            }
        }

        SBUInteger offset = SBParagraphGetOffset(paragraph);
        JavaBridge bridge(env);

        return bridge.BidiRun_construct(levelIndex + offset, nextIndex + offset, runLevel);
    }

    return nullptr;
}

static jbyte createLine(JNIEnv *env, jobject obj, jlong handle, jint charStart, jint charEnd)
{
    SBParagraphRef paragraph = reinterpret_cast<SBParagraphRef>(handle);
    SBLineRef line = SBParagraphCreateLine(paragraph, charStart, charEnd - charStart);
    return reinterpret_cast<jlong>(line);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeDispose", "(J)V", (void *)dispose },
    { "nativeGetCharStart", "(J)I", (void *)getCharStart },
    { "nativeGetCharEnd", "(J)I", (void *)getCharEnd },
    { "nativeGetBaseLevel", "(J)B", (void *)getBaseLevel },
    { "nativeGetLevel", "(JI)B", (void *)getLevel },
    { "nativeGetRun", "(JI)Lcom/mta/tehreer/bidi/BidiRun;", (void *)getRun },
    { "nativeCreateLine", "(JII)J", (void *)createLine },
};

jint register_com_mta_tehreer_bidi_BidiParagraph(JNIEnv *env) {
    return JavaBridge::registerClass(env, "com/mta/tehreer/bidi/BidiParagraph", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
