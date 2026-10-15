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

#include <Tehreer/TRGlyphCache.h>

#include "GlyphCache.h"
#include "JavaBridge.h"

using namespace Tehreer;

static jlong create(JNIEnv *env, jclass clazz, jlong capacity)
{
    return reinterpret_cast<jlong>(TRGlyphCacheCreate(static_cast<TRUInteger>(capacity)));
}

/* The default cache lives as long as the process, so the caller gets its own reference to it. */
static jlong getDefault(JNIEnv *env, jclass clazz)
{
    TRGlyphCacheRef cache = TRGlyphCacheGetDefault();

    return reinterpret_cast<jlong>(cache ? TRGlyphCacheRetain(cache) : nullptr);
}

static jlong getCapacity(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jlong>(TRGlyphCacheGetCapacity(toGlyphCache(handle)));
}

static void setCapacity(JNIEnv *env, jobject obj, jlong handle, jlong capacity)
{
    TRGlyphCacheSetCapacity(toGlyphCache(handle), static_cast<TRUInteger>(capacity));
}

static jlong getSize(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jlong>(TRGlyphCacheGetSize(toGlyphCache(handle)));
}

static void clear(JNIEnv *env, jobject obj, jlong handle)
{
    TRGlyphCacheClear(toGlyphCache(handle));
}

static void dispose(JNIEnv *env, jclass clazz, jlong handle)
{
    TRGlyphCacheRelease(toGlyphCache(handle));
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "(J)J", (void *)create },
    { "nGetDefault", "()J", (void *)getDefault },
    { "nGetCapacity", "(J)J", (void *)getCapacity },
    { "nSetCapacity", "(JJ)V", (void *)setCapacity },
    { "nGetSize", "(J)J", (void *)getSize },
    { "nClear", "(J)V", (void *)clear },
    { "nDispose", "(J)V", (void *)dispose },
};

jint register_com_mta_tehreer_graphics_GlyphCache(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/GlyphCache", JNI_METHODS,
                                     sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
