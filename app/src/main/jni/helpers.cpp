#include <stdlib.h>
#include <cv.h>
#include <highgui.h>

#define DEBUG_TAG "NDK_VMIMO"

using namespace cv;

extern "C" {
/**
 * Utility function for projectiveTransform().
 * Sorts corners as: top left, top right, back right, back left.
 */
void sortCorners(std::vector<cv::Point2f>& corners, cv::Point2f center)
{
    std::vector<cv::Point2f> top, bot;

    for (int i = 0; i < corners.size(); i++)
    {
        if (corners[i].y < center.y)
            top.push_back(corners[i]);
        else
            bot.push_back(corners[i]);
    }

    cv::Point2f tl = top[0].x > top[1].x ? top[1] : top[0];
    cv::Point2f tr = top[0].x > top[1].x ? top[0] : top[1];
    cv::Point2f bl = bot[0].x > bot[1].x ? bot[1] : bot[0];
    cv::Point2f br = bot[0].x > bot[1].x ? bot[0] : bot[1];

    corners.clear();
    corners.push_back(tl);
    corners.push_back(tr);
    corners.push_back(br);
    corners.push_back(bl);
}

/**
 * Performs projective transform on image. For given set of corners, skews
 * image to known ratio.
 */
void projectiveTransform(Mat &image, Mat &dest,
                         float c0x, float c0y,
                         float c1x, float c1y,
                         float c2x, float c2y,
                         float c3x, float c3y) {

    //perspective transform
    std::vector<Point2f> corners;
    corners.push_back(cv::Point2f(c0x, c0y));
    corners.push_back(cv::Point2f(c1x, c1y));
    corners.push_back(cv::Point2f(c2x, c2y));
    corners.push_back(cv::Point2f(c3x, c3y));

    // Get mass center
    cv::Point2f center(0,0);
    for (int i = 0; i < corners.size(); i++)
        center += corners[i];

    center *= (1. / corners.size());
    sortCorners(corners, center);


    // Corners of the destination image
    std::vector<cv::Point2f> dest_pts;
    dest_pts.push_back(cv::Point2f(0, 0));
    dest_pts.push_back(cv::Point2f(dest.cols, 0));
    dest_pts.push_back(cv::Point2f(dest.cols, dest.rows));
    dest_pts.push_back(cv::Point2f(0, dest.rows));

    // Get transformation matrix
    cv::Mat transmtx = cv::getPerspectiveTransform(corners, dest_pts);

    // Apply perspective transformation
    cv::warpPerspective(image, dest, transmtx, dest.size());
}

/**
 * Performs histogram equalization on image. Normalizes image intensity.
 * TODO(revan): preserve transform matrix for application to second image.
 */
void histogramEqualization(Mat &image) {
    // Convert from BGR to YCbCr, because we need intensity as its own channel
    cv::cvtColor(image, image, CV_BGR2YCrCb);

    std::vector<Mat> channels;
    split(image, channels);

    cv::equalizeHist(channels[0], channels[0]);

    merge(channels, image);

    cv::cvtColor(image, image, CV_YCrCb2BGR);
}
}

