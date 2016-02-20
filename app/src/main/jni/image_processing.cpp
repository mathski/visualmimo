#include <string.h>
#include <limits.h>
#include <cv.h>
#include <highgui.h>

#include "helpers.h"
#include "android_compat.h"

#define NUM_FRAMES 6

using namespace cv;
extern "C" {
  /**
   * Extracts a message from image. Iterates through ROI grid, using average to
   * determine on/off state.
   */
  void extractMessage(Mat &image, unsigned char *message,
                      int width, int height,
                      int width_blocks, int height_blocks) {
    unsigned char message_buff[width_blocks * height_blocks];
    int block_width = width / width_blocks;
    int block_height = height / height_blocks;
	
    // Threshold for message extraction is average intensity of difference.
    Scalar m = mean(image);
    int cutoff = m[0] + m[1] + m[2];
    // debug_log_print("NDK:LC: [threshold: %d]", threshold);
	
    int k = 0;
    for (int i = 0; i < height; i += block_height) {
      for (int j = 0; j < width; j += block_width) {
        // Get region of interest
        Mat block = image(Rect(i, j, block_height, block_width));
        Scalar m = mean(block);
        message_buff[k++] = (m[0] + m[1] + m[2] > cutoff);

#ifndef ON_DEVICE
        // char strbuff[80];
        // sprintf(strbuff, "%d", (m[0] + m[1] + m[2] > cutoff));
        // debug_log_print(strbuff);
        // imshow("patch", block);
        // waitKey(0);
#endif

      }
    }

    // reorder message correctly
    k = 0;
    for (int i = 0; i < height_blocks; i++) {
			for (int j = width_blocks - 1; j >= 0; j--) {
        // Skip four corners (parity bits)
        if (!(i == 0 && (j == 0 || j == width_blocks - 1))
            && !(i == height_blocks - 1 && (j == 0 || j == width_blocks - 1))) {
          message[k++] = message_buff[i * width_blocks + j];
        }
			}
		}
  }

  /**
   * Checks if any corner is "on" in difference, indicating parity mismatch.
   */
  bool isParityMismatch(Mat &img1, Mat &img2, int width, int height,
                        int width_blocks, int height_blocks) {
    int block_width = width / width_blocks;
    int block_height = height / height_blocks;

    cv::Mat diff, block;
    cv::Scalar m;
    cv::absdiff(img1, img2, diff);

    // Threshold for message extraction is average intensity of difference.
    m = mean(diff);
    int cutoff = m[0] + m[1] + m[2];
    // debug_log_print("NDK:LC: [threshold: %d]", threshold);

    block = diff(Rect(0, 0, block_height, block_width));
    m = mean(block);
    bool topright = (m[0] + m[1] + m[2] > cutoff);

    block = diff(Rect(0, width-block_width, block_height, block_width));
    m = mean(block);
    bool bottomright = (m[0] + m[1] + m[2] > cutoff);

    block = diff(Rect(height-block_height, 0, block_height, block_width));
    m = mean(block);
    bool topleft = (m[0] + m[1] + m[2] > cutoff);

    block = diff(Rect(height-block_height, width-block_width, block_height, block_width));
    m = mean(block);
    bool bottomleft = (m[0] + m[1] + m[2] > cutoff);

    if (!topright && !bottomright && !topleft && !bottomleft) {
      debug_log_print("Parity bits are valid.");
      return false;
    } else {
      debug_log_print("Parity bits mismatch!");
      return true;
    }
  }

  /**
   * Sums intensity of parity bits for given image.
   */
  int getParityIntensities(Mat& image, int width, int height, int width_blocks, int height_blocks) {
    int ins = 0;
    cv::Scalar m;

    int block_width = width / width_blocks;
    int block_height = height / height_blocks;

    m = mean(image(Rect(0, 0, block_height, block_width)));
    ins += m[0] + m[1] + m[2];
    m = mean(image(Rect(0, width-block_width, block_height, block_width)));
    ins += m[0] + m[1] + m[2];
    m = mean(image(Rect(height-block_height, 0, block_height, block_width)));
    ins += m[0] + m[1] + m[2];
    m = mean(image(Rect(height-block_height, width-block_width, block_height, block_width)));
    ins += m[0] + m[1] + m[2];
    
    return ins;
  }

  bool processFrames(Mat (&matImages)[NUM_FRAMES], int width, int height, unsigned char *message,
                     int width_blocks, int height_blocks, float (&corners)[NUM_FRAMES][8]) {
    // corners is an array of six arrays of eight corners, x y

    char strbuff[80];

    Scalar m;
    int ins[NUM_FRAMES];

    // Define the destination image
    cv::Mat targets[NUM_FRAMES];
    for (int i = 0; i < NUM_FRAMES; i++) {
      targets[i] = cv::Mat::zeros(width, height, CV_8UC1);
      projectiveTransform(matImages[i], targets[i], corners[i]);

      m = mean(targets[i]);
      ins[i] = m[0] + m[1] + m[2];
    }

    int best1 = 0;
    int best2 = 0;
    int bestDiff = 0;

    int block_width = width / width_blocks;
    int block_height = height / height_blocks;

    // Track highest and lowest corner intensities to determine parity.
    int max_ins = INT_MIN;
    int min_ins = INT_MAX;
	
    // Find best pair of frames: NUM_FRAMES nCr 2 choices
    for (int i = 0; i < NUM_FRAMES - 1; i++) {

      int pIns = getParityIntensities(targets[i], width, height, width_blocks, height_blocks);
      if (pIns > max_ins) {
        max_ins = pIns;
      }
      if (pIns < min_ins) {
        min_ins = pIns;
      }

      for (int k = i + 1; k < NUM_FRAMES; k++) {

        cv::Mat diffImg;
        subtract(targets[i], targets[k], diffImg);
        sprintf(strbuff, "%d - %d", i, k);

        int diff = abs(ins[i] - ins[k]);
        if (diff > bestDiff) {
          int ii = i;
          int kk = k;
          if (ins[ii] < ins[kk]) {
            ii = k;
            kk = i;
          }

          sprintf(strbuff, "checking parity on %d-%d", ii, kk);
          debug_log_print(strbuff);
          subtract(targets[ii], targets[kk], diffImg);

#ifndef ON_DEVICE
          // imshow(strbuff, diffImg);
#endif

          if (isParityMismatch(targets[ii], targets[kk], width, height, width_blocks, height_blocks)) {
            continue;
          }

          bestDiff = diff;
          best1 = ii;
          best2 = kk;
        }
      }
    }

    // message is odd if parity bits are on
    float avgParityIns = (max_ins + min_ins) / 2;

    int pIns = getParityIntensities(targets[best1], width, height, width_blocks, height_blocks);

    bool isOddFrame = pIns > avgParityIns;

    sprintf(strbuff, "NDK:LC: %d and %d", best1 + 1, best2 + 1);
    debug_log_print(strbuff);
    sprintf(strbuff, "NDK:LC: parity: %s", isOddFrame ? "odd" : "even");
    debug_log_print(strbuff);

#ifndef ON_DEVICE
    imshow("best1", targets[best1]);
    imshow("best2", targets[best2]);
#endif


    // Histogram equalization.
    // Optimally we'd apply the same transformation to each image, but this is pretty close.
    histogramEqualization(targets[best1]);
    histogramEqualization(targets[best2]);

    // Subtract, overwrite first.
    subtract(targets[best1], targets[best2], targets[best1]);

#ifndef ON_DEVICE
    imshow("subtract", targets[best1]);
#endif

    extractMessage(targets[best1], message, width, height, width_blocks, height_blocks);

#ifndef ON_DEVICE
    waitKey(0);
#endif

    return isOddFrame;
  }
}
