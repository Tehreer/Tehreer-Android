/*
 * Copyright (C) 2018 Muhammad Tayyab Akram
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
#include <SBScriptLocator.h>
}

#include <jni.h>

#include "BidiBuffer.h"
#include "JavaBridge.h"
#include "ScriptClassifier.h"

using namespace Tehreer;

static jint classify(JNIEnv *env, jobject obj, jstring text, jbyteArray scripts)
{
    const jchar *charArray = env->GetStringChars(text, nullptr);
    jsize charCount = env->GetStringLength(text);

    void *scriptsPtr = env->GetPrimitiveArrayCritical(scripts, nullptr);
    jbyte *scriptArray = static_cast<jbyte *>(scriptsPtr);
    jint runCount = 0;

    SBCodepointSequence codepointSequence;
    codepointSequence.stringEncoding = SBStringEncodingUTF16;
    codepointSequence.stringBuffer = (void *)charArray;
    codepointSequence.stringLength = static_cast<SBUInteger>(charCount);

    SBScriptLocatorRef scriptLocator = SBScriptLocatorCreate();
    const SBScriptAgent *scriptAgent = SBScriptLocatorGetAgent(scriptLocator);
    SBScriptLocatorLoadCodepoints(scriptLocator, &codepointSequence);

    while (SBScriptLocatorMoveNext(scriptLocator)) {
        SBUInteger start = scriptAgent->offset;
        SBUInteger limit = start + scriptAgent->length;
        SBScript script = scriptAgent->script;

        for (SBUInteger i = start; i < limit; i++) {
            scriptArray[i] = script;
        }

        runCount += 1;
    }

    SBScriptLocatorRelease(scriptLocator);

    env->ReleasePrimitiveArrayCritical(scripts, scriptsPtr, 0);
    env->ReleaseStringChars(text, charArray);

    return runCount;
}

static JNINativeMethod JNI_METHODS[] = {
    { "nClassify", "(Ljava/lang/String;[B)I", (void *)classify },
};

jint register_com_mta_tehreer_unicode_ScriptClassifier(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/unicode/ScriptClassifier", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
