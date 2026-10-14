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

#include <Tehreer/TRLineLocator.h>

#include "JavaBridge.h"
#include "LayoutHandles.h"
#include "LineLocator.h"

using namespace Tehreer;

static TRLineLocatorRef toLocator(jlong handle)
{
    return reinterpret_cast<TRLineLocatorRef>(handle);
}

static jlong create(JNIEnv *env, jclass clazz, jlong frameHandle, jlong rendererHandle)
{
    return reinterpret_cast<jlong>(TRLineLocatorCreate(toFrame(frameHandle),
                                                       toRenderer(rendererHandle)));
}

static jint getLineCount(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRLineLocatorGetLineCount(toLocator(handle)));
}

static void getLineBox(JNIEnv *env, jobject obj, jlong handle, jint lineIndex, jobject outBox)
{
    TRRect box = { { 0.0f, 0.0f }, { 0.0f, 0.0f } };
    TRLineLocatorGetLineBox(toLocator(handle), static_cast<TRUInteger>(lineIndex), &box);

    JavaBridge(env).RectF_set(outBox, box.origin.x, box.origin.y, box.origin.x + box.size.width,
                              box.origin.y + box.size.height);
}

namespace {

/* The indexes go straight into the array of Java, up to its capacity, while all are counted. */
struct LineCollector {
    jint *indexes;
    jint capacity;
    jint count;
};

}

static void collectLine(void *userData, TRUInteger lineIndex, TRBoolean *stop)
{
    auto collector = static_cast<LineCollector *>(userData);

    if (collector->count < collector->capacity) {
        collector->indexes[collector->count] = static_cast<jint>(lineIndex);
    }

    collector->count += 1;
}

static jint findLines(JNIEnv *env, jobject obj, jlong handle, jfloat left, jfloat top, jfloat right,
    jfloat bottom, jintArray indexes)
{
    TRRect rect = { { left, top }, { right - left, bottom - top } };
    LineCollector collector = { nullptr, env->GetArrayLength(indexes), 0 };

    /* Nothing but the plain memory is touched while the array is held. */
    collector.indexes = static_cast<jint *>(env->GetPrimitiveArrayCritical(indexes, nullptr));
    if (!collector.indexes) {
        return 0;
    }

    TRLineLocatorEnumerateLines(toLocator(handle), rect, collectLine, &collector);
    env->ReleasePrimitiveArrayCritical(indexes, collector.indexes, 0);

    return collector.count;
}

static jint getFirstLineIndexBelow(JNIEnv *env, jobject obj, jlong handle, jfloat y)
{
    TRUInteger lineIndex = TRLineLocatorGetFirstLineIndexBelow(toLocator(handle), y);

    return (lineIndex != TRInvalidIndex ? static_cast<jint>(lineIndex) : -1);
}

static void dispose(JNIEnv *env, jclass clazz, jlong handle)
{
    TRLineLocatorRelease(toLocator(handle));
}

static JNINativeMethod LOCATOR_METHODS[] = {
    { "nCreate", "(JJ)J", (void *)create },
    { "nGetLineCount", "(J)I", (void *)getLineCount },
    { "nGetLineBox", "(JILandroid/graphics/RectF;)V", (void *)getLineBox },
    { "nFindLines", "(JFFFF[I)I", (void *)findLines },
    { "nGetFirstLineIndexBelow", "(JF)I", (void *)getFirstLineIndexBelow },
    { "nDispose", "(J)V", (void *)dispose },
};

jint register_com_mta_tehreer_layout_LineLocator(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/LineLocator", LOCATOR_METHODS,
                                     sizeof(LOCATOR_METHODS) / sizeof(LOCATOR_METHODS[0]));
}
