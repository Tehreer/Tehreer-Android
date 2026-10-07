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
#include <jni.h>
#include <mutex>
#include <vector>

#include <Tehreer/TRTypeface.h>

#include "FontFile.h"
#include "JavaBridge.h"
#include "RenderableFace.h"
#include "SfntTables.h"

namespace Tehreer {

class Typeface {
public:
    static Typeface *createFromFile(FontFile *fontFile, FT_Long faceIndex);

    ~Typeface();

    Typeface *deriveVariation(const float *coordArray, size_t coordCount);
    Typeface *deriveColor(const uint32_t *colorArray, size_t colorCount);

    void lock() { m_renderableFace.lock(); };
    void unlock() { m_renderableFace.unlock(); }

    inline TRTypefaceRef core() const { return m_core; }

    inline RenderableFace &renderableFace() const { return m_renderableFace; }
    inline FT_Face ftFace() const { return m_renderableFace.ftFace(); }

    inline uint16_t unitsPerEM() const { return TRTypefaceGetUnitsPerEM(m_core); }
    inline int16_t ascent() const { return TRTypefaceGetAscent(m_core); }
    inline int16_t descent() const { return TRTypefaceGetDescent(m_core); }
    inline int16_t leading() const { return TRTypefaceGetLeading(m_core); }

    inline int32_t glyphCount() const { return (int32_t)TRTypefaceGetGlyphCount(m_core); }

    inline int16_t underlinePosition() const { return TRTypefaceGetUnderlinePosition(m_core); }
    inline int16_t underlineThickness() const { return TRTypefaceGetUnderlineThickness(m_core); }

    inline int16_t strikeoutPosition() const { return TRTypefaceGetStrikeoutPosition(m_core); }
    inline int16_t strikeoutThickness() const { return TRTypefaceGetStrikeoutThickness(m_core); }

    jobject getNameRecord(const JavaBridge &javaBridge, int32_t nameIndex);
    jstring getNameString(const JavaBridge &javaBridge, int32_t nameIndex);

    uint16_t getGlyphID(uint32_t codePoint);
    float getGlyphAdvance(uint16_t glyphID, float typeSize, bool vertical);

    jobject getGlyphPath(JavaBridge bridge, uint16_t glyphID, float typeSize, float *transform);

private:
    /* The typeface of Core answers everything about the font. The face of FreeType only serves the
     * tables and the names that the sfnt package reads, which do not depend on the variations or
     * the colors, so the instances of a typeface share it. */
    TRTypefaceRef m_core;

    RenderableFace &m_renderableFace;
    Typeface(TRTypefaceRef core, RenderableFace &renderableFace);
    Typeface(const Typeface &parent, TRTypefaceRef core);
};

}

jint register_com_mta_tehreer_graphics_Typeface(JNIEnv *env);

#endif
