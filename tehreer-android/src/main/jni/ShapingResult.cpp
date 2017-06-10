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

void ShapingResult::sanitizeClusterMap()
{
    // HACK: Instead of creating a new array, we're going to manipulate the existing one.
    SFUInteger length = SFAlbumGetCodeunitCount(m_sfAlbum);
    SFInteger *array = (SFInteger *)(SFAlbumGetCodeunitToGlyphMapPtr(m_sfAlbum));
    SFInteger prior = -1;

    struct ClusterStack {
        SFUInteger index;
        SFInteger value;
    };

    ClusterStack *stack = new ClusterStack[length + 1];
    SFUInteger top = 0;

    // Push a minimum entry in the stack.
    stack[0].index = SFInvalidIndex;
    stack[0].value = -1;

    for (SFUInteger i = 0; i < length; i++) {
        SFInteger value = array[i];
        if (value > prior) {
            // Value is increasing. Cluster would be distinct if it succeeds the stack entry;
            // it would be continuation otherwise.
            if (value > stack[top].value) {
                // Push the current value on stack.
                top++;
                stack[top].index = i;
                stack[top].value = value;
            } else {
                array[i] = array[i - 1];
            }
        } else {
            // Search the smallest stack entry greater than or equal to the current value.
            // Start looking from the top as fewer elements would be in decreasing order.
            for (SFUInteger j = top; j > 0; j--) {
                if (stack[j - 1].value < value) {
                    // Update the value of all in-between elements.
                    for (SFUInteger k = stack[j].index; k < i; k++) {
                        array[k] = value;
                    }

                    // Pop the stack to this entry.
                    stack[j] = stack[top];
                    top = j;
                    break;
                }
            }
        }

        prior = value;
    }

    delete[] stack;
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

static jlong getCharToGlyphMapPtr(JNIEnv *env, jobject obj, jlong resultHandle)
{
    ShapingResult *shapingResult = reinterpret_cast<ShapingResult *>(resultHandle);
    SFAlbumRef baseAlbum = shapingResult->sfAlbum();
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
    { "nativeGetGlyphIdsPtr", "(J)J", (void *)getGlyphIdsPtr },
    { "nativeGetGlyphOffsetsPtr", "(J)J", (void *)getGlyphOffsetsPtr },
    { "nativeGetGlyphAdvancesPtr", "(J)J", (void *)getGlyphAdvancesPtr },
    { "nativeGetCharToGlyphMapPtr", "(J)J", (void *)getCharToGlyphMapPtr },
};

jint register_com_mta_tehreer_opentype_ShapingResult(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/opentype/ShapingResult", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
