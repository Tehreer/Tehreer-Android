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
#include <SBAlgorithm.h>
#include <SBBase.h>
#include <SBCodepointSequence.h>
#include <SBParagraph.h>
}

#include <jni.h>

#include "BidiBuffer.h"
#include "JavaBridge.h"
#include "BidiAlgorithm.h"

using namespace Tehreer;

static jlong create(JNIEnv *env, jobject obj, jlong bufferHandle)
{
    auto bidiBuffer = reinterpret_cast<BidiBuffer *>(bufferHandle);
    auto stringBuffer = static_cast<void *>(bidiBuffer->data());
    auto stringLength = static_cast<SBUInteger>(bidiBuffer->length());

    SBCodepointSequence codepointSequence = { SBStringEncodingUTF16, stringBuffer, stringLength };
    SBAlgorithmRef bidiAlgorithm = SBAlgorithmCreate(&codepointSequence);

    return reinterpret_cast<jlong>(bidiAlgorithm);
}

static void dispose(JNIEnv *env, jobject obj, jlong algorithmHandle)
{
    auto bidiAlgorithm = reinterpret_cast<SBAlgorithmRef>(algorithmHandle);
    SBAlgorithmRelease(bidiAlgorithm);
}

static jlong getCharBidiClassesPtr(JNIEnv *env, jobject obj, jlong algorithmHandle)
{
    auto bidiAlgorithm = reinterpret_cast<SBAlgorithmRef>(algorithmHandle);
    const SBBidiType *bidiClasses = SBAlgorithmGetBidiTypesPtr(bidiAlgorithm);

    return reinterpret_cast<jlong>(bidiClasses);
}

static jint getParagraphBoundary(JNIEnv *env, jobject obj,
    jlong algorithmHandle, jint charStart, jint charEnd)
{
    auto bidiAlgorithm = reinterpret_cast<SBAlgorithmRef>(algorithmHandle);
    auto paragraphOffset = static_cast<SBUInteger>(charStart);
    auto suggestedLength = static_cast<SBUInteger>(charEnd - charStart);

    SBUInteger actualLength;
    SBAlgorithmGetParagraphBoundary(bidiAlgorithm,
                                    paragraphOffset, suggestedLength,
                                    &actualLength, nullptr);

    return static_cast<jint>(paragraphOffset + actualLength);
}

static jlong createParagraph(JNIEnv *env, jobject obj,
    jlong algorithmHandle, jint charStart, jint charEnd, jint baseLevel)
{
    auto bidiAlgorithm = reinterpret_cast<SBAlgorithmRef>(algorithmHandle);
    auto paragraphOffset = static_cast<SBUInteger>(charStart);
    auto suggestedLength = static_cast<SBUInteger>(charEnd - charStart);
    auto inputLevel = static_cast<SBLevel>(baseLevel);

    SBParagraphRef paragraph = SBAlgorithmCreateParagraph(bidiAlgorithm,
                                                          paragraphOffset, suggestedLength,
                                                          inputLevel);

    return reinterpret_cast<jlong>(paragraph);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "(J)J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetCharBidiClassesPtr", "(J)J", (void *)getCharBidiClassesPtr },
    { "nGetParagraphBoundary", "(JII)I", (void *)getParagraphBoundary },
    { "nCreateParagraph", "(JIII)J", (void *)createParagraph },
};

jint register_com_mta_tehreer_unicode_BidiAlgorithm(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/unicode/BidiAlgorithm", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
