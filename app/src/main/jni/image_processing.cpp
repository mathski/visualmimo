#include <string.h>
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
    unsigned char message_buff[width_blocks * height_blocks - 4];
    int block_width = width / width_blocks;
    int block_height = height / height_blocks;
	
    // Threshold for message extraction is average intensity of difference.
    Scalar m = mean(image);
    int cutoff = m[0] + m[1] + m[2];
    // debug_log_print("NDK:LC: [threshold: %d]", threshold);
	
    int k = 0;
    for (int i = 0; i < height; i += block_height) {
      for (int j = 0; j < width; j += block_width) {
        // Skip four corners (parity bits)
        if ((i == 0 && (j == 0 || j == width-block_width))
            || (i == height-block_height && (j == 0 || j == width-block_width))) {
          continue;
        }

        // Get region of interest
        Mat block = image(Rect(i, j, block_height, block_width));
        Scalar m = mean(block);
        message_buff[k++] = (m[0] + m[1] + m[2] > cutoff);
      }
    }

    // reorder message correctly
    k = 0;
    for (int i = 0; i < height_blocks; i++) {
			for (int j = width_blocks; j >= 0; j--) {
        message[k++] = message_buff[i * 10 + j];
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
    subtract(img1, img2, diff);

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

    if (topright && bottomright && topleft && bottomleft
        || !topright && !bottomright && !topleft && !bottomleft) {
      debug_log_print("Parity bits are valid.");
      return false;
    } else {
      debug_log_print("Parity bits mismatch!");
      return true;
    }
  }

  void processFrames(Mat (&matImages)[NUM_FRAMES], int width, int height, unsigned char *message,
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

    // Find best pair of frames: NUM_FRAMES nCr 2 choices
    for (int i = 0; i < NUM_FRAMES - 1; i++) {
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
          imshow(strbuff, diffImg);
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

    sprintf(strbuff, "NDK:LC: %d and %d", best1 + 1, best2 + 1);
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
  }
}
