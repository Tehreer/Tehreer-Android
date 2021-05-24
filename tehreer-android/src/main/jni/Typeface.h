/*
 * Copyright (C) 2016-2021 Muhammad Tayyab Akram
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

#ifndef _TEHREER__TYPEFACE_H
#define _TEHREER__TYPEFACE_H

extern "C" {
#include <ft2build.h>
#include FT_COLOR_H
#include FT_FREETYPE_H
#include FT_STROKER_H
}

#include <cstddef>
#include <cstdint>
#include <hb.h>
#include <vector>

#include "FontFile.h"
#include "JavaBridge.h"
#include "IntrinsicFace.h"
#include "RenderableFace.h"
#include "ShapableFace.h"

namespace Tehreer {

class Typeface {
public:
    using Palette = std::vector<FT_Color>;

    static Typeface *createFromFile(FontFile *fontFile, FT_Long faceIndex, FT_Long instanceIndex);
    ~Typeface();

    Typeface *deriveVariation(FT_Fixed *coordArray, FT_UInt coordCount);
    Typeface *deriveColor(const uint32_t *colorArray, size_t colorCount);

    void lock() { m_instance->renderableFace().lock(); };
    void unlock() { m_instance->renderableFace().unlock(); }

    inline FT_Face ftFace() const { return m_instance->renderableFace().ftFace(); }
    inline FT_Size ftSize() const { return m_instance->ftSize(); }
    inline FT_Stroker ftStroker() { return m_instance->ftStroker(); };

    inline hb_font_t *hbFont() const { return m_instance->shapableFace().hbFont(); }

    inline const Palette *palette() const { return m_palette.size() == 0 ? nullptr : &m_palette; }

    inline int32_t familyName() const { return m_instance->familyName(); }
    inline int32_t styleName() const { return m_instance->styleName(); }
    inline int32_t fullName() const { return m_instance->fullName(); }

    inline uint16_t weight() const { return m_instance->weight(); }
    inline uint16_t width() const { return m_instance->width(); }
    inline uint16_t slope() const { return m_instance->slope(); }

    inline uint16_t unitsPerEM() const { return m_instance->unitsPerEM(); }
    inline int16_t ascent() const { return m_instance->ascent(); }
    inline int16_t descent() const { return m_instance->descent(); }
    inline int16_t leading() const { return m_instance->leading(); }

    inline int32_t glyphCount() const { return m_instance->glyphCount(); }

    inline int16_t underlinePosition() const { return m_instance->underlinePosition(); }
    inline int16_t underlineThickness() const { return m_instance->underlineThickness(); }

    inline int16_t strikeoutPosition() const { return m_instance->strikeoutPosition(); }
    inline int16_t strikeoutThickness() const { return m_instance->strikeoutThickness(); }

    void loadSfntTable(FT_ULong tag, FT_Byte *buffer, FT_ULong *length);
    int32_t searchNameRecordIndex(uint16_t nameID);

    FT_UInt getGlyphID(FT_ULong codePoint);
    float getGlyphAdvance(uint16_t glyphID, float typeSize, bool vertical);

    jobject getGlyphPathNoLock(JavaBridge bridge, FT_UInt glyphID);
    jobject getGlyphPath(JavaBridge bridge, FT_UInt glyphID, FT_F26Dot6 typeSize, FT_Matrix *matrix, FT_Vector *delta);

private:
    IntrinsicFace *m_instance;
    Palette m_palette;

    Typeface(IntrinsicFace *instance);
    Typeface(const Typeface &typeface, IntrinsicFace *instance);
    Typeface(const Typeface &typeface, const FT_Color *colorArray, size_t colorCount);
};

}

jint register_com_mta_tehreer_graphics_Typeface(JNIEnv *env);

#endif
