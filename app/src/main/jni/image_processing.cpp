#include <cmath>
#include <cv.h>
#include <highgui.h>

#include "helpers.h"
#include "android_compat.h"
#include "ndk_result.h"

#define NUM_FRAMES 6

#define INDEX_LENGTH 4
#define PAYLOAD_CHARS 4

using namespace cv;
extern "C" {
  /**
   * Extracts a message from image. Iterates through ROI grid, using average to
   * determine on/off state.
   * Returns: number of mismatches
   */
  int extractMessage(Mat &image, unsigned char *payload,
                      const int width, const int height,
                      const int width_blocks, const int height_blocks) {
    double deltas_buff[width_blocks * height_blocks];
    double deltas[width_blocks * height_blocks];
    const int block_width = width / width_blocks;
    const int block_height = height / height_blocks;
    int mismatches = 0;

    // Threshold for message extraction is average intensity of difference.
    Scalar m = mean(image);
    const double threshold = m[0] + m[1] + m[2];

    char buff[1000];
    sprintf(buff, "NDK:LC: [threshold: %f]", threshold);
    debug_log_print(buff);

    int k = 0;
    for (int i = 0; i < height; i += block_height) {
      for (int j = 0; j < width; j += block_width) {
        // Get region of interest
        Mat block = image(Rect(i, j, block_height, block_width));
        Scalar m = mean(block);
        deltas_buff[k++] = (m[0] + m[1] + m[2]) - threshold;

//#ifndef ON_DEVICE
//         char strbuff[80];
//         sprintf(strbuff, "%d", (m[0] + m[1] + m[2] > cutoff));
//         debug_log_print(strbuff);
//         imshow("patch", block);
//         waitKey(0);
//#endif

      }
    }

    // reorder message correctly
    k = 0;
    for (int i = 0; i < height_blocks; i++) {
      for (int j = width_blocks - 1; j >= 0; j--) {
        // Skip four corners (parity bits)
        if (!(i == 0 && (j == 0 || j == width_blocks - 1))
            && !(i == height_blocks - 1 && (j == 0 || j == width_blocks - 1))) {
          deltas[k++] = deltas_buff[i * width_blocks + j];
        }
      }
    }

    sprintf(buff, "deltas:");
    for (int i = 0; i < k; i++) {
      sprintf(buff, "%s, %f", buff, deltas[i]);
    }
    debug_log_print(buff);


    // from MATLAB: pattern = [index messagepattern invert(index) invert(messagepattern)];
    // extract payload from consensus doubling
    const int payload_length = (width_blocks * height_blocks - 4) / 2;
    for (int i = 0; i < payload_length; i++) {
      bool state;
      if (deltas[i] > 0 && deltas[i + payload_length] < 0) {
        state = true;
      } else if (deltas[i] < 0 && deltas[i + payload_length] > 0) {
        state = false;
      } else {
        mismatches += (i < INDEX_LENGTH ? 100 : 1); // a mismatch in parity counts as 100

        // two bits don't agree, so take the one further from the threshold
        if (std::abs(deltas[i]) > std::abs(deltas[i + payload_length])) {
          state = deltas[i] > 0;
        } else {
          state = deltas[i + payload_length] < 0;
        }

        sprintf(buff, "Warning: duplicate bits for index %d don't agree.\tUsing most significant value between: %f and %f: %s",
                i, deltas[i], deltas[i + payload_length], state ? "true" : "false");
        debug_log_print(buff);
      }

      payload[i] = state;
    }

    return mismatches;
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
    double cutoff = m[0] + m[1] + m[2];
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


  struct NDK_RESULT processFrames(Mat (&matImages)[NUM_FRAMES], int width, int height, unsigned char *message,
                     int width_blocks, int height_blocks, float (&corners)[NUM_FRAMES][8]) {
    // corners is an array of six arrays of eight corners, x y
    // returns index

    char strbuff[80];

    Scalar m;
    int ins[NUM_FRAMES];
    unsigned char payload[(width_blocks * height_blocks - 4)/2];

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

    int mismatches = extractMessage(targets[best1], payload, width, height, width_blocks, height_blocks);


    // extract index and message from combined array `message`

    int index = 0;
    for (int i = 0; i < INDEX_LENGTH; i++) {
      if (payload[i]) {
        index += (1 << INDEX_LENGTH - i - 1);
      }
    }

    const int message_length = PAYLOAD_CHARS * 7;
    // copy message to drop index
    for (int i = 0; i < message_length; i++) {
      message[i] = payload[i + INDEX_LENGTH];
    }

#ifndef ON_DEVICE
    waitKey(0);
#endif

    struct NDK_RESULT r;
    r.index = index;
    r.mismatches = mismatches;
    return r;
  }
}
