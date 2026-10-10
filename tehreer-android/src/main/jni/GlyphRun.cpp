/*
 * Copyright (C) 2026 Muhammad Tayyab Akram
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

#include <jni.h>

#include <Tehreer/TRGlyphRun.h>

#include "GlyphRun.h"
#include "JavaBridge.h"
#include "LayoutHandles.h"
#include "Renderer.h"

using namespace Tehreer;

static jint getCharStart(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetCodeUnitStart(toRun(handle)));
}

static jint getCharEnd(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetCodeUnitEnd(toRun(handle)));
}

static jint getStartExtraLength(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetStartExtraLength(toRun(handle)));
}

static jint getEndExtraLength(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetEndExtraLength(toRun(handle)));
}

static jint getBidiLevel(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetBidiLevel(toRun(handle)));
}

static jint getWritingDirection(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetWritingDirection(toRun(handle)));
}

static jboolean isBackward(JNIEnv *env, jobject obj, jlong handle)
{
    return TRGlyphRunIsBackward(toRun(handle)) ? JNI_TRUE : JNI_FALSE;
}

static jboolean hasForegroundColor(JNIEnv *env, jobject obj, jlong handle)
{
    TRColor color;

    return TRGlyphRunGetForegroundColor(toRun(handle), &color) ? JNI_TRUE : JNI_FALSE;
}

static jint getForegroundColor(JNIEnv *env, jobject obj, jlong handle)
{
    TRColor color = 0;

    TRGlyphRunGetForegroundColor(toRun(handle), &color);

    return static_cast<jint>(color);
}

static jfloat getTypeSize(JNIEnv *env, jobject obj, jlong handle)
{
    return TRGlyphRunGetTypeSize(toRun(handle));
}

static jfloat getScaleX(JNIEnv *env, jobject obj, jlong handle)
{
    return TRGlyphRunGetScaleX(toRun(handle));
}

static jfloat getAscent(JNIEnv *env, jobject obj, jlong handle)
{
    return TRGlyphRunGetAscent(toRun(handle));
}

static jfloat getDescent(JNIEnv *env, jobject obj, jlong handle)
{
    return TRGlyphRunGetDescent(toRun(handle));
}

static jfloat getLeading(JNIEnv *env, jobject obj, jlong handle)
{
    return TRGlyphRunGetLeading(toRun(handle));
}

static jfloat getOriginX(JNIEnv *env, jobject obj, jlong handle)
{
    return TRGlyphRunGetOrigin(toRun(handle)).x;
}

static jfloat getOriginY(JNIEnv *env, jobject obj, jlong handle)
{
    return TRGlyphRunGetOrigin(toRun(handle)).y;
}

static jfloat getWidth(JNIEnv *env, jobject obj, jlong handle)
{
    return TRGlyphRunGetWidth(toRun(handle));
}

static jfloat getHeight(JNIEnv *env, jobject obj, jlong handle)
{
    return TRGlyphRunGetHeight(toRun(handle));
}

static jlong getTypeface(JNIEnv *env, jobject obj, jlong handle)
{
    return reinterpret_cast<jlong>(TRGlyphRunGetTypeface(toRun(handle)));
}

static jboolean hasReplacement(JNIEnv *env, jobject obj, jlong handle)
{
    return TRGlyphRunGetReplacement(toRun(handle)) ? JNI_TRUE : JNI_FALSE;
}

static jint getGlyphCount(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetGlyphCount(toRun(handle)));
}

static jint getClusterMapCount(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetClusterMapCount(toRun(handle)));
}

/* The memory belongs to the run, and lives as long as the run does. */
static jlong getGlyphIdsPtr(JNIEnv *env, jobject obj, jlong handle)
{
    return reinterpret_cast<jlong>(TRGlyphRunGetGlyphIDsPtr(toRun(handle)));
}

static jlong getGlyphOffsetsPtr(JNIEnv *env, jobject obj, jlong handle)
{
    return reinterpret_cast<jlong>(TRGlyphRunGetGlyphOffsetsPtr(toRun(handle)));
}

static jlong getGlyphAdvancesPtr(JNIEnv *env, jobject obj, jlong handle)
{
    return reinterpret_cast<jlong>(TRGlyphRunGetGlyphAdvancesPtr(toRun(handle)));
}

static jlong getClusterMapPtr(JNIEnv *env, jobject obj, jlong handle)
{
    return reinterpret_cast<jlong>(TRGlyphRunGetClusterMapPtr(toRun(handle)));
}

static jint getClusterStart(JNIEnv *env, jobject obj, jlong handle, jint index)
{
    return static_cast<jint>(TRGlyphRunGetClusterStart(toRun(handle), static_cast<TRUInteger>(index)));
}

static jint getClusterEnd(JNIEnv *env, jobject obj, jlong handle, jint index)
{
    return static_cast<jint>(TRGlyphRunGetClusterEnd(toRun(handle), static_cast<TRUInteger>(index)));
}

static jfloat getDistance(JNIEnv *env, jobject obj, jlong handle, jint index)
{
    TRFloat distance = 0.0f;

    TRGlyphRunGetCodeUnitDistance(toRun(handle), static_cast<TRUInteger>(index), &distance);

    return distance;
}

static jint getIndexOfCodeUnit(JNIEnv *env, jobject obj, jlong handle, jfloat distance)
{
    return static_cast<jint>(TRGlyphRunGetCodeUnitIndex(toRun(handle), distance));
}

static jobject getBoundingBox(JNIEnv *env, jobject obj, jlong handle, jlong rendererHandle)
{
    TRRect box = TRGlyphRunGetInkBox(toRun(handle), toRenderer(rendererHandle));

    return JavaBridge(env).RectF_construct(box.origin.x, box.origin.y,
                                           box.origin.x + box.size.width,
                                           box.origin.y + box.size.height);
}

static void draw(JNIEnv *env, jobject obj, jlong handle, jlong rendererHandle, jobject canvas,
    jobject paint, jobject drawer, jfloat x, jfloat y)
{
    TRPoint origin;
    origin.x = x;
    origin.y = y;

    Drawing drawing(env, toRenderer(rendererHandle), canvas, paint, drawer);
    TRGlyphRunDraw(toRun(handle), toRenderer(rendererHandle), origin);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nGetCharStart", "(J)I", (void *)getCharStart },
    { "nGetCharEnd", "(J)I", (void *)getCharEnd },
    { "nGetStartExtraLength", "(J)I", (void *)getStartExtraLength },
    { "nGetEndExtraLength", "(J)I", (void *)getEndExtraLength },
    { "nGetBidiLevel", "(J)I", (void *)getBidiLevel },
    { "nGetWritingDirection", "(J)I", (void *)getWritingDirection },
    { "nIsBackward", "(J)Z", (void *)isBackward },
    { "nHasForegroundColor", "(J)Z", (void *)hasForegroundColor },
    { "nGetForegroundColor", "(J)I", (void *)getForegroundColor },
    { "nGetTypeSize", "(J)F", (void *)getTypeSize },
    { "nGetScaleX", "(J)F", (void *)getScaleX },
    { "nGetAscent", "(J)F", (void *)getAscent },
    { "nGetDescent", "(J)F", (void *)getDescent },
    { "nGetLeading", "(J)F", (void *)getLeading },
    { "nGetOriginX", "(J)F", (void *)getOriginX },
    { "nGetOriginY", "(J)F", (void *)getOriginY },
    { "nGetWidth", "(J)F", (void *)getWidth },
    { "nGetHeight", "(J)F", (void *)getHeight },
    { "nGetTypeface", "(J)J", (void *)getTypeface },
    { "nHasReplacement", "(J)Z", (void *)hasReplacement },
    { "nGetGlyphCount", "(J)I", (void *)getGlyphCount },
    { "nGetClusterMapCount", "(J)I", (void *)getClusterMapCount },
    { "nGetGlyphIdsPtr", "(J)J", (void *)getGlyphIdsPtr },
    { "nGetGlyphOffsetsPtr", "(J)J", (void *)getGlyphOffsetsPtr },
    { "nGetGlyphAdvancesPtr", "(J)J", (void *)getGlyphAdvancesPtr },
    { "nGetClusterMapPtr", "(J)J", (void *)getClusterMapPtr },
    { "nGetClusterStart", "(JI)I", (void *)getClusterStart },
    { "nGetClusterEnd", "(JI)I", (void *)getClusterEnd },
    { "nGetDistance", "(JI)F", (void *)getDistance },
    { "nGetIndexOfCodeUnit", "(JF)I", (void *)getIndexOfCodeUnit },
    { "nDraw", "(JJLandroid/graphics/Canvas;Landroid/graphics/Paint;Ljava/lang/Object;FF)V", (void *)draw },
    { "nGetBoundingBox", "(JJ)Landroid/graphics/RectF;", (void *)getBoundingBox },
};

jint register_com_mta_tehreer_layout_GlyphRun(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/GlyphRun", JNI_METHODS,
                                     sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
