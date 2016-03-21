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
	imgs[0] = cv::imread("vmimo0.bmp", 1);
	imgs[1] = cv::imread("vmimo1.bmp", 1);
	imgs[2] = cv::imread("vmimo2.bmp", 1);
	imgs[3] = cv::imread("vmimo3.bmp", 1);
	imgs[4] = cv::imread("vmimo4.bmp", 1);
	imgs[5] = cv::imread("vmimo5.bmp", 1);
	int width = 1280;
	int height = 720;

	int width_blocks = 10;
	int height_blocks = 8;
	int num_blocks = width_blocks * height_blocks - 4;

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
  // float corners[][8] = {
  //   {
  //     362.516357,
  //     605.100464,
  //     377.041260,
  //     147.544556,
  //     709.030701,
  //     152.422897,
  //     722.891296,
  //     598.115234},
  //   {
  //     360.996368,
  //     606.771057,
  //     376.240051,
  //     147.314270,
  //     709.316162,
  //     152.773453,
  //     723.305847,
  //     599.862915},
  //   {
  //     361.009644,
  //     606.220398,
  //     375.817963,
  //     147.297836,
  //     708.586670,
  //     152.681671,
  //     722.329956,
  //     599.248169},
  //   {
  //     364.059143,
  //     602.956970,
  //     376.601257,
  //     147.324875,
  //     707.931580,
  //     152.023804,
  //     719.101196,
  //     596.543823},
  //   {
  //     372.200623,
  //     596.261841,
  //     382.274658,
  //     152.824921,
  //     708.835327,
  //     148.796066,
  //     715.821045,
  //     594.211670},
  //   {
  //     363.593567,
  //     603.181335,
  //     377.503876,
  //     147.637177,
  //     708.767578,
  //     151.786713,
  //     720.810059,
  //     596.969727}
  // };


  // // 3 and 6
  // // m2
  // float corners[][8] = {
  // {
  //   450.042053,
  //   525.253540,
  //   447.877472,
  //   171.167969,
  //   712.058105,
  //   162.176208,
  //   719.615112,
  //   522.501465
  //   },
  // {
  //   446.564911,
  //   530.655396,
  //   443.179443,
  //   160.195602,
  //   707.952148,
  //   169.482712,
  //   716.808533,
  //   519.711914
  //   },
  // {
  //   449.057404,
  //   532.018555,
  //   443.443512,
  //   159.372482,
  //   707.519226,
  //   168.932190,
  //   715.309082,
  //   517.923950
  //   },
  // {
  //   446.596069,
  //   530.689209,
  //   443.459656,
  //   161.046082,
  //   708.270325,
  //   169.816559,
  //   717.900574,
  //   520.255066
  //   },
  // {
  //   446.375854,
  //   530.291870,
  //   443.609985,
  //   161.219055,
  //   709.012268,
  //   169.280807,
  //   718.416443,
  //   520.407532
  //   },
  // {
  //   448.411987,
  //   531.661377,
  //   443.923157,
  //   159.459045,
  //   707.730042,
  //   169.778992,
  //   716.960632,
  //   519.175293
  //   }
  // };




//   02-17 05:27:14.719    2588-7721/com.android.visualmimo D/NDK_VMIMO﹕ checking parity on 0-2
// 02-17 05:27:14.750    2588-7721/com.android.visualmimo D/NDK_VMIMO﹕ Parity bits are valid.
// 02-17 05:27:14.774    2588-7721/com.android.visualmimo D/NDK_VMIMO﹕ checking parity on 0-3
// 02-17 05:27:14.811    2588-7721/com.android.visualmimo D/NDK_VMIMO﹕ Parity bits are valid.
// 02-17 05:27:15.012    2588-7721/com.android.visualmimo D/NDK_VMIMO﹕ checking parity on 4-3
// 02-17 05:27:15.060    2588-7721/com.android.visualmimo D/NDK_VMIMO﹕ Parity bits are valid.
// 02-17 05:27:15.114    2588-7721/com.android.visualmimo D/NDK_VMIMO﹕ NDK:LC: 5 and 4
float corners[][8] = {
    {
    258.249176,
    583.992737,
    270.976501,
    263.193512,
    508.173279,
    268.709503,
    515.561096,
    574.196899
    },
    {
    257.797302,
    584.091003,
    270.878204,
    263.293671,
    508.171997,
    268.756378,
    515.455078,
    574.383057
    },
    {
    257.819275,
    584.167847,
    270.600433,
    263.393616,
    507.771606,
    268.944641,
    515.066833,
    574.402588
    },
    {
    256.576660,
    584.168884,
    270.680359,
    263.778290,
    508.025299,
    269.320435,
    515.426819,
    574.641907
    },
    {
    255.103943,
    585.580383,
    269.937256,
    263.803131,
    508.682495,
    268.731323,
    516.646729,
    575.666199
    },
    {
    255.626709,
    585.430786,
    269.945282,
    263.710510,
    508.518677,
    268.480011,
    516.485229,
    575.384277
    }
};
// 02-17 05:27:15.413    2588-7721/com.android.visualmimo D/NDK_VMIMO﹕ width:1280, height:720
// 02-17 05:27:15.413    2588-7721/com.android.visualmimo I/System.out﹕ MESSAGE:
// 02-17 05:27:15.413    2588-7721/com.android.visualmimo I/System.out﹕ S X X . . . . . X S
// 02-17 05:27:15.413    2588-7721/com.android.visualmimo I/System.out﹕ X . . . . . X X . .
// 02-17 05:27:15.413    2588-7721/com.android.visualmimo I/System.out﹕ . X . X X . . X X .
// 02-17 05:27:15.414    2588-7721/com.android.visualmimo I/System.out﹕ X X . . X . . X . .
// 02-17 05:27:15.414    2588-7721/com.android.visualmimo I/System.out﹕ . . . X X X . . . X
// 02-17 05:27:15.414    2588-7721/com.android.visualmimo I/System.out﹕ . X X X . . . . X X
// 02-17 05:27:15.414    2588-7721/com.android.visualmimo I/System.out﹕ X X . . X X X X X X
// 02-17 05:27:15.414    2588-7721/com.android.visualmimo I/System.out﹕ S X . . . . . . . S
// 02-17 05:27:15.414    2588-7721/com.android.visualmimo I/System.out﹕ MESSAGE MATLAB: [1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, ];
// 02-17 05:27:15.414    2588-7721/com.android.visualmimo I/System.out﹕ ``bfdAbpy~
// 02-17 05:27:15.414    2588-7721/com.android.visualmimo I/System.out﹕ 0.7894736842105263


  unsigned char* message = new unsigned char[num_blocks];
	processFrames(imgs, width, height, message, width_blocks, height_blocks, corners);

#ifndef NO_INDEX
	for (int i = 0; i < num_blocks; i++) {
#else
  // 3 chars * 7 bits = 21
  for (int i = 0; i < 21; i++) {
#endif
		cout << !!message[i];
	}
	cout << endl;

	return 0;
}
