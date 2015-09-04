LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=on
include $(OPENCV_PACKAGE_DIR)/sdk/native/jni/OpenCV.mk

LOCAL_LDLIBS := -llog

LOCAL_MODULE    := ndk1
LOCAL_SRC_FILES := native.cpp helpers.cpp

LOCAL_STATIC_LIBRARIES += libopencv_contrib libopencv_legacy libopencv_ml libopencv_stitching libopencv_nonfree libopencv_objdetect libopencv_videostab libopencv_calib3d libopencv_photo libopencv_video libopencv_features2d libopencv_highgui libopencv_androidcamera libopencv_flann libopencv_imgproc libopencv_ts libopencv_core
include $(BUILD_SHARED_LIBRARY)

