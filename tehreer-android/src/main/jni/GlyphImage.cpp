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

#include <cstdint>
#include <jni.h>
#include <vector>

#include <Tehreer/TRGlyphImage.h>

#include "GlyphImage.h"
#include "JavaBridge.h"

using namespace std;
using namespace Tehreer;

static JavaVM *javaVM = nullptr;

/**
 * The bitmap that an image of Core is drawn from, which is kept with the image as its native data.
 */
static void destroyBitmap(void *data)
{
    auto bitmap = static_cast<jobject>(data);
    JNIEnv *env = nullptr;
    bool isAttached = false;

    if (javaVM->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) == JNI_EDETACHED) {
        if (javaVM->AttachCurrentThread(&env, nullptr) != JNI_OK) {
            return;
        }
        isAttached = true;
    }

    env->DeleteGlobalRef(bitmap);

    if (isAttached) {
        javaVM->DetachCurrentThread();
    }
}

static jobject createBitmap(JNIEnv *env, TRGlyphImageRef image)
{
    JavaBridge bridge(env);
    jint width = static_cast<jint>(TRGlyphImageGetWidth(image));
    jint height = static_cast<jint>(TRGlyphImageGetHeight(image));
    const TRUInt8 *pixels = TRGlyphImageGetPixelsPtr(image);

    if (width <= 0 || height <= 0 || !pixels) {
        return nullptr;
    }

    if (TRGlyphImageGetFormat(image) == TRGlyphImageFormatAlpha) {
        jobject bitmap = bridge.Bitmap_create(width, height, JavaBridge::BitmapConfig::Alpha8);
        bridge.Bitmap_setPixels(bitmap, pixels, static_cast<size_t>(width) * height);

        return bitmap;
    }

    /* The pixels of Core are in the order alpha, red, green and blue, while those of a bitmap are
     * red, green, blue and alpha. Both have the colors premultiplied with alpha. */
    size_t length = static_cast<size_t>(width) * height * 4;
    vector<uint8_t> converted(length);

    for (size_t i = 0; i < length; i += 4) {
        converted[i + 0] = pixels[i + 1];
        converted[i + 1] = pixels[i + 2];
        converted[i + 2] = pixels[i + 3];
        converted[i + 3] = pixels[i + 0];
    }

    jobject bitmap = bridge.Bitmap_create(width, height, JavaBridge::BitmapConfig::ARGB_8888);
    bridge.Bitmap_setPixels(bitmap, converted.data(), length);

    return bitmap;
}

jobject getGlyphImageBitmap(JNIEnv *env, TRGlyphImageRef image)
{
    void *data = TRGlyphImageGetNativeData(image);
    if (data) {
        return env->NewLocalRef(static_cast<jobject>(data));
    }

    jobject bitmap = createBitmap(env, image);
    if (!bitmap) {
        return nullptr;
    }

    jobject global = env->NewGlobalRef(bitmap);
    if (!TRGlyphImageSetNativeData(image, global, destroyBitmap)) {
        /* Another thread attached its own bitmap first, which is used as it is. */
        env->DeleteGlobalRef(global);

        data = TRGlyphImageGetNativeData(image);
        if (data) {
            env->DeleteLocalRef(bitmap);
            return env->NewLocalRef(static_cast<jobject>(data));
        }
    }

    return bitmap;
}

static jint getFormat(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphImageGetFormat(toGlyphImage(handle)));
}

static jint getLeft(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphImageGetLeft(toGlyphImage(handle)));
}

static jint getTop(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphImageGetTop(toGlyphImage(handle)));
}

static jint getWidth(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphImageGetWidth(toGlyphImage(handle)));
}

static jint getHeight(JNIEnv *env, jobject obj, jlong handle)
{
    return static_cast<jint>(TRGlyphImageGetHeight(toGlyphImage(handle)));
}

static jobject getBitmap(JNIEnv *env, jobject obj, jlong handle)
{
    return getGlyphImageBitmap(env, toGlyphImage(handle));
}

static void dispose(JNIEnv *env, jclass clazz, jlong handle)
{
    TRGlyphImageRelease(toGlyphImage(handle));
}

static JNINativeMethod JNI_METHODS[] = {
    { "nGetFormat", "(J)I", (void *)getFormat },
    { "nGetLeft", "(J)I", (void *)getLeft },
    { "nGetTop", "(J)I", (void *)getTop },
    { "nGetWidth", "(J)I", (void *)getWidth },
    { "nGetHeight", "(J)I", (void *)getHeight },
    { "nGetBitmap", "(J)Landroid/graphics/Bitmap;", (void *)getBitmap },
    { "nDispose", "(J)V", (void *)dispose },
};

jint register_com_mta_tehreer_graphics_GlyphImage(JNIEnv *env)
{
    env->GetJavaVM(&javaVM);

    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/GlyphImage", JNI_METHODS,
                                     sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
