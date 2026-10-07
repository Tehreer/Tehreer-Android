#
# Copyright (C) 2026 Muhammad Tayyab Akram
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cmake_minimum_required(VERSION 3.22.1)
project(tehreer C CXX)

set(CMAKE_C_STANDARD 99)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_C_EXTENSIONS OFF)
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

set(ROOT_PATH ${CMAKE_CURRENT_LIST_DIR}/../../../..)

#########################FREETYPE##########################
set(FT_ROOT_PATH ${ROOT_PATH}/freetype)
set(FT_HEADERS_PATH ${FT_ROOT_PATH}/include)
set(FT_SOURCE_PATH ${FT_ROOT_PATH}/src)

set(FT_FILE_LIST
    autofit/autofit.c
    base/ftbase.c
    base/ftbbox.c
    base/ftbitmap.c
    base/ftdebug.c
    base/ftgasp.c
    base/ftglyph.c
    base/ftinit.c
    base/ftlcdfil.c
    base/ftmm.c
    base/ftfntfmt.c
    base/ftpatent.c
    base/ftsynth.c
    base/ftstroke.c
    base/ftsystem.c
    bdf/bdf.c
    cff/cff.c
    cid/type1cid.c
    gzip/ftgzip.c
    lzw/ftlzw.c
    pcf/pcf.c
    pfr/pfr.c
    psaux/psaux.c
    pshinter/pshinter.c
    psnames/psnames.c
    raster/raster.c
    sdf/sdf.c
    sfnt/sfnt.c
    smooth/smooth.c
    svg/svg.c
    truetype/truetype.c
    type1/type1.c
    type42/type42.c
    winfonts/winfnt.c
)
list(TRANSFORM FT_FILE_LIST PREPEND ${FT_SOURCE_PATH}/)

add_library(freetype STATIC ${FT_FILE_LIST})
target_include_directories(freetype PUBLIC ${FT_HEADERS_PATH})
target_compile_definitions(freetype PRIVATE FT2_BUILD_LIBRARY)
#########################FREETYPE##########################

########################SHEEN BIDI#########################
set(SB_ROOT_PATH ${ROOT_PATH}/sheenbidi)
set(SB_HEADERS_PATH ${SB_ROOT_PATH}/Headers)
set(SB_SOURCE_PATH ${SB_ROOT_PATH}/Source)

if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(SB_FILE_LIST
        API/SBAlgorithm.c
        API/SBAllocator.c
        API/SBAttributeList.c
        API/SBAttributeRegistry.c
        API/SBBase.c
        API/SBCodepoint.c
        API/SBCodepointSequence.c
        API/SBLine.c
        API/SBLog.c
        API/SBMirrorLocator.c
        API/SBParagraph.c
        API/SBScriptLocator.c
        API/SBText.c
        API/SBTextConfig.c
        API/SBTextIterators.c
        Core/List.c
        Core/Memory.c
        Core/Object.c
        Core/Once.c
        Data/BidiTypeLookup.c
        Data/GeneralCategoryLookup.c
        Data/PairingLookup.c
        Data/ScriptLookup.c
        Script/ScriptStack.c
        Text/AttributeDictionary.c
        Text/AttributeManager.c
        Text/BidiTypesBuffer.c
        Text/TextAnalysis.c
        Text/TextBuffer.c
        UBA/BidiChain.c
        UBA/BracketQueue.c
        UBA/IsolatingRun.c
        UBA/LevelRun.c
        UBA/RunQueue.c
        UBA/StatusStack.c
    )
else()
    set(SB_FILE_LIST SheenBidi.c)
    set(SB_UNITY SB_CONFIG_UNITY)
endif()
list(TRANSFORM SB_FILE_LIST PREPEND ${SB_SOURCE_PATH}/)

add_library(sheenbidi STATIC ${SB_FILE_LIST})
target_include_directories(sheenbidi PUBLIC ${SB_HEADERS_PATH} PRIVATE ${SB_SOURCE_PATH})
target_compile_definitions(sheenbidi PRIVATE SB_CONFIG_DISABLE_SCRATCH_MEMORY ${SB_UNITY})
target_compile_definitions(sheenbidi PUBLIC SB_CONFIG_EXPERIMENTAL_TEXT_API)
###########################################################

#########################HARFBUZZ##########################
set(HB_ROOT_PATH ${ROOT_PATH}/harfbuzz)
set(HB_SOURCE_PATH ${HB_ROOT_PATH}/src)

add_library(harfbuzz STATIC ${HB_SOURCE_PATH}/harfbuzz.cc)
target_include_directories(harfbuzz PUBLIC ${HB_SOURCE_PATH})
target_compile_definitions(harfbuzz PRIVATE
    HAVE_PTHREAD
    HAVE_FREETYPE
    HAVE_FT_GET_VAR_BLEND_COORDINATES
    HAVE_FT_DONE_MM_VAR
)
target_link_libraries(harfbuzz PUBLIC freetype)
###########################################################

#########################UNIBREAK##########################
set(UB_ROOT_PATH ${ROOT_PATH}/unibreak)
set(UB_SOURCE_PATH ${UB_ROOT_PATH}/src)

# The data files are included by the definition files.
set(UB_FILE_LIST
    eastasianwidthdef.c
    emojidef.c
    graphemebreak.c
    linebreak.c
    linebreakdata.c
    linebreakdef.c
    unibreakbase.c
    unibreakdef.c
    wordbreak.c
)
list(TRANSFORM UB_FILE_LIST PREPEND ${UB_SOURCE_PATH}/)

add_library(unibreak STATIC ${UB_FILE_LIST})
target_include_directories(unibreak PUBLIC ${UB_SOURCE_PATH})
###########################################################

#######################TEHREER CORE########################
set(TR_ROOT_PATH ${ROOT_PATH}/tehreer-core)

add_library(tehreer STATIC ${TR_ROOT_PATH}/Source/Tehreer.c)
target_include_directories(tehreer PUBLIC ${TR_ROOT_PATH}/Headers PRIVATE ${TR_ROOT_PATH}/Source)
target_compile_definitions(tehreer PRIVATE TR_CONFIG_UNITY)
target_link_libraries(tehreer PRIVATE freetype harfbuzz unibreak PUBLIC sheenbidi)
###########################################################

##########################TEHREER##########################
set(FILE_LIST
    AdvanceCache.cpp
    BidiAlgorithm.cpp
    BidiBuffer.cpp
    BidiLine.cpp
    BidiMirrorLocator.cpp
    BidiParagraph.cpp
    FontFile.cpp
    FreeType.cpp
    GlyphOutline.cpp
    GlyphRasterizer.cpp
    JavaBridge.cpp
    Raw.cpp
    RenderableFace.cpp
    ScriptClassifier.cpp
    SfntTables.cpp
    ShapableFace.cpp
    ShapingEngine.cpp
    ShapingResult.cpp
    StreamUtils.cpp
    Tehreer.cpp
    Typeface.cpp
    Unicode.cpp
)

add_library(tehreerjni SHARED ${FILE_LIST})
target_link_libraries(tehreerjni PRIVATE tehreer harfbuzz sheenbidi freetype atomic android jnigraphics log)
target_link_options(tehreerjni PRIVATE $<$<NOT:$<CONFIG:Debug>>:-s>)
###########################################################
