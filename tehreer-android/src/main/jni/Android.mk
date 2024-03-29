#
# Copyright (C) 2016-2023 Muhammad Tayyab Akram
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

LOCAL_PATH := $(call my-dir)
ROOT_PATH := $(LOCAL_PATH)/../../../..

#########################FREETYPE##########################
include $(CLEAR_VARS)

LOCAL_MODULE := freetype

FT_ROOT_PATH := $(ROOT_PATH)/freetype
FT_HEADERS_PATH := $(FT_ROOT_PATH)/include
FT_SOURCE_PATH := $(FT_ROOT_PATH)/src

FT_FILE_LIST := \
    autofit/autofit.c \
    base/ftbase.c \
    base/ftbbox.c \
    base/ftbitmap.c \
    base/ftdebug.c \
    base/ftgasp.c \
    base/ftglyph.c \
    base/ftinit.c \
    base/ftlcdfil.c \
    base/ftmm.c \
    base/ftfntfmt.c \
    base/ftpatent.c \
    base/ftsynth.c \
    base/ftstroke.c \
    base/ftsystem.c \
    bdf/bdf.c \
    cff/cff.c \
    cid/type1cid.c \
    gzip/ftgzip.c \
    lzw/ftlzw.c \
    pcf/pcf.c \
    pfr/pfr.c \
    psaux/psaux.c \
    pshinter/pshinter.c \
    psnames/psnames.c \
    raster/raster.c \
    sdf/sdf.c \
    sfnt/sfnt.c \
    smooth/smooth.c \
    svg/svg.c \
    truetype/truetype.c \
    type1/type1.c \
    type42/type42.c \
    winfonts/winfnt.c

LOCAL_CFLAGS := -DFT2_BUILD_LIBRARY
LOCAL_C_INCLUDES := $(FT_HEADERS_PATH)
LOCAL_EXPORT_C_INCLUDES := $(FT_HEADERS_PATH)
LOCAL_SRC_FILES := $(FT_FILE_LIST:%=$(FT_SOURCE_PATH)/%)

include $(BUILD_STATIC_LIBRARY)
#########################FREETYPE##########################

########################SHEEN BIDI#########################
include $(CLEAR_VARS)

LOCAL_MODULE := sheenbidi

SB_ROOT_PATH := $(ROOT_PATH)/sheenbidi
SB_HEADERS_PATH := $(SB_ROOT_PATH)/Headers
SB_SOURCE_PATH := $(SB_ROOT_PATH)/Source

ifeq ($(APP_OPTIM), debug)
    SB_FILE_LIST := \
        BidiChain.c \
        BidiTypeLookup.c \
        BracketQueue.c \
        GeneralCategoryLookup.c \
        IsolatingRun.c \
        LevelRun.c \
        PairingLookup.c \
        RunQueue.c \
        SBAlgorithm.c \
        SBBase.c \
        SBCodepointSequence.c \
        SBLine.c \
        SBLog.c \
        SBMirrorLocator.c \
        SBParagraph.c \
        SBScriptLocator.c \
        ScriptLookup.c \
        ScriptStack.c \
        StatusStack.c
else
    SB_FILE_LIST := SheenBidi.c
    SB_MACROS := -DSB_CONFIG_UNITY
endif

LOCAL_CFLAGS := $(SB_MACROS)
LOCAL_C_INCLUDES := $(SB_HEADERS_PATH)
LOCAL_EXPORT_C_INCLUDES := $(SB_HEADERS_PATH)
LOCAL_SRC_FILES := $(SB_FILE_LIST:%=$(SB_SOURCE_PATH)/%)

include $(BUILD_STATIC_LIBRARY)
###########################################################

#########################HARFBUZZ##########################
include $(CLEAR_VARS)

LOCAL_MODULE := harfbuzz

HB_ROOT_PATH := $(ROOT_PATH)/harfbuzz
HB_SOURCE_PATH := $(HB_ROOT_PATH)/src

HB_FILE_LIST := harfbuzz.cc
HB_MACROS := -DHAVE_PTHREAD -DHAVE_FREETYPE -DHAVE_FT_GET_VAR_BLEND_COORDINATES -DHAVE_FT_DONE_MM_VAR

LOCAL_CFLAGS := $(HB_MACROS)
LOCAL_C_INCLUDES := $(HB_SOURCE_PATH) $(FT_HEADERS_PATH)
LOCAL_EXPORT_C_INCLUDES := $(HB_SOURCE_PATH)
LOCAL_SRC_FILES := $(HB_FILE_LIST:%=$(HB_SOURCE_PATH)/%)

include $(BUILD_STATIC_LIBRARY)
###########################################################

##########################TEHREER##########################
include $(CLEAR_VARS)

LOCAL_MODULE := tehreerjni

FILE_LIST := \
    AdvanceCache.cpp \
    BidiAlgorithm.cpp \
    BidiBuffer.cpp \
    BidiLine.cpp \
    BidiMirrorLocator.cpp \
    BidiParagraph.cpp \
    FontFile.cpp \
    FreeType.cpp \
    GlyphOutline.cpp \
    GlyphRasterizer.cpp \
    JavaBridge.cpp \
    Raw.cpp \
    RenderableFace.cpp \
    ScriptClassifier.cpp \
    SfntTables.cpp \
    ShapableFace.cpp \
    ShapingEngine.cpp \
    ShapingResult.cpp \
    StreamUtils.cpp \
    Tehreer.cpp \
    Typeface.cpp \
    Unicode.cpp

LOCAL_LDLIBS := -latomic -landroid -ljnigraphics -llog
LOCAL_STATIC_LIBRARIES := freetype sheenbidi harfbuzz
LOCAL_SRC_FILES := $(FILE_LIST:%=$(LOCAL_PATH)/%)

include $(BUILD_SHARED_LIBRARY)
###########################################################
