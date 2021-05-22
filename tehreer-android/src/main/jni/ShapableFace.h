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

#ifndef _TEHREER__SHAPABLE_FACE_H
#define _TEHREER__SHAPABLE_FACE_H

#include <atomic>
#include <hb.h>
#include <mutex>

#include "RenderableFace.h"

namespace Tehreer {

class ShapableFace {
public:
    static ShapableFace *create(RenderableFace *renderableFace);
    static ShapableFace *create(ShapableFace *parent, RenderableFace *renderableFace);
    ~ShapableFace();

    ShapableFace *retain();
    void release();

    inline hb_font_t *hbFont() const { return m_hbFont; }

private:
    static hb_font_funcs_t *createFontFuncs();
    static hb_font_funcs_t *defaultFontFuncs();

    ShapableFace *m_rootFace;

    RenderableFace *m_renderableFace;
    hb_font_t *m_hbFont;

    std::atomic_int m_retainCount;

    ShapableFace(RenderableFace *renderableFace);
    ShapableFace(ShapableFace *parent, RenderableFace *renderableFace);

    void setupCoordinates();

    inline FT_Face ftFace() const { return m_renderableFace->ftFace(); }
};

}

#endif
