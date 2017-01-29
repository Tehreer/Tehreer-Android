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

#include "Tehreer.h"

using namespace Tehreer;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *jvm, void *reserved) {
    JNIEnv *env;
    jint result;

    if (jvm->GetEnv((void **)&env, JNI_VERSION_1_6)) {
        return JNI_ERR;
    }

    if (env == nullptr) {
        return JNI_ERR;
    }

    JavaBridge::load(env);
    FreeType::load(env);

    result = register_com_mta_tehreer_bidi_BidiAlgorithm(env) == JNI_OK
          && register_com_mta_tehreer_bidi_BidiBuffer(env) == JNI_OK
          && register_com_mta_tehreer_bidi_BidiLine(env) == JNI_OK
          && register_com_mta_tehreer_bidi_BidiMirrorLocator(env) == JNI_OK
          && register_com_mta_tehreer_bidi_BidiParagraph(env) == JNI_OK
          && register_com_mta_tehreer_graphics_Glyph(env) == JNI_OK
          && register_com_mta_tehreer_graphics_GlyphRasterizer(env) == JNI_OK
          && register_com_mta_tehreer_graphics_Typeface(env) == JNI_OK
          && register_com_mta_tehreer_internal_Memory(env) == JNI_OK
          && register_com_mta_tehreer_opentype_OpenTypeAlbum(env) == JNI_OK
          && register_com_mta_tehreer_opentype_OpenTypeArtist(env) == JNI_OK
          && register_com_mta_tehreer_opentype_SfntNames(env) == JNI_OK;

    if (!result) {
        return JNI_ERR;
    }

    return JNI_VERSION_1_6;
}
