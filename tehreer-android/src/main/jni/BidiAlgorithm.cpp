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
#include <SBAlgorithm.h>
#include <SBCodepointSequence.h>
}

#include <jni.h>

#include "BidiBuffer.h"
#include "BidiParagraph.h"
#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "BidiAlgorithm.h"

using namespace Tehreer;

static jlong create(JNIEnv *env, jobject obj, jlong bufferHandle)
{
    BidiBuffer *bidiBuffer = reinterpret_cast<BidiBuffer *>(bufferHandle);

    SBCodepointSequence codepointSequence;
    codepointSequence.stringEncoding = SBStringEncodingUTF16;
    codepointSequence.stringBuffer = bidiBuffer->data();
    codepointSequence.stringLength = bidiBuffer->length();

    SBAlgorithmRef algorithm = SBAlgorithmCreate(&codepointSequence);
    return reinterpret_cast<jlong>(algorithm);
}

static void dispose(JNIEnv *env, jobject obj, jlong handle)
{
    SBAlgorithmRef algorithm = reinterpret_cast<SBAlgorithmRef>(handle);
    SBAlgorithmRelease(algorithm);
}

static jint getParagraphBoundary(JNIEnv *env, jobject obj, jlong handle, jint charStart, jint charEnd)
{
    SBAlgorithmRef algorithm = reinterpret_cast<SBAlgorithmRef>(handle);

    SBUInteger actualLength;
    SBAlgorithmGetParagraphBoundary(algorithm,
                                    charStart, charEnd - charStart,
                                    &actualLength, nullptr);

    return (jint)(charStart + actualLength);
}

static jlong createParagraph(JNIEnv *env, jobject obj, jlong handle, jint charStart, jint charEnd, jint baseLevel)
{
    SBAlgorithmRef algorithm = reinterpret_cast<SBAlgorithmRef>(handle);
    SBParagraphRef paragraph = SBAlgorithmCreateParagraph(algorithm,
                                                          charStart, charEnd - charStart,
                                                          baseLevel);

    return reinterpret_cast<jlong>(paragraph);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeCreate", "(J)J", (void *)create },
    { "nativeDispose", "(J)V", (void *)dispose },
    { "nativeGetParagraphBoundary", "(JII)I", (void *)getParagraphBoundary },
    { "nativeCreateParagraph", "(JIII)J", (void *)createParagraph },
};

jint register_com_mta_tehreer_bidi_BidiAlgorithm(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/bidi/BidiAlgorithm", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
