package com.shl.checkpin.android.jobs;

import android.content.Context;
import android.os.AsyncTask;
import com.shl.checkpin.android.canvas.Circle;
import com.shl.checkpin.android.opencv.ImageProcessingService;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.io.File;


/**
 * Created by sesshoumaru on 03.01.16.
 */
public class ImageBillCutOutTask extends AsyncTask<Circle, Void, Boolean> {

    private final Context context;
    private final File source;
    private final File lowres;
    private final OnTaskCompletedListener listener;

    private ImageProcessingService processingService = new ImageProcessingService();

    public ImageBillCutOutTask(Context context, File source, File lowres, OnTaskCompletedListener listener) {
        this.context = context;
        this.source = source;
        this.lowres = lowres;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Circle... circles) {
        if (circles.length == 4) {
            correctRotationOfImage();
            correctScaleOfCircleCoords(circles);
            double width = Math.max(circles[0].distanceTo(circles[1]), circles[2].distanceTo(circles[3]));
            double height = Math.max(circles[0].distanceTo(circles[3]), circles[1].distanceTo(circles[2]));
            processingService.cutOutRectangle(source, source, circles, new Size(width, height));
            processingService.adaptiveThreshold(source, source);
            return true;
        } else {
            return false;
        }
    }

    private void correctScaleOfCircleCoords(Circle[] circles) {
        Point sourceDimension = processingService.getDimension(source);
        Point lowresDimension = processingService.getDimension(lowres);
        double scaleFactor = sourceDimension.x / lowresDimension.x;
        for (Circle circle : circles) {
            circle.setX((int) (circle.getX() * scaleFactor));
            circle.setY((int) (circle.getY() * scaleFactor));
        }
    }

    private void correctRotationOfImage() {
        double angle = processingService.getExifRotationAngle(source);
        processingService.rotate(source, source, angle);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            listener.onTaskCompleted();
        }
    }
}
