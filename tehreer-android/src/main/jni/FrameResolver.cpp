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

#include <Tehreer/TRComposedFrame.h>
#include <Tehreer/TRFrameResolver.h>

#include "FrameResolver.h"
#include "JavaBridge.h"
#include "LayoutHandles.h"

using namespace Tehreer;

static TRFrameResolverRef toResolver(jlong handle)
{
    return reinterpret_cast<TRFrameResolverRef>(handle);
}

static jlong create(JNIEnv *env, jclass clazz)
{
    return reinterpret_cast<jlong>(TRFrameResolverCreate());
}

static void setTypesetter(JNIEnv *env, jobject obj, jlong handle, jlong typesetterHandle)
{
    TRFrameResolverSetTypesetter(toResolver(handle), toTypesetter(typesetterHandle));
}

static jfloat getFrameWidth(JNIEnv *env, jobject obj, jlong handle)
{
    return TRFrameResolverGetFrameSize(toResolver(handle)).width;
}

static jfloat getFrameHeight(JNIEnv *env, jobject obj, jlong handle)
{
    return TRFrameResolverGetFrameSize(toResolver(handle)).height;
}

static void setFrameSize(JNIEnv *env, jobject obj, jlong handle, jfloat width, jfloat height)
{
    TRSize size = { width, height };
    TRFrameResolverSetFrameSize(toResolver(handle), size);
}

static jboolean getFitsHorizontally(JNIEnv *env, jobject obj, jlong handle)
{
    return TRFrameResolverGetFitsHorizontally(toResolver(handle)) ? JNI_TRUE : JNI_FALSE;
}

static void setFitsHorizontally(JNIEnv *env, jobject obj, jlong handle, jboolean fits)
{
    TRFrameResolverSetFitsHorizontally(toResolver(handle), fits ? TRTrue : TRFalse);
}

static jboolean getFitsVertically(JNIEnv *env, jobject obj, jlong handle)
{
    return TRFrameResolverGetFitsVertically(toResolver(handle)) ? JNI_TRUE : JNI_FALSE;
}

static void setFitsVertically(JNIEnv *env, jobject obj, jlong handle, jboolean fits)
{
    TRFrameResolverSetFitsVertically(toResolver(handle), fits ? TRTrue : TRFalse);
}

static jint getTextAlignment(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRFrameResolverGetTextAlignment(toResolver(handle)));
}

static void setTextAlignment(JNIEnv *env, jobject obj, jlong handle, jint textAlignment)
{
    TRFrameResolverSetTextAlignment(toResolver(handle), static_cast<TRTextAlignment>(textAlignment));
}

static jint getVerticalAlignment(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRFrameResolverGetVerticalAlignment(toResolver(handle)));
}

static void setVerticalAlignment(JNIEnv *env, jobject obj, jlong handle, jint verticalAlignment)
{
    TRFrameResolverSetVerticalAlignment(toResolver(handle),
                                        static_cast<TRVerticalAlignment>(verticalAlignment));
}

static jint getTruncationMode(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRFrameResolverGetTruncationMode(toResolver(handle)));
}

static void setTruncationMode(JNIEnv *env, jobject obj, jlong handle, jint truncationMode)
{
    TRFrameResolverSetTruncationMode(toResolver(handle), static_cast<TRBreakMode>(truncationMode));
}

static jint getTruncationPlace(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRFrameResolverGetTruncationPlace(toResolver(handle)));
}

static void setTruncationPlace(JNIEnv *env, jobject obj, jlong handle, jint truncationPlace)
{
    TRFrameResolverSetTruncationPlace(toResolver(handle),
                                      static_cast<TRTruncationPlace>(truncationPlace));
}

static jboolean isTruncationEnabled(JNIEnv *env, jobject obj, jlong handle)
{
    return TRFrameResolverIsTruncationEnabled(toResolver(handle)) ? JNI_TRUE : JNI_FALSE;
}

static void setTruncationEnabled(JNIEnv *env, jobject obj, jlong handle, jboolean isEnabled)
{
    TRFrameResolverSetTruncationEnabled(toResolver(handle), isEnabled ? TRTrue : TRFalse);
}

static jboolean isJustificationEnabled(JNIEnv *env, jobject obj, jlong handle)
{
    return TRFrameResolverIsJustificationEnabled(toResolver(handle)) ? JNI_TRUE : JNI_FALSE;
}

static void setJustificationEnabled(JNIEnv *env, jobject obj, jlong handle, jboolean isEnabled)
{
    TRFrameResolverSetJustificationEnabled(toResolver(handle), isEnabled ? TRTrue : TRFalse);
}

static jfloat getJustificationLevel(JNIEnv *env, jobject obj, jlong handle)
{
    return TRFrameResolverGetJustificationLevel(toResolver(handle));
}

static void setJustificationLevel(JNIEnv *env, jobject obj, jlong handle, jfloat level)
{
    TRFrameResolverSetJustificationLevel(toResolver(handle), level);
}

static jint getMaxLines(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRFrameResolverGetMaxLines(toResolver(handle)));
}

static void setMaxLines(JNIEnv *env, jobject obj, jlong handle, jint maxLines)
{
    TRFrameResolverSetMaxLines(toResolver(handle), static_cast<TRUInteger>(maxLines));
}

static jfloat getExtraLineSpacing(JNIEnv *env, jobject obj, jlong handle)
{
    return TRFrameResolverGetExtraLineSpacing(toResolver(handle));
}

static void setExtraLineSpacing(JNIEnv *env, jobject obj, jlong handle, jfloat spacing)
{
    TRFrameResolverSetExtraLineSpacing(toResolver(handle), spacing);
}

static jfloat getLineHeightMultiplier(JNIEnv *env, jobject obj, jlong handle)
{
    return TRFrameResolverGetLineHeightMultiplier(toResolver(handle));
}

static void setLineHeightMultiplier(JNIEnv *env, jobject obj, jlong handle, jfloat multiplier)
{
    TRFrameResolverSetLineHeightMultiplier(toResolver(handle), multiplier);
}

static jlong createFrame(JNIEnv *env, jobject obj, jlong handle, jint start, jint end)
{
    return reinterpret_cast<jlong>(TRFrameResolverCreateFrame(toResolver(handle), toIndex(start),
                                                              toLength(start, end)));
}

static void dispose(JNIEnv *env, jclass clazz, jlong handle)
{
    TRFrameResolverRelease(toResolver(handle));
}

static JNINativeMethod RESOLVER_METHODS[] = {
    { "nCreate", "()J", (void *)create },
    { "nSetTypesetter", "(JJ)V", (void *)setTypesetter },
    { "nGetFrameWidth", "(J)F", (void *)getFrameWidth },
    { "nGetFrameHeight", "(J)F", (void *)getFrameHeight },
    { "nSetFrameSize", "(JFF)V", (void *)setFrameSize },
    { "nGetFitsHorizontally", "(J)Z", (void *)getFitsHorizontally },
    { "nSetFitsHorizontally", "(JZ)V", (void *)setFitsHorizontally },
    { "nGetFitsVertically", "(J)Z", (void *)getFitsVertically },
    { "nSetFitsVertically", "(JZ)V", (void *)setFitsVertically },
    { "nGetTextAlignment", "(J)I", (void *)getTextAlignment },
    { "nSetTextAlignment", "(JI)V", (void *)setTextAlignment },
    { "nGetVerticalAlignment", "(J)I", (void *)getVerticalAlignment },
    { "nSetVerticalAlignment", "(JI)V", (void *)setVerticalAlignment },
    { "nGetTruncationMode", "(J)I", (void *)getTruncationMode },
    { "nSetTruncationMode", "(JI)V", (void *)setTruncationMode },
    { "nGetTruncationPlace", "(J)I", (void *)getTruncationPlace },
    { "nSetTruncationPlace", "(JI)V", (void *)setTruncationPlace },
    { "nIsTruncationEnabled", "(J)Z", (void *)isTruncationEnabled },
    { "nSetTruncationEnabled", "(JZ)V", (void *)setTruncationEnabled },
    { "nIsJustificationEnabled", "(J)Z", (void *)isJustificationEnabled },
    { "nSetJustificationEnabled", "(JZ)V", (void *)setJustificationEnabled },
    { "nGetJustificationLevel", "(J)F", (void *)getJustificationLevel },
    { "nSetJustificationLevel", "(JF)V", (void *)setJustificationLevel },
    { "nGetMaxLines", "(J)I", (void *)getMaxLines },
    { "nSetMaxLines", "(JI)V", (void *)setMaxLines },
    { "nGetExtraLineSpacing", "(J)F", (void *)getExtraLineSpacing },
    { "nSetExtraLineSpacing", "(JF)V", (void *)setExtraLineSpacing },
    { "nGetLineHeightMultiplier", "(J)F", (void *)getLineHeightMultiplier },
    { "nSetLineHeightMultiplier", "(JF)V", (void *)setLineHeightMultiplier },
    { "nCreateFrame", "(JII)J", (void *)createFrame },
    { "nDispose", "(J)V", (void *)dispose },
};

jint register_com_mta_tehreer_layout_FrameResolver(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/FrameResolver", RESOLVER_METHODS,
                                     sizeof(RESOLVER_METHODS) / sizeof(RESOLVER_METHODS[0]));
}
