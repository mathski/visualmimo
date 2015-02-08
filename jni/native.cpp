#include <jni.h>
#include <string.h>
#include <android/log.h>

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
     * Subtracts frame2 from frame1, overwriting frame1
     */
    JNIEXPORT void Java_com_android_visualmimo_MainActivity_frameSubtraction(JNIEnv *env, jobject obj,
        jbyteArray frame1, jbyteArray frame2,
        jint width, jint height,
        jfloat c0x, jfloat c0y,
        jfloat c1x, jfloat c1y,
        jfloat c2x, jfloat c2y,
        jfloat c3x, jfloat c3y
        )
    {
      //f1 and f2 are the byte data of the two frames, l1 and l2 are the array lengths
      jbyte* f1 = env->GetByteArrayElements(frame1, NULL);
      jbyte* f2 = env->GetByteArrayElements(frame2, NULL);
      jsize l1 = env->GetArrayLength(frame1);

      // Create OpenCV matrix from 1D array.
      Mat matImage1(l1, 1, CV_8UC1, (unsigned char *)f1);
      Mat matImage2(l1, 1, CV_8UC1, (unsigned char *)f2);

      // Reshape matrix. 3 layers, correct height.
      // NOTE(revan): still needs to be flipped and have colors reordered (see MATLAB script).
      Mat reshapedImage1 = matImage1.reshape(3, height);
      Mat reshapedImage2 = matImage2.reshape(3, height);

      // Define the destination image
      cv::Mat target1 = cv::Mat::zeros(560, 420, CV_8UC1);
      cv::Mat target2 = cv::Mat::zeros(560, 420, CV_8UC1);
      projectiveTransform(reshapedImage1, target1, c0x, c0y, c1x, c1y, c2x, c2y, c3x, c3y);
      projectiveTransform(reshapedImage2, target2, c0x, c0y, c1x, c1y, c2x, c2y, c3x, c3y);

      histogramEqualization(target1);
      histogramEqualization(target2);

      imwrite("/sdcard/opencv2.bmp", target1);
      imwrite("/sdcard/opencv3.bmp", target2);

      // Subtract, overwrite first.
      subtract(target1, target2, target1);

      // Save to file
      flip(reshapedImage1.t(), reshapedImage1, 1);
      flip(target1.t(), target1, 1);
      imwrite("/sdcard/opencv.bmp", reshapedImage1);
      imwrite("/sdcard/opencv4.bmp", target1);


      // last arg: 0 -> copy array back, JNI_ABBORT -> don't copy
      env->ReleaseByteArrayElements(frame1, f1, 0);
      env->ReleaseByteArrayElements(frame2, f2, JNI_ABORT);
    }
}
