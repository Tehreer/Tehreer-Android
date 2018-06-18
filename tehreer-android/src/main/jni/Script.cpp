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
#include <SBBase.h>
#include <SBScript.h>
}

#include <jni.h>

#include "JavaBridge.h"
#include "Script.h"

using namespace Tehreer;

static jint getOpenTypeTag(JNIEnv *env, jobject obj, jbyte value)
{
    SBScript script = static_cast<SBScript>(value);
    SBUInt32 tag = SBScriptGetOpenTypeTag(script);

    return static_cast<jint>(tag);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nGetOpenTypeTag", "(B)I", (void *)getOpenTypeTag },
};

jint register_com_mta_tehreer_unicode_Script(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/unicode/Script", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
