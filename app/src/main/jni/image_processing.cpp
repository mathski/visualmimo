#include <cmath>
#include <cv.h>
#include <highgui.h>
#include <cstring>

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
   */
  double arrayMedian(double *nums, int size){
    int i, j, numsSize = 0;
    double temp = 0.0;
    double arr[size];

    memcpy(&arr, nums, size * sizeof(double)); // Duplicate the array so we can sort without issue.


    for(i = 0; i < size; i ++){
      for(j = i + 1; j < size; j ++){
        if(arr[j] <= arr[i]){
          temp = arr[i];
          arr[i] = arr[j];
          arr[j] = temp;
        }
      }
    }

    if(size % 2 != 0) return arr[size / 2];
    return ((arr[(size - 1) / 2] + arr[(size + 1) / 2])/2);
  }

  /**
   *
   */
  void findBlockDeltas(Mat &image, double *deltas,
                        const int width, const int height,
                        const int width_blocks, const int height_blocks) {
    double deltas_buff[width_blocks * height_blocks];
    const int block_width = width / width_blocks;
    const int block_height = height / height_blocks;

    double m[height_blocks*width_blocks];

    Mat spl[3];
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

    const double cutoff = arrayMedian(m, 80);

    for (int block_idx = 0; block_idx < k; block_idx ++){
      deltas_buff[block_idx] = m[block_idx] - cutoff;
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


  }

  /**
   * Extracts a message from image. Iterates through ROI grid, using average to
   * determine on/off state.
   */
  int extractMessage(Mat &image, unsigned char *payload,
                     const int width, const int height,
                     const int width_blocks, const int height_blocks) {
    char buff[1000];
    double deltas[width_blocks * height_blocks];
    int mismatches = 0;

    findBlockDeltas(image, deltas, width, height, width_blocks, height_blocks);

    sprintf(buff, "deltas:");
    for (int i = 0; i < width_blocks * height_blocks - 4; i++) {
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
    #ifdef SHOW_IMAGES
        imshow("best1", targets[best1]);
        imshow("best2", targets[best2]);
    #endif
#endif


    // Histogram equalization.
    // Optimally we'd apply the same transformation to each image, but this is pretty close.
//    histogramEqualization(targets[best1]);
//    histogramEqualization(targets[best2]);

    // Subtract, overwrite first.
    subtract(targets[best1], targets[best2], targets[best1]);

#ifndef ON_DEVICE
    #ifdef SHOW_IMAGES
        imshow("subtract", targets[best1]);
    #endif
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