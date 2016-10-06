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
#include FT_FREETYPE_H
}

#include <jni.h>

#include "FreeType.h"

using namespace Tehreer;

FreeType *FreeType::s_instance = nullptr;

void FreeType::load(JNIEnv *env)
{
    FT_Library library;
    FT_Init_FreeType(&library);

    s_instance = new FreeType();
    s_instance->m_library = library;
}

FreeType::FreeType()
    : m_mutex()
    , m_library(nullptr)
{
}

FreeType::~FreeType()
{
    FT_Done_FreeType(m_library);
}
