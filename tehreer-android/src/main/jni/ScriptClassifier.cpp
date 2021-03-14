/*
 * Copyright (C) 2018-2021 Muhammad Tayyab Akram
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
#include <SBScript.h>
#include <SBScriptLocator.h>
}

#include <jni.h>

#include "JavaBridge.h"
#include "ScriptClassifier.h"

using namespace Tehreer;

static void classify(JNIEnv *env, jobject obj, jstring text, jbyteArray scripts)
{
    const jchar *charArray = env->GetStringChars(text, nullptr);
    jsize charCount = env->GetStringLength(text);

    void *scriptsPtr = env->GetPrimitiveArrayCritical(scripts, nullptr);
    auto scriptArray = static_cast<jbyte *>(scriptsPtr);

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
    }

    SBScriptLocatorRelease(scriptLocator);

    env->ReleasePrimitiveArrayCritical(scripts, scriptsPtr, 0);
    env->ReleaseStringChars(text, charArray);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nClassify", "(Ljava/lang/String;[B)V", (void *)classify },
};

jint register_com_mta_tehreer_unicode_ScriptClassifier(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/unicode/ScriptClassifier", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
