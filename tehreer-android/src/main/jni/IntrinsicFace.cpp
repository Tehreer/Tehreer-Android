/*
 * Copyright (C) 2021 Muhammad Tayyab Akram
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
#include FT_MULTIPLE_MASTERS_H
#include FT_SFNT_NAMES_H
#include FT_SIZES_H
#include FT_STROKER_H
#include FT_TRUETYPE_TABLES_H
}

#include <cmath>
#include <cstdint>
#include <cstring>
#include <mutex>

#include "Convert.h"
#include "FontFile.h"
#include "FreeType.h"
#include "RenderableFace.h"
#include "SfntTables.h"
#include "ShapableFace.h"
#include "IntrinsicFace.h"

using namespace std;
using namespace Tehreer;
using namespace Tehreer::SFNT::head;
using namespace Tehreer::SFNT::name;
using namespace Tehreer::SFNT::OS2;

using FaceLock = lock_guard<RenderableFace>;

/**
 * NOTE: The caller needs to lock the typeface before invoking this function.
 */
static int32_t searchEnglishNameRecordIndex(FT_Face face, uint16_t nameID)
{
    FT_UInt nameCount = FT_Get_Sfnt_Name_Count(face);
    int32_t candidate = -1;

    for (FT_UInt i = 0; i < nameCount; i++) {
        FT_SfntName record;
        FT_Get_Sfnt_Name(face, i, &record);

        if (record.name_id != nameID) {
            continue;
        }

        Locale locale(record.platform_id, record.language_id);
        const string *language = locale.language();

        if (language && *language == "en") {
            const string *region = locale.region();

            if (record.platform_id == PlatformID::WINDOWS && region && *region == "US") {
                return static_cast<int32_t>(i);
            }

            if (candidate == -1 || record.platform_id == PlatformID::MACINTOSH) {
                candidate = static_cast<int32_t>(i);
            }
        }
    }

    return candidate;
}

static int32_t searchFamilyNameRecordIndex(FT_Face face, TT_OS2 *os2Table)
{
    int32_t familyName = -1;

    if (os2Table && (os2Table->fsSelection & FSSelection::WWS)) {
        familyName = searchEnglishNameRecordIndex(face, NameID::WWS_FAMILY);
    }
    if (familyName == -1) {
        familyName = searchEnglishNameRecordIndex(face, NameID::TYPOGRAPHIC_FAMILY);
    }
    if (familyName == -1) {
        familyName = searchEnglishNameRecordIndex(face, NameID::FONT_FAMILY);
    }

    return familyName;
}

static int32_t searchStyleNameRecordIndex(FT_Face face, TT_OS2 *os2Table)
{
    int32_t styleName = -1;

    if (os2Table && (os2Table->fsSelection & FSSelection::WWS)) {
        styleName = searchEnglishNameRecordIndex(face, NameID::WWS_SUBFAMILY);
    }
    if (styleName == -1) {
        styleName = searchEnglishNameRecordIndex(face, NameID::TYPOGRAPHIC_SUBFAMILY);
    }
    if (styleName == -1) {
        styleName = searchEnglishNameRecordIndex(face, NameID::FONT_SUBFAMILY);
    }

    return styleName;
}

static int32_t searchFullNameRecordIndex(FT_Face face)
{
    return searchEnglishNameRecordIndex(face, NameID::FULL);
}

static inline uint16_t variableWeightToStandard(float value)
{
    if (value < 1) {
        return 1;
    }
    if (value > 1000) {
        return 1000;
    }

    return static_cast<uint16_t>(value);
}

static inline uint16_t variableWidthToStandard(float value)
{
    if (value < 50) {
        return 1;
    }
    if (value < 125) {
        return static_cast<uint16_t>(((value - 50) / 12.5) + 1);
    }
    if (value < 200) {
        return static_cast<uint16_t>(((value - 125) / 25) + 7);
    }

    return 9;
}

static inline IntrinsicFace::Slope variableItalicToSlope(float value)
{
    return value >= 1.0 ? IntrinsicFace::Slope::ITALIC : IntrinsicFace::Slope::PLAIN;
}

static inline IntrinsicFace::Slope variableSlantToSlope(float value)
{
    return value != 0.0 ? IntrinsicFace::Slope::OBLIQUE : IntrinsicFace::Slope::PLAIN;
}

IntrinsicFace &IntrinsicFace::create(RenderableFace &renderableFace)
{
    auto instance = new IntrinsicFace(renderableFace);
    return *instance;
}

IntrinsicFace::IntrinsicFace(RenderableFace &renderableFace)
    : m_renderableFace(renderableFace.retain())
    , m_ftSize(nullptr)
    , m_ftStroker(nullptr)
    , m_shapableFace(nullptr)
    , m_strikeoutPosition(0)
    , m_strikeoutThickness(0)
    , m_retainCount(1)
{
    setupSize();
    setupDescription();
    setupStrikeout();
    setupHarfBuzz();
}

IntrinsicFace::IntrinsicFace(const IntrinsicFace &parent, RenderableFace &renderableFace)
    : m_renderableFace(renderableFace.retain())
    , m_ftSize(nullptr)
    , m_ftStroker(nullptr)
    , m_shapableFace(nullptr)
    , m_strikeoutPosition(0)
    , m_strikeoutThickness(0)
    , m_retainCount(1)
{
    m_defaults = parent.m_defaults;

    setupSize();
    setupStrikeout();
    setupHarfBuzz(parent.m_shapableFace);
}

void IntrinsicFace::setupCoordinates(const float *coordArray, size_t coordCount)
{
    m_renderableFace.setupCoordinates(coordArray, coordCount);
}

void IntrinsicFace::setupSize()
{
    FT_New_Size(m_renderableFace.ftFace(), &m_ftSize);
}

void IntrinsicFace::setupDescription()
{
    FT_Face ftFace = m_renderableFace.ftFace();
    auto os2Table = static_cast<TT_OS2 *>(FT_Get_Sfnt_Table(ftFace, FT_SFNT_OS2));
    auto headTable = static_cast<TT_Header *>(FT_Get_Sfnt_Table(ftFace, FT_SFNT_HEAD));

    Description description;
    description.familyName = searchFamilyNameRecordIndex(ftFace, os2Table);
    description.styleName = searchStyleNameRecordIndex(ftFace, os2Table);
    description.fullName = searchFullNameRecordIndex(ftFace);

    if (os2Table) {
        description.weight = os2Table->usWeightClass;
        description.width = os2Table->usWidthClass;

        if (os2Table->fsSelection & FSSelection::OBLIQUE) {
            description.slope = Slope::OBLIQUE;
        } else if (os2Table->fsSelection & FSSelection::ITALIC) {
            description.slope = Slope::ITALIC;
        }
    } else if (headTable) {
        if (headTable->Mac_Style & MacStyle::BOLD) {
            description.weight = Weight::BOLD;
        }

        if (headTable->Mac_Style & MacStyle::CONDENSED) {
            description.width = Width::CONDENSED;
        } else if (headTable->Mac_Style & MacStyle::EXTENDED) {
            description.width = Width::EXPANDED;
        }

        if (headTable->Mac_Style & MacStyle::ITALIC) {
            description.slope = Slope::ITALIC;
        }
    }

    m_defaults.description = description;
    m_description = description;
}

void IntrinsicFace::setupStrikeout()
{
    FT_Face ftFace = m_renderableFace.ftFace();
    auto os2Table = static_cast<TT_OS2 *>(FT_Get_Sfnt_Table(ftFace, FT_SFNT_OS2));

    if (os2Table) {
        m_strikeoutPosition = os2Table->yStrikeoutPosition;
        m_strikeoutThickness = os2Table->yStrikeoutSize;
    }
}

void IntrinsicFace::setupHarfBuzz(ShapableFace *parent)
{
    if (parent) {
        m_shapableFace = &parent->deriveVariation(m_renderableFace);
    } else {
        m_shapableFace = &ShapableFace::create(m_renderableFace);
    }
}

void IntrinsicFace::setupVariation(float italValue, float slntValue, float wdthValue, float wghtValue)
{
    if (!isnan(italValue)) {
        m_description.slope = variableItalicToSlope(italValue);
    }
    if (!isnan(slntValue)) {
        m_description.slope = variableSlantToSlope(slntValue);
    }
    if (!isnan(wdthValue)) {
        m_description.width = variableWidthToStandard(wdthValue);
    }
    if (!isnan(wghtValue)) {
        m_description.weight = variableWeightToStandard(wghtValue);
    }
}

IntrinsicFace::~IntrinsicFace()
{
    m_shapableFace->release();

    if (m_ftStroker) {
        FT_Stroker_Done(m_ftStroker);
    }
    if (m_ftSize) {
        FaceLock lock(m_renderableFace);
        FT_Done_Size(m_ftSize);
    }

    m_renderableFace.release();
}

IntrinsicFace &IntrinsicFace::retain()
{
    m_retainCount++;
    return *this;
}

void IntrinsicFace::release()
{
    if (--m_retainCount == 0) {
        delete this;
    }
}

IntrinsicFace *IntrinsicFace::deriveVariation(const float *coordArray, size_t coordCount)
{
    RenderableFace *renderableFace = m_renderableFace.deriveVariation(coordArray, coordCount);
    if (!renderableFace) {
        return nullptr;
    }

    auto instance = new IntrinsicFace(*this, *renderableFace);

    renderableFace->release();

    return instance;
}

size_t IntrinsicFace::getTableLength(uint32_t tag)
{
    FaceLock lock(m_renderableFace);
    FT_Face ftFace = m_renderableFace.ftFace();

    FT_ULong length = 0;
    FT_Load_Sfnt_Table(ftFace, tag, 0, nullptr, &length);

    return length;
}

void IntrinsicFace::getTableData(uint32_t tag, void *buffer)
{
    FaceLock lock(m_renderableFace);
    FT_Face ftFace = m_renderableFace.ftFace();

    auto ftBuffer = reinterpret_cast<FT_Byte *>(buffer);
    FT_Load_Sfnt_Table(ftFace, tag, 0, ftBuffer, nullptr);
}

int32_t IntrinsicFace::searchNameRecordIndex(uint16_t nameID)
{
    FaceLock lock(m_renderableFace);

    FT_Face ftFace = m_renderableFace.ftFace();
    int32_t recordIndex = searchEnglishNameRecordIndex(ftFace, nameID);

    return recordIndex;
}

uint16_t IntrinsicFace::getGlyphID(uint32_t codePoint)
{
    FaceLock lock(m_renderableFace);

    FT_Face ftFace = m_renderableFace.ftFace();
    FT_UInt glyphID = FT_Get_Char_Index(ftFace, codePoint);

    return static_cast<uint16_t>(glyphID);
}

float IntrinsicFace::getGlyphAdvance(uint16_t glyphID, float typeSize, bool vertical)
{
    FT_Int32 loadFlags = FT_LOAD_DEFAULT;
    if (vertical) {
        loadFlags |= FT_LOAD_VERTICAL_LAYOUT;
    }

    FaceLock lock(m_renderableFace);
    FT_Face ftFace = m_renderableFace.ftFace();

    FT_Activate_Size(ftSize());
    FT_Set_Char_Size(ftFace, 0, toF26Dot6(typeSize), 0, 0);
    FT_Set_Transform(ftFace, nullptr, nullptr);

    FT_Fixed advance;
    FT_Get_Advance(ftFace, glyphID, loadFlags, &advance);

    return f16Dot16toFloat(advance);
}

jobject IntrinsicFace::unsafeGetGlyphPath(JavaBridge bridge, uint16_t glyphID)
{
    jobject glyphPath = nullptr;

    FT_Error error = FT_Load_Glyph(ftFace(), glyphID, FT_LOAD_NO_BITMAP);
    if (error == FT_Err_Ok) {
        struct PathContext {
            JavaBridge bridge;
            jobject path;
        };

        FT_Outline_Funcs funcs;
        funcs.move_to = [](const FT_Vector *to, void *user) -> int
        {
            auto context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_moveTo(context->path,
                                        f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.line_to = [](const FT_Vector *to, void *user) -> int
        {
            auto context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_lineTo(context->path,
                                        f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.conic_to = [](const FT_Vector *control1, const FT_Vector *to, void *user) -> int
        {
            auto context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_quadTo(context->path,
                                        f26Dot6PosToFloat(control1->x), f26Dot6PosToFloat(control1->y),
                                        f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.cubic_to = [](const FT_Vector *control1, const FT_Vector *control2, const FT_Vector *to, void *user) -> int
        {
            auto context = reinterpret_cast<PathContext *>(user);
            context->bridge.Path_cubicTo(context->path,
                                         f26Dot6PosToFloat(control1->x), f26Dot6PosToFloat(control1->y),
                                         f26Dot6PosToFloat(control2->x), f26Dot6PosToFloat(control2->y),
                                         f26Dot6PosToFloat(to->x), f26Dot6PosToFloat(to->y));
            return 0;
        };
        funcs.shift = 0;
        funcs.delta = 0;

        PathContext context = { bridge, bridge.Path_construct() };

        FT_Outline *outline = &ftFace()->glyph->outline;
        error = FT_Outline_Decompose(outline, &funcs, &context);
        if (error == FT_Err_Ok) {
            glyphPath = context.path;
        }
    }

    return glyphPath;
}

jobject IntrinsicFace::getGlyphPath(JavaBridge bridge, uint16_t glyphID, float typeSize, float *transform)
{
    FT_Matrix matrix;
    FT_Vector delta;

    if (!transform) {
        matrix = { 0x10000, 0, 0, -0x10000 };
        delta = { 0, 0 };
    } else {
        FT_Matrix actual = {
            toF16Dot16(transform[0]), toF16Dot16(transform[1]),
            toF16Dot16(transform[3]), toF16Dot16(transform[4]),
        };
        FT_Matrix flip = { 1, 0, 0, -1 };

        matrix = {
            (actual.xx * flip.xx) + (actual.xy * flip.yx), (actual.xx * flip.xy) + (actual.xy * flip.yy),
            (actual.yx * flip.xx) + (actual.yy * flip.yx), (actual.yx * flip.xy) + (actual.yy * flip.yy)
        };
        delta = {
            toF26Dot6(transform[2]), toF26Dot6(transform[5]),
        };
    }

    FaceLock lock(m_renderableFace);
    FT_Face ftFace = m_renderableFace.ftFace();

    FT_Activate_Size(ftSize());
    FT_Set_Char_Size(ftFace, 0, toF26Dot6(typeSize), 0, 0);
    FT_Set_Transform(ftFace, &matrix, &delta);

    return unsafeGetGlyphPath(bridge, glyphID);
}

FT_Stroker IntrinsicFace::ftStroker()
{
    if (!m_ftStroker) {
        m_mutex.lock();

        if (!m_ftStroker) {
            /*
             * There is no need to lock 'library' as it is only taken to have access to FreeType's
             * memory handling functions.
             */
            FT_Stroker_New(FreeType::library(), &m_ftStroker);
        }

        m_mutex.unlock();
    }

    return m_ftStroker;
}
