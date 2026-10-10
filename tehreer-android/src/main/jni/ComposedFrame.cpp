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
#include <vector>

#include <Tehreer/TRComposedFrame.h>
#include <Tehreer/TRComposedLine.h>
#include <Tehreer/TRTypesetter.h>

#include "FloatCollector.h"
#include "ComposedFrame.h"
#include "JavaBridge.h"
#include "LayoutHandles.h"
#include "Renderer.h"

using namespace Tehreer;

/* The index that Core gives when there is no such line, which the public headers do not define. */
static const TRUInteger InvalidIndex = static_cast<TRUInteger>(-1);

static void disposeFrame(JNIEnv *env, jclass clazz, jlong handle)
{
    TRComposedFrameRelease(toFrame(handle));
}

static jint getFrameCharStart(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRComposedFrameGetCodeUnitStart(toFrame(handle)));
}

static jint getFrameCharEnd(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRComposedFrameGetCodeUnitEnd(toFrame(handle)));
}

static jfloat getFrameWidth(JNIEnv *env, jobject obj, jlong handle)
{
    return TRComposedFrameGetWidth(toFrame(handle));
}

static jfloat getFrameHeight(JNIEnv *env, jobject obj, jlong handle)
{
    return TRComposedFrameGetHeight(toFrame(handle));
}

static jint getFrameLineCount(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRComposedFrameGetLineCount(toFrame(handle)));
}

/* Returns a line that the caller owns, as the frame might be disposed before the line. */
static jlong getFrameLine(JNIEnv *env, jobject obj, jlong handle, jint index)
{
    TRComposedLineRef line = TRComposedFrameGetLine(toFrame(handle), static_cast<TRUInteger>(index));

    return reinterpret_cast<jlong>(TRComposedLineRetain(line));
}

static jint getLineIndexForCodeUnit(JNIEnv *env, jobject obj, jlong handle, jint index)
{
    TRUInteger lineIndex = TRComposedFrameGetIndexOfLineForCodeUnit(toFrame(handle),
                                                                    static_cast<TRUInteger>(index));

    return (lineIndex != InvalidIndex ? static_cast<jint>(lineIndex) : -1);
}

static jint getLineIndexAtPosition(JNIEnv *env, jobject obj, jlong handle, jfloat x, jfloat y)
{
    TRPoint position;
    position.x = x;
    position.y = y;

    return static_cast<jint>(TRComposedFrameGetIndexOfLineAtPosition(toFrame(handle), position));
}

static void putSelectionRect(void *userData, TRRect rect, TRBoolean *stop)
{
    auto collector = static_cast<const FloatCollector *>(userData);

    collector->add(rect.origin.x);
    collector->add(rect.origin.y);
    collector->add(rect.origin.x + rect.size.width);
    collector->add(rect.origin.y + rect.size.height);
}

/* Hands the left, top, right and bottom of each rectangle of a selection, one after the other. */
static void enumerateSelection(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jobject collector)
{
    FloatCollector floats(env, collector);

    TRComposedFrameEnumerateSelection(toFrame(handle), toIndex(start), toLength(start, end),
                                      putSelectionRect, &floats);
}

static void drawFrame(JNIEnv *env, jobject obj, jlong handle, jlong rendererHandle,
    jobject canvas, jobject paint, jobject drawer, jfloat x, jfloat y)
{
    TRPoint origin;
    origin.x = x;
    origin.y = y;

    Drawing drawing(env, toRenderer(rendererHandle), canvas, paint, drawer);
    TRComposedFrameDraw(toFrame(handle), toRenderer(rendererHandle), origin);
}

static JNINativeMethod FRAME_METHODS[] = {
    { "nDispose", "(J)V", (void *)disposeFrame },
    { "nDraw", "(JJLandroid/graphics/Canvas;Landroid/graphics/Paint;Ljava/lang/Object;FF)V", (void *)drawFrame },
    { "nGetCharStart", "(J)I", (void *)getFrameCharStart },
    { "nGetCharEnd", "(J)I", (void *)getFrameCharEnd },
    { "nGetWidth", "(J)F", (void *)getFrameWidth },
    { "nGetHeight", "(J)F", (void *)getFrameHeight },
    { "nGetLineCount", "(J)I", (void *)getFrameLineCount },
    { "nGetLine", "(JI)J", (void *)getFrameLine },
    { "nGetLineIndexForCodeUnit", "(JI)I", (void *)getLineIndexForCodeUnit },
    { "nGetLineIndexAtPosition", "(JFF)I", (void *)getLineIndexAtPosition },
    { "nEnumerateSelection", "(JIILcom/mta/tehreer/internal/util/FloatCollector;)V", (void *)enumerateSelection },
};

jint register_com_mta_tehreer_layout_ComposedFrame(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/ComposedFrame", FRAME_METHODS,
                                     sizeof(FRAME_METHODS) / sizeof(FRAME_METHODS[0]));
}
