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
}

#include <jni.h>

#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "OpenTypeAlbum.h"

using namespace Tehreer;

OpenTypeAlbum::OpenTypeAlbum()
{
    m_sfAlbum = SFAlbumCreate();
    m_isBackward = false;
}

OpenTypeAlbum::~OpenTypeAlbum()
{
    SFAlbumRelease(m_sfAlbum);
}

void OpenTypeAlbum::associateText(Range textRange, bool isBackward)
{
    m_textRange = textRange;
    m_isBackward = isBackward;
}

jint OpenTypeAlbum::getCharGlyphIndex(jint charIndex) const
{
    return SFAlbumGetCodeunitToGlyphMapPtr(m_sfAlbum)[charIndex - m_textRange.start];
}

void OpenTypeAlbum::copyGlyphInfos(Range glyphRange, jfloat scaleFactor,
    jint *glyphIDBuffer, jfloat *xOffsetBuffer, jfloat *yOffsetBuffer, jfloat *advanceBuffer) const
{
    const SFGlyphID *albumGlyphIDs = SFAlbumGetGlyphIDsPtr(m_sfAlbum) + glyphRange.start;
    const SFPoint *albumOffsets = SFAlbumGetGlyphOffsetsPtr(m_sfAlbum) + glyphRange.start;
    const SFAdvance *albumAdvances = SFAlbumGetGlyphAdvancesPtr(m_sfAlbum) + glyphRange.start;

    jint glyphCount = glyphRange.length();
    for (jint i = 0; i < glyphCount; i++) {
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

void OpenTypeAlbum::copyCharGlyphIndexes(Range textRange, jint *glyphIndexBuffer) const
{
    const SFUInteger *codeunitToGlyphMap = SFAlbumGetCodeunitToGlyphMapPtr(m_sfAlbum);
    jint codeunitCount = textRange.length();

    for (jint i = 0; i < codeunitCount; i++) {
        glyphIndexBuffer[i] = codeunitToGlyphMap[textRange.start + i - m_textRange.start];
    }
}

static jlong create(JNIEnv *env, jobject obj)
{
    OpenTypeAlbum *opentypeAlbum = new OpenTypeAlbum();
    return reinterpret_cast<jlong>(opentypeAlbum);
}

static void dispose(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    delete opentypeAlbum;
}

static jint isBackward(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    return opentypeAlbum->isBackward();
}

static jint getCharStart(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    return opentypeAlbum->textRange().start;
}

static jint getCharEnd(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    return opentypeAlbum->textRange().end;
}

static jint getGlyphCount(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    return opentypeAlbum->glyphCount();
}

static jint getCharGlyphIndex(JNIEnv *env, jobject obj, jlong handle, jint charIndex)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    return opentypeAlbum->getCharGlyphIndex(charIndex);
}

static jint getGlyphId(JNIEnv *env, jobject obj, jlong handle, jint glyphIndex)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    return opentypeAlbum->glyphIDAt(glyphIndex);
}

static jint getGlyphXOffset(JNIEnv *env, jobject obj, jlong handle, jint glyphIndex)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    return opentypeAlbum->glyphXOffsetAt(glyphIndex);
}

static jint getGlyphYOffset(JNIEnv *env, jobject obj, jlong handle, jint glyphIndex)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    return opentypeAlbum->glyphYOffsetAt(glyphIndex);
}

static jint getGlyphAdvance(JNIEnv *env, jobject obj, jlong handle, jint glyphIndex)
{
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    return opentypeAlbum->glyphAdvanceAt(glyphIndex);
}

void copyGlyphInfos(JNIEnv *env, jobject obj, jlong handle, jint fromIndex, jint toIndex, jfloat scaleFactor,
        jintArray glyphIDs, jfloatArray xOffsets, jfloatArray yOffsets, jfloatArray advances)
{
    void *glyphIDBuffer = env->GetPrimitiveArrayCritical(glyphIDs, nullptr);
    void *xOffsetBuffer = env->GetPrimitiveArrayCritical(xOffsets, nullptr);
    void *yOffsetBuffer = env->GetPrimitiveArrayCritical(yOffsets, nullptr);
    void *advanceBuffer = env->GetPrimitiveArrayCritical(advances, nullptr);

    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    opentypeAlbum->copyGlyphInfos(Range(fromIndex, toIndex), scaleFactor,
                                  (jint *)glyphIDBuffer,
                                  (jfloat *)xOffsetBuffer, (jfloat *)yOffsetBuffer,
                                  (jfloat *)advanceBuffer);

    env->ReleasePrimitiveArrayCritical(glyphIDs, glyphIDBuffer, 0);
    env->ReleasePrimitiveArrayCritical(xOffsets, xOffsetBuffer, 0);
    env->ReleasePrimitiveArrayCritical(yOffsets, yOffsetBuffer, 0);
    env->ReleasePrimitiveArrayCritical(advances, advanceBuffer, 0);
}

void copyCharGlyphIndexes(JNIEnv *env, jobject obj, jlong handle, jint fromIndex, jint toIndex, jintArray charGlyphIndexes)
{
    void *charGlyphIndexBuffer = env->GetPrimitiveArrayCritical(charGlyphIndexes, nullptr);

    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(handle);
    opentypeAlbum->copyCharGlyphIndexes(Range(fromIndex, toIndex), (jint *)charGlyphIndexBuffer);

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
