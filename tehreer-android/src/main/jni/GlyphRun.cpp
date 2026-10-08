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

using namespace Tehreer;

static jint getCharStart(JNIEnv *env, jclass clazz, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetCodeUnitRange(toRun(handle)).index);
}

static jint getCharEnd(JNIEnv *env, jclass clazz, jlong handle)
{
    TRRange range = TRGlyphRunGetCodeUnitRange(toRun(handle));

    return static_cast<jint>(range.index + range.length);
}

static jint getStartExtraLength(JNIEnv *env, jclass clazz, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetStartExtraLength(toRun(handle)));
}

static jint getEndExtraLength(JNIEnv *env, jclass clazz, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetEndExtraLength(toRun(handle)));
}

static jint getBidiLevel(JNIEnv *env, jclass clazz, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetBidiLevel(toRun(handle)));
}

static jint getWritingDirection(JNIEnv *env, jclass clazz, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetWritingDirection(toRun(handle)));
}

static jboolean isBackward(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRGlyphRunIsBackward(toRun(handle)) ? JNI_TRUE : JNI_FALSE;
}

static jboolean hasForegroundColor(JNIEnv *env, jclass clazz, jlong handle)
{
    TRColor color;

    return TRGlyphRunGetForegroundColor(toRun(handle), &color) ? JNI_TRUE : JNI_FALSE;
}

static jint getForegroundColor(JNIEnv *env, jclass clazz, jlong handle)
{
    TRColor color = 0;

    TRGlyphRunGetForegroundColor(toRun(handle), &color);

    return static_cast<jint>(color);
}

static jfloat getTypeSize(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRGlyphRunGetTypeSize(toRun(handle));
}

static jfloat getScaleX(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRGlyphRunGetScaleX(toRun(handle));
}

static jfloat getAscent(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRGlyphRunGetAscent(toRun(handle));
}

static jfloat getDescent(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRGlyphRunGetDescent(toRun(handle));
}

static jfloat getLeading(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRGlyphRunGetLeading(toRun(handle));
}

static jfloat getOriginX(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRGlyphRunGetOrigin(toRun(handle)).x;
}

static jfloat getOriginY(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRGlyphRunGetOrigin(toRun(handle)).y;
}

static jfloat getWidth(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRGlyphRunGetWidth(toRun(handle));
}

static jfloat getHeight(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRGlyphRunGetHeight(toRun(handle));
}

static jlong getTypeface(JNIEnv *env, jclass clazz, jlong handle)
{
    return reinterpret_cast<jlong>(TRGlyphRunGetTypeface(toRun(handle)));
}

static jboolean hasReplacement(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRGlyphRunGetReplacement(toRun(handle)) ? JNI_TRUE : JNI_FALSE;
}

static jint getGlyphCount(JNIEnv *env, jclass clazz, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetGlyphCount(toRun(handle)));
}

static jint getClusterMapCount(JNIEnv *env, jclass clazz, jlong handle)
{
    return static_cast<jint>(TRGlyphRunGetClusterMapCount(toRun(handle)));
}

/* The memory belongs to the run, and lives as long as the run does. */
static jlong getGlyphIdsPtr(JNIEnv *env, jclass clazz, jlong handle)
{
    return reinterpret_cast<jlong>(TRGlyphRunGetGlyphIDsPtr(toRun(handle)));
}

static jlong getGlyphOffsetsPtr(JNIEnv *env, jclass clazz, jlong handle)
{
    return reinterpret_cast<jlong>(TRGlyphRunGetGlyphOffsetsPtr(toRun(handle)));
}

static jlong getGlyphAdvancesPtr(JNIEnv *env, jclass clazz, jlong handle)
{
    return reinterpret_cast<jlong>(TRGlyphRunGetGlyphAdvancesPtr(toRun(handle)));
}

static jlong getClusterMapPtr(JNIEnv *env, jclass clazz, jlong handle)
{
    return reinterpret_cast<jlong>(TRGlyphRunGetClusterMapPtr(toRun(handle)));
}

static jint getClusterStart(JNIEnv *env, jclass clazz, jlong handle, jint index)
{
    return static_cast<jint>(TRGlyphRunGetClusterStart(toRun(handle), static_cast<TRUInteger>(index)));
}

static jint getClusterEnd(JNIEnv *env, jclass clazz, jlong handle, jint index)
{
    return static_cast<jint>(TRGlyphRunGetClusterEnd(toRun(handle), static_cast<TRUInteger>(index)));
}

static jint getLeadingGlyphIndex(JNIEnv *env, jclass clazz, jlong handle, jint index)
{
    return static_cast<jint>(TRGlyphRunGetLeadingGlyphIndex(toRun(handle), static_cast<TRUInteger>(index)));
}

static jint getTrailingGlyphIndex(JNIEnv *env, jclass clazz, jlong handle, jint index)
{
    return static_cast<jint>(TRGlyphRunGetTrailingGlyphIndex(toRun(handle), static_cast<TRUInteger>(index)));
}

static jfloat getDistance(JNIEnv *env, jclass clazz, jlong handle, jint index)
{
    return TRGlyphRunGetDistance(toRun(handle), static_cast<TRUInteger>(index));
}

static jint getIndexOfCodeUnit(JNIEnv *env, jclass clazz, jlong handle, jfloat distance)
{
    return static_cast<jint>(TRGlyphRunGetIndexOfCodeUnit(toRun(handle), distance));
}

static jobject getBoundingBox(JNIEnv *env, jclass clazz, jlong handle, jint glyphStart,
    jint glyphEnd, jlong rendererHandle)
{
    TRRect box = TRGlyphRunGetBoundingBox(toRun(handle), makeRange(glyphStart, glyphEnd),
                                          toRenderer(rendererHandle));

    return JavaBridge(env).RectF_construct(box.origin.x, box.origin.y,
                                           box.origin.x + box.size.width,
                                           box.origin.y + box.size.height);
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
    { "nGetLeadingGlyphIndex", "(JI)I", (void *)getLeadingGlyphIndex },
    { "nGetTrailingGlyphIndex", "(JI)I", (void *)getTrailingGlyphIndex },
    { "nGetDistance", "(JI)F", (void *)getDistance },
    { "nGetIndexOfCodeUnit", "(JF)I", (void *)getIndexOfCodeUnit },
    { "nGetBoundingBox", "(JIIJ)Landroid/graphics/RectF;", (void *)getBoundingBox },
};

jint register_com_mta_tehreer_layout_GlyphRun(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/GlyphRun", JNI_METHODS,
                                     sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
