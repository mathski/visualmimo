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

	// Subtract, overwrite first.
	//subtract(reshapedImage1, reshapedImage2, reshapedImage1);

	//TODO: use OpenCV magic here

	/*
	//subtraction demo
	int i;
	for (i = 0; i < l1; i++) {
		f1[i] = f1[i] - f2[i];
	}
	*/


	//TODO: perspective transform
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
	
	// Define the destination image
	cv::Mat quad = cv::Mat::zeros(height, width, CV_8UC3);

	// Corners of the destination image
	std::vector<cv::Point2f> quad_pts;
	quad_pts.push_back(cv::Point2f(0, 0));
	quad_pts.push_back(cv::Point2f(quad.cols, 0));
	quad_pts.push_back(cv::Point2f(quad.cols, quad.rows));
	quad_pts.push_back(cv::Point2f(0, quad.rows));

	// Get transformation matrix
	cv::Mat transmtx = cv::getPerspectiveTransform(corners, quad_pts);

	// Apply perspective transformation
	cv::warpPerspective(reshapedImage1, quad, transmtx, quad.size());


	// Save to file
	imwrite("/sdcard/opencv.bmp", reshapedImage1);
	imwrite("/sdcard/opencv2.bmp", quad);


	// last arg: 0 -> copy array back, JNI_ABBORT -> don't copy
	env->ReleaseByteArrayElements(frame1, f1, 0);
	env->ReleaseByteArrayElements(frame2, f2, JNI_ABORT);
}
}
