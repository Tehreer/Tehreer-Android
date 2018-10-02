/*
 * Copyright (C) 2016-2018 Muhammad Tayyab Akram
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
#include FT_SIZES_H
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

static inline FT_F26Dot6 toF26Dot6(float value)
{
    return static_cast<FT_F26Dot6>((value * 64) + 0.5);
}

static inline FT_Fixed toF16Dot16(float value)
{
    return static_cast<FT_Fixed>((value * 0x10000) + 0.5);
}

static inline float f16Dot16toFloat(FT_Fixed value)
{
    return value / static_cast<float>(0x10000);
}

static inline float f26Dot6PosToFloat(FT_Pos value)
{
    return static_cast<float>(value / 64.0);
}

struct PathContext {
    const JavaBridge *bridge;
    jobject path;
};

static int processMoveTo(const FT_Vector *to, void *user)
{
    PathContext *context = reinterpret_cast<PathContext *>(user);
    context->bridge->Path_moveTo(context->path,
                                 f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
    return 0;
}

static int processLineTo(const FT_Vector *to, void *user)
{
    PathContext *context = reinterpret_cast<PathContext *>(user);
    context->bridge->Path_lineTo(context->path,
                                 f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
    return 0;
}

static int processQuadTo(const FT_Vector *control1, const FT_Vector *to, void *user)
{
    PathContext *context = reinterpret_cast<PathContext *>(user);
    context->bridge->Path_quadTo(context->path,
                                 f26Dot6PosToFloat(control1->x), f26Dot6PosToFloat(control1->y),
                                 f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
    return 0;
}

static int processCubicTo(const FT_Vector *control1, const FT_Vector *control2, const FT_Vector *to, void *user)
{
    PathContext *context = reinterpret_cast<PathContext *>(user);
    context->bridge->Path_cubicTo(context->path,
                                  f26Dot6PosToFloat(control1->x), f26Dot6PosToFloat(control1->y),
                                  f26Dot6PosToFloat(control2->x), f26Dot6PosToFloat(control2->y),
                                  f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
    return 0;
}

static unsigned long assetStreamRead(FT_Stream assetStream,
    unsigned long offset, unsigned char *buffer, unsigned long count)
{
    AAsset *asset = static_cast<AAsset *>(assetStream->descriptor.pointer);
    int bytesRead = 0;

    if (!count && offset > assetStream->size) {
        return 1;
    }

    if (assetStream->pos != offset) {
        AAsset_seek(asset, offset, SEEK_SET);
    }
    bytesRead = AAsset_read(asset, buffer, count);

    return static_cast<unsigned long>(bytesRead);
}

static void assetStreamClose(FT_Stream assetStream)
{
    AAsset *asset = static_cast<AAsset *>(assetStream->descriptor.pointer);
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
    assetStream->size = static_cast<unsigned long>(size);
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
    args.pathname = const_cast<FT_String *>(path);
    args.stream = nullptr;

    return createWithArgs(&args);
}

Typeface *Typeface::createFromStream(const JavaBridge &bridge, jobject stream)
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
    : m_strikeoutPosition(0)
    , m_strikeoutThickness(0)
{
    SFFontProtocol protocol;
    protocol.finalize = nullptr;
    protocol.loadTable = [](void *object, SFTag tag, SFUInt8 *buffer, SFUInteger *length) {
        Typeface *typeface = reinterpret_cast<Typeface *>(object);
        FT_ULong tableSize = 0;

        typeface->loadSfntTable(tag, buffer, length ? &tableSize : nullptr);

        if (length) {
            *length = tableSize;
        }
    };
    protocol.getGlyphIDForCodepoint = [](void *object, SFCodepoint codepoint) {
        Typeface *typeface = reinterpret_cast<Typeface *>(object);
        FT_UInt glyphID = typeface->getGlyphID(codepoint);
        if (glyphID > 0xFFFF) {
            LOGW("Received invalid glyph id for code point: %u", codepoint);
            glyphID = 0;
        }

        return static_cast<SFGlyphID>(glyphID);
    };
    protocol.getAdvanceForGlyph = [](void *object, SFFontLayout fontLayout, SFGlyphID glyphID)
    {
        Typeface *typeface = reinterpret_cast<Typeface *>(object);
        FT_Fixed glyphAdvance = typeface->getGlyphAdvance(glyphID, fontLayout == SFFontLayoutVertical);

        return static_cast<SFInt32>(glyphAdvance);
    };

    m_buffer = buffer;
    m_ftStream = ftStream;
    m_ftFace = ftFace;
    m_ftSize = nullptr;
    m_ftStroker = nullptr;
    m_sfFont = SFFontCreateWithProtocol(&protocol, this);

    FT_New_Size(m_ftFace, &m_ftSize);

    TT_OS2 *os2Table = static_cast<TT_OS2 *>(FT_Get_Sfnt_Table(ftFace, FT_SFNT_OS2));
    if (os2Table) {
        m_strikeoutPosition = os2Table->yStrikeoutPosition;
        m_strikeoutThickness = os2Table->yStrikeoutSize;
    }
}

Typeface::~Typeface()
{
    SFFontRelease(m_sfFont);

    if (m_ftStroker) {
        FT_Stroker_Done(m_ftStroker);
    }

    if (m_ftSize) {
        m_mutex.lock();

        FT_Done_Size(m_ftSize);

        m_mutex.unlock();
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

FT_Stroker Typeface::ftStroker()
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

void Typeface::loadSfntTable(FT_ULong tag, FT_Byte *buffer, FT_ULong *length)
{
    m_mutex.lock();

    FT_Load_Sfnt_Table(m_ftFace, tag, 0, buffer, length);

    m_mutex.unlock();
}

FT_UInt Typeface::getGlyphID(FT_ULong codePoint)
{
    m_mutex.lock();

    FT_UInt glyphID = FT_Get_Char_Index(m_ftFace, codePoint);

    m_mutex.unlock();

    return glyphID;
}

FT_Fixed Typeface::getGlyphAdvance(FT_UInt glyphID, bool vertical)
{
    FT_Int32 loadFlags = FT_LOAD_NO_SCALE;
    if (vertical) {
        loadFlags |= FT_LOAD_VERTICAL_LAYOUT;
    }

    m_mutex.lock();

    FT_Fixed advance;
    FT_Get_Advance(m_ftFace, glyphID, loadFlags, &advance);

    m_mutex.unlock();

    return advance;
}

FT_Fixed Typeface::getGlyphAdvance(FT_UInt glyphID, FT_F26Dot6 typeSize, bool vertical)
{
    FT_Int32 loadFlags = FT_LOAD_DEFAULT;
    if (vertical) {
        loadFlags |= FT_LOAD_VERTICAL_LAYOUT;
    }

    m_mutex.lock();

    FT_Activate_Size(m_ftSize);
    FT_Set_Char_Size(m_ftFace, 0, typeSize, 0, 0);
    FT_Set_Transform(m_ftFace, nullptr, nullptr);

    FT_Fixed advance;
    FT_Get_Advance(m_ftFace, glyphID, loadFlags, &advance);

    m_mutex.unlock();

    return advance;
}

jobject Typeface::getGlyphPathNoLock(JavaBridge bridge, FT_UInt glyphID)
{
    jobject glyphPath = nullptr;

    FT_Error error = FT_Load_Glyph(m_ftFace, glyphID, FT_LOAD_NO_BITMAP);
    if (error == FT_Err_Ok) {
        FT_Outline_Funcs funcs;
        funcs.move_to = processMoveTo;
        funcs.line_to = processLineTo;
        funcs.conic_to = processQuadTo;
        funcs.cubic_to = processCubicTo;
        funcs.shift = 0;
        funcs.delta = 0;

        PathContext pathContext;
        pathContext.bridge = &bridge;
        pathContext.path = bridge.Path_construct();

        FT_Outline *outline = &m_ftFace->glyph->outline;
        error = FT_Outline_Decompose(outline, &funcs, &pathContext);
        if (error == FT_Err_Ok) {
            glyphPath = pathContext.path;
        }
    }

    return glyphPath;
}

jobject Typeface::getGlyphPath(JavaBridge bridge, FT_UInt glyphID, FT_F26Dot6 typeSize, FT_Matrix *matrix, FT_Vector *delta)
{
    jobject glyphPath = nullptr;

    m_mutex.lock();

    FT_Activate_Size(m_ftSize);
    FT_Set_Char_Size(m_ftFace, 0, typeSize, 0, 0);
    FT_Set_Transform(m_ftFace, matrix, delta);

    glyphPath = getGlyphPathNoLock(bridge, glyphID);

    m_mutex.unlock();

    return glyphPath;
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

static void dispose(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    delete typeface;
}

static jbyteArray getTableData(JNIEnv *env, jobject obj, jlong typefaceHandle, jint tableTag)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_ULong inputTag = static_cast<SFTag>(tableTag);
    FT_ULong length = 0;

    typeface->loadSfntTable(inputTag, nullptr, &length);

    if (length > 0) {
        jint dataLength = static_cast<jint>(length);
        jbyteArray dataArray = env->NewByteArray(dataLength);
        void *dataBuffer = env->GetPrimitiveArrayCritical(dataArray, nullptr);

        FT_Byte *dataBytes = static_cast<FT_Byte *>(dataBuffer);
        typeface->loadSfntTable(inputTag, dataBytes, nullptr);

        env->ReleasePrimitiveArrayCritical(dataArray, dataBuffer, 0);

        return dataArray;
    }

    return nullptr;
}

static jint getUnitsPerEm(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_UShort unitsPerEm = baseFace->units_per_EM;

    return static_cast<jint>(unitsPerEm);
}

static jint getAscent(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_Short ascender = baseFace->ascender;

    return static_cast<jint>(ascender);
}

static jint getDescent(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_Short descender = baseFace->descender;

    return static_cast<jint>(-descender);
}

static jint getLeading(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_Short ascender = baseFace->ascender;
    FT_Short descender = baseFace->descender;
    FT_Short height = baseFace->height;

    return static_cast<jint>(height - (ascender - descender));
}

static jint getGlyphCount(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_Long glyphCount = baseFace->num_glyphs;

    return static_cast<jint>(glyphCount);
}

static jint getGlyphId(JNIEnv *env, jobject obj, jlong typefaceHandle, jint codePoint)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_UInt glyphId = typeface->getGlyphID(static_cast<FT_ULong>(codePoint));

    return static_cast<jint>(glyphId);
}

static jfloat getGlyphAdvance(JNIEnv *env, jobject obj, jlong typefaceHandle, jint glyphId, jfloat typeSize, jboolean vertical)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_UInt glyphIndex = static_cast<FT_UInt>(glyphId);
    FT_F26Dot6 fixedSize = toF26Dot6(typeSize);
    FT_Fixed advance = typeface->getGlyphAdvance(glyphIndex, fixedSize, vertical);

    return f16Dot16toFloat(advance);
}

static jobject getGlyphPath(JNIEnv *env, jobject obj, jlong typefaceHandle, jint glyphId, jfloat typeSize, jfloatArray matrixArray)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_UInt glyphIndex = static_cast<FT_UInt>(glyphId);
    FT_F26Dot6 fixedSize = toF26Dot6(typeSize);
    FT_Matrix transform;
    FT_Vector delta;

    if (!matrixArray) {
        transform = { 0x10000, 0, 0, -0x10000 };
        delta = { 0, 0 };
    } else {
        jfloat *matrixValues = env->GetFloatArrayElements(matrixArray, nullptr);

        FT_Matrix actual = {
            toF16Dot16(matrixValues[0]), toF16Dot16(matrixValues[1]),
            toF16Dot16(matrixValues[3]), toF16Dot16(matrixValues[4]),
        };
        FT_Matrix flip = { 1, 0, 0, -1 };

        transform = {
            (actual.xx * flip.xx) + (actual.xy * flip.yx), (actual.xx * flip.xy) + (actual.xy * flip.yy),
            (actual.yx * flip.xx) + (actual.yy * flip.yx), (actual.yx * flip.xy) + (actual.yy * flip.yy)
        };
        delta = {
            toF26Dot6(matrixValues[2]), toF26Dot6(matrixValues[5]),
        };

        env->ReleaseFloatArrayElements(matrixArray, matrixValues, 0);
    }

    return typeface->getGlyphPath(JavaBridge(env), glyphIndex, fixedSize, &transform, &delta);
}

static void getBoundingBox(JNIEnv *env, jobject obj, jlong typefaceHandle, jobject rect)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_BBox bbox = baseFace->bbox;

    JavaBridge(env).Rect_set(rect,
                             static_cast<jint>(bbox.xMin), static_cast<jint>(bbox.yMin),
                             static_cast<jint>(bbox.xMax), static_cast<jint>(bbox.yMax));
}

static jint getUnderlinePosition(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_Short underlinePosition = baseFace->underline_position;

    return static_cast<jint>(underlinePosition);
}

static jint getUnderlineThickness(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Face baseFace = typeface->ftFace();
    FT_Short underlineThickness = baseFace->underline_thickness;

    return static_cast<jint>(underlineThickness);
}

static jint getStrikeoutPosition(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Short strikeoutPosition = typeface->strikeoutPosition();

    return static_cast<jint>(strikeoutPosition);
}

static jint getStrikeoutThickness(JNIEnv *env, jobject obj, jlong typefaceHandle)
{
    Typeface *typeface = reinterpret_cast<Typeface *>(typefaceHandle);
    FT_Short strikeoutThickness = typeface->strikeoutThickness();

    return static_cast<jint>(strikeoutThickness);
}

static JNINativeMethod JNI_METHODS[] = {
    { "nCreateWithAsset", "(Landroid/content/res/AssetManager;Ljava/lang/String;)J", (void *)createWithAsset },
    { "nCreateWithFile", "(Ljava/lang/String;)J", (void *)createWithFile },
    { "nCreateFromStream", "(Ljava/io/InputStream;)J", (void *)createFromStream },
    { "nDispose", "(J)V", (void *)dispose },
    { "nGetTableData", "(JI)[B", (void *)getTableData },
    { "nGetUnitsPerEm", "(J)I", (void *)getUnitsPerEm },
    { "nGetAscent", "(J)I", (void *)getAscent },
    { "nGetDescent", "(J)I", (void *)getDescent },
    { "nGetLeading", "(J)I", (void *)getLeading },
    { "nGetGlyphCount", "(J)I", (void *)getGlyphCount },
    { "nGetGlyphId", "(JI)I", (void *)getGlyphId },
    { "nGetGlyphAdvance", "(JIFZ)F", (void *)getGlyphAdvance },
    { "nGetGlyphPath", "(JIF[F)Landroid/graphics/Path;", (void *)getGlyphPath },
    { "nGetBoundingBox", "(JLandroid/graphics/Rect;)V", (void *)getBoundingBox },
    { "nGetUnderlinePosition", "(J)I", (void *)getUnderlinePosition },
    { "nGetUnderlineThickness", "(J)I", (void *)getUnderlineThickness },
    { "nGetStrikeoutPosition", "(J)I", (void *)getStrikeoutPosition },
    { "nGetStrikeoutThickness", "(J)I", (void *)getStrikeoutThickness },
};

jint register_com_mta_tehreer_graphics_Typeface(JNIEnv *env)
{
    return JavaBridge::registerClass(env, "com/mta/tehreer/graphics/Typeface", JNI_METHODS, sizeof(JNI_METHODS) / sizeof(JNI_METHODS[0]));
}
