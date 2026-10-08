/*
 * Copyright (C) 2016-2026 Muhammad Tayyab Akram
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
static jobject   BITMAP_CONFIG__ARGB_8888;

static jclass    BITMAP;
static jmethodID BITMAP__CREATE_BITMAP;


static jmethodID INPUT_STREAM__READ;

static jclass    PATH;
static jmethodID PATH__CONSTRUCTOR;
static jmethodID PATH__CLOSE;
static jmethodID PATH__CUBIC_TO;
static jmethodID PATH__LINE_TO;
static jmethodID PATH__MOVE_TO;
static jmethodID PATH__QUAD_TO;

static jclass    RECT;
static jmethodID RECT__CONSTRUCTOR;

static jmethodID CANVAS__DRAW_BITMAP;

static jclass    RECT_F;
static jmethodID RECT_F__CONSTRUCTOR;



void JavaBridge::load(JNIEnv* env)
{
    jclass clazz;
    jfieldID fieldID;
    jobject field;

    clazz = env->FindClass("com/mta/tehreer/unicode/BidiPair");
    BIDI_PAIR = (jclass)env->NewGlobalRef(clazz);
    BIDI_PAIR__CONSTRUCTOR = env->GetMethodID(clazz, "<init>", "(III)V");

    clazz = env->FindClass("com/mta/tehreer/unicode/BidiRun");
    BIDI_RUN = (jclass)env->NewGlobalRef(clazz);
    BIDI_RUN__CONSTRUCTOR = env->GetMethodID(clazz, "<init>", "(IIB)V");

    clazz = env->FindClass("android/graphics/Bitmap");
    BITMAP = (jclass)env->NewGlobalRef(clazz);
    BITMAP__CREATE_BITMAP = env->GetStaticMethodID(BITMAP, "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    clazz = env->FindClass("android/graphics/Bitmap$Config");
    fieldID = env->GetStaticFieldID(clazz, "ALPHA_8", "Landroid/graphics/Bitmap$Config;");
    field = env->GetStaticObjectField(clazz, fieldID);
    BITMAP_CONFIG__ALPHA_8 = env->NewGlobalRef(field);

    fieldID = env->GetStaticFieldID(clazz, "ARGB_8888", "Landroid/graphics/Bitmap$Config;");
    field = env->GetStaticObjectField(clazz, fieldID);
    BITMAP_CONFIG__ARGB_8888 = env->NewGlobalRef(field);


    clazz = env->FindClass("java/io/InputStream");
    INPUT_STREAM__READ = env->GetMethodID(clazz, "read", "([BII)I");

    clazz = env->FindClass("android/graphics/Path");
    PATH = (jclass)env->NewGlobalRef(clazz);
    PATH__CONSTRUCTOR = env->GetMethodID(clazz, "<init>", "()V");
    PATH__CLOSE = env->GetMethodID(clazz, "close", "()V");
    PATH__CUBIC_TO = env->GetMethodID(clazz, "cubicTo", "(FFFFFF)V");
    PATH__LINE_TO = env->GetMethodID(clazz, "lineTo", "(FF)V");
    PATH__MOVE_TO = env->GetMethodID(clazz, "moveTo", "(FF)V");
    PATH__QUAD_TO = env->GetMethodID(clazz, "quadTo", "(FFFF)V");

    clazz = env->FindClass("android/graphics/Rect");
    RECT = (jclass)env->NewGlobalRef(clazz);
    RECT__CONSTRUCTOR = env->GetMethodID(clazz, "<init>", "(IIII)V");

    clazz = env->FindClass("android/graphics/Canvas");
    CANVAS__DRAW_BITMAP = env->GetMethodID(clazz, "drawBitmap", "(Landroid/graphics/Bitmap;FFLandroid/graphics/Paint;)V");

    clazz = env->FindClass("android/graphics/RectF");
    RECT_F = (jclass)env->NewGlobalRef(clazz);
    RECT_F__CONSTRUCTOR = env->GetMethodID(clazz, "<init>", "(FFFF)V");


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

jobject JavaBridge::BidiPair_construct(jint charIndex, jint actualCodePoint, jint pairingCodePoint) const
{
    return m_env->NewObject(BIDI_PAIR, BIDI_PAIR__CONSTRUCTOR, charIndex, actualCodePoint, pairingCodePoint);
}

jobject JavaBridge::BidiRun_construct(jint charStart, jint charEnd, jbyte embeddingLevel) const
{
    return m_env->NewObject(BIDI_RUN, BIDI_RUN__CONSTRUCTOR, charStart, charEnd, embeddingLevel);
}

jobject JavaBridge::Bitmap_create(jint width, jint height, BitmapConfig config) const
{
    jobject configField = nullptr;

    switch (config) {
    case BitmapConfig::ARGB_8888:
        configField = BITMAP_CONFIG__ARGB_8888;
        break;

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

jint JavaBridge::InputStream_read(jobject inputStream, jbyteArray buffer, jint offset, jint length) const
{
    return m_env->CallIntMethod(inputStream, INPUT_STREAM__READ, buffer, offset, length);
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

jobject JavaBridge::RectF_construct(jfloat left, jfloat top, jfloat right, jfloat bottom) const
{
    return m_env->NewObject(RECT_F, RECT_F__CONSTRUCTOR, left, top, right, bottom);
}

jobject JavaBridge::Rect_construct(jint left, jint top, jint right, jint bottom) const
{
    return m_env->NewObject(RECT, RECT__CONSTRUCTOR, left, top, right, bottom);
}

void JavaBridge::Canvas_drawBitmap(jobject canvas, jobject bitmap, jfloat left, jfloat top, jobject paint) const
{
    m_env->CallVoidMethod(canvas, CANVAS__DRAW_BITMAP, bitmap, left, top, paint);
}
