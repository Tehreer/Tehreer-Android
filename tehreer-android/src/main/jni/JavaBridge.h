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

#ifndef _TEHREER__JAVA_BRIDGE_H
#define _TEHREER__JAVA_BRIDGE_H

#include <jni.h>

namespace Tehreer {

class JavaBridge {
public:
    static void load(JNIEnv *env);
    static jint registerClass(JNIEnv *env, const char *className, const JNINativeMethod *methodArray, jint methodCount);

    JavaBridge(JNIEnv *env);
    ~JavaBridge();

    JNIEnv *env() const { return m_env; }

    enum class BitmapConfig {
        Alpha8,
        ARGB_8888
    };

    jobject BidiPair_construct(jint charIndex, jint actualCodePoint, jint pairingCodePoint) const;

    jobject BidiRun_construct(jint charStart, jint charEnd, jbyte embeddingLevel) const;

    jobject Bitmap_create(jint width, jint height, BitmapConfig config) const;
    void Bitmap_setPixels(jobject bitmap, const void *pixels, size_t length) const;

    jint Glyph_getGlyphID(jobject glyph) const;
    void Glyph_ownOutline(jobject glyph, jlong nativeOutline) const;
    void Glyph_ownPath(jobject glyph, jobject path) const;

    jobject GlyphImage_construct(jobject bitmap, jint left, jint top) const;

    jint InputStream_read(jobject inputStream, jbyteArray buffer, jint offset, jint length) const;

    jobject NameTableRecord_construct(jint nameId, jint platformId, jint languageId, jint encodingId, jbyteArray bytes) const;

    jobject Path_construct() const;
    void Path_close(jobject path) const;
    void Path_cubicTo(jobject path, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3) const;
    void Path_lineTo(jobject path, jfloat x, jfloat y) const;
    void Path_moveTo(jobject path, jfloat dx, jfloat dy) const;
    void Path_quadTo(jobject path, jfloat x1, jfloat y1, jfloat x2, jfloat y2) const;

    void Rect_set(jobject rect, jint left, jint top, jint right, jint bottom) const;

    jclass String_class() const;

    jobject Typeface_construct(jlong typefaceHandle) const;
    jlong Typeface_getNativeTypeface(jobject typeface) const;

private:
    JNIEnv *m_env;
};

}

#endif
