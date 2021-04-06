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
#include FT_SIZES_H
#include FT_STROKER_H
#include FT_SYSTEM_H
}

#include <android/asset_manager.h>
#include <atomic>
#include <cstddef>
#include <cstdint>
#include <hb.h>
#include <jni.h>
#include <mutex>

#include "FontFile.h"
#include "JavaBridge.h"

namespace Tehreer {

class Typeface {
public:
    enum Slope : uint16_t {
        PLAIN = 0,
        ITALIC = 1,
        OBLIQUE = 2,
    };

    struct Palette {
        FT_Color *colors;
        size_t count;
    };

    static Typeface *createFromFile(FontFile *fontFile, FT_Long faceIndex, FT_Long instanceIndex);

    ~Typeface();

    Typeface *deriveVariation(FT_Fixed *coordArray, FT_UInt coordCount);
    Typeface *deriveColor(const uint32_t *colorArray, size_t colorCount);

    void lock() { m_instance->m_mutex.lock(); };
    void unlock() { m_instance->m_mutex.unlock(); }

    FT_Face ftFace() const { return m_instance->m_ftFace; }
    FT_Size ftSize() const { return m_instance->m_ftSize; }
    FT_Stroker ftStroker();

    hb_font_t *hbFont() const { return m_instance->m_hbFont; }

    const Palette *palette() const { return m_palette.count == 0 ? nullptr : &m_palette; }

    int32_t familyName() const { return m_instance->m_familyName; }
    int32_t styleName() const { return m_instance->m_styleName; }
    int32_t fullName() const { return m_instance->m_fullName; }

    uint16_t weight() const { return m_instance->m_weight; }
    uint16_t width() const { return m_instance->m_width; }
    uint16_t slope() const { return m_instance->m_slope; }

    uint16_t unitsPerEM() const { return ftFace()->units_per_EM; }
    int16_t ascent() const { return ftFace()->ascender; }
    int16_t descent() const { return -ftFace()->descender; }
    int16_t leading() const { return ftFace()->height - (ascent() + descent()); }

    int32_t glyphCount() const { return (int32_t)ftFace()->num_glyphs; }

    int16_t underlinePosition() const { return ftFace()->underline_position; }
    int16_t underlineThickness() const { return ftFace()->underline_thickness; }

    int16_t strikeoutPosition() const { return m_instance->m_strikeoutPosition; }
    int16_t strikeoutThickness() const { return m_instance->m_strikeoutThickness; }

    void loadSfntTable(FT_ULong tag, FT_Byte *buffer, FT_ULong *length);

    FT_UInt getGlyphID(FT_ULong codePoint);
    FT_Fixed getGlyphAdvance(FT_UInt glyphID, FT_F26Dot6 typeSize, bool vertical);

    jobject getGlyphPathNoLock(JavaBridge bridge, FT_UInt glyphID);
    jobject getGlyphPath(JavaBridge bridge, FT_UInt glyphID, FT_F26Dot6 typeSize, FT_Matrix *matrix, FT_Vector *delta);

private:
    class Instance {
    private:
        std::mutex m_mutex;
        std::atomic_int m_retainCount;

        FontFile *m_fontFile;
        FT_Face m_ftFace;
        FT_Size m_ftSize;
        FT_Stroker m_ftStroker;

        hb_font_t *m_hbFont;

        int32_t m_familyName;
        int32_t m_styleName;
        int32_t m_fullName;

        uint16_t m_weight;
        uint16_t m_width;
        uint16_t m_slope;

        int16_t m_strikeoutPosition;
        int16_t m_strikeoutThickness;

        Instance(FontFile *fontFile, FT_Face ftFace);
        ~Instance();

        void setupDescription();
        void setupVariation();
        void setupHarfBuzz();

        Instance *retain();
        void release();

        void loadSfntTable(FT_ULong tag, FT_Byte *buffer, FT_ULong *length);

        FT_UInt getGlyphID(FT_ULong codePoint);
        FT_Fixed getUnscaledAdvance(FT_UInt glyphID, bool vertical);

        friend class Typeface;
    };

    Instance *m_instance;
    Palette m_palette;

    Typeface(Instance *instance);
    Typeface(const Typeface &typeface, const Palette &palette);
};

}

jint register_com_mta_tehreer_graphics_Typeface(JNIEnv *env);

#endif
