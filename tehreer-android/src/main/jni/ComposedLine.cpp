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

#include <Tehreer/TRComposedLine.h>

#include "ComposedLine.h"
#include "FloatCollector.h"
#include "JavaBridge.h"
#include "LayoutHandles.h"

using namespace Tehreer;

static void dispose(JNIEnv *env, jclass clazz, jlong handle)
{
    TRComposedLineRelease(toLine(handle));
}

static jint getCharStart(JNIEnv *env, jclass clazz, jlong handle)
{
    return static_cast<jint>(TRComposedLineGetCodeUnitRange(toLine(handle)).index);
}

static jint getCharEnd(JNIEnv *env, jclass clazz, jlong handle)
{
    TRRange range = TRComposedLineGetCodeUnitRange(toLine(handle));

    return static_cast<jint>(range.index + range.length);
}

static jint getParagraphLevel(JNIEnv *env, jclass clazz, jlong handle)
{
    return static_cast<jint>(TRComposedLineGetParagraphLevel(toLine(handle)));
}

static jboolean isBlock(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRComposedLineIsBlock(toLine(handle)) ? JNI_TRUE : JNI_FALSE;
}

static jfloat getAscent(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRComposedLineGetAscent(toLine(handle));
}

static jfloat getDescent(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRComposedLineGetDescent(toLine(handle));
}

static jfloat getLeading(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRComposedLineGetLeading(toLine(handle));
}

static jfloat getWidth(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRComposedLineGetWidth(toLine(handle));
}

static jfloat getTrailingWhitespaceExtent(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRComposedLineGetTrailingWhitespaceExtent(toLine(handle));
}

static jfloat getOriginX(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRComposedLineGetOrigin(toLine(handle)).x;
}

static jfloat getOriginY(JNIEnv *env, jclass clazz, jlong handle)
{
    return TRComposedLineGetOrigin(toLine(handle)).y;
}

static jint getRunCount(JNIEnv *env, jclass clazz, jlong handle)
{
    return static_cast<jint>(TRComposedLineGetGlyphRunCount(toLine(handle)));
}

/* The run belongs to the line, and lives as long as the line does. */
static jlong getRun(JNIEnv *env, jclass clazz, jlong handle, jint index)
{
    return reinterpret_cast<jlong>(TRComposedLineGetGlyphRun(toLine(handle), static_cast<TRUInteger>(index)));
}

static jfloat getDistance(JNIEnv *env, jclass clazz, jlong handle, jint index)
{
    return TRComposedLineGetCodeUnitDistance(toLine(handle), static_cast<TRUInteger>(index));
}

static void putEdge(void *userData, TRFloat left, TRFloat right)
{
    auto collector = static_cast<const FloatCollector *>(userData);

    collector->add(left);
    collector->add(right);
}

/* Hands the left and the right of each part that the range takes, one after the other. */
static void enumerateEdges(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jobject collector)
{
    FloatCollector floats(env, collector);

    TRComposedLineEnumerateEdges(toLine(handle), makeRange(start, end), putEdge, &floats);
}

static jint getIndexOfCodeUnit(JNIEnv *env, jclass clazz, jlong handle, jfloat distance)
{
    return static_cast<jint>(TRComposedLineGetCodeUnitIndex(toLine(handle), distance));
}

static jfloat getPenOffset(JNIEnv *env, jclass clazz, jlong handle, jfloat flushFactor,
    jfloat flushExtent)
{
    return TRComposedLineGetPenOffset(toLine(handle), flushFactor, flushExtent);
}

static jobject getBoundingBox(JNIEnv *env, jclass clazz, jlong handle, jlong rendererHandle)
{
    TRRect box = TRComposedLineGetBoundingBox(toLine(handle), toRenderer(rendererHandle));

    return JavaBridge(env).RectF_construct(box.origin.x, box.origin.y,
                                           box.origin.x + box.size.width,
                                           box.origin.y + box.size.height);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetCharStart", "(J)I", (void *)getCharStart },
    { "nGetCharEnd", "(J)I", (void *)getCharEnd },
    { "nGetParagraphLevel", "(J)I", (void *)getParagraphLevel },
    { "nIsBlock", "(J)Z", (void *)isBlock },
    { "nGetAscent", "(J)F", (void *)getAscent },
    { "nGetDescent", "(J)F", (void *)getDescent },
    { "nGetLeading", "(J)F", (void *)getLeading },
    { "nGetWidth", "(J)F", (void *)getWidth },
    { "nGetTrailingWhitespaceExtent", "(J)F", (void *)getTrailingWhitespaceExtent },
    { "nGetOriginX", "(J)F", (void *)getOriginX },
    { "nGetOriginY", "(J)F", (void *)getOriginY },
    { "nGetRunCount", "(J)I", (void *)getRunCount },
    { "nGetRun", "(JI)J", (void *)getRun },
    { "nGetDistance", "(JI)F", (void *)getDistance },
    { "nEnumerateEdges", "(JIILcom/mta/tehreer/internal/util/FloatCollector;)V", (void *)enumerateEdges },
    { "nGetIndexOfCodeUnit", "(JF)I", (void *)getIndexOfCodeUnit },
    { "nGetPenOffset", "(JFF)F", (void *)getPenOffset },
    { "nGetBoundingBox", "(JJ)Landroid/graphics/RectF;", (void *)getBoundingBox },
};

jint register_com_mta_tehreer_layout_ComposedLine(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/ComposedLine", JNI_METHODS,
                                     sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
