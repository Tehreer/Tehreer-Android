/*
 * Copyright (C) 2017 Muhammad Tayyab Akram
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

extern "C" {
#include <SFAlbum.h>
#include <SFBase.h>
}

#include <jni.h>

#include "JavaBridge.h"
#include "OpenTypeAlbum.h"

using namespace Tehreer;

OpenTypeAlbum::OpenTypeAlbum()
    : m_sfAlbum(SFAlbumCreate())
    , m_isBackward(false)
    , m_charStart(0)
    , m_charEnd(0)
{
}

OpenTypeAlbum::~OpenTypeAlbum()
{
    SFAlbumRelease(m_sfAlbum);
}

void OpenTypeAlbum::associateText(jint charStart, jint charEnd, bool isBackward, jfloat sizeByEm)
{
    m_charStart = charStart;
    m_charEnd = charEnd;
    m_isBackward = isBackward;
    m_sizeByEm = sizeByEm;
}

static jlong create(JNIEnv *env, jobject obj)
{
    OpenTypeAlbum *opentypeAlbum = new OpenTypeAlbum();
    return reinterpret_cast<jlong>(opentypeAlbum);
}

static void dispose(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    delete opentypeAlbum;
}

static jint isBackward(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    return opentypeAlbum->isBackward();
}

static jfloat getSizeByEm(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    jfloat sizeByEm = opentypeAlbum->sizeByEm();

    return sizeByEm;
}

static jint getCharStart(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    jint charStart = opentypeAlbum->charStart();

    return charStart;
}

static jint getCharEnd(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    jint charEnd = opentypeAlbum->charEnd();

    return charEnd;
}

static jint getCharCount(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    jint charCount = opentypeAlbum->charEnd() - opentypeAlbum->charStart();

    return charCount;
}

static jint getGlyphCount(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    SFAlbumRef baseAlbum = opentypeAlbum->sfAlbum();
    SFUInteger glyphCount = SFAlbumGetGlyphCount(baseAlbum);

    return static_cast<jint>(glyphCount);
}

static jlong getGlyphCodesPtr(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    SFAlbumRef baseAlbum = opentypeAlbum->sfAlbum();
    const SFGlyphID *glyphIDsPtr = SFAlbumGetGlyphIDsPtr(baseAlbum);

    return reinterpret_cast<jlong>(glyphIDsPtr);
}

static jlong getGlyphOffsetsPtr(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    SFAlbumRef baseAlbum = opentypeAlbum->sfAlbum();
    const SFPoint *glyphOffsetsPtr = SFAlbumGetGlyphOffsetsPtr(baseAlbum);

    return reinterpret_cast<jlong>(glyphOffsetsPtr);
}

static jlong getGlyphAdvancesPtr(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    SFAlbumRef baseAlbum = opentypeAlbum->sfAlbum();
    const SFInt32 *glyphAdvancesPtr = SFAlbumGetGlyphAdvancesPtr(baseAlbum);

    return reinterpret_cast<jlong>(glyphAdvancesPtr);
}

static jlong getCharToGlyphMapPtr(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    SFAlbumRef baseAlbum = opentypeAlbum->sfAlbum();
    const SFUInteger *charToGlyphMapPtr = SFAlbumGetCodeunitToGlyphMapPtr(baseAlbum);

    return reinterpret_cast<jlong>(charToGlyphMapPtr);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeCreate", "()J", (void *)create },
    { "nativeDispose", "(J)V", (void *)dispose },
    { "nativeIsBackward", "(J)Z", (void *)isBackward },
    { "nativeGetSizeByEm", "(J)F", (void *)getSizeByEm },
    { "nativeGetCharStart", "(J)I", (void *)getCharStart },
    { "nativeGetCharEnd", "(J)I", (void *)getCharEnd },
    { "nativeGetCharCount", "(J)I", (void *)getCharCount },
    { "nativeGetGlyphCount", "(J)I", (void *)getGlyphCount },
    { "nativeGetGlyphCodesPtr", "(J)J", (void *)getGlyphCodesPtr },
    { "nativeGetGlyphOffsetsPtr", "(J)J", (void *)getGlyphOffsetsPtr },
    { "nativeGetGlyphAdvancesPtr", "(J)J", (void *)getGlyphAdvancesPtr },
    { "nativeGetCharToGlyphMapPtr", "(J)J", (void *)getCharToGlyphMapPtr },
};

jint register_com_mta_tehreer_opentype_OpenTypeAlbum(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/opentype/OpenTypeAlbum", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
