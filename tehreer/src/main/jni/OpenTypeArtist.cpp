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
#include <SFArtist.h>
#include <SFBase.h>
#include <SFPattern.h>
#include <SFScheme.h>
}

#include <cstddef>
#include <cstring>
#include <jni.h>
#include <map>

#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "OpenTypeTag.h"
#include "PatternCache.h"
#include "TextDirection.h"
#include "OpenTypeArtist.h"

using namespace Tehreer;

static inline SFTextDirection makeSFTextDirection(TextDirection textDirection, SFTag scriptTag)
{
    switch (textDirection) {
    case TextDirection::LeftToRight:
        return SFTextDirectionLeftToRight;

    case TextDirection::RightToLeft:
        return SFTextDirectionRightToLeft;

    default:
        return SFScriptGetDefaultDirection(scriptTag);
    }
}

static inline TextDirection makeTRTextDirection(SFTextDirection textDirection) {
    switch (textDirection) {
    case SFTextDirectionRightToLeft:
        return TextDirection::RightToLeft;

    default:
        return TextDirection::LeftToRight;
    }
}

TextDirection OpenTypeArtist::getScriptDefaultDirection(uint32_t scriptTag)
{
    SFTextDirection textDirection = SFScriptGetDefaultDirection(scriptTag);
    return makeTRTextDirection(textDirection);
}

OpenTypeArtist::OpenTypeArtist()
{
    m_sfArtist = SFArtistCreate();
    m_sfScheme = SFSchemeCreate();
    m_typeface = nullptr;
    m_scriptTag = OpenTypeTag::make('d', 'f', 'l', 't');
    m_languageTag = OpenTypeTag::make('d', 'f', 'l', 't');
    m_charArray = nullptr;
    m_textRange = Range();
    m_textDirection = TextDirection::Default;
    m_textMode = SFTextModeForward;
}

OpenTypeArtist::~OpenTypeArtist()
{
    SFArtistRelease(m_sfArtist);
    SFSchemeRelease(m_sfScheme);
    delete [] m_charArray;
}

void OpenTypeArtist::setText(const jchar *charArray, jint charCount)
{
    delete [] m_charArray;
    m_charArray = new jchar[charCount];
    m_textRange = Range(0, charCount);

    memcpy(m_charArray, charArray, sizeof(jchar) * charCount);
}

void OpenTypeArtist::setTextRange(Range textRange)
{
    m_textRange = textRange;
    SFArtistSetString(m_sfArtist, SFStringEncodingUTF16, m_charArray + textRange.start, textRange.length());
}

void OpenTypeArtist::setTextMode(SFTextMode textMode)
{
    m_textMode = textMode;
    SFArtistSetTextMode(m_sfArtist, textMode);
}

void OpenTypeArtist::fillAlbum(OpenTypeAlbum &album)
{
    PatternCache &cache = m_typeface->patternCache();
    PatternKey key(m_scriptTag, m_languageTag);
    SFPatternRef pattern = cache.get(key);

    if (!pattern) {
        SFSchemeSetFont(m_sfScheme, m_typeface->sheenfigureFont());
        SFSchemeSetScriptTag(m_sfScheme, m_scriptTag);
        SFSchemeSetLanguageTag(m_sfScheme, m_languageTag);

        pattern = SFSchemeBuildPattern(m_sfScheme);
        cache.put(key, pattern);
        SFPatternRelease(pattern);
    }

    SFTextDirection textDirection = makeSFTextDirection(m_textDirection, m_scriptTag);

    SFArtistSetPattern(m_sfArtist, pattern);
    SFArtistSetTextDirection(m_sfArtist, textDirection);
    SFArtistFillAlbum(m_sfArtist, album.SFAlbum());

    album.associateText(m_textRange, m_textMode == SFTextModeBackward);
}

static jint getScriptDefaultDirection(JNIEnv *env, jobject obj, jint scriptTag)
{
    return (jint)OpenTypeArtist::getScriptDefaultDirection(scriptTag);
}

static jlong create(JNIEnv *env, jobject obj)
{
    OpenTypeArtist *opentypeArtist = new OpenTypeArtist();
    return reinterpret_cast<jlong>(opentypeArtist);
}

static void dispose(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    delete opentypeArtist;
}

static void setTypeface(JNIEnv *env, jobject obj, jlong artistHandle, jobject jtypeface)
{
    JavaBridge bridge(env);
    jlong typefaceHandle = bridge.Typeface_getNativeTypeface(jtypeface);

    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(artistHandle);
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);

    opentypeArtist->setTypeface(typeface);
}

static jint getScriptTag(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    return opentypeArtist->scriptTag();
}

static void setScriptTag(JNIEnv *env, jobject obj, jlong handle, jint scriptTag)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    opentypeArtist->setScriptTag(scriptTag);
}

static jint getLanguageTag(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    return opentypeArtist->languageTag();
}

static void setLanguageTag(JNIEnv *env, jobject obj, jlong handle, jint languageTag)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    opentypeArtist->setLanguageTag(languageTag);
}

static void setText(JNIEnv *env, jobject obj, jlong handle, jstring text)
{
    if (text) {
        const jchar *charArray = env->GetStringChars(text, nullptr);
        jint charCount = env->GetStringLength(text);

        OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
        opentypeArtist->setText(charArray, charCount);

        env->ReleaseStringChars(text, charArray);
    } else {
        OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
        opentypeArtist->setText(NULL, 0);
    }
}

static jint getTextStart(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    return opentypeArtist->textRange().start;
}

static jint getTextEnd(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    return opentypeArtist->textRange().end;
}

static void setTextRange(JNIEnv *env, jobject obj, jlong handle, jint charStart, jint charEnd)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    opentypeArtist->setTextRange(Range(charStart, charEnd));
}

static jint getTextDirection(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    return (jint)opentypeArtist->textDirection();
}

static void setTextDirection(JNIEnv *env, jobject obj, jlong handle, jint textDirection)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    opentypeArtist->setTextDirection((TextDirection)textDirection);
}

static jint getTextMode(JNIEnv *env, jobject obj, jlong handle)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    return opentypeArtist->textMode();
}

static void setTextMode(JNIEnv *env, jobject obj, jlong handle, jint textMode)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(handle);
    opentypeArtist->setTextMode(textMode);
}

static void fillAlbum(JNIEnv *env, jobject obj, jlong artistHandle, jlong albumHandle)
{
    OpenTypeArtist *opentypeArtist = reinterpret_cast<OpenTypeArtist *>(artistHandle);
    OpenTypeAlbum *opentypeAlbum = reinterpret_cast<OpenTypeAlbum *>(albumHandle);
    opentypeArtist->fillAlbum(*opentypeAlbum);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeCreate", "()J", (void *)create },
    { "nativeDispose", "(J)V", (void *)dispose },
    { "nativeGetScriptDefaultDirection", "(I)I", (void *)getScriptDefaultDirection },
    { "nativeSetTypeface", "(JLcom/mta/tehreer/graphics/Typeface;)V", (void *)setTypeface },
    { "nativeGetScriptTag", "(J)I", (void *)getScriptTag },
    { "nativeSetScriptTag", "(JI)V", (void *)setScriptTag },
    { "nativeGetLanguageTag", "(J)I", (void *)getLanguageTag },
    { "nativeSetLanguageTag", "(JI)V", (void *)setLanguageTag },
    { "nativeSetText", "(JLjava/lang/String;)V", (void *)setText },
    { "nativeGetTextStart", "(J)I", (void *)getTextStart },
    { "nativeGetTextEnd", "(J)I", (void *)getTextEnd },
    { "nativeSetTextRange", "(JII)V", (void *)setTextRange },
    { "nativeGetTextDirection", "(J)I", (void *)getTextDirection },
    { "nativeSetTextDirection", "(JI)V", (void *)setTextDirection },
    { "nativeGetTextMode", "(J)I", (void *)getTextMode },
    { "nativeSetTextMode", "(JI)V", (void *)setTextMode },
    { "nativeFillAlbum", "(JJ)V", (void *)fillAlbum },
};

jint register_com_mta_tehreer_opentype_OpenTypeArtist(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/opentype/OpenTypeArtist", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
