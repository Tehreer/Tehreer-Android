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

#include <cstdint>
#include <cstring>
#include <jni.h>
#include <vector>

#include <Tehreer/TRGlyphImage.h>
#include <Tehreer/TRPath.h>
#include <Tehreer/TRRenderer.h>

#include "JavaBridge.h"
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

struct PathContext {
    JavaBridge bridge;
    jobject path;
};

TRPathCallbacks makePathCallbacks()
{
    TRPathCallbacks callbacks = {};
    callbacks.moveTo = [](void *user, TRFloat x, TRFloat y) {
        auto context = static_cast<PathContext *>(user);
        context->bridge.Path_moveTo(context->path, x, y);
    };
    callbacks.lineTo = [](void *user, TRFloat x, TRFloat y) {
        auto context = static_cast<PathContext *>(user);
        context->bridge.Path_lineTo(context->path, x, y);
    };
    callbacks.quadTo = [](void *user, TRFloat controlX, TRFloat controlY, TRFloat x, TRFloat y) {
        auto context = static_cast<PathContext *>(user);
        context->bridge.Path_quadTo(context->path, controlX, controlY, x, y);
    };
    callbacks.cubicTo = [](void *user, TRFloat control1X, TRFloat control1Y,
                           TRFloat control2X, TRFloat control2Y, TRFloat x, TRFloat y) {
        auto context = static_cast<PathContext *>(user);
        context->bridge.Path_cubicTo(context->path, control1X, control1Y,
                                     control2X, control2Y, x, y);
    };
    callbacks.close = [](void *user) {
        auto context = static_cast<PathContext *>(user);
        context->bridge.Path_close(context->path);
    };

    return callbacks;
}

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

static jlong create(JNIEnv *env, jobject obj)
{
    return reinterpret_cast<jlong>(TRRendererCreate());
}

static void dispose(JNIEnv *env, jobject obj, jlong handle)
{
    TRRendererRelease(toRenderer(handle));
}

static void setTypeface(JNIEnv *env, jobject obj, jlong handle, jobject jtypeface)
{
    TRTypefaceRef typeface = nullptr;

    if (jtypeface) {
        jlong typefaceHandle = JavaBridge(env).Typeface_getNativeTypeface(jtypeface);
        typeface = reinterpret_cast<Typeface *>(typefaceHandle)->core();
    }

    TRRendererSetTypeface(toRenderer(handle), typeface);
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
    TRRendererSetStrokeWidth(toRenderer(handle), strokeWidth);
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
    JavaBridge bridge(env);
    PathContext context = { bridge, bridge.Path_construct() };

    TRPathRef corePath = TRRendererGetGlyphPath(toRenderer(handle), static_cast<TRGlyphID>(glyphId));
    if (corePath) {
        TRPathCallbacks callbacks = makePathCallbacks();
        TRPathEnumerate(corePath, nullptr, &callbacks, &context);
        TRPathRelease(corePath);
    }

    return context.path;
}

static jobject getRunPath(JNIEnv *env, jobject obj, jlong handle, jintArray glyphIds,
    jfloatArray offsets, jfloatArray advances, jint count)
{
    JavaBridge bridge(env);
    PathContext context = { bridge, bridge.Path_construct() };
    RunData run(env, glyphIds, offsets, advances, count);

    TRPathCallbacks callbacks = makePathCallbacks();
    TRRendererEnumerateGlyphPaths(toRenderer(handle), run.glyphIds.data(), run.offsets.data(),
                                  run.advances.data(), static_cast<TRUInteger>(count),
                                  &callbacks, &context);

    return context.path;
}

static jboolean getGlyphBoundingBox(JNIEnv *env, jobject obj, jlong handle, jint glyphId, jfloatArray box)
{
    TRRect rect = TRRendererGetGlyphBoundingBox(toRenderer(handle), static_cast<TRGlyphID>(glyphId));
    jfloat values[4] = { rect.origin.x, rect.origin.y,
                         rect.origin.x + rect.size.width, rect.origin.y + rect.size.height };

    env->SetFloatArrayRegion(box, 0, 4, values);

    return (rect.size.width > 0.0f || rect.size.height > 0.0f) ? JNI_TRUE : JNI_FALSE;
}

static jboolean getRunBoundingBox(JNIEnv *env, jobject obj, jlong handle, jintArray glyphIds,
    jfloatArray offsets, jfloatArray advances, jint count, jfloatArray box)
{
    RunData run(env, glyphIds, offsets, advances, count);

    TRRect rect = TRRendererGetRunBoundingBox(toRenderer(handle), run.glyphIds.data(),
                                              run.offsets.data(), run.advances.data(),
                                              static_cast<TRUInteger>(count));
    jfloat values[4] = { rect.origin.x, rect.origin.y,
                         rect.origin.x + rect.size.width, rect.origin.y + rect.size.height };

    env->SetFloatArrayRegion(box, 0, 4, values);

    return (rect.size.width > 0.0f || rect.size.height > 0.0f) ? JNI_TRUE : JNI_FALSE;
}

/* Fills the bitmap and the position of each glyph. The positions are in pairs, in pixels. */
static void getPlacements(JNIEnv *env, jobject obj, jlong handle, jint kind, jintArray glyphIds,
    jfloatArray offsets, jfloatArray advances, jint count, jobjectArray bitmaps, jintArray positions)
{
    if (count <= 0) {
        return;
    }

    RunData run(env, glyphIds, offsets, advances, count);
    vector<TRGlyphPlacement> placements(count);

    TRRendererGetGlyphPlacements(toRenderer(handle), static_cast<TRGlyphImageKind>(kind),
                                 run.glyphIds.data(), run.offsets.data(), run.advances.data(),
                                 static_cast<TRUInteger>(count), placements.data());

    for (jint i = 0; i < count; i++) {
        if (placements[i].image) {
            jobject bitmap = getBitmap(env, placements[i].image);

            if (bitmap) {
                jint position[2] = { static_cast<jint>(placements[i].origin.x),
                                     static_cast<jint>(placements[i].origin.y) };

                env->SetObjectArrayElement(bitmaps, i, bitmap);
                env->SetIntArrayRegion(positions, i * 2, 2, position);
                env->DeleteLocalRef(bitmap);
            }
        }
    }

    TRRendererReleaseGlyphPlacements(placements.data(), static_cast<TRUInteger>(count));
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nSetTypeface", "(JLcom/mta/tehreer/graphics/Typeface;)V", (void *)setTypeface },
    { "nSetTypeSize", "(JF)V", (void *)setTypeSize },
    { "nSetScaleX", "(JF)V", (void *)setScaleX },
    { "nSetScaleY", "(JF)V", (void *)setScaleY },
    { "nSetSkewX", "(JF)V", (void *)setSkewX },
    { "nSetWritingDirection", "(JI)V", (void *)setWritingDirection },
    { "nSetForegroundColor", "(JI)V", (void *)setForegroundColor },
    { "nSetStrokeWidth", "(JF)V", (void *)setStrokeWidth },
    { "nSetStrokeCap", "(JI)V", (void *)setStrokeCap },
    { "nSetStrokeJoin", "(JI)V", (void *)setStrokeJoin },
    { "nSetStrokeMiter", "(JF)V", (void *)setStrokeMiter },
    { "nIsRenderable", "(J)Z", (void *)isRenderable },
    { "nGetGlyphPath", "(JI)Landroid/graphics/Path;", (void *)getGlyphPath },
    { "nGetRunPath", "(J[I[F[FI)Landroid/graphics/Path;", (void *)getRunPath },
    { "nGetGlyphBoundingBox", "(JI[F)Z", (void *)getGlyphBoundingBox },
    { "nGetRunBoundingBox", "(J[I[F[FI[F)Z", (void *)getRunBoundingBox },
    { "nGetPlacements", "(JI[I[F[FI[Landroid/graphics/Bitmap;[I)V", (void *)getPlacements },
};

jint register_com_mta_tehreer_graphics_Renderer(JNIEnv *env)
{
    env->GetJavaVM(&javaVM);

    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/Renderer", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
