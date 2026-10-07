/*
 * Copyright (C) 2016-2021 Muhammad Tayyab Akram
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

#include <cstdint>
#include <jni.h>
#include <vector>

#include <Tehreer/TRAttribute.h>
#include <Tehreer/TRComposedLine.h>
#include <Tehreer/TRGlyphRun.h>
#include <Tehreer/TRReplacement.h>
#include <Tehreer/TRText.h>
#include <Tehreer/TRTypesetter.h>

#include "JavaBridge.h"
#include "Typesetter.h"
#include "Typeface.h"

using namespace std;
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

static TRTypesetterRef toTypesetter(jlong handle)
{
    return reinterpret_cast<TRTypesetterRef>(handle);
}

static TRComposedLineRef toLine(jlong handle)
{
    return reinterpret_cast<TRComposedLineRef>(handle);
}

static TRGlyphRunRef toRun(jlong handle)
{
    return reinterpret_cast<TRGlyphRunRef>(handle);
}

static TRRange makeRange(jint start, jint end)
{
    TRRange range;
    range.index = static_cast<TRUInteger>(start);
    range.length = static_cast<TRUInteger>(end - start);

    return range;
}

// MARK: Typesetter

static void setAttribute(TRMutableTextRef text, jint start, jint end, const TRAttribute &attribute)
{
    TRTextSetAttribute(text, static_cast<TRUInteger>(start), static_cast<TRUInteger>(end - start), &attribute);
}

/*
 * Creates a typesetter for a text that is described by its runs and colors. The runs cover all of
 * the text. The typefaces are the native handles of the typefaces, and the holders are the objects
 * that decide the room of the replacements, or null for the runs that are not replacements.
 */
static jlong createTypesetter(JNIEnv *env, jobject obj, jstring jtext, jint runCount,
    jintArray runBounds, jobjectArray typefaces, jfloatArray typeSizes, jfloatArray scaleXs,
    jfloatArray baselineShifts, jobjectArray holders, jfloatArray holderLeadings,
    jbooleanArray holderBlocks, jint colorCount, jintArray colorBounds, jintArray colors)
{
    jsize length = env->GetStringLength(jtext);
    const jchar *chars = env->GetStringChars(jtext, nullptr);

    TRMutableTextRef text = TRTextCreateMutable(TRStringEncodingUTF16);
    if (!text) {
        env->ReleaseStringChars(jtext, chars);
        return 0;
    }

    TRTextBeginEditing(text);
    TRTextAppendCodeUnits(text, chars, static_cast<TRUInteger>(length));

    env->ReleaseStringChars(jtext, chars);

    vector<jint> bounds(runCount * 2);
    vector<jfloat> sizes(runCount);
    vector<jfloat> scales(runCount);
    vector<jfloat> shifts(runCount);
    vector<jfloat> leadings(runCount);
    vector<jboolean> blocks(runCount);

    if (runCount > 0) {
        env->GetIntArrayRegion(runBounds, 0, runCount * 2, bounds.data());
        env->GetFloatArrayRegion(typeSizes, 0, runCount, sizes.data());
        env->GetFloatArrayRegion(scaleXs, 0, runCount, scales.data());
        env->GetFloatArrayRegion(baselineShifts, 0, runCount, shifts.data());
        env->GetFloatArrayRegion(holderLeadings, 0, runCount, leadings.data());
        env->GetBooleanArrayRegion(holderBlocks, 0, runCount, blocks.data());
    }

    for (jint i = 0; i < runCount; i++) {
        jint start = bounds[i * 2];
        jint end = bounds[i * 2 + 1];
        TRAttribute attribute;

        attribute = TRAttribute();
        attribute.type = TRAttributeTypeface;
        jobject jtypeface = env->GetObjectArrayElement(typefaces, i);
        jlong typefaceHandle = JavaBridge(env).Typeface_getNativeTypeface(jtypeface);
        env->DeleteLocalRef(jtypeface);
        attribute.value.typeface = reinterpret_cast<Typeface *>(typefaceHandle)->core();
        setAttribute(text, start, end, attribute);

        attribute = TRAttribute();
        attribute.type = TRAttributePointSize;
        attribute.value.pointSize = sizes[i];
        setAttribute(text, start, end, attribute);

        attribute = TRAttribute();
        attribute.type = TRAttributeScaleX;
        attribute.value.scaleX = scales[i];
        setAttribute(text, start, end, attribute);

        attribute = TRAttribute();
        attribute.type = TRAttributeBaselineOffset;
        attribute.value.baselineOffset = shifts[i];
        setAttribute(text, start, end, attribute);

        jobject holder = env->GetObjectArrayElement(holders, i);
        if (holder) {
            TRReplacementCallbacks callbacks = {};
            callbacks.computeRoom = computeRoom;
            callbacks.finalize = finalizeReplacement;

            jobject global = env->NewGlobalRef(holder);
            TRReplacementRef replacement = TRReplacementCreate(&callbacks, global, leadings[i],
                                                               blocks[i] ? TRTrue : TRFalse);
            if (replacement) {
                attribute = TRAttribute();
                attribute.type = TRAttributeReplacement;
                attribute.value.replacement = replacement;
                setAttribute(text, start, end, attribute);

                /* The text holds the replacement now. */
                TRReplacementRelease(replacement);
            } else {
                env->DeleteGlobalRef(global);
            }

            env->DeleteLocalRef(holder);
        }
    }

    if (colorCount > 0) {
        vector<jint> colorRanges(colorCount * 2);
        vector<jint> colorValues(colorCount);

        env->GetIntArrayRegion(colorBounds, 0, colorCount * 2, colorRanges.data());
        env->GetIntArrayRegion(colors, 0, colorCount, colorValues.data());

        for (jint i = 0; i < colorCount; i++) {
            TRAttribute attribute = TRAttribute();
            attribute.type = TRAttributeForegroundColor;
            attribute.value.foregroundColor = static_cast<TRColor>(colorValues[i]);
            setAttribute(text, colorRanges[i * 2], colorRanges[i * 2 + 1], attribute);
        }
    }

    TRTextEndEditing(text);

    TRTypesetterRef typesetter = TRTypesetterCreate(text, nullptr, 0);
    TRTextRelease(text);

    return reinterpret_cast<jlong>(typesetter);
}

static void disposeTypesetter(JNIEnv *env, jobject obj, jlong handle)
{
    TRTypesetterRelease(toTypesetter(handle));
}

static jint suggestForwardBreak(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode)
{
    return static_cast<jint>(TRTypesetterSuggestForwardBreak(toTypesetter(handle),
        makeRange(start, end), extent, static_cast<TRBreakMode>(breakMode)));
}

static jint suggestBackwardBreak(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode)
{
    return static_cast<jint>(TRTypesetterSuggestBackwardBreak(toTypesetter(handle),
        makeRange(start, end), extent, static_cast<TRBreakMode>(breakMode)));
}

static void getParagraph(JNIEnv *env, jobject obj, jlong handle, jint index, jintArray out)
{
    TRRange range;
    TRUInt8 baseLevel;

    TRTypesetterGetParagraph(toTypesetter(handle), static_cast<TRUInteger>(index), &range, &baseLevel);

    jint values[3] = { static_cast<jint>(range.index),
                       static_cast<jint>(range.index + range.length),
                       static_cast<jint>(baseLevel) };

    env->SetIntArrayRegion(out, 0, 3, values);
}

static jlong createSimpleLine(JNIEnv *env, jobject obj, jlong handle, jint start, jint end)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateSimpleLine(toTypesetter(handle), makeRange(start, end)));
}

static jlong createFrameLine(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat layoutWidth)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateFrameLine(toTypesetter(handle),
        makeRange(start, end), layoutWidth));
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
        makeRange(start, end), static_cast<TRTruncationPlace>(place), chars,
        static_cast<TRUInteger>(length), TRStringEncodingUTF16);

    if (chars) {
        env->ReleaseStringChars(token, chars);
    }

    return reinterpret_cast<jlong>(line);
}

static jlong createTruncatedLine(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat extent, jint breakMode, jint place, jlong tokenHandle)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateTruncatedLine(toTypesetter(handle),
        makeRange(start, end), extent, static_cast<TRBreakMode>(breakMode),
        static_cast<TRTruncationPlace>(place), toLine(tokenHandle)));
}

static jlong createJustifiedLine(JNIEnv *env, jobject obj, jlong handle, jint start, jint end,
    jfloat factor, jfloat extent)
{
    return reinterpret_cast<jlong>(TRTypesetterCreateJustifiedLine(toTypesetter(handle),
        makeRange(start, end), factor, extent));
}

static JNINativeMethod TYPESETTER_METHODS[] = {
    { "nCreate", "(Ljava/lang/String;I[I[Lcom/mta/tehreer/graphics/Typeface;[F[F[F[Ljava/lang/Object;[F[ZI[I[I)J", (void *)createTypesetter },
    { "nDispose", "(J)V", (void *)disposeTypesetter },
    { "nSuggestForwardBreak", "(JIIFI)I", (void *)suggestForwardBreak },
    { "nSuggestBackwardBreak", "(JIIFI)I", (void *)suggestBackwardBreak },
    { "nGetParagraph", "(JI[I)V", (void *)getParagraph },
    { "nCreateSimpleLine", "(JII)J", (void *)createSimpleLine },
    { "nCreateFrameLine", "(JIIF)J", (void *)createFrameLine },
    { "nCreateTruncationToken", "(JIIILjava/lang/String;)J", (void *)createTruncationToken },
    { "nCreateTruncatedLine", "(JIIFIIJ)J", (void *)createTruncatedLine },
    { "nCreateJustifiedLine", "(JIIFF)J", (void *)createJustifiedLine },
};

jint register_com_mta_tehreer_layout_Typesetter(JNIEnv *env)
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

    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/Typesetter", TYPESETTER_METHODS,
                                     sizeof(TYPESETTER_METHODS) / sizeof(TYPESETTER_METHODS[0]));
}

// MARK: Composed Line

static void disposeLine(JNIEnv *env, jobject obj, jlong handle)
{
    TRComposedLineRelease(toLine(handle));
}

static void getLineInts(JNIEnv *env, jobject obj, jlong handle, jintArray out)
{
    TRComposedLineRef line = toLine(handle);
    TRRange range = TRComposedLineGetCodeUnitRange(line);
    jint values[6] = {
        static_cast<jint>(range.index),
        static_cast<jint>(range.index + range.length),
        static_cast<jint>(TRComposedLineGetParagraphLevel(line)),
        TRComposedLineIsBlock(line) ? 1 : 0,
        TRComposedLineIsTruncated(line) ? 1 : 0,
        static_cast<jint>(TRComposedLineGetGlyphRunCount(line)),
    };

    env->SetIntArrayRegion(out, 0, 6, values);
}

static void getLineFloats(JNIEnv *env, jobject obj, jlong handle, jfloatArray out)
{
    TRComposedLineRef line = toLine(handle);
    jfloat values[5] = {
        TRComposedLineGetAscent(line),
        TRComposedLineGetDescent(line),
        TRComposedLineGetLeading(line),
        TRComposedLineGetWidth(line),
        TRComposedLineGetTrailingWhitespaceExtent(line),
    };

    env->SetFloatArrayRegion(out, 0, 5, values);
}

static jlong getLineRun(JNIEnv *env, jobject obj, jlong handle, jint index)
{
    return reinterpret_cast<jlong>(TRComposedLineGetGlyphRun(toLine(handle), static_cast<TRUInteger>(index)));
}

static JNINativeMethod LINE_METHODS[] = {
    { "nDispose", "(J)V", (void *)disposeLine },
    { "nGetInts", "(J[I)V", (void *)getLineInts },
    { "nGetFloats", "(J[F)V", (void *)getLineFloats },
    { "nGetRun", "(JI)J", (void *)getLineRun },
};

jint register_com_mta_tehreer_layout_ComposedLine(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/ComposedLine", LINE_METHODS,
                                     sizeof(LINE_METHODS) / sizeof(LINE_METHODS[0]));
}

// MARK: Glyph Run

static void getRunInts(JNIEnv *env, jobject obj, jlong handle, jintArray out)
{
    TRGlyphRunRef run = toRun(handle);
    TRRange range = TRGlyphRunGetCodeUnitRange(run);
    TRColor color;
    jint values[11] = {
        static_cast<jint>(range.index),
        static_cast<jint>(range.index + range.length),
        static_cast<jint>(TRGlyphRunGetStartExtraLength(run)),
        static_cast<jint>(TRGlyphRunGetEndExtraLength(run)),
        static_cast<jint>(TRGlyphRunGetBidiLevel(run)),
        static_cast<jint>(TRGlyphRunGetWritingDirection(run)),
        TRGlyphRunIsBackward(run) ? 1 : 0,
        static_cast<jint>(TRGlyphRunGetGlyphCount(run)),
        static_cast<jint>(TRGlyphRunGetClusterMapCount(run)),
        TRGlyphRunGetForegroundColor(run, &color) ? 1 : 0,
        static_cast<jint>(color),
    };

    env->SetIntArrayRegion(out, 0, 11, values);
}

static void getRunFloats(JNIEnv *env, jobject obj, jlong handle, jfloatArray out)
{
    TRGlyphRunRef run = toRun(handle);
    TRPoint origin = TRGlyphRunGetOrigin(run);
    jfloat values[9] = {
        TRGlyphRunGetTypeSize(run),
        TRGlyphRunGetScaleX(run),
        TRGlyphRunGetAscent(run),
        TRGlyphRunGetDescent(run),
        TRGlyphRunGetLeading(run),
        origin.x,
        origin.y,
        TRGlyphRunGetWidth(run),
        TRGlyphRunGetHeight(run),
    };

    env->SetFloatArrayRegion(out, 0, 9, values);
}

static jlong getRunTypeface(JNIEnv *env, jobject obj, jlong handle)
{
    return reinterpret_cast<jlong>(TRGlyphRunGetTypeface(toRun(handle)));
}

static jobject getRunReplacement(JNIEnv *env, jobject obj, jlong handle)
{
    TRReplacementRef replacement = TRGlyphRunGetReplacement(toRun(handle));
    if (!replacement) {
        return nullptr;
    }

    return env->NewLocalRef(static_cast<jobject>(TRReplacementGetUserData(replacement)));
}

static void getRunGlyphIds(JNIEnv *env, jobject obj, jlong handle, jintArray out)
{
    TRGlyphRunRef run = toRun(handle);
    const TRGlyphID *ids = TRGlyphRunGetGlyphIDsPtr(run);
    jint count = static_cast<jint>(TRGlyphRunGetGlyphCount(run));
    vector<jint> values(count);

    for (jint i = 0; i < count; i++) {
        values[i] = ids[i];
    }

    env->SetIntArrayRegion(out, 0, count, values.data());
}

static void getRunGlyphOffsets(JNIEnv *env, jobject obj, jlong handle, jfloatArray out)
{
    TRGlyphRunRef run = toRun(handle);
    const TRPoint *offsets = TRGlyphRunGetGlyphOffsetsPtr(run);
    jint count = static_cast<jint>(TRGlyphRunGetGlyphCount(run));
    vector<jfloat> values(count * 2);

    for (jint i = 0; i < count; i++) {
        values[i * 2] = offsets[i].x;
        values[i * 2 + 1] = offsets[i].y;
    }

    env->SetFloatArrayRegion(out, 0, count * 2, values.data());
}

static void getRunGlyphAdvances(JNIEnv *env, jobject obj, jlong handle, jfloatArray out)
{
    TRGlyphRunRef run = toRun(handle);

    env->SetFloatArrayRegion(out, 0, static_cast<jint>(TRGlyphRunGetGlyphCount(run)),
                             TRGlyphRunGetGlyphAdvancesPtr(run));
}

static void getRunClusterMap(JNIEnv *env, jobject obj, jlong handle, jintArray out)
{
    TRGlyphRunRef run = toRun(handle);
    const TRUInteger *map = TRGlyphRunGetClusterMapPtr(run);
    jint count = static_cast<jint>(TRGlyphRunGetClusterMapCount(run));
    vector<jint> values(count);

    for (jint i = 0; i < count; i++) {
        values[i] = static_cast<jint>(map[i]);
    }

    env->SetIntArrayRegion(out, 0, count, values.data());
}

static jint getRunClusterStart(JNIEnv *env, jobject obj, jlong handle, jint index)
{
    return static_cast<jint>(TRGlyphRunGetClusterStart(toRun(handle), static_cast<TRUInteger>(index)));
}

static jint getRunClusterEnd(JNIEnv *env, jobject obj, jlong handle, jint index)
{
    return static_cast<jint>(TRGlyphRunGetClusterEnd(toRun(handle), static_cast<TRUInteger>(index)));
}

static jint getRunLeadingGlyphIndex(JNIEnv *env, jobject obj, jlong handle, jint index)
{
    return static_cast<jint>(TRGlyphRunGetLeadingGlyphIndex(toRun(handle), static_cast<TRUInteger>(index)));
}

static jint getRunTrailingGlyphIndex(JNIEnv *env, jobject obj, jlong handle, jint index)
{
    return static_cast<jint>(TRGlyphRunGetTrailingGlyphIndex(toRun(handle), static_cast<TRUInteger>(index)));
}

static jfloat getRunDistance(JNIEnv *env, jobject obj, jlong handle, jint index)
{
    return TRGlyphRunGetDistance(toRun(handle), static_cast<TRUInteger>(index));
}

static jint getRunIndexOfCodeUnit(JNIEnv *env, jobject obj, jlong handle, jfloat distance)
{
    return static_cast<jint>(TRGlyphRunGetIndexOfCodeUnit(toRun(handle), distance));
}

static void getRunBoundingBox(JNIEnv *env, jobject obj, jlong handle, jint glyphStart, jint glyphEnd,
    jlong rendererHandle, jfloatArray out)
{
    TRRect box = TRGlyphRunGetBoundingBox(toRun(handle), makeRange(glyphStart, glyphEnd),
                                          reinterpret_cast<TRRendererRef>(rendererHandle));
    jfloat values[4] = { box.origin.x, box.origin.y,
                         box.origin.x + box.size.width, box.origin.y + box.size.height };

    env->SetFloatArrayRegion(out, 0, 4, values);
}

static JNINativeMethod RUN_METHODS[] = {
    { "nGetInts", "(J[I)V", (void *)getRunInts },
    { "nGetFloats", "(J[F)V", (void *)getRunFloats },
    { "nGetTypeface", "(J)J", (void *)getRunTypeface },
    { "nGetReplacement", "(J)Ljava/lang/Object;", (void *)getRunReplacement },
    { "nGetGlyphIds", "(J[I)V", (void *)getRunGlyphIds },
    { "nGetGlyphOffsets", "(J[F)V", (void *)getRunGlyphOffsets },
    { "nGetGlyphAdvances", "(J[F)V", (void *)getRunGlyphAdvances },
    { "nGetClusterMap", "(J[I)V", (void *)getRunClusterMap },
    { "nGetClusterStart", "(JI)I", (void *)getRunClusterStart },
    { "nGetClusterEnd", "(JI)I", (void *)getRunClusterEnd },
    { "nGetLeadingGlyphIndex", "(JI)I", (void *)getRunLeadingGlyphIndex },
    { "nGetTrailingGlyphIndex", "(JI)I", (void *)getRunTrailingGlyphIndex },
    { "nGetDistance", "(JI)F", (void *)getRunDistance },
    { "nGetIndexOfCodeUnit", "(JF)I", (void *)getRunIndexOfCodeUnit },
    { "nGetBoundingBox", "(JIIJ[F)V", (void *)getRunBoundingBox },
};

jint register_com_mta_tehreer_layout_GlyphRun(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/layout/GlyphRun", RUN_METHODS,
                                     sizeof(RUN_METHODS) / sizeof(RUN_METHODS[0]));
}
