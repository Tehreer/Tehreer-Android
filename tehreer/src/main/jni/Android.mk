#
# Copyright (C) 2016 Muhammad Tayyab Akram
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

FILE_LIST := $(FT_SOURCE_PATH)/autofit/autofit.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftbase.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftapi.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftbbox.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftbitmap.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftdebug.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftgasp.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftglyph.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftinit.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftlcdfil.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftmm.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftpatent.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftsynth.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftstroke.c
FILE_LIST += $(FT_SOURCE_PATH)/base/ftsystem.c
FILE_LIST += $(FT_SOURCE_PATH)/bdf/bdf.c
FILE_LIST += $(FT_SOURCE_PATH)/cff/cff.c
FILE_LIST += $(FT_SOURCE_PATH)/cid/type1cid.c
FILE_LIST += $(FT_SOURCE_PATH)/gzip/ftgzip.c
FILE_LIST += $(FT_SOURCE_PATH)/lzw/ftlzw.c
FILE_LIST += $(FT_SOURCE_PATH)/pcf/pcf.c
FILE_LIST += $(FT_SOURCE_PATH)/pfr/pfr.c
FILE_LIST += $(FT_SOURCE_PATH)/psaux/psaux.c
FILE_LIST += $(FT_SOURCE_PATH)/pshinter/pshinter.c
FILE_LIST += $(FT_SOURCE_PATH)/psnames/psnames.c
FILE_LIST += $(FT_SOURCE_PATH)/raster/raster.c
FILE_LIST += $(FT_SOURCE_PATH)/sfnt/sfnt.c
FILE_LIST += $(FT_SOURCE_PATH)/smooth/smooth.c
FILE_LIST += $(FT_SOURCE_PATH)/truetype/truetype.c
FILE_LIST += $(FT_SOURCE_PATH)/type1/type1.c
FILE_LIST += $(FT_SOURCE_PATH)/type42/type42.c
FILE_LIST += $(FT_SOURCE_PATH)/winfonts/winfnt.c
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_CFLAGS := -DFT2_BUILD_LIBRARY -std=c99
LOCAL_C_INCLUDES := $(FT_HEADERS_PATH)
LOCAL_EXPORT_C_INCLUDES := $(FT_HEADERS_PATH)

include $(BUILD_STATIC_LIBRARY)
#########################FREETYPE##########################

########################SHEEN BIDI#########################
include $(CLEAR_VARS)

LOCAL_MODULE := sheenbidi

SB_ROOT_PATH := $(ROOT_PATH)/sheenbidi
SB_HEADERS_PATH := $(SB_ROOT_PATH)/Headers
SB_SOURCE_PATH := $(SB_ROOT_PATH)/Source

FILE_LIST := $(wildcard $(SB_SOURCE_PATH)/*.c)
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_C_INCLUDES := $(SB_HEADERS_PATH)
LOCAL_EXPORT_C_INCLUDES := $(SB_HEADERS_PATH)

include $(BUILD_STATIC_LIBRARY)
###########################################################

#######################SHEEN FIGURE########################
include $(CLEAR_VARS)

LOCAL_MODULE := sheenfigure

SF_ROOT_PATH := $(ROOT_PATH)/sheenfigure
SF_HEADERS_PATH := $(SF_ROOT_PATH)/Headers
SF_SOURCE_PATH := $(SF_ROOT_PATH)/Source

FILE_LIST := $(wildcard $(SF_SOURCE_PATH)/*.c)
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_C_INCLUDES := $(SF_HEADERS_PATH)
LOCAL_C_INCLUDES += $(SB_HEADERS_PATH)
LOCAL_EXPORT_C_INCLUDES := $(SF_HEADERS_PATH)

include $(BUILD_STATIC_LIBRARY)
###########################################################

##########################TEHREER##########################
include $(CLEAR_VARS)

LOCAL_MODULE := tehreer

FILE_LIST := $(wildcard $(LOCAL_PATH)/*.cpp)
LOCAL_SRC_FILES := $(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_LDLIBS := -landroid
LOCAL_LDLIBS += -ljnigraphics
LOCAL_LDLIBS += -llog

LOCAL_STATIC_LIBRARIES := freetype
LOCAL_STATIC_LIBRARIES += sheenbidi
LOCAL_STATIC_LIBRARIES += sheenfigure

include $(BUILD_SHARED_LIBRARY)
###########################################################
