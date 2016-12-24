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

#include <android/bitmap.h>
#include <cstring>
#include <jni.h>

#include "JavaBridge.h"

using namespace Tehreer;

static jclass    BIDI_PAIR;
static jmethodID BIDI_PAIR__CONSTRUCTOR;

static jclass    BIDI_RUN;
static jmethodID BIDI_RUN__CONSTRUCTOR;

static jobject   BITMAP_CONFIG__ALPHA_8;

static jclass    BITMAP;
static jmethodID BITMAP__CREATE_BITMAP;

static jclass    GLYPH;
static jmethodID GLYPH__CONSTRUCTOR;
static jfieldID  GLYPH__GLYPH_ID;
static jfieldID  GLYPH__NATIVE_OUTLINE;
static jmethodID GLYPH__OWN_BITMAP;
static jmethodID GLYPH__OWN_OUTLINE;
static jmethodID GLYPH__OWN_PATH;

static jmethodID INPUT_STREAM__READ;

static jclass    NAME_ENTRY;
static jmethodID NAME_ENTRY__CONSTRUCTOR;

static jclass    PATH;
static jmethodID PATH__CONSTRUCTOR;
static jmethodID PATH__CLOSE;
static jmethodID PATH__CUBIC_TO;
static jmethodID PATH__LINE_TO;
static jmethodID PATH__MOVE_TO;
static jmethodID PATH__QUAD_TO;

static jmethodID RECT__SET;

static jclass    SFNT_NAMES;
static jmethodID SFNT_NAMES__CREATE_LOCALE;
static jmethodID SFNT_NAMES__DECODE_BYTES;
static jmethodID SFNT_NAMES__ADD_NAME;

static jfieldID  TYPEFACE__NATIVE_TYPEFACE;

void JavaBridge::load(JNIEnv* env)
{
    jclass clazz;
    jfieldID fieldID;
    jobject field;

    clazz = env->FindClass("com/mta/tehreer/bidi/BidiPair");
    BIDI_PAIR = (jclass)env->NewGlobalRef(clazz);
    BIDI_PAIR__CONSTRUCTOR = env->GetMethodID(clazz, "<init>", "(II)V");

    clazz = env->FindClass("com/mta/tehreer/bidi/BidiRun");
    BIDI_RUN = (jclass)env->NewGlobalRef(clazz);
    BIDI_RUN__CONSTRUCTOR = env->GetMethodID(clazz, "<init>", "(IIB)V");

    clazz = env->FindClass("android/graphics/Bitmap");
    BITMAP = (jclass)env->NewGlobalRef(clazz);
    BITMAP__CREATE_BITMAP = env->GetStaticMethodID(BITMAP, "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    clazz = env->FindClass("android/graphics/Bitmap$Config");
    fieldID = env->GetStaticFieldID(clazz, "ALPHA_8", "Landroid/graphics/Bitmap$Config;");
    field = env->GetStaticObjectField(clazz, fieldID);
    BITMAP_CONFIG__ALPHA_8 = env->NewGlobalRef(field);

    clazz = env->FindClass("com/mta/tehreer/graphics/Glyph");
    GLYPH = (jclass)env->NewGlobalRef(clazz);
    GLYPH__CONSTRUCTOR = env->GetMethodID(clazz, "<init>", "(I)V");
    GLYPH__GLYPH_ID = env->GetFieldID(clazz, "mGlyphId", "I");
    GLYPH__NATIVE_OUTLINE = env->GetFieldID(clazz, "mNativeOutline", "J");
    GLYPH__OWN_BITMAP = env->GetMethodID(clazz, "ownBitmap", "(Landroid/graphics/Bitmap;II)V");
    GLYPH__OWN_OUTLINE = env->GetMethodID(clazz, "ownOutline", "(J)V");
    GLYPH__OWN_PATH = env->GetMethodID(clazz, "ownPath", "(Landroid/graphics/Path;)V");

    clazz = env->FindClass("java/io/InputStream");
    INPUT_STREAM__READ = env->GetMethodID(clazz, "read", "([BII)I");

    clazz = env->FindClass("com/mta/tehreer/opentype/NameEntry");
    NAME_ENTRY = (jclass)env->NewGlobalRef(clazz);
    NAME_ENTRY__CONSTRUCTOR = env->GetMethodID(clazz, "<init>", "(IIII[BLjava/util/Locale;Ljava/lang/String;)V");

    clazz = env->FindClass("android/graphics/Path");
    PATH = (jclass)env->NewGlobalRef(clazz);
    PATH__CONSTRUCTOR = env->GetMethodID(clazz, "<init>", "()V");
    PATH__CLOSE = env->GetMethodID(clazz, "close", "()V");
    PATH__CUBIC_TO = env->GetMethodID(clazz, "cubicTo", "(FFFFFF)V");
    PATH__LINE_TO = env->GetMethodID(clazz, "lineTo", "(FF)V");
    PATH__MOVE_TO = env->GetMethodID(clazz, "moveTo", "(FF)V");
    PATH__QUAD_TO = env->GetMethodID(clazz, "quadTo", "(FFFF)V");

    clazz = env->FindClass("android/graphics/Rect");
    RECT__SET = env->GetMethodID(clazz, "set", "(IIII)V");

    clazz = env->FindClass("com/mta/tehreer/opentype/SfntNames");
    SFNT_NAMES = (jclass)env->NewGlobalRef(clazz);
    SFNT_NAMES__CREATE_LOCALE = env->GetStaticMethodID(clazz, "createLocale", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/util/Locale;");
    SFNT_NAMES__DECODE_BYTES = env->GetStaticMethodID(clazz, "decodeBytes", "(Ljava/lang/String;[B)Ljava/lang/String;");
    SFNT_NAMES__ADD_NAME = env->GetMethodID(clazz, "addName", "(ILjava/util/Locale;Ljava/lang/String;)V");

    clazz = env->FindClass("com/mta/tehreer/graphics/Typeface");
    TYPEFACE__NATIVE_TYPEFACE = env->GetFieldID(clazz, "nativeTypeface", "J");
}

jint JavaBridge::registerClass(JNIEnv *env, const char *className, const JNINativeMethod *methodArray, jint methodCount)
{
    jclass clazz = env->FindClass(className);
    if (clazz == nullptr) {
        return JNI_ERR;
    }

    return env->RegisterNatives(clazz, methodArray, methodCount);
}

JavaBridge::JavaBridge(JNIEnv* env)
    : m_env(env)
{
}

JavaBridge::~JavaBridge()
{
}

jobject JavaBridge::BidiPair_construct(jint charIndex, jint pairingCodePoint) const
{
    return m_env->NewObject(BIDI_PAIR, BIDI_PAIR__CONSTRUCTOR, charIndex, pairingCodePoint);
}

jobject JavaBridge::BidiRun_construct(jint charStart, jint charEnd, jbyte embeddingLevel) const
{
    return m_env->NewObject(BIDI_RUN, BIDI_RUN__CONSTRUCTOR, charStart, charEnd, embeddingLevel);
}

jobject JavaBridge::Bitmap_create(jint width, jint height, BitmapConfig config) const
{
    jobject configField = nullptr;

    switch (config) {
    default:
        configField = BITMAP_CONFIG__ALPHA_8;
        break;
    }

    return m_env->CallStaticObjectMethod(BITMAP, BITMAP__CREATE_BITMAP, width, height, configField);
}

void JavaBridge::Bitmap_setPixels(jobject bitmap, const void *pixels, size_t length) const
{
    void *source = nullptr;

    AndroidBitmap_lockPixels(m_env, bitmap, &source);
    memcpy(source, pixels, length);
    AndroidBitmap_unlockPixels(m_env, bitmap);
}

jobject JavaBridge::Glyph_construct(jint glyphID) const
{
    return m_env->NewObject(GLYPH, GLYPH__CONSTRUCTOR, glyphID);
}

jint JavaBridge::Glyph_getGlyphID(jobject glyph) const
{
    return m_env->GetIntField(glyph, GLYPH__GLYPH_ID);
}

jlong JavaBridge::Glyph_getNativeOutline(jobject glyph) const
{
    return m_env->GetLongField(glyph, GLYPH__NATIVE_OUTLINE);
}

void JavaBridge::Glyph_ownBitmap(jobject glyph, jobject bitmap, jint left, jint top) const
{
    m_env->CallVoidMethod(glyph, GLYPH__OWN_BITMAP, bitmap, left, top);
}

void JavaBridge::Glyph_ownOutline(jobject glyph, jlong nativeOutline) const
{
    m_env->CallVoidMethod(glyph, GLYPH__OWN_OUTLINE, nativeOutline);
}

void JavaBridge::Glyph_ownPath(jobject glyph, jobject path) const
{
    m_env->CallVoidMethod(glyph, GLYPH__OWN_PATH, path);
}

jint JavaBridge::InputStream_read(jobject inputStream, jbyteArray buffer, jint offset, jint length) const
{
    return m_env->CallIntMethod(inputStream, INPUT_STREAM__READ, buffer, offset, length);
}

jobject JavaBridge::NameEntry_construct(jint nameId, jint platformId, jint languageId, jint encodingId, jbyteArray encodedBytes, jobject relevantLocale, jstring decodedString) const
{
    return m_env->NewObject(NAME_ENTRY, NAME_ENTRY__CONSTRUCTOR, nameId, platformId, languageId, encodingId, encodedBytes, relevantLocale, decodedString);
}

jobject JavaBridge::Path_construct() const
{
    return m_env->NewObject(PATH, PATH__CONSTRUCTOR);
}

void JavaBridge::Path_close(jobject path) const
{
    m_env->CallVoidMethod(path, PATH__CLOSE);
}

void JavaBridge::Path_cubicTo(jobject path, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3) const
{
    m_env->CallVoidMethod(path, PATH__CUBIC_TO, x1, y1, x2, y2, x3, y3);
}

void JavaBridge::Path_lineTo(jobject path, jfloat x, jfloat y) const
{
    m_env->CallVoidMethod(path, PATH__LINE_TO, x, y);
}

void JavaBridge::Path_moveTo(jobject path, jfloat dx, jfloat dy) const
{
    m_env->CallVoidMethod(path, PATH__MOVE_TO, dx, dy);
}

void JavaBridge::Path_quadTo(jobject path, jfloat x1, jfloat y1, jfloat x2, jfloat y2) const
{
    m_env->CallVoidMethod(path, PATH__QUAD_TO, x1, y1, x2, y2);
}

void JavaBridge::Rect_set(jobject rect, jint left, jint top, jint right, jint bottom) const
{
    m_env->CallVoidMethod(rect, RECT__SET, left, top, right, bottom);
}

jobject JavaBridge::SfntNames_createLocale(jstring platform, jstring language, jstring region, jstring script, jstring variant) const
{
    return m_env->CallStaticObjectMethod(SFNT_NAMES, SFNT_NAMES__CREATE_LOCALE, platform, language, region, script, variant);
}

jstring JavaBridge::SfntNames_decodeBytes(jstring encoding, jbyteArray bytes) const
{
    return static_cast<jstring>(m_env->CallStaticObjectMethod(SFNT_NAMES, SFNT_NAMES__DECODE_BYTES, encoding, bytes));
}

void JavaBridge::SfntNames_addName(jobject sfntNames, jint nameId, jobject relevantLocale, jstring decodedString) const
{
    m_env->CallVoidMethod(sfntNames, SFNT_NAMES__ADD_NAME, nameId, relevantLocale, decodedString);
}

jlong JavaBridge::Typeface_getNativeTypeface(jobject typeface) const
{
    return m_env->GetLongField(typeface, TYPEFACE__NATIVE_TYPEFACE);
}
