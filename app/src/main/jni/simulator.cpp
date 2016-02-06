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
 * Reorders message accounting for weird embedding.
 */
void parseMessageToBinary(unsigned char *pattern, char *buffer) {
	int p = 0;
	for (int i = 0; i < 8; i++) {
		for (int j = 9; j >= 0; j--) {
			unsigned char b = pattern[i * 10 + j];
			if(b)
				buffer[p++] = 0;
			else
				buffer[p++] = 1;
		}
	}
}

/**
 * Set of four images and corresponding points gotten from running Android code. 92% accuracy.
 */
int main(int argv, char **argc) {
	cv::Mat img1;
	img1 = cv::imread("vmimo1.bmp", 1);
	cv::Mat img2;
	img2 = cv::imread("vmimo2.bmp", 1);
	cv::Mat img3;
	img3 = cv::imread("vmimo3.bmp", 1);
	cv::Mat img4;
	img4 = cv::imread("vmimo4.bmp", 1);

	int width = 1280;
	int height = 720;

	int width_blocks = 10;
	int height_blocks = 8;
	int num_blocks = width_blocks * height_blocks;

	// block encoding
 //    float c0x1 = 280.267365;
 //    float c0y1 = 543.941772;
 //    float c1x1 = 286.725128;
 //    float c1y1 = 147.125107;
 //    float c2x1 = 577.401611;
 //    float c2y1 = 158.468842;
 //    float c3x1 = 586.266479;
 //    float c3y1 = 533.417419;

 //    float c0x2 = 282.925659;
 //    float c0y2 = 544.202271;
 //    float c1x2 = 291.117157;
 //    float c1y2 = 153.630417;
 //    float c2x2 = 580.129944;
 //    float c2y2 = 156.355011;
 //    float c3x2 = 587.155945;
 //    float c3y2 = 535.737549;

 //    float c0x3 = 281.457306;
 //    float c0y3 = 545.584106;
 //    float c1x3 = 288.589355;
 //    float c1y3 = 149.445526;
 //    float c2x3 = 578.264709;
 //    float c2y3 = 161.667664;
 //    float c3x3 = 587.091064;
 //    float c3y3 = 535.294250;

 //    float c0x4 = 281.684906;
 //    float c0y4 = 546.358582;	
 //    float c1x4 = 289.750366;
 //    float c1y4 = 151.903473;
 //    float c2x4 = 579.929993;
 //    float c2y4 = 159.874130;
 //    float c3x4 = 589.030273;
 //    float c3y4 = 536.386536;


	// color encoding 2
	float c0x1 = 561.139832;
	float c0y1 = 524.236938;
    float c1x1 = 514.165771;
    float c1y1 = 128.341705;
    float c2x1 = 737.101196;
    float c2y1 = 184.818466;
    float c3x1 = 817.442444;
    float c3y1 = 536.361328;

    float c0x2 = 562.130127;
    float c0y2 = 523.482178;
    float c1x2 = 516.142212;
    float c1y2 = 127.982834;
    float c2x2 = 738.535950;
    float c2y2 = 185.069641;
    float c3x2 = 817.890686;
    float c3y2 = 536.431885;

    float c0x3 = 562.085327;
    float c0y3 = 522.535339;
    float c1x3 = 516.139587;
    float c1y3 = 127.654999;
    float c2x3 = 738.309570;
    float c2y3 = 184.711700;
    float c3x3 = 817.548218;
    float c3y3 = 535.587280;

    float c0x4 = 517.230042;
    float c0y4 = 549.311157;
    float c1x4 = 521.936096;
    float c1y4 = 68.203064;
    float c2x4 = 840.960815;
    float c2y4 = 80.264313;
    float c3x4 = 824.984680;
    float c3y4 = 547.062500;

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

	unsigned char* message = new unsigned char[num_blocks];
	processFrames(img1, img2, img3, img4, width, height, message, width_blocks, height_blocks, c0x1,c0y1,c1x1,c1y1,c2x1,c2y1,c3x1,c3y1,c0x2,c0y2,c1x2,c1y2,c2x2,c2y2,c3x2,c3y2,c0x3,c0y3,c1x3,c1y3,c2x3,c2y3,c3x3,c3y3,c0x4,c0y4,c1x4,c1y4,c2x4,c2y4,c3x4,c3y4);

	char *readable = new char[num_blocks];
	parseMessageToBinary(message, readable);

	for (int i = 0; i < num_blocks; i++) {
		cout << !!readable[i];
	}
	cout << endl;

	return 0;
}