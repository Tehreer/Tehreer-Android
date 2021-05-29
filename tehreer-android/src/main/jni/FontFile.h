/*
 * Copyright (C) 2019-2021 Muhammad Tayyab Akram
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

extern "C" {
#include <ft2build.h>
#include FT_FREETYPE_H
#include FT_SYSTEM_H
}

#include <android/asset_manager.h>
#include <atomic>
#include <jni.h>

#include "JavaBridge.h"

namespace Tehreer {

class RenderableFace;

class FontFile {
public:
    static FontFile *createFromAsset(AAssetManager *assetManager, const char *path);
    static FontFile *createFromPath(const char *path);
    static FontFile *createFromStream(const JavaBridge &bridge, jobject stream);

    ~FontFile();

    FT_Long numFaces() const { return m_numFaces; }
    RenderableFace *createRenderableFace(FT_Long faceIndex);

    FontFile &retain();
    void release();

private:
    FT_Open_Args m_args;

    void *m_buffer;
    FT_Stream m_stream;
    FT_Long m_numFaces;
    std::atomic_int m_retainCount;

    static FontFile *createWithArgs(const FT_Open_Args *args);

    FontFile(const FT_Open_Args *args, void *buffer, FT_Stream stream, FT_Long numFaces);
};

}

jint register_com_mta_tehreer_font_FontFile(JNIEnv *env);

#endif
