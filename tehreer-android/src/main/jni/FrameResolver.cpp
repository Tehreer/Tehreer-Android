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
#include <Tehreer/TRFrameResolver.h>
#include <Tehreer/TRTypesetter.h>

#include "FloatCollector.h"
#include "FrameResolver.h"
#include "JavaBridge.h"
#include "LayoutHandles.h"

using namespace Tehreer;

/* The index that Core gives when there is no such line, which the public headers do not define. */
static const TRUInteger InvalidIndex = static_cast<TRUInteger>(-1);

// MARK: Frame Resolver

/*
 * Resolves a frame of a range of the text with the given properties. The place of truncation is
 * negative if the lines are not truncated.
 */
static jlong createFrame(JNIEnv *env, jclass clazz, jlong typesetterHandle, jint start, jint end,
    jfloat width, jfloat height, jboolean fitsHorizontally, jboolean fitsVertically,
    jint textAlignment, jint verticalAlignment, jint truncationMode, jint truncationPlace,
    jboolean isJustificationEnabled, jfloat justificationLevel, jint maxLines,
    jfloat extraLineSpacing, jfloat lineHeightMultiplier)
{
    TRFrameResolverRef resolver = TRFrameResolverCreate();
    if (!resolver) {
        return 0;
    }

    TRFrameResolverSetTypesetter(resolver, toTypesetter(typesetterHandle));
    TRFrameResolverSetFrameSize(resolver, width, height);
    TRFrameResolverSetFitsHorizontally(resolver, fitsHorizontally ? TRTrue : TRFalse);
    TRFrameResolverSetFitsVertically(resolver, fitsVertically ? TRTrue : TRFalse);
    TRFrameResolverSetTextAlignment(resolver, static_cast<TRTextAlignment>(textAlignment));
    TRFrameResolverSetVerticalAlignment(resolver, static_cast<TRVerticalAlignment>(verticalAlignment));
    TRFrameResolverSetTruncationMode(resolver, static_cast<TRBreakMode>(truncationMode));
    if (truncationPlace >= 0) {
        TRFrameResolverSetTruncationPlace(resolver, static_cast<TRTruncationPlace>(truncationPlace));
    } else {
        TRFrameResolverDisableTruncation(resolver);
    }
    TRFrameResolverSetJustificationEnabled(resolver, isJustificationEnabled ? TRTrue : TRFalse);
    TRFrameResolverSetJustificationLevel(resolver, justificationLevel);
    TRFrameResolverSetMaxLines(resolver, static_cast<TRUInteger>(maxLines));
    TRFrameResolverSetExtraLineSpacing(resolver, extraLineSpacing);
    TRFrameResolverSetLineHeightMultiplier(resolver, lineHeightMultiplier);

    TRRange range;
    range.index = static_cast<TRUInteger>(start);
    range.length = static_cast<TRUInteger>(end - start);

    TRComposedFrameRef frame = TRFrameResolverCreateFrame(resolver, range);
    TRFrameResolverRelease(resolver);

    return reinterpret_cast<jlong>(frame);
}

static JNINativeMethod RESOLVER_METHODS[] = {
    { "nCreateFrame", "(JIIFFZZIIIIZFIFF)J", (void *)createFrame },
};

jint register_com_mta_tehreer_layout_FrameResolver(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/FrameResolver", RESOLVER_METHODS,
                                     sizeof(RESOLVER_METHODS) / sizeof(RESOLVER_METHODS[0]));
}

// MARK: Composed Frame

static void disposeFrame(JNIEnv *env, jclass clazz, jlong handle)
{
    TRComposedFrameRelease(toFrame(handle));
}

static jint getFrameCharStart(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRComposedFrameGetCodeUnitRange(toFrame(handle)).index);
}

static jint getFrameCharEnd(JNIEnv *env, jobject obj, jlong handle)
{
    TRRange range = TRComposedFrameGetCodeUnitRange(toFrame(handle));

    return static_cast<jint>(range.index + range.length);
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

static void putSelectionRect(void *userData, TRRect rect)
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

    TRComposedFrameEnumerateSelection(toFrame(handle), makeRange(start, end), putSelectionRect,
                                      &floats);
}

static JNINativeMethod FRAME_METHODS[] = {
    { "nDispose", "(J)V", (void *)disposeFrame },
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
