/*
 * Copyright (C) 2016-2026 Muhammad Tayyab Akram
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

#include <cstdint>
#include <cstring>
#include <jni.h>
#include <vector>

#include <Tehreer/TRGlyphImage.h>
#include <Tehreer/TRGlyphRun.h>
#include <Tehreer/TRPath.h>
#include <Tehreer/TRRenderer.h>

#include "JavaBridge.h"
#include "PathBuilder.h"
#include "Typeface.h"
#include "Renderer.h"

using namespace std;
using namespace Tehreer;

static JavaVM *javaVM = nullptr;

static TRRendererRef toRenderer(jlong handle)
{
    return reinterpret_cast<TRRendererRef>(handle);
}

/**
 * The bitmap that an image of Core is drawn from, which is kept with the image as its native data.
 */
static void destroyBitmap(void *data)
{
    auto bitmap = static_cast<jobject>(data);
    JNIEnv *env = nullptr;
    bool isAttached = false;

    if (javaVM->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) == JNI_EDETACHED) {
        if (javaVM->AttachCurrentThread(&env, nullptr) != JNI_OK) {
            return;
        }
        isAttached = true;
    }

    env->DeleteGlobalRef(bitmap);

    if (isAttached) {
        javaVM->DetachCurrentThread();
    }
}

static jobject createBitmap(JNIEnv *env, TRGlyphImageRef image)
{
    JavaBridge bridge(env);
    jint width = static_cast<jint>(TRGlyphImageGetWidth(image));
    jint height = static_cast<jint>(TRGlyphImageGetHeight(image));
    const TRUInt8 *pixels = TRGlyphImageGetPixelsPtr(image);

    if (width <= 0 || height <= 0 || !pixels) {
        return nullptr;
    }

    if (TRGlyphImageGetFormat(image) == TRGlyphImageFormatAlpha) {
        jobject bitmap = bridge.Bitmap_create(width, height, JavaBridge::BitmapConfig::Alpha8);
        bridge.Bitmap_setPixels(bitmap, pixels, static_cast<size_t>(width) * height);

        return bitmap;
    }

    /* The pixels of Core are in the order alpha, red, green and blue, while those of a bitmap are
     * red, green, blue and alpha. Both have the colors premultiplied with alpha. */
    size_t length = static_cast<size_t>(width) * height * 4;
    vector<uint8_t> converted(length);

    for (size_t i = 0; i < length; i += 4) {
        converted[i + 0] = pixels[i + 1];
        converted[i + 1] = pixels[i + 2];
        converted[i + 2] = pixels[i + 3];
        converted[i + 3] = pixels[i + 0];
    }

    jobject bitmap = bridge.Bitmap_create(width, height, JavaBridge::BitmapConfig::ARGB_8888);
    bridge.Bitmap_setPixels(bitmap, converted.data(), length);

    return bitmap;
}

/* Returns a local reference to the bitmap of an image, which is made when it is asked for first. */
static jobject getBitmap(JNIEnv *env, TRGlyphImageRef image)
{
    void *data = TRGlyphImageGetNativeData(image);
    if (data) {
        return env->NewLocalRef(static_cast<jobject>(data));
    }

    jobject bitmap = createBitmap(env, image);
    if (!bitmap) {
        return nullptr;
    }

    jobject global = env->NewGlobalRef(bitmap);
    if (!TRGlyphImageSetNativeData(image, global, destroyBitmap)) {
        /* Another thread attached its own bitmap first, which is used as it is. */
        env->DeleteGlobalRef(global);

        data = TRGlyphImageGetNativeData(image);
        if (data) {
            env->DeleteLocalRef(bitmap);
            return env->NewLocalRef(static_cast<jobject>(data));
        }
    }

    return bitmap;
}

namespace {

/** The glyphs of a run, copied from the arrays of Java into those that Core reads. */
struct RunData {
    vector<TRGlyphID> glyphIds;
    vector<TRPoint> offsets;
    vector<TRFloat> advances;

    RunData(JNIEnv *env, jintArray jglyphIds, jfloatArray joffsets, jfloatArray jadvances, jint count)
        : glyphIds(count), offsets(count), advances(count)
    {
        if (count == 0) {
            return;
        }

        vector<jint> ids(count);
        vector<jfloat> points(count * 2);

        env->GetIntArrayRegion(jglyphIds, 0, count, ids.data());
        env->GetFloatArrayRegion(joffsets, 0, count * 2, points.data());
        env->GetFloatArrayRegion(jadvances, 0, count, advances.data());

        for (jint i = 0; i < count; i++) {
            glyphIds[i] = static_cast<TRGlyphID>(ids[i]);
            offsets[i].x = points[i * 2];
            offsets[i].y = points[i * 2 + 1];
        }
    }
};

}

static jlong create(JNIEnv *env, jclass clazz)
{
    return reinterpret_cast<jlong>(TRRendererCreate());
}

static void dispose(JNIEnv *env, jclass clazz, jlong handle)
{
    TRRendererRelease(toRenderer(handle));
}

static void setTypeface(JNIEnv *env, jobject obj, jlong handle, jlong typefaceHandle)
{
    TRRendererSetTypeface(toRenderer(handle), toTypeface(typefaceHandle));
}

static void setTypeSize(JNIEnv *env, jobject obj, jlong handle, jfloat typeSize)
{
    TRRendererSetTypeSize(toRenderer(handle), typeSize);
}

static void setScaleX(JNIEnv *env, jobject obj, jlong handle, jfloat scaleX)
{
    TRRendererSetScaleX(toRenderer(handle), scaleX);
}

static void setScaleY(JNIEnv *env, jobject obj, jlong handle, jfloat scaleY)
{
    TRRendererSetScaleY(toRenderer(handle), scaleY);
}

static void setSkewX(JNIEnv *env, jobject obj, jlong handle, jfloat skewX)
{
    TRRendererSetSkewX(toRenderer(handle), skewX);
}

static void setWritingDirection(JNIEnv *env, jobject obj, jlong handle, jint writingDirection)
{
    TRRendererSetWritingDirection(toRenderer(handle), static_cast<TRWritingDirection>(writingDirection));
}

static void setForegroundColor(JNIEnv *env, jobject obj, jlong handle, jint color)
{
    TRRendererSetForegroundColor(toRenderer(handle), static_cast<TRColor>(color));
}

static void setStrokeWidth(JNIEnv *env, jobject obj, jlong handle, jfloat strokeWidth)
{
    TRRendererSetStrokeRadius(toRenderer(handle), strokeWidth * 0.5f);
}

static void setStrokeColor(JNIEnv *env, jobject obj, jlong handle, jint color)
{
    TRRendererSetStrokeColor(toRenderer(handle), static_cast<TRColor>(color));
}

static void setDrawStyle(JNIEnv *env, jobject obj, jlong handle, jint drawStyle)
{
    TRRendererSetDrawStyle(toRenderer(handle), static_cast<TRDrawStyle>(drawStyle));
}

static void setStrokeCap(JNIEnv *env, jobject obj, jlong handle, jint strokeCap)
{
    TRRendererSetStrokeCap(toRenderer(handle), static_cast<TRStrokeCap>(strokeCap));
}

static void setStrokeJoin(JNIEnv *env, jobject obj, jlong handle, jint strokeJoin)
{
    TRRendererSetStrokeJoin(toRenderer(handle), static_cast<TRStrokeJoin>(strokeJoin));
}

static void setStrokeMiter(JNIEnv *env, jobject obj, jlong handle, jfloat strokeMiter)
{
    TRRendererSetStrokeMiter(toRenderer(handle), strokeMiter);
}

static jboolean isRenderable(JNIEnv *env, jobject obj, jlong handle)
{
    return TRRendererIsRenderable(toRenderer(handle)) ? JNI_TRUE : JNI_FALSE;
}

static jobject getGlyphPath(JNIEnv *env, jobject obj, jlong handle, jint glyphId)
{
    PathBuilder builder(env);

    TRPathRef corePath = TRRendererCopyGlyphPath(toRenderer(handle), static_cast<TRGlyphID>(glyphId));
    if (corePath) {
        TRPathCallbacks callbacks = PathBuilder::callbacks();
        TRPathEnumerate(corePath, nullptr, &callbacks, &builder);
        TRPathRelease(corePath);
    }

    return builder.path;
}

static jobject getRunPath(JNIEnv *env, jobject obj, jlong handle, jintArray glyphIds,
    jfloatArray offsets, jfloatArray advances, jint count)
{
    PathBuilder builder(env);
    RunData run(env, glyphIds, offsets, advances, count);

    TRPathCallbacks callbacks = PathBuilder::callbacks();
    TRRendererEnumerateGlyphPaths(toRenderer(handle), run.glyphIds.data(), run.offsets.data(),
                                  run.advances.data(), static_cast<TRUInteger>(count),
                                  &callbacks, &builder);

    return builder.path;
}

/* A glyph without any image has no box, which is null. */
static jobject makeBox(JNIEnv *env, const TRRect &rect)
{
    if (rect.size.width > 0.0f || rect.size.height > 0.0f) {
        return JavaBridge(env).RectF_construct(rect.origin.x, rect.origin.y,
                                               rect.origin.x + rect.size.width,
                                               rect.origin.y + rect.size.height);
    }

    return nullptr;
}

static jobject getGlyphBoundingBox(JNIEnv *env, jobject obj, jlong handle, jint glyphId)
{
    return makeBox(env, TRRendererGetGlyphInkBox(toRenderer(handle), static_cast<TRGlyphID>(glyphId)));
}

static jobject getRunBoundingBox(JNIEnv *env, jobject obj, jlong handle, jintArray glyphIds,
    jfloatArray offsets, jfloatArray advances, jint count)
{
    RunData run(env, glyphIds, offsets, advances, count);

    return makeBox(env, TRRendererGetRunInkBox(toRenderer(handle), run.glyphIds.data(),
                                                    run.offsets.data(), run.advances.data(),
                                                    static_cast<TRUInteger>(count)));
}

struct DrawTarget {
    JNIEnv *env;
    jobject canvas;
    jobject paint;
};

/* Draws the bitmap of each glyph at its position, in pixels. */
static void drawPlacement(void *userData, TRUInteger glyphIndex, TRGlyphImageRef image,
    TRPoint origin, TRFloat scaleX, TRFloat scaleY, TRBoolean *stop)
{
    auto target = static_cast<DrawTarget *>(userData);
    JNIEnv *env = target->env;
    jobject bitmap = getBitmap(env, image);

    if (bitmap) {
        /* The images of a bitmap font are drawn at the size of their strike, so they are scaled. */
        if (scaleX != 1.0f || scaleY != 1.0f) {
            JavaBridge(env).Canvas_drawScaledBitmap(target->canvas, bitmap, origin.x, origin.y,
                                                    scaleX, scaleY, target->paint);
        } else {
            JavaBridge(env).Canvas_drawBitmap(target->canvas, bitmap, origin.x, origin.y,
                                              target->paint);
        }

        env->DeleteLocalRef(bitmap);
    }
}

static void drawGlyphs(JNIEnv *env, jobject obj, jlong handle, jint kind, jintArray glyphIds,
    jfloatArray offsets, jfloatArray advances, jint count, jobject canvas, jobject paint)
{
    if (count <= 0) {
        return;
    }

    RunData run(env, glyphIds, offsets, advances, count);
    DrawTarget target = { env, canvas, paint };

    TRRendererEnumerateGlyphPlacements(toRenderer(handle), static_cast<TRGlyphImageKind>(kind),
                                       run.glyphIds.data(), run.offsets.data(), run.advances.data(),
                                       static_cast<TRUInteger>(count), drawPlacement, &target);
}

/* ---------- Drawing ---------- */

/* Draws the bitmap of a glyph in the color of the glyph, limited to the clip if it has one. */
static void drawGlyphImage(void *userData, TRGlyphImageRef image, TRPoint origin, TRFloat scaleX,
    TRFloat scaleY, TRColor color, const TRRect *clip)
{
    auto drawing = static_cast<Drawing *>(userData);
    JNIEnv *env = drawing->env;
    jobject bitmap = getBitmap(env, image);

    if (bitmap) {
        JavaBridge bridge(env);
        jint saveCount = bridge.Canvas_save(drawing->canvas);

        if (clip) {
            bridge.Canvas_clipRect(drawing->canvas, clip->origin.x, clip->origin.y,
                                   clip->origin.x + clip->size.width,
                                   clip->origin.y + clip->size.height);
        }

        bridge.Paint_setColor(drawing->paint, static_cast<jint>(color));
        bridge.Canvas_drawScaledBitmap(drawing->canvas, bitmap, origin.x, origin.y, scaleX, scaleY,
                                       drawing->paint);
        bridge.Canvas_restoreToCount(drawing->canvas, saveCount);

        env->DeleteLocalRef(bitmap);
    }
}

static void fillRect(void *userData, TRRect rect, TRColor color)
{
    auto drawing = static_cast<Drawing *>(userData);
    JavaBridge bridge(drawing->env);

    bridge.Paint_setColor(drawing->paint, static_cast<jint>(color));
    bridge.Canvas_drawRect(drawing->canvas, rect.origin.x, rect.origin.y,
                           rect.origin.x + rect.size.width, rect.origin.y + rect.size.height,
                           drawing->paint);
}

/* Hands the replacement to the drawer, translated to the position where its run starts. */
static void drawReplacement(void *userData, const struct _TRGlyphRun *run, TRPoint origin)
{
    auto drawing = static_cast<Drawing *>(userData);

    if (drawing->drawer) {
        JavaBridge bridge(drawing->env);
        jint saveCount = bridge.Canvas_save(drawing->canvas);

        drawing->env->CallVoidMethod(drawing->drawer, drawing->drawReplacement, drawing->canvas,
            static_cast<jint>(TRGlyphRunGetCodeUnitStart(run)), origin.x, origin.y,
            TRGlyphRunGetAscent(run), TRGlyphRunGetDescent(run));

        bridge.Canvas_restoreToCount(drawing->canvas, saveCount);
    }
}

Drawing::Drawing(JNIEnv *env, TRRendererRef renderer, jobject canvas, jobject paint, jobject drawer)
    : env(env)
    , canvas(canvas)
    , paint(paint)
    , drawer(drawer)
    , drawReplacement(nullptr)
    , m_renderer(renderer)
{
    TRDrawCallbacks callbacks = {};
    callbacks.drawGlyphImage = ::drawGlyphImage;
    callbacks.fillRect = ::fillRect;
    callbacks.drawReplacement = ::drawReplacement;

    if (drawer) {
        jclass drawerClass = env->GetObjectClass(drawer);
        drawReplacement = env->GetMethodID(drawerClass, "drawReplacement",
                                           "(Landroid/graphics/Canvas;IFFFF)V");
        env->DeleteLocalRef(drawerClass);
    }

    TRRendererSetDrawCallbacks(m_renderer, &callbacks, this);
}

Drawing::~Drawing()
{
    TRRendererSetDrawCallbacks(m_renderer, nullptr, nullptr);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nSetTypeface", "(JJ)V", (void *)setTypeface },
    { "nSetTypeSize", "(JF)V", (void *)setTypeSize },
    { "nSetScaleX", "(JF)V", (void *)setScaleX },
    { "nSetScaleY", "(JF)V", (void *)setScaleY },
    { "nSetSkewX", "(JF)V", (void *)setSkewX },
    { "nSetWritingDirection", "(JI)V", (void *)setWritingDirection },
    { "nSetForegroundColor", "(JI)V", (void *)setForegroundColor },
    { "nSetStrokeWidth", "(JF)V", (void *)setStrokeWidth },
    { "nSetStrokeColor", "(JI)V", (void *)setStrokeColor },
    { "nSetDrawStyle", "(JI)V", (void *)setDrawStyle },
    { "nSetStrokeCap", "(JI)V", (void *)setStrokeCap },
    { "nSetStrokeJoin", "(JI)V", (void *)setStrokeJoin },
    { "nSetStrokeMiter", "(JF)V", (void *)setStrokeMiter },
    { "nIsRenderable", "(J)Z", (void *)isRenderable },
    { "nGetGlyphPath", "(JI)Landroid/graphics/Path;", (void *)getGlyphPath },
    { "nGetRunPath", "(J[I[F[FI)Landroid/graphics/Path;", (void *)getRunPath },
    { "nGetGlyphBoundingBox", "(JI)Landroid/graphics/RectF;", (void *)getGlyphBoundingBox },
    { "nGetRunBoundingBox", "(J[I[F[FI)Landroid/graphics/RectF;", (void *)getRunBoundingBox },
    { "nDrawGlyphs", "(JI[I[F[FILandroid/graphics/Canvas;Landroid/graphics/Paint;)V", (void *)drawGlyphs },
};

jint register_com_mta_tehreer_graphics_Renderer(JNIEnv *env)
{
    env->GetJavaVM(&javaVM);

    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/Renderer", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
