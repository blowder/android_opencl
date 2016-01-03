package com.shl.checkpin.android.opencv;

import com.shl.checkpin.android.canvas.Circle;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;

/**
 * Created by sesshoumaru on 17.12.15.
 */
public class ImageProcessingService {

    public File detectAndCorrectSkew(File source, File target) {
        int lowerBorder = -100;
        int upperBorder = 100;
        Mat temp = Highgui.imread(source.getAbsolutePath());
        int width = Math.min(temp.cols(), temp.rows()) / 3;

        temp = OpenCvUtils.resize(temp, width);
        temp = OpenCvUtils.erode(temp);
        temp = OpenCvUtils.adaptiveThreshold(temp);
        Mat detectedLines = OpenCvUtils.getHoughLines(temp);
        Map<Integer, Integer> angles = OpenCvUtils.calculateAnglesQuantity(detectedLines);
        Map<Integer, Integer> filteredAngles = filterAngles(lowerBorder, upperBorder, angles);
        int skewAngle = getMostPopularAngle(filteredAngles);

        Mat origin = Highgui.imread(source.getAbsolutePath());
        OpenCvUtils.rotate(origin, origin, -skewAngle);
        Highgui.imwrite(target.getAbsolutePath(), origin);
        return target;
    }

    public File rotate(File source, File target, double degrease) {
        Mat origin = Highgui.imread(source.getAbsolutePath());
        OpenCvUtils.rotate(origin, origin, degrease);
        Highgui.imwrite(target.getAbsolutePath(), origin);
        return target;
    }

    private Map<Integer, Integer> filterAngles(int lowerBorder, int upperBorder, Map<Integer, Integer> angles) {
        Map<Integer, Integer> result = new HashMap<Integer, Integer>();
        for (Map.Entry<Integer, Integer> entry : angles.entrySet())
            if (entry.getKey() > lowerBorder && entry.getKey() < upperBorder)
                result.put(entry.getKey(), entry.getValue());
        return result;
    }

    private int getMostPopularAngle(Map<Integer, Integer> angles) {
        List<Map.Entry<Integer, Integer>> result = new ArrayList<Map.Entry<Integer, Integer>>(angles.entrySet());
        Collections.sort(result, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        return result.size() == 0 ? 0 : result.get(0).getKey();
    }

    int getMinRectangleArea(int cols, int rows) {
        int maxSide = Math.max(cols, rows);
        return maxSide / 40 * maxSide / 40;
    }

    public File prepareForSend(File source, File target) {
        int divider = 4;
        int blurFactor = 9;
        Size denoiseFactor = new Size(5, 5);
        Mat temp = Highgui.imread(source.getAbsolutePath(), CvType.CV_8UC1);
        Size sourceSize = new Size(temp.cols() / divider, temp.rows() / divider);
        Imgproc.resize(temp, temp, sourceSize);

        Imgproc.medianBlur(temp, temp, blurFactor);
        temp = OpenCvUtils.adaptiveThreshold(temp);
        temp = OpenCvUtils.denoise(temp, denoiseFactor);
        temp = OpenCvUtils.erode(temp);
        temp = OpenCvUtils.erode(temp);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(temp.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                return Double.compare(Imgproc.contourArea(o1), Imgproc.contourArea(o2));
            }
        });
        //F1 factor calculation
        // List<MatOfPoint> resultContours = new ArrayList<MatOfPoint>();
        double f1Factor = 0;
        double savedRecall = 0;
        List<Point> listOfPoints = new ArrayList<Point>();
        Rect bb = null;
        double fullArea = temp.cols() * temp.rows();
        for (MatOfPoint contour : contours) {
            double recall = (savedRecall + Imgproc.contourArea(contour)) / fullArea;
            List<Point> tempPointList = new ArrayList<Point>(contour.toList());
            tempPointList.addAll(listOfPoints);
            MatOfPoint matOfPoint = new MatOfPoint();
            matOfPoint.fromArray(tempPointList.toArray(new Point[tempPointList.size()]));
            Rect rect = Imgproc.boundingRect(matOfPoint);

            double precision = (fullArea - rect.area()) / fullArea;
            double newF1Factor = 2 * (recall * precision) / (recall + precision);
            if (newF1Factor >= f1Factor) {
                f1Factor = newF1Factor;
                savedRecall = recall;
                listOfPoints.addAll(new ArrayList<Point>(contour.toList()));
                //resultContours.add(contour);
                bb = rect;
                //Core.rectangle(temp, rect.tl(), rect.br(), new Scalar(0, 0, 0), 4);
            }
        }
        //Core.rectangle(temp, bb.tl(), bb.br(), new Scalar(0, 0, 0), 4);
        //System.out.println(Imgproc.contourArea(contours.get(0)));
        //Imgproc.drawContours(temp, contours, -1, new Scalar(255, 255, 255), 3);

        bb = new Rect(bb.x * divider, bb.y * divider, bb.width * divider, bb.height * divider);
        temp = Highgui.imread(source.getAbsolutePath(), CvType.CV_8UC1);

        Highgui.imwrite(target.getAbsolutePath(), temp.submat(bb));
        return target;
    }

    public File resize(File source, File target, int width, int height) {
        Mat temp = Highgui.imread(source.getAbsolutePath());
        Size newSize = OpenCvUtils.getScaledDimension(temp.size(), new Size(width, height));
        Imgproc.resize(temp, temp, newSize);
        Highgui.imwrite(target.getAbsolutePath(), temp);
        return target;
    }


    @Deprecated
    public File prepareForSend2(File source, File target) {
        Size blurSize = new Size(5, 5);
        int outline = 50;
        Mat temp = Highgui.imread(source.getAbsolutePath());
        //temp = OpenCvUtils.resize(temp,1000);
        int minRectangleArea = getMinRectangleArea(temp.cols(), temp.rows());

        temp = OpenCvUtils.adaptiveThreshold(temp);
        Imgproc.blur(temp, temp, blurSize);
        temp = OpenCvUtils.denoise(temp, new Size(2, 2));

        Highgui.imwrite(target.getAbsolutePath(), temp);

        List<Rect> rectangles = OpenCvUtils.detectLetters2(temp);
        Mat rotated = new Mat();
        Core.flip(temp.t(), rotated, 1);

        List<Rect> rotatedRectangles = OpenCvUtils.detectLetters2(rotated);
        rectangles = rectangles.size() < rotatedRectangles.size() ? rectangles : rotatedRectangles;
        temp = rectangles.size() < rotatedRectangles.size() ? temp : rotated;
        if (rectangles.size() == 0)
            throw new RuntimeException("prepareForSend(): Text not found on " + source);
        Iterator<Rect> iterator = rectangles.iterator();
        while (iterator.hasNext())
            if (iterator.next().area() < minRectangleArea)
                iterator.remove();


        List<Integer> rectanglesHeight = new ArrayList<Integer>();
        for (Rect rect : rectangles)
            rectanglesHeight.add(rect.height);
        int heightThreshold = 50;
        int height = getMostPopular(rectanglesHeight, heightThreshold);

        iterator = rectangles.iterator();
        while (iterator.hasNext()) {
            Rect rect = iterator.next();
            if (rect.height > height + heightThreshold || rect.height < height - heightThreshold / 2)
                iterator.remove();
        }


        List<Point> points = new ArrayList<Point>();
        for (Rect rect : rectangles) {
            Core.rectangle(temp, rect.tl(), rect.br(), new Scalar(0, 0, 0));
            points.add(rect.br());
            points.add(rect.tl());
        }

        Point[] points2 = points.toArray(new Point[points.size()]);

        MatOfPoint matOfPoint = new MatOfPoint();
        matOfPoint.fromArray(points2);
        Rect bb = Imgproc.boundingRect(matOfPoint);

        bb = addOutline(bb, outline, temp.size());

        //Core.rectangle(temp, resultBB.tl(), resultBB.br(), new Scalar(0, 0, 0));

        Highgui.imwrite(target.getAbsolutePath(), temp.submat(bb));
        return target;
    }

    private Rect addOutline(Rect bb, int outline, Size maxBBSize) {
        Point topLeft = bb.tl();
        Point bottomRight = bb.br();
        double newTLX = (topLeft.x - outline) < 0 ? 0 : topLeft.x - outline;
        double newTLY = (topLeft.y - outline) < 0 ? 0 : topLeft.y - outline;
        double newBRX = (bottomRight.x + outline) > maxBBSize.width ? maxBBSize.width : bottomRight.x + outline;
        double newBRY = (bottomRight.y + outline) > maxBBSize.height ? maxBBSize.height : bottomRight.y + outline;
        return new Rect(new Point(newTLX, newTLY), new Point(newBRX, newBRY));
    }

    public boolean isBill(File source) {
        int threshold = 0;
        Mat temp = Highgui.imread(source.getAbsolutePath(), Imgproc.COLOR_BGR2GRAY);
        int minRectangleArea = getMinRectangleArea(temp.cols(), temp.rows());

        List<Rect> rectangles = OpenCvUtils.detectLetters2(temp);
        Iterator<Rect> iterator = rectangles.iterator();
        while (iterator.hasNext())
            if (iterator.next().area() < minRectangleArea)
                iterator.remove();

        List<Integer> xValues = new ArrayList<Integer>();
        List<Integer> yValues = new ArrayList<Integer>();
        for (Rect rect : rectangles) {
            xValues.add(rect.x);
            // xValues.add(rect.x + rect.width);
            yValues.add(rect.y);
            //yValues.add(rect.y + rect.height);
        }
        int mostPopularX = getMostPopular(xValues, threshold);
        int mostPopularY = getMostPopular(yValues, threshold);
        List<Rect> rectanglesOnHorizontalLine = new ArrayList<Rect>();
        List<Rect> rectanglesOnVerticalLine = new ArrayList<Rect>();
        for (Rect rect : rectangles) {
            if (mostPopularY - threshold <= rect.y && mostPopularY + threshold >= rect.y)
                rectanglesOnHorizontalLine.add(rect);
            if (mostPopularX - threshold <= rect.x && mostPopularX + threshold >= rect.x)
                rectanglesOnVerticalLine.add(rect);
        }

        return rectanglesOnHorizontalLine.size() > 1 && rectanglesOnVerticalLine.size() > 1;
    }

    private int getMostPopular(List<Integer> arguments, int threshold) {
        Map<Integer, Integer> argumentCounter = new HashMap<Integer, Integer>();
        for (Integer argument : arguments) {
            int counter = argumentCounter.get(argument) == null ? 1 : argumentCounter.get(argument) + 1;
            argumentCounter.put(argument, counter);
        }
        Map<Integer, Integer> newArgumentCounter = new HashMap<Integer, Integer>();
        newArgumentCounter.putAll(argumentCounter);
        for (Map.Entry<Integer, Integer> entry : argumentCounter.entrySet())
            for (int i = entry.getKey() - threshold; i <= entry.getKey() + threshold; i++)
                if (argumentCounter.get(i) != null && i != entry.getKey()) {
                    int counter = newArgumentCounter.get(entry.getKey()) == null ? entry.getValue() : newArgumentCounter.get(entry.getKey());
                    newArgumentCounter.put(entry.getKey(), counter + argumentCounter.get(i));
                }

        List<Map.Entry<Integer, Integer>> sorted = new ArrayList<Map.Entry<Integer, Integer>>(newArgumentCounter.entrySet());
        Collections.sort(sorted, new Comparator<Map.Entry<Integer, Integer>>() {
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        return sorted.get(0).getKey();
    }

    public void cutOutRectangle(File source, File target, Circle[] circles, Size size) {
        Mat temp = Highgui.imread(source.getAbsolutePath());
        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0,
                circles[0].getX(), circles[0].getY(),
                circles[1].getX(), circles[1].getY(),
                circles[2].getX(), circles[2].getY(),
                circles[3].getX(), circles[3].getY());

        dst_mat.put(0, 0,
                0, 0,
                size.width, 0,
                size.width, size.height,
                0, size.height);

        Mat transformationMatrix = Imgproc.getPerspectiveTransform(src_mat, dst_mat);
        Imgproc.warpPerspective(temp, temp, transformationMatrix, size);
        Highgui.imwrite(target.getAbsolutePath(), temp);
    }
}
