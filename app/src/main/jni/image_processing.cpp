#include <string.h>
#include <cv.h>
#include <highgui.h>
#include <numeric>

#include "helpers.h"
#include "android_compat.h"

using namespace cv;
extern "C" {
/**
 * Extracts a message from image. Iterates through ROI grid, using average to
 * determine on/off state.
 */
double arrayMedian(double nums[]){
      int i, j, numsSize = 0;
	  double median, temp = 0.0;
	  double* arr = new double[80];
      numsSize = 80;
	  
	  for(i = 0; i < numsSize; i ++){
		  arr[i] = nums[i];
	  }
	  
      for(i = 0; i < numsSize; i ++){
          for(j = i + 1; j < numsSize; j ++){
              if(arr[j] <= arr[i]){
                  temp = arr[i];
                  arr[i] = arr[j];
                  arr[j] = temp;
              }
          }
      }
	  
      if(numsSize % 2 != 0) median = arr[numsSize / 2];
      else median = ((arr[(numsSize - 1) / 2] + arr[(numsSize + 1) / 2])/2);
      return median;
  }

  void extractMessage(Mat &image, unsigned char *message,
          int width, int height,
          int width_blocks, int height_blocks, int threshold) {

      Mat spl[3];
      double cutoff = 0;
      int block_width = width / width_blocks;
      int block_height = height / height_blocks;
      double* m = new double[height_blocks*width_blocks];

      split(image,spl);
      Mat image_mag = image;
	  int k = 0;
      for (int i = 0; i < height; i += block_height) {
          for (int j = 0; j < width; j += block_width) {
			  double numDistances = block_width * block_height;
			  double total = 0.0;
			  
			  // This is the more correct way to do this.
			  for(int l = 0; l < block_width; l += 1){
				 for(int m = 0; m < block_height; m += 1){
					Mat pixels = image_mag(Rect(i + m, j + l, 1, 1));
					Scalar averages = mean(pixels);
					total += sqrt( (averages[0] * averages[0]) + (averages[1] * averages[1]) + (averages[2] * averages[2]) );
				 } 
			  }
			  m[k] = total / numDistances;
			  k = k + 1;
          }
      }
	  
      cutoff = arrayMedian(m);
	  
	  for(int l = 0; l < 80; l ++){
		  std::cout << "val " << m[l] << std::endl;
	  }
	  std::cout << "Total number of blocks: " << k << std::endl;
      for (int block_idx = 0; block_idx < k; block_idx ++){
		 message[block_idx] = (m[block_idx] > cutoff);
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

	// good_image1 = &target3;
	// good_image2 = &target2;
	
	// Histogram equalization.
	// Optimally we'd apply the same transformation to each image, but this is pretty close.
	// histogramEqualization(*good_image1);
	// histogramEqualization(*good_image2);

	// Subtract, overwrite first.
	// imshow("Best1", *good_image1);
	// imshow("Best2", *good_image2);
	imwrite("storage/emulated/0/Pictures/best1.png", *good_image1);
	imwrite("storage/emulated/0/Pictures/best2.png", *good_image2);
	//imwrite("best1.png", *good_image1);
	//imwrite("best2.png", *good_image2);
	// subtract(*good_image1, *good_image2, *good_image1);
	
	// imshow("Subtract", *good_image1);
	imwrite("storage/emulated/0/Pictures/subtract.png", *good_image1);
	// imwrite("subtract.png", *good_image1);
	// waitKey(0);
	
	extractMessage(*good_image1, message, width, height, width_blocks, height_blocks, 0);
	
}
}