#include <cv.h>
using namespace cv;

extern "C" {
  void extractMessage(Mat &image, unsigned char *message,
                      int width, int height,
                      int width_blocks, int height_blocks);

  void processFrames(Mat matImages[], int width, int height, unsigned char *message,
                     int width_blocks, int height_blocks, float corners[][8]);
}
