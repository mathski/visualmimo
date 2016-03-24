#include <string.h>
#include <jni.h>

#include <stdlib.h>

#include <android/log.h>
#define DEBUG_TAG "NDK_VMIMO"

#include <cv.h>
#include <highgui.h>

#include "image_processing.h"
#include "android_compat.h"
#include "ndk_result.h"

#define NUM_FRAMES 6

using namespace cv;

extern "C" {
  /**
	 * Determines two frames with most differing average intensity.
   * Subtracts frame2 from frame1, overwriting frame1
   */
  JNIEXPORT jobject Java_com_android_visualmimo_FrameProcessing_frameSubtraction(JNIEnv *env,
      jobject obj, jobjectArray frames, jint width, jint height, jobjectArray cornersArray)
  {

    Mat matImages[NUM_FRAMES];
    float corners[NUM_FRAMES][8];
    for (int i = 0; i < NUM_FRAMES; i++) {
      jbyteArray frame = (jbyteArray) env->GetObjectArrayElement(frames, i);

      // Create OpenCV matrix from 1D array.
      Mat matImage1D = Mat(env->GetArrayLength(frame), 1, CV_8UC1, (unsigned char *) env->GetByteArrayElements(frame, NULL));

      // Reshape matrix. 3 layers, correct height.
      // NOTE: still needs to be flipped and have colors reordered (see MATLAB script).
      //       This doesn't strictly matter for message extraction though.
      matImages[i] = matImage1D.reshape(3, height);

      jfloatArray frameCorners = (jfloatArray) env->GetObjectArrayElement(cornersArray, i);
      float* cornerSet = (float*) env->GetFloatArrayElements(frameCorners, NULL);
      std::copy(cornerSet, cornerSet + 8, corners[i]);
      env->ReleaseFloatArrayElements(frameCorners, cornerSet, JNI_ABORT);
    }

    // Extract message
    int width_blocks = 10;
    int height_blocks = 8;
    int num_blocks = (width_blocks * height_blocks - 4)/2 - 4; // - 4 for the four corners, index bits

    jboolean message[num_blocks];
    NDK_RESULT r = processFrames(
        matImages,
        width,
        height,
        message,
        width_blocks,
        height_blocks,
        corners);


    // return message
    jbooleanArray message_jboolean = env->NewBooleanArray(num_blocks);
    env->SetBooleanArrayRegion(message_jboolean, 0, num_blocks, message);


    #ifdef WRITE_FRAMES
    char strbuff[800];
    // Save to disk for simulator use
    for (int i = 0; i < NUM_FRAMES; i++) {
      sprintf(strbuff, "/sdcard/vmimo%d.bmp", i);
      imwrite(strbuff, matImages[i]);
    }

    for (int i = 0; i < NUM_FRAMES; i++) {
      sprintf(strbuff, "{\n%f,\n%f,\n%f,\n%f,\n%f,\n%f,\n%f,\n%f\n},",
              corners[i][0], corners[i][1], corners[i][2], corners[i][3],
              corners[i][4], corners[i][5], corners[i][6], corners[i][7]);
      debug_log_print(strbuff);
    }

    sprintf(strbuff, "width:%d, height:%d\n", width, height);
    debug_log_print(strbuff);
    #endif

    jclass cls = env->FindClass("com/android/visualmimo/NDKResult");
    jmethodID methodId = env->GetMethodID(cls, "<init>", "(II[Z)V"); //[Z for bool array, I for int
    jobject ret = env->NewObject(cls, methodId, r.index, r.mismatches, message_jboolean);

    //TODO figure out if this causes crashes
    // last arg: 0 -> copy array back, JNI_ABBORT -> don't copy
    // env->ReleaseByteArrayElements(frame1, f1, JNI_ABORT);
    // env->ReleaseByteArrayElements(frame2, f2, JNI_ABORT);
    // env->ReleaseByteArrayElements(frame3, f2, JNI_ABORT);
    // env->ReleaseByteArrayElements(frame4, f2, JNI_ABORT);

    return ret;
  }
}
