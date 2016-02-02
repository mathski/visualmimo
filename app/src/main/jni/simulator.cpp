#define SIMULATOR

#include <iostream>
#include <stdlib.h>
#include <string.h>
#include "opencv2/opencv.hpp"
#include "opencv2/highgui/highgui.hpp"


#include "image_processing.h"

using namespace cv;
using namespace std;

/**
 * Set of four images and corresponding points gotten from running Android code. 92% accuracy.
 */
int main(int argv, char **argc) {
  cv::Mat imgs[6];
	imgs[0] = cv::imread("vmimo1.bmp", 1);
	imgs[1] = cv::imread("vmimo2.bmp", 1);
	imgs[2] = cv::imread("vmimo3.bmp", 1);
	imgs[3] = cv::imread("vmimo4.bmp", 1);
	imgs[4] = cv::imread("vmimo5.bmp", 1);
	imgs[5] = cv::imread("vmimo6.bmp", 1);
	int width = 1280;
	int height = 720;

	int width_blocks = 10;
	int height_blocks = 8;
	int num_blocks = width_blocks * height_blocks;

  /* single frame corners *
     float c0x1 = 280.267365;
     float c0y1 = 543.941772;
     float c1x1 = 286.725128;
     float c1y1 = 147.125107;
     float c2x1 = 577.401611;
     float c2y1 = 158.468842;
     float c3x1 = 586.266479;
     float c3y1 = 533.417419;

     float c0x2 = 282.925659;
     float c0y2 = 544.202271;
     float c1x2 = 291.117157;
     float c1y2 = 153.630417;
     float c2x2 = 580.129944;
     float c2y2 = 156.355011;
     float c3x2 = 587.155945;
     float c3y2 = 535.737549;

     float c0x3 = 281.457306;
     float c0y3 = 545.584106;
     float c1x3 = 288.589355;
     float c1y3 = 149.445526;
     float c2x3 = 578.264709;
     float c2y3 = 161.667664;
     float c3x3 = 587.091064;
     float c3y3 = 535.294250;

     float c0x4 = 281.684906;
     float c0y4 = 546.358582;	
     float c1x4 = 289.750366;
     float c1y4 = 151.903473;
     float c2x4 = 579.929993;
     float c2y4 = 159.874130;
     float c3x4 = 589.030273;
     float c3y4 = 536.386536;
  */
    
  //02-01 01:31:58.827    8286-9862/? D/NDK_VMIMO﹕ NDK:LC: 2 and 4
  float corners[][8] = {
    {
      362.516357,
      605.100464,
      377.041260,
      147.544556,
      709.030701,
      152.422897,
      722.891296,
      598.115234},
    {
      360.996368,
      606.771057,
      376.240051,
      147.314270,
      709.316162,
      152.773453,
      723.305847,
      599.862915},
    {
      361.009644,
      606.220398,
      375.817963,
      147.297836,
      708.586670,
      152.681671,
      722.329956,
      599.248169},
    {
      364.059143,
      602.956970,
      376.601257,
      147.324875,
      707.931580,
      152.023804,
      719.101196,
      596.543823},
    {
      372.200623,
      596.261841,
      382.274658,
      152.824921,
      708.835327,
      148.796066,
      715.821045,
      594.211670},
    {
      363.593567,
      603.181335,
      377.503876,
      147.637177,
      708.767578,
      151.786713,
      720.810059,
      596.969727}
  };

  unsigned char* message = new unsigned char[num_blocks];
	processFrames(imgs, width, height, message, width_blocks, height_blocks, corners);

	for (int i = 0; i < num_blocks; i++) {
		cout << !!message[i];
	}
	cout << endl;

	return 0;
}
