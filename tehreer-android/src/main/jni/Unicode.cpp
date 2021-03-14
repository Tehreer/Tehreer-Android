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
#include <SBBase.h>
#include <SBBidiType.h>
#include <SBCodepoint.h>
#include <SBGeneralCategory.h>
#include <SBScript.h>
}

#include <jni.h>

#include "JavaBridge.h"
#include "Unicode.h"

using namespace Tehreer;

static jint getCodePointBidiClass(JNIEnv *env, jobject obj, jint codePoint)
{
    auto numValue = static_cast<SBCodepoint>(codePoint);
    SBBidiType bidiType = SBCodepointGetBidiType(numValue);

    return static_cast<jint>(bidiType);
}

static jint getCodePointGeneralCategory(JNIEnv *env, jobject obj, jint codePoint)
{
    auto numValue = static_cast<SBCodepoint>(codePoint);
    SBGeneralCategory generalCategory = SBCodepointGetGeneralCategory(numValue);

    return static_cast<jint>(generalCategory);
}

static jint getCodePointScript(JNIEnv *env, jobject obj, jint codePoint)
{
    auto numValue = static_cast<SBCodepoint>(codePoint);
    SBScript script = SBCodepointGetScript(numValue);

    return static_cast<jint>(script);
}

static jint getCodePointMirror(JNIEnv *env, jobject obj, jint codePoint)
{
    auto numValue = static_cast<SBCodepoint>(codePoint);
    SBCodepoint mirror = SBCodepointGetMirror(numValue);

    return static_cast<jint>(mirror);
}

static jint getScriptOpenTypeTag(JNIEnv *env, jobject obj, jint script)
{
    auto numValue = static_cast<SBScript>(script);
    SBUInt32 openTypeTag = SBScriptGetOpenTypeTag(numValue);

    return static_cast<jint>(openTypeTag);
}

static JNINativeMethod JNI_METHODS[] = {
    { "getCodePointBidiClass", "(I)I", (void *)getCodePointBidiClass },
    { "getCodePointGeneralCategory", "(I)I", (void *)getCodePointGeneralCategory },
    { "getCodePointScript", "(I)I", (void *)getCodePointScript },
    { "getCodePointMirror", "(I)I", (void *)getCodePointMirror },
    { "getScriptOpenTypeTag", "(I)I", (void *)getScriptOpenTypeTag },
};

jint register_com_mta_tehreer_unicode_Unicode(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/unicode/Unicode", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
