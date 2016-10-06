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

#ifndef _TEHREER__FREETYPE_H
#define _TEHREER__FREETYPE_H

extern "C" {
#include <ft2build.h>
#include FT_FREETYPE_H
}

#include <jni.h>
#include <mutex>

namespace Tehreer {

class FreeType {
public:
    static void load(JNIEnv *env);

    static std::mutex &mutex() { return s_instance->m_mutex; }
    static FT_Library library() { return s_instance->m_library; }

private:
    static FreeType *s_instance;
    std::mutex m_mutex;
    FT_Library m_library;

    FreeType();
    ~FreeType();
};

}

#endif
