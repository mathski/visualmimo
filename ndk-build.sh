#!/bin/bash
#Wrapper for ndk-build to unclobber libVuforia.so

export OPENCV_PACKAGE_DIR="OpenCV-2.4.10-android-sdk"
ndk-build
cp libVuforia.so libs/armeabi/
cp libVuforia.so libs/armeabi-v7a/
