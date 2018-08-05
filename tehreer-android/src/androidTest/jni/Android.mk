TEST_PATH := $(call my-dir)
SRC_PATH := $(TEST_PATH)/../..
MAIN_PATH := $(SRC_PATH)/main/jni

include $(MAIN_PATH)/Android.mk

###########################TEST############################
include $(CLEAR_VARS)

LOCAL_PATH := $(TEST_PATH)
LOCAL_MODULE := testjni

FILE_LIST := \
    Memory.cpp \
    Test.cpp

LOCAL_C_INCLUDES := $(FT_HEADERS_PATH) $(SB_HEADERS_PATH) $(SF_HEADERS_PATH) $(MAIN_PATH)
LOCAL_SRC_FILES := $(FILE_LIST:%=$(LOCAL_PATH)/%)
LOCAL_LDLIBS := -latomic -landroid -ljnigraphics -llog
LOCAL_SHARED_LIBRARIES := tehreerjni

include $(BUILD_SHARED_LIBRARY)
###########################################################
