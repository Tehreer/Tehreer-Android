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

extern "C" {
#include <ft2build.h>
#include FT_FREETYPE_H
#include FT_BITMAP_H
#include FT_IMAGE_H
#include FT_OUTLINE_H
#include FT_SIZES_H
#include FT_STROKER_H
#include FT_TYPES_H
}

#include <jni.h>

#include "Convert.h"
#include "FreeType.h"
#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "GlyphRasterizer.h"

using namespace Tehreer;

enum GlyphType : jint {
    UNKNOWN = 0,
    MASK = 1,
    COLOR = 2,
    MIXED = 3,
};

GlyphRasterizer::GlyphRasterizer(Typeface &typeface, FT_F26Dot6 pixelWidth, FT_F26Dot6 pixelHeight, FT_Matrix transform)
    : m_typeface(typeface)
    , m_size(nullptr)
    , m_transform(transform)
{
    m_typeface.lock();

    FT_Face baseFace = m_typeface.ftFace();
    FT_New_Size(baseFace, &m_size);
    FT_Activate_Size(m_size);
    FT_Set_Char_Size(baseFace, pixelWidth, pixelHeight, 0, 0);

    m_typeface.unlock();
}

GlyphRasterizer::~GlyphRasterizer()
{
    if (m_size) {
        /*
         * NOTE:
         *      FreeType face must be locked before releasing the size because it changes an
         *      internal list of the face containing all the sizes.
         */

        m_typeface.lock();
        FT_Done_Size(m_size);
        m_typeface.unlock();
    }
}

void GlyphRasterizer::unsafeActivate(FT_Face face, FT_Matrix *transform, const Typeface::Palette *palette)
{
    FT_Activate_Size(m_size);
    FT_Set_Transform(face, transform, nullptr);

    if (palette) {
        FT_Color *colors;
        FT_Palette_Select(face, 0, &colors);

        memcpy(colors, palette->data(), sizeof(FT_Color) * palette->size());
    }
}

jobject GlyphRasterizer::unsafeCreateBitmap(const JavaBridge bridge, const FT_Bitmap *bitmap)
{
    char pixelMode = bitmap->pixel_mode;
    jobject glyphBitmap = nullptr;
    size_t bitmapLength = 0;

    switch (pixelMode) {
    case FT_PIXEL_MODE_GRAY:
        bitmapLength = bitmap->width * bitmap->rows;
        if (bitmapLength > 0) {
            glyphBitmap = bridge.Bitmap_create(bitmap->width, bitmap->rows, JavaBridge::BitmapConfig::Alpha8);
            bridge.Bitmap_setPixels(glyphBitmap, bitmap->buffer, bitmapLength);
        }
        break;

    case FT_PIXEL_MODE_BGRA:
        bitmapLength = bitmap->width * bitmap->rows * 4;
        if (bitmapLength > 0) {
            for (size_t i = 0; i < bitmapLength; i += 4) {
                uint8_t b = bitmap->buffer[i + 0];
                uint8_t g = bitmap->buffer[i + 1];
                uint8_t r = bitmap->buffer[i + 2];
                uint8_t a = bitmap->buffer[i + 3];

                bitmap->buffer[i + 0] = r;
                bitmap->buffer[i + 1] = g;
                bitmap->buffer[i + 2] = b;
                bitmap->buffer[i + 3] = a;
            }

            glyphBitmap = bridge.Bitmap_create(bitmap->width, bitmap->rows, JavaBridge::BitmapConfig::ARGB_8888);
            bridge.Bitmap_setPixels(glyphBitmap, bitmap->buffer, bitmapLength);
        }
        break;

    default:
        LOGW("Unsupported pixel mode of freetype bitmap");
        break;
    }

    return glyphBitmap;
}

jint GlyphRasterizer::getGlyphType(FT_UInt glyphID)
{
    m_typeface.lock();

    FT_Face face = m_typeface.ftFace();
    FT_LayerIterator iterator;
    iterator.p = nullptr;

    FT_UInt layerGlyphID;
    FT_UInt colorIndex;

    bool isColored = false;
    bool hasMask = false;

    while (FT_Get_Color_Glyph_Layer(face, glyphID, &layerGlyphID, &colorIndex, &iterator)) {
        isColored = true;

        if (colorIndex == 0xFFFF) {
            hasMask = true;
            break;
        }
    }

    m_typeface.unlock();

    if (!isColored) {
        return GlyphType::MASK;
    }
    if (!hasMask) {
        return GlyphType::COLOR;
    }

    return GlyphType::MIXED;
}

jobject GlyphRasterizer::getGlyphImage(const JavaBridge bridge,
    FT_UInt glyphID, FT_Color foregroundColor)
{
    jobject glyphBitmap = nullptr;
    jint left = 0;
    jint top = 0;

    m_typeface.lock();

    FT_Face face = m_typeface.ftFace();
    unsafeActivate(face, m_typeface.palette());

    FT_Palette_Set_Foreground_Color(face, foregroundColor);
    FT_Error error = FT_Load_Glyph(face, glyphID, FT_LOAD_COLOR | FT_LOAD_RENDER);
    if (error == FT_Err_Ok) {
        FT_GlyphSlot glyphSlot = face->glyph;
        glyphBitmap = unsafeCreateBitmap(bridge, &glyphSlot->bitmap);

        if (glyphBitmap) {
            left = glyphSlot->bitmap_left;
            top = glyphSlot->bitmap_top;
        }
    }

    m_typeface.unlock();

    if (glyphBitmap) {
        return bridge.GlyphImage_construct(glyphBitmap, left, top);
    }

    return nullptr;
}

jobject GlyphRasterizer::getStrokeImage(const JavaBridge bridge, FT_Glyph baseGlyph,
    FT_Fixed lineRadius, FT_Stroker_LineCap lineCap,
    FT_Stroker_LineJoin lineJoin, FT_Fixed miterLimit)
{
    m_typeface.lock();

    FT_Stroker stroker = m_typeface.ftStroker();
    FT_Stroker_Set(stroker, lineRadius, lineCap, lineJoin, miterLimit);
    FT_Error error = FT_Glyph_Stroke(&baseGlyph, stroker, 0);

    m_typeface.unlock();

    if (error == FT_Err_Ok) {
        FT_Glyph_To_Bitmap(&baseGlyph, FT_RENDER_MODE_NORMAL, nullptr, 1);

        auto bitmapGlyph = reinterpret_cast<FT_BitmapGlyph>(baseGlyph);
        jobject strokeBitmap = nullptr;
        jint left = 0;
        jint top = 0;

        strokeBitmap = unsafeCreateBitmap(bridge, &bitmapGlyph->bitmap);
        if (strokeBitmap) {
            left = bitmapGlyph->left;
            top = bitmapGlyph->top;
        }

        /* Dispose the stroked / bitmap glyph. */
        FT_Done_Glyph(baseGlyph);

        if (strokeBitmap) {
            return bridge.GlyphImage_construct(strokeBitmap, left, top);
        }
    }

    return nullptr;
}

FT_Glyph GlyphRasterizer::getGlyphOutline(FT_UInt glyphID)
{
    m_typeface.lock();

    FT_Face baseFace = m_typeface.ftFace();
    unsafeActivate(baseFace, m_typeface.palette());

    FT_Glyph outline = nullptr;
    FT_Error error = FT_Load_Glyph(baseFace, glyphID, FT_LOAD_NO_BITMAP);
    if (error == FT_Err_Ok) {
        FT_Get_Glyph(baseFace->glyph, &outline);
    }

    m_typeface.unlock();

    return outline;
}

jobject GlyphRasterizer::getGlyphPath(const JavaBridge bridge, FT_UInt glyphID)
{
    FT_Matrix flip = { 1, 0, 0, -1 };
    FT_Matrix transform = {
        (m_transform.xx * flip.xx) + (m_transform.xy * flip.yx),
        (m_transform.xx * flip.xy) + (m_transform.xy * flip.yy),
        (m_transform.yx * flip.xx) + (m_transform.yy * flip.yx),
        (m_transform.yx * flip.xy) + (m_transform.yy * flip.yy)
    };

    m_typeface.lock();

    FT_Face face = m_typeface.ftFace();
    unsafeActivate(face, &transform);

    jobject glyphPath = m_typeface.unsafeGetGlyphPath(bridge, glyphID);

    m_typeface.unlock();

    return glyphPath;
}

static jlong create(JNIEnv *env, jobject obj, jlong typefaceHandle, jint pixelWidth, jint pixelHeight,
    jint transformXX, jint transformXY, jint transformYX, jint transformYY)
{
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Matrix transform = {
        transformXX, transformXY,
        transformYX, transformYY
    };

    auto glyphRasterizer = new GlyphRasterizer(*typeface, pixelWidth, pixelHeight, transform);
    return reinterpret_cast<jlong>(glyphRasterizer);
}

static void dispose(JNIEnv *env, jobject obj, jlong rasterizerHandle)
{
    auto glyphRasterizer = reinterpret_cast<GlyphRasterizer *>(rasterizerHandle);
    delete glyphRasterizer;
}

static jint getGlyphType(JNIEnv *env, jobject obj, jlong rasterizerHandle, jint glyphId)
{
    auto glyphRasterizer = reinterpret_cast<GlyphRasterizer *>(rasterizerHandle);
    auto glyphIndex = static_cast<FT_UInt>(glyphId);

    return glyphRasterizer->getGlyphType(glyphIndex);
}

static jobject getGlyphImage(JNIEnv *env, jobject obj, jlong rasterizerHandle,
    jint glyphId, jint foregroundColor)
{
    auto glyphRasterizer = reinterpret_cast<GlyphRasterizer *>(rasterizerHandle);
    auto glyphIndex = static_cast<FT_UInt>(glyphId);
    auto intColor = static_cast<uint32_t>(foregroundColor);
    FT_Color ftColor = toFTColor(intColor);

    return glyphRasterizer->getGlyphImage(JavaBridge(env), glyphIndex, ftColor);
}

static jobject getStrokeImage(JNIEnv *env, jobject obj, jlong rasterizerHandle, jlong outlineHandle,
    jint lineRadius, jint lineCap, jint lineJoin, jint miterLimit)
{
    auto glyphRasterizer = reinterpret_cast<GlyphRasterizer *>(rasterizerHandle);
    auto baseGlyph = reinterpret_cast<FT_Glyph>(outlineHandle);
    auto strokeRadius = static_cast<FT_Fixed>(lineRadius);
    auto strokeCap = static_cast<FT_Stroker_LineCap>(lineCap);
    auto strokeJoin = static_cast<FT_Stroker_LineJoin>(lineJoin);
    auto strokeMiter = static_cast<FT_Fixed>(miterLimit);

    return glyphRasterizer->getStrokeImage(JavaBridge(env), baseGlyph, strokeRadius,
                                           strokeCap, strokeJoin, strokeMiter);
}

static jlong getGlyphOutline(JNIEnv *env, jobject obj, jlong rasterizerHandle, jint glyphId)
{
    auto glyphRasterizer = reinterpret_cast<GlyphRasterizer *>(rasterizerHandle);
    auto glyphIndex = static_cast<FT_UInt>(glyphId);
    FT_Glyph glyphOutline = glyphRasterizer->getGlyphOutline(glyphIndex);

    return reinterpret_cast<jlong>(glyphOutline);
}

static jobject getGlyphPath(JNIEnv *env, jobject obj, jlong rasterizerHandle, jint glyphId)
{
    auto glyphRasterizer = reinterpret_cast<GlyphRasterizer *>(rasterizerHandle);
    auto glyphIndex = static_cast<FT_UInt>(glyphId);

    return glyphRasterizer->getGlyphPath(JavaBridge(env), glyphIndex);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "(JIIIIII)J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetGlyphType", "(JI)I", (void *)getGlyphType },
    { "nGetGlyphImage", "(JII)Lcom/mta/tehreer/graphics/GlyphImage;", (void *)getGlyphImage },
    { "nGetStrokeImage", "(JJIIII)Lcom/mta/tehreer/graphics/GlyphImage;", (void *)getStrokeImage },
    { "nGetGlyphOutline", "(JI)J", (void *)getGlyphOutline },
    { "nGetGlyphPath", "(JI)Landroid/graphics/Path;", (void *)getGlyphPath },
};

jint register_com_mta_tehreer_graphics_GlyphRasterizer(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/GlyphRasterizer", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
