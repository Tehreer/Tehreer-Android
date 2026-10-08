/*
 * Copyright (C) 2018-2026 Muhammad Tayyab Akram
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
#include <SheenBidi/SBCodepointSequence.h>
#include <SheenBidi/SBScript.h>
#include <SheenBidi/SBScriptLocator.h>
}

#include <cstdint>
#include <cstdlib>
#include <jni.h>

#include "JavaBridge.h"
#include "ScriptClassifier.h"

using namespace Tehreer;

/*
 * Resolves the script of each character of the text, and returns the memory that holds them, one
 * byte for each character. It belongs to the caller, who has to dispose it.
 */
static jlong classify(JNIEnv *env, jclass clazz, jstring text)
{
    const jchar *charArray = env->GetStringChars(text, nullptr);
    jsize charCount = env->GetStringLength(text);

    auto scriptArray = static_cast<uint8_t *>(malloc(charCount > 0 ? charCount : 1));

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
            scriptArray[i] = static_cast<uint8_t>(script);
        }
    }

    SBScriptLocatorRelease(scriptLocator);

    env->ReleaseStringChars(text, charArray);

    return reinterpret_cast<jlong>(scriptArray);
}

static void dispose(JNIEnv *env, jclass clazz, jlong scriptsHandle)
{
    free(reinterpret_cast<void *>(scriptsHandle));
}

static JNINativeMethod JNI_METHODS[] = {
    { "nClassify", "(Ljava/lang/String;)J", (void *)classify },
    { "nDispose", "(J)V", (void *)dispose },
};

jint register_com_mta_tehreer_unicode_ScriptClassifier(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/unicode/ScriptClassifier", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
