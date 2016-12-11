/*
 * Copyright (C) 2016 Muhammad Tayyab Akram
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

void OpenTypeAlbum::associateText(jint charStart, jint charEnd, bool isBackward)
{
    m_charStart = charStart;
    m_charEnd = charEnd;
    m_isBackward = isBackward;
}

jint OpenTypeAlbum::getCharGlyphIndex(jint charIndex) const
{
    const SFUInteger *codeunitMap = SFAlbumGetCodeunitToGlyphMapPtr(m_sfAlbum);
    SFUInteger glyphIndex = codeunitMap[charIndex - m_charStart];

    return static_cast<jint>(glyphIndex);
}

void OpenTypeAlbum::copyGlyphInfos(jint fromIndex, jint toIndex, jfloat scaleFactor,
    jint *glyphIDBuffer, jfloat *xOffsetBuffer, jfloat *yOffsetBuffer, jfloat *advanceBuffer) const
{
    const SFGlyphID *albumGlyphIDs = SFAlbumGetGlyphIDsPtr(m_sfAlbum) + fromIndex;
    const SFPoint *albumOffsets = SFAlbumGetGlyphOffsetsPtr(m_sfAlbum) + fromIndex;
    const SFInt32 *albumAdvances = SFAlbumGetGlyphAdvancesPtr(m_sfAlbum) + fromIndex;

    size_t glyphCount = static_cast<size_t >(toIndex - fromIndex);
    for (size_t i = 0; i < glyphCount; i++) {
        if (glyphIDBuffer) {
            glyphIDBuffer[i] = albumGlyphIDs[i];
        }
        if (xOffsetBuffer) {
            xOffsetBuffer[i] = albumOffsets[i].x * scaleFactor;
        }
        if (yOffsetBuffer) {
            yOffsetBuffer[i] = albumOffsets[i].y * scaleFactor;
        }
        if (advanceBuffer) {
            advanceBuffer[i] = albumAdvances[i] * scaleFactor;
        }
    }
}

void OpenTypeAlbum::copyCharGlyphIndexes(jint fromIndex, jint toIndex, jint *glyphIndexBuffer) const
{
    const SFUInteger *codeunitToGlyphMap = SFAlbumGetCodeunitToGlyphMapPtr(m_sfAlbum);
    size_t codeunitCount = static_cast<size_t>(toIndex - fromIndex);

    for (jint i = 0; i < codeunitCount; i++) {
        glyphIndexBuffer[i] = static_cast<jint>(codeunitToGlyphMap[fromIndex + i - m_charStart]);
    }
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

static jint getCharStart(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    return opentypeAlbum->charStart();
}

static jint getCharEnd(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    return opentypeAlbum->charEnd();
}

static jint getGlyphCount(JNIEnv *env, jobject obj, jlong albumHandle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    SFAlbumRef baseAlbum = opentypeAlbum->sfAlbum();
    SFUInteger glyphCount = SFAlbumGetGlyphCount(baseAlbum);

    return static_cast<jint>(glyphCount);
}

static jint getCharGlyphIndex(JNIEnv *env, jobject obj, jlong albumHandle, jint charIndex)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    return opentypeAlbum->getCharGlyphIndex(charIndex);
}

static jint getGlyphId(JNIEnv *env, jobject obj, jlong albumHandle, jint glyphIndex)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    SFAlbumRef baseAlbum = opentypeAlbum->sfAlbum();
    const SFGlyphID *glyphIDsPtr = SFAlbumGetGlyphIDsPtr(baseAlbum);

    return static_cast<jint>(glyphIDsPtr[glyphIndex]);
}

static jint getGlyphXOffset(JNIEnv *env, jobject obj, jlong albumHandle, jint glyphIndex)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    SFAlbumRef baseAlbum = opentypeAlbum->sfAlbum();
    const SFPoint *glyphOffsetsPtr = SFAlbumGetGlyphOffsetsPtr(baseAlbum);

    return static_cast<jint>(glyphOffsetsPtr[glyphIndex].x);
}

static jint getGlyphYOffset(JNIEnv *env, jobject obj, jlong albumHandle, jint glyphIndex)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    SFAlbumRef baseAlbum = opentypeAlbum->sfAlbum();
    const SFPoint *glyphOffsetsPtr = SFAlbumGetGlyphOffsetsPtr(baseAlbum);

    return static_cast<jint>(glyphOffsetsPtr[glyphIndex].y);
}

static jint getGlyphAdvance(JNIEnv *env, jobject obj, jlong albumHandle, jint glyphIndex)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    SFAlbumRef baseAlbum = opentypeAlbum->sfAlbum();
    const SFInt32 *glyphAdvancesPtr = SFAlbumGetGlyphAdvancesPtr(baseAlbum);

    return static_cast<jint>(glyphAdvancesPtr[glyphIndex]);
}

void copyGlyphInfos(JNIEnv *env, jobject obj, jlong albumHandle,
    jint fromIndex, jint toIndex, jfloat scaleFactor,
    jintArray glyphIDs, jfloatArray xOffsets, jfloatArray yOffsets, jfloatArray advances)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);

    void *glyphIDBuffer = env->GetPrimitiveArrayCritical(glyphIDs, nullptr);
    void *xOffsetBuffer = env->GetPrimitiveArrayCritical(xOffsets, nullptr);
    void *yOffsetBuffer = env->GetPrimitiveArrayCritical(yOffsets, nullptr);
    void *advanceBuffer = env->GetPrimitiveArrayCritical(advances, nullptr);

    opentypeAlbum->copyGlyphInfos(fromIndex, toIndex, scaleFactor,
                                  static_cast<jint *>(glyphIDBuffer),
                                  static_cast<jfloat *>(xOffsetBuffer),
                                  static_cast<jfloat *>(yOffsetBuffer),
                                  static_cast<jfloat *>(advanceBuffer));

    env->ReleasePrimitiveArrayCritical(advances, advanceBuffer, 0);
    env->ReleasePrimitiveArrayCritical(yOffsets, yOffsetBuffer, 0);
    env->ReleasePrimitiveArrayCritical(xOffsets, xOffsetBuffer, 0);
    env->ReleasePrimitiveArrayCritical(glyphIDs, glyphIDBuffer, 0);
}

void copyCharGlyphIndexes(JNIEnv *env, jobject obj, jlong albumHandle,
    jint fromIndex, jint toIndex, jintArray charGlyphIndexes)
{
    void *charGlyphIndexBuffer = env->GetPrimitiveArrayCritical(charGlyphIndexes, nullptr);

    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    opentypeAlbum->copyCharGlyphIndexes(fromIndex, toIndex, (jint *)charGlyphIndexBuffer);

    env->ReleasePrimitiveArrayCritical(charGlyphIndexes, charGlyphIndexBuffer, 0);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeCreate", "()J", (void *)create },
    { "nativeDispose", "(J)V", (void *)dispose },
    { "nativeIsBackward", "(J)Z", (void *)isBackward },
    { "nativeGetCharStart", "(J)I", (void *)getCharStart },
    { "nativeGetCharEnd", "(J)I", (void *)getCharEnd },
    { "nativeGetGlyphCount", "(J)I", (void *)getGlyphCount },
    { "nativeGetGlyphId", "(JI)I", (void *)getGlyphId },
    { "nativeGetGlyphXOffset", "(JI)I", (void *)getGlyphXOffset },
    { "nativeGetGlyphYOffset", "(JI)I", (void *)getGlyphYOffset },
    { "nativeGetGlyphAdvance", "(JI)I", (void *)getGlyphAdvance },
    { "nativeGetCharGlyphIndex", "(JI)I", (void *)getCharGlyphIndex },
    { "nativeCopyGlyphInfos", "(JIIF[I[F[F[F)V", (void *)copyGlyphInfos },
    { "nativeCopyCharGlyphIndexes", "(JII[I)V", (void *)copyCharGlyphIndexes },
};

jint register_com_mta_tehreer_opentype_OpenTypeAlbum(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/opentype/OpenTypeAlbum", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
