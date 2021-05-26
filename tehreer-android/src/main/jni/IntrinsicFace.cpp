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
    FT_Int candidate = -1;

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
                return i;
            }

            if (candidate == -1 || record.platform_id == PlatformID::MACINTOSH) {
                candidate = i;
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

static inline uint16_t variableWeightToStandard(FT_Fixed coordinate)
{
    float value = f16Dot16toFloat(coordinate);

    if (value < 1) {
        return 1;
    }
    if (value > 1000) {
        return 1000;
    }

    return static_cast<uint16_t>(value);
}

static inline uint16_t variableWidthToStandard(FT_Fixed coordinate)
{
    float value = f16Dot16toFloat(coordinate);

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

static inline IntrinsicFace::Slope variableItalicToSlope(FT_Fixed coordinate)
{
    return coordinate >= 0x10000 ? IntrinsicFace::Slope::ITALIC : IntrinsicFace::Slope::PLAIN;
}

static inline IntrinsicFace::Slope variableSlantToSlope(FT_Fixed coordinate)
{
    return coordinate != 0 ? IntrinsicFace::Slope::OBLIQUE : IntrinsicFace::Slope::PLAIN;
}

IntrinsicFace *IntrinsicFace::create(RenderableFace *renderableFace)
{
    return new IntrinsicFace(renderableFace);
}

IntrinsicFace::IntrinsicFace(RenderableFace *renderableFace)
    : m_renderableFace(renderableFace->retain())
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
    setupVariation();
    setupHarfBuzz();
}

IntrinsicFace::IntrinsicFace(IntrinsicFace *parent, FT_Fixed *coordArray, FT_UInt coordCount)
    : m_renderableFace(nullptr)
    , m_ftSize(nullptr)
    , m_ftStroker(nullptr)
    , m_shapableFace(nullptr)
    , m_strikeoutPosition(0)
    , m_strikeoutThickness(0)
    , m_retainCount(1)
{
    m_renderableFace = parent->renderableFace().deriveVariation(coordArray, coordCount);
    m_defaults = parent->m_defaults;

    setupSize();
    setupStrikeout();
    setupVariation();
    setupHarfBuzz();
}

void IntrinsicFace::setupSize()
{
    FT_New_Size(m_renderableFace->ftFace(), &m_ftSize);
}

void IntrinsicFace::setupDescription()
{
    FT_Face ftFace = m_renderableFace->ftFace();
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
    FT_Face ftFace = m_renderableFace->ftFace();
    auto os2Table = static_cast<TT_OS2 *>(FT_Get_Sfnt_Table(ftFace, FT_SFNT_OS2));

    if (os2Table) {
        m_strikeoutPosition = os2Table->yStrikeoutPosition;
        m_strikeoutThickness = os2Table->yStrikeoutSize;
    }
}

void IntrinsicFace::setupVariation()
{
    FT_Face ftFace = m_renderableFace->ftFace();

    FT_MM_Var *variation;
    FT_Error error = FT_Get_MM_Var(ftFace, &variation);

    if (error == FT_Err_Ok) {
        Description description;

        FT_UInt numCoords = variation->num_axis;
        FT_Fixed fixedCoords[numCoords];

        if (FT_Get_Var_Design_Coordinates(ftFace, numCoords, fixedCoords) == FT_Err_Ok) {
            // Reset the style name and the full name.
            description.styleName = -1;
            description.fullName = -1;

            // Get the style name of this instance.
            for (FT_UInt i = 0; i < variation->num_namedstyles; i++) {
                FT_Var_Named_Style *namedStyle = &variation->namedstyle[i];
                FT_Fixed *namedCoords = namedStyle->coords;

                int result = memcmp(namedCoords, fixedCoords, sizeof(FT_Fixed) * numCoords);
                if (result == 0) {
                    description.styleName = searchEnglishNameRecordIndex(ftFace, static_cast<uint16_t>(namedStyle->strid));
                    break;
                }
            }

            // Get the values of variation axes.
            for (FT_UInt i = 0; i < numCoords; i++) {
                FT_Var_Axis *axis = &variation->axis[i];

                switch (axis->tag) {
                case FT_MAKE_TAG('i', 't', 'a', 'l'):
                    description.slope = variableItalicToSlope(fixedCoords[i]);
                    break;

                case FT_MAKE_TAG('s', 'l', 'n', 't'):
                    description.slope = variableSlantToSlope(fixedCoords[i]);
                    break;

                case FT_MAKE_TAG('w', 'd', 't', 'h'):
                    description.width = variableWidthToStandard(fixedCoords[i]);
                    break;

                case FT_MAKE_TAG('w', 'g', 'h', 't'):
                    description.weight = variableWeightToStandard(fixedCoords[i]);
                    break;
                }
            }
        }

        FT_Done_MM_Var(FreeType::library(), variation);

        m_description = description;
    }
}

void IntrinsicFace::setupHarfBuzz(IntrinsicFace *parent)
{
    if (parent) {
        m_shapableFace = parent->shapableFace().deriveVariation(m_renderableFace);
    } else {
        m_shapableFace = ShapableFace::create(m_renderableFace);
    }
}

IntrinsicFace::~IntrinsicFace()
{
    m_shapableFace->release();

    if (m_ftStroker) {
        FT_Stroker_Done(m_ftStroker);
    }
    if (m_ftSize) {
        FaceLock lock(*m_renderableFace);
        FT_Done_Size(m_ftSize);
    }

    m_renderableFace->release();
}

IntrinsicFace *IntrinsicFace::retain()
{
    m_retainCount++;
    return this;
}

void IntrinsicFace::release()
{
    if (--m_retainCount == 0) {
        delete this;
    }
}

IntrinsicFace *IntrinsicFace::deriveVariation(FT_Fixed *coordArray, FT_UInt coordCount)
{
    return new IntrinsicFace(this, coordArray, coordCount);
}

void IntrinsicFace::loadSfntTable(FT_ULong tag, FT_Byte *buffer, FT_ULong *length)
{
    FaceLock lock(*m_renderableFace);

    FT_Face ftFace = m_renderableFace->ftFace();
    FT_Load_Sfnt_Table(ftFace, tag, 0, buffer, length);
}

int32_t IntrinsicFace::searchNameRecordIndex(uint16_t nameID)
{
    FaceLock lock(*m_renderableFace);

    FT_Face ftFace = m_renderableFace->ftFace();
    int32_t recordIndex = searchEnglishNameRecordIndex(ftFace, nameID);

    return recordIndex;
}

FT_UInt IntrinsicFace::getGlyphID(FT_ULong codePoint)
{
    FaceLock lock(*m_renderableFace);

    FT_Face ftFace = m_renderableFace->ftFace();
    FT_UInt glyphID = FT_Get_Char_Index(ftFace, codePoint);

    return glyphID;
}

float IntrinsicFace::getGlyphAdvance(uint16_t glyphID, float typeSize, bool vertical)
{
    FT_Int32 loadFlags = FT_LOAD_DEFAULT;
    if (vertical) {
        loadFlags |= FT_LOAD_VERTICAL_LAYOUT;
    }

    FaceLock lock(*m_renderableFace);
    FT_Face ftFace = m_renderableFace->ftFace();

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

    FaceLock lock(*m_renderableFace);
    FT_Face ftFace = m_renderableFace->ftFace();

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
