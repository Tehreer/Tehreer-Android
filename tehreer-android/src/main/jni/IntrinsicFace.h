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
#include <cstddef>
#include <cstdint>
#include <jni.h>
#include <mutex>

#include "FontFile.h"
#include "RenderableFace.h"
#include "SfntTables.h"
#include "ShapableFace.h"

namespace Tehreer {

class IntrinsicFace {
public:
    enum Slope : uint16_t {
        PLAIN = 0,
        ITALIC = 1,
        OBLIQUE = 2,
    };

    static IntrinsicFace &create(RenderableFace &renderableFace);

    void setupCoordinates(const float *coordArray, size_t coordCount);
    void setupVariation(float italValue, float slntValue, float wdthValue, float wghtValue);

    ~IntrinsicFace();

    IntrinsicFace *deriveVariation(const float *coordArray, size_t coordCount);

    inline RenderableFace &renderableFace() const { return m_renderableFace; }
    inline FT_Size ftSize() const { return m_ftSize; }
    FT_Stroker ftStroker();

    inline ShapableFace &shapableFace() const { return *m_shapableFace; }

    inline const CoordArray *coordinates() const { return m_renderableFace.coordinates(); }

    inline int32_t familyName() const { return m_description.familyName; }
    inline int32_t styleName() const { return m_description.styleName; }
    inline int32_t fullName() const { return m_description.fullName; }

    inline uint16_t weight() const { return m_description.weight; }
    inline uint16_t width() const { return m_description.width; }
    inline uint16_t slope() const { return m_description.slope; }

    inline uint16_t unitsPerEM() const { return ftFace()->units_per_EM; }
    inline int16_t ascent() const { return ftFace()->ascender; }
    inline int16_t descent() const { return -ftFace()->descender; }
    inline int16_t leading() const { return ftFace()->height - (ascent() + descent()); }

    inline int32_t glyphCount() const { return (int32_t)ftFace()->num_glyphs; }

    inline int16_t underlinePosition() const { return ftFace()->underline_position; }
    inline int16_t underlineThickness() const { return ftFace()->underline_thickness; }

    inline int16_t strikeoutPosition() const { return m_strikeoutPosition; }
    inline int16_t strikeoutThickness() const { return m_strikeoutThickness; }

    size_t getTableLength(uint32_t tag);
    void getTableData(uint32_t tag, void *buffer);

    int32_t searchNameRecordIndex(uint16_t nameID);

    uint16_t getGlyphID(uint32_t codePoint);
    float getGlyphAdvance(uint16_t glyphID, float typeSize, bool vertical);

    jobject unsafeGetGlyphPath(JavaBridge bridge, uint16_t glyphID);
    jobject getGlyphPath(JavaBridge bridge, uint16_t glyphID, float typeSize, float *transform);

    IntrinsicFace &retain();
    void release();

private:
    struct Description {
        int32_t familyName;
        int32_t styleName;
        int32_t fullName;

        uint16_t weight;
        uint16_t width;
        uint16_t slope;

        Description() {
            familyName = -1;
            styleName = -1;
            fullName = -1;

            weight = SFNT::OS2::Weight::REGULAR;
            width = SFNT::OS2::Width::NORMAL;
            slope = Slope::PLAIN;
        }
    };

    struct DefaultProperties {
        Description description;
    };

    std::mutex m_mutex;

    RenderableFace &m_renderableFace;
    FT_Size m_ftSize;
    FT_Stroker m_ftStroker;

    ShapableFace *m_shapableFace;

    DefaultProperties m_defaults;
    Description m_description;

    int16_t m_strikeoutPosition;
    int16_t m_strikeoutThickness;

    std::atomic_int m_retainCount;

    IntrinsicFace(RenderableFace &renderableFace);
    IntrinsicFace(const IntrinsicFace &parent, RenderableFace &renderableFace);

    void setupSize();
    void setupStrikeout();
    void setupDescription();
    void setupHarfBuzz(ShapableFace *parent = nullptr);

    inline FT_Face ftFace() const { return m_renderableFace.ftFace(); }
};

}

#endif
