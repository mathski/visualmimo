#include <cv.h>
#include "ndk_result.h"

#define NUM_FRAMES 6
using namespace cv;

extern "C" {
  int extractMessage(Mat &image, unsigned char* payload,
                      int width, int height,
                      int width_blocks, int height_blocks);

  struct NDK_RESULT processFrames(Mat (&matImages)[NUM_FRAMES], int width, int height, unsigned char *message,
                     int width_blocks, int height_blocks, float (&corners)[NUM_FRAMES][8]);

  double arrayMedian(double nums[]);
}