#!/bin/bash
#Wrapper for ndk-build to unclobber libVuforia.so

ndk-build
cp libVuforia.so libs/armeabi/
