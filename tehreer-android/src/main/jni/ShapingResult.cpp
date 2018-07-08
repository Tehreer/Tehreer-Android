/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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
#include "ShapingResult.h"

using namespace Tehreer;

ShapingResult::ShapingResult()
    : m_sfAlbum(SFAlbumCreate())
    , m_isBackward(false)
    , m_charStart(0)
    , m_charEnd(0)
{
}

ShapingResult::~ShapingResult()
{
    SFAlbumRelease(m_sfAlbum);
}

void ShapingResult::setAdditionalInfo(jfloat sizeByEm, bool isBackward, jint charStart, jint charEnd)
{
    m_sizeByEm = sizeByEm;
    m_isBackward = isBackward;
    m_charStart = charStart;
    m_charEnd = charEnd;
}

static jlong create(JNIEnv *env, jobject obj)
{
    ShapingResult *shapingResult = new ShapingResult();
    return reinterpret_cast<jlong>(shapingResult);
}

static void dispose(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    delete shapingResult;
}

static jint isBackward(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    return shapingResult->isBackward();
}

static jfloat getSizeByEm(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    jfloat sizeByEm = shapingResult->sizeByEm();

    return sizeByEm;
}

static jint getCharStart(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    jint charStart = shapingResult->charStart();

    return charStart;
}

static jint getCharEnd(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    jint charEnd = shapingResult->charEnd();

    return charEnd;
}

static jint getCharCount(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    jint charCount = shapingResult->charEnd() - shapingResult->charStart();

    return charCount;
}

static jint getGlyphCount(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    SFAlbumRef baseAlbum = shapingResult->sfAlbum();
    SFUInteger glyphCount = SFAlbumGetGlyphCount(baseAlbum);

    return static_cast<jint>(glyphCount);
}

static jlong getGlyphIdsPtr(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    SFAlbumRef baseAlbum = shapingResult->sfAlbum();
    const SFGlyphID *glyphIDsPtr = SFAlbumGetGlyphIDsPtr(baseAlbum);

    return reinterpret_cast<jlong>(glyphIDsPtr);
}

static jlong getGlyphOffsetsPtr(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    SFAlbumRef baseAlbum = shapingResult->sfAlbum();
    const SFPoint *glyphOffsetsPtr = SFAlbumGetGlyphOffsetsPtr(baseAlbum);

    return reinterpret_cast<jlong>(glyphOffsetsPtr);
}

static jlong getGlyphAdvancesPtr(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    SFAlbumRef baseAlbum = shapingResult->sfAlbum();
    const SFInt32 *glyphAdvancesPtr = SFAlbumGetGlyphAdvancesPtr(baseAlbum);

    return reinterpret_cast<jlong>(glyphAdvancesPtr);
}

static jlong getClusterMapPtr(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    SFAlbumRef baseAlbum = shapingResult->sfAlbum();
    const SFUInteger *charToGlyphMapPtr = SFAlbumGetCodeunitToGlyphMapPtr(baseAlbum);

    return reinterpret_cast<jlong>(charToGlyphMapPtr);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nDispose", "(J)V", (void *)dispose },
    { "nIsBackward", "(J)Z", (void *)isBackward },
    { "nGetSizeByEm", "(J)F", (void *)getSizeByEm },
    { "nGetCharStart", "(J)I", (void *)getCharStart },
    { "nGetCharEnd", "(J)I", (void *)getCharEnd },
    { "nGetCharCount", "(J)I", (void *)getCharCount },
    { "nGetGlyphCount", "(J)I", (void *)getGlyphCount },
    { "nGetGlyphIdsPtr", "(J)J", (void *)getGlyphIdsPtr },
    { "nGetGlyphOffsetsPtr", "(J)J", (void *)getGlyphOffsetsPtr },
    { "nGetGlyphAdvancesPtr", "(J)J", (void *)getGlyphAdvancesPtr },
    { "nGetClusterMapPtr", "(J)J", (void *)getClusterMapPtr },
};

jint register_com_mta_tehreer_sfnt_ShapingResult(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/sfnt/ShapingResult", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
