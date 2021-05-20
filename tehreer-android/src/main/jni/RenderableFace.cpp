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
}

#include <mutex>

#include "FontFile.h"
#include "FreeType.h"
#include "RenderableFace.h"

using namespace std;
using namespace Tehreer;

RenderableFace *RenderableFace::create(FT_Face ftFace)
{
    if (ftFace) {
        return new RenderableFace(ftFace);
    }

    return nullptr;
}

RenderableFace::RenderableFace(FT_Face ftFace)
    : m_retainCount(1)
    , m_ftFace(ftFace)
{
}

RenderableFace::~RenderableFace()
{
    std::mutex &mutex = FreeType::mutex();
    mutex.lock();

    FT_Done_Face(m_ftFace);

    mutex.unlock();
}

RenderableFace *RenderableFace::retain()
{
    m_retainCount++;
    return this;
}

void RenderableFace::release()
{
    if (--m_retainCount == 0) {
        delete this;
    }
}
