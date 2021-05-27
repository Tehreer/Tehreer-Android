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

extern "C" {
#include <ft2build.h>
#include FT_FREETYPE_H
#include FT_MULTIPLE_MASTERS_H
}

#include <mutex>

#include "FontFile.h"
#include "FreeType.h"
#include "RenderableFace.h"

using namespace std;
using namespace Tehreer;

RenderableFace *RenderableFace::create(FontFile *fontFile, FT_Face ftFace)
{
    if (fontFile && ftFace) {
        return new RenderableFace(fontFile, ftFace);
    }

    return nullptr;
}

RenderableFace::RenderableFace(FontFile *fontFile, FT_Face ftFace)
    : m_fontFile(&fontFile->retain())
    , m_ftFace(ftFace)
    , m_retainCount(1)
{
}

RenderableFace::~RenderableFace()
{
    std::mutex &mutex = FreeType::mutex();
    mutex.lock();

    FT_Done_Face(m_ftFace);

    mutex.unlock();

    m_fontFile->release();
}

RenderableFace *RenderableFace::deriveVariation(FT_Fixed *coordArray, FT_UInt coordCount)
{
    FT_Long faceIndex = m_ftFace->face_index;
    RenderableFace *renderableFace = m_fontFile->createRenderableFace(faceIndex, 0);
    FT_Face ftFace = renderableFace->ftFace();

    FT_Set_Var_Design_Coordinates(ftFace, coordCount, coordArray);

    return renderableFace;
}

RenderableFace &RenderableFace::retain()
{
    m_retainCount++;
    return *this;
}

void RenderableFace::release()
{
    if (--m_retainCount == 0) {
        delete this;
    }
}
