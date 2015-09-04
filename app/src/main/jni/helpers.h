#include <jni.h>
#include <stdlib.h>
#include <cv.h>

extern "C" {
void sortCorners(std::vector < cv::Point2f > &corners, cv::Point2f center) ;

void projectiveTransform(cv::Mat &image, cv::Mat &dest,
                         jfloat c0x, jfloat c0y,
                         jfloat c1x, jfloat c1y,
                         jfloat c2x, jfloat c2y,
                         jfloat c3x, jfloat c3y);

void histogramEqualization(cv::Mat & image);
}