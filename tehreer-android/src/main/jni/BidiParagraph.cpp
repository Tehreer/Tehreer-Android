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
    auto bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBParagraphRelease(bidiParagraph);
}

static jint getCharStart(JNIEnv *env, jobject obj, jlong paragraphHandle)
{
    auto bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBUInteger paragraphOffset = SBParagraphGetOffset(bidiParagraph);

    return static_cast<jint>(paragraphOffset);
}

static jint getCharEnd(JNIEnv *env, jobject obj, jlong paragraphHandle)
{
    auto bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBUInteger paragraphOffset = SBParagraphGetOffset(bidiParagraph);
    SBUInteger paragraphLength = SBParagraphGetLength(bidiParagraph);

    return static_cast<jint>(paragraphOffset + paragraphLength);
}

static jint getCharCount(JNIEnv *env, jobject obj, jlong paragraphHandle)
{
    auto bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBUInteger paragraphLength = SBParagraphGetLength(bidiParagraph);

    return static_cast<jint>(paragraphLength);
}

static jbyte getBaseLevel(JNIEnv *env, jobject obj, jlong paragraphHandle)
{
    auto bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBLevel baseLevel = SBParagraphGetBaseLevel(bidiParagraph);

    return static_cast<jbyte>(baseLevel);
}

static jlong getLevelsPtr(JNIEnv *env, jobject obj, jlong paragraphHandle)
{
    auto bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    const SBLevel *levelsPtr = SBParagraphGetLevelsPtr(bidiParagraph);

    return reinterpret_cast<jlong>(levelsPtr);
}

static jobject getOnwardRun(JNIEnv *env, jobject obj, jlong paragraphHandle, jint charIndex)
{
    auto bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    SBUInteger paragraphOffset = SBParagraphGetOffset(bidiParagraph);
    SBUInteger paragraphLength = SBParagraphGetLength(bidiParagraph);

    jint levelIndex = static_cast<jint>(charIndex - paragraphOffset);
    if (levelIndex < paragraphLength) {
        const SBLevel *levelsPtr = SBParagraphGetLevelsPtr(bidiParagraph);
        SBLevel currentLevel = levelsPtr[levelIndex];
        auto nextIndex = static_cast<SBUInteger>(levelIndex);

        while (++nextIndex < paragraphLength) {
            if (levelsPtr[nextIndex] != currentLevel) {
                break;
            }
        }

        auto charStart = static_cast<jint>(levelIndex + paragraphOffset);
        auto charEnd = static_cast<jint>(nextIndex + paragraphOffset);
        auto embeddingLevel = static_cast<jbyte>(currentLevel);

        return JavaBridge(env).BidiRun_construct(charStart, charEnd, embeddingLevel);
    }

    return nullptr;
}

static jlong createLine(JNIEnv *env, jobject obj, jlong paragraphHandle, jint charStart, jint charEnd)
{
    auto bidiParagraph = reinterpret_cast<SBParagraphRef>(paragraphHandle);
    auto lineOffset = static_cast<SBUInteger>(charStart);
    auto lineLength = static_cast<SBUInteger>(charEnd - charStart);

    SBLineRef bidiLine = SBParagraphCreateLine(bidiParagraph, lineOffset, lineLength);
    return reinterpret_cast<jlong>(bidiLine);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetCharStart", "(J)I", (void *)getCharStart },
    { "nGetCharEnd", "(J)I", (void *)getCharEnd },
    { "nGetCharCount", "(J)I", (void *)getCharCount },
    { "nGetBaseLevel", "(J)B", (void *)getBaseLevel },
    { "nGetLevelsPtr", "(J)J", (void *)getLevelsPtr },
    { "nGetOnwardRun", "(JI)Lcom/mta/tehreer/unicode/BidiRun;", (void *)getOnwardRun },
    { "nCreateLine", "(JII)J", (void *)createLine },
};

jint register_com_mta_tehreer_unicode_BidiParagraph(JNIEnv *env) {
    return JavaBridge::registerClass(env, "com/mta/tehreer/unicode/BidiParagraph", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
