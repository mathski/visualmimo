# Visual MIMO
An Android implementation of visible light communication. [See report here.](https://github.com/revan/special-problems-report/blob/master/report.pdf)


## Setup - Android

- Import project in Android Studio
- Add: `ndk.dir=[path/to/ndk]` to `local.properties`
- Download folder `OpenCV-2.4.10-android-sdk` and place in root of project

## Setup - MATLAB

- Run `randomMessageSample.m` to generate `vmimo.avi`. It's short so run it looping: `mplayer -loop 0 vmimo.avi`.

## NDK Code
C++ code is in `app/src/main/jni/`. The CV logic is in `image_processing.cpp`, with the NDK abstracted out. `native.cpp` connects `image_processing.cpp` to the Android code, and `simulator.cpp` allows developing CV algorithms without any Android by simulating the flow of images and corners from the Android code.

To run on a computer, `cd` to the `jni` directory and run `make`, then `./simulator`.

## Current hacks

- At the time of writing, Android Studio / gradle support for NDK is limited to projects that don't include libraries. The gradle config thus disables the NDK compile and manually runs `ndk-compile` when building the project.
- The Android project was based on an old Vuforia sample, so large parts of it are super ugly. If you want to add a UI element or feature, look at `app/src/main/java/com/android/visualmimo/MainActivity.java`.

## TODO

- Play with new new embedding methods
- Improved "test bench" with, for example, a way to randomly generate a message in MATLAB and test accuracy of reading on device (communicate test string over network, or generate using same short seed?)
- Clean up MATLAB code a lot
- Figure out how to draw on Android screen to display information
- Trim ancient sample code from Android
- Chromecast pairing demo