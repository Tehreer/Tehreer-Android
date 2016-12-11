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
#include <SBParagraph.h>
}

#include <jni.h>

#include "JavaBridge.h"
#include "BidiParagraph.h"

using namespace Tehreer;

static void dispose(JNIEnv *env, jobject obj, jlong paragraphHandle)
{
    SBParagraphRef bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBParagraphRelease(bidiParagraph);
}

static jint getCharStart(JNIEnv *env, jobject obj, jlong paragraphHandle)
{
    SBParagraphRef bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBUInteger paragraphOffset = SBParagraphGetOffset(bidiParagraph);

    return static_cast<jint>(paragraphOffset);
}

static jint getCharEnd(JNIEnv *env, jobject obj, jlong paragraphHandle)
{
    SBParagraphRef bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBUInteger paragraphOffset = SBParagraphGetOffset(bidiParagraph);
    SBUInteger paragraphLength = SBParagraphGetLength(bidiParagraph);

    return static_cast<jint>(paragraphOffset + paragraphLength);
}

static jbyte getBaseLevel(JNIEnv *env, jobject obj, jlong paragraphHandle)
{
    SBParagraphRef bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBLevel baseLevel = SBParagraphGetBaseLevel(bidiParagraph);

    return static_cast<jbyte>(baseLevel);
}

static jbyte getLevel(JNIEnv *env, jobject obj, jlong paragraphHandle, jint levelIndex)
{
    SBParagraphRef bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    const SBLevel *levelsPtr = SBParagraphGetLevelsPtr(bidiParagraph);

    return static_cast<jbyte>(levelsPtr[levelIndex]);
}

static jobject getRun(JNIEnv *env, jobject obj, jlong paragraphHandle, jint levelIndex)
{
    SBParagraphRef bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBUInteger paragraphLength = SBParagraphGetLength(bidiParagraph);

    if (levelIndex < paragraphLength) {
        const SBLevel *levelsPtr = SBParagraphGetLevelsPtr(bidiParagraph);
        SBLevel currentLevel = levelsPtr[levelIndex];
        SBUInteger nextIndex = static_cast<SBUInteger>(levelIndex);

        while (++nextIndex < paragraphLength) {
            if (levelsPtr[nextIndex] != currentLevel) {
                break;
            }
        }

        SBUInteger paragraphOffset = SBParagraphGetOffset(bidiParagraph);
        jint charStart = static_cast<jint>(levelIndex + paragraphOffset);
        jint charEnd = static_cast<jint>(nextIndex + paragraphOffset);
        jbyte embeddingLevel = static_cast<jbyte>(currentLevel);

        return JavaBridge(env).BidiRun_construct(charStart, charEnd, embeddingLevel);
    }

    return nullptr;
}

static jlong createLine(JNIEnv *env, jobject obj, jlong paragraphHandle, jint charStart, jint charEnd)
{
    SBParagraphRef bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBUInteger lineOffset = static_cast<SBUInteger>(charStart);
    SBUInteger lineLength = static_cast<SBUInteger>(charEnd - charStart);

    SBLineRef bidiLine = SBParagraphCreateLine(bidiParagraph, lineOffset, lineLength);
    return reinterpret_cast<jlong>(bidiLine);
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
