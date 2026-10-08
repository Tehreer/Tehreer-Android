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

#include <Tehreer/TRAttribute.h>
#include <Tehreer/TRReplacement.h>
#include <Tehreer/TRText.h>
#include <Tehreer/TRTypesetter.h>

#include "JavaBridge.h"
#include "LayoutHandles.h"
#include "Typeface.h"
#include "Typesetter.h"

using namespace Tehreer;

static JavaVM *javaVM = nullptr;
static jmethodID holderComputeRoom = nullptr;

/* Gets the environment of the current thread, attaching the thread if it is not known yet. */
class ScopedEnv {
public:
    ScopedEnv() : m_env(nullptr), m_isAttached(false) {
        if (javaVM->GetEnv(reinterpret_cast<void **>(&m_env), JNI_VERSION_1_6) == JNI_EDETACHED) {
            if (javaVM->AttachCurrentThread(&m_env, nullptr) == JNI_OK) {
                m_isAttached = true;
            }
        }
    }

    ~ScopedEnv() {
        if (m_isAttached) {
            javaVM->DetachCurrentThread();
        }
    }

    JNIEnv *env() const { return m_env; }

private:
    JNIEnv *m_env;
    bool m_isAttached;
};

/* The replacement of Core asks the holder of its span for the room when it is needed. */
static void computeRoom(void *userData, TRFloat layoutWidth, TRReplacementRoom *room)
{
    ScopedEnv scoped;
    JNIEnv *env = scoped.env();
    auto holder = static_cast<jobject>(userData);

    room->ascent = 0.0f;
    room->descent = 0.0f;
    room->extent = 0.0f;

    if (!env) {
        return;
    }

    jfloatArray result = env->NewFloatArray(3);
    env->CallVoidMethod(holder, holderComputeRoom, layoutWidth, result);

    if (env->ExceptionCheck()) {
        env->ExceptionClear();
    } else {
        jfloat values[3];
        env->GetFloatArrayRegion(result, 0, 3, values);

        room->ascent = values[0];
        room->descent = values[1];
        room->extent = values[2];
    }

    env->DeleteLocalRef(result);
}

static void finalizeReplacement(void *userData)
{
    ScopedEnv scoped;

    if (scoped.env()) {
        scoped.env()->DeleteGlobalRef(static_cast<jobject>(userData));
    }
}

// MARK: Typesetter Input

static TRMutableTextRef toText(jlong handle)
{
    return reinterpret_cast<TRMutableTextRef>(handle);
}

static void setAttribute(jlong handle, jint start, jint end, const TRAttribute &attribute)
{
    TRTextSetAttribute(toText(handle), static_cast<TRUInteger>(start),
                       static_cast<TRUInteger>(end - start), &attribute);
}

/* Creates a text that is open for editing, so that its attributes can be set one by one. */
static jlong createText(JNIEnv *env, jclass clazz, jstring jtext)
{
    jsize length = env->GetStringLength(jtext);
    const jchar *chars = env->GetStringChars(jtext, nullptr);

    TRMutableTextRef text = TRTextCreateMutable(TRStringEncodingUTF16);
    if (text) {
        TRTextBeginEditing(text);
        TRTextAppendCodeUnits(text, chars, static_cast<TRUInteger>(length));
    }

    env->ReleaseStringChars(jtext, chars);

    return reinterpret_cast<jlong>(text);
}

static void disposeText(JNIEnv *env, jclass clazz, jlong handle)
{
    TRTextRelease(toText(handle));
}

static void setTypeface(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jlong typefaceHandle)
{
    TRAttribute attribute = TRAttribute();
    attribute.type = TRAttributeTypeface;
    attribute.value.typeface = toTypeface(typefaceHandle);
    setAttribute(handle, start, end, attribute);
}

static void setTypeSize(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end, jfloat value)
{
    TRAttribute attribute = TRAttribute();
    attribute.type = TRAttributePointSize;
    attribute.value.pointSize = value;
    setAttribute(handle, start, end, attribute);
}

static void setScaleX(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end, jfloat value)
{
    TRAttribute attribute = TRAttribute();
    attribute.type = TRAttributeScaleX;
    attribute.value.scaleX = value;
    setAttribute(handle, start, end, attribute);
}

static void setBaselineOffset(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jfloat value)
{
    TRAttribute attribute = TRAttribute();
    attribute.type = TRAttributeBaselineOffset;
    attribute.value.baselineOffset = value;
    setAttribute(handle, start, end, attribute);
}

static void setForegroundColor(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jint value)
{
    TRAttribute attribute = TRAttribute();
    attribute.type = TRAttributeForegroundColor;
    attribute.value.foregroundColor = static_cast<TRColor>(value);
    setAttribute(handle, start, end, attribute);
}

/* The holder decides the room of the replacement, and has to stay alive as long as the text. */
static jlong createReplacement(JNIEnv *env, jclass clazz, jobject holder, jboolean isBlock)
{
    TRReplacementCallbacks callbacks = {};
    callbacks.computeRoom = computeRoom;
    callbacks.finalize = finalizeReplacement;

    jobject global = env->NewGlobalRef(holder);
    TRReplacementRef replacement = TRReplacementCreate(&callbacks, global,
        isBlock ? TRReplacementKindBlock : TRReplacementKindInline);

    if (!replacement) {
        env->DeleteGlobalRef(global);
    }

    return reinterpret_cast<jlong>(replacement);
}

static void setReplacement(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jlong replacementHandle)
{
    TRAttribute attribute = TRAttribute();
    attribute.type = TRAttributeReplacement;
    attribute.value.replacement = reinterpret_cast<TRReplacementRef>(replacementHandle);
    setAttribute(handle, start, end, attribute);
}

static void releaseReplacement(JNIEnv *env, jclass clazz, jlong replacementHandle)
{
    TRReplacementRelease(reinterpret_cast<TRReplacementRef>(replacementHandle));
}

static void setTextAlignment(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jint value)
{
    TRAttribute attribute = TRAttribute();
    attribute.type = TRAttributeTextAlignment;
    attribute.value.textAlignment = static_cast<TRTextAlignment>(value);
    setAttribute(handle, start, end, attribute);
}

static void setFirstLineHeadIndent(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jfloat value)
{
    TRAttribute attribute = TRAttribute();
    attribute.type = TRAttributeFirstLineHeadIndent;
    attribute.value.firstLineHeadIndent = value;
    setAttribute(handle, start, end, attribute);
}

static void setHeadIndent(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jfloat value)
{
    TRAttribute attribute = TRAttribute();
    attribute.type = TRAttributeHeadIndent;
    attribute.value.headIndent = value;
    setAttribute(handle, start, end, attribute);
}

static void setFirstIndentLineCount(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jint value)
{
    TRAttribute attribute = TRAttribute();
    attribute.type = TRAttributeFirstIndentLineCount;
    attribute.value.firstIndentLineCount = static_cast<TRUInteger>(value);
    setAttribute(handle, start, end, attribute);
}

/* Closes the text, and makes a typesetter of it. The text is released in any case. */
static jlong createTypesetter(JNIEnv *env, jclass clazz, jlong handle)
{
    TRMutableTextRef text = toText(handle);

    TRTextEndEditing(text);
    TRTypesetterRef typesetter = TRTypesetterCreate(text, nullptr, 0);
    TRTextRelease(text);

    return reinterpret_cast<jlong>(typesetter);
}

static JNINativeMethod INPUT_METHODS[] = {
    { "nCreateText", "(Ljava/lang/String;)J", (void *)createText },
    { "nDisposeText", "(J)V", (void *)disposeText },
    { "nSetTypeface", "(JIIJ)V", (void *)setTypeface },
    { "nSetTypeSize", "(JIIF)V", (void *)setTypeSize },
    { "nSetScaleX", "(JIIF)V", (void *)setScaleX },
    { "nSetBaselineOffset", "(JIIF)V", (void *)setBaselineOffset },
    { "nSetForegroundColor", "(JIII)V", (void *)setForegroundColor },
    { "nCreateReplacement", "(Ljava/lang/Object;Z)J", (void *)createReplacement },
    { "nSetReplacement", "(JIIJ)V", (void *)setReplacement },
    { "nReleaseReplacement", "(J)V", (void *)releaseReplacement },
    { "nSetTextAlignment", "(JIII)V", (void *)setTextAlignment },
    { "nSetFirstLineHeadIndent", "(JIIF)V", (void *)setFirstLineHeadIndent },
    { "nSetHeadIndent", "(JIIF)V", (void *)setHeadIndent },
    { "nSetFirstIndentLineCount", "(JIII)V", (void *)setFirstIndentLineCount },
    { "nCreateTypesetter", "(J)J", (void *)createTypesetter },
};

jint register_com_mta_tehreer_layout_TypesetterInput(JNIEnv *env)
{
    env->GetJavaVM(&javaVM);

    jclass holder = env->FindClass("com/mta/tehreer/layout/ReplacementHolder");
    if (!holder) {
        return JNI_ERR;
    }
    holderComputeRoom = env->GetMethodID(holder, "computeRoom", "(F[F)V");
    if (!holderComputeRoom) {
        return JNI_ERR;
    }

    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/TypesetterInput", INPUT_METHODS,
                                     sizeof(INPUT_METHODS) / sizeof(INPUT_METHODS[0]));
}

// MARK: Typesetter

static void disposeTypesetter(JNIEnv *env, jclass clazz, jlong handle)
{
    TRTypesetterRelease(toTypesetter(handle));
}

static jint suggestForwardBreak(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode)
{
    return static_cast<jint>(TRTypesetterSuggestForwardBreak(toTypesetter(handle),
        makeRange(start, end), extent, static_cast<TRBreakMode>(breakMode)));
}

static jint suggestBackwardBreak(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode)
{
    return static_cast<jint>(TRTypesetterSuggestBackwardBreak(toTypesetter(handle),
        makeRange(start, end), extent, static_cast<TRBreakMode>(breakMode)));
}

static jlong createSimpleLine(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateSimpleLine(toTypesetter(handle), makeRange(start, end)));
}

static jlong createTruncationToken(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jint place, jstring token)
{
    const jchar *chars = nullptr;
    jsize length = 0;

    if (token) {
        length = env->GetStringLength(token);
        chars = env->GetStringChars(token, nullptr);
    }

    TRComposedLineRef line = TRTypesetterCreateTruncationToken(toTypesetter(handle),
        makeRange(start, end), static_cast<TRTruncationPlace>(place), chars,
        static_cast<TRUInteger>(length), TRStringEncodingUTF16);

    if (chars) {
        env->ReleaseStringChars(token, chars);
    }

    return reinterpret_cast<jlong>(line);
}

static jlong createTruncatedLine(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode, jint place, jlong tokenHandle)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateTruncatedLine(toTypesetter(handle),
        makeRange(start, end), extent, static_cast<TRBreakMode>(breakMode),
        static_cast<TRTruncationPlace>(place), toLine(tokenHandle)));
}

static jlong createJustifiedLine(JNIEnv *env, jclass clazz, jlong handle, jint start, jint end,
    jfloat factor, jfloat extent)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateJustifiedLine(toTypesetter(handle),
        makeRange(start, end), factor, extent));
}

static JNINativeMethod TYPESETTER_METHODS[] = {
    { "nDispose", "(J)V", (void *)disposeTypesetter },
    { "nSuggestForwardBreak", "(JIIFI)I", (void *)suggestForwardBreak },
    { "nSuggestBackwardBreak", "(JIIFI)I", (void *)suggestBackwardBreak },
    { "nCreateSimpleLine", "(JII)J", (void *)createSimpleLine },
    { "nCreateTruncationToken", "(JIIILjava/lang/String;)J", (void *)createTruncationToken },
    { "nCreateTruncatedLine", "(JIIFIIJ)J", (void *)createTruncatedLine },
    { "nCreateJustifiedLine", "(JIIFF)J", (void *)createJustifiedLine },
};

jint register_com_mta_tehreer_layout_Typesetter(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/Typesetter", TYPESETTER_METHODS,
                                     sizeof(TYPESETTER_METHODS) / sizeof(TYPESETTER_METHODS[0]));
}
