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

#include <cstddef>
#include <mutex>

#include "Convert.h"
#include "FontFile.h"
#include "FreeType.h"
#include "RenderableFace.h"

using namespace std;
using namespace Tehreer;

RenderableFace *RenderableFace::create(FontFile &fontFile, FT_Face ftFace)
{
    if (!ftFace) {
        return nullptr;
    }

    return new RenderableFace(fontFile, ftFace);
}

RenderableFace::RenderableFace(FontFile &fontFile, FT_Face ftFace)
    : m_fontFile(fontFile.retain())
    , m_ftFace(ftFace)
    , m_retainCount(1)
{
    setupDefaultCoordinates();
}

void RenderableFace::setupDefaultCoordinates()
{
    FT_MM_Var *variation;
    if (FT_Get_MM_Var(m_ftFace, &variation) != FT_Err_Ok) {
        return;
    }

    FT_UInt numCoords = variation->num_axis;
    FT_Fixed fixedCoords[numCoords];

    if (FT_Get_Var_Blend_Coordinates(m_ftFace, numCoords, fixedCoords) == FT_Err_Ok) {
        m_coordinates.reserve(numCoords);

        for (FT_UInt i = 0; i < numCoords; i++) {
            m_coordinates.push_back(f16Dot16toFloat(fixedCoords[i]));
        }
    }

    FT_Done_MM_Var(FreeType::library(), variation);
}

RenderableFace::~RenderableFace()
{
    std::mutex &mutex = FreeType::mutex();
    mutex.lock();

    FT_Done_Face(m_ftFace);

    mutex.unlock();

    m_fontFile.release();
}

RenderableFace *RenderableFace::deriveVariation(const float *coordArray, size_t coordCount)
{
    FT_Long faceIndex = m_ftFace->face_index;
    RenderableFace *renderableFace = m_fontFile.createRenderableFace(faceIndex);

    renderableFace->m_coordinates = CoordArray(coordArray, coordArray + coordCount);

    FT_Face ftFace = renderableFace->ftFace();
    FT_Fixed fixedCoords[coordCount];

    for (size_t i = 0; i < coordCount; i++) {
        fixedCoords[i] = toF16Dot16(coordArray[i]);
    }

    FT_Set_Var_Design_Coordinates(ftFace, coordCount, fixedCoords);

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
