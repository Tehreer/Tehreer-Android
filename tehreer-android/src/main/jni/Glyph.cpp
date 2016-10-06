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
#include <ft2build.h>
#include FT_GLYPH_H
}

#include <jni.h>

#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "Glyph.h"

using namespace Tehreer;

static void disposeOutline(JNIEnv *env, jobject obj, jlong handle)
{
    FT_Glyph glyph = reinterpret_cast<FT_Glyph>(handle);
    FT_Done_Glyph(glyph);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeDisposeOutline", "(J)V", (void *)disposeOutline },
};

jint register_com_mta_tehreer_graphics_Glyph(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/Glyph", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
