#include <cv.h>
#define NUM_FRAMES 6
using namespace cv;

extern "C" {
  void extractMessage(Mat &image, unsigned char *message,
                      int width, int height,
                      int width_blocks, int height_blocks);

  bool processFrames(Mat (&matImages)[NUM_FRAMES], int width, int height, unsigned char *message,
                     int width_blocks, int height_blocks, float (&corners)[NUM_FRAMES][8]);
}
