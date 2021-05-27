/*
 * Copyright (C) 2021 Muhammad Tayyab Akram
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

#ifndef _TEHREER__RENDERABLE_FACE_H
#define _TEHREER__RENDERABLE_FACE_H

extern "C" {
#include <ft2build.h>
#include FT_FREETYPE_H
}

#include <atomic>
#include <mutex>

#include "FontFile.h"

namespace Tehreer {

class RenderableFace {
public:
    static RenderableFace *create(FontFile *fontFile, FT_Face ftFace);
    ~RenderableFace();

    RenderableFace *deriveVariation(FT_Fixed *coordArray, FT_UInt coordCount);

    inline void lock() { m_mutex.lock(); };
    inline void unlock() { m_mutex.unlock(); }

    inline FontFile &fontFile() const { return *m_fontFile; }
    inline FT_Face ftFace() const { return m_ftFace; }

    RenderableFace &retain();
    void release();

private:
    std::mutex m_mutex;

    FontFile *m_fontFile;
    FT_Face m_ftFace;

    std::atomic_int m_retainCount;

    RenderableFace(FontFile *fontFile, FT_Face ftFace);
};

}

#endif
