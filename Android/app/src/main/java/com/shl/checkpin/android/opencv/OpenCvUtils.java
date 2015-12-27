package com.shl.checkpin.android.opencv;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;

/**
 * Created by sesshoumaru on 9/13/15.
 */
public class OpenCvUtils {
    //need adaptive threshold of canny for work
    public static Mat getHoughLines(Mat source) {
        Mat lines = new Mat();
        int threshold = 150;
        int minLineSize = 100;
        int lineGap = 50;
        Mat inverted = new Mat();
        if (source.type() != CvType.CV_8UC1)
            Imgproc.cvtColor(source, source, Imgproc.COLOR_BGR2GRAY);
        Core.bitwise_not(source, inverted);
        Imgproc.HoughLinesP(inverted, lines, 1, Math.PI / 180, threshold, minLineSize, lineGap);
        return lines;
    }

    public static Mat denoise(Mat source, Size maxNoiseSize) {
        Mat result = new Mat();
        Mat kernel = new Mat(maxNoiseSize, CvType.CV_8UC1, new Scalar(255));
        Imgproc.morphologyEx(source, result, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(result, result, Imgproc.MORPH_CLOSE, kernel);
        return result;
    }

    public static Mat resize(Mat source, int width) {
        Mat result = new Mat();
        int divider = Math.max(source.cols(), source.rows()) / width;
        Size size = new Size(source.cols() / divider, source.rows() / divider);
        Imgproc.resize(source, result, size);
        return result;
    }

    public static Map<Integer, Integer> calculateAnglesQuantity(Mat lines) {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        for (int x = 0; x < lines.cols(); x++) {
            double[] vec = lines.get(0, x);
            double angle = Math.atan2(vec[3] - vec[1], vec[2] - vec[0]);
            int angleInDegrees = (int) Math.round(angle * 180 / Math.PI);
            int quantity = result.get(angleInDegrees) != null ? result.get(angleInDegrees) : 0;
            result.put(angleInDegrees, quantity + 1);
        }
        return result;
    }

    public static void rotate(Mat source, Mat target, double degreeAngle) {
        int len = Math.max(source.cols(), source.rows());
        Mat rotationMat = Imgproc.getRotationMatrix2D(new Point(len / 2, len / 2), degreeAngle, 1);
        Imgproc.warpAffine(source, target, rotationMat, new Size(len, len));
    }

    public static void sharpen(File source, File target) {
        Mat opencvSource = Highgui.imread(source.getAbsolutePath());
        Mat temp = new Mat();
        Imgproc.GaussianBlur(opencvSource, temp, new Size(0, 0), 3);
        Core.addWeighted(opencvSource, 1.5, temp, -0.5, 0, temp);
        Highgui.imwrite(target.getAbsolutePath(), temp);
    }


    public static Mat adaptiveThreshold(Mat source) {
        Mat result = new Mat();
        if (source.type() != CvType.CV_8UC1)
            Imgproc.cvtColor(source, result, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(result, result, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 21, 10);
        return result;
    }

    public static void convexHull(File source, File target) {
        Mat opencvSource = Highgui.imread(source.getAbsolutePath());
        Mat temp = new Mat();
        Mat thresholded = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.cvtColor(opencvSource, temp, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(temp, thresholded, 127, 255, 0);
        Imgproc.findContours(thresholded, contours, temp, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        for (MatOfPoint point : contours) {
            if (Imgproc.contourArea(point) > 5000) {
                MatOfInt hull = new MatOfInt();
                Imgproc.convexHull(point, hull);
                MatOfPoint2f result = new MatOfPoint2f();
                Imgproc.approxPolyDP(new MatOfPoint2f(new MatOfFloat(hull)), result, 0.1 * Imgproc.arcLength(new MatOfPoint2f(hull), true), true);
                //if
                List<MatOfPoint> result2 = new ArrayList<MatOfPoint>();
                Imgproc.drawContours(opencvSource, result2, 0, new Scalar(0, 255, 0), 2);
                Highgui.imwrite(target.getAbsolutePath(), opencvSource);
            }
        }

    }

    public static List<Rect> detectLetters2(Mat img) {
        List<Rect> boundRect = new ArrayList<Rect>();
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
        Mat result = img.clone();

        if (result.type() != CvType.CV_8UC1)
            Imgproc.cvtColor(result, result, Imgproc.COLOR_BGR2GRAY);

        Imgproc.morphologyEx(result, result, Imgproc.MORPH_GRADIENT, element);
        Imgproc.threshold(result, result, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);

        element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 1));
        Imgproc.morphologyEx(result, result, Imgproc.MORPH_CLOSE, element);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(result, contours, result, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        for (MatOfPoint contour : contours) {
            MatOfPoint2f tempContour = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contour.toArray()), tempContour, 3, true);
            Rect appRect = Imgproc.boundingRect(new MatOfPoint(tempContour.toArray()));
            if (appRect.width > appRect.height)
                boundRect.add(appRect);
        }
        return boundRect;
    }

    @Deprecated
    public static List<Rect> detectLetters(Mat img) {
        //Mat img = Highgui.imread(source.getAbsolutePath());
        List<Rect> boundRect = new ArrayList<Rect>();
        Mat img_gray = img.clone(), img_sobel = new Mat(), img_threshold = new Mat(), element;
        if (img_gray.type() != CvType.CV_8UC1)
            Imgproc.cvtColor(img_gray, img_gray, Imgproc.COLOR_BGR2GRAY);
        //Imgproc.Sobel(img_gray,img_sobel, img_gray.depth(), 1, 0);
        Imgproc.Sobel(img_gray, img_sobel, img_gray.depth(), 1, 0, 3, 1, 0, Imgproc.BORDER_DEFAULT);
        //cv::Sobel(img_gray, img_sobel, CV_8U, 1, 0, 3, 1, 0, cv::BORDER_DEFAULT);
        Imgproc.threshold(img_sobel, img_threshold, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
        element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(17, 3));
        Imgproc.morphologyEx(img_threshold, img_threshold, Imgproc.MORPH_CLOSE, element); //Does the trick

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Imgproc.findContours(img_threshold, contours, img_threshold, 0, 1);

        List<MatOfPoint2f> contours_poly = new ArrayList<MatOfPoint2f>();
        //std::vector < std::vector < cv::Point >> contours_poly(contours.size());

        for (int i = 0; i < contours.size(); i++) {
            //if (contours.get(i).width() > 100) {
            MatOfPoint2f tempCountur = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), tempCountur, 3, true);
            contours_poly.add(tempCountur);
            Rect appRect = Imgproc.boundingRect(new MatOfPoint(tempCountur.toArray()));
            if (appRect.width > appRect.height)
                boundRect.add(appRect);
            //}

        }
        return boundRect;
    }

    public static Set<Rect> findSquares(final Mat sourceImage) {
        Set<Rect> sqares = new HashSet<Rect>();
        //squares.clear();

        int thresh = 50, N = 11;

        Mat pyr, timing, gray = new Mat(sourceImage.size(), CvType.CV_8UC1);
        pyr = new Mat(sourceImage.size(), CvType.CV_8UC1);
        timing = new Mat(sourceImage.size(), CvType.CV_8UC1);

        List<Mat> grayO = new ArrayList<Mat>();
        List<Mat> timing1 = new ArrayList<Mat>();

        // down-scale and upscale the image to filter out the noise
        Imgproc.pyrDown(sourceImage, pyr, new Size(sourceImage.cols() / 2, sourceImage.rows() / 2));
        Imgproc.pyrUp(pyr, timing, sourceImage.size());

        //here could be a problem
        timing1.add(0, timing); // or timing1.add(timing)
        grayO.add(0, new Mat(timing.size(), timing.type()));

        // find squares in every color plane of the image
        for (int c = 0; c < 3; c++) {
            int ch[] = {c, 0};
            MatOfInt fromto = new MatOfInt(ch);
            Core.mixChannels(timing1, grayO, fromto);

            // try several threshold levels
            for (int l = 0; l < N; l++) {
                Mat output = grayO.get(0);
                // hack: use Canny instead of zero threshold level.
                // Canny helps to catch squares with gradient shading
                if (l == 0) {
                    // apply Canny. Take the upper threshold from slider
                    // and set the lower to 0 (which forces edges merging)
                    Imgproc.Canny(output, gray, 5, thresh);
                    // dilate canny output to remove potential
                    // holes between edge segments
                    Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1);
                } else {
                    // apply threshold if l!=0:
                    //     tgray(x,y) = gray(x,y) < (l+1)*255/N ? 255 : 0
                    //gray = grayO >= (l + 1) * 255 / N;
                    //TODO find out why threshold don`t work with find contours
                    //Imgproc.threshold(output, gray, (l + 1) * 255 / N, 255, Imgproc.THRESH_BINARY);
                }

                // find contours and store them all as a list
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                // gray.convertTo(gray, CvType.CV_32SC1);
                // Mat copyImage = gray.clone();

                Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                //vector<Point> approx;
                MatOfPoint2f approxCurve = new MatOfPoint2f();

                // test each contour
                for (int i = 0; i < contours.size(); i++) {
                    // approximate contour with accuracy proportional
                    // to the contour perimeter
                    MatOfPoint tempContour = contours.get(i);
                    MatOfPoint2f newMat = new MatOfPoint2f(tempContour.toArray());
                    int contourSize = (int) tempContour.total();

                    Imgproc.approxPolyDP(newMat, approxCurve, contourSize * 0.02, true);

                    // square contours should have 4 vertices after approximation
                    // relatively large area (to filter out noisy contours)
                    // and be convex.
                    // Note: absolute value of an area is used because
                    // area may be positive or negative - in accordance with the
                    // contour orientation
                    MatOfPoint points = new MatOfPoint(approxCurve.toArray());

                    if (points.toArray().length == 4
                            && (Math.abs(Imgproc.contourArea(points)) > 1000)
                            && Imgproc.isContourConvex(points)) {
                        double maxCosine = 0;

                        for (int j = 2; j < 5; j++) {
                            // find the maximum cosine of the angle between joint edges
                            double cosine = Math.abs(angle(points.toArray()[j % 4], points.toArray()[j - 2], points.toArray()[j - 1]));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        // if cosines of all angles are small
                        // (all angles are ~90 degree) then write quandrante
                        // vertices to resultant sequence
                        if (maxCosine < 0.3) {
                            Rect rect = Imgproc.boundingRect(points);
                            sqares.add(rect);
                        }

                    }
                }
            }
        }
        return sqares;
    }

    private static double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    public static Mat erode(Mat source) {
        Mat result = new Mat();
        int size = Math.max(source.rows(), source.cols()) / 100;
        size = size == 0 ? 1 : size;
        Imgproc.erode(source, result, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(size, size)));
        return result;
    }
}
