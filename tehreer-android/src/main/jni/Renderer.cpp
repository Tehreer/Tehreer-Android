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

static void setTypeface(JNIEnv *env, jobject obj, jlong handle, jlong typefaceHandle)
{
    TRRendererSetTypeface(toRenderer(handle), toTypeface(typefaceHandle));
}

static jfloat getTypeSize(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jfloat>(TRRendererGetTypeSize(toRenderer(handle)));
}

static void setTypeSize(JNIEnv *env, jobject obj, jlong handle, jfloat typeSize)
{
    TRRendererSetTypeSize(toRenderer(handle), static_cast<TRFloat>(typeSize));
}

static jfloat getScaleX(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jfloat>(TRRendererGetScaleX(toRenderer(handle)));
}

static void setScaleX(JNIEnv *env, jobject obj, jlong handle, jfloat scaleX)
{
    TRRendererSetScaleX(toRenderer(handle), static_cast<TRFloat>(scaleX));
}

static jfloat getScaleY(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jfloat>(TRRendererGetScaleY(toRenderer(handle)));
}

static void setScaleY(JNIEnv *env, jobject obj, jlong handle, jfloat scaleY)
{
    TRRendererSetScaleY(toRenderer(handle), static_cast<TRFloat>(scaleY));
}

static jfloat getSkewX(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jfloat>(TRRendererGetSkewX(toRenderer(handle)));
}

static void setSkewX(JNIEnv *env, jobject obj, jlong handle, jfloat skewX)
{
    TRRendererSetSkewX(toRenderer(handle), static_cast<TRFloat>(skewX));
}

static jint getWritingDirection(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRRendererGetWritingDirection(toRenderer(handle)));
}

static void setWritingDirection(JNIEnv *env, jobject obj, jlong handle, jint writingDirection)
{
    TRRendererSetWritingDirection(toRenderer(handle), static_cast<TRWritingDirection>(writingDirection));
}

static jint getForegroundColor(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRRendererGetForegroundColor(toRenderer(handle)));
}

static void setForegroundColor(JNIEnv *env, jobject obj, jlong handle, jint foregroundColor)
{
    TRRendererSetForegroundColor(toRenderer(handle), static_cast<TRColor>(foregroundColor));
}

static jfloat getStrokeRadius(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jfloat>(TRRendererGetStrokeRadius(toRenderer(handle)));
}

static void setStrokeRadius(JNIEnv *env, jobject obj, jlong handle, jfloat strokeRadius)
{
    TRRendererSetStrokeRadius(toRenderer(handle), static_cast<TRFloat>(strokeRadius));
}

static jint getStrokeCap(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRRendererGetStrokeCap(toRenderer(handle)));
}

static void setStrokeCap(JNIEnv *env, jobject obj, jlong handle, jint strokeCap)
{
    TRRendererSetStrokeCap(toRenderer(handle), static_cast<TRStrokeCap>(strokeCap));
}

static jint getStrokeJoin(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRRendererGetStrokeJoin(toRenderer(handle)));
}

static void setStrokeJoin(JNIEnv *env, jobject obj, jlong handle, jint strokeJoin)
{
    TRRendererSetStrokeJoin(toRenderer(handle), static_cast<TRStrokeJoin>(strokeJoin));
}

static jfloat getStrokeMiter(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jfloat>(TRRendererGetStrokeMiter(toRenderer(handle)));
}

static void setStrokeMiter(JNIEnv *env, jobject obj, jlong handle, jfloat strokeMiter)
{
    TRRendererSetStrokeMiter(toRenderer(handle), static_cast<TRFloat>(strokeMiter));
}

static jint getStrokeColor(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRRendererGetStrokeColor(toRenderer(handle)));
}

static void setStrokeColor(JNIEnv *env, jobject obj, jlong handle, jint strokeColor)
{
    TRRendererSetStrokeColor(toRenderer(handle), static_cast<TRColor>(strokeColor));
}

static jfloat getShadowRadius(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jfloat>(TRRendererGetShadowRadius(toRenderer(handle)));
}

static void setShadowRadius(JNIEnv *env, jobject obj, jlong handle, jfloat shadowRadius)
{
    TRRendererSetShadowRadius(toRenderer(handle), static_cast<TRFloat>(shadowRadius));
}

static jfloat getShadowDx(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jfloat>(TRRendererGetShadowDx(toRenderer(handle)));
}

static void setShadowDx(JNIEnv *env, jobject obj, jlong handle, jfloat shadowDx)
{
    TRRendererSetShadowDx(toRenderer(handle), static_cast<TRFloat>(shadowDx));
}

static jfloat getShadowDy(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jfloat>(TRRendererGetShadowDy(toRenderer(handle)));
}

static void setShadowDy(JNIEnv *env, jobject obj, jlong handle, jfloat shadowDy)
{
    TRRendererSetShadowDy(toRenderer(handle), static_cast<TRFloat>(shadowDy));
}

static jint getShadowColor(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRRendererGetShadowColor(toRenderer(handle)));
}

static void setShadowColor(JNIEnv *env, jobject obj, jlong handle, jint shadowColor)
{
    TRRendererSetShadowColor(toRenderer(handle), static_cast<TRColor>(shadowColor));
}

static jint getDrawStyle(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRRendererGetDrawStyle(toRenderer(handle)));
}

static void setDrawStyle(JNIEnv *env, jobject obj, jlong handle, jint drawStyle)
{
    TRRendererSetDrawStyle(toRenderer(handle), static_cast<TRDrawStyle>(drawStyle));
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

static void dispose(JNIEnv *env, jclass clazz, jlong handle)
{
    TRRendererRelease(toRenderer(handle));
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
        jint saveCount = 0;

        /* The calls into Java are costly, so only the ones that are needed are made. */
        if (clip) {
            saveCount = bridge.Canvas_save(drawing->canvas);
            bridge.Canvas_clipRect(drawing->canvas, clip->origin.x, clip->origin.y,
                                   clip->origin.x + clip->size.width,
                                   clip->origin.y + clip->size.height);
        }

        drawing->setColor(bridge, color);

        if (scaleX != 1.0f || scaleY != 1.0f) {
            bridge.Canvas_drawScaledBitmap(drawing->canvas, bitmap, origin.x, origin.y, scaleX,
                                           scaleY, drawing->paint);
        } else {
            bridge.Canvas_drawBitmap(drawing->canvas, bitmap, origin.x, origin.y, drawing->paint);
        }

        if (clip) {
            bridge.Canvas_restoreToCount(drawing->canvas, saveCount);
        }

        env->DeleteLocalRef(bitmap);
    }
}

static void fillRect(void *userData, TRRect rect, TRColor color)
{
    auto drawing = static_cast<Drawing *>(userData);
    JavaBridge bridge(drawing->env);

    drawing->setColor(bridge, color);
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

/* Applies the shadow to the paint, which draws the glyphs until the shadow ends. */
static void beginShadow(void *userData, TRFloat radius, TRFloat dx, TRFloat dy, TRColor color)
{
    auto drawing = static_cast<Drawing *>(userData);

    JavaBridge(drawing->env).Paint_setShadowLayer(drawing->paint, radius, dx, dy,
                                                  static_cast<jint>(color));
}

static void endShadow(void *userData)
{
    auto drawing = static_cast<Drawing *>(userData);

    JavaBridge(drawing->env).Paint_clearShadowLayer(drawing->paint);
}

Drawing::Drawing(JNIEnv *env, TRRendererRef renderer, jobject canvas, jobject paint, jobject drawer)
    : env(env)
    , canvas(canvas)
    , paint(paint)
    , drawer(drawer)
    , drawReplacement(nullptr)
    , m_renderer(renderer)
    , m_hasColor(false)
    , m_color(0)
{
    TRDrawCallbacks callbacks = {};
    callbacks.drawGlyphImage = ::drawGlyphImage;
    callbacks.fillRect = ::fillRect;
    callbacks.drawReplacement = ::drawReplacement;
    callbacks.beginShadow = ::beginShadow;
    callbacks.endShadow = ::endShadow;

    if (drawer) {
        jclass drawerClass = env->GetObjectClass(drawer);
        drawReplacement = env->GetMethodID(drawerClass, "drawReplacement",
                                           "(Landroid/graphics/Canvas;IFFFF)V");
        env->DeleteLocalRef(drawerClass);
    }

    TRRendererSetDrawCallbacks(m_renderer, &callbacks, this);
}

void Drawing::setColor(const JavaBridge &bridge, TRColor color)
{
    if (!m_hasColor || m_color != color) {
        bridge.Paint_setColor(paint, static_cast<jint>(color));
        m_hasColor = true;
        m_color = color;
    }
}

Drawing::~Drawing()
{
    TRRendererSetDrawCallbacks(m_renderer, nullptr, nullptr);
}


static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nSetTypeface", "(JJ)V", (void *)setTypeface },
    { "nGetTypeSize", "(J)F", (void *)getTypeSize },
    { "nSetTypeSize", "(JF)V", (void *)setTypeSize },
    { "nGetScaleX", "(J)F", (void *)getScaleX },
    { "nSetScaleX", "(JF)V", (void *)setScaleX },
    { "nGetScaleY", "(J)F", (void *)getScaleY },
    { "nSetScaleY", "(JF)V", (void *)setScaleY },
    { "nGetSkewX", "(J)F", (void *)getSkewX },
    { "nSetSkewX", "(JF)V", (void *)setSkewX },
    { "nGetWritingDirection", "(J)I", (void *)getWritingDirection },
    { "nSetWritingDirection", "(JI)V", (void *)setWritingDirection },
    { "nGetForegroundColor", "(J)I", (void *)getForegroundColor },
    { "nSetForegroundColor", "(JI)V", (void *)setForegroundColor },
    { "nGetStrokeRadius", "(J)F", (void *)getStrokeRadius },
    { "nSetStrokeRadius", "(JF)V", (void *)setStrokeRadius },
    { "nGetStrokeCap", "(J)I", (void *)getStrokeCap },
    { "nSetStrokeCap", "(JI)V", (void *)setStrokeCap },
    { "nGetStrokeJoin", "(J)I", (void *)getStrokeJoin },
    { "nSetStrokeJoin", "(JI)V", (void *)setStrokeJoin },
    { "nGetStrokeMiter", "(J)F", (void *)getStrokeMiter },
    { "nSetStrokeMiter", "(JF)V", (void *)setStrokeMiter },
    { "nGetStrokeColor", "(J)I", (void *)getStrokeColor },
    { "nSetStrokeColor", "(JI)V", (void *)setStrokeColor },
    { "nGetShadowRadius", "(J)F", (void *)getShadowRadius },
    { "nSetShadowRadius", "(JF)V", (void *)setShadowRadius },
    { "nGetShadowDx", "(J)F", (void *)getShadowDx },
    { "nSetShadowDx", "(JF)V", (void *)setShadowDx },
    { "nGetShadowDy", "(J)F", (void *)getShadowDy },
    { "nSetShadowDy", "(JF)V", (void *)setShadowDy },
    { "nGetShadowColor", "(J)I", (void *)getShadowColor },
    { "nSetShadowColor", "(JI)V", (void *)setShadowColor },
    { "nGetDrawStyle", "(J)I", (void *)getDrawStyle },
    { "nSetDrawStyle", "(JI)V", (void *)setDrawStyle },
    { "nIsRenderable", "(J)Z", (void *)isRenderable },
    { "nGetGlyphPath", "(JI)Landroid/graphics/Path;", (void *)getGlyphPath },
    { "nGetGlyphBoundingBox", "(JI)Landroid/graphics/RectF;", (void *)getGlyphBoundingBox },
    { "nGetRunBoundingBox", "(J[I[F[FI)Landroid/graphics/RectF;", (void *)getRunBoundingBox },
    { "nDrawGlyphs", "(JI[I[F[FILandroid/graphics/Canvas;Landroid/graphics/Paint;)V", (void *)drawGlyphs },
    { "nGetRunPath", "(J[I[F[FI)Landroid/graphics/Path;", (void *)getRunPath },
    { "nDispose", "(J)V", (void *)dispose },
};

jint register_com_mta_tehreer_graphics_Renderer(JNIEnv *env)
{
    env->GetJavaVM(&javaVM);

    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/Renderer", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
