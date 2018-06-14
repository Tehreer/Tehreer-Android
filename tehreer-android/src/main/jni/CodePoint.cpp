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
#include <SBBidiType.h>
#include <SBCodepoint.h>
#include <SBGeneralCategory.h>
#include <SBScript.h>
}

#include <jni.h>

#include "JavaBridge.h"
#include "CodePoint.h"

using namespace Tehreer;

static jbyte getBidiClass(JNIEnv *env, jobject obj, jint codePoint)
{
    SBCodepoint input = static_cast<SBCodepoint>(codePoint);
    SBBidiType bidiType = SBCodepointGetBidiType(input);

    return static_cast<jbyte>(bidiType);
}

static jbyte getGeneralCategory(JNIEnv *env, jobject obj, jint codePoint)
{
    SBCodepoint input = static_cast<SBCodepoint>(codePoint);
    SBGeneralCategory generalCategory = SBCodepointGetGeneralCategory(input);

    return static_cast<jbyte>(generalCategory);
}

static jshort getScript(JNIEnv *env, jobject obj, jint codePoint)
{
    SBCodepoint input = static_cast<SBCodepoint>(codePoint);
    SBScript script = SBCodepointGetScript(input);

    return static_cast<jshort>(script);
}

static jint getMirror(JNIEnv *env, jobject obj, jint codePoint)
{
    SBCodepoint input = static_cast<SBCodepoint>(codePoint);
    SBCodepoint mirror = SBCodepointGetMirror(input);

    return static_cast<jint>(mirror);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nGetBidiClass", "(I)B", (void *)getBidiClass },
    { "nGetGeneralCategory", "(I)B", (void *)getGeneralCategory },
    { "nGetScript", "(I)B", (void *)getScript },
    { "nGetMirror", "(I)I", (void *)getMirror },
};

jint register_com_mta_tehreer_unicode_CodePoint(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/unicode/CodePoint", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
