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

	// color encoding 2
//	float c0x1 = 561.139832;
//	float c0y1 = 524.236938;
//    float c1x1 = 514.165771;
//    float c1y1 = 128.341705;
//    float c2x1 = 737.101196;
//    float c2y1 = 184.818466;
//    float c3x1 = 817.442444;
//    float c3y1 = 536.361328;
//
//    float c0x2 = 562.130127;
//    float c0y2 = 523.482178;
//    float c1x2 = 516.142212;
//    float c1y2 = 127.982834;
//    float c2x2 = 738.535950;
//    float c2y2 = 185.069641;
//    float c3x2 = 817.890686;
//    float c3y2 = 536.431885;
//
//    float c0x3 = 562.085327;
//    float c0y3 = 522.535339;
//    float c1x3 = 516.139587;
//    float c1y3 = 127.654999;
//    float c2x3 = 738.309570;
//    float c2y3 = 184.711700;
//    float c3x3 = 817.548218;
//    float c3y3 = 535.587280;
//
//    float c0x4 = 517.230042;
//    float c0y4 = 549.311157;
//    float c1x4 = 521.936096;
//    float c1y4 = 68.203064;
//    float c2x4 = 840.960815;
//    float c2y4 = 80.264313;
//    float c3x4 = 824.984680;
//    float c3y4 = 547.062500;

    // color encoding 5
    // float c0x1 = 419.906647;
    // float c0y1 = 540.113342;
    // float c1x1 = 414.487976;
    // float c1y1 = 47.731567;
    // float c2x1 = 778.132080;
    // float c2y1 = 63.153809;
    // float c3x1 = 766.704895;
    // float c3y1 = 527.178589;

    // float c0x2 = 419.302734;
    // float c0y2 = 538.758545;
    // float c1x2 = 412.672150;
    // float c1y2 = 46.713287;
    // float c2x2 = 776.376892;
    // float c2y2 = 61.155579;
    // float c3x2 = 766.237427;
    // float c3y2 = 525.390076;

    // float c0x3 = 421.045807;
    // float c0y3 = 537.436646;
    // float c1x3 = 413.686523;
    // float c1y3 = 46.514069;
    // float c2x3 = 776.634216;
    // float c2y3 = 59.898651;
    // float c3x3 = 768.062500;
    // float c3y3 = 524.088806;

    // float c0x4 = 423.367920;
    // float c0y4 = 537.529175;
    // float c1x4 = 415.584167;
    // float c1y4 = 45.812134;
    // float c2x4 = 778.336243;
    // float c2y4 = 59.164307;
    // float c3x4 = 770.374634;
    // float c3y4 = 523.381592;

    // color encoding 10
    // float c0x1 = 496.674622;
    // float c0y1 = 577.724243;
    // float c1x1 = 496.370636;
    // float c1y1 = 86.442291;
    // float c2x1 = 883.756836;
    // float c2y1 = 97.062317;
    // float c3x1 = 873.314453;
    // float c3y1 = 570.258301;

    // float c0x2 = 496.423950;
    // float c0y2 = 578.276855;
    // float c1x2 = 498.263306;
    // float c1y2 = 86.977539;
    // float c2x2 = 883.595032;
    // float c2y2 = 98.896271;
    // float c3x2 = 874.727173;
    // float c3y2 = 572.211426;

    // float c0x3 = 496.380554;
    // float c0y3 = 578.400818;
    // float c1x3 = 498.275879;
    // float c1y3 = 87.401245;
    // float c2x3 = 883.751587;
    // float c2y3 = 99.739899;
    // float c3x3 = 873.334351;
    // float c3y3 = 572.122681;

    // float c0x4 = 496.464905;
    // float c0y4 = 575.433228;
    // float c1x4 = 495.461426;
    // float c1y4 = 84.598602;
    // float c2x4 = 881.297852;
    // float c2y4 = 94.782959;
    // float c3x4 = 873.117676;
    // float c3y4 = 567.225708;


	int num_blocks = (width_blocks * height_blocks - 4)/2 - 4;


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
	struct NDK_RESULT r = processFrames(imgs, width, height, message, width_blocks, height_blocks, corners);

	cout << "index: " << r.index << endl;
	cout << "mismatches: " << r.mismatches << endl;

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
