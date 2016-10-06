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
#include <ft2build.h>
#include FT_ADVANCES_H
#include FT_FREETYPE_H
#include FT_STROKER_H
#include FT_SYSTEM_H
#include FT_TRUETYPE_TABLES_H
#include FT_TYPES_H

#include <SFFont.h>
}

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <cstdlib>
#include <jni.h>
#include <mutex>

#include "FreeType.h"
#include "JavaBridge.h"
#include "Miscellaneous.h"
#include "StreamUtils.h"
#include "Typeface.h"

using namespace Tehreer;

static void protocolLoadTable(void *object, SFTag tag, SFUInt8 *buffer, SFUInteger *length)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(object);

    FT_Face ftFace;
    typeface->lockFreetypeFace(&ftFace);

    FT_ULong size = 0;
    FT_Load_Sfnt_Table(ftFace, tag, 0, buffer, length ? &size : nullptr);

    typeface->unlockFreetypeFace();

    if (length) {
        *length = size;
    }
}

static SFGlyphID protocolGetGlyphIDForCodepoint(void *object, SFCodepoint codepoint)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(object);

    FT_Face ftFace;
    typeface->lockFreetypeFace(&ftFace);

    FT_UInt glyphID = FT_Get_Char_Index(ftFace, codepoint);

    typeface->unlockFreetypeFace();

    if (glyphID > 0xFFFF) {
        LOGW("Received invalid glyph id for code point: %u", codepoint);
        glyphID = 0;
    }

    return (SFGlyphID)glyphID;
}

static SFAdvance protocolGetAdvanceForGlyph(void *object, SFFontLayout fontLayout, SFGlyphID glyphID)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(object);

    FT_Int32 flags = FT_LOAD_NO_SCALE;
    if (fontLayout == SFFontLayoutVertical) {
        flags |= FT_LOAD_VERTICAL_LAYOUT;
    }

    FT_Face ftFace;
    typeface->lockFreetypeFace(&ftFace);

    FT_Fixed advance = 0;
    FT_Get_Advance(ftFace, glyphID, flags, &advance);

    typeface->unlockFreetypeFace();

    // TODO: Cache the advances.
    
    return (SFAdvance)advance;
}

static unsigned long assetStreamRead(FT_Stream assetStream, unsigned long offset, unsigned char *buffer,
                                     unsigned long count)
{
    if (!count && offset > assetStream->size) {
        return 1;
    }

    AAsset *asset = (AAsset *)assetStream->descriptor.pointer;

    if (assetStream->pos != offset) {
        AAsset_seek(asset, offset, SEEK_SET);
    }

    return (unsigned long)AAsset_read(asset, buffer, count);
}

static void assetStreamClose(FT_Stream assetStream)
{
    AAsset *asset = (AAsset *)assetStream->descriptor.pointer;
    AAsset_close(asset);

    assetStream->descriptor.pointer = nullptr;
    assetStream->size = 0;
    assetStream->base = 0;
}

static FT_Stream assetStreamCreate(AAssetManager *assetManager, const char *path)
{
    AAsset *asset = AAssetManager_open(assetManager, path, AASSET_MODE_UNKNOWN);
    if (!asset) {
        return nullptr;
    }

    off_t size = AAsset_getLength(asset);
    if (size == 0) {
        return nullptr;
    }

    FT_Stream assetStream;
    assetStream = (FT_Stream)malloc(sizeof(*assetStream));
    assetStream->base = nullptr;
    assetStream->size = size;
    assetStream->pos = 0;
    assetStream->descriptor.pointer = asset;
    assetStream->pathname.pointer = nullptr;
    assetStream->read = assetStreamRead;
    assetStream->close = assetStreamClose;

    return assetStream;
}

static void assetStreamDispose(FT_Stream assetStream)
{
    free(assetStream);
}

Typeface *Typeface::createWithAsset(AAssetManager *assetManager, const char *path)
{
    FT_Stream stream = assetStreamCreate(assetManager, path);
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

Typeface *Typeface::createWithFile(const char *path)
{
    FT_Open_Args args;
    args.flags = FT_OPEN_PATHNAME;
    args.memory_base = nullptr;
    args.memory_size = 0;
    args.pathname = (FT_String *)path;
    args.stream = nullptr;

    return createWithArgs(&args);
}

Typeface *Typeface::createFromStream(const JavaBridge &bridge, jobject stream)
{
    jint length;
    void *buffer = StreamUtils::toRawBuffer(bridge, stream, &length);

    if (buffer) {
        FT_Open_Args args;
        args.flags = FT_OPEN_MEMORY;
        args.memory_base = (const FT_Byte *)buffer;
        args.memory_size = length;
        args.pathname = nullptr;
        args.stream = nullptr;

        return createWithArgs(&args);
    }

    return nullptr;
}

Typeface *Typeface::createWithArgs(const FT_Open_Args *args)
{
    std::mutex &mutex = FreeType::mutex();
    mutex.lock();

    FT_Face ftFace = nullptr;
    FT_Error error = FT_Open_Face(FreeType::library(), args, 0, &ftFace);
    if (error == FT_Err_Ok) {
        if (!FT_IS_SCALABLE(ftFace)) {
            FT_Done_Face(ftFace);
            ftFace = nullptr;
        }
    }

    mutex.unlock();

    return (ftFace ? new Typeface((void *)args->memory_base, args->stream, ftFace) : nullptr);
}

Typeface::Typeface(void *buffer, FT_Stream ftStream, FT_Face ftFace)
{
    SFFontProtocol protocol;
    protocol.finalize = nullptr;
    protocol.loadTable = &protocolLoadTable;
    protocol.getGlyphIDForCodepoint = &protocolGetGlyphIDForCodepoint;
    protocol.getAdvanceForGlyph = &protocolGetAdvanceForGlyph;

    m_buffer = buffer;
    m_ftStream = ftStream;
    m_ftFace = ftFace;
    m_ftStroker = nullptr;
    m_sfFont = SFFontCreateWithProtocol(&protocol, this);
}

Typeface::~Typeface()
{
    SFFontRelease(m_sfFont);

    if (m_ftStroker) {
        FT_Stroker_Done(m_ftStroker);
    }

    if (m_ftFace) {
        std::mutex &mutex = FreeType::mutex();
        mutex.lock();

        FT_Done_Face(m_ftFace);

        mutex.unlock();
    }

    if (m_ftStream) {
        assetStreamDispose(m_ftStream);
    }

    if (m_buffer) {
        free(m_buffer);
    }
}

void Typeface::lockFreetypeFace(FT_Face *ftFace)
{
    m_mutex.lock();
    *ftFace = m_ftFace;
}

void Typeface::unlockFreetypeFace()
{
    m_mutex.unlock();
}

FT_Stroker Typeface::freetypeStroker()
{
    /*
     * NOTE:
     *      The caller is responsible to lock the mutex.
     */

    if (!m_ftStroker) {
        /*
         * There is no need to lock 'library' as it is only taken to have access to FreeType's
         * memory handling functions.
         */
        FT_Stroker_New(FreeType::library(), &m_ftStroker);
    }

    return m_ftStroker;
}

static jlong createWithAsset(JNIEnv *env, jobject obj, jobject assetManager, jstring path)
{
    if (path) {
        const char *utfChars = env->GetStringUTFChars(path, nullptr);
        AAssetManager *nativeAssetManager = AAssetManager_fromJava(env, assetManager);
        Typeface *typeface = Typeface::createWithAsset(nativeAssetManager, utfChars);

        env->ReleaseStringUTFChars(path, utfChars);

        return reinterpret_cast<jlong>(typeface);
    }

    return 0;
}

static jlong createWithFile(JNIEnv *env, jobject obj, jstring path)
{
    if (path) {
        const char *utfChars = env->GetStringUTFChars(path, nullptr);
        Typeface *typeface = Typeface::createWithFile(utfChars);

        env->ReleaseStringUTFChars(path, utfChars);

        return reinterpret_cast<jlong>(typeface);
    }

    return 0;
}

static jlong createFromStream(JNIEnv *env, jobject obj, jobject stream)
{
    if (stream) {
        Typeface *typeface = Typeface::createFromStream(JavaBridge(env), stream);
        return reinterpret_cast<jlong>(typeface);
    }

    return 0;
}

static void dispose(JNIEnv *env, jobject obj, jlong handle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(handle);
    delete typeface;
}

static jbyteArray copyTable(JNIEnv *env, jobject obj, jlong handle, jint tableTag)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(handle);

    SFUInteger length;
    protocolLoadTable(nullptr, tableTag, nullptr, &length);

    if (length > 0) {
        jbyteArray dataArray = env->NewByteArray(length);
        void *dataBuffer = env->GetPrimitiveArrayCritical(dataArray, nullptr);

        protocolLoadTable(nullptr, tableTag, (SFUInt8 *)dataBuffer, nullptr);

        env->ReleasePrimitiveArrayCritical(dataArray, dataBuffer, 0);

        return dataArray;
    }

    return nullptr;
}

static jint getUnitsPerEm(JNIEnv *env, jobject obj, jlong handle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(handle);
    return typeface->unitsPerEm();
}

static jint getAscent(JNIEnv *env, jobject obj, jlong handle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(handle);
    return typeface->ascender();
}

static jint getDescent(JNIEnv *env, jobject obj, jlong handle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(handle);
    return -typeface->descender();
}

static jint getGlyphCount(JNIEnv *env, jobject obj, jlong handle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(handle);
    return typeface->glyphCount();
}

static void getBoundingBox(JNIEnv *env, jobject obj, jlong handle, jobject rect)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(handle);
    JavaBridge bridge(env);

    FT_BBox bbox = typeface->boundingBox();
    bridge.Rect_set(rect, bbox.xMin, bbox.yMin, bbox.xMax, bbox.yMax);
}

static jint getUnderlinePosition(JNIEnv *env, jobject obj, jlong handle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(handle);
    return typeface->underlinePosition();
}

static jint getUnderlineThickness(JNIEnv *env, jobject obj, jlong handle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(handle);
    return typeface->underlineThickness();
}

static JNINativeMethod JNI_METHODS[] = {
    { "nativeCreateWithAsset", "(Landroid/content/res/AssetManager;Ljava/lang/String;)J", (void *)createWithAsset },
    { "nativeCreateWithFile", "(Ljava/lang/String;)J", (void *)createWithFile },
    { "nativeCreateFromStream", "(Ljava/io/InputStream;)J", (void *)createFromStream },
    { "nativeDispose", "(J)V", (void *)dispose },
    { "nativeCopyTable", "(JI)[B", (void *)copyTable },
    { "nativeGetUnitsPerEm", "(J)I", (void *)getUnitsPerEm },
    { "nativeGetAscent", "(J)I", (void *)getAscent },
    { "nativeGetDescent", "(J)I", (void *)getDescent },
    { "nativeGetGlyphCount", "(J)I", (void *)getGlyphCount },
    { "nativeGetBoundingBox", "(JLandroid/graphics/Rect;)V", (void *)getBoundingBox },
    { "nativeGetUnderlinePosition", "(J)I", (void *)getUnderlinePosition },
    { "nativeGetUnderlineThickness", "(J)I", (void *)getUnderlineThickness },
};

jint register_com_mta_tehreer_graphics_Typeface(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/Typeface", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
