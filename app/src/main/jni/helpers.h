#include <stdlib.h>
#include <cv.h>

extern "C" {
  void sortCorners(std::vector < cv::Point2f > &corners, cv::Point2f center) ;

  void projectiveTransform(cv::Mat &image, cv::Mat &dest, float corners[]);

  void histogramEqualization(cv::Mat & image);
}
