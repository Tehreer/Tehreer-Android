/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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
#include "Glyph.h"
#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "GlyphRasterizer.h"

using namespace Tehreer;

struct PathContext {
    const JavaBridge *bridge;
    jobject path;
};

static inline jfloat toJfloat(FT_Pos pos)
{
    return (jfloat)(pos >> 6);
}

static int processMoveTo(const FT_Vector *to, void *user) {
    PathContext *context = reinterpret_cast<PathContext *>(user);
    context->bridge->Path_moveTo(context->path, toJfloat(to->x), toJfloat(to->y));
    return 0;
}

static int processLineTo(const FT_Vector *to, void *user) {
    PathContext *context = reinterpret_cast<PathContext *>(user);
    context->bridge->Path_lineTo(context->path, toJfloat(to->x), toJfloat(to->y));
    return 0;
}

static int processQuadTo(const FT_Vector *control1, const FT_Vector *to, void *user) {
    PathContext *context = reinterpret_cast<PathContext *>(user);
    context->bridge->Path_quadTo(context->path,
                                 toJfloat(control1->x), toJfloat(control1->y),
                                 toJfloat(to->x), toJfloat(to->y));
    return 0;
}

static int processCubicTo(const FT_Vector *control1, const FT_Vector *control2, const FT_Vector *to, void *user) {
    PathContext *context = reinterpret_cast<PathContext *>(user);
    context->bridge->Path_cubicTo(context->path,
                                  toJfloat(control1->x), toJfloat(control1->y),
                                  toJfloat(control2->x), toJfloat(control2->y),
                                  toJfloat(to->x), toJfloat(to->y));
    return 0;
}

GlyphRasterizer::GlyphRasterizer(Typeface &typeface, FT_F26Dot6 pixelWidth, FT_F26Dot6 pixelHeight, FT_Matrix transform)
    : m_typeface(typeface)
    , m_size(nullptr)
    , m_transform(transform)
{
    FT_Face ftFace;
    m_typeface.lockFreetypeFace(&ftFace);

    FT_New_Size(ftFace, &m_size);
    FT_Activate_Size(m_size);
    FT_Set_Char_Size(ftFace, pixelWidth, pixelHeight, 0, 0);

    m_typeface.unlockFreetypeFace();
}

GlyphRasterizer::~GlyphRasterizer()
{
    if (m_size) {
        /*
         * NOTE:
         *      Freetype face must be locked before releasing the size because it changes an
         *      internal list of the face containing all the sizes.
         */

        FT_Face ftFace;
        m_typeface.lockFreetypeFace(&ftFace);

        FT_Done_Size(m_size);

        m_typeface.unlockFreetypeFace();
    }
}

void GlyphRasterizer::unsafeActivate(FT_Face ftFace)
{
    FT_Activate_Size(m_size);
    FT_Set_Transform(ftFace, &m_transform, nullptr);
}

jobject GlyphRasterizer::unsafeCreateBitmap(const JavaBridge &bridge, const FT_Bitmap *bitmap)
{
    char pixelMode = bitmap->pixel_mode;
    jobject glyphBitmap = nullptr;
    int bitmapLength = 0;

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

void GlyphRasterizer::loadBitmap(const JavaBridge &bridge, jobject glyph)
{
    jint glyphID = bridge.Glyph_getGlyphID(glyph);
    jobject glyphBitmap = nullptr;
    jint glyphLeft = 0;
    jint glyphTop = 0;

    FT_Face ftFace;
    m_typeface.lockFreetypeFace(&ftFace);

    unsafeActivate(ftFace);

    FT_Error error = FT_Load_Glyph(ftFace, glyphID, FT_LOAD_RENDER);
    if (error == FT_Err_Ok) {
        FT_GlyphSlot slot = ftFace->glyph;
        glyphBitmap = unsafeCreateBitmap(bridge, &slot->bitmap);

        if (glyphBitmap) {
            glyphLeft = slot->bitmap_left;
            glyphTop = slot->bitmap_top;
        }
    }

    m_typeface.unlockFreetypeFace();

    bridge.Glyph_ownBitmap(glyph, glyphBitmap, glyphLeft, glyphTop);
}

void GlyphRasterizer::loadOutline(const JavaBridge &bridge, jobject glyph)
{
    jint glyphID = bridge.Glyph_getGlyphID(glyph);

    FT_Face ftFace;
    m_typeface.lockFreetypeFace(&ftFace);

    unsafeActivate(ftFace);

    FT_Glyph outline = nullptr;
    FT_Error error = FT_Load_Glyph(ftFace, glyphID, FT_LOAD_NO_BITMAP);
    if (error == FT_Err_Ok) {
        FT_Get_Glyph(ftFace->glyph, &outline);
    }

    m_typeface.unlockFreetypeFace();

    bridge.Glyph_ownOutline(glyph, outline ? reinterpret_cast<jlong>(outline) : 0);
}

void GlyphRasterizer::loadPath(const JavaBridge &bridge, jobject glyph)
{
    jint glyphID = bridge.Glyph_getGlyphID(glyph);
    jobject glyphPath = nullptr;

    FT_Face ftFace;
    m_typeface.lockFreetypeFace(&ftFace);

    unsafeActivate(ftFace);

    FT_Error error = FT_Load_Glyph(ftFace, glyphID, FT_LOAD_NO_BITMAP);
    if (error == FT_Err_Ok) {
        FT_Outline_Funcs funcs;
        funcs.move_to = processMoveTo;
        funcs.line_to = processLineTo;
        funcs.conic_to = processQuadTo;
        funcs.cubic_to = processCubicTo;
        funcs.shift = 0;
        funcs.delta = 0;

        PathContext pathContext;
        pathContext.bridge = &bridge;
        pathContext.path = bridge.Path_construct();

        FT_Outline *outline = &ftFace->glyph->outline;
        error = FT_Outline_Decompose(outline, &funcs, &pathContext);
        if (error == FT_Err_Ok) {
            glyphPath = pathContext.path;
        }
    }

    m_typeface.unlockFreetypeFace();

    bridge.Glyph_ownPath(glyph, glyphPath);
}

jobject GlyphRasterizer::strokeGlyph(const JavaBridge &bridge, jobject glyphInfo, FT_Fixed lineRadius,
                                     FT_Stroker_LineCap lineCap, FT_Stroker_LineJoin lineJoin, FT_Fixed miterLimit)
{
    jint glyphID = bridge.Glyph_getGlyphID(glyphInfo);
    FT_Glyph outline = reinterpret_cast<FT_Glyph>(bridge.Glyph_getNativeOutline(glyphInfo));

    if (outline) {
        FT_Face ftFace;
        m_typeface.lockFreetypeFace(&ftFace);

        FT_Stroker stroker = m_typeface.freetypeStroker();
        FT_Stroker_Set(stroker, lineRadius, lineCap, lineJoin, miterLimit);
        FT_Error error = FT_Glyph_Stroke(&outline, stroker, 0);

        m_typeface.unlockFreetypeFace();

        if (error == FT_Err_Ok) {
            FT_Glyph_To_Bitmap(&outline, FT_RENDER_MODE_NORMAL, nullptr, 1);

            FT_BitmapGlyph output = (FT_BitmapGlyph)outline;
            jobject glyphBitmap = nullptr;
            jint glyphLeft = 0;
            jint glyphTop = 0;

            glyphBitmap = unsafeCreateBitmap(bridge, &output->bitmap);
            if (glyphBitmap) {
                glyphLeft = output->left;
                glyphTop = output->top;
            }

            jobject result = bridge.Glyph_construct(glyphID);
            bridge.Glyph_ownBitmap(result, glyphBitmap, glyphLeft, glyphTop);

            /* Dispose the stroked / bitmap glyph. */
            FT_Done_Glyph(outline);

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

    GlyphRasterizer *rasterizer = new GlyphRasterizer(*typeface, pixelWidth, pixelHeight, transform);
    return reinterpret_cast<jlong>(rasterizer);
}

static void dispose(JNIEnv *env, jobject obj, jlong handle)
{
    GlyphRasterizer *rasterizer = reinterpret_cast<GlyphRasterizer *>(handle);
    delete rasterizer;
}

static void loadBitmap(JNIEnv *env, jobject obj, jlong handle, jobject glyph)
{
    GlyphRasterizer *rasterizer = reinterpret_cast<GlyphRasterizer *>(handle);
    rasterizer->loadBitmap(JavaBridge(env), glyph);
}

static void loadOutline(JNIEnv *env, jobject obj, jlong handle, jobject glyph)
{
    GlyphRasterizer *rasterizer = reinterpret_cast<GlyphRasterizer *>(handle);
    rasterizer->loadOutline(JavaBridge(env), glyph);
}

static void loadPath(JNIEnv *env, jobject obj, jlong handle, jobject glyph)
{
    GlyphRasterizer *rasterizer = reinterpret_cast<GlyphRasterizer *>(handle);
    rasterizer->loadPath(JavaBridge(env), glyph);
}

static jobject strokeGlyph(JNIEnv *env, jobject obj, jlong handle, jobject glyph, jint lineRadius,
                           jint lineCap, jint lineJoin, jint miterLimit)
{
    GlyphRasterizer *rasterizer = reinterpret_cast<GlyphRasterizer *>(handle);
    jobject result = rasterizer->strokeGlyph(JavaBridge(env), glyph, lineRadius, (FT_Stroker_LineCap)lineCap, (FT_Stroker_LineJoin)lineJoin, miterLimit);
    return result;
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeCreate", "(JIIIIII)J", (void *)create },
    { "nativeDispose", "(J)V", (void *)dispose },
    { "nativeLoadBitmap", "(JLcom/mta/tehreer/graphics/Glyph;)V", (void *)loadBitmap },
    { "nativeLoadOutline", "(JLcom/mta/tehreer/graphics/Glyph;)V", (void *)loadOutline },
    { "nativeLoadPath", "(JLcom/mta/tehreer/graphics/Glyph;)V", (void *)loadPath },
    { "nativeStrokeGlyph", "(JLcom/mta/tehreer/graphics/Glyph;IIII)Lcom/mta/tehreer/graphics/Glyph;", (void *)strokeGlyph },
};

jint register_com_mta_tehreer_graphics_GlyphRasterizer(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/GlyphRasterizer", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
