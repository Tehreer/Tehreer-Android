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

IntrinsicFace *IntrinsicFace::create(FontFile *fontFile, RenderableFace *renderableFace)
{
    if (fontFile && renderableFace) {
        return new IntrinsicFace(fontFile, renderableFace);
    }

    return nullptr;
}

IntrinsicFace::IntrinsicFace(FontFile *fontFile, RenderableFace *renderableFace)
    : m_fontFile(fontFile->retain())
    , m_renderableFace(renderableFace->retain())
    , m_ftSize(nullptr)
    , m_ftStroker(nullptr)
    , m_shapableFace(nullptr)
    , m_familyName(-1)
    , m_styleName(-1)
    , m_fullName(-1)
    , m_weight(Weight::REGULAR)
    , m_width(Width::NORMAL)
    , m_slope(Slope::PLAIN)
    , m_strikeoutPosition(0)
    , m_strikeoutThickness(0)
    , m_retainCount(1)
{
    FT_New_Size(renderableFace->ftFace(), &m_ftSize);

    setupDescription();
    setupVariation();
    setupHarfBuzz();
}

void IntrinsicFace::setupDescription()
{
    FT_Face ftFace = m_renderableFace->ftFace();
    auto os2Table = static_cast<TT_OS2 *>(FT_Get_Sfnt_Table(ftFace, FT_SFNT_OS2));
    auto headTable = static_cast<TT_Header *>(FT_Get_Sfnt_Table(ftFace, FT_SFNT_HEAD));

    m_familyName = searchFamilyNameRecordIndex(ftFace, os2Table);
    m_styleName = searchStyleNameRecordIndex(ftFace, os2Table);
    m_fullName = searchFullNameRecordIndex(ftFace);

    if (os2Table) {
        m_weight = os2Table->usWeightClass;
        m_width = os2Table->usWidthClass;

        if (os2Table->fsSelection & FSSelection::OBLIQUE) {
            m_slope = Slope::OBLIQUE;
        } else if (os2Table->fsSelection & FSSelection::ITALIC) {
            m_slope = Slope::ITALIC;
        }

        m_strikeoutPosition = os2Table->yStrikeoutPosition;
        m_strikeoutThickness = os2Table->yStrikeoutSize;
    } else if (headTable) {
        if (headTable->Mac_Style & MacStyle::BOLD) {
            m_weight = Weight::BOLD;
        }

        if (headTable->Mac_Style & MacStyle::CONDENSED) {
            m_width = Width::CONDENSED;
        } else if (headTable->Mac_Style & MacStyle::EXTENDED) {
            m_width = Width::EXPANDED;
        }

        if (headTable->Mac_Style & MacStyle::ITALIC) {
            m_slope = Slope::ITALIC;
        }
    }
}

void IntrinsicFace::setupVariation()
{
    FT_Face ftFace = m_renderableFace->ftFace();

    FT_MM_Var *variation;
    FT_Error error = FT_Get_MM_Var(ftFace, &variation);

    if (error == FT_Err_Ok) {
        FT_UInt numCoords = variation->num_axis;
        FT_Fixed fixedCoords[numCoords];

        if (FT_Get_Var_Design_Coordinates(ftFace, numCoords, fixedCoords) == FT_Err_Ok) {
            // Reset the style name and the full name.
            m_styleName = -1;
            m_fullName = -1;

            // Get the style name of this instance.
            for (FT_UInt i = 0; i < variation->num_namedstyles; i++) {
                FT_Var_Named_Style *namedStyle = &variation->namedstyle[i];
                FT_Fixed *namedCoords = namedStyle->coords;

                int result = memcmp(namedCoords, fixedCoords, sizeof(FT_Fixed) * numCoords);
                if (result == 0) {
                    m_styleName = searchEnglishNameRecordIndex(ftFace, static_cast<uint16_t>(namedStyle->strid));
                    break;
                }
            }

            // Get the values of variation axes.
            for (FT_UInt i = 0; i < numCoords; i++) {
                FT_Var_Axis *axis = &variation->axis[i];

                switch (axis->tag) {
                case FT_MAKE_TAG('i', 't', 'a', 'l'):
                    m_slope = variableItalicToSlope(fixedCoords[i]);
                    break;

                case FT_MAKE_TAG('s', 'l', 'n', 't'):
                    m_slope = variableSlantToSlope(fixedCoords[i]);
                    break;

                case FT_MAKE_TAG('w', 'd', 't', 'h'):
                    m_width = variableWidthToStandard(fixedCoords[i]);
                    break;

                case FT_MAKE_TAG('w', 'g', 'h', 't'):
                    m_weight = variableWeightToStandard(fixedCoords[i]);
                    break;
                }
            }
        }

        FT_Done_MM_Var(FreeType::library(), variation);
    }
}

void IntrinsicFace::setupHarfBuzz()
{
    m_shapableFace = ShapableFace::create(m_renderableFace);
}

IntrinsicFace::~IntrinsicFace()
{
    m_shapableFace->release();

    if (m_ftStroker) {
        FT_Stroker_Done(m_ftStroker);
    }
    if (m_ftSize) {
        m_renderableFace->lock();

        FT_Done_Size(m_ftSize);

        m_renderableFace->unlock();
    }

    m_renderableFace->release();
    m_fontFile->release();
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

void IntrinsicFace::loadSfntTable(FT_ULong tag, FT_Byte *buffer, FT_ULong *length)
{
    m_renderableFace->lock();

    FT_Face ftFace = m_renderableFace->ftFace();
    FT_Load_Sfnt_Table(ftFace, tag, 0, buffer, length);

    m_renderableFace->unlock();
}

int32_t IntrinsicFace::searchNameRecordIndex(uint16_t nameID)
{
    m_renderableFace->lock();

    FT_Face ftFace = m_renderableFace->ftFace();
    int32_t recordIndex = searchEnglishNameRecordIndex(ftFace, nameID);

    m_renderableFace->unlock();

    return recordIndex;
}

FT_UInt IntrinsicFace::getGlyphID(FT_ULong codePoint)
{
    m_renderableFace->lock();

    FT_Face ftFace = m_renderableFace->ftFace();
    FT_UInt glyphID = FT_Get_Char_Index(ftFace, codePoint);

    m_renderableFace->unlock();

    return glyphID;
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
