#include <string.h>
#include <jni.h>

#include <stdlib.h>

#include <android/log.h>
#define DEBUG_TAG "NDK_VMIMO"

#include <cv.h>
#include <highgui.h>

#include "image_processing.h"
#include "android_compat.h"

using namespace cv;

extern "C" {
    /**
	 * Determines two frames with most differing average intensity.
     * Subtracts frame2 from frame1, overwriting frame1
     */
    JNIEXPORT jbooleanArray Java_com_android_visualmimo_FrameProcessing_frameSubtraction(JNIEnv *env, jobject obj,
        jbyteArray frame1, jbyteArray frame2, jbyteArray frame3, jbyteArray frame4, jbyteArray frame5, jbyteArray frame6,
        jint width, jint height,
        jfloat c0x1, jfloat c0y1,
        jfloat c1x1, jfloat c1y1,
        jfloat c2x1, jfloat c2y1,
        jfloat c3x1, jfloat c3y1,

        jfloat c0x2, jfloat c0y2,
        jfloat c1x2, jfloat c1y2,
        jfloat c2x2, jfloat c2y2,
        jfloat c3x2, jfloat c3y2,

        jfloat c0x3, jfloat c0y3,
        jfloat c1x3, jfloat c1y3,
        jfloat c2x3, jfloat c2y3,
        jfloat c3x3, jfloat c3y3,

        jfloat c0x4, jfloat c0y4,
        jfloat c1x4, jfloat c1y4,
        jfloat c2x4, jfloat c2y4,
        jfloat c3x4, jfloat c3y4,

         jfloat c0x5, jfloat c0y5,
         jfloat c1x5, jfloat c1y5,
         jfloat c2x5, jfloat c2y5,
         jfloat c3x5, jfloat c3y5,

         jfloat c0x6, jfloat c0y6,
         jfloat c1x6, jfloat c1y6,
         jfloat c2x6, jfloat c2y6,
         jfloat c3x6, jfloat c3y6
        )
    {
      //f1 and f2 are the byte data of the two frames, l1 and l2 are the array lengths
      jbyte* f1 = env->GetByteArrayElements(frame1, NULL);
      jbyte* f2 = env->GetByteArrayElements(frame2, NULL);
      jbyte* f3 = env->GetByteArrayElements(frame3, NULL);
      jbyte* f4 = env->GetByteArrayElements(frame4, NULL);
        jbyte* f5 = env->GetByteArrayElements(frame5, NULL);
        jbyte* f6 = env->GetByteArrayElements(frame6, NULL);
      jsize l1 = env->GetArrayLength(frame1);

      // Create OpenCV matrix from 1D array.
      Mat matImage1(l1, 1, CV_8UC1, (unsigned char *)f1);
      Mat matImage2(l1, 1, CV_8UC1, (unsigned char *)f2);
      Mat matImage3(l1, 1, CV_8UC1, (unsigned char *)f3);
      Mat matImage4(l1, 1, CV_8UC1, (unsigned char *)f4);
        Mat matImage5(l1, 1, CV_8UC1, (unsigned char *)f5);
        Mat matImage6(l1, 1, CV_8UC1, (unsigned char *)f6);

      // Reshape matrix. 3 layers, correct height.
      // NOTE(revan): still needs to be flipped and have colors reordered (see MATLAB script).
	  //              This doesn't matter for message extraction though.
      Mat reshapedImage1 = matImage1.reshape(3, height);
      Mat reshapedImage2 = matImage2.reshape(3, height);
      Mat reshapedImage3 = matImage3.reshape(3, height);
      Mat reshapedImage4 = matImage4.reshape(3, height);
        Mat reshapedImage5 = matImage5.reshape(3, height);
        Mat reshapedImage6 = matImage6.reshape(3, height);

	// Extract message
	int width_blocks = 10;
	int height_blocks = 8;
	int num_blocks = width_blocks * height_blocks;

	jboolean message[num_blocks];
	processFrames(reshapedImage1, reshapedImage2, reshapedImage3, reshapedImage4, width, height, message, width_blocks, height_blocks, c0x1,c0y1,c1x1,c1y1,c2x1,c2y1,c3x1,c3y1,c0x2,c0y2,c1x2,c1y2,c2x2,c2y2,c3x2,c3y2,c0x3,c0y3,c1x3,c1y3,c2x3,c2y3,c3x3,c3y3,c0x4,c0y4,c1x4,c1y4,c2x4,c2y4,c3x4,c3y4);


	// return message
	jbooleanArray message_jboolean = env->NewBooleanArray(num_blocks);

	env->SetBooleanArrayRegion(message_jboolean, 0, num_blocks, message);
	// for (int i = 0; i < num_blocks; i++) {
	// 	message_jboolean[i] = num_blocks;
	// }

	// Save to file
//	flip(reshapedImage1.t(), reshapedImage1, 1);
//	flip(target1.t(), target1, 1);
//	imwrite("/sdcard/vmimo-orig.bmp", reshapedImage1);
//	imwrite("/sdcard/vmimo-subtract.bmp", target1);

	 imwrite("/sdcard/vmimo1.bmp", reshapedImage1);
	 imwrite("/sdcard/vmimo2.bmp", reshapedImage2);
	 imwrite("/sdcard/vmimo3.bmp", reshapedImage3);
	 imwrite("/sdcard/vmimo4.bmp", reshapedImage4);
        imwrite("/sdcard/vmimo5.bmp", reshapedImage5);
        imwrite("/sdcard/vmimo6.bmp", reshapedImage6);
	 char buffer [1000];
	 sprintf(buffer, "%f\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n\n",
	 	c0x1, c0y1,
         c1x1, c1y1,
         c2x1, c2y1,
         c3x1, c3y1,

         c0x2, c0y2,
         c1x2, c1y2,
         c2x2, c2y2,
         c3x2, c3y2,

         c0x3, c0y3,
         c1x3, c1y3,
         c2x3, c2y3,
         c3x3, c3y3,

         c0x4, c0y4,
         c1x4, c1y4,
         c2x4, c2y4,
         c3x4, c3y4,

             c0x5, c0y5,
             c1x5, c1y5,
             c2x5, c2y5,
             c3x5, c3y5,

        c0x6, c0y6,
        c1x6, c1y6,
        c2x6, c2y6,
        c3x6, c3y6);
	 debug_log_print(buffer);
	 sprintf(buffer, "width:%d, height:%d\n", width, height);
	 debug_log_print(buffer);

	// last arg: 0 -> copy array back, JNI_ABBORT -> don't copy
	env->ReleaseByteArrayElements(frame1, f1, JNI_ABORT);
	env->ReleaseByteArrayElements(frame2, f2, JNI_ABORT);
	env->ReleaseByteArrayElements(frame3, f2, JNI_ABORT);
	env->ReleaseByteArrayElements(frame4, f2, JNI_ABORT);

	return message_jboolean;
    }
}
