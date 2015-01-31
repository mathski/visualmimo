LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
include $(OPENCV_PACKAGE_DIR)/sdk/native/jni/OpenCV.mk

LOCAL_LDLIBS := -llog

LOCAL_MODULE    := ndk1
LOCAL_SRC_FILES := native.c

include $(BUILD_SHARED_LIBRARY)
