#include <jni.h>
#include <string.h>
#include <android/log.h>
#include <stdlib.h>

#include <cv.h>
#include <highgui.h>

#define DEBUG_TAG "NDK_AndroidNDK1SampleActivity"

using namespace cv;

extern "C" {
    JNIEXPORT void Java_com_android_visualmimo_MainActivity_helloLog(JNIEnv * env, jobject thisObj, jstring logThis)
    {
        jboolean isCopy;
        const char * szLogThis = env->GetStringUTFChars(logThis, &isCopy);

        __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", szLogThis);

        env->ReleaseStringUTFChars(logThis, szLogThis);
    }

	/**
	 * Utility function for projectiveTransform().
	 * Sorts corners as: top left, top right, back right, back left.
	 */
    void sortCorners(std::vector<cv::Point2f>& corners, cv::Point2f center)
    {
        std::vector<cv::Point2f> top, bot;

        for (int i = 0; i < corners.size(); i++)
        {
            if (corners[i].y < center.y)
                top.push_back(corners[i]);
            else
                bot.push_back(corners[i]);
        }

        cv::Point2f tl = top[0].x > top[1].x ? top[1] : top[0];
        cv::Point2f tr = top[0].x > top[1].x ? top[0] : top[1];
        cv::Point2f bl = bot[0].x > bot[1].x ? bot[1] : bot[0];
        cv::Point2f br = bot[0].x > bot[1].x ? bot[0] : bot[1];

        corners.clear();
        corners.push_back(tl);
        corners.push_back(tr);
        corners.push_back(br);
        corners.push_back(bl);
    }

	/**
	 * Performs projective transform on image. For given set of corners, skews
	 * image to known ratio.
	 */
    void projectiveTransform(Mat &image, Mat &dest,
        jfloat c0x, jfloat c0y,
        jfloat c1x, jfloat c1y,
        jfloat c2x, jfloat c2y,
        jfloat c3x, jfloat c3y) {

      //perspective transform
      std::vector<Point2f> corners;
      corners.push_back(cv::Point2f(c0x, c0y));
      corners.push_back(cv::Point2f(c1x, c1y));
      corners.push_back(cv::Point2f(c2x, c2y));
      corners.push_back(cv::Point2f(c3x, c3y));

      // Get mass center
      cv::Point2f center(0,0);
      for (int i = 0; i < corners.size(); i++)
        center += corners[i];

      center *= (1. / corners.size());
      sortCorners(corners, center);


      // Corners of the destination image
      std::vector<cv::Point2f> dest_pts;
      dest_pts.push_back(cv::Point2f(0, 0));
      dest_pts.push_back(cv::Point2f(dest.cols, 0));
      dest_pts.push_back(cv::Point2f(dest.cols, dest.rows));
      dest_pts.push_back(cv::Point2f(0, dest.rows));

      // Get transformation matrix
      cv::Mat transmtx = cv::getPerspectiveTransform(corners, dest_pts);

      // Apply perspective transformation
      cv::warpPerspective(image, dest, transmtx, dest.size());
    }

	/**
	 * Performs histogram equalization on image. Normalizes image intensity.
	 * TODO(revan): preserve transform matrix for application to second image.
	 */
    void histogramEqualization(Mat &image) {
      // Convert from BGR to YCbCr, because we need intensity as its own channel
      cv::cvtColor(image, image, CV_BGR2YCrCb);

      vector<Mat> channels;
      split(image, channels);

      cv::equalizeHist(channels[0], channels[0]);

      merge(channels, image);

      cv::cvtColor(image, image, CV_YCrCb2BGR);
    }

	/**
	 * Extracts a message from image. Iterates through ROI grid, using average to
	 * determine on/off state.
	 */
	void extractMessage(Mat &image, jboolean *message,
			int width, int height,
			int width_blocks, int height_blocks,
			int cutoff) {
		int block_width = width / width_blocks;
		int block_height = height / height_blocks;
		
		int k = 0;
		for (int i = 0; i < height; i += block_height) {
			for (int j = 0; j < width; j += block_width) {
				// Get region of interest
				Mat block = image(Rect(i, j, block_height, block_width));
				Scalar m = mean(block);
				message[k++] = (m[0] + m[1] + m[2] > cutoff) ? true : false;
			}
		}
	}

    /**
	 * Determines two frames with most differing average intensity.
     * Subtracts frame2 from frame1, overwriting frame1
     */
    JNIEXPORT jbooleanArray Java_com_android_visualmimo_MainActivity_frameSubtraction(JNIEnv *env, jobject obj,
        jbyteArray frame1, jbyteArray frame2, jbyteArray frame3, jbyteArray frame4,
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
        jfloat c3x4, jfloat c3y4
        )
    {
      //f1 and f2 are the byte data of the two frames, l1 and l2 are the array lengths
      jbyte* f1 = env->GetByteArrayElements(frame1, NULL);
      jbyte* f2 = env->GetByteArrayElements(frame2, NULL);
      jbyte* f3 = env->GetByteArrayElements(frame3, NULL);
      jbyte* f4 = env->GetByteArrayElements(frame4, NULL);
      jsize l1 = env->GetArrayLength(frame1);

      // Create OpenCV matrix from 1D array.
      Mat matImage1(l1, 1, CV_8UC1, (unsigned char *)f1);
      Mat matImage2(l1, 1, CV_8UC1, (unsigned char *)f2);
      Mat matImage3(l1, 1, CV_8UC1, (unsigned char *)f3);
      Mat matImage4(l1, 1, CV_8UC1, (unsigned char *)f4);

      // Reshape matrix. 3 layers, correct height.
      // NOTE(revan): still needs to be flipped and have colors reordered (see MATLAB script).
	  //              This doesn't matter for message extraction though.
      Mat reshapedImage1 = matImage1.reshape(3, height);
      Mat reshapedImage2 = matImage2.reshape(3, height);
      Mat reshapedImage3 = matImage3.reshape(3, height);
      Mat reshapedImage4 = matImage4.reshape(3, height);
	  //matImage1.release();
	  //matImage2.release();
	  //matImage3.release();
	  //matImage4.release();


      // Define the destination image
      cv::Mat target1 = cv::Mat::zeros(width, height, CV_8UC1);
      cv::Mat target2 = cv::Mat::zeros(width, height, CV_8UC1);
      cv::Mat target3 = cv::Mat::zeros(width, height, CV_8UC1);
      cv::Mat target4 = cv::Mat::zeros(width, height, CV_8UC1);
      projectiveTransform(reshapedImage1, target1, c0x1, c0y1, c1x1, c1y1, c2x1, c2y1, c3x1, c3y1);
      projectiveTransform(reshapedImage2, target2, c0x2, c0y2, c1x2, c1y2, c2x2, c2y2, c3x2, c3y2);
      projectiveTransform(reshapedImage3, target3, c0x3, c0y3, c1x3, c1y3, c2x3, c2y3, c3x3, c3y3);
      projectiveTransform(reshapedImage4, target4, c0x4, c0y4, c1x4, c1y4, c2x4, c2y4, c3x4, c3y4);

	  // Find best pair of frames
	  Scalar m;
	  m = mean(target1);
	  int in1 = m[0] + m[1] + m[2];
	  m = mean(target2);
	  int in2 = m[0] + m[1] + m[2];
	  m = mean(target3);
	  int in3 = m[0] + m[1] + m[2];
	  m = mean(target4);
	  int in4 = m[0] + m[1] + m[2];

	  int pair_1_2 = abs(in1 - in2);
	  int pair_1_3 = abs(in1 - in3);
	  int pair_1_4 = abs(in1 - in4);
	  int pair_2_3 = abs(in2 - in3);
	  int pair_2_4 = abs(in2 - in4);
	  int pair_3_4 = abs(in3 - in4);

	  int threshold;

      //imwrite("/sdcard/vmimo-frame1.bmp", target1);
      //imwrite("/sdcard/vmimo-frame2.bmp", target2);
      //imwrite("/sdcard/vmimo-frame3.bmp", target3);
      //imwrite("/sdcard/vmimo-frame4.bmp", target4);
	  Mat *good_image1;
	  Mat *good_image2;
	  if (pair_1_2 > pair_1_3
			  && pair_1_2 > pair_1_4
			  && pair_1_2 > pair_2_3
			  && pair_1_2 > pair_2_4
			  && pair_1_2 > pair_3_4) {
		  good_image1 = &target1;
		  good_image2 = &target2;
		  //target3.release();
		  //target4.release();
		  threshold = (in1 + in2) / 6;
		  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", "1 and 2");
	  } else if (pair_1_3 > pair_1_4
			  && pair_1_3 > pair_2_3
			  && pair_1_3 > pair_2_4
			  && pair_1_3 > pair_3_4) {
		  good_image1 = &target1;
		  good_image2 = &target3;
		  //target2.release();
		  //target4.release();
		  threshold = (in1 + in3) / 6;
		  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", "1 and 3");
	  } else if (pair_1_4 > pair_2_3
			  && pair_1_4 > pair_2_4
			  && pair_1_4 > pair_3_4) {
		  good_image1 = &target1;
		  good_image2 = &target4;
		  //target2.release();
		  //target3.release();
		  threshold = (in1 + in4) / 6;
		  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", "1 and 4");
	  } else if (pair_2_3 > pair_2_4
			  && pair_2_3 > pair_3_4) {
		  good_image1 = &target2;
		  good_image2 = &target3;
		  //target1.release();
		  //target4.release();
		  threshold = (in2 + in3) / 6;
		  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", "2 and 3");
	  } else if (pair_2_4 > pair_3_4) {
		  good_image1 = &target2;
		  good_image2 = &target4;
		  //target3.release();
		  //target4.release();
		  threshold = (in2 + in4) / 6;
		  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", "2 and 4");
	  } else {
		  good_image1 = &target3;
		  good_image2 = &target4;
		  //target1.release();
		  //target2.release();
		  threshold = (in3 + in4) / 6;
		  __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", "3 and 4");
	  }


      //imwrite("/sdcard/vmimo-frame1-noeq.bmp", target1);

      histogramEqualization(*good_image1);
      histogramEqualization(*good_image2);

      //imwrite("/sdcard/vmimo-frame1.bmp", target1);
      //imwrite("/sdcard/vmimo-frame2.bmp", target2);

      // Subtract, overwrite first.
      subtract(*good_image1, *good_image2, *good_image1);

	  // Extract message
	  int width_blocks = 10;
	  int height_blocks = 8;
	  int num_blocks = width_blocks * height_blocks;
	  jboolean message[num_blocks];
	  extractMessage(*good_image1, message, width, height, width_blocks, height_blocks, threshold);

	  // return message
	  jbooleanArray message_jboolean = env->NewBooleanArray(num_blocks);
	  env->SetBooleanArrayRegion(message_jboolean, 0, num_blocks, message);

      // Save to file
//      flip(reshapedImage1.t(), reshapedImage1, 1);
      //flip(target1.t(), target1, 1);
      //imwrite("/sdcard/vmimo-orig.bmp", reshapedImage1);
//      imwrite("/sdcard/vmimo-subtract.bmp", target1);


      // last arg: 0 -> copy array back, JNI_ABBORT -> don't copy
      env->ReleaseByteArrayElements(frame1, f1, JNI_ABORT);
      env->ReleaseByteArrayElements(frame2, f2, JNI_ABORT);
      env->ReleaseByteArrayElements(frame3, f2, JNI_ABORT);
      env->ReleaseByteArrayElements(frame4, f2, JNI_ABORT);

	  return message_jboolean;
    }
}
