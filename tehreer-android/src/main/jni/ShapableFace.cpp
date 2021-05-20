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
#include FT_ADVANCES_H
#include FT_FREETYPE_H
#include FT_TRUETYPE_TABLES_H
}

#include "ShapableFace.h"

using namespace std;
using namespace Tehreer;

hb_font_funcs_t *ShapableFace::createFontFuncs()
{
    hb_font_funcs_t *funcs = hb_font_funcs_create();

    hb_font_funcs_set_nominal_glyph_func(funcs, [](hb_font_t *font, void *object,
                                                   hb_codepoint_t unicode,
                                                   hb_codepoint_t *glyph,
                                                   void *userData) -> hb_bool_t
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);
        instance->m_renderableFace->lock();

        FT_Face ftFace = instance->ftFace();
        FT_UInt glyphID = FT_Get_Char_Index(ftFace, unicode);
        bool found = false;

        if (glyphID) {
            *glyph = glyphID;
            found = true;
        }

        instance->m_renderableFace->unlock();

        return found;
    }, nullptr, nullptr);

    hb_font_funcs_set_nominal_glyphs_func(funcs, [](hb_font_t *font, void *object,
                                                    unsigned int count,
                                                    const hb_codepoint_t *firstUnicode,
                                                    unsigned int unicodeStride,
                                                    hb_codepoint_t *firstGlyph,
                                                    unsigned int glyphStride,
                                                    void *user_data) -> unsigned int
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);
        instance->m_renderableFace->lock();

        FT_Face ftFace = instance->ftFace();
        unsigned int done;

        auto unicodePtr = reinterpret_cast<const uint8_t *>(firstUnicode);
        auto glyphPtr = reinterpret_cast<uint8_t *>(firstGlyph);

        for (done = 0; done < count; done++) {
            auto unicodeRef = reinterpret_cast<const hb_codepoint_t *>(unicodePtr);
            auto glyphRef = reinterpret_cast<hb_codepoint_t *>(glyphPtr);

            FT_UInt glyphID = FT_Get_Char_Index(ftFace, *unicodeRef);

            if (glyphID) {
                *glyphRef = glyphID;
            } else {
                break;
            }

            unicodePtr += unicodeStride;
            glyphPtr += glyphStride;
        }

        instance->m_renderableFace->unlock();

        return done;
    }, nullptr, nullptr);

    hb_font_funcs_set_variation_glyph_func(funcs, [](hb_font_t *font, void *object,
                                                     hb_codepoint_t unicode,
                                                     hb_codepoint_t variationSelector,
                                                     hb_codepoint_t *glyph,
                                                     void *userData) -> hb_bool_t
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);
        instance->m_renderableFace->lock();

        FT_Face ftFace = instance->ftFace();
        FT_UInt glyphID = FT_Face_GetCharVariantIndex(ftFace, unicode, variationSelector);
        bool found = false;

        if (glyphID) {
            *glyph = glyphID;
            found = true;
        }

        instance->m_renderableFace->unlock();

        return found;
    }, nullptr, nullptr);

    hb_font_funcs_set_glyph_h_advance_func(funcs, [](hb_font_t *font, void *object,
                                                     hb_codepoint_t glyph,
                                                     void *userData) -> hb_position_t
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);
        instance->m_renderableFace->lock();

        FT_Face ftFace = instance->ftFace();

        FT_Fixed advance = 0;
        FT_Get_Advance(ftFace, glyph, FT_LOAD_NO_SCALE, &advance);

        instance->m_renderableFace->unlock();

        return advance;
    }, nullptr, nullptr);

    hb_font_funcs_set_glyph_h_advances_func(funcs, [](hb_font_t *font, void *object,
                                                      unsigned int count,
                                                      const hb_codepoint_t *firstGlyph,
                                                      unsigned glyphStride,
                                                      hb_position_t *firstAdvance,
                                                      unsigned advanceStride,
                                                      void *user_data) -> void
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);
        instance->m_renderableFace->lock();

        FT_Face ftFace = instance->ftFace();

        auto glyphPtr = reinterpret_cast<const uint8_t *>(firstGlyph);
        auto advancePtr = reinterpret_cast<uint8_t *>(firstAdvance);

        for (unsigned int i = 0; i < count; i++) {
            auto glyphRef = reinterpret_cast<const hb_codepoint_t *>(glyphPtr);
            auto advanceRef = reinterpret_cast<hb_codepoint_t *>(advancePtr);

            FT_Fixed advance = 0;
            FT_Get_Advance(ftFace, *glyphRef, FT_LOAD_NO_SCALE, &advance);

            *advanceRef = advance;

            glyphPtr += glyphStride;
            advancePtr += advanceStride;
        }

        instance->m_renderableFace->unlock();
    }, nullptr, nullptr);

    hb_font_funcs_make_immutable(funcs);

    return funcs;
}

hb_font_funcs_t *ShapableFace::defaultFontFuncs() {
    static hb_font_funcs_t *defaultFontFuncs = createFontFuncs();
    return defaultFontFuncs;
}

ShapableFace *ShapableFace::create(RenderableFace *renderableFace)
{
    if (renderableFace) {
        return new ShapableFace(renderableFace);
    }

    return nullptr;
}

ShapableFace::ShapableFace(RenderableFace *renderableFace)
    : m_retainCount(1)
    , m_renderableFace(renderableFace)
{
    FT_Face ftFace = renderableFace->ftFace();

    hb_face_t *hbFace = hb_face_create_for_tables([](hb_face_t *face, hb_tag_t tag,
                                                     void *object) -> hb_blob_t *
    {
        auto instance = reinterpret_cast<ShapableFace *>(object);
        instance->m_renderableFace->lock();

        FT_Face ftFace = instance->ftFace();
        void *memory = nullptr;
        
        FT_ULong length = 0;
        FT_Load_Sfnt_Table(ftFace, tag, 0, nullptr, &length);

        if (length > 0) {
            memory = malloc(length);

            auto buffer = reinterpret_cast<FT_Byte *>(memory);
            FT_Load_Sfnt_Table(ftFace, tag, 0, buffer, nullptr);
        }

        instance->m_renderableFace->unlock();

        if (memory) {
            auto buffer = reinterpret_cast<const char *>(memory);
            return hb_blob_create(buffer, length, HB_MEMORY_MODE_WRITABLE, nullptr, free);
        }

        return nullptr;
    }, this, nullptr);

    hb_face_set_index(hbFace, ftFace->face_index);
    hb_face_set_upem(hbFace, ftFace->units_per_EM);

    m_hbFont = hb_font_create(hbFace);
    hb_font_set_funcs(m_hbFont, defaultFontFuncs(), this, nullptr);

    hb_face_destroy(hbFace);
}

ShapableFace::~ShapableFace()
{
    hb_font_destroy(m_hbFont);
    m_renderableFace->release();
}

ShapableFace *ShapableFace::retain()
{
    m_retainCount++;
    return this;
}

void ShapableFace::release()
{
    if (--m_retainCount == 0) {
        delete this;
    }
}
