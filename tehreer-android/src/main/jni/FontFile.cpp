/*
 * Copyright (C) 2019-2026 Muhammad Tayyab Akram
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

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstdlib>
#include <jni.h>

#include <Tehreer/TRFontFile.h>
#include <Tehreer/TRTypeface.h>

#include "FontFile.h"
#include "JavaBridge.h"
#include "StreamUtils.h"
#include "Typeface.h"

using namespace Tehreer;

static TRFontFileRef toFontFile(jlong handle)
{
    return reinterpret_cast<TRFontFileRef>(handle);
}

TRFontFileRef Tehreer::createFontFileFromAsset(JNIEnv *env, jobject assetManager, jstring path)
{
    TRFontFileRef fontFile = nullptr;

    if (assetManager && path) {
        const char *utfChars = env->GetStringUTFChars(path, nullptr);
        AAssetManager *nativeAssetManager = AAssetManager_fromJava(env, assetManager);
        AAsset *asset = AAssetManager_open(nativeAssetManager, utfChars, AASSET_MODE_BUFFER);

        if (asset) {
            /* Core keeps its own copy of the data, so the asset is not needed afterwards. */
            const void *data = AAsset_getBuffer(asset);
            if (data) {
                fontFile = TRFontFileCreateFromMemory(data, static_cast<TRUInteger>(AAsset_getLength64(asset)));
            }

            AAsset_close(asset);
        }

        env->ReleaseStringUTFChars(path, utfChars);
    }

    return fontFile;
}

TRFontFileRef Tehreer::createFontFileFromPath(JNIEnv *env, jstring path)
{
    TRFontFileRef fontFile = nullptr;

    if (path) {
        const char *utfChars = env->GetStringUTFChars(path, nullptr);

        fontFile = TRFontFileCreateFromPath(utfChars);

        env->ReleaseStringUTFChars(path, utfChars);
    }

    return fontFile;
}

TRFontFileRef Tehreer::createFontFileFromStream(JNIEnv *env, jobject stream)
{
    TRFontFileRef fontFile = nullptr;

    if (stream) {
        size_t length;
        void *buffer = StreamUtils::toRawBuffer(JavaBridge(env), stream, &length);

        if (buffer) {
            fontFile = TRFontFileCreateFromMemory(buffer, static_cast<TRUInteger>(length));
            free(buffer);
        }
    }

    return fontFile;
}

static jlong createFromAsset(JNIEnv *env, jclass clazz, jobject assetManager, jstring path)
{
    return reinterpret_cast<jlong>(createFontFileFromAsset(env, assetManager, path));
}

static jlong createFromPath(JNIEnv *env, jclass clazz, jstring path)
{
    return reinterpret_cast<jlong>(createFontFileFromPath(env, path));
}

static jlong createFromStream(JNIEnv *env, jclass clazz, jobject stream)
{
    return reinterpret_cast<jlong>(createFontFileFromStream(env, stream));
}

static void release(JNIEnv *env, jclass clazz, jlong fontFileHandle)
{
    TRFontFileRelease(toFontFile(fontFileHandle));
}

static jint getFaceCount(JNIEnv *env, jobject obj, jlong fontFileHandle)
{
    return static_cast<jint>(TRFontFileGetFaceCount(toFontFile(fontFileHandle)));
}

static jlong createTypeface(JNIEnv *env, jobject obj, jlong fontFileHandle, jint faceIndex)
{
    return reinterpret_cast<jlong>(TRTypefaceCreate(toFontFile(fontFileHandle),
                                                    static_cast<TRUInteger>(faceIndex)));
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreateFromAsset", "(Landroid/content/res/AssetManager;Ljava/lang/String;)J", (void *)createFromAsset },
    { "nCreateFromPath", "(Ljava/lang/String;)J", (void *)createFromPath },
    { "nCreateFromStream", "(Ljava/io/InputStream;)J", (void *)createFromStream },
    { "nRelease", "(J)V", (void *)release },
    { "nGetFaceCount", "(J)I", (void *)getFaceCount },
    { "nCreateTypeface", "(JI)J", (void *)createTypeface },
};

jint register_com_mta_tehreer_font_FontFile(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/font/FontFile", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
