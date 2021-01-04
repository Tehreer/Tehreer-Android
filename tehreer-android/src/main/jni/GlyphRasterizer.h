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

#ifndef _TEHREER__GLYPH_RASTERIZER_H
#define _TEHREER__GLYPH_RASTERIZER_H

extern "C" {
#include <ft2build.h>
#include FT_COLOR_H
#include FT_FREETYPE_H
#include FT_TYPES_H
}

#include <jni.h>

#include "FreeType.h"
#include "Glyph.h"
#include "JavaBridge.h"
#include "Typeface.h"

namespace Tehreer {

class GlyphRasterizer {
public:
    GlyphRasterizer(Typeface &typeface, FT_F26Dot6 pixelWidth, FT_F26Dot6 pixelHeight, FT_Matrix transform);
    ~GlyphRasterizer();

    Typeface &typeface() { return m_typeface; }

    jint getGlyphType(FT_UInt glyphID);
    jobject getGlyphImage(const JavaBridge bridge, FT_UInt glyphID, FT_Color foregroundColor);
    jobject getStrokeImage(const JavaBridge bridge, FT_Glyph baseGlyph, FT_Fixed lineRadius,
        FT_Stroker_LineCap lineCap, FT_Stroker_LineJoin lineJoin, FT_Fixed miterLimit);

    void loadOutline(const JavaBridge bridge, jobject glyph);
    void loadPath(const JavaBridge bridge, jobject glyph);

private:
    Typeface &m_typeface;
    FT_Size m_size;
    FT_Matrix m_transform;

    void unsafeActivate(FT_Face ftFace, const Typeface::Palette *palette);
    jobject unsafeCreateBitmap(const JavaBridge bridge, const FT_Bitmap *bitmap);
};

}

jint register_com_mta_tehreer_graphics_GlyphRasterizer(JNIEnv *env);

#endif
