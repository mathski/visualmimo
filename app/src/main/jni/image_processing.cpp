#include <string.h>
#include <cv.h>
#include <highgui.h>

#include "helpers.h"
#include "android_compat.h"

using namespace cv;
extern "C" {
/**
 * Extracts a message from image. Iterates through ROI grid, using average to
 * determine on/off state.
 */
void extractMessage(Mat &image, unsigned char *message,
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
			message[k++] = (m[0] + m[1] + m[2] > cutoff);
		}
	}
}

void processFrames(Mat matImage1, Mat matImage2, Mat matImage3, Mat matImage4,
	int width, int height, unsigned char *message,
	int width_blocks, int height_blocks,

	float c0x1, float c0y1,
    float c1x1, float c1y1,
    float c2x1, float c2y1,
    float c3x1, float c3y1,

    float c0x2, float c0y2,
    float c1x2, float c1y2,
    float c2x2, float c2y2,
    float c3x2, float c3y2,

    float c0x3, float c0y3,
    float c1x3, float c1y3,
    float c2x3, float c2y3,
    float c3x3, float c3y3,

    float c0x4, float c0y4,
    float c1x4, float c1y4,
    float c2x4, float c2y4,
    float c3x4, float c3y4
	) {

	// Define the destination image
	cv::Mat target1 = cv::Mat::zeros(width, height, CV_8UC1);
	cv::Mat target2 = cv::Mat::zeros(width, height, CV_8UC1);
	cv::Mat target3 = cv::Mat::zeros(width, height, CV_8UC1);
	cv::Mat target4 = cv::Mat::zeros(width, height, CV_8UC1);
	projectiveTransform(matImage1, target1, c0x1, c0y1, c1x1, c1y1, c2x1, c2y1, c3x1, c3y1);
	projectiveTransform(matImage2, target2, c0x2, c0y2, c1x2, c1y2, c2x2, c2y2, c3x2, c3y2);
	projectiveTransform(matImage3, target3, c0x3, c0y3, c1x3, c1y3, c2x3, c2y3, c3x3, c3y3);
	projectiveTransform(matImage4, target4, c0x4, c0y4, c1x4, c1y4, c2x4, c2y4, c3x4, c3y4);

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
	  if (in1 < in2) {
		  good_image1 = &target1;
		  good_image2 = &target2;
	  } else {
		  good_image1 = &target2;
		  good_image2 = &target1;
	  }
	  debug_log_print("NDK:LC: 1 and 2");
	} else if (pair_1_3 > pair_1_4
		  && pair_1_3 > pair_2_3
		  && pair_1_3 > pair_2_4
		  && pair_1_3 > pair_3_4) {
	  if (in1 < in3) {
		  good_image1 = &target1;
		  good_image2 = &target3;
	  } else {
		  good_image1 = &target3;
		  good_image2 = &target1;
	  }
	  debug_log_print("NDK:LC: 1 and 3");
	} else if (pair_1_4 > pair_2_3
		  && pair_1_4 > pair_2_4
		  && pair_1_4 > pair_3_4) {
	  if (in1 < in4) {
		  good_image1 = &target1;
		  good_image2 = &target4;
	  } else {
		  good_image1 = &target4;
		  good_image2 = &target1;
	  }
	  debug_log_print("NDK:LC: 1 and 4");
	} else if (pair_2_3 > pair_2_4
		  && pair_2_3 > pair_3_4) {
	  if (in2 < in3) {
		  good_image1 = &target2;
		  good_image2 = &target3;
	  } else {
		  good_image1 = &target3;
		  good_image2 = &target2;
	  }
	  debug_log_print("NDK:LC: 2 and 3");
	} else if (pair_2_4 > pair_3_4) {
	  if (in2 < in4) {
		  good_image1 = &target2;
		  good_image2 = &target4;
	  } else {
		  good_image1 = &target4;
		  good_image2 = &target2;
	  }
	  debug_log_print("NDK:LC: 2 and 4");
	} else {
	  if (in3 < in4) {
		  good_image1 = &target3;
		  good_image2 = &target4;
	  } else {
		  good_image1 = &target4;
		  good_image2 = &target3;
	  }
	  debug_log_print("NDK:LC: 3 and 4");
	}

	// Histogram equalization.
	// Optimally we'd apply the same transformation to each image, but this is pretty close.
	histogramEqualization(*good_image1);
	histogramEqualization(*good_image2);

	// Subtract, overwrite first.
	subtract(*good_image1, *good_image2, *good_image1);

	// Threshold for message extraction is average intensity of difference.
	m = mean(*good_image1);
	int threshold = m[0] + m[1] + m[2];
	// debug_log_print("NDK:LC: [threshold: %d]", threshold);
	
	extractMessage(*good_image1, message, width, height, width_blocks, height_blocks, threshold);
}
}