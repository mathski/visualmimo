#include <cv.h>
using namespace cv;

extern "C" {
void extractMessage(Mat &image, unsigned char *message,
		int width, int height,
		int width_blocks, int height_blocks,
		int threshold);

double arrayMedian(double nums[]);
		
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
	);
}