#include <stdlib.h>
#include <cv.h>

extern "C" {
void sortCorners(std::vector < cv::Point2f > &corners, cv::Point2f center) ;

void projectiveTransform(cv::Mat &image, cv::Mat &dest,
                         float c0x, float c0y,
                         float c1x, float c1y,
                         float c2x, float c2y,
                         float c3x, float c3y);

void histogramEqualization(cv::Mat & image);
}