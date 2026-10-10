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

#include <Tehreer/TRText.h>
#include <Tehreer/TRTypesetter.h>

#include "JavaBridge.h"
#include "LayoutHandles.h"
#include "Typesetter.h"

using namespace Tehreer;

/* Closes the text, and makes a typesetter of it. The text is released in any case. */
static jlong createTypesetter(JNIEnv *env, jclass clazz, jlong handle)
{
    TRMutableTextRef text = reinterpret_cast<TRMutableTextRef>(handle);

    TRTextEndEditing(text);
    TRTypesetterRef typesetter = TRTypesetterCreate(text, nullptr, 0);
    TRTextRelease(text);

    return reinterpret_cast<jlong>(typesetter);
}

static void disposeTypesetter(JNIEnv *env, jclass clazz, jlong handle)
{
    TRTypesetterRelease(toTypesetter(handle));
}

static jint suggestForwardBreak(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode)
{
    return static_cast<jint>(TRTypesetterSuggestForwardBreak(toTypesetter(handle),
        toIndex(start), toLength(start, end), extent, static_cast<TRBreakMode>(breakMode)));
}

static jint suggestBackwardBreak(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode)
{
    return static_cast<jint>(TRTypesetterSuggestBackwardBreak(toTypesetter(handle),
        toIndex(start), toLength(start, end), extent, static_cast<TRBreakMode>(breakMode)));
}

static jlong createSimpleLine(JNIEnv *env, jobject obj, jlong handle, jint start, jint end)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateSimpleLine(toTypesetter(handle), toIndex(start),
                                                                            toLength(start, end)));
}

static jlong createTruncationToken(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jint place, jstring token)
{
    const jchar *chars = nullptr;
    jsize length = 0;

    if (token) {
        length = env->GetStringLength(token);
        chars = env->GetStringChars(token, nullptr);
    }

    TRComposedLineRef line = TRTypesetterCreateTruncationToken(toTypesetter(handle),
        toIndex(start), toLength(start, end), static_cast<TRTruncationPlace>(place), chars,
        static_cast<TRUInteger>(length), TRStringEncodingUTF16);

    if (chars) {
        env->ReleaseStringChars(token, chars);
    }

    return reinterpret_cast<jlong>(line);
}

static jlong createTruncatedLine(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode, jint place, jlong tokenHandle, jobject token)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateTruncatedLine(toTypesetter(handle),
        toIndex(start), toLength(start, end), extent, static_cast<TRBreakMode>(breakMode),
        static_cast<TRTruncationPlace>(place), toLine(tokenHandle)));
}

static jlong createJustifiedLine(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat factor, jfloat extent)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateJustifiedLine(toTypesetter(handle),
        toIndex(start), toLength(start, end), factor, extent));
}

static JNINativeMethod TYPESETTER_METHODS[] = {
    { "nCreateTypesetter", "(J)J", (void *)createTypesetter },
    { "nDispose", "(J)V", (void *)disposeTypesetter },
    { "nSuggestForwardBreak", "(JIIFI)I", (void *)suggestForwardBreak },
    { "nSuggestBackwardBreak", "(JIIFI)I", (void *)suggestBackwardBreak },
    { "nCreateSimpleLine", "(JII)J", (void *)createSimpleLine },
    { "nCreateTruncationToken", "(JIIILjava/lang/String;)J", (void *)createTruncationToken },
    { "nCreateTruncatedLine", "(JIIFIIJLjava/lang/Object;)J", (void *)createTruncatedLine },
    { "nCreateJustifiedLine", "(JIIFF)J", (void *)createJustifiedLine },
};

jint register_com_mta_tehreer_layout_Typesetter(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/Typesetter", TYPESETTER_METHODS,
                                     sizeof(TYPESETTER_METHODS) / sizeof(TYPESETTER_METHODS[0]));
}
