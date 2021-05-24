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
#include "JavaBridge.h"

namespace Tehreer {

class RenderableFace {
public:
    static RenderableFace *create(FT_Face ftFace);
    ~RenderableFace();

    RenderableFace *retain();
    void release();

    inline void lock() { m_mutex.lock(); };
    inline void unlock() { m_mutex.unlock(); }

    inline FT_Face ftFace() const { return m_ftFace; }

private:
    std::mutex m_mutex;
    std::atomic_int m_retainCount;

    FT_Face m_ftFace;

    RenderableFace(FT_Face ftFace);
};

}

#endif