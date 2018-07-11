/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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

#include "FreeType.h"
#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "GlyphRasterizer.h"

using namespace Tehreer;

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

void GlyphRasterizer::unsafeActivate(FT_Face ftFace)
{
    FT_Activate_Size(m_size);
    FT_Set_Transform(ftFace, &m_transform, nullptr);
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

    default:
        LOGW("Unsupported pixel mode of freetype bitmap");
        break;
    }

    return glyphBitmap;
}

void GlyphRasterizer::loadBitmap(const JavaBridge bridge, jobject glyph)
{
    FT_UInt glyphID = static_cast<FT_UInt>(bridge.Glyph_getGlyphID(glyph));
    jobject glyphBitmap = nullptr;
    jint leftSideBearing = 0;
    jint topSideBearing = 0;

    m_typeface.lock();

    FT_Face baseFace = m_typeface.ftFace();
    unsafeActivate(baseFace);

    FT_Error error = FT_Load_Glyph(baseFace, glyphID, FT_LOAD_RENDER);
    if (error == FT_Err_Ok) {
        FT_GlyphSlot glyphSlot = baseFace->glyph;
        glyphBitmap = unsafeCreateBitmap(bridge, &glyphSlot->bitmap);

        if (glyphBitmap) {
            leftSideBearing = glyphSlot->bitmap_left;
            topSideBearing = glyphSlot->bitmap_top;
        }
    }

    m_typeface.unlock();

    bridge.Glyph_ownBitmap(glyph, glyphBitmap, leftSideBearing, topSideBearing);
}

void GlyphRasterizer::loadOutline(const JavaBridge bridge, jobject glyph)
{
    FT_UInt glyphID = static_cast<FT_UInt>(bridge.Glyph_getGlyphID(glyph));

    m_typeface.lock();

    FT_Face baseFace = m_typeface.ftFace();
    unsafeActivate(baseFace);

    FT_Glyph outline = nullptr;
    FT_Error error = FT_Load_Glyph(baseFace, glyphID, FT_LOAD_NO_BITMAP);
    if (error == FT_Err_Ok) {
        FT_Get_Glyph(baseFace->glyph, &outline);
    }

    m_typeface.unlock();

    bridge.Glyph_ownOutline(glyph, outline ? reinterpret_cast<jlong>(outline) : 0);
}

void GlyphRasterizer::loadPath(const JavaBridge bridge, jobject glyph)
{
    FT_UInt glyphID = static_cast<FT_UInt>(bridge.Glyph_getGlyphID(glyph));

    m_typeface.lock();

    FT_Face baseFace = m_typeface.ftFace();
    unsafeActivate(baseFace);

    jobject glyphPath = m_typeface.getGlyphPathNoLock(bridge, glyphID);

    m_typeface.unlock();

    bridge.Glyph_ownPath(glyph, glyphPath);
}

jobject GlyphRasterizer::strokeGlyph(const JavaBridge bridge, jobject glyph, FT_Fixed lineRadius,
    FT_Stroker_LineCap lineCap, FT_Stroker_LineJoin lineJoin, FT_Fixed miterLimit)
{
    FT_UInt glyphID = static_cast<FT_UInt>(bridge.Glyph_getGlyphID(glyph));
    FT_Glyph baseGlyph = reinterpret_cast<FT_Glyph>(bridge.Glyph_getNativeOutline(glyph));

    if (baseGlyph) {
        m_typeface.lock();

        FT_Stroker stroker = m_typeface.ftStroker();
        FT_Stroker_Set(stroker, lineRadius, lineCap, lineJoin, miterLimit);
        FT_Error error = FT_Glyph_Stroke(&baseGlyph, stroker, 0);

        m_typeface.unlock();

        if (error == FT_Err_Ok) {
            FT_Glyph_To_Bitmap(&baseGlyph, FT_RENDER_MODE_NORMAL, nullptr, 1);

            FT_BitmapGlyph bitmapGlyph = reinterpret_cast<FT_BitmapGlyph>(baseGlyph);
            jobject strokeBitmap = nullptr;
            jint leftSideBearing = 0;
            jint topSideBearing = 0;

            strokeBitmap = unsafeCreateBitmap(bridge, &bitmapGlyph->bitmap);
            if (strokeBitmap) {
                leftSideBearing = bitmapGlyph->left;
                topSideBearing = bitmapGlyph->top;
            }

            jobject result = bridge.Glyph_construct(glyphID);
            bridge.Glyph_ownBitmap(result, strokeBitmap, leftSideBearing, topSideBearing);

            /* Dispose the stroked / bitmap glyph. */
            FT_Done_Glyph(baseGlyph);

            return result;
        }
    }

    return nullptr;
}

static jlong create(JNIEnv *env, jobject obj, jlong typefaceHandle, jint pixelWidth, jint pixelHeight,
    jint transformXX, jint transformXY, jint transformYX, jint transformYY)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Matrix transform = {
        transformXX, transformXY,
        transformYX, transformYY
    };

    GlyphRasterizer *glyphRasterizer = new GlyphRasterizer(*typeface, pixelWidth, pixelHeight, transform);
    return reinterpret_cast<jlong>(glyphRasterizer);
}

static void dispose(JNIEnv *env, jobject obj, jlong rasterizerHandle)
{
    GlyphRasterizer *glyphRasterizer = reinterpret_cast<GlyphRasterizer *>(rasterizerHandle);
    delete glyphRasterizer;
}

static void loadBitmap(JNIEnv *env, jobject obj, jlong rasterizerHandle, jobject glyph)
{
    GlyphRasterizer *glyphRasterizer = reinterpret_cast<GlyphRasterizer *>(rasterizerHandle);
    glyphRasterizer->loadBitmap(JavaBridge(env), glyph);
}

static void loadOutline(JNIEnv *env, jobject obj, jlong rasterizerHandle, jobject glyph)
{
    GlyphRasterizer *glyphRasterizer = reinterpret_cast<GlyphRasterizer *>(rasterizerHandle);
    glyphRasterizer->loadOutline(JavaBridge(env), glyph);
}

static void loadPath(JNIEnv *env, jobject obj, jlong rasterizerHandle, jobject glyph)
{
    GlyphRasterizer *glyphRasterizer = reinterpret_cast<GlyphRasterizer *>(rasterizerHandle);
    glyphRasterizer->loadPath(JavaBridge(env), glyph);
}

static jobject strokeGlyph(JNIEnv *env, jobject obj, jlong rasterizerHandle, jobject glyph,
    jint lineRadius, jint lineCap, jint lineJoin, jint miterLimit)
{
    GlyphRasterizer *glyphRasterizer = reinterpret_cast<GlyphRasterizer *>(rasterizerHandle);
    FT_Fixed strokeRadius = static_cast<FT_Fixed >(lineRadius);
    FT_Stroker_LineCap strokeCap = static_cast<FT_Stroker_LineCap>(lineCap);
    FT_Stroker_LineJoin strokeJoin = static_cast<FT_Stroker_LineJoin>(lineJoin);
    FT_Fixed strokeMiter = static_cast<FT_Fixed>(miterLimit);

    return glyphRasterizer->strokeGlyph(JavaBridge(env), glyph, strokeRadius,
                                        strokeCap, strokeJoin, strokeMiter);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "(JIIIIII)J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nLoadBitmap", "(JLcom/mta/tehreer/graphics/Glyph;)V", (void *)loadBitmap },
    { "nLoadOutline", "(JLcom/mta/tehreer/graphics/Glyph;)V", (void *)loadOutline },
    { "nLoadPath", "(JLcom/mta/tehreer/graphics/Glyph;)V", (void *)loadPath },
    { "nStrokeGlyph", "(JLcom/mta/tehreer/graphics/Glyph;IIII)Lcom/mta/tehreer/graphics/Glyph;", (void *)strokeGlyph },
};

jint register_com_mta_tehreer_graphics_GlyphRasterizer(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/GlyphRasterizer", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
