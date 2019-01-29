/*
 * Copyright (C) 2016-2019 Muhammad Tayyab Akram
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
#include FT_FREETYPE_H
#include FT_STROKER_H
#include FT_SYSTEM_H

#include <SFFont.h>
}

#include <android/asset_manager.h>
#include <cstdint>
#include <jni.h>
#include <mutex>

#include "FontFile.h"
#include "JavaBridge.h"
#include "PatternCache.h"

namespace Tehreer {

class Typeface {
public:
    enum Slope : uint16_t {
        PLAIN = 0,
        ITALIC = 1,
        OBLIQUE = 2,
    };

    static Typeface *createFromFile(FontFile *fontFile, FT_Long faceIndex, FT_Long instanceIndex);

    ~Typeface();

    void lock() { m_mutex.lock(); };
    void unlock() { m_mutex.unlock(); }

    FT_Face ftFace() const { return m_ftFace; }
    FT_Stroker ftStroker();

    SFFontRef sfFont() const { return m_sfFont; }
    PatternCache &patternCache() { return m_patternCache; }

    uint16_t unitsPerEM() const { return m_ftFace->units_per_EM; }
    int16_t ascent() const { return m_ftFace->ascender; }
    int16_t descent() const { return -m_ftFace->descender; }
    int16_t leading() const { return m_ftFace->height - (ascent() + descent()); }

    int32_t glyphCount() const { return (int32_t)m_ftFace->num_glyphs; }

    int32_t familyName() const { return m_familyName; }
    int32_t styleName() const { return m_styleName; }
    int32_t fullName() const { return m_fullName; }

    uint16_t weight() const { return m_weight; }
    uint16_t width() const { return m_width; }
    uint16_t slope() const { return m_slope; }

    int16_t underlinePosition() const { return m_ftFace->underline_position; }
    int16_t underlineThickness() const { return m_ftFace->underline_thickness; }
    int16_t strikeoutPosition() const { return m_strikeoutPosition; }
    int16_t strikeoutThickness() const { return m_strikeoutThickness; }

    void loadSfntTable(FT_ULong tag, FT_Byte *buffer, FT_ULong *length);

    FT_UInt getGlyphID(FT_ULong codePoint);
    FT_Fixed getGlyphAdvance(FT_UInt glyphID, bool vertical);
    FT_Fixed getGlyphAdvance(FT_UInt glyphID, FT_F26Dot6 typeSize, bool vertical);

    jobject getGlyphPathNoLock(JavaBridge bridge, FT_UInt glyphID);
    jobject getGlyphPath(JavaBridge bridge, FT_UInt glyphID, FT_F26Dot6 typeSize, FT_Matrix *matrix, FT_Vector *delta);

private:
    std::mutex m_mutex;
    FontFile *m_fontFile;
    FT_Face m_ftFace;
    FT_Size m_ftSize;
    FT_Stroker m_ftStroker;
    SFFontRef m_sfFont;
    PatternCache m_patternCache;

    int32_t m_familyName;
    int32_t m_styleName;
    int32_t m_fullName;

    uint16_t m_weight;
    uint16_t m_width;
    uint16_t m_slope;

    int16_t m_strikeoutPosition;
    int16_t m_strikeoutThickness;

    Typeface(FontFile *fontFile, FT_Face ftFace);

    void setupDescription();
    void setupVariation();
};

}

jint register_com_mta_tehreer_graphics_Typeface(JNIEnv *env);

#endif
