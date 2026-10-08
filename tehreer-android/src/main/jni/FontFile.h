/*
 * Copyright (C) 2019-2026 Muhammad Tayyab Akram
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

#ifndef _TEHREER__FONT_FILE_H
#define _TEHREER__FONT_FILE_H

#include <jni.h>

#include <Tehreer/TRFontFile.h>

namespace Tehreer {

/* The functions create a font file of Core, which the caller has to release. They return null if
 * the argument is null or does not give a usable font. */
TRFontFileRef createFontFileFromAsset(JNIEnv *env, jobject assetManager, jstring path);
TRFontFileRef createFontFileFromPath(JNIEnv *env, jstring path);
TRFontFileRef createFontFileFromStream(JNIEnv *env, jobject stream);

}

jint register_com_mta_tehreer_font_FontFile(JNIEnv *env);

#endif
