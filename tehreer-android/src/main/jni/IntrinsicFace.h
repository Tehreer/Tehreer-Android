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

#ifndef _TEHREER__INTRINSIC_FACE_H
#define _TEHREER__INTRINSIC_FACE_H

extern "C" {
#include <ft2build.h>
#include FT_FREETYPE_H
#include FT_STROKER_H
}

#include <atomic>
#include <cstdint>
#include <mutex>

#include "FontFile.h"
#include "RenderableFace.h"
#include "ShapableFace.h"

namespace Tehreer {

class IntrinsicFace {
public:
    enum Slope : uint16_t {
        PLAIN = 0,
        ITALIC = 1,
        OBLIQUE = 2,
    };

    static IntrinsicFace *create(FontFile *fontFile, RenderableFace *renderableFace);
    ~IntrinsicFace();

    inline FontFile *fontFile() const { return m_fontFile; }

    inline RenderableFace *renderableFace() const { return m_renderableFace; }
    inline FT_Size ftSize() const { return m_ftSize; }
    FT_Stroker ftStroker();

    inline ShapableFace *shapableFace() const { return m_shapableFace; }

    inline int32_t familyName() const { return m_familyName; }
    inline int32_t styleName() const { return m_styleName; }
    inline int32_t fullName() const { return m_fullName; }

    inline uint16_t weight() const { return m_weight; }
    inline uint16_t width() const { return m_width; }
    inline uint16_t slope() const { return m_slope; }

    inline uint16_t unitsPerEM() const { return ftFace()->units_per_EM; }
    inline int16_t ascent() const { return ftFace()->ascender; }
    inline int16_t descent() const { return -ftFace()->descender; }
    inline int16_t leading() const { return ftFace()->height - (ascent() + descent()); }

    inline int32_t glyphCount() const { return (int32_t)ftFace()->num_glyphs; }

    inline int16_t underlinePosition() const { return ftFace()->underline_position; }
    inline int16_t underlineThickness() const { return ftFace()->underline_thickness; }

    inline int16_t strikeoutPosition() const { return m_strikeoutPosition; }
    inline int16_t strikeoutThickness() const { return m_strikeoutThickness; }

    void loadSfntTable(FT_ULong tag, FT_Byte *buffer, FT_ULong *length);
    int32_t searchNameRecordIndex(uint16_t nameID);
    FT_UInt getGlyphID(FT_ULong codePoint);

    IntrinsicFace *retain();
    void release();

private:
    std::mutex m_mutex;

    FontFile *m_fontFile;
    RenderableFace *m_renderableFace;
    FT_Size m_ftSize;
    FT_Stroker m_ftStroker;

    ShapableFace *m_shapableFace;

    int32_t m_familyName;
    int32_t m_styleName;
    int32_t m_fullName;

    uint16_t m_weight;
    uint16_t m_width;
    uint16_t m_slope;

    int16_t m_strikeoutPosition;
    int16_t m_strikeoutThickness;

    std::atomic_int m_retainCount;

    IntrinsicFace(FontFile *fontFile, RenderableFace *renderableFace);
    void setupDescription();
    void setupVariation();
    void setupHarfBuzz();

    inline FT_Face ftFace() const { return m_renderableFace->ftFace(); }
};

}

#endif
