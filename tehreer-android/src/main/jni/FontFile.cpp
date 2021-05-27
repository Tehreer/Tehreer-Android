/*
 * Copyright (C) 2019-2021 Muhammad Tayyab Akram
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
#include <ft2build.h>
#include FT_FREETYPE_H
#include FT_SYSTEM_H
}

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstdlib>
#include <jni.h>
#include <mutex>

#include "FreeType.h"
#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "RenderableFace.h"
#include "Typeface.h"
#include "StreamUtils.h"
#include "FontFile.h"

using namespace Tehreer;

static FT_Stream createStream(AAssetManager *assetManager, const char *path)
{
    AAsset *asset = AAssetManager_open(assetManager, path, AASSET_MODE_UNKNOWN);
    if (!asset) {
        return nullptr;
    }

    off_t size = AAsset_getLength(asset);
    if (size == 0) {
        return nullptr;
    }

    FT_Stream stream;
    stream = (FT_Stream)malloc(sizeof(*stream));
    stream->base = nullptr;
    stream->size = static_cast<unsigned long>(size);
    stream->pos = 0;
    stream->descriptor.pointer = asset;
    stream->pathname.pointer = nullptr;
    stream->read = [](FT_Stream stream, unsigned long offset,
                      unsigned char *buffer, unsigned long count) -> unsigned long {
        auto asset = static_cast<AAsset *>(stream->descriptor.pointer);
        int bytesRead = 0;

        if (count == 0 && offset > stream->size) {
            return 1;
        }

        if (stream->pos != offset) {
            AAsset_seek(asset, offset, SEEK_SET);
        }
        bytesRead = AAsset_read(asset, buffer, count);

        return static_cast<unsigned long>(bytesRead);
    };
    stream->close = nullptr;

    return stream;
}

static void disposeStream(FT_Stream stream)
{
    auto asset = static_cast<AAsset *>(stream->descriptor.pointer);
    AAsset_close(asset);

    free(stream);
}

FontFile *FontFile::createFromAsset(AAssetManager *assetManager, const char *path)
{
    FT_Stream stream = createStream(assetManager, path);
    if (stream) {
        FT_Open_Args args;
        args.flags = FT_OPEN_STREAM;
        args.memory_base = nullptr;
        args.memory_size = 0;
        args.pathname = nullptr;
        args.stream = stream;

        return createWithArgs(&args);
    }

    return nullptr;
}

FontFile *FontFile::createFromPath(const char *path)
{
    FT_Open_Args args;
    args.flags = FT_OPEN_PATHNAME;
    args.memory_base = nullptr;
    args.memory_size = 0;
    args.pathname = const_cast<FT_String *>(path);
    args.stream = nullptr;

    return createWithArgs(&args);
}

FontFile *FontFile::createFromStream(const JavaBridge &bridge, jobject stream)
{
    size_t length;
    void *buffer = StreamUtils::toRawBuffer(bridge, stream, &length);

    if (buffer) {
        FT_Open_Args args;
        args.flags = FT_OPEN_MEMORY;
        args.memory_base = static_cast<const FT_Byte *>(buffer);
        args.memory_size = length;
        args.pathname = nullptr;
        args.stream = nullptr;

        return createWithArgs(&args);
    }

    return nullptr;
}

FontFile *FontFile::createWithArgs(const FT_Open_Args *args)
{
    std::mutex &mutex = FreeType::mutex();
    mutex.lock();

    FT_Face ftFace = nullptr;
    FT_Long numFaces = 0;
    FT_Error error = FT_Open_Face(FreeType::library(), args, -1, &ftFace);
    if (error == FT_Err_Ok) {
        numFaces = ftFace->num_faces;
        FT_Done_Face(ftFace);
    }

    mutex.unlock();

    return new FontFile(args, (void *)args->memory_base, args->stream, numFaces);
}

FontFile::FontFile(const FT_Open_Args *args, void *buffer, FT_Stream stream, FT_Long numFaces)
{
    m_args = *args;
    m_buffer = buffer;
    m_stream = stream;
    m_numFaces = numFaces;
    m_retainCount = 1;
}

FontFile::~FontFile()
{
    if (m_stream) {
        disposeStream(m_stream);
    }
    if (m_buffer) {
        free(m_buffer);
    }
}

FontFile &FontFile::retain()
{
    m_retainCount++;
    return *this;
}

void FontFile::release()
{
    if (--m_retainCount == 0) {
        delete this;
    }
}

RenderableFace *FontFile::createRenderableFace(FT_Long faceIndex, FT_Long instanceIndex)
{
    std::mutex &mutex = FreeType::mutex();
    mutex.lock();

    FT_Face ftFace = nullptr;
    FT_Long id = (instanceIndex << 16) + faceIndex;
    FT_Error error = FT_Open_Face(FreeType::library(), &m_args, id, &ftFace);
    if (error == FT_Err_Ok) {
        if (!FT_IS_SCALABLE(ftFace)) {
            FT_Done_Face(ftFace);
            ftFace = nullptr;
        }
    }

    mutex.unlock();

    if (ftFace) {
        return RenderableFace::create(this, ftFace);
    }

    return nullptr;
}

static jlong createFromAsset(JNIEnv *env, jobject obj, jobject assetManager, jstring path)
{
    if (path) {
        const char *utfChars = env->GetStringUTFChars(path, nullptr);
        AAssetManager *nativeAssetManager = AAssetManager_fromJava(env, assetManager);
        FontFile *fontFile = FontFile::createFromAsset(nativeAssetManager, utfChars);

        env->ReleaseStringUTFChars(path, utfChars);

        return reinterpret_cast<jlong>(fontFile);
    }

    return 0;
}

static jlong createFromPath(JNIEnv *env, jobject obj, jstring path)
{
    if (path) {
        const char *utfChars = env->GetStringUTFChars(path, nullptr);
        FontFile *fontFile = FontFile::createFromPath(utfChars);

        env->ReleaseStringUTFChars(path, utfChars);

        return reinterpret_cast<jlong>(fontFile);
    }

    return 0;
}

static jlong createFromStream(JNIEnv *env, jobject obj, jobject stream)
{
    if (stream) {
        FontFile *fontFile = FontFile::createFromStream(JavaBridge(env), stream);
        return reinterpret_cast<jlong>(fontFile);
    }

    return 0;
}

static void release(JNIEnv *env, jobject obj, jlong fontFileHandle)
{
    auto fontFile = reinterpret_cast<FontFile *>(fontFileHandle);
    fontFile->release();
}

static jint getFaceCount(JNIEnv *env, jobject obj, jlong fontFileHandle)
{
    auto fontFile = reinterpret_cast<FontFile *>(fontFileHandle);
    FT_Long numFaces = fontFile->numFaces();

    return static_cast<jint>(numFaces);
}

static jint getInstanceCount(JNIEnv *env, jobject obj, jobject jtypeface)
{
    jlong typefaceHandle = JavaBridge(env).Typeface_getNativeTypeface(jtypeface);
    auto typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_Long numInstances = baseFace->style_flags >> 16;

    return static_cast<jint>(numInstances);
}

static jobject createTypeface(JNIEnv *env, jobject obj,
    jlong fontFileHandle, jint faceIndex, jint instanceIndex)
{
    auto fontFile = reinterpret_cast<FontFile *>(fontFileHandle);
    Typeface *typeface = Typeface::createFromFile(fontFile, faceIndex, instanceIndex);

    if (typeface) {
        auto typefaceHandle = reinterpret_cast<jlong>(typeface);
        JavaBridge bridge(env);

        return bridge.Typeface_construct(typefaceHandle);
    }

    return nullptr;
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreateFromAsset", "(Landroid/content/res/AssetManager;Ljava/lang/String;)J", (void *)createFromAsset },
    { "nCreateFromPath", "(Ljava/lang/String;)J", (void *)createFromPath },
    { "nCreateFromStream", "(Ljava/io/InputStream;)J", (void *)createFromStream },
    { "nRelease", "(J)V", (void *)release },
    { "nGetFaceCount", "(J)I", (void *)getFaceCount },
    { "nGetInstanceCount", "(Lcom/mta/tehreer/graphics/Typeface;)I", (void *)getInstanceCount },
    { "nCreateTypeface", "(JII)Lcom/mta/tehreer/graphics/Typeface;", (void *)createTypeface },
};

jint register_com_mta_tehreer_font_FontFile(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/font/FontFile", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
