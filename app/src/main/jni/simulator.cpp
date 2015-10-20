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