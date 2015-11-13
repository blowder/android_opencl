package billcollector.app.utils;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sesshoumaru on 9/13/15.
 */
public class OpenCvUtils {
    public static void sharpen(File source, File target) {
        Mat opencvSource = Highgui.imread(source.getAbsolutePath());
        Mat temp = new Mat();
        Imgproc.GaussianBlur(opencvSource, temp, new Size(0, 0), 3);
        Core.addWeighted(opencvSource, 1.5, temp, -0.5, 0, temp);
        Highgui.imwrite(target.getAbsolutePath(), temp);
    }

    public static void adaptiveThreshold(File source, File target) {
        Mat opencvSource = Highgui.imread(source.getAbsolutePath());
        Mat temp = new Mat();
        Imgproc.cvtColor(opencvSource, temp, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(temp, temp, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 8);
        Highgui.imwrite(target.getAbsolutePath(), temp);
    }

    public static List<Rect> detectLetters(File source) {
        Mat img = Highgui.imread(source.getAbsolutePath());
        List<Rect> boundRect = new ArrayList<Rect>();
        Mat img_gray = new Mat(), img_sobel = new Mat(), img_threshold = new Mat(), element;
        Imgproc.cvtColor(img, img_gray, Imgproc.COLOR_BGR2GRAY);
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


}
